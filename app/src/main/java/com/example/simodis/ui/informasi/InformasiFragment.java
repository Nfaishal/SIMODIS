package com.example.simodis.ui.informasi; // Pastikan package ini sesuai dengan proyek Anda

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.simodis.databinding.FragmentInformasiBinding; // Import class Binding yang dibuat otomatis

public class InformasiFragment extends Fragment {

    // Deklarasikan variabel untuk view binding.
    // Ini akan menampung semua referensi ID dari layout XML Anda.
    private FragmentInformasiBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate (mengubah) layout XML menjadi objek View menggunakan ViewBinding
        binding = FragmentInformasiBinding.inflate(inflater, container, false);
        // Mengembalikan root view dari layout yang sudah di-inflate
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setelah view dibuat, kita bisa menambahkan logika untuk setiap tombol/kartu.
        // Cukup panggil ID dari layout melalui objek 'binding'.
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Menambahkan listener untuk setiap kartu layanan
        binding.cardAdisti.setOnClickListener(v -> openUrl("https://wa.me/6282279797495?text=Halo%20Adisti!"));
        binding.cardRomantik.setOnClickListener(v -> openUrl("https://romantik.web.bps.go.id"));
        binding.cardPst.setOnClickListener(v -> openUrl("https://perpustakaan.bps.go.id/opac/"));
        binding.cardSilastik.setOnClickListener(v -> openUrl("https://silastik.bps.go.id/v3/index.php/site/login/"));
        binding.cardPpid.setOnClickListener(v -> openUrl("https://ppid.bps.go.id/?mfd=1805"));
    }

    /**
     * Fungsi pembantu (helper method) untuk membuka URL di browser.
     * @param url Alamat website yang akan dibuka.
     */
    private void openUrl(String url) {
        // Membuat Intent dengan aksi untuk MELIHAT (VIEW) sebuah data
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        // Memulai activity (browser) untuk menangani Intent ini
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ini adalah langkah PENTING untuk menghindari memory leak di Fragment.
        // Set variabel binding menjadi null saat view dihancurkan.
        binding = null;
    }
}