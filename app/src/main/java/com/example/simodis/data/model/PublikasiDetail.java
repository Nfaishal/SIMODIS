package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class PublikasiDetail {
    @SerializedName("title") private String title;
    @SerializedName("rl_date") private String releaseDate;
    @SerializedName("kat_no") private String noKatalog;
    @SerializedName("pub_no") private String noPublikasi;
    @SerializedName("issn") private String issn;
    @SerializedName("size") private String ukuranFile;
    @SerializedName("abstract") private String abstractText;
    @SerializedName("cover") private String coverUrl;
    @SerializedName("pdf") private String pdfUrl;

    // Buat Getter untuk semua variabel
    public String getTitle() { return title; }
    public String getReleaseDate() { return releaseDate; }
    public String getNoKatalog() { return noKatalog; }
    public String getNoPublikasi() { return noPublikasi; }
    public String getIssn() { return issn; }
    public String getUkuranFile() { return ukuranFile; }
    public String getAbstractText() { return abstractText; }
    public String getCoverUrl() { return coverUrl; }
    public String getPdfUrl() { return pdfUrl; }
}
