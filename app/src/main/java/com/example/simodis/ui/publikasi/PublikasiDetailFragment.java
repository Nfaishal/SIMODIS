package com.example.simodis.ui.publikasi;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.simodis.R;
import com.example.simodis.data.model.PublikasiDetail;
import com.example.simodis.data.model.PublikasiDetailResponse;
import com.example.simodis.data.network.RetrofitClient;
import com.example.simodis.databinding.FragmentPublikasiDetailBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublikasiDetailFragment extends Fragment {

    private FragmentPublikasiDetailBinding binding;
    private String pubId;
    private PublikasiDetail currentPublikasi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pubId = getArguments().getString("pub_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPublikasiDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (pubId != null && !pubId.isEmpty()) {
            fetchPublikasiDetail(pubId);
        }
        setupListeners();
    }

    private void fetchPublikasiDetail(String id) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentScrollview.setVisibility(View.INVISIBLE);

        Call<PublikasiDetailResponse> call = RetrofitClient.getApiService().getPublikasiDetail(id);
        call.enqueue(new Callback<PublikasiDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<PublikasiDetailResponse> call, @NonNull Response<PublikasiDetailResponse> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentPublikasi = response.body().getData();
                    populateUI(currentPublikasi);
                    binding.contentScrollview.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "Gagal memuat detail publikasi.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PublikasiDetailResponse> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateUI(PublikasiDetail data) {
        Glide.with(requireContext())
                .load(data.getCoverUrl())
                .placeholder(R.color.grey)
                .into(binding.ivCover);

        binding.tvTitle.setText(data.getTitle());
        binding.tvReleaseDate.setText("Dirilis pada: " + data.getReleaseDate());
        binding.tvNoKatalog.setText(data.getNoKatalog());
        binding.tvNoPublikasi.setText(data.getNoPublikasi());
        binding.tvIssn.setText(data.getIssn());
        binding.tvUkuranFile.setText(data.getUkuranFile());

        // Menghilangkan tag HTML dari abstrak
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.tvAbstract.setText(Html.fromHtml(data.getAbstractText(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            binding.tvAbstract.setText(Html.fromHtml(data.getAbstractText()));
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnUnduh.setOnClickListener(v -> {
            if (currentPublikasi != null && currentPublikasi.getPdfUrl() != null) {
                downloadFile(currentPublikasi);
            } else {
                Toast.makeText(getContext(), "Link unduhan tidak tersedia.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFile(PublikasiDetail data) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(data.getPdfUrl()));
        String title = data.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";

        request.setTitle(data.getTitle());
        request.setDescription("Mengunduh publikasi...");
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
