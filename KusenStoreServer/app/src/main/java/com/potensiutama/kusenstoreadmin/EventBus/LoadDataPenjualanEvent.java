package com.potensiutama.kusenstoreadmin.EventBus;

import java.util.Date;

public class LoadDataPenjualanEvent {
    private Date tanggal;

    public LoadDataPenjualanEvent(Date tanggal) {
        this.tanggal = tanggal;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }
}
