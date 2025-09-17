package com.example.simodis.ui.infografis;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.bumptech.glide.Glide;
import com.example.simodis.R;
import com.example.simodis.data.model.Infografis;
import com.example.simodis.data.model.InfografisResponse;
import com.example.simodis.data.model.PageInfo;
import com.example.simodis.data.network.RetrofitClient;
import com.example.simodis.databinding.FragmentInfografisBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfografisFragment extends Fragment implements InfografisAdapter.OnImageClickListener {

    private FragmentInfografisBinding binding;
    private InfografisAdapter adapter;
    private int currentPage = 1;
    private int totalPages = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInfografisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupPaginationListeners();

        fetchInfografis(currentPage);
    }

    private void setupRecyclerView() {
        // Menggunakan GridLayoutManager dengan 2 kolom
        binding.rvInfografis.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // Inisialisasi adapter dengan daftar kosong dan listener
        adapter = new InfografisAdapter(getContext(), new ArrayList<>(), this);
        binding.rvInfografis.setAdapter(adapter);
    }

    private void setupPaginationListeners() {
        binding.btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                fetchInfografis(currentPage - 1);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                fetchInfografis(currentPage + 1);
            }
        });
    }

    private void updatePaginationUI() {
        binding.paginationLayout.setVisibility(View.VISIBLE);
        binding.tvPageInfo.setText(String.format("%d / %d", currentPage, totalPages));
        binding.btnPrev.setEnabled(currentPage > 1);
        binding.btnNext.setEnabled(currentPage < totalPages);
    }

    private void fetchInfografis(int page) {
        binding.progressBar.setVisibility(View.VISIBLE);

        Call<InfografisResponse> call = RetrofitClient.getApiService().getInfografis(page);
        call.enqueue(new Callback<InfografisResponse>() {
            @Override
            public void onResponse(@NonNull Call<InfografisResponse> call, @NonNull Response<InfografisResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulResponse(response.body());
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data infografis.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<InfografisResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleSuccessfulResponse(InfografisResponse responseBody) {
        List<Object> dataArray = responseBody.getData();
        if (dataArray != null && dataArray.size() == 2) {
            Gson gson = new Gson();
            // Ambil dan proses PageInfo dari elemen pertama array
            PageInfo pageInfo = gson.fromJson(gson.toJson(dataArray.get(0)), PageInfo.class);
            currentPage = pageInfo.getPage();
            totalPages = pageInfo.getPages();

            // Ambil dan proses List<Infografis> dari elemen kedua array
            Type infografisListType = new TypeToken<ArrayList<Infografis>>(){}.getType();
            List<Infografis> infografisList = gson.fromJson(gson.toJson(dataArray.get(1)), infografisListType);

            adapter.updateList(infografisList);
            updatePaginationUI();
        }
    }

    @Override
    public void onImageClicked(Infografis infografis) {
        // Logika untuk menampilkan dialog pop-up
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_preview);

        if (dialog.getWindow() != null) {
            // Mengatur lebar dialog agar sesuai dengan lebar layar
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // Membuat background dialog transparan agar sudut melengkung dari layout kita terlihat
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        ImageView ivPreview = dialog.findViewById(R.id.iv_preview);
        Button btnUnduh = dialog.findViewById(R.id.btn_unduh_gambar);

        // Muat gambar pratinjau ke dalam dialog
        Glide.with(requireContext())
                .load(infografis.getImageUrl())
                .into(ivPreview);

        // Tambahkan listener untuk tombol unduh di dalam dialog
        btnUnduh.setOnClickListener(v -> {
            downloadImage(infografis);
            dialog.dismiss(); // Tutup dialog setelah tombol diklik
        });

        dialog.show();
    }

    private void downloadImage(Infografis infografis) {
        if (infografis.getImageUrl() != null && !infografis.getImageUrl().isEmpty()) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(infografis.getImageUrl()));
            String title = infografis.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".png";

            request.setTitle(infografis.getTitle());
            request.setDescription("Mengunduh infografis...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);

            DownloadManager downloadManager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(getContext(), "Mulai mengunduh: " + infografis.getTitle(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Tautan unduhan tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
