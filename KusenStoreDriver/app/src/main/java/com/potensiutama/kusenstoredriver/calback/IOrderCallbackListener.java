package com.potensiutama.kusenstoredriver.calback;

import com.potensiutama.kusenstoredriver.model.OrderModel;

import java.util.List;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);
}
