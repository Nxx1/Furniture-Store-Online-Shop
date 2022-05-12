package com.potensiutama.kusenstoreclient.ui.cart;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.potensiutama.kusenstoreclient.Adapter.MyCartAdapter;
import com.potensiutama.kusenstoreclient.Callback.ILoadTimeFromFirebaseListener;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.Common.MySwipeHelper;
import com.potensiutama.kusenstoreclient.Database.CartDataSource;
import com.potensiutama.kusenstoreclient.Database.CartDatabase;
import com.potensiutama.kusenstoreclient.Database.CartItem;
import com.potensiutama.kusenstoreclient.Database.LocalCartDataSource;
import com.potensiutama.kusenstoreclient.EventBus.CounterCartEvent;
import com.potensiutama.kusenstoreclient.EventBus.HideFABCart;
import com.potensiutama.kusenstoreclient.EventBus.MenuItemBack;
import com.potensiutama.kusenstoreclient.EventBus.PrintOrderEvent;
import com.potensiutama.kusenstoreclient.EventBus.UpdateItemInCart;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.Remote.IFCMService;
import com.potensiutama.kusenstoreclient.Remote.RetrofitFCMClient;
import com.potensiutama.kusenstoreclient.model.FCMSendData;
import com.potensiutama.kusenstoreclient.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.potensiutama.kusenstoreclient.ui.buat_pesanan.BuatPesananActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;

    ILoadTimeFromFirebaseListener listener;
    IFCMService ifcmService;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;
    @BindView(R.id.txt_cara_hapus)
    TextView txt_cara_hapus;

    private double total_harga_pemesanan;
    EditText edt_jumlah_bayar;



    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick(){

        Intent intent = new Intent(getContext(), BuatPesananActivity.class);
        startActivity(intent);

    }

    private void paymentCOD(String address, String comment) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {
            cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Double>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Double totalPrice) {
                            double finalPrice = totalPrice;
                            OrderModel order = new OrderModel();
                            order.setUserId(Common.currentUser.getUid());
                            order.setUserName(Common.currentUser.getName());
                            order.setUserPhone(Common.currentUser.getPhone());
                            order.setShippingAddress(address);
                            order.setComment(comment);
                            order.setLat(-0.1f);
                            order.setLng(-0.1f);
                            order.setJumlahBayar(Integer.parseInt(edt_jumlah_bayar.getText().toString()));
                            order.setCartItemList(cartItems);
                            order.setTotalPayment(totalPrice);
                            order.setDiscount(0);
                            order.setFinalPayment(finalPrice);
                            order.setCod(true);
                            order.setTransactionId("Tunai");
                            double kembalian = Double.parseDouble(edt_jumlah_bayar.getText().toString()) - finalPrice;
                            order.setKembali((int)kembalian);

                            syncLocalTimeWithGlobalTime(order);

                        }

                        @Override
                        public void onError(Throwable e) {
                            if(!e.getMessage().contains("Query returned empty result set"))
                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }, throwable -> {
            Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
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
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    cartDataSource.cleanCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    Map<String,String> notiData = new HashMap<>();
                                    notiData.put(Common.NOTI_TITLE,"Pemesanan Baru");
                                    notiData.put(Common.NOTI_CONTENT,"Konfirmasi pemesanan baru a/n "+order.getShippingAddress());

                                    FCMSendData sendData = new FCMSendData(Common.createTopicOrder(),notiData);

                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        Toast.makeText(getContext(), "Pemesanan Berhasil!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "Gagal mengirim notifikasi!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }

    private MyCartAdapter adapter;

    private Unbinder unbinder;

    private CartViewModel cartViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel =
                ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        listener = this;

        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if(cartItems == null || cartItems.isEmpty()){
                recycler_cart.setVisibility(View.GONE);
                group_place_holder.setVisibility(View.GONE);
                txt_empty_cart.setVisibility(View.VISIBLE);
                txt_cara_hapus.setVisibility(View.GONE);
            }else{
                recycler_cart.setVisibility(View.VISIBLE);
                txt_cara_hapus.setVisibility(View.VISIBLE);
                group_place_holder.setVisibility(View.VISIBLE);
                txt_empty_cart.setVisibility(View.GONE);

                adapter = new MyCartAdapter(getContext(),cartItems);
                recycler_cart.setAdapter(adapter);
            }
        });
        unbinder = ButterKnife.bind(this,root);
        initViews();

        return root;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_cart,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Hapus",30,0, Color.parseColor("#FF3C30"),
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            adapter.notifyItemRemoved(pos);
                                            sumAllItemInCart();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            Toast.makeText(getContext(), "Berhasil hapus item dari keranjang!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
            }
        };

        sumAllItemInCart();
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
                        txt_total_price.setText(new StringBuilder("Total : Rp").append(Common.formatPrice(aDouble)));
                        total_harga_pemesanan = aDouble;
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_clear_cart).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_clear_cart){
            cartDataSource.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText(getContext(), "Berhasil Kosongkan Keranjang!", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        cartViewModel.onStop();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        compositeDisposable.clear();
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event){
        if(event.getCartItem() != null){
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), "[UPDATE KERANJANG]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {
                        txt_total_price.setText(new StringBuilder("Total : Rp")
                        .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs) {
        order.setCreateDate(estimateTimeInMs);
        order.setOrderStatus(0);
        cetakStruk(order);
        writeOrderToFirebase(order);
    }

    private void cetakStruk(OrderModel orderModel) {
        if(orderModel != null){
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
            builder.setTitle("Cetak")
                    .setMessage("Apakah anda ingin cetak order ini?")
                    .setNegativeButton("Batal", (dialogInterface, i) -> {
                        writeOrderToFirebase(orderModel);
                        dialogInterface.dismiss();
                    }).setPositiveButton("Cetak",((dialogInterface, i) -> {
                Dexter.withActivity(getActivity())
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                //Toast.makeText(getContext(), "Cetak Klik ["+Common.getAppPath(getActivity()).toString()+"] "+adapter.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().postSticky(new PrintOrderEvent(new StringBuilder(Common.getAppPath(getActivity()))
                                        .append(Common.FILE_PRINT).toString(), orderModel));
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Toast.makeText(getContext(), "Please accept this permission", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                            }
                        }).check();
            }));
            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

}