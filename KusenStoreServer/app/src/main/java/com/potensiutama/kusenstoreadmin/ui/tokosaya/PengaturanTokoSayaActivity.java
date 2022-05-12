package com.potensiutama.kusenstoreadmin.ui.tokosaya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.InformasiTokoModel;
import com.potensiutama.kusenstoreadmin.model.ProdukModel;
import com.potensiutama.kusenstoreadmin.ui.list_produk.TambahProdukActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PengaturanTokoSayaActivity extends AppCompatActivity {

    public EditText edtEmail,edtNoHp,edtAlamatLengkap,edtKoordinat;

    ImageView bBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan_toko_saya);

        Common.pengaturanTokoSayaActivity = PengaturanTokoSayaActivity.this;

        bBack = findViewById(R.id.images_toko_saya_back);

        bBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edtEmail = findViewById(R.id.edt_toko_email);
        edtNoHp = findViewById(R.id.edt_toko_no_hp);
        edtAlamatLengkap = findViewById(R.id.edt_toko_alamat);
        edtKoordinat = findViewById(R.id.edt_toko_titik_koordinat);

        TextView bSimpan = findViewById(R.id.button_toko_saya_simpan);

        bSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InformasiTokoModel informasiTokoModel = new InformasiTokoModel();

                informasiTokoModel.setEmail(edtEmail.getText().toString());
                informasiTokoModel.setNo_hp(Long.parseLong(edtNoHp.getText().toString()));
                informasiTokoModel.setAlamat_lengkap(edtAlamatLengkap.getText().toString());
                informasiTokoModel.setKoordinatLat(Common.selectedLatitude);
                informasiTokoModel.setKoordinatLong(Common.selectedLongitude);

                Map<String,Object> updateData = new HashMap<>();
                updateData.put(Common.TOKO_SAYA_REF,informasiTokoModel);


                FirebaseDatabase.getInstance()
                        .getReference(Common.TOKO_SAYA_REF)
                        .setValue(informasiTokoModel)
                        .addOnFailureListener(e -> {
                            Toast.makeText(PengaturanTokoSayaActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(PengaturanTokoSayaActivity.this, "Sukses!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });

        LoadDataInformasiToko();


        edtKoordinat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PengaturanTokoSayaActivity.this, LocationPickerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void LoadDataInformasiToko(){
        DatabaseReference informasiTokoRef = FirebaseDatabase.getInstance().getReference(Common.TOKO_SAYA_REF);

        informasiTokoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                InformasiTokoModel informasiTokoModel = new InformasiTokoModel();
                informasiTokoModel =snapshot.getValue(InformasiTokoModel.class);

                if(informasiTokoModel.getEmail() != null){
                    edtEmail.setText(informasiTokoModel.getEmail().toString());
                }

                if(informasiTokoModel.getNo_hp() != null){
                    edtNoHp.setText(informasiTokoModel.getNo_hp().toString());
                }

                if(informasiTokoModel.getEmail() != null){
                    edtAlamatLengkap.setText(informasiTokoModel.getAlamat_lengkap().toString());
                }
                
                if(informasiTokoModel.getKoordinatLong() != null || informasiTokoModel.getKoordinatLat() != null){
                    edtKoordinat.setText(informasiTokoModel.getKoordinatLat().toString()+ " | "+informasiTokoModel.getKoordinatLong().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

}