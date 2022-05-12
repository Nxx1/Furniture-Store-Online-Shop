package com.potensiutama.kusenstoreclient.model;

public class InformasiAkunSayaModel {

    private String email,alamat_lengkap;
    private Long no_hp;
    private Double koordinatLat,koordinatLong;

    public InformasiAkunSayaModel() {
    }

    public InformasiAkunSayaModel(String email, String alamat_lengkap, Long no_hp, Double koordinatLat, Double koordinatLong) {
        this.email = email;
        this.alamat_lengkap = alamat_lengkap;
        this.no_hp = no_hp;
        this.koordinatLat = koordinatLat;
        this.koordinatLong = koordinatLong;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlamat_lengkap() {
        return alamat_lengkap;
    }

    public void setAlamat_lengkap(String alamat_lengkap) {
        this.alamat_lengkap = alamat_lengkap;
    }

    public Long getNo_hp() {
        return no_hp;
    }

    public void setNo_hp(Long no_hp) {
        this.no_hp = no_hp;
    }

    public Double getKoordinatLat() {
        return koordinatLat;
    }

    public void setKoordinatLat(Double koordinatLat) {
        this.koordinatLat = koordinatLat;
    }

    public Double getKoordinatLong() {
        return koordinatLong;
    }

    public void setKoordinatLong(Double koordinatLong) {
        this.koordinatLong = koordinatLong;
    }
}
