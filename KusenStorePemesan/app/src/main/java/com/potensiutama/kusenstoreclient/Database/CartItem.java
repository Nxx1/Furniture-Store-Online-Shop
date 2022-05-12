package com.potensiutama.kusenstoreclient.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "Cart",primaryKeys = {"uid","produkId"})
public class CartItem {
    @NonNull
    @ColumnInfo(name = "produkId")
    private String produkId;

    @ColumnInfo(name = "produkName")
    private String produkName;

    @ColumnInfo(name = "produkImage")
    private String produkImage;

    @ColumnInfo(name = "produkPrice")
    private Double produkPrice;

    @ColumnInfo(name = "produkQuantity")
    private int produkQuantity;

    @ColumnInfo(name = "produkOngkir")
    private Double produkOngkir;

    @ColumnInfo(name = "userPhone")
    private String userPhone;

    @NonNull
    @ColumnInfo(name = "uid")
    private String uid;

    public Double getProdukOngkir() {
        return produkOngkir;
    }

    public void setProdukOngkir(Double produkOngkir) {
        this.produkOngkir = produkOngkir;
    }

    @NonNull
    public String getProdukId() {
        return produkId;
    }

    public void setProdukId(@NonNull String produkId) {
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

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof CartItem))
            return false;
        CartItem cartItem = (CartItem) obj;
        return cartItem.getProdukId().equals(this.produkId);
    }
}
