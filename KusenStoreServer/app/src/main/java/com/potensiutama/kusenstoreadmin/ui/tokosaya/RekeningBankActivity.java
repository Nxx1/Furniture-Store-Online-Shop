package com.potensiutama.kusenstoreadmin.ui.tokosaya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.InformasiTokoModel;
import com.potensiutama.kusenstoreadmin.model.RekeningBankModel;

import java.util.HashMap;
import java.util.Map;

public class RekeningBankActivity extends AppCompatActivity {

    EditText edtNamaPemilik,edtNomorRekening,edtNamaBank;
    TextView bSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekening_bank);

        edtNamaPemilik = findViewById(R.id.edt_rekening_nama_pemilik);
        edtNomorRekening = findViewById(R.id.edt_rekening_nomor);
        edtNamaBank = findViewById(R.id.edt_rekening_nama_bank);

        bSimpan = findViewById(R.id.button_rekening_simpan);

        bSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RekeningBankModel rekeningBankModel = new RekeningBankModel();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String key = database.getReference(Common.REKENING_REF).push().getKey();

                rekeningBankModel.setNama_lengkap(edtNamaPemilik.getText().toString());
                rekeningBankModel.setNomor_rekening(Long.parseLong(edtNomorRekening.getText().toString()));
                rekeningBankModel.setNama_bank(edtNamaBank.getText().toString());
                rekeningBankModel.setId(key);

                Map<String,Object> updateData = new HashMap<>();
                updateData.put(rekeningBankModel.getId().toString(),rekeningBankModel);



                FirebaseDatabase.getInstance()
                        .getReference(Common.REKENING_REF)
                        .updateChildren(updateData)
                        .addOnFailureListener(e -> {
                            Toast.makeText(RekeningBankActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(RekeningBankActivity.this, "Sukses!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }
}