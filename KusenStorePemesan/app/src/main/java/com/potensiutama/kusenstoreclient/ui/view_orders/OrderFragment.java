package com.potensiutama.kusenstoreclient.ui.view_orders;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.potensiutama.kusenstoreclient.Adapter.MyOrdersAdapter;
import com.potensiutama.kusenstoreclient.Common.BottomSheetOrderFragment;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.Common.MySwipeHelper;
import com.potensiutama.kusenstoreclient.EventBus.LoadOrderEvent;
import com.potensiutama.kusenstoreclient.EventBus.PrintOrderEvent;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.Remote.IFCMService;
import com.potensiutama.kusenstoreclient.Remote.RetrofitFCMClient;
import com.potensiutama.kusenstoreclient.model.FCMSendData;
import com.potensiutama.kusenstoreclient.model.OrderModel;
import com.potensiutama.kusenstoreclient.model.TokenModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderFragment extends Fragment {


    @BindView(R.id.recycler_orders)
    RecyclerView recycler_order;

    Unbinder unbinder;
    int statusOrder = 0;
    LayoutAnimationController layoutAnimationController;
    MyOrdersAdapter adapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;

    private OrderViewModel orderViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        orderViewModel =
                ViewModelProviders.of(this).get(OrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_order, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        orderViewModel.getMessageError().observe(getViewLifecycleOwner(),s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
        orderViewModel.getOrderModelMutableLiveData().observe(getViewLifecycleOwner(),orderModels -> {
            if(orderModels != null){
                adapter = new MyOrdersAdapter(getContext(),orderModels);
                recycler_order.setAdapter(adapter);
            }
        });
        return root;
    }

    private void initViews() {

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        setHasOptionsMenu(true);

        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_order,width/3) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                if(statusOrder == 2) {
                    buf.add(new MyButton(getContext(), "Cetak", 30, 0, Color.parseColor("#8b0010"),
                            pos -> {

                                Dexter.withActivity(getActivity())
                                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        .withListener(new PermissionListener() {
                                            @Override
                                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                                //Toast.makeText(getContext(), "Cetak Klik ["+Common.getAppPath(getActivity()).toString()+"] "+adapter.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new PrintOrderEvent(new StringBuilder(Common.getAppPath(getActivity()))
                                                        .append(Common.FILE_PRINT).toString(), adapter.getItemAtPosition(pos)));
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
                }else if(statusOrder == 0) {
                    buf.add(new MyButton(getContext(), "Cancel", 30, 0, Color.parseColor("#FF3C30"),
                            pos -> {
                                OrderModel orderModel = ((MyOrdersAdapter) recycler_order.getAdapter()).getItemAtPosition(pos);
                                if (orderModel.getOrderStatus() == 0) {
                                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                    builder.setTitle("Cancel Order")
                                            .setMessage("Apakah anda yakin ingin membatalkan order ini?")
                                            .setNegativeButton("Tidak", (dialogInterface, i) -> dialogInterface.dismiss())
                                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                                updateOrder(pos,orderModel,-1);

                                            });
                                    androidx.appcompat.app.AlertDialog dialog = builder.create();
                                    dialog.show();

                                } else {
                                    Toast.makeText(getContext(), new StringBuilder("Status pemesanan ini ")
                                            .append(Common.converStatusToText(orderModel.getOrderStatus()))
                                            .append(" jadi tidak bisa dibatalkan!"), Toast.LENGTH_SHORT).show();
                                }
                            }));
                }
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu,menu);
    }

    private void updateOrder(int pos,OrderModel orderModel,int status){
        if(!TextUtils.isEmpty(orderModel.getKey())){
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("orderStatus",status);

            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(aVoid -> {

                        android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
                        dialog.show();

                        FirebaseDatabase.getInstance()
                                .getReference(Common.TOKEN_REF)
                                .child(orderModel.getUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                                            Map<String,String> notiData = new HashMap<>();
                                            notiData.put(Common.NOTI_TITLE,"Data pemesanan telah diupdate");
                                            notiData.put(Common.NOTI_CONTENT,new StringBuilder("Update Pemesanan [")
                                                    .append(orderModel.getShippingAddress())
                                                    .append("] : ")
                                                    .append(Common.converStatusToText(status)).toString());

                                            FCMSendData sendData = new FCMSendData(tokenModel.getToken(),notiData);

                                            compositeDisposable.add(ifcmService.sendNotification(sendData)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(fcmResponse -> {
                                                        dialog.dismiss();
                                                        if(fcmResponse.getSuccess() == 1){
                                                            Toast.makeText(getContext(), "Pemesanan berhasil dibatalkan!", Toast.LENGTH_SHORT).show();
                                                        }else{
                                                            Toast.makeText(getContext(), "Pemesanan berhasil dibatalkan, tetapi gagal mengirim notifikasi!", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }, throwable -> {
                                                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }));

                                        }else{
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        adapter.removeItem(pos);
                        adapter.notifyItemRemoved(pos);
                    });
        }else{
            Toast.makeText(getContext(), "Nomor pemesanan tidak boleh kosong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_filter){
            BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
            bottomSheetOrderFragment.show(getActivity().getSupportFragmentManager(),"OrderFilter");
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {

        if(EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onLoaderEvent(LoadOrderEvent event){
        statusOrder = event.getStatus();
        orderViewModel.loadOrderByStatus(event.getStatus());
    }
}