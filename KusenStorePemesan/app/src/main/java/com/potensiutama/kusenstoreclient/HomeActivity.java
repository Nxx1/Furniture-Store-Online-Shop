package com.potensiutama.kusenstoreclient;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.andremion.counterfab.CounterFab;
import com.potensiutama.kusenstoreclient.Adapter.PdfDocumentAdapter;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.Common.PDFUtils;
import com.potensiutama.kusenstoreclient.Database.CartDataSource;
import com.potensiutama.kusenstoreclient.Database.CartDatabase;
import com.potensiutama.kusenstoreclient.Database.LocalCartDataSource;
import com.potensiutama.kusenstoreclient.EventBus.CategoryClick;
import com.potensiutama.kusenstoreclient.EventBus.CounterCartEvent;
import com.potensiutama.kusenstoreclient.EventBus.FoodItemClick;
import com.potensiutama.kusenstoreclient.EventBus.HideFABCart;
import com.potensiutama.kusenstoreclient.EventBus.MenuItemBack;
import com.potensiutama.kusenstoreclient.EventBus.PrintOrderEvent;
import com.potensiutama.kusenstoreclient.model.OrderModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    public  NavController navController;

    private CartDataSource cartDataSource;

    android.app.AlertDialog dialog;

    int menuClickId = 1;

    @BindView(R.id.fab)
    CounterFab fab;

    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        ButterKnife.bind(this);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
                navController.navigate(R.id.nav_cart)
        );
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_detail,
                R.id.nav_view_orders, R.id.nav_cart, R.id.nav_food_list,R.id.nav_akun_saya)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Common.homeActivity = HomeActivity.this;
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hi, ",Common.currentUser.getName(),txt_user);

        countCartItem();

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()){
            case R.id.nav_home:
                navController.navigate(R.id.nav_home);
                break;
            case R.id.nav_menu:
                navController.navigate(R.id.nav_menu);
                break;
            case R.id.nav_cart:
                navController.navigate(R.id.nav_cart);
                break;
            case R.id.nav_view_orders:
                navController.navigate(R.id.nav_view_orders);
                break;
            case R.id.nav_akun_saya:
                navController.navigate(R.id.nav_akun_saya);
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
        }

        menuClickId = item.getItemId();

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

                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //EventBus

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
    public void onCategorySelected(CategoryClick event){
        if(event.isSuccess()){
            navController.navigate(R.id.nav_food_list);
            //Toast.makeText(this, "Click to "+event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
        }

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event){
        if(event.isSuccess()){
            navController.navigate(R.id.nav_food_detail);

        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event){
        if(event.isHidden()){
            fab.hide();
        }else{
            fab.show();
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event){
        if(event.isSuccess()){
            countCartItem();
        }
    }

    private void countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>(){
                    @Override
                    public void onSubscribe(Disposable d){

                    }

                    @Override
                    public void onSuccess(Integer integer){
                        fab.setCount(integer);
                    }

                    @Override
                    public void onError(Throwable e){
                        if(!e.getMessage().contains("empty")){
                            Toast.makeText(HomeActivity.this,"[Isi Keranjang] "+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }else {
                            fab.setCount(0);
                        }
                    }
                });
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void countCartAgain(CounterCartEvent event){
        if(event.isSuccess()){
            countCartItem();
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event){
        menuClickId = -1;
        if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
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
            Rectangle pagesize = new Rectangle(57, 47);
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            //Setting
            document.setPageSize(pagesize);
            document.addCreationDate();
            document.addAuthor("Elder");
            document.addCreator(Common.currentUser.getName());

            //Font
            BaseColor colorAccent = new BaseColor(0,153,204,255);
            float fontSize = 20.0f;

            //Custom Font
            BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf","UTF-8",BaseFont.EMBEDDED);

            //Title Document
            Font titleFont = new Font(fontName,36.0f,Font.NORMAL,BaseColor.BLACK);
            PDFUtils.addLineSpace(document);
            PDFUtils.addNewItem(document,"ELDER [Electronic Food Ordering]",Element.ALIGN_CENTER,titleFont);
            PDFUtils.addLineSeperator(document);

            PDFUtils.addNewItem(document,"Detail Order", Element.ALIGN_CENTER,titleFont);


            Font orderNumberFormat = new Font(fontName,fontSize,Font.NORMAL,colorAccent);
            //PDFUtils.addNewItem(document,"Nomor pemesanan:",Element.ALIGN_LEFT,orderNumberFormat);
            Font orderNumberValueFormat = new Font(fontName,fontSize,Font.NORMAL,BaseColor.BLACK);
            //PDFUtils.addNewItem(document,orderModel.getKey(),Element.ALIGN_LEFT,orderNumberValueFormat);

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
                                        .append("Rp")
                                        .append(Common.formatPrice(cartItem.getProdukPrice()))
                                        .toString(),
                                new StringBuilder()
                                        .append("Rp")
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
                                        .append("Rp")
                                        .append(Common.formatPrice(orderModel.getTotalPayment())).toString(),
                                titleFont,
                                titleFont);

                        PDFUtils.addNewItemWithLeftAndRight(document, "Tunai ",
                                new StringBuilder()
                                        .append("Rp")
                                        .append(Common.formatPrice(orderModel.getJumlahBayar())).toString(),
                                titleFont,
                                titleFont);

                        PDFUtils.addLineSeperator(document);

                        PDFUtils.addNewItemWithLeftAndRight(document, "Kembali ",
                                new StringBuilder()
                                        .append("Rp")
                                        .append(Common.formatPrice(orderModel.getKembali())).toString(),
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

    public void printPDF() {
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
