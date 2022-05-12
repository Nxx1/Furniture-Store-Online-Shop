package com.potensiutama.kusenstoredriver.ui.order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.potensiutama.kusenstoredriver.R;
import com.potensiutama.kusenstoredriver.adapter.MyProdukPesananAdapter;
import com.potensiutama.kusenstoredriver.common.Common;
import com.potensiutama.kusenstoredriver.model.FCMSendData;
import com.potensiutama.kusenstoredriver.model.OrderModel;
import com.potensiutama.kusenstoredriver.model.TokenModel;
import com.potensiutama.kusenstoredriver.remote.IFCMService;
import com.potensiutama.kusenstoredriver.remote.RetrofitFCMClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetailPesananActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private android.app.AlertDialog dialog;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pesanan);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        TextView txtAlamat = findViewById(R.id.txt_alamat_pengguna);
        TextView txtInfoPemesan = findViewById(R.id.txt_informasi_pemesan);

        txtInfoPemesan.setText("Data Pemesan\n"
                +"Nama : "+ Common.orderModelPembayaran.getUserName() +
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

        if(Common.orderModelPembayaran.isCod()){
            metodePembayaran.setText("Metode pembayaran : Bayar ditempat (COD)");
        }else{
            String currentString = Common.orderModelPembayaran.getRekeningPembayaran();
            String[] separated = currentString.split("-");
            metodePembayaran.setText("Metode pembayaran : Transfer Bank");
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

        TextView bRuteKirim = findViewById(R.id.button_rute_kirim);

        bRuteKirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri navigationIntentUri = Uri.parse("google.navigation:q=" + Common.orderModelPembayaran.getLat() +"," + Common.orderModelPembayaran.getLng());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });

        if(Common.orderModelPembayaran.getOrderStatus() == 4){
            bUpdatePesanan.setVisibility(View.GONE);
            bRuteKirim.setVisibility(View.GONE);
        }


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

        RadioButton rdi_dikirim = (RadioButton) layout_dialog.findViewById(R.id.rdi_dikirim);
        RadioButton rdi_selesai = (RadioButton) layout_dialog.findViewById(R.id.rdi_selesai);
        RadioButton rdi_cancelled = (RadioButton) layout_dialog.findViewById(R.id.rdi_cancelled);

        TextView txt_status = (TextView) layout_dialog.findViewById(R.id.txt_status);

        txt_status.setText(new StringBuilder("Status Pesanan (")
                .append(Common.convertStatusToString(orderModel.getOrderStatus()))
                .append(")"));

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);


        switch (orderModel.getOrderStatus()){
            case 2:
                rdi_dikirim.setVisibility(View.VISIBLE);
                rdi_dikirim.setChecked(true);
                rdi_selesai.setVisibility(View.GONE);
                rdi_cancelled.setVisibility(View.VISIBLE);
                break;
            case 3:
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

                        FirebaseDatabase.getInstance()
                                .getReference(Common.TOKEN_REF)
                                .child(orderModel.getUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                                            Map<String,String> notiData = new HashMap<>();
                                            notiData.put(Common.NOTI_TITLE,"Data pesanan telah diupdate");
                                            notiData.put(Common.NOTI_CONTENT,new StringBuilder("Update pesanan [")
                                                    .append(orderModel.getShippingAddress())
                                                    .append("] : ")
                                                    .append(Common.convertStatusToString(status)).toString());

                                            FCMSendData sendData = new FCMSendData(tokenModel.getToken(),notiData);

                                            compositeDisposable.add(ifcmService.sendNotification(sendData)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(fcmResponse -> {
                                                        dialog.dismiss();
                                                        if(fcmResponse.getSuccess() == 1){
                                                            Toast.makeText(DetailPesananActivity.this, "Update pemesanan berhasil!", Toast.LENGTH_SHORT).show();
                                                        }else{
                                                            Toast.makeText(DetailPesananActivity.this, "Update pemesanan berhasil, tetapi gagal mengirim notifikasi!", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }, throwable -> {
                                                        Toast.makeText(DetailPesananActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }));

                                            finish();

                                        }else{
                                            dialog.dismiss();
                                            Toast.makeText(DetailPesananActivity.this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        dialog.dismiss();
                                        Toast.makeText(DetailPesananActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
        }else{
            Toast.makeText(DetailPesananActivity.this, "Nomor pesanan tidak boleh kosong!", Toast.LENGTH_SHORT).show();
        }
    }
}