package com.potensiutama.kusenstoreclient.Callback;

import com.potensiutama.kusenstoreclient.model.ProdukModel;

import java.util.List;

public interface IProdukCallbackListener {
    void onProdukLoadSuccess(List<ProdukModel> produkModelList);
    void onProdukLoadFailed(String message);
}
