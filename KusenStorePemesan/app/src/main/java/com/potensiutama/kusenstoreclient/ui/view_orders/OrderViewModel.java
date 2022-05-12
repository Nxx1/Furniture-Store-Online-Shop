package com.potensiutama.kusenstoreclient.ui.view_orders;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.potensiutama.kusenstoreclient.Callback.ILoadOrderCallbackListener;
import com.potensiutama.kusenstoreclient.Common.Common;
import com.potensiutama.kusenstoreclient.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderViewModel extends ViewModel implements ILoadOrderCallbackListener {

    private MutableLiveData<List<OrderModel>> orderModelMutableLiveData;
    private MutableLiveData<String> messageError;

    private ILoadOrderCallbackListener listener;

    public OrderViewModel() {
        orderModelMutableLiveData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;
    }

    public MutableLiveData<List<OrderModel>> getOrderModelMutableLiveData() {
        loadOrderByStatus(0);
        return orderModelMutableLiveData;
    }

    public void loadOrderByStatus(int status) {
        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid());
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<OrderModel> tempList = new ArrayList<>();
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    OrderModel orderModel = itemSnapshot.getValue(OrderModel.class);
                    orderModel.setKey(itemSnapshot.getKey());
                    tempList.add(orderModel);
                }
                listener.onLoadOrderSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onLoadOrderFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onLoadOrderSuccess(List<OrderModel> orderModelList) {
        if(orderModelList.size() > 0){
            Collections.sort(orderModelList,(orderModel, t1) ->{
                if(orderModel.getCreateDate() > t1.getCreateDate())
                    return -1;
                return orderModel.getCreateDate() == t1.getCreateDate() ? 0:1;
            });
        }
        orderModelMutableLiveData.setValue(orderModelList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        messageError.setValue(message);
    }
}