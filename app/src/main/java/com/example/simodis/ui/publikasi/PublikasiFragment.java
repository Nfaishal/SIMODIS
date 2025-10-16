package com.example.simodis.ui.publikasi;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // --- BARU: Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.simodis.R;
import com.example.simodis.data.model.PageInfo;
import com.example.simodis.data.model.Publikasi;
import com.example.simodis.data.model.PublikasiPopuler;
import com.example.simodis.data.model.PublikasiResponse;
import com.example.simodis.data.network.RetrofitClient;
import com.example.simodis.databinding.FragmentPublikasiBinding;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublikasiFragment extends Fragment implements PublikasiAdapter.OnItemClickListener {

    private static final String TAG = "PublikasiFragment";

    private FragmentPublikasiBinding binding;
    private PublikasiAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPublikasiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupListeners();
        binding.toggleGroupFilter.check(binding.btnTerbaru.getId());
        fetchPublikasiData(1);
    }

    private void setupRecyclerView() {
        adapter = new PublikasiAdapter(getContext(), new ArrayList<>(), this);
        binding.rvPublikasi.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPublikasi.setAdapter(adapter);
    }

    private void setupListeners() {
        // Listener untuk tombol paginasi
        binding.btnPrev.setOnClickListener(v -> {
            if (currentPage > 1 && !isLoading) {
                fetchPublikasiData(currentPage - 1);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages && !isLoading) {
                fetchPublikasiData(currentPage + 1);
            }
        });

        // Listener untuk filter "Terbaru" dan "Populer"
        binding.toggleGroupFilter.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                // Hapus pencarian sebelumnya jika ada
                binding.searchEditText.setText("");
                if (checkedId == R.id.btn_terbaru) {
                    binding.searchInputLayout.setVisibility(View.VISIBLE);
                    fetchPublikasiData(1);
                } else if (checkedId == R.id.btn_populer) {
                    binding.searchInputLayout.setVisibility(View.GONE);
                    fetchPublikasiPopuler();
                }
            }
        });

        // Listener untuk search bar (debouncing)
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                searchRunnable = () -> {
                    if (query.isEmpty()) {
                        // Jika search bar kosong, muat kembali halaman pertama data terbaru
                        if (binding.toggleGroupFilter.getCheckedButtonId() == R.id.btn_terbaru) {
                            fetchPublikasiData(1);
                        }
                    } else {
                        searchPublikasi(query);
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500); // 500ms delay
            }
        });
    }

    private void updatePaginationUI() {
        binding.paginationLayout.setVisibility(View.VISIBLE);
        binding.tvPageInfo.setText(String.format("%d / %d", currentPage, totalPages));
        binding.btnPrev.setEnabled(currentPage > 1 && !isLoading);
        binding.btnNext.setEnabled(currentPage < totalPages && !isLoading);
    }

    private void fetchPublikasiData(int page) {
        if (isLoading) return;
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.paginationLayout.setVisibility(View.VISIBLE);

        Call<PublikasiResponse> call = RetrofitClient.getApiService().getPublikasi(page);

        // --- BARU: Logging URL Request ---
        Log.d(TAG, "Request URL: " + call.request().url());

        call.enqueue(new Callback<PublikasiResponse>() {
            @Override
            public void onResponse(@NonNull Call<PublikasiResponse> call, @NonNull Response<PublikasiResponse> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    // --- BARU: Logging Respons Sukses ---
                    Log.d(TAG, "onResponse (Success): Code " + response.code());
                    handleSuccessfulResponse(response.body());
                } else {
                    // --- BARU: Logging Respons Gagal ---
                    String errorBody = "N/A";
                    if (response.errorBody() != null) {
                        try { errorBody = response.errorBody().string(); } catch (IOException e) { e.printStackTrace(); }
                    }
                    Log.e(TAG, "onResponse (Error): Code " + response.code() + " | Message: " + response.message() + " | Body: " + errorBody);
                    Toast.makeText(getContext(), "Gagal memuat data. Kode: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PublikasiResponse> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;

                // --- BARU: Logging Kegagalan Koneksi ---
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchPublikasiPopuler() {
        if (isLoading) return;
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.paginationLayout.setVisibility(View.GONE);

        Call<List<PublikasiPopuler>> call = RetrofitClient.getApiService().getPublikasiPopuler();
        Log.d(TAG, "Request URL: " + call.request().url());

        call.enqueue(new Callback<List<PublikasiPopuler>>() {
            @Override
            public void onResponse(@NonNull Call<List<PublikasiPopuler>> call, @NonNull Response<List<PublikasiPopuler>> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "onResponse (Populer): Code " + response.code());
                    ArrayList<Publikasi> convertedList = new ArrayList<>();
                    for (PublikasiPopuler populer : response.body()) {
                        Publikasi publikasi = new Publikasi(
                                populer.getTitle(),
                                populer.getCoverUrl(),
                                populer.getPdfUrl(),
                                populer.getReleaseDate(),
                                populer.getPubId()
                        );
                        convertedList.add(publikasi);
                    }
                    adapter.updateData(convertedList);
                } else {
                    Log.e(TAG, "onResponse (Populer-Error): Code " + response.code());
                    Toast.makeText(getContext(), "Gagal memuat data populer.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<PublikasiPopuler>> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;
                Log.e(TAG, "onFailure (Populer): " + t.getMessage(), t);
                Toast.makeText(getContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchPublikasi(String keyword) {
        if (isLoading) return;
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.paginationLayout.setVisibility(View.GONE);

        Call<JsonElement> call = RetrofitClient.getApiService().searchPublikasi(keyword);
        Log.d(TAG, "Request URL: " + call.request().url());

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "onResponse (Search): Code " + response.code());
                    JsonElement jsonElement = response.body();

                    if (jsonElement.isJsonObject()) {
                        PublikasiResponse responseBody = new Gson().fromJson(jsonElement, PublikasiResponse.class);
                        handleSuccessfulResponse(responseBody);
                    } else if (jsonElement.isJsonPrimitive()) {
                        String message = jsonElement.getAsString();
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        adapter.updateData(new ArrayList<>());
                    }
                } else {
                    Log.e(TAG, "onResponse (Search-Error): Code " + response.code());
                    Toast.makeText(getContext(), "Gagal melakukan pencarian.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;
                Log.e(TAG, "onFailure (Search): " + t.getMessage(), t);
                Toast.makeText(getContext(), "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleSuccessfulResponse(PublikasiResponse responseBody) {
        if ("list-not-available".equals(responseBody.getDataAvailability())) {
            Toast.makeText(getContext(), "Publikasi tidak ditemukan.", Toast.LENGTH_SHORT).show();
            adapter.updateData(new ArrayList<>());
            binding.paginationLayout.setVisibility(View.GONE);
            return;
        }

        JsonElement dataElement = responseBody.getData();

        if (dataElement != null && dataElement.isJsonArray()) {
            List<Object> dataArray = new Gson().fromJson(dataElement, new TypeToken<List<Object>>(){}.getType());

            if (dataArray.size() == 2) {
                Gson gson = new Gson();
                PageInfo pageInfo = gson.fromJson(gson.toJson(dataArray.get(0)), PageInfo.class);
                currentPage = pageInfo.getPage();
                totalPages = pageInfo.getPages();

                Type publikasiListType = new TypeToken<ArrayList<Publikasi>>(){}.getType();
                List<Publikasi> publikasiList = gson.fromJson(gson.toJson(dataArray.get(1)), publikasiListType);

                adapter.updateData(new ArrayList<>(publikasiList));
                updatePaginationUI();
            }
        } else {
            Toast.makeText(getContext(), "Format data tidak dikenali.", Toast.LENGTH_SHORT).show();
            adapter.updateData(new ArrayList<>());
            binding.paginationLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemRootClicked(Publikasi publikasi) {
        Bundle bundle = new Bundle();
        bundle.putString("pub_id", publikasi.getPubId());
        Navigation.findNavController(requireView()).navigate(R.id.action_publikasiFragment_to_publikasiDetailFragment, bundle);
    }

    @Override
    public void onLihatClicked(Publikasi publikasi) {
        onItemRootClicked(publikasi);
    }

    @Override
    public void onUnduhClicked(Publikasi publikasi) {
        if (publikasi.getPdfUrl() != null && !publikasi.getPdfUrl().isEmpty()) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(publikasi.getPdfUrl()));
            String title = publikasi.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";

            request.setTitle(publikasi.getTitle());
            request.setDescription("Mengunduh publikasi...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);

            DownloadManager downloadManager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);

            Toast.makeText(getContext(), "Mulai mengunduh: " + publikasi.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Link unduhan tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hapus callback handler untuk mencegah memory leak
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}