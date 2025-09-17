package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InfografisResponse {
    @SerializedName("data")
    private List<Object> data;

    public List<Object> getData() { return data; }
}
