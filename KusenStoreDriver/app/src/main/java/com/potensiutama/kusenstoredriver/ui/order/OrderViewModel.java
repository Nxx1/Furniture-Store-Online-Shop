package com.potensiutama.kusenstoredriver.ui.order;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.potensiutama.kusenstoredriver.calback.IOrderCallbackListener;
import com.potensiutama.kusenstoredriver.common.Common;
import com.potensiutama.kusenstoredriver.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderViewModel extends ViewModel implements IOrderCallbackListener {

    private MutableLiveData<List<OrderModel>> orderModelMutableLiveData;
    private MutableLiveData<String> messageError;

    private IOrderCallbackListener listener;

    public OrderViewModel() {
        orderModelMutableLiveData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;
    }

    public MutableLiveData<List<OrderModel>> getOrderModelMutableLiveData() {
        loadOrderByStatus(-2);
        //loadAllOrder();
        return orderModelMutableLiveData;
    }

    public void loadAllOrder() {
        List<OrderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("createDate");
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    OrderModel orderModel = itemSnapshot.getValue(OrderModel.class);
                    orderModel.setKey(itemSnapshot.getKey());
                    tempList.add(orderModel);
                }
                listener.onOrderLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onOrderLoadFailed(databaseError.getMessage());
            }
        });
    }

    public void loadOrderByStatus(int status) {
        List<OrderModel> tempList = new ArrayList<>();
        Query orderRef;
        if(status == -2){
            orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                    .orderByChild("createDate");
        }else{
            orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                    .orderByChild("orderStatus")
                    .equalTo(status);
        }

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    OrderModel orderModel = itemSnapshot.getValue(OrderModel.class);
                    orderModel.setKey(itemSnapshot.getKey());
                    if(orderModel.getIdKurir() != null){
                        if(orderModel.getIdKurir().equals(Common.currentServerUser.getPhone())){
                            tempList.add(orderModel);
                        }
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

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onOrderLoadSuccess(List<OrderModel> orderModelList) {

        if(orderModelList.size() > 0){
            Collections.sort(orderModelList,(orderModel,t1) ->{
                if(orderModel.getCreateDate() < t1.getCreateDate())
                    return -1;
                return orderModel.getCreateDate() == t1.getCreateDate() ? 0:1;
            });
        }

        orderModelMutableLiveData.setValue(orderModelList);

    }

    @Override
    public void onOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}