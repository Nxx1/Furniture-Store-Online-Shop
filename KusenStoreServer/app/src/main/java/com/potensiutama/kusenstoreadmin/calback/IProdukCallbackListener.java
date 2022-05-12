package com.potensiutama.kusenstoreadmin.calback;
import com.potensiutama.kusenstoreadmin.model.ProdukModel;

import java.util.List;

public interface IProdukCallbackListener {
    void onProdukLoadSuccess(List<ProdukModel> produkModelList);
    void onProdukLoadFailed(String message);
}
