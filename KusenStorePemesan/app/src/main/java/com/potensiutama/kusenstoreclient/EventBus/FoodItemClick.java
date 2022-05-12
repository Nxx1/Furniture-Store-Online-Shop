package com.potensiutama.kusenstoreclient.EventBus;

import com.potensiutama.kusenstoreclient.model.ProdukModel;

public class FoodItemClick {
    private boolean success;
    private ProdukModel produkModel;

    public FoodItemClick(boolean success, ProdukModel produkModel) {
        this.success = success;
        this.produkModel = produkModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ProdukModel getFoodModel() {
        return produkModel;
    }

    public void setFoodModel(ProdukModel produkModel) {
        this.produkModel = produkModel;
    }
}
