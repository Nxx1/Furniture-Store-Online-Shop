package com.potensiutama.kusenstoreadmin.ui.datakurir;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.adapter.MyDataKurirAdapter;
import com.potensiutama.kusenstoreadmin.adapter.MyDataRekeningAdapter;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.RekeningBankModel;
import com.potensiutama.kusenstoreadmin.model.ServerUserModel;
import com.potensiutama.kusenstoreadmin.ui.metodepembayaran.MetodePembayaranActivity;

import java.util.ArrayList;

public class DataKurirActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<ServerUserModel> serverUserModelArrayList;
    DatabaseReference dbDataKurir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_kurir);

        LoadDataKurir();

        ImageView bBack = findViewById(R.id.images_kurir_back);

        bBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void LoadDataKurir(){
        dbDataKurir = FirebaseDatabase.getInstance().getReference("Server");

        listView = findViewById(R.id.lv_data_kurir_list);

        serverUserModelArrayList = new ArrayList<>();

        dbDataKurir.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                serverUserModelArrayList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ServerUserModel serverUserModel = dataSnapshot1.getValue(ServerUserModel.class);

                    if(!serverUserModel.getName().equals("Administrator")){
                        serverUserModelArrayList.add(serverUserModel);
                    }
                }

                MyDataKurirAdapter adapter = new MyDataKurirAdapter(DataKurirActivity.this);
                adapter.setKurirList(serverUserModelArrayList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DataKurirActivity.this, "Terjadi kesalahan.", Toast.LENGTH_SHORT).show();
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