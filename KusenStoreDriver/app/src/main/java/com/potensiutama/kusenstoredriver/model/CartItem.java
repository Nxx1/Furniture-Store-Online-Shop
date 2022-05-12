package com.potensiutama.kusenstoredriver.model;

import androidx.annotation.NonNull;

public class CartItem {

    private String produkId;
    private String produkName;
    private String produkImage;
    private Double produkPrice;
    private int produkQuantity;
    private Double produkOngkir;
    private String userPhone;
    private String uid;

    public CartItem() {
    }

    public String getProdukId() {
        return produkId;
    }

    public void setProdukId(String produkId) {
        this.produkId = produkId;
    }

    public String getProdukName() {
        return produkName;
    }

    public void setProdukName(String produkName) {
        this.produkName = produkName;
    }

    public String getProdukImage() {
        return produkImage;
    }

    public void setProdukImage(String produkImage) {
        this.produkImage = produkImage;
    }

    public Double getProdukPrice() {
        return produkPrice;
    }

    public void setProdukPrice(Double produkPrice) {
        this.produkPrice = produkPrice;
    }

    public int getProdukQuantity() {
        return produkQuantity;
    }

    public void setProdukQuantity(int produkQuantity) {
        this.produkQuantity = produkQuantity;
    }

    public Double getProdukOngkir() {
        return produkOngkir;
    }

    public void setProdukOngkir(Double produkOngkir) {
        this.produkOngkir = produkOngkir;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
