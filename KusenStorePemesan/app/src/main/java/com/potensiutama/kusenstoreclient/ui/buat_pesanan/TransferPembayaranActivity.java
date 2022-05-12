package com.potensiutama.kusenstoreclient.ui.buat_pesanan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.potensiutama.kusenstoreclient.Adapter.MyProdukPesananAdapter;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.Database.CartItem;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.SendNotificationPack.APIService;
import com.potensiutama.kusenstoreclient.SendNotificationPack.Client;
import com.potensiutama.kusenstoreclient.SendNotificationPack.Data;
import com.potensiutama.kusenstoreclient.SendNotificationPack.MyResponse;
import com.potensiutama.kusenstoreclient.SendNotificationPack.NotificationSender;
import com.potensiutama.kusenstoreclient.model.OrderModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransferPembayaranActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST_1 = 1;
    private Uri outputFileUri;
    private Uri imageUri = null;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private android.app.AlertDialog dialog;

    TextView bBatalkanPesanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_pembayaran);

        TextView txtAlamat = findViewById(R.id.txt_alamat_pengguna);
        bBatalkanPesanan = findViewById(R.id.button_batalkan_pesanan);



        txtAlamat.setText(
                "Alamat Pengiriman\n"+
                        Common.currentUser.getName() + " | " +Common.currentUser.getPhone().toString() + "\n"
                        + Common.orderModelPembayaran.getShippingAddress()
        );

        ListView listViewProdukPesanan = findViewById(R.id.list_produk_pesanan);

        MyProdukPesananAdapter adapter = new MyProdukPesananAdapter(TransferPembayaranActivity.this);
        adapter.setProdukCheckoutList(Common.orderModelPembayaran.getCartItemList());
        listViewProdukPesanan.setAdapter(adapter);

        TextView totalHargaPesanan = findViewById(R.id.total_harga_pesanan);

        totalHargaPesanan.setText("Rp"+Common.formatPrice(Common.orderModelPembayaran.getFinalPayment()));

        TextView statusPesanan = findViewById(R.id.txt_status_pesanan);

        statusPesanan.setText("Status pesanan :\n"+Common.converStatusToText(Common.orderModelPembayaran.getOrderStatus()));

        TextView metodePembayaran = findViewById(R.id.txt_metode_pembayaran);

        TextView bUploadBuktiTf = findViewById(R.id.button_upload_transfer);

        ImageView fotoBuktiTf = findViewById(R.id.foto_bukti_tf);

        if(Common.orderModelPembayaran.getOrderStatus() != 0){
            bBatalkanPesanan.setVisibility(View.GONE);
        }

        bBatalkanPesanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDibatalkan(Common.orderModelPembayaran);
            }
        });

        if(Common.orderModelPembayaran.isCod()){
            metodePembayaran.setText("Metode pembayaran : Bayar ditempat (COD)");
            bUploadBuktiTf.setVisibility(View.GONE);


        }else{
            String currentString = Common.orderModelPembayaran.getRekeningPembayaran();
            String[] separated = currentString.split("-");
            //Log.d("Rek",separated);
            metodePembayaran.setText("Metode pembayaran : Transfer Bank"+
                    "\nNama Bank : "+ separated[0]+
                    "\nNama Penerima : "+separated[1]+
                    "\nNomor Rekening : "+separated[2]
                    );

        }

        if(Common.orderModelPembayaran.getBuktiTransfer() != null){
            Glide.with(TransferPembayaranActivity.this).load(Common.orderModelPembayaran.getBuktiTransfer()).into(fotoBuktiTf);
            bUploadBuktiTf.setVisibility(View.GONE);
        }else{
            fotoBuktiTf.setVisibility(View.GONE);
        }

        bUploadBuktiTf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadBuktiTF();
            }
        });

        ImageView imgBack = findViewById(R.id.images_back);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void UploadBuktiTF() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(TransferPembayaranActivity.this).setCancelable(false).build();

        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Pilih Foto Bukti Transfer"),PICK_IMAGE_REQUEST_1);
        Toast.makeText(TransferPembayaranActivity.this, "bukti transfer", Toast.LENGTH_SHORT).show();
   */
        openImageIntent();
    }

    private void openImageIntent() {

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = UUID.randomUUID().toString();
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST_1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST_1) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                if (isCamera) {
                    imageUri = outputFileUri;
                } else {
                    imageUri = data == null ? null : data.getData();
                }
                if(data != null && data.getData() != null){
                    SimpanDatabase();
                }
            }
        }

    }

    private void SimpanDatabase(){
        dialog.setMessage("Uploading...");
        dialog.show();
        OrderModel orderModel = Common.orderModelPembayaran;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String unique_name = UUID.randomUUID().toString();
        final StorageReference ImageName = storageReference.child("images/"+unique_name);
        ImageName.putFile(imageUri)
                .addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ImageName.getDownloadUrl().addOnSuccessListener(
                                        new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                dialog.dismiss();
                                                orderModel.setBuktiTransfer(uri.toString());
                                                orderModel.setOrderStatus(1);
                                                Map<String,Object> updateData = new HashMap<>();
                                                updateData.put(orderModel.getKey(),orderModel);

                                                FirebaseDatabase.getInstance()
                                                        .getReference(Common.ORDER_REF)
                                                        .updateChildren(updateData)
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(TransferPembayaranActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }).addOnCompleteListener(task -> {
                                                    if(task.isSuccessful()){
                                                        sendNotifications(orderModel.getKey(),orderModel.getUserName());
                                                        Toast.makeText(TransferPembayaranActivity.this, "Sukses!", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });

                                            }
                                        }
                                );
                            }
                        }
                ).addOnProgressListener(taskSnapshot -> {
            double progress = (100* taskSnapshot.getBytesTransferred() /taskSnapshot.getTotalByteCount());
            dialog.setMessage(new StringBuilder("Uploading ")
                    .append(" : ")
                    .append(progress).append("%"));
        });

    }

    public void sendNotifications(String nomorPesanan,String namaPemesan) {

        APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        String TOPIC = Common.createTopicOrder();
        Data data = new Data("Pesanan "+nomorPesanan, namaPemesan+" telah melakukan pembayaran"+"\nStatus : menunggu konfirmasi toko");
        NotificationSender sender = new NotificationSender(data, TOPIC);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(TransferPembayaranActivity.this, "Gagal mengirim notifikasi ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

    private void updateDibatalkan(OrderModel orderModel){
        if(!TextUtils.isEmpty(orderModel.getKey())){
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("orderStatus",-1);

            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> Toast.makeText(TransferPembayaranActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(aVoid -> {

                        android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(TransferPembayaranActivity.this).setCancelable(false).build();
                        dialog.show();
                        finish();
                       // sendNotifications(orderModel,"Update Pesanan "+orderModel.getKey(),Common.convertStatusToString(status));
                        dialog.dismiss();
                    });
        }else{
            Toast.makeText(TransferPembayaranActivity.this, "Nomor pesanan tidak boleh kosong!", Toast.LENGTH_SHORT).show();
        }
    }
}