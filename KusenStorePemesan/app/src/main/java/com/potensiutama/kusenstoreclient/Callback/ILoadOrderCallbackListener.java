package com.potensiutama.kusenstoreclient.Callback;

import com.potensiutama.kusenstoreclient.model.OrderModel;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<OrderModel> orderList);
    void onLoadOrderFailed(String message);
}
