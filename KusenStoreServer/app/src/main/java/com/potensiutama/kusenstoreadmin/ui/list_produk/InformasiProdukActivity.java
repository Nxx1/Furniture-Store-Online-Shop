package com.potensiutama.kusenstoreadmin.ui.list_produk;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.potensiutama.kusenstoreadmin.EventBus.ToastEvent;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.adapter.ViewPagerFotoProdukAdapter;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.ProdukModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class InformasiProdukActivity extends AppCompatActivity {

    private EditText edtDeskripsiProduk,edtStokBarang,edtBeratBarang,edtLebarBarang,edtPanjangBarang,edtTinggiBarang,edtOngkirPerkm;
    private TextView txtNamaProduk,txtHargaProduk;
    private EditText edtKategori,edtPreorder,edtUnggulan;

    private ImageView bBack;

    private TextView bUbah,bHapus;

    // creating object of ViewPager
    ViewPager mViewPager;

    ViewPagerFotoProdukAdapter viewPagerFotoProdukAdapter;

    List<String> fotoProduk =  new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informasi_produk);

        txtNamaProduk = findViewById(R.id.txt_informasi_nama_produk);
        txtHargaProduk = findViewById(R.id.txt_informasi_harga_produk);
        edtDeskripsiProduk = findViewById(R.id.edt_informasi_deskripsi_produk);
        edtStokBarang = findViewById(R.id.edt_informasi_stok_barang);
        edtBeratBarang = findViewById(R.id.edt_informasi_berat_barang);
        edtLebarBarang = findViewById(R.id.edt_informasi_lebar_barang);
        edtPanjangBarang = findViewById(R.id.edt_informasi_panjang_barang);
        edtTinggiBarang = findViewById(R.id.edt_informasi_tinggi_barang);
        edtOngkirPerkm = findViewById(R.id.edt_informasi_ongkos_perkm);
        edtKategori = findViewById(R.id.edt_informasi_kategori_barang);
        edtPreorder = findViewById(R.id.edt_informasi_preorder_barang);
        edtUnggulan = findViewById(R.id.edt_informasi_unggulan_barang);

        txtNamaProduk.setText(Common.selectedProduk.getNama_produk());
        txtHargaProduk.setText("Rp"+Common.selectedProduk.getHarga_produk().toString());
        edtDeskripsiProduk.setText(Common.selectedProduk.getDeskripsi());
        edtStokBarang.setText(Common.selectedProduk.getStok_produk().toString());
        edtBeratBarang.setText(Common.selectedProduk.getBerat_produk().toString()+" kg");
        edtLebarBarang.setText(Common.selectedProduk.getLebar_produk().toString()+"cm");
        edtPanjangBarang.setText(Common.selectedProduk.getPanjang_produk().toString()+"cm");
        edtTinggiBarang.setText(Common.selectedProduk.getTinggi_produk().toString()+"cm");
        edtOngkirPerkm.setText("Rp"+Common.selectedProduk.getOngkir_perkm().toString());
        edtKategori.setText(Common.selectedProduk.getKategori());

        if(Common.selectedProduk.getPreorder()){
            edtPreorder.setText("Ya");
        }else{
            edtPreorder.setText("Tidak");
        }
        if(Common.selectedProduk.getUnggulan()){
            edtPreorder.setText("Ya");
        }else{
            edtPreorder.setText("Tidak");
        }

        bUbah = findViewById(R.id.button_informasi_ubah);
        bHapus = findViewById(R.id.button_informasi_hapus);

        bUbah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InformasiProdukActivity.this,UbahProdukActivity.class);
                startActivity(intent);
                finish();
            }
        });

        bHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InformasiProdukActivity.this);
                builder.setTitle("HAPUS")
                        .setMessage("Apakah anda yakin ingin menghapus produk ini?")
                        .setNegativeButton("Batal", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }).setPositiveButton("Hapus",((dialogInterface, i) -> {
                    FirebaseDatabase.getInstance()
                            .getReference(Common.PRODUK_REF)
                            .child(Common.selectedProduk.getId())
                            .removeValue()
                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                            .addOnCompleteListener(task -> {
                                Toast.makeText(getApplicationContext(), "Sukses", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                }));
                AlertDialog deleteDialog = builder.create();
                deleteDialog.show();
            }
        });

        if(Common.selectedProduk.getGambar1() != null){
            fotoProduk.add(Common.selectedProduk.getGambar1().toString());
        }
        if(Common.selectedProduk.getGambar2() != null){
            fotoProduk.add(Common.selectedProduk.getGambar2().toString());
        }
        if(Common.selectedProduk.getGambar3() != null){
            fotoProduk.add(Common.selectedProduk.getGambar3().toString());
        }
        if(Common.selectedProduk.getGambar4() != null){
            fotoProduk.add(Common.selectedProduk.getGambar4().toString());
        }
        if(Common.selectedProduk.getGambar5() != null){
            fotoProduk.add(Common.selectedProduk.getGambar5().toString());
        }

        // Initializing the ViewPager Object
        mViewPager = (ViewPager)findViewById(R.id.viewpager_informasi_foto);

        // Initializing the ViewPagerAdapter
        viewPagerFotoProdukAdapter = new ViewPagerFotoProdukAdapter(InformasiProdukActivity.this, fotoProduk);

        // Adding the Adapter to the ViewPager
        mViewPager.setAdapter(viewPagerFotoProdukAdapter);

        bBack = findViewById(R.id.images_informasi_back);

        bBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Common.selectedProduk = null;
            }
        });

    }
}