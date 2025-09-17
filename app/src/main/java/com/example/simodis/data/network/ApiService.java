package com.example.simodis.data.network;

import com.example.simodis.data.model.DataSektoral;
import com.example.simodis.data.model.IndikatorStrategis;
import com.example.simodis.data.model.IndikatorDetailResponse;
import com.example.simodis.data.model.InfografisResponse;
import com.example.simodis.data.model.PublikasiResponse;
import com.example.simodis.data.model.PublikasiPopuler;
import com.example.simodis.data.model.TabelStatisResponse;
import com.example.simodis.data.model.PublikasiDetailResponse;
import com.google.gson.JsonElement;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Path;
import okhttp3.ResponseBody;
import retrofit2.http.Streaming;

public interface ApiService {

    @GET("api/indikator-strategis")
    Call<List<IndikatorStrategis>> getIndikatorStrategis();
    @GET("api/indikator/search")
    Call<List<IndikatorStrategis>> searchIndikator(@Query("q") String keyword);
    @GET("api/indikator-strategis/{id}")
    Call<IndikatorDetailResponse> getIndikatorDetail(@Path("id") int indikatorId);
    @Streaming // Penting untuk unduhan file besar
    @GET("api/indikator-strategis/{id}/export-excel")
    Call<ResponseBody> downloadIndikatorExcel(@Path("id") int indikatorId);


    @GET("api/publikasi")
    Call<PublikasiResponse> getPublikasi();
    @GET("api/publikasi/{page}")
    Call<PublikasiResponse> getPublikasi(@Path("page") int page);
    @GET("api/publikasi/search")
    Call<JsonElement> searchPublikasi(@Query("keyword") String keyword);
    @GET("api/publikasi/populer")
    Call<List<PublikasiPopuler>> getPublikasiPopuler();
    @GET("api/publikasi/detail/{id}")
    Call<PublikasiDetailResponse> getPublikasiDetail(@Path("id") String pubId);

    @GET("api/infografis")
    Call<InfografisResponse> getInfografis();
    @GET("api/infografis/{page}")
    Call<InfografisResponse> getInfografis(@Path("page") int page);


    @GET("api/data-sektoral")
    Call<List<DataSektoral>> getDataSektoral();
    @GET("api/data-sektoral/search") // Sesuaikan dengan route di backend Anda
    Call<List<DataSektoral>> searchDataSektoral(@Query("keyword") String keyword);


    @GET("api/statictable")
    Call<TabelStatisResponse> getTabelStatis();
    @GET("api/statictable/{page}")
    Call<TabelStatisResponse> getTabelStatis(@Path("page") int page);
    @GET("api/statictable/search") // Sesuaikan dengan route di backend Anda
    Call<JsonElement> searchTabelStatis(@Query("keyword") String keyword);
}

