package com.potensiutama.kusenstoreclient.ui.productlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.database.FirebaseDatabase;
import com.potensiutama.kusenstoreclient.Adapter.ViewPagerFotoProdukAdapter;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.Database.CartDataSource;
import com.potensiutama.kusenstoreclient.Database.CartDatabase;
import com.potensiutama.kusenstoreclient.Database.CartItem;
import com.potensiutama.kusenstoreclient.Database.LocalCartDataSource;
import com.potensiutama.kusenstoreclient.EventBus.CounterCartEvent;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.model.FCMSendData;
import com.potensiutama.kusenstoreclient.ui.buat_pesanan.BuatPesananActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class InformasiProdukActivity extends AppCompatActivity {

    private EditText edtDeskripsiProduk,edtStokBarang,edtBeratBarang,edtLebarBarang,edtPanjangBarang,edtTinggiBarang,edtOngkirPerkm;
    private TextView txtNamaProduk,txtHargaProduk;
    private EditText edtKategori,edtPreorder,edtUnggulan;

    private ImageView bBack;

    private TextView bTambahKeranjang;
    private TextView bCheckout;

    // creating object of ViewPager
    ViewPager mViewPager;

    ViewPagerFotoProdukAdapter viewPagerFotoProdukAdapter;

    List<String> fotoProduk =  new ArrayList<>();


    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;


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
        txtHargaProduk.setText("Rp"+Common.formatPrice(Common.selectedProduk.getHarga_produk()));
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


        this.compositeDisposable = new CompositeDisposable();
        this.cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(InformasiProdukActivity.this).cartDAO());

        bTambahKeranjang = findViewById(R.id.button_produk_tambah_keranjang);
        bCheckout = findViewById(R.id.button_produk_checkout);

        bTambahKeranjang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TambahKeranjang();
                finish();


            }
        });

        bCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartDataSource cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(InformasiProdukActivity.this).cartDAO());

                cartDataSource.cleanCart(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Integer integer) {
                                TambahKeranjang();
                                // Common.homeActivity.navController.navigate(R.id.nav_cart);
                                startActivity(new Intent(InformasiProdukActivity.this, BuatPesananActivity.class));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(InformasiProdukActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
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

    private void TambahKeranjang(){
        CartItem cartItem = new CartItem();
        cartItem.setUid(Common.currentUser.getUid());
        cartItem.setUserPhone(Common.currentUser.getPhone());
        cartItem.setProdukId(Common.selectedProduk.getId());
        cartItem.setProdukName(Common.selectedProduk.getNama_produk());
        cartItem.setProdukImage(Common.selectedProduk.getGambar1());
        cartItem.setProdukPrice(Double.valueOf(String.valueOf(Common.selectedProduk.getHarga_produk())));
        cartItem.setProdukQuantity(1);
        cartItem.setProdukOngkir(Double.valueOf(Common.selectedProduk.getOngkir_perkm()));
        cartDataSource.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                cartItem.getProdukId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CartItem cartItemFromDB) {
                        if(cartItemFromDB.equals(cartItem)){
                            cartItemFromDB.setProdukQuantity(cartItemFromDB.getProdukQuantity() + cartItem.getProdukQuantity());

                            cartDataSource.updateCartItems(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            Toast.makeText(InformasiProdukActivity.this, "Update Keranjang Berhasil", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(InformasiProdukActivity.this, "[Error Update Keranjang]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }else{
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                       // Toast.makeText(InformasiProdukActivity.this, "Berhasil Menambahkan Ke Keranjang", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    },throwable -> {
                                        Toast.makeText(InformasiProdukActivity.this, "[Keranjang ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                            );
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e.getMessage().contains("empty")){
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(InformasiProdukActivity.this, "Berhasil Menambahkan Ke Keranjang", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    },throwable -> {
                                        Toast.makeText(InformasiProdukActivity.this, "[Keranjang ERROR]"+ Common.selectedProduk.getId().toString() +throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                            );
                        }else {
                            Toast.makeText(InformasiProdukActivity.this, "[Keranjang] " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}