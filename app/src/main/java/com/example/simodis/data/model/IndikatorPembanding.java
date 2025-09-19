package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class IndikatorPembanding {
    @SerializedName("id_pembanding")
    private int idPembanding;
    @SerializedName("id_indikator")
    private int idIndikator;
    @SerializedName("tahun")
    private int tahun;
    @SerializedName("level")
    private String level;
    @SerializedName("nilai_pembanding")
    private String nilaiPembanding;

    // Buat Getter untuk semua variabel

    public int getIdPembanding() { return idPembanding; }
    public int getIdIndikator() { return idIndikator; }
    public int getTahun() { return tahun; }
    public String getLevel() { return level; }
    public String getNilaiPembanding() { return nilaiPembanding; }
}