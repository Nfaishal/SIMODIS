package com.example.simodis.ui.publikasi;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.simodis.data.model.PageInfo;
import com.example.simodis.data.model.Publikasi;
import com.example.simodis.data.model.PublikasiResponse;
import com.example.simodis.data.model.PublikasiPopuler;
import com.example.simodis.data.network.RetrofitClient;
import com.example.simodis.databinding.FragmentPublikasiBinding;
import com.example.simodis.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PublikasiFragment extends Fragment implements PublikasiAdapter.OnItemClickListener {

    private FragmentPublikasiBinding binding;
    private PublikasiAdapter adapter;
    private final List<Publikasi> originalPublikasiList = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private int currentPage = 1;
    private int totalPages = 1;

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
        setupSearchListener();
        setupFilterButtons();
        setupPaginationListeners();
        binding.btnTerbaru.setPressed(true);
        fetchPublikasiData(currentPage);
    }

    @Override
    public void onItemRootClicked(Publikasi publikasi) {
        Bundle bundle = new Bundle();
        bundle.putString("pub_id", publikasi.getPubId());
        Navigation.findNavController(requireView()).navigate(R.id.action_publikasiFragment_to_publikasiDetailFragment, bundle);
    }

    private void setupRecyclerView() {
        // Inisialisasi adapter dengan daftar kosong dan listener 'this' (Fragment ini sendiri)
        adapter = new PublikasiAdapter(getContext(), new ArrayList<>(), this);
        binding.rvPublikasi.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPublikasi.setAdapter(adapter);
    }

    private void setupPaginationListeners() {
        binding.btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                fetchPublikasiData(currentPage - 1);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                fetchPublikasiData(currentPage + 1);
            }
        });
    }

    private void updatePaginationUI() {
        binding.paginationLayout.setVisibility(View.VISIBLE);
        binding.tvPageInfo.setText(currentPage + " / " + totalPages);
        binding.btnPrev.setEnabled(currentPage > 1);
        binding.btnNext.setEnabled(currentPage < totalPages);
    }

    private void setupFilterButtons() {
        final TextWatcher searchWatcher = new TextWatcher() {
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
                String query = s.toString();
                searchRunnable = () -> {
                    if (query.isEmpty()) {
                        // Jika search bar kosong, muat kembali halaman pertama data terbaru
                        // Cek dulu apakah tombol "Terbaru" memang aktif
                        if (binding.btnTerbaru.isSelected()) {
                            fetchPublikasiData(1);
                        }
                    } else {
                        // Jika ada teks, lakukan pencarian
                        searchPublikasi(query);
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500);
            }
        };

        // Pasang listener ke search bar
        binding.searchEditText.addTextChangedListener(searchWatcher);

        binding.toggleGroupFilter.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == binding.btnTerbaru.getId()) {
                    binding.searchInputLayout.setVisibility(View.VISIBLE);
                    binding.paginationLayout.setVisibility(View.VISIBLE);
                    fetchPublikasiData(1);
                } else if (checkedId == binding.btnPopuler.getId()) {
                    binding.searchInputLayout.setVisibility(View.GONE);
                    binding.paginationLayout.setVisibility(View.GONE);
                    fetchPublikasiPopuler();
                }
            }
        });
    }

    private void fetchPublikasiData(int page) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.paginationLayout.setVisibility(View.VISIBLE);

        Call<PublikasiResponse> call = RetrofitClient.getApiService().getPublikasi(page);
        call.enqueue(new Callback<PublikasiResponse>() {
            @Override
            public void onResponse(@NonNull Call<PublikasiResponse> call, @NonNull Response<PublikasiResponse> response) {
                if (binding == null) {
                    return; // Hentikan eksekusi jika fragment sudah dihancurkan
                }

                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulResponse(response.body());
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data publikasi.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PublikasiResponse> call, @NonNull Throwable t) {
                if (binding == null) {
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchPublikasiPopuler() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.paginationLayout.setVisibility(View.GONE);

        Call<List<PublikasiPopuler>> call = RetrofitClient.getApiService().getPublikasiPopuler();
        call.enqueue(new Callback<List<PublikasiPopuler>>() {
            @Override
            public void onResponse(@NonNull Call<List<PublikasiPopuler>> call, @NonNull Response<List<PublikasiPopuler>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Publikasi> convertedList = new ArrayList<>();
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
                    adapter.filterList(new ArrayList<>(convertedList));
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data populer.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<PublikasiPopuler>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchPublikasi(String keyword) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.paginationLayout.setVisibility(View.GONE);

        // DIUBAH: Panggilan API sekarang mengharapkan JsonElement
        Call<JsonElement> call = RetrofitClient.getApiService().searchPublikasi(keyword);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement jsonElement = response.body();

                    // Cek apakah responsnya adalah Objek (struktur normal)
                    if (jsonElement.isJsonObject()) {
                        PublikasiResponse responseBody = new Gson().fromJson(jsonElement, PublikasiResponse.class);
                        handleSuccessfulResponse(responseBody);
                    }
                    // Cek apakah responsnya adalah String
                    else if (jsonElement.isJsonPrimitive()) {
                        String message = jsonElement.getAsString();
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        adapter.filterList(new ArrayList<>()); // Kosongkan daftar
                    }
                } else {
                    Toast.makeText(getContext(), "Gagal melakukan pencarian.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleSuccessfulResponse(PublikasiResponse responseBody) {
        // Cek dulu status dari 'data-availability'
        if (responseBody.getDataAvailability().equals("list-not-available")) {
            Toast.makeText(getContext(), "Publikasi tidak ditemukan.", Toast.LENGTH_SHORT).show();
            adapter.filterList(new ArrayList<>()); // Kosongkan daftar
            binding.paginationLayout.setVisibility(View.GONE); // Sembunyikan paginasi
            return; // Hentikan eksekusi
        }

        JsonElement dataElement = responseBody.getData();

        // Cek apakah 'data' adalah sebuah Array
        if (dataElement != null && dataElement.isJsonArray()) {
            List<Object> dataArray = new Gson().fromJson(dataElement, new TypeToken<List<Object>>(){}.getType());

            if (dataArray.size() == 2) {
                Gson gson = new Gson();
                // Ambil dan proses PageInfo
                PageInfo pageInfo = gson.fromJson(gson.toJson(dataArray.get(0)), PageInfo.class);
                currentPage = pageInfo.getPage();
                totalPages = pageInfo.getPages();

                // Ambil dan proses List<Publikasi>
                Type publikasiListType = new TypeToken<ArrayList<Publikasi>>(){}.getType();
                List<Publikasi> publikasiList = gson.fromJson(gson.toJson(dataArray.get(1)), publikasiListType);

                adapter.filterList(new ArrayList<>(publikasiList));
                updatePaginationUI();
            }
        } else {
            // Jika 'data' bukan array atau null, anggap tidak ada hasil
            Toast.makeText(getContext(), "Format data tidak dikenali.", Toast.LENGTH_SHORT).show();
            adapter.filterList(new ArrayList<>());
            binding.paginationLayout.setVisibility(View.GONE);
        }
    }

    private void setupSearchListener() {
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
                String query = s.toString();
                searchRunnable = () -> {
                    if (query.isEmpty()) {
                        // Jika search bar kosong, muat kembali halaman pertama data terbaru
                        binding.btnTerbaru.setPressed(true);
                        fetchPublikasiData(1);
                    } else {
                        // Jika ada teks, lakukan pencarian
                        searchPublikasi(query);
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500); // 500ms delay
            }
        });
    }

    private void filter(String text) {
        ArrayList<Publikasi> filteredList = new ArrayList<>();
        for (Publikasi item : originalPublikasiList) {
            // Filter berdasarkan judul publikasi
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (adapter != null) {
            adapter.filterList(filteredList);
        }
    }

    // Implementasi metode dari interface OnItemClickListener
    @Override
    public void onLihatClicked(Publikasi publikasi) {
        Bundle bundle = new Bundle();
        bundle.putString("pub_id", publikasi.getPubId());
        Navigation.findNavController(requireView()).navigate(R.id.action_publikasiFragment_to_publikasiDetailFragment, bundle);
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
        binding = null;
    }
}
