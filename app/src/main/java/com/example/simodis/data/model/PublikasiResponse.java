package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonElement;

public class PublikasiResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("data-availability")
    private String dataAvailability;

    @SerializedName("data")
    private JsonElement data;

    public String getStatus() { return status; }
    public String getDataAvailability() { return dataAvailability; }
    public JsonElement getData() { return data; }
}
