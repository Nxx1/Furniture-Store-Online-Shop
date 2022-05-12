package com.potensiutama.kusenstoreclient.ui.akunsaya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.model.InformasiAkunSayaModel;

import java.util.HashMap;
import java.util.Map;

public class AkunSayaActivity extends AppCompatActivity {

    public EditText edtEmail,edtNoHp,edtAlamatLengkap,edtKoordinat;

    ImageView bBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akun_saya);

        Common.akunSayaActivity = AkunSayaActivity.this;

        bBack = findViewById(R.id.images_toko_saya_back);

        bBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edtEmail = findViewById(R.id.edt_toko_email);
       // edtNoHp = findViewById(R.id.edt_toko_no_hp);
        edtAlamatLengkap = findViewById(R.id.edt_toko_alamat);
        edtKoordinat = findViewById(R.id.edt_toko_titik_koordinat);

        TextView bSimpan = findViewById(R.id.button_toko_saya_simpan);

        bSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InformasiAkunSayaModel informasiAkunSayaModel = new InformasiAkunSayaModel();

                informasiAkunSayaModel.setEmail(edtEmail.getText().toString());
                //informasiAkunSayaModel.setNo_hp(Common.currentUser.getPhone().toString());
                informasiAkunSayaModel.setAlamat_lengkap(edtAlamatLengkap.getText().toString());
                informasiAkunSayaModel.setKoordinatLat(Common.selectedLatitude);
                informasiAkunSayaModel.setKoordinatLong(Common.selectedLongitude);

                FirebaseDatabase.getInstance()
                        .getReference(Common.USER_REFERENCESER)
                        .child(Common.currentUser.getUid())
                        .child(Common.AKUN_SAYA_REF)
                        .setValue(informasiAkunSayaModel)
                        .addOnFailureListener(e -> {
                            Toast.makeText(AkunSayaActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(AkunSayaActivity.this, "Sukses!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });

        LoadDataInformasiSaya();


        edtKoordinat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AkunSayaActivity.this, LocationPickerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void LoadDataInformasiSaya(){
        DatabaseReference informasiAkunSayaRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCESER)
                .child(Common.currentUser.getUid())
                .child(Common.AKUN_SAYA_REF);

        informasiAkunSayaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    InformasiAkunSayaModel informasiAkunSayaModel = new InformasiAkunSayaModel();
                    informasiAkunSayaModel =snapshot.getValue(InformasiAkunSayaModel.class);

                    if(informasiAkunSayaModel.getEmail() != null){
                        edtEmail.setText(informasiAkunSayaModel.getEmail().toString());
                    }
                    if(informasiAkunSayaModel.getAlamat_lengkap() != null){
                        edtAlamatLengkap.setText(informasiAkunSayaModel.getAlamat_lengkap().toString());
                    }

                    if(informasiAkunSayaModel.getKoordinatLong() != null || informasiAkunSayaModel.getKoordinatLat() != null){
                        edtKoordinat.setText(informasiAkunSayaModel.getKoordinatLat().toString()+ " | "+informasiAkunSayaModel.getKoordinatLong().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

}