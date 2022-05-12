package com.potensiutama.kusenstoreadmin.ui.metodepembayaran;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.adapter.MyDataRekeningAdapter;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.InformasiTokoModel;
import com.potensiutama.kusenstoreadmin.model.RekeningBankModel;
import com.potensiutama.kusenstoreadmin.ui.list_produk.TambahProdukActivity;
import com.potensiutama.kusenstoreadmin.ui.tokosaya.RekeningBankActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetodePembayaranActivity extends AppCompatActivity {

    Switch swCOD;

    private ListView listView;
    private ArrayList<RekeningBankModel> rekeningBankModelArrayList;
    DatabaseReference dbRekening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metode_pembayaran);

        LoadDataRekening();

        swCOD = findViewById(R.id.sw_cod);

        LoadCOD();

        swCOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swCOD.isChecked()){
                    Map<String,Object> updateData = new HashMap<>();
                    updateData.put("cod",true);

                    FirebaseDatabase.getInstance()
                            .getReference(Common.COD_REF)
                            .updateChildren(updateData)
                            .addOnFailureListener(e -> {
                                Toast.makeText(MetodePembayaranActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(MetodePembayaranActivity.this, "COD Aktif", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Map<String,Object> updateData = new HashMap<>();
                    updateData.put("cod",true);

                    FirebaseDatabase.getInstance()
                            .getReference(Common.COD_REF)
                            .updateChildren(updateData)
                            .addOnFailureListener(e -> {
                                Toast.makeText(MetodePembayaranActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(MetodePembayaranActivity.this, "COD Nonaktif", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        ImageView imAddMetodePembayaran = findViewById(R.id.im_metode_pembayaran);
        ImageView im2AddMetodePembayaran = findViewById(R.id.im2_metode_pembayaran);
        TextView tvAddMetodePembayaran = findViewById(R.id.tv_metode_pembayaran);
        Intent intentRekening = new Intent(MetodePembayaranActivity.this,RekeningBankActivity.class);

        imAddMetodePembayaran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentRekening);
            }
        });

        im2AddMetodePembayaran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentRekening);
            }
        });

        tvAddMetodePembayaran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentRekening);
            }
        });

        ImageView bKembali = findViewById(R.id.images_metode_pembayaran_back);

        bKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void LoadCOD(){
        DatabaseReference codRef = FirebaseDatabase.getInstance().getReference(Common.COD_REF).child("cod");

        codRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean codCheck =snapshot.getValue(Boolean.class);

                swCOD.setChecked(codCheck);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void LoadDataRekening(){
        dbRekening = FirebaseDatabase.getInstance().getReference(Common.REKENING_REF);

        listView = findViewById(R.id.metode_pembayaran_list_rekening);

        rekeningBankModelArrayList = new ArrayList<>();


        dbRekening.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                rekeningBankModelArrayList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    RekeningBankModel rekeningBankModel = dataSnapshot1.getValue(RekeningBankModel.class);
                    rekeningBankModelArrayList.add(rekeningBankModel);
                }

                MyDataRekeningAdapter adapter = new MyDataRekeningAdapter(MetodePembayaranActivity.this);
                adapter.setRekeningList(rekeningBankModelArrayList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MetodePembayaranActivity.this, "Terjadi kesalahan.", Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //    Common.daftarPAUDSelected = daftarPAUDModelArrayList.get(i);
               // UpdateForm();
            }
        });

    }

}