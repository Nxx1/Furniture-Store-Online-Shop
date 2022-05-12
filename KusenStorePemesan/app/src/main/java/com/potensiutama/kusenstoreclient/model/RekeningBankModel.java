package com.potensiutama.kusenstoreclient.model;

public class RekeningBankModel {
    private String id,nama_lengkap,nama_bank;
    private Long nomor_rekening;

    public RekeningBankModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama_lengkap() {
        return nama_lengkap;
    }

    public void setNama_lengkap(String nama_lengkap) {
        this.nama_lengkap = nama_lengkap;
    }

    public String getNama_bank() {
        return nama_bank;
    }

    public void setNama_bank(String nama_bank) {
        this.nama_bank = nama_bank;
    }

    public Long getNomor_rekening() {
        return nomor_rekening;
    }

    public void setNomor_rekening(Long nomor_rekening) {
        this.nomor_rekening = nomor_rekening;
    }
}
