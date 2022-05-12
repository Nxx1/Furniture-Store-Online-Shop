package com.potensiutama.kusenstoreclient.ui.buat_pesanan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.potensiutama.kusenstoreclient.Adapter.MyCartAdapter;
import com.potensiutama.kusenstoreclient.Adapter.MyProdukPesananAdapter;
import com.potensiutama.kusenstoreclient.Callback.ILoadTimeFromFirebaseListener;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.Common.MySwipeHelper;
import com.potensiutama.kusenstoreclient.Database.CartDataSource;
import com.potensiutama.kusenstoreclient.Database.CartDatabase;
import com.potensiutama.kusenstoreclient.Database.CartItem;
import com.potensiutama.kusenstoreclient.Database.LocalCartDataSource;
import com.potensiutama.kusenstoreclient.EventBus.CounterCartEvent;
import com.potensiutama.kusenstoreclient.EventBus.HideFABCart;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.Remote.IFCMService;
import com.potensiutama.kusenstoreclient.Remote.RetrofitFCMClient;
import com.potensiutama.kusenstoreclient.SendNotificationPack.APIService;
import com.potensiutama.kusenstoreclient.SendNotificationPack.Client;
import com.potensiutama.kusenstoreclient.SendNotificationPack.Data;
import com.potensiutama.kusenstoreclient.SendNotificationPack.MyResponse;
import com.potensiutama.kusenstoreclient.SendNotificationPack.NotificationSender;
import com.potensiutama.kusenstoreclient.model.FCMSendData;
import com.potensiutama.kusenstoreclient.model.InformasiAkunSayaModel;
import com.potensiutama.kusenstoreclient.model.InformasiTokoModel;
import com.potensiutama.kusenstoreclient.model.OrderModel;
import com.potensiutama.kusenstoreclient.model.RekeningBankModel;
import com.potensiutama.kusenstoreclient.ui.akunsaya.AkunSayaActivity;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuatPesananActivity extends AppCompatActivity implements ILoadTimeFromFirebaseListener {


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Spinner spListMetodePembayaran;

    private CartDataSource cartDataSource;


    private ListView listViewProdukPesanan;

    private List<CartItem> cartItemArrayList;

    TextView tTotalPembayaran;


    private double total_harga_pemesanan;

    private Double latUser,lngUser,latToko,lngToko;
    private Double jarakLokasi;

    InformasiAkunSayaModel informasiAkunSayaModel;


    ILoadTimeFromFirebaseListener listener;

    IFCMService ifcmService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_pesanan);

        listener = this;

        LoadDataAlamatSaya();

        ImageView imgBack = findViewById(R.id.images_checkout_back);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().postSticky(new HideFABCart(true));
                finish();
            }
        });

        spListMetodePembayaran = findViewById(R.id.spinner_list_metode_pembayaran);

        DatabaseReference metodePembayaranRef = FirebaseDatabase.getInstance().getReference(Common.REKENING_REF);
        metodePembayaranRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final List<String> namaBank = new ArrayList<String>();


                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    RekeningBankModel kategoryModel = itemSnapshot.getValue(RekeningBankModel.class);
                    String data = kategoryModel.getNama_bank().toUpperCase() + "-" +kategoryModel.getNama_lengkap() + "-"+ kategoryModel.getNomor_rekening().toString();
                    namaBank.add(data);
                }

                //namaBank.add("COD (Bayar ditempat)");

                ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(BuatPesananActivity.this, android.R.layout.simple_spinner_item, namaBank);
                areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spListMetodePembayaran.setAdapter(areasAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BuatPesananActivity.this, ""+databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        initViews();
    }

    private void initViews() {

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);


        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(BuatPesananActivity.this).cartDAO());
        tTotalPembayaran = findViewById(R.id.textview_total_pembayaran);

        LoadDataProdukPesanan();
        sumAllItemInCart();
        HitungJarakOngkir();

    }

    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        total_harga_pemesanan += aDouble;
                        TextView subTotalProduk = findViewById(R.id.subtotal_produk);
                        subTotalProduk.setText(new StringBuilder("Rp").append(Common.formatPrice(aDouble)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(BuatPesananActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sumAllOngkirInCart() {

        TextView tOngkir = findViewById(R.id.edt_informasi_ongkos_perkm);

        cartDataSource.sumOngkirInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        tOngkir.setText(new StringBuilder("Rp").append(Common.formatPrice(aDouble)));

                        Double beforeongkir = total_harga_pemesanan;
                        Double subTotalOngkir = aDouble * jarakLokasi;
                        total_harga_pemesanan += subTotalOngkir;

                        TextView tSubTotalOngkir = findViewById(R.id.subtotal_ongkir);
                        tSubTotalOngkir.setText(new StringBuilder("Rp").append(Common.formatPrice(subTotalOngkir)));

                        tTotalPembayaran.setText(new StringBuilder("Total Pembayaran \nRp").append(Common.formatPrice(total_harga_pemesanan)));
                        TextView totalHarga = findViewById(R.id.total_harga_produk);
                        totalHarga.setText(new StringBuilder("Rp").append(Common.formatPrice(total_harga_pemesanan)));

                        TextView bCheckout = findViewById(R.id.button_checkout);

                        bCheckout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                paymentBank(informasiAkunSayaModel,total_harga_pemesanan,beforeongkir);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(BuatPesananActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void LoadDataProdukPesanan(){

        listViewProdukPesanan = findViewById(R.id.list_produk_pesanan);

        cartItemArrayList = new ArrayList<>();

        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    cartItemArrayList = cartItems;
                    MyProdukPesananAdapter adapter = new MyProdukPesananAdapter(BuatPesananActivity.this);
                    adapter.setProdukCheckoutList(cartItemArrayList);
                    listViewProdukPesanan.setAdapter(adapter);

                }, throwable -> {
                    Toast.makeText(BuatPesananActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().postSticky(new HideFABCart(true));
        super.onBackPressed();
    }

    private void LoadDataAlamatSaya(){
        DatabaseReference informasiAkunSayaRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCESER)
                .child(Common.currentUser.getUid())
                .child(Common.AKUN_SAYA_REF);

        informasiAkunSayaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    informasiAkunSayaModel = new InformasiAkunSayaModel();
                    informasiAkunSayaModel =snapshot.getValue(InformasiAkunSayaModel.class);

                    TextView txtAlamat = findViewById(R.id.txt_alamat_pengguna);

                    txtAlamat.setText(
                            "Alamat Pengiriman\n"+
                                    Common.currentUser.getName() + " | " +Common.currentUser.getPhone().toString() + "\n"
                            + informasiAkunSayaModel.getAlamat_lengkap()
                    );

                    latUser = informasiAkunSayaModel.getKoordinatLat();
                    lngUser = informasiAkunSayaModel.getKoordinatLong();
                }else{
                    Toast.makeText(BuatPesananActivity.this, "Silahkan mengisi informasi alamat terlebih dahulu", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(BuatPesananActivity.this, AkunSayaActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void HitungJarakOngkir(){
        DatabaseReference ongkirRefToko = FirebaseDatabase.getInstance().getReference(Common.TOKO_SAYA_REF);

        ongkirRefToko.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    InformasiTokoModel informasiTokoModel = new InformasiTokoModel();
                    informasiTokoModel =snapshot.getValue(InformasiTokoModel.class);

                    latToko = informasiTokoModel.getKoordinatLat();
                    lngToko = informasiTokoModel.getKoordinatLong();

                    Point koordinatUser = Point.fromLngLat(lngUser, latUser);
                    Point KoordinatToko = Point.fromLngLat(lngToko, latToko);

                    jarakLokasi = TurfMeasurement.distance(koordinatUser, KoordinatToko, TurfConstants.UNIT_KILOMETERS);

                    sumAllOngkirInCart();
                }else{
                    Toast.makeText(BuatPesananActivity.this, "Error, alamat toko tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void paymentBank(InformasiAkunSayaModel informasiAkunSayaModel, Double totalPrice,Double beforeongkir) {
        double finalPrice = totalPrice;
        OrderModel order = new OrderModel();
        order.setUserId(Common.currentUser.getUid());
        order.setUserName(Common.currentUser.getName());
        order.setUserPhone(Common.currentUser.getPhone());
        order.setShippingAddress(informasiAkunSayaModel.getAlamat_lengkap());
        order.setLat(informasiAkunSayaModel.getKoordinatLat());
        order.setLng(informasiAkunSayaModel.getKoordinatLong());
        order.setCartItemList(cartItemArrayList);
        order.setTotalPayment(beforeongkir);
        order.setFinalPayment(finalPrice);
        order.setBuktiTransfer(null);
        order.setRekeningPembayaran(spListMetodePembayaran.getSelectedItem().toString());
        order.setCod(false);
        order.setTransactionId(Common.createOrderNumber());
        order.setOrderStatus(0);
        syncLocalTimeWithGlobalTime(order);
    }

    private void syncLocalTimeWithGlobalTime(OrderModel order) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long offset = dataSnapshot.getValue(Long.class);
                long estimatedServerTime = System.currentTimeMillis()+offset;
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                Date resultDate = new Date(estimatedServerTime);
                //Log.d("Test Tanggal",""+sdf.format(resultDate));

                listener.onLoadTimeSuccess(order,estimatedServerTime);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onLoadTimeFailed(databaseError.getMessage());
            }
        });
    }

    private void writeOrderToFirebase(OrderModel order) {
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> {
                    Toast.makeText(BuatPesananActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {

                    Common.orderModelPembayaran = order;
                    cartDataSource.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            sendNotifications(order.getUserName());
                            Toast.makeText(BuatPesananActivity.this, "Pesanan sukses, silahkan melakukan proses pembayaran", Toast.LENGTH_SHORT).show();
                            finish();
                            Common.homeActivity.navController.navigate(R.id.nav_view_orders);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(BuatPesananActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs) {
        order.setCreateDate(estimateTimeInMs);
        writeOrderToFirebase(order);
    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(BuatPesananActivity.this, ""+message, Toast.LENGTH_SHORT).show();
    }

    public void sendNotifications(String namaPemesan) {

        APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        String TOPIC = Common.createTopicOrder();
        Data data = new Data("Pesanan baru a/n "+namaPemesan , "Status : menunggu proses pembayaran");
        NotificationSender sender = new NotificationSender(data, TOPIC);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(BuatPesananActivity.this, "Gagal mengirim notifikasi ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }
}