package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class PublikasiPopuler {

    @SerializedName("title")
    private String title;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("cover_url")
    private String coverUrl;

    @SerializedName("pdf_url")
    private String pdfUrl;

    @SerializedName("pub_id")
    private String pubId;


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

    public String getPubId() {
        return pubId;
    }
}
