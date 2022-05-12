package com.potensiutama.kusenstoreadmin.calback;

import com.potensiutama.kusenstoreadmin.model.OrderModel;

import java.util.List;

public interface IDataPenjualanCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> dataPenjualanModelList);
    void onOrderLoadFailed(String message);
}
