package com.example.simodis.ui.tabelstatis;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.simodis.data.model.InfografisResponse;
import com.example.simodis.data.model.PageInfo;
import com.example.simodis.data.model.Publikasi;
import com.example.simodis.data.model.PublikasiResponse;
import com.example.simodis.data.model.TabelStatis;
import com.example.simodis.data.model.TabelStatisResponse;
import com.example.simodis.data.network.RetrofitClient;
import com.example.simodis.databinding.FragmentTabelStatisBinding; // Pastikan nama file layout sesuai
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TabelStatisFragment extends Fragment implements TabelStatisAdapter.OnItemClickListener {
    private FragmentTabelStatisBinding binding;
    private TabelStatisAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private int currentPage = 1;
    private int totalPages = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTabelStatisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearchListener();
        setupPaginationListeners();
        fetchTabelStatis(1); // Muat semua data saat pertama kali dibuka
    }

    private void setupRecyclerView() {
        adapter = new TabelStatisAdapter(new ArrayList<>(), this);
        binding.rvTabelStatis.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTabelStatis.setAdapter(adapter);
    }

    private void fetchTabelStatis(int page) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.paginationLayout.setVisibility(View.VISIBLE); // Tampilkan paginasi

        Call<TabelStatisResponse> call = RetrofitClient.getApiService().getTabelStatis(page);
        call.enqueue(new Callback<TabelStatisResponse>() {
            @Override
            public void onResponse(@NonNull Call<TabelStatisResponse> call, @NonNull Response<TabelStatisResponse> response) {
                if (binding == null) {
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulResponse(response.body());
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TabelStatisResponse> call, @NonNull Throwable t) {
                if (binding == null) {
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleSuccessfulResponse(TabelStatisResponse responseBody) {
        if (responseBody.getDataAvailability() != null && responseBody.getDataAvailability().equals("list-not-available")) {
            Toast.makeText(getContext(), "Tabel data tidak ditemukan.", Toast.LENGTH_SHORT).show();
            adapter.filterList(new ArrayList<>());
            binding.paginationLayout.setVisibility(View.GONE);
            return;
        }

        JsonElement dataElement = responseBody.getData();

        List<Object> dataArray = new Gson().fromJson(dataElement, new TypeToken<List<Object>>(){}.getType());

        if (dataArray != null && dataArray.size() == 2) {
            Gson gson = new Gson();
            PageInfo pageInfo = gson.fromJson(gson.toJson(dataArray.get(0)), PageInfo.class);
            currentPage = pageInfo.getPage();
            totalPages = pageInfo.getPages();

            Type listType = new TypeToken<ArrayList<TabelStatis>>(){}.getType();
            List<TabelStatis> listData = gson.fromJson(gson.toJson(dataArray.get(1)), listType);

            adapter.filterList(new ArrayList<>(listData));
            updatePaginationUI();
        } else {
            // Jika struktur data tidak sesuai, anggap tidak ada hasil
            adapter.filterList(new ArrayList<>());
            Toast.makeText(getContext(), "Tidak ada hasil ditemukan.", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchTabelStatis(String keyword) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.paginationLayout.setVisibility(View.GONE);

        Call<JsonElement> call = RetrofitClient.getApiService().searchTabelStatis(keyword);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {

                    JsonElement jsonElement = response.body();

                    if (jsonElement.isJsonObject()) {

                        TabelStatisResponse responseBody = new Gson().fromJson(jsonElement, TabelStatisResponse.class);
                        handleSuccessfulResponse(responseBody);

                    }
                    else if (jsonElement.isJsonPrimitive()) {
                        String message = jsonElement.getAsString();
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        adapter.filterList(new ArrayList<>());
                        binding.paginationLayout.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(getContext(), "Gagal memuat data publikasi.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updatePaginationUI() {
        binding.tvPageInfo.setText(String.format("%d / %d", currentPage, totalPages));
        binding.btnPrev.setEnabled(currentPage > 1);
        binding.btnNext.setEnabled(currentPage < totalPages);
    }

    private void setupPaginationListeners() {
        binding.btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) fetchTabelStatis(currentPage - 1);
        });
        binding.btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) fetchTabelStatis(currentPage + 1);
        });
    }

    private void setupSearchListener() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            }
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                searchRunnable = () -> {
                    if (query.isEmpty()) {
                        fetchTabelStatis(1);
                    } else {
                        searchTabelStatis(query);
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });
    }

    @Override
    public void onUnduhClicked(TabelStatis data) {
        String url = data.getExcelUrl();
        if (url == null || url.isEmpty()) {
            Toast.makeText(getContext(), "Link unduhan tidak tersedia.", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String title = data.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".xlsx";

        request.setTitle(data.getTitle());
        request.setDescription("Mengunduh tabel data...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);

        DownloadManager downloadManager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(getContext(), "Mulai mengunduh...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
