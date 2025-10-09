package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class IndikatorMaster {
    @SerializedName("id_indikator")
    private int idIndikator;
    @SerializedName("nama_indikator")
    private String namaIndikator;
    @SerializedName("satuan")
    private String satuan;
    @SerializedName("deskripsi")
    private String deskripsi;
    @SerializedName("sumber_url")
    private String sumberUrl;
    @SerializedName("pembanding")
    private int perbandingan;

    public int getIdIndikator() { return idIndikator; }
    public String getNamaIndikator() { return namaIndikator; }
    public String getSatuan() { return satuan; }
    public String getDeskripsi() { return deskripsi; }
    public String getSumberUrl() { return sumberUrl; }
    public int getPerbandingan() { return perbandingan; }
    public boolean hasPerbandingan() { return perbandingan == 1; }

}