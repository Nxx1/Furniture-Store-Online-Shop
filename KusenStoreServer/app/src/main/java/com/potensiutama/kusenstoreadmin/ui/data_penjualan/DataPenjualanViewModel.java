package com.potensiutama.kusenstoreadmin.ui.data_penjualan;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.potensiutama.kusenstoreadmin.calback.IDataPenjualanCallbackListener;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.potensiutama.kusenstoreadmin.model.OrderModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataPenjualanViewModel extends ViewModel implements IDataPenjualanCallbackListener {

    private MutableLiveData<List<OrderModel>> dataPenjualanModelMutableLiveData;
    private MutableLiveData<String> messageError;

    private IDataPenjualanCallbackListener listener;

    public DataPenjualanViewModel() {
        dataPenjualanModelMutableLiveData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;
    }

    public MutableLiveData<List<OrderModel>> getDataPenjualanModelMutableLiveData() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        //loadOrderByStatus(4,formattedDate);
        loadSemuaData(4);
        return dataPenjualanModelMutableLiveData;
    }

    public void loadOrderByStatus(int status,String tanggal) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo(status);
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<OrderModel> tempList = new ArrayList<>();
                Common.totalDataPenjualan = 0;
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    OrderModel dataPenjualanModel = itemSnapshot.getValue(OrderModel.class);
                    String tanggalDb = simpleDateFormat.format(dataPenjualanModel.getCreateDate());
                    if(tanggalDb.equals(tanggal)) {
                        dataPenjualanModel.setKey(itemSnapshot.getKey());
                        tempList.add(dataPenjualanModel);
                        Common.totalDataPenjualan += dataPenjualanModel.getTotalPayment();
                    }
                }
                listener.onOrderLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onOrderLoadFailed(databaseError.getMessage());
            }
        });
    }

    public void loadSemuaData(int status) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<OrderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo(status);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Common.totalDataPenjualan = 0;
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    OrderModel dataPenjualanModel = itemSnapshot.getValue(OrderModel.class);
                    dataPenjualanModel.setKey(itemSnapshot.getKey());
                    tempList.add(dataPenjualanModel);
                    Common.totalDataPenjualan += dataPenjualanModel.getTotalPayment();
                }
                listener.onOrderLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onOrderLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onOrderLoadSuccess(List<OrderModel> dataPenjualanModelList) {

        if(dataPenjualanModelList.size() > 0){
            Collections.sort(dataPenjualanModelList,(orderModel, t1) ->{
                if(orderModel.getCreateDate() < t1.getCreateDate())
                    return -1;
                return orderModel.getCreateDate() == t1.getCreateDate() ? 0:1;
            });
        }

        dataPenjualanModelMutableLiveData.setValue(dataPenjualanModelList);

    }

    @Override
    public void onOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}
