package com.potensiutama.kusenstoreadmin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.potensiutama.kusenstoreadmin.EventBus.KategoriClick;
import com.potensiutama.kusenstoreadmin.EventBus.ChangeMenuClick;
import com.potensiutama.kusenstoreadmin.EventBus.PrintOrderEvent;
import com.potensiutama.kusenstoreadmin.EventBus.ToastEvent;
import com.potensiutama.kusenstoreadmin.adapter.PdfDocumentAdapter;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.common.PDFUtils;
import com.potensiutama.kusenstoreadmin.model.OrderModel;

import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.potensiutama.kusenstoreadmin.ui.datakurir.DataKurirActivity;
import com.potensiutama.kusenstoreadmin.ui.metodepembayaran.MetodePembayaranActivity;
import com.potensiutama.kusenstoreadmin.ui.tokosaya.PengaturanTokoSayaActivity;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick=-1;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order,R.id.nav_data_penjualan,R.id.nav_produk,R.id.nav_informasi_toko,
        R.id.nav_metode_pembayaran,R.id.nav_data_kurir)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("", Common.currentServerUser.getName(),txt_user);

        menuClick = R.id.nav_category;

        checkIsOpenFromActivity();
    }

    private void init() {

        subscribeToTopic(Common.createTopicOrder());

        dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage("Mohon tunggu...")
                .create();
    }

    private void checkIsOpenFromActivity() {
        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,false);
        if(isOpenFromNewOrder){
            navController.popBackStack();
            navController.navigate(R.id.nav_order);
            menuClick = R.id.nav_order;
        }
    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    if(!task.isSuccessful())
                        Toast.makeText(this, "Gagal "+task.isSuccessful(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCategoryClick(KategoriClick event){
        if(event.isSuccess()){
            if(menuClick != R.id.nav_food_list){
                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;
            }
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event){
        if(event.getAction() == Common.ACTION.CREATE){
            Toast.makeText(this, "Tambah data berhasil!", Toast.LENGTH_SHORT).show();
        }else if(event.getAction() == Common.ACTION.UPDATE){
            Toast.makeText(this, "Ubah data berhasil!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Hapus data berhasil!", Toast.LENGTH_SHORT).show();
        }
        EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodList()));
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event){
        if(event.isFromFoodList()){
            navController.popBackStack(R.id.nav_category,true);
            navController.navigate(R.id.nav_category);
        }else{
            navController.popBackStack(R.id.nav_food_list,true);
            navController.navigate(R.id.nav_food_list);
        }
        menuClick = -1;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch (menuItem.getItemId()){
            case R.id.nav_category:
            //    navController.popBackStack();
                navController.navigate(R.id.nav_category);
                break;
            case R.id.nav_produk:
             //   navController.popBackStack();
                navController.navigate(R.id.nav_produk);
                break;
            case R.id.nav_order:
               //navController.popBackStack();
                navController.navigate(R.id.nav_order);
                break;

            case R.id.nav_data_penjualan:
                //navController.popBackStack();
                navController.navigate(R.id.nav_data_penjualan);
                break;
            case R.id.nav_informasi_toko:
                Intent intent = new Intent(HomeActivity.this, PengaturanTokoSayaActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_metode_pembayaran:
                Intent intent1 = new Intent(HomeActivity.this, MetodePembayaranActivity.class);
                startActivity(intent1);
                break;
            case R.id.nav_data_kurir:
                Intent intent2 = new Intent(HomeActivity.this, DataKurirActivity.class);
                startActivity(intent2);
                break;

            case R.id.nav_sign_out:
                signOut();
                break;
                default:
                    menuClick = -1;
                    break;
        }
        menuClick = menuItem.getItemId();
        return true;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Apakah anda yakin ingin keluar dari aplikasi ini?")
                .setNegativeButton("Batalkan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                Common.selectedProduk = null;
                Common.categorySelected = null;
                Common.currentServerUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                System.exit(0);

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onPrintEventListener(PrintOrderEvent event){
        createPDFFile(event.getPath(),event.getOrderModel());
    }

    private void createPDFFile(String path, OrderModel orderModel) {
        dialog.show();
        if(new File(path).exists())
            new File(path).delete();
        try {
            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(path));

            document.open();

            //Setting
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor("Elder");
            document.addCreator(Common.currentServerUser.getName());

            //Font
            BaseColor colorAccent = new BaseColor(0,153,204,255);
            float fontSize = 20.0f;

            //Custom Font
            BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf","UTF-8",BaseFont.EMBEDDED);

            //Title Document
            Font titleFont = new Font(fontName,36.0f,Font.NORMAL,BaseColor.BLACK);
            PDFUtils.addNewItem(document,"Detail Order", Element.ALIGN_CENTER,titleFont);

            Font orderNumberFormat = new Font(fontName,fontSize,Font.NORMAL,colorAccent);
            PDFUtils.addNewItem(document,"Nomor pemesanan:",Element.ALIGN_LEFT,orderNumberFormat);
            Font orderNumberValueFormat = new Font(fontName,fontSize,Font.NORMAL,BaseColor.BLACK);
            PDFUtils.addNewItem(document,orderModel.getKey(),Element.ALIGN_LEFT,orderNumberValueFormat);

            PDFUtils.addLineSeperator(document);

            PDFUtils.addNewItem(document,"Tanggal",Element.ALIGN_LEFT,orderNumberFormat);
            PDFUtils.addNewItem(document,new SimpleDateFormat("dd/MM/yyyy").format(orderModel.getCreateDate()),Element.ALIGN_LEFT,orderNumberValueFormat);

            PDFUtils.addLineSeperator(document);

            PDFUtils.addNewItem(document,"Nama Pelanggan",Element.ALIGN_LEFT,orderNumberFormat);
            PDFUtils.addNewItem(document,orderModel.getShippingAddress(),Element.ALIGN_LEFT,orderNumberValueFormat);

            PDFUtils.addLineSeperator(document);

            //Produk Dan Detail
            PDFUtils.addLineSpace(document);
            PDFUtils.addNewItem(document,"Detail Produk",Element.ALIGN_CENTER,titleFont);
            PDFUtils.addLineSeperator(document);

            //RxJava Produk
            Observable.fromIterable(orderModel.getCartItemList())
                    //.flatMap(cartItem -> Common.getBitmapFromUrl(HomeActivity.this,cartItem,document))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItem -> { //On Next

                        //Nama Makanan/minuman
                        PDFUtils.addNewItemWithLeftAndRight(document,cartItem.getProdukName(),
                                (""),
                                titleFont,
                                orderNumberValueFormat);

                        //Harga
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                new StringBuilder()
                                        .append(cartItem.getProdukQuantity())
                                .append("*")
                                .append(Common.formatPrice(cartItem.getProdukPrice()))
                                .toString(),
                                new StringBuilder()
                                .append(Common.formatPrice(cartItem.getProdukQuantity() * (cartItem.getProdukPrice())))
                                .toString(),
                                titleFont,
                                orderNumberValueFormat);

                        PDFUtils.addLineSeperator(document);



                    },throwable -> { //On error
                        dialog.dismiss();
                        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    },() -> { //On Complete

                        PDFUtils.addLineSpace(document);
                        PDFUtils.addLineSpace(document);

                        PDFUtils.addNewItemWithLeftAndRight(document, "Total ",
                                new StringBuilder()
                        .append(Common.formatPrice(orderModel.getTotalPayment())).toString(),
                                titleFont,
                                titleFont);

                        document.close();
                        dialog.dismiss();
                        Toast.makeText(this, "Berhasil!", Toast.LENGTH_SHORT).show();

                        printPDF();

                    });





        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }catch (DocumentException e){
            e.printStackTrace();
        }
    }

    private void printPDF() {
        PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(this,new StringBuilder(Common.getAppPath(this))
            .append(Common.FILE_PRINT).toString());
            printManager.print("Document",printDocumentAdapter,new PrintAttributes.Builder().build());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
