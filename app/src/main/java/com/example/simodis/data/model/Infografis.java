package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class Infografis {
    @SerializedName("title")
    private String title;

    // URL untuk menampilkan gambar (thumbnail/preview)
    @SerializedName("img")
    private String imageUrl;

    // DITAMBAHKAN: URL untuk mengunduh file asli
    @SerializedName("dl")
    private String downloadUrl;

    // Buat Getter untuk semua variabel
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getDownloadUrl() { return downloadUrl; }
}
