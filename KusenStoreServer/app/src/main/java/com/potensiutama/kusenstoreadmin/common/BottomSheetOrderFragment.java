package com.potensiutama.kusenstoreadmin.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.potensiutama.kusenstoreadmin.EventBus.LoadOrderEvent;
import com.potensiutama.kusenstoreadmin.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class BottomSheetOrderFragment extends BottomSheetDialogFragment {


    @OnClick(R.id.semua_order)
    public void onSemuaOrderClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(-2));
        dismiss();
    }

    @OnClick(R.id.konfirmasi_pembayaran_filter)
    public void onKonfirmasiPembayaranFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(0));
        dismiss();
    }

    @OnClick(R.id.konfirmasi_toko_filter)
    public void onKonfirmasiTokoFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(1));
        dismiss();
    }

    @OnClick(R.id.produk_dikemas_filter)
    public void onDikemasFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(2));
        dismiss();

    }

    @OnClick(R.id.produk_dikirim_filter)
    public void onDikirimFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(3));
        dismiss();
    }

    @OnClick(R.id.selesai_filter)
    public void onSelesaiFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(4));
        dismiss();
    }

    @OnClick(R.id.cancelled_filter)
    public void onDibatalkanFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(-1));
        dismiss();
    }

    private Unbinder unbinder;

    private static BottomSheetOrderFragment instance;

    public static BottomSheetOrderFragment getInstance() {
        return instance == null ? new BottomSheetOrderFragment() : instance;
    }

    public BottomSheetOrderFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_order_filter,container,false);
        unbinder = ButterKnife.bind(this,itemView);
        return itemView;
    }
}
