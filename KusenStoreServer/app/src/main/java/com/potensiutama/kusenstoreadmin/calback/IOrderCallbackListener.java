package com.potensiutama.kusenstoreadmin.calback;

import com.potensiutama.kusenstoreadmin.model.OrderModel;

import java.util.List;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);
}
