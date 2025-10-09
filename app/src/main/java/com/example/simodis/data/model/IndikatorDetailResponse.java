package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class IndikatorDetailResponse {
    @SerializedName("master")
    private IndikatorMaster master;
    @SerializedName("history")
    private List<IndikatorNilai> history;
    @SerializedName("pembanding")
    private List<IndikatorPembanding> pembanding;

    // Buat Getter untuk semua variabel
    public IndikatorMaster getMaster() { return master; }
    public List<IndikatorNilai> getHistory() { return history; }
    public List<IndikatorPembanding> getPembanding() { return pembanding; }

}