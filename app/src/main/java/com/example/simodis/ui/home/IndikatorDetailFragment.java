package com.example.simodis.ui.home;

import android.app.DownloadManager;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.simodis.R;
import com.example.simodis.data.model.IndikatorDetailResponse;
import com.example.simodis.data.model.IndikatorNilai;
import com.example.simodis.data.network.RetrofitClient;
import com.example.simodis.databinding.FragmentIndikatorDetailBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndikatorDetailFragment extends Fragment {

    private FragmentIndikatorDetailBinding binding;
    private int indikatorId;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Jika izin diberikan, coba simpan lagi
                    saveChartToGallery();
                } else {
                    Toast.makeText(getContext(), "Izin penyimpanan ditolak.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            indikatorId = getArguments().getInt("indikator_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIndikatorDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (indikatorId > 0) {
            fetchIndicatorDetail(indikatorId);
        }
        setupButtonListeners();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void fetchIndicatorDetail(int id) {
        Call<IndikatorDetailResponse> call = RetrofitClient.getApiService().getIndikatorDetail(id);
        call.enqueue(new Callback<IndikatorDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<IndikatorDetailResponse> call, @NonNull Response<IndikatorDetailResponse> response) {
                if (binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    populateUI(response.body());
                } else {
                    Toast.makeText(getContext(), "Gagal memuat detail indikator.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<IndikatorDetailResponse> call, @NonNull Throwable t) {
                if (binding == null) return;
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateUI(IndikatorDetailResponse data) {
        if (data.getMaster() != null) {
            binding.chipIndicatorTitle.setText(data.getMaster().getNamaIndikator());
            binding.tvDescription.setText(data.getMaster().getDeskripsi());
        }

        if (data.getHistory() != null && !data.getHistory().isEmpty()) {
            populateTable(data.getHistory());
            setupChart(data);
        }
    }

    private void populateTable(List<IndikatorNilai> history) {
        int historySize = history.size();
        TextView[] headers = {binding.tvYear3Header, binding.tvYear2Header, binding.tvYear1Header};
        TextView[] values = {binding.tvYear3Value, binding.tvYear2Value, binding.tvYear1Value};

        for (int i = 0; i < 3; i++) {
            int dataIndex = historySize - 1 - i;
            if (dataIndex >= 0) {
                IndikatorNilai item = history.get(dataIndex);
                headers[i].setText(String.valueOf(item.getTahun()));
                values[i].setText(item.getNilai().replace(",", "."));
                headers[i].setVisibility(View.VISIBLE);
                values[i].setVisibility(View.VISIBLE);
            } else {
                headers[i].setVisibility(View.GONE);
                values[i].setVisibility(View.GONE);
            }
        }
    }

    private void setupChart(IndikatorDetailResponse data) {
        List<IndikatorNilai> history = data.getHistory();
        if (history == null || history.isEmpty()) return;

        LineChart chart = binding.lineChart;
        ArrayList<Entry> entries = new ArrayList<>();

        for (IndikatorNilai nilai : history) {
            try {
                float floatValue = Float.parseFloat(nilai.getNilai().replace(",", "."));
                entries.add(new Entry(nilai.getTahun(), floatValue));
            } catch (NumberFormatException e) {
                // Abaikan data yang tidak valid
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, data.getMaster().getNamaIndikator());
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setExtraOffsets(5, 5, 5, 10);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    private void setupButtonListeners() {
        binding.btnUnduhExcel.setOnClickListener(v -> {
            downloadExcelFile();
        });

        binding.btnUnduhGrafik.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveChartToGallery();
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveChartToGallery();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
    }

    private void downloadExcelFile() {
        if (indikatorId <= 0) {
            Toast.makeText(getContext(), "ID Indikator tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = RetrofitClient.BASE_URL + "api/indikator-strategis/" + indikatorId + "/export-excel";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String title = "Data_" + binding.chipIndicatorTitle.getText().toString().replaceAll("[^a-zA-Z0-9.-]", "_") + ".xlsx";

        request.setTitle(binding.chipIndicatorTitle.getText().toString());
        request.setDescription("Mengunduh file Excel...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
        request.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        DownloadManager downloadManager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(getContext(), "Mulai mengunduh Excel...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fungsi untuk menyimpan grafik dengan cara modern menggunakan MediaStore
     */
    private void saveChartToGallery() {
        if (binding.lineChart.getChartBitmap() == null) {
            Toast.makeText(getContext(), "Grafik belum siap.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap chartBitmap = binding.lineChart.getChartBitmap();
        String fileName = "Grafik_" + binding.chipIndicatorTitle.getText().toString().replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + System.currentTimeMillis() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SIMODIS");
        }

        Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (java.io.OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri)) {
                chartBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                Toast.makeText(getContext(), "Grafik disimpan ke Galeri.", Toast.LENGTH_SHORT).show();
            } catch (java.io.IOException e) {
                Toast.makeText(getContext(), "Gagal menyimpan grafik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Gagal membuat file di galeri.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}