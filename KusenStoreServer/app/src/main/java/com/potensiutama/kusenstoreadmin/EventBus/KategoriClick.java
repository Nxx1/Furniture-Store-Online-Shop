package com.potensiutama.kusenstoreadmin.EventBus;

import com.potensiutama.kusenstoreadmin.model.KategoryModel;

public class KategoriClick {
    private boolean success;
    private KategoryModel kategoryModel;

    public KategoriClick(boolean success, KategoryModel kategoryModel) {
        this.success = success;
        this.kategoryModel = kategoryModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public KategoryModel getKategoryModel() {
        return kategoryModel;
    }

    public void setKategoryModel(KategoryModel kategoryModel) {
        this.kategoryModel = kategoryModel;
    }
}