package com.potensiutama.kusenstoreadmin.ui.order;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.SendNotificationPack.APIService;
import com.potensiutama.kusenstoreadmin.SendNotificationPack.Client;
import com.potensiutama.kusenstoreadmin.SendNotificationPack.Data;
import com.potensiutama.kusenstoreadmin.SendNotificationPack.MyResponse;
import com.potensiutama.kusenstoreadmin.SendNotificationPack.NotificationSender;
import com.potensiutama.kusenstoreadmin.adapter.MyProdukPesananAdapter;
import com.potensiutama.kusenstoreadmin.adapter.PdfDocumentAdapter;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.common.PDFUtils;
import com.potensiutama.kusenstoreadmin.model.FCMSendData;
import com.potensiutama.kusenstoreadmin.model.OrderModel;
import com.potensiutama.kusenstoreadmin.model.ServerUserModel;
import com.potensiutama.kusenstoreadmin.model.TokenModel;
import com.potensiutama.kusenstoreadmin.remote.IFCMService;
import com.potensiutama.kusenstoreadmin.remote.RetrofitFCMClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailPesananActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST_1 = 1;

    private Uri imageUri = null;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private android.app.AlertDialog dialog;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;


    private AlertDialog dialogCetak;

    List<String> daftarKurir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pesanan);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        dialogCetak = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage("Mohon tunggu...")
                .create();


        TextView txtAlamat = findViewById(R.id.txt_alamat_pengguna);
        TextView txtInfoPemesan = findViewById(R.id.txt_informasi_pemesan);

        txtInfoPemesan.setText("Data Pemesan\n"
        +"Nama : "+Common.orderModelPembayaran.getUserName() +
                        "\nNomor HP : " +Common.orderModelPembayaran.getUserPhone().toString()
        );

        txtAlamat.setText(
                "Alamat Pengiriman\n"+ Common.orderModelPembayaran.getShippingAddress()
        );

        ListView listViewProdukPesanan = findViewById(R.id.list_produk_pesanan);

        MyProdukPesananAdapter adapter = new MyProdukPesananAdapter(DetailPesananActivity.this);
        adapter.setProdukCheckoutList(Common.orderModelPembayaran.getCartItemList());
        listViewProdukPesanan.setAdapter(adapter);

        TextView totalHargaPesanan = findViewById(R.id.total_harga_pesanan);

        totalHargaPesanan.setText("Rp"+Common.formatPrice(Common.orderModelPembayaran.getFinalPayment()));

        TextView statusPesanan = findViewById(R.id.txt_status_pesanan);

        statusPesanan.setText("Status pesanan :\n"+Common.convertStatusToString(Common.orderModelPembayaran.getOrderStatus()));

        TextView metodePembayaran = findViewById(R.id.txt_metode_pembayaran);

        TextView bUpdatePesanan = findViewById(R.id.button_update_pesanan);

        ImageView fotoBuktiTf = findViewById(R.id.foto_bukti_tf);

        if(Common.orderModelPembayaran.isCod()){
            metodePembayaran.setText("Metode pembayaran : Bayar ditempat (COD)");
        }else{
            String currentString = Common.orderModelPembayaran.getRekeningPembayaran();
            String[] separated = currentString.split("-");
            metodePembayaran.setText("Metode pembayaran : Transfer Bank"+
                    "\nNama Bank : "+ separated[0]+
                    "\nNama Penerima : "+separated[1]+
                    "\nNomor Rekening : "+separated[2]
            );
        }

        if(Common.orderModelPembayaran.getBuktiTransfer() != null){
            Glide.with(DetailPesananActivity.this).load(Common.orderModelPembayaran.getBuktiTransfer()).into(fotoBuktiTf);
        }else{
            fotoBuktiTf.setVisibility(View.GONE);
        }


        ImageView imgBack = findViewById(R.id.images_back);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        bUpdatePesanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog(Common.orderModelPembayaran);
            }
        });

        TextView bPilihKurir = findViewById(R.id.button_pilih_kurir);

        FloatingActionButton fabCetak = findViewById(R.id.fab_cetak_invoice);

        bPilihKurir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPilihKurir(Common.orderModelPembayaran);
            }
        });

        fabCetak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPDFFile(new StringBuilder(Common.getAppPath(getApplicationContext()))
                        .append(Common.FILE_PRINT).toString(),Common.orderModelPembayaran);
            }
        });

        LoadDaftarKurir();

    }

    private void LoadDaftarKurir(){
        TextView namaKurir = findViewById(R.id.txt_nama_kurir);
        namaKurir.setVisibility(View.GONE);
        DatabaseReference kurirRef = FirebaseDatabase.getInstance().getReference(Common.KURIR_REF);
        kurirRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                daftarKurir = new ArrayList<String>();

                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    ServerUserModel kurirModel = itemSnapshot.getValue(ServerUserModel.class);
                    String data = kurirModel.getPhone() + "-" +kurirModel.getName();
                    daftarKurir.add(data);
                    if(kurirModel.getPhone().equals(Common.orderModelPembayaran.getIdKurir())){
                        namaKurir.setText("Kurir : "+kurirModel.getName() + " ("+kurirModel.getPhone()+")");
                        namaKurir.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DetailPesananActivity.this, ""+databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(OrderModel orderModel) {
        View layout_dialog;
        AlertDialog.Builder builder;
        layout_dialog = LayoutInflater.from(DetailPesananActivity.this)
                .inflate(R.layout.layout_dialog_update_pesanan,null);
        builder = new AlertDialog.Builder(DetailPesananActivity.this)
                .setView(layout_dialog);

        Button btn_ok = (Button) layout_dialog.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) layout_dialog.findViewById(R.id.btn_cancel);

        RadioButton rdi_proses_pembayaran = (RadioButton) layout_dialog.findViewById(R.id.rdi_proses_pembayaran);
        RadioButton rdi_konfirmasi_toko = (RadioButton) layout_dialog.findViewById(R.id.rdi_konfirmasi_toko);
        RadioButton rdi_dikemas = (RadioButton) layout_dialog.findViewById(R.id.rdi_dikemas);
        RadioButton rdi_dikirim = (RadioButton) layout_dialog.findViewById(R.id.rdi_dikirim);
        RadioButton rdi_selesai = (RadioButton) layout_dialog.findViewById(R.id.rdi_selesai);
        RadioButton rdi_cancelled = (RadioButton) layout_dialog.findViewById(R.id.rdi_cancelled);

        TextView txt_status = (TextView) layout_dialog.findViewById(R.id.txt_status);

        txt_status.setText(Common.convertStatusToString(orderModel.getOrderStatus()));

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);


        switch (orderModel.getOrderStatus()){
            case 0 :
                rdi_proses_pembayaran.setVisibility(View.VISIBLE);
                rdi_proses_pembayaran.setChecked(true);
                rdi_konfirmasi_toko.setVisibility(View.GONE);
                rdi_dikemas.setVisibility(View.GONE);
                rdi_dikirim.setVisibility(View.GONE);
                rdi_selesai.setVisibility(View.GONE);
                rdi_cancelled.setVisibility(View.VISIBLE);
                break;
            case 1:
                rdi_proses_pembayaran.setVisibility(View.GONE);
                rdi_konfirmasi_toko.setVisibility(View.GONE);
                rdi_dikemas.setVisibility(View.VISIBLE);
                rdi_dikemas.setChecked(true);
                rdi_dikirim.setVisibility(View.GONE);
                rdi_selesai.setVisibility(View.GONE);
                rdi_cancelled.setVisibility(View.VISIBLE);
                break;
            case 2:
                rdi_proses_pembayaran.setVisibility(View.GONE);
                rdi_konfirmasi_toko.setVisibility(View.GONE);
                rdi_dikemas.setVisibility(View.GONE);
                rdi_dikirim.setVisibility(View.VISIBLE);
                rdi_dikirim.setChecked(true);
                rdi_selesai.setVisibility(View.GONE);
                rdi_cancelled.setVisibility(View.VISIBLE);
                break;
            case 3:
                rdi_proses_pembayaran.setVisibility(View.GONE);
                rdi_konfirmasi_toko.setVisibility(View.GONE);
                rdi_dikemas.setVisibility(View.GONE);
                rdi_dikirim.setVisibility(View.GONE);
                rdi_selesai.setVisibility(View.VISIBLE);
                rdi_selesai.setChecked(true);
                rdi_cancelled.setVisibility(View.VISIBLE);
                break;
        }


        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_ok.setOnClickListener(view -> {
            dialog.dismiss();
            if(rdi_cancelled != null && rdi_cancelled.isChecked())
                updateOrder(orderModel,-1);
            else if(rdi_proses_pembayaran != null && rdi_proses_pembayaran.isChecked())
                updateOrder(orderModel,0);
            else if(rdi_konfirmasi_toko != null && rdi_konfirmasi_toko.isChecked())
                updateOrder(orderModel,1);
            else if(rdi_dikemas != null && rdi_dikemas.isChecked())
                updateOrder(orderModel,2);
            else if(rdi_dikirim != null && rdi_dikirim.isChecked())
                updateOrder(orderModel,3);
            else if(rdi_selesai != null && rdi_selesai.isChecked())
                updateOrder(orderModel,4);
        });

    }

    private void updateOrder(OrderModel orderModel,int status){
        if(!TextUtils.isEmpty(orderModel.getKey())){
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("orderStatus",status);

            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> Toast.makeText(DetailPesananActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(aVoid -> {

                        android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(DetailPesananActivity.this).setCancelable(false).build();
                        dialog.show();

                        sendNotifications(orderModel,"Update Pesanan "+orderModel.getKey(),Common.convertStatusToString(status));
                        dialog.dismiss();
                    });
        }else{
            Toast.makeText(DetailPesananActivity.this, "Nomor pesanan tidak boleh kosong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateKurir(OrderModel orderModel,String id_kurir){
        if(!TextUtils.isEmpty(orderModel.getKey())){
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("orderStatus",3);
            updateData.put("idKurir",id_kurir);

            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> Toast.makeText(DetailPesananActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(aVoid -> {

                        android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(DetailPesananActivity.this).setCancelable(false).build();
                        dialog.show();

                        //sendNotifications(orderModel,"Update Pesanan "+orderModel.getKey(),Common.convertStatusToString(status));
                        dialog.dismiss();
                        finish();
                    });
        }else{
            Toast.makeText(DetailPesananActivity.this, "Nomor pesanan tidak boleh kosong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void createPDFFile(String path, OrderModel orderModel) {
        dialogCetak.show();
        if(new File(path).exists())
            new File(path).delete();
        try {
            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(path));

            document.open();

            //Setting
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor("Baut Jaya");
            document.addCreator(Common.currentServerUser.getName());

            //Font
            BaseColor colorAccent = new BaseColor(0,153,204,255);
            float fontSize = 20.0f;

            //Custom Font
            BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf","UTF-8",BaseFont.EMBEDDED);

            //Title Document
            Font titleFont = new Font(fontName,36.0f,Font.NORMAL,BaseColor.BLACK);
            Font produkFont = new Font(fontName,18f,Font.NORMAL,BaseColor.BLACK);
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
            PDFUtils.addNewItem(document,orderModel.getUserName(),Element.ALIGN_LEFT,orderNumberValueFormat);

            PDFUtils.addLineSeperator(document);

            PDFUtils.addNewItem(document,"Nomor Handphone",Element.ALIGN_LEFT,orderNumberFormat);
            PDFUtils.addNewItem(document,orderModel.getUserPhone(),Element.ALIGN_LEFT,orderNumberValueFormat);

            PDFUtils.addLineSeperator(document);

            PDFUtils.addNewItem(document,"Alamat",Element.ALIGN_LEFT,orderNumberFormat);
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

                        //Nama Produk
                        PDFUtils.addNewItemWithLeftAndRight(document,cartItem.getProdukName(),
                                (""),
                                produkFont,
                                orderNumberValueFormat);

                        //Harga
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                new StringBuilder()
                                        .append(cartItem.getProdukQuantity())
                                        .append("*Rp")
                                        .append(Common.formatPrice(cartItem.getProdukPrice()))
                                        .toString(),
                                new StringBuilder("Rp")
                                        .append(Common.formatPrice(cartItem.getProdukQuantity() * (cartItem.getProdukPrice())))
                                        .toString(),
                                produkFont,
                                orderNumberValueFormat);

                        PDFUtils.addLineSeperator(document);



                    },throwable -> { //On error
                        dialogCetak.dismiss();
                        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    },() -> { //On Complete

                        PDFUtils.addLineSpace(document);
                        PDFUtils.addLineSpace(document);

                        PDFUtils.addNewItemWithLeftAndRight(document, "Total (Termasuk Ongkir)",
                                new StringBuilder("Rp")
                                        .append(Common.formatPrice(orderModel.getTotalPayment())).toString(),
                                titleFont,
                                titleFont);

                        document.close();
                        dialogCetak.dismiss();
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
            Toast.makeText(this, ""+ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    private void showPilihKurir(OrderModel orderModel){
        View layout_dialog;
        AlertDialog.Builder builder;
        layout_dialog = LayoutInflater.from(DetailPesananActivity.this)
                .inflate(R.layout.layout_dialog_update_kurir,null);
        builder = new AlertDialog.Builder(DetailPesananActivity.this)
                .setView(layout_dialog);

        Button btn_ok = (Button) layout_dialog.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) layout_dialog.findViewById(R.id.btn_cancel);

        Spinner spKurir = layout_dialog.findViewById(R.id.sp_daftar_kurir);

        ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(DetailPesananActivity.this, android.R.layout.simple_spinner_item, daftarKurir);
        areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKurir.setAdapter(areasAdapter);



        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_ok.setOnClickListener(view -> {
            dialog.dismiss();
            String currentString = spKurir.getSelectedItem().toString();
            String[] separated = currentString.split("-");
            orderModel.setIdKurir(separated[0]);
            orderModel.setOrderStatus(3);
            updateKurir(orderModel,separated[0]);
        });
    }

    public void sendNotifications(OrderModel orderModel, String title,String body) {
        APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference(Common.TOKEN_REF).child(orderModel.getUserId());
        tokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                Data data = new Data(title, body);
                NotificationSender sender = new NotificationSender(data, tokenModel.getToken());
                apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        if (response.code() == 200) {
                            if (response.body().success != 1) {
                                Toast.makeText(DetailPesananActivity.this, "Gagal mengirim notifikasi ", Toast.LENGTH_LONG);
                            }
                        }

                        finish();
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}