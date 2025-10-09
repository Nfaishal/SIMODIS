package com.example.simodis.ui.home;

import android.app.DownloadManager;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
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
import com.example.simodis.data.model.IndikatorPembanding;
import com.example.simodis.data.network.RetrofitClient;
import com.example.simodis.databinding.FragmentIndikatorDetailBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndikatorDetailFragment extends Fragment {

    private FragmentIndikatorDetailBinding binding;
    private int indikatorId;
    private IndikatorDetailResponse detailData;
    private boolean showProvinsi = false;
    private boolean showNasional = false;

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
                    detailData = response.body();
                    populateUI(detailData);
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
            if (data.getMaster().getSatuan() != null){
                binding.chipIndicatorTitle.setText(String.format("%s (%s)", data.getMaster().getNamaIndikator(), data.getMaster().getSatuan()));
            }else {
                binding.chipIndicatorTitle.setText(data.getMaster().getNamaIndikator());
            }

            binding.tvDescription.setText(data.getMaster().getDeskripsi());

            // Cek apakah ada perbandingan
            if (data.getMaster().hasPerbandingan()) {
                binding.layoutToggleButtons.setVisibility(View.VISIBLE);
                android.util.Log.d("DetailFragment", "Perbandingan tersedia");
            } else {
                binding.layoutToggleButtons.setVisibility(View.GONE);
                binding.rowProvinsiData.setVisibility(View.GONE);
                binding.rowNasionalData.setVisibility(View.GONE);
                android.util.Log.d("DetailFragment", "Perbandingan tidak tersedia");
            }
        }

        if (data.getHistory() != null && !data.getHistory().isEmpty()) {
            populateTable(data.getHistory());

            // Populate pembanding table jika ada
            if (data.getMaster().hasPerbandingan() && data.getPembanding() != null) {
                populatePembandingTable(data.getPembanding());
            }

            setupChart(data);
        }

        // Debug setelah data dimuat
        debugPembandingData();
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

    private void populatePembandingTable(List<IndikatorPembanding> pembanding) {
        // Reset visibility
        binding.rowProvinsiData.setVisibility(View.GONE);
        binding.rowNasionalData.setVisibility(View.GONE);

        // Populate provinsi data
        TextView[] provinsiValues = {
                binding.tvProvinsiYear3Value,
                binding.tvProvinsiYear2Value,
                binding.tvProvinsiYear1Value
        };

        // Populate nasional data
        TextView[] nasionalValues = {
                binding.tvNasionalYear3Value,
                binding.tvNasionalYear2Value,
                binding.tvNasionalYear1Value
        };

        // Get years from main data for reference
        List<IndikatorNilai> history = detailData.getHistory();
        int historySize = history.size();

        for (int i = 0; i < 3; i++) {
            int dataIndex = historySize - 1 - i;
            if (dataIndex >= 0) {
                int tahun = history.get(dataIndex).getTahun();

                // Find provinsi data for this year
                String provinsiNilai = findPembandingValue(pembanding, tahun, "provinsi");
                if (provinsiNilai != null) {
                    provinsiValues[i].setText(provinsiNilai.replace(",", "."));
                } else {
                    provinsiValues[i].setText("-");
                }

                // Find nasional data for this year
                String nasionalNilai = findPembandingValue(pembanding, tahun, "nasional");
                if (nasionalNilai != null) {
                    nasionalValues[i].setText(nasionalNilai.replace(",", "."));
                } else {
                    nasionalValues[i].setText("-");
                }
            }
        }
    }

    private String findPembandingValue(List<IndikatorPembanding> pembanding, int tahun, String level) {
        for (IndikatorPembanding item : pembanding) {
            if (item.getTahun() == tahun && level.equals(item.getLevel())) {
                return item.getNilaiPembanding();
            }
        }
        return null;
    }

    private void setupChart(IndikatorDetailResponse data) {
        List<IndikatorNilai> history = data.getHistory();
        if (history == null || history.isEmpty()) return;

        LineChart chart = binding.lineChart;
        chart.clear();
        chart.getData();

        ArrayList<LineDataSet> dataSets = new ArrayList<>();

        // Dataset untuk data utama (Kab/Kota)
        ArrayList<Entry> entries = new ArrayList<>();
        for (IndikatorNilai nilai : history) {
            try {
                float floatValue = Float.parseFloat(nilai.getNilai().replace(",", "."));
                entries.add(new Entry(nilai.getTahun(), floatValue));
            } catch (NumberFormatException e) {
                // Abaikan data yang tidak valid
            }
        }

        LineDataSet mainDataSet = new LineDataSet(entries, "Data Kabupaten/Kota");
        mainDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        mainDataSet.setLineWidth(2.5f);
        mainDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        mainDataSet.setCircleRadius(5f);
        mainDataSet.setDrawCircleHole(false);
        mainDataSet.setValueTextSize(10f);
        mainDataSet.setValueTextColor(Color.BLACK);
        dataSets.add(mainDataSet);

        // Tambahkan data pembanding jika diperlukan
        if (data.getPembanding() != null && !data.getPembanding().isEmpty()) {
            if (showProvinsi) {
                LineDataSet provinsiDataSet = createPembandingDataSet(data.getPembanding(), "provinsi", "Data Provinsi", Color.BLUE);
                if (provinsiDataSet != null) {
                    dataSets.add(provinsiDataSet);
                }
            }
            if (showNasional) {
                LineDataSet nasionalDataSet = createPembandingDataSet(data.getPembanding(), "nasional", "Data Nasional", Color.RED);
                if (nasionalDataSet != null) {
                    dataSets.add(nasionalDataSet);
                }
            }
        }

        LineData lineData = new LineData();
        for (LineDataSet dataSet : dataSets) {
            lineData.addDataSet(dataSet);
        }

        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setWordWrapEnabled(true);
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
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private LineDataSet createPembandingDataSet(List<IndikatorPembanding> pembanding, String level, String label, int color) {
        ArrayList<Entry> entries = new ArrayList<>();

        for (IndikatorPembanding item : pembanding) {
            if (level.equals(item.getLevel())) {
                try {
                    float floatValue = Float.parseFloat(item.getNilaiPembanding().replace(",", "."));
                    entries.add(new Entry(item.getTahun(), floatValue));
                } catch (NumberFormatException e) {
                    // Abaikan data yang tidak valid
                }
            }
        }

        if (!entries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(entries, label);
            dataSet.setColor(color);
            dataSet.setLineWidth(2f);
            dataSet.setCircleColor(color);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(9f);
            dataSet.setValueTextColor(color);
            return dataSet;
        }
        return null;
    }

    private void setupButtonListeners() {
        binding.btnUnduhExcel.setOnClickListener(v -> downloadExcelFile());

        binding.btnUnduhPdf.setOnClickListener(v -> generateAndSavePDF());

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

        // Toggle buttons - hanya setup listener jika ada perbandingan
        binding.btnToggleProvinsi.setOnClickListener(v -> {
            showProvinsi = !showProvinsi;
            updateToggleButton(binding.btnToggleProvinsi, showProvinsi);
            updateTableVisibility();

            // Debug
            debugPembandingData();

            if (detailData != null) {
                setupChart(detailData);
            }
        });

        binding.btnToggleNasional.setOnClickListener(v -> {
            showNasional = !showNasional;
            updateToggleButton(binding.btnToggleNasional, showNasional);
            updateTableVisibility();

            // Debug
            debugPembandingData();

            if (detailData != null) {
                setupChart(detailData);
            }
        });

        // Tombol sumber
        binding.btnSumber.setOnClickListener(v -> {
            if (detailData != null && detailData.getMaster() != null &&
                    detailData.getMaster().getSumberUrl() != null &&
                    !detailData.getMaster().getSumberUrl().isEmpty()) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(detailData.getMaster().getSumberUrl()));
                startActivity(browserIntent);
            } else {
                Toast.makeText(getContext(), "URL sumber tidak tersedia.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTableVisibility() {
        // Show/hide provinsi row
        if (showProvinsi) {
            binding.rowProvinsiData.setVisibility(View.VISIBLE);
        } else {
            binding.rowProvinsiData.setVisibility(View.GONE);
        }

        // Show/hide nasional row
        if (showNasional) {
            binding.rowNasionalData.setVisibility(View.VISIBLE);
        } else {
            binding.rowNasionalData.setVisibility(View.GONE);
        }

        android.util.Log.d("TABLE", "Table visibility updated - Provinsi: " + showProvinsi + ", Nasional: " + showNasional);
    }

    private void updateToggleButton(MaterialButton button, boolean isActive) {
        if (isActive) {
            button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary));
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        }
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

    private void generateAndSavePDF() {
        if (detailData == null || detailData.getMaster() == null) {
            Toast.makeText(getContext(), "Data tidak tersedia untuk membuat PDF.", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Title
        paint.setTextSize(18f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.BLACK);
        canvas.drawText("DETAIL INDIKATOR", 50, 50, paint);

        // Indicator Name
        paint.setTextSize(16f);
        canvas.drawText(detailData.getMaster().getNamaIndikator(), 50, 80, paint);

        // Description
        paint.setTextSize(12f);
        paint.setTypeface(Typeface.DEFAULT);
        String description = detailData.getMaster().getDeskripsi();
        if (description != null && description.length() > 0) {
            // Wrap text for description
            String[] words = description.split(" ");
            StringBuilder line = new StringBuilder();
            float yPosition = 110;

            for (String word : words) {
                if (paint.measureText(line + word + " ") < 500) {
                    line.append(word).append(" ");
                } else {
                    canvas.drawText(line.toString(), 50, yPosition, paint);
                    line = new StringBuilder(word + " ");
                    yPosition += 20;
                }
            }
            if (line.length() > 0) {
                canvas.drawText(line.toString(), 50, yPosition, paint);
                yPosition += 20;
            }
        }

        // Table Header
        paint.setTextSize(14f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("DATA HISTORIS", 50, 200, paint);

        // Table
        if (detailData.getHistory() != null && !detailData.getHistory().isEmpty()) {
            List<IndikatorNilai> history = detailData.getHistory();
            int historySize = history.size();

            // Table headers
            paint.setTextSize(12f);
            float tableY = 230;
            canvas.drawText("Tahun", 50, tableY, paint);
            canvas.drawText("Kab/Kota", 150, tableY, paint);

            // Add headers for pembanding if available
            boolean hasProvinsi = detailData.getMaster().hasPerbandingan() &&
                    detailData.getPembanding() != null &&
                    hasPembandingData("provinsi");
            boolean hasNasional = detailData.getMaster().hasPerbandingan() &&
                    detailData.getPembanding() != null &&
                    hasPembandingData("nasional");

            if (hasProvinsi) {
                canvas.drawText("Provinsi", 250, tableY, paint);
            }
            if (hasNasional) {
                canvas.drawText("Nasional", hasProvinsi ? 350 : 250, tableY, paint);
            }

            canvas.drawText("Satuan", hasProvinsi && hasNasional ? 450 : (hasProvinsi || hasNasional ? 350 : 250), tableY, paint);

            // Draw line under header
            canvas.drawLine(50, tableY + 5, 500, tableY + 5, paint);

            // Table data
            paint.setTypeface(Typeface.DEFAULT);
            tableY += 25;

            for (int i = Math.max(0, historySize - 3); i < historySize; i++) {
                IndikatorNilai item = history.get(i);
                canvas.drawText(String.valueOf(item.getTahun()), 50, tableY, paint);
                canvas.drawText(item.getNilai(), 150, tableY, paint);

                // Add pembanding data if available
                if (hasProvinsi) {
                    String provinsiValue = findPembandingValue(detailData.getPembanding(), item.getTahun(), "provinsi");
                    canvas.drawText(provinsiValue != null ? provinsiValue : "-", 250, tableY, paint);
                }

                if (hasNasional) {
                    String nasionalValue = findPembandingValue(detailData.getPembanding(), item.getTahun(), "nasional");
                    canvas.drawText(nasionalValue != null ? nasionalValue : "-", hasProvinsi ? 350 : 250, tableY, paint);
                }

                canvas.drawText(detailData.getMaster().getSatuan() != null ? detailData.getMaster().getSatuan() : "-",
                        hasProvinsi && hasNasional ? 450 : (hasProvinsi || hasNasional ? 350 : 250), tableY, paint);
                tableY += 20;
            }
        }

        pdfDocument.finishPage(page);

        // Save PDF
        String fileName = "Detail_" + detailData.getMaster().getNamaIndikator().replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + System.currentTimeMillis() + ".pdf";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SIMODIS");

                Uri uri = requireContext().getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    try (FileOutputStream fos = (FileOutputStream) requireContext().getContentResolver().openOutputStream(uri)) {
                        pdfDocument.writeTo(fos);
                        Toast.makeText(getContext(), "PDF berhasil disimpan ke Downloads.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // For older Android versions
                java.io.File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                java.io.File pdfFile = new java.io.File(downloadsDir, fileName);
                try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                    pdfDocument.writeTo(fos);
                    Toast.makeText(getContext(), "PDF berhasil disimpan ke Downloads.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Gagal menyimpan PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    private boolean hasPembandingData(String level) {
        if (detailData.getPembanding() == null) return false;

        for (IndikatorPembanding item : detailData.getPembanding()) {
            if (level.equals(item.getLevel())) {
                return true;
            }
        }
        return false;
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

    private void debugPembandingData() {
        if (detailData == null) {
            android.util.Log.e("DEBUG", "detailData is null");
            return;
        }

        if (detailData.getMaster() != null) {
            android.util.Log.d("DEBUG", "Perbandingan field: " + detailData.getMaster().getPerbandingan());
            android.util.Log.d("DEBUG", "Has perbandingan: " + detailData.getMaster().hasPerbandingan());
        }

        if (detailData.getPembanding() == null) {
            android.util.Log.e("DEBUG", "pembanding data is null");
            return;
        }

        android.util.Log.d("DEBUG", "Total pembanding data: " + detailData.getPembanding().size());

        int provinsiCount = 0;
        int nasionalCount = 0;

        for (IndikatorPembanding item : detailData.getPembanding()) {
            android.util.Log.d("DEBUG", String.format("Data: ID=%d, Level=%s, Tahun=%d, Nilai=%s",
                    item.getIdPembanding(), item.getLevel(), item.getTahun(), item.getNilaiPembanding()));

            if ("provinsi".equals(item.getLevel())) {
                provinsiCount++;
            } else if ("nasional".equals(item.getLevel())) {
                nasionalCount++;
            }
        }

        android.util.Log.d("DEBUG", "Provinsi data count: " + provinsiCount);
        android.util.Log.d("DEBUG", "Nasional data count: " + nasionalCount);
        android.util.Log.d("DEBUG", "showProvinsi: " + showProvinsi + ", showNasional: " + showNasional);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}