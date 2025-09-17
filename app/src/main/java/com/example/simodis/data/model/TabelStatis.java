package com.example.simodis.data.model;

import com.google.gson.annotations.SerializedName;

public class TabelStatis {
    @SerializedName("table_id")
    private String tableId;

    @SerializedName("title")
    private String title;

    @SerializedName("subj")
    private String subject;

    @SerializedName("updt_date")
    private String lastUpdate;

    @SerializedName("excel")
    private String excelUrl;

    // Buat Getter untuk semua variabel
    public String getTableId() { return tableId; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getLastUpdate() { return lastUpdate; }
    public String getExcelUrl() { return excelUrl; }
}
