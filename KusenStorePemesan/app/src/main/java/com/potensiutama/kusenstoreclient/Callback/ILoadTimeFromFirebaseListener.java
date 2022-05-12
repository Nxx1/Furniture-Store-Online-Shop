package com.potensiutama.kusenstoreclient.Callback;

import com.potensiutama.kusenstoreclient.model.OrderModel;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
