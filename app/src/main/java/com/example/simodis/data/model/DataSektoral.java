package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class DataSektoral {

    @SerializedName("judul_tabel")
    private String judulTabel;

    @SerializedName("sumber_instansi")
    private String sumberInstansi;

    @SerializedName("tahun_rilis")
    private int tahunRilis;

    @SerializedName("link_spreadsheet")
    private String linkSpreadsheet;

    // Buat Getter untuk semua variabel
    public String getJudulTabel() { return judulTabel; }
    public String getSumberInstansi() { return sumberInstansi; }
    public int getTahunRilis() { return tahunRilis; }
    public String getLinkSpreadsheet() { return linkSpreadsheet; }
}
