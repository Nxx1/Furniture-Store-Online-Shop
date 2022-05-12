package com.potensiutama.kusenstoreadmin.ui.list_produk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.potensiutama.kusenstoreadmin.EventBus.ToastEvent;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.KategoryModel;
import com.potensiutama.kusenstoreadmin.model.ProdukModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

public class TambahProdukActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST_1 = 1;
    private static final int PICK_IMAGE_REQUEST_2 = 2;
    private static final int PICK_IMAGE_REQUEST_3 = 3;
    private static final int PICK_IMAGE_REQUEST_4 = 4;
    private static final int PICK_IMAGE_REQUEST_5 = 5;

    private Uri imageUri1 = null;
    private Uri imageUri2 = null;
    private Uri imageUri3 = null;
    private Uri imageUri4 = null;
    private Uri imageUri5 = null;

    private ArrayList<Uri> listImageUri = new ArrayList<Uri>();

    private ImageView imagePicker1,imagePicker2,imagePicker3,imagePicker4,imagePicker5;

    private EditText edtNamaProduk,edtDeskripsiProduk,edtHargaProduk,edtStokBarang,edtBeratBarang,edtLebarBarang,edtPanjangBarang,edtTinggiBarang,edtOngkirPerkm;
    private Switch swPreoder,swUnggulan;
    private Spinner spinnerListKategori;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private android.app.AlertDialog dialog;

    private TextView bSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_produk);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(TambahProdukActivity.this).setCancelable(false).build();


        imagePicker1 = findViewById(R.id.imagepicker1);
        imagePicker2 = findViewById(R.id.imagepicker2);
        imagePicker3 = findViewById(R.id.imagepicker3);
        imagePicker4 = findViewById(R.id.imagepicker4);
        imagePicker5 = findViewById(R.id.imagepicker5);

        imagePicker1.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Pilih Foto Produk"),PICK_IMAGE_REQUEST_1);
            Toast.makeText(TambahProdukActivity.this, "Foto 1", Toast.LENGTH_SHORT).show();
        });

        imagePicker2.setOnClickListener(view -> {
            Intent intent2 = new Intent();
            intent2.setType("image/*");
            intent2.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent2,"Pilih Foto Produk"),PICK_IMAGE_REQUEST_2);
            Toast.makeText(TambahProdukActivity.this, "Foto 2", Toast.LENGTH_SHORT).show();
        });

        imagePicker3.setOnClickListener(view -> {
            Intent intent3 = new Intent();
            intent3.setType("image/*");
            intent3.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent3,"Pilih Foto Produk"),PICK_IMAGE_REQUEST_3);
            Toast.makeText(TambahProdukActivity.this, "Foto 3", Toast.LENGTH_SHORT).show();
        });

        imagePicker4.setOnClickListener(view -> {
            Intent intent4 = new Intent();
            intent4.setType("image/*");
            intent4.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent4,"Pilih Foto Produk"),PICK_IMAGE_REQUEST_4);
            Toast.makeText(TambahProdukActivity.this, "Foto 4", Toast.LENGTH_SHORT).show();
        });

        imagePicker5.setOnClickListener(view -> {
            Intent intent5 = new Intent();
            intent5.setType("image/*");
            intent5.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent5,"Pilih Foto Produk"),PICK_IMAGE_REQUEST_5);
            Toast.makeText(TambahProdukActivity.this, "Foto 5", Toast.LENGTH_SHORT).show();
        });

        edtNamaProduk = findViewById(R.id.edt_nama_produk);
        edtDeskripsiProduk = findViewById(R.id.edt_deskripsi_produk);
        edtHargaProduk = findViewById(R.id.edt_harga_produk);
        edtStokBarang = findViewById(R.id.edt_stok_barang);
        edtBeratBarang = findViewById(R.id.edt_berat_barang);
        edtLebarBarang = findViewById(R.id.edt_lebar_barang);
        edtPanjangBarang = findViewById(R.id.edt_panjang_barang);
        edtTinggiBarang = findViewById(R.id.edt_tinggi_barang);
        edtOngkirPerkm = findViewById(R.id.edt_ongkos_perkm);

        spinnerListKategori = findViewById(R.id.spinner_list_kategori);

        swPreoder = findViewById(R.id.sw_preorder);
        swUnggulan = findViewById(R.id.sw_produk_unggulan);

        bSimpan = findViewById(R.id.button_simpan);

        bSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtNamaProduk.getText().toString().isEmpty() || edtHargaProduk.getText().toString().isEmpty()){
                    Toast.makeText(TambahProdukActivity.this, "Data Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
                }else{
                    SimpanDatabase();
                }

            }
        });

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final List<String> namaKategori = new ArrayList<String>();


                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    KategoryModel kategoryModel = itemSnapshot.getValue(KategoryModel.class);
                    namaKategori.add(kategoryModel.getName().toString());
                }

                ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(TambahProdukActivity.this, android.R.layout.simple_spinner_item, namaKategori);
                areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerListKategori.setAdapter(areasAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TambahProdukActivity.this, ""+databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case PICK_IMAGE_REQUEST_1:
                    if(data != null && data.getData() != null){
                        imageUri1 = data.getData();
                        imagePicker1.setImageURI(imageUri1);
                        listImageUri.add(imageUri1);
                    }
                    break;
                case PICK_IMAGE_REQUEST_2:
                    if(data != null && data.getData() != null){
                        imageUri2 = data.getData();
                        imagePicker2.setImageURI(imageUri2);
                        listImageUri.add(imageUri2);
                    }
                    break;
                case PICK_IMAGE_REQUEST_3:
                    if(data != null && data.getData() != null){
                        imageUri3 = data.getData();
                        imagePicker3.setImageURI(imageUri3);
                        listImageUri.add(imageUri3);
                    }
                    break;
                case PICK_IMAGE_REQUEST_4:
                    if(data != null && data.getData() != null){
                        imageUri4 = data.getData();
                        imagePicker4.setImageURI(imageUri4);
                        listImageUri.add(imageUri4);
                    }
                    break;
                case PICK_IMAGE_REQUEST_5:
                    if(data != null && data.getData() != null){
                        imageUri5 = data.getData();
                        imagePicker5.setImageURI(imageUri5);
                        listImageUri.add(imageUri5);
                    }
                    break;

            }
        }

    }

    private void SimpanDatabase(){
        ProdukModel produkModel = new ProdukModel();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String key = database.getReference(Common.PRODUK_REF).push().getKey();

        produkModel.setId(key);
        produkModel.setNama_produk(edtNamaProduk.getText().toString());
        produkModel.setDeskripsi(edtDeskripsiProduk.getText().toString());
        produkModel.setHarga_produk(TextUtils.isEmpty(edtHargaProduk.getText()) ? 0 :
                Long.parseLong(edtHargaProduk.getText().toString()));
        produkModel.setStok_produk(Long.parseLong(edtStokBarang.getText().toString()));
        produkModel.setBerat_produk(Long.parseLong(edtBeratBarang.getText().toString()));
        produkModel.setLebar_produk(Long.parseLong(edtLebarBarang.getText().toString()));
        produkModel.setPanjang_produk(Long.parseLong(edtPanjangBarang.getText().toString()));
        produkModel.setTinggi_produk(Long.parseLong(edtTinggiBarang.getText().toString()));
        produkModel.setOngkir_perkm(TextUtils.isEmpty(edtOngkirPerkm.getText()) ? 0 :
                Long.parseLong(edtOngkirPerkm.getText().toString()));
        produkModel.setKategori(spinnerListKategori.getSelectedItem().toString());

        if(swPreoder.isChecked()){
            produkModel.setPreorder(true);
        }else{
            produkModel.setPreorder(false);
        }

        if(swUnggulan.isChecked()){
            produkModel.setUnggulan(true);
        }else{
            produkModel.setUnggulan(false);
        }

        if(listImageUri.size() > 0){
            dialog.setMessage("Uploading...");
            dialog.show();

            for (int upload_count = 0; upload_count < listImageUri.size(); upload_count++) {
                Uri IndividualImage = listImageUri.get(upload_count);
                String unique_name = UUID.randomUUID().toString();
                final StorageReference ImageName = storageReference.child("images/"+unique_name);

                int finalUpload_count = upload_count;
                if(IndividualImage != null){
                    ImageName.putFile(IndividualImage)
                            .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    ImageName.getDownloadUrl().addOnSuccessListener(
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    switch (finalUpload_count){
                                                        case 0:
                                                            produkModel.setGambar1(uri.toString());
                                                            break;
                                                        case 1:
                                                            produkModel.setGambar2(uri.toString());
                                                            break;
                                                        case 2:
                                                            produkModel.setGambar3(uri.toString());
                                                            break;
                                                        case 3:
                                                            produkModel.setGambar4(uri.toString());
                                                            break;
                                                        case 4:
                                                            produkModel.setGambar5(uri.toString());
                                                            break;
                                                    }

                                                    if (finalUpload_count == (listImageUri.size() - 1)){
                                                        dialog.dismiss();
                                                        Map<String,Object> updateData = new HashMap<>();
                                                        updateData.put(produkModel.getId().toString(),produkModel);

                                                        FirebaseDatabase.getInstance()
                                                                .getReference(Common.PRODUK_REF)
                                                                .updateChildren(updateData)
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(TambahProdukActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }).addOnCompleteListener(task -> {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(TambahProdukActivity.this, "Sukses!", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            }
                                                        });
                                                    }

                                                }
                                            }
                                    );
                                }
                            }
                    ).addOnProgressListener(taskSnapshot -> {
                        double progress = (100* taskSnapshot.getBytesTransferred() /taskSnapshot.getTotalByteCount());
                        dialog.setMessage(new StringBuilder("Uploading ")
                                .append("Gambar ")
                                .append(finalUpload_count+1)
                                .append(" : ")
                                .append(progress).append("%"));
                    });
                }else{

                }
            }

        }else{
            Toast.makeText(TambahProdukActivity.this, "Silahkan tambahkan foto terlebih dahulu", Toast.LENGTH_SHORT).show();
        }

    }
}