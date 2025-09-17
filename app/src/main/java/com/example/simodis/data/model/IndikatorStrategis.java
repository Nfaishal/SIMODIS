package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class IndikatorStrategis {
    @SerializedName("id_indikator")
    private int idIndikator;

    @SerializedName("nama_indikator")
    private String namaIndikator;

    @SerializedName("satuan")
    private String satuan;

    @SerializedName("tahun")
    private int tahun;

    @SerializedName("nilai")
    private String nilai;

    @SerializedName("image_name")
    private String imageName;

    @SerializedName("color_theme")
    private String colorTheme;

    public int getIdIndikator() { return idIndikator; }
    public String getNamaIndikator() { return namaIndikator; }
    public String getSatuan() { return satuan; }
    public int getTahun() { return tahun; }
    public String getNilai() { return nilai; }
    public String getImageName() { return imageName; }
    public String getColorTheme() { return colorTheme; }
}