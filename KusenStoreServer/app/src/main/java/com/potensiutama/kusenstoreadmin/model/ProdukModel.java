package com.potensiutama.kusenstoreadmin.model;

public class ProdukModel {
    private String nama_produk, gambar1,gambar2,gambar3,gambar4,gambar5,id, deskripsi,kategori;
    private Long harga_produk,stok_produk,berat_produk,lebar_produk,panjang_produk,tinggi_produk,ongkir_perkm;
    private Boolean preorder,unggulan;

    private int positionInList = -1;

    public ProdukModel() {
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public Boolean getUnggulan() {
        return unggulan;
    }

    public void setUnggulan(Boolean unggulan) {
        this.unggulan = unggulan;
    }

    public Boolean getPreorder() {
        return preorder;
    }

    public void setPreorder(Boolean preorder) {
        this.preorder = preorder;
    }

    public Long getStok_produk() {
        return stok_produk;
    }

    public void setStok_produk(Long stok_produk) {
        this.stok_produk = stok_produk;
    }

    public Long getBerat_produk() {
        return berat_produk;
    }

    public void setBerat_produk(Long berat_produk) {
        this.berat_produk = berat_produk;
    }

    public Long getLebar_produk() {
        return lebar_produk;
    }

    public void setLebar_produk(Long lebar_produk) {
        this.lebar_produk = lebar_produk;
    }

    public Long getPanjang_produk() {
        return panjang_produk;
    }

    public void setPanjang_produk(Long panjang_produk) {
        this.panjang_produk = panjang_produk;
    }

    public Long getTinggi_produk() {
        return tinggi_produk;
    }

    public void setTinggi_produk(Long tinggi_produk) {
        this.tinggi_produk = tinggi_produk;
    }

    public Long getOngkir_perkm() {
        return ongkir_perkm;
    }

    public void setOngkir_perkm(Long ongkir_perkm) {
        this.ongkir_perkm = ongkir_perkm;
    }

    public String getGambar2() {
        return gambar2;
    }

    public void setGambar2(String gambar2) {
        this.gambar2 = gambar2;
    }

    public String getGambar3() {
        return gambar3;
    }

    public void setGambar3(String gambar3) {
        this.gambar3 = gambar3;
    }

    public String getGambar4() {
        return gambar4;
    }

    public void setGambar4(String gambar4) {
        this.gambar4 = gambar4;
    }

    public String getGambar5() {
        return gambar5;
    }

    public void setGambar5(String gambar5) {
        this.gambar5 = gambar5;
    }

    public String getNama_produk() {
        return nama_produk;
    }

    public void setNama_produk(String nama_produk) {
        this.nama_produk = nama_produk;
    }

    public String getGambar1() {
        return gambar1;
    }

    public void setGambar1(String gambar1) {
        this.gambar1 = gambar1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public Long getHarga_produk() {
        return harga_produk;
    }

    public void setHarga_produk(Long harga_produk) {
        this.harga_produk = harga_produk;
    }

    public int getPositionInList() {
        return positionInList;
    }

    public void setPositionInList(int positionInList) {
        this.positionInList = positionInList;
    }
}
