package com.potensiutama.kusenstoreadmin.calback;

import com.potensiutama.kusenstoreadmin.model.KategoryModel;

import java.util.List;

public interface IKategoriCallbackListener {
    void onCategoryLoadSuccess(List<KategoryModel> kategoryModelList);
    void onCategoryLoadFailed(String message);
}
