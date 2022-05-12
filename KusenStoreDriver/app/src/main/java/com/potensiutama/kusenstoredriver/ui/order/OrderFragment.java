package com.potensiutama.kusenstoredriver.ui.order;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.potensiutama.kusenstoredriver.EventBus.ChangeMenuClick;
import com.potensiutama.kusenstoredriver.EventBus.LoadOrderEvent;
import com.potensiutama.kusenstoredriver.R;
import com.potensiutama.kusenstoredriver.adapter.MyOrderAdapter;
import com.potensiutama.kusenstoredriver.common.BottomSheetOrderFragment;
import com.potensiutama.kusenstoredriver.common.Common;
import com.potensiutama.kusenstoredriver.common.MySwipeHelper;
import com.potensiutama.kusenstoredriver.model.FCMSendData;
import com.potensiutama.kusenstoredriver.model.OrderModel;
import com.potensiutama.kusenstoredriver.model.TokenModel;
import com.potensiutama.kusenstoredriver.remote.IFCMService;
import com.potensiutama.kusenstoredriver.remote.RetrofitFCMClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;
    @BindView(R.id.txt_order_filter)
    TextView txt_order_filter;

    Unbinder unbinder;

    LayoutAnimationController layoutAnimationController;
    MyOrderAdapter adapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;

    private OrderViewModel orderViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        orderViewModel =
                ViewModelProviders.of(this).get(OrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_order, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        orderViewModel.getMessageError().observe(getViewLifecycleOwner(),s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
        orderViewModel.getOrderModelMutableLiveData().observe(getViewLifecycleOwner(),orderModels -> {
            if(orderModels != null){
                adapter = new MyOrderAdapter(getContext(),orderModels);
                recycler_order.setAdapter(adapter);
                recycler_order.setLayoutAnimation(layoutAnimationController);

                updateTextCounter();
            }
        });
        return root;
    }

    private void initViews() {

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        setHasOptionsMenu(true);

        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
    }

    private void updateTextCounter() {
        txt_order_filter.setText(new StringBuilder("Daftar Pengiriman (")
                .append(adapter.getItemCount())
                .append(")"));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu,menu);
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

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onLoaderEvent(LoadOrderEvent event){
        orderViewModel.loadOrderByStatus(event.getStatus());
    }
}