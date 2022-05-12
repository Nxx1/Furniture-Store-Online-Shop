package com.potensiutama.kusenstoreclient.Callback;


import com.potensiutama.kusenstoreclient.model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);
}
