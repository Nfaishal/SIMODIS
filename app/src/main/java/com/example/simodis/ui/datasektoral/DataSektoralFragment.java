package com.example.simodis.ui.datasektoral;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.simodis.data.model.DataSektoral;
import com.example.simodis.data.network.RetrofitClient;
import com.example.simodis.databinding.FragmentDataSektoralBinding;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class DataSektoralFragment extends Fragment implements DataSektoralAdapter.OnItemClickListener {

    private FragmentDataSektoralBinding binding;
    private DataSektoralAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDataSektoralBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearchListener();
        fetchDataSektoral(""); // Muat semua data saat pertama kali dibuka
    }

    private void setupRecyclerView() {
        adapter = new DataSektoralAdapter(new ArrayList<>(), this);
        binding.rvDataSektoral.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvDataSektoral.setAdapter(adapter);
    }

    private void fetchDataSektoral(String keyword) {
        binding.progressBar.setVisibility(View.VISIBLE);
        Call<List<DataSektoral>> call;
        if (keyword.isEmpty()) {
            call = RetrofitClient.getApiService().getDataSektoral();
        } else {
            call = RetrofitClient.getApiService().searchDataSektoral(keyword);
        }

        call.enqueue(new Callback<List<DataSektoral>>() {
            @Override
            public void onResponse(@NonNull Call<List<DataSektoral>> call, @NonNull Response<List<DataSektoral>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.filterList(new ArrayList<>(response.body()));
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DataSektoral>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
                searchRunnable = () -> fetchDataSektoral(query);
                searchHandler.postDelayed(searchRunnable, 500); // 500ms delay
            }
        });
    }

    @Override
    public void onLihatClicked(DataSektoral data) {
        if (data.getLinkSpreadsheet() != null && !data.getLinkSpreadsheet().isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getLinkSpreadsheet()));
            startActivity(browserIntent);
        } else {
            Toast.makeText(getContext(), "Link tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUnduhClicked(DataSektoral data) {
        String originalLink = data.getLinkSpreadsheet();
        if (originalLink != null && !originalLink.isEmpty()) {
            String downloadLink = originalLink + "/export?file=xls";

            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadLink));
                String title = data.getJudulTabel().replaceAll("[^a-zA-Z0-9.-]", "_") + ".xlsx";

                request.setTitle(data.getJudulTabel());
                request.setDescription("Mengunduh data sektoral...");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);

                DownloadManager downloadManager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                if (downloadManager != null) {
                    downloadManager.enqueue(request);
                    Toast.makeText(getContext(), "Mulai mengunduh: " + data.getJudulTabel(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // Jika URL yang dimodifikasi tidak valid, buka saja link aslinya di browser sebagai fallback
                Toast.makeText(getContext(), "Gagal memulai unduhan, membuka link di browser.", Toast.LENGTH_LONG).show();
                onLihatClicked(data);
            }

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