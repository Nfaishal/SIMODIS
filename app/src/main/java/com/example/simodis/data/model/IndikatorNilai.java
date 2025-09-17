package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class IndikatorNilai {
    @SerializedName("id_nilai")
    private int idNilai;
    @SerializedName("id_indikator")
    private int idIndikator;
    @SerializedName("tahun")
    private int tahun;
    @SerializedName("nilai")
    private String nilai;

    // Buat Getter untuk semua variabel

    public int getIdNilai() { return idNilai; }
    public int getIdIndikator() { return idIndikator; }
    public int getTahun() { return tahun; }
    public String getNilai() { return nilai; }
}