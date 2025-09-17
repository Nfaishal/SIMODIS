package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class Publikasi {

    @SerializedName("pub_id")
    private String pubId;

    @SerializedName("title")
    private String title;

    @SerializedName("rl_date")
    private String releaseDate;

    @SerializedName("cover")
    private String coverUrl;

    @SerializedName("pdf")
    private String pdfUrl;

    public Publikasi(String title, String coverUrl, String pdfUrl, String releaseDate, String pubId) {
        this.pubId = pubId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.pdfUrl = pdfUrl;
        this.releaseDate = releaseDate;
    }

    public String getPubId() {
        return pubId;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }
}