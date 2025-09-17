package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class PublikasiDetailResponse {
    @SerializedName("data")
    private PublikasiDetail data;

    public PublikasiDetail getData() {
        return data;
    }
}
