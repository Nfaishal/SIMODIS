package com.example.simodis.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.TransitionManager;

import com.example.simodis.R;
import com.example.simodis.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivityDebug";
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("simodis_prefs", MODE_PRIVATE);
        boolean isFirstLaunch = !prefs.getBoolean("onboarding_completed", false);

        if (isFirstLaunch) {
            // Tampilkan onboarding jika ini pertama kali
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            finish(); // Tutup MainActivity agar tidak bisa kembali ke sini
            return;   // Hentikan eksekusi kode selanjutnya
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        binding.fabWhatsapp.setOnClickListener(v -> {
            String url = "https://wa.me/6282279797495?text=Halo%20Adisti!";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Panggil listener navigasi yang baru
        setupNavigationListener();

        handleOnBackPressed();
    }

    private void setupNavigationListener() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                adjustFabPositionForFragment((String) destination.getLabel());
            });
        }
    }

    private void adjustFabPositionForFragment(String fragmentLabel) {
        ConstraintLayout mainLayout = binding.mainConstraintLayout;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mainLayout);

        TransitionManager.beginDelayedTransition(mainLayout);


        if (Objects.equals(fragmentLabel, "fragment_informasi")) {
            // Sembunyikan FAB untuk fragment ID ini
            constraintSet.setVisibility(R.id.fab_whatsapp, ConstraintSet.GONE);

        } else if (Objects.equals(fragmentLabel, "fragment_publikasi") ||
                Objects.equals(fragmentLabel, "fragment_data_hub")) {
            // Tampilkan FAB di posisi lebih tinggi (untuk fragment dengan pagination)
            constraintSet.setVisibility(R.id.fab_whatsapp, ConstraintSet.VISIBLE);
            constraintSet.setMargin(R.id.fab_whatsapp, ConstraintSet.BOTTOM,
                    getResources().getDimensionPixelSize(R.dimen.fab_margin_with_pagination));

        } else {
            // Tampilkan FAB di posisi normal untuk semua fragment lainnya
            constraintSet.setVisibility(R.id.fab_whatsapp, ConstraintSet.VISIBLE);
            constraintSet.setMargin(R.id.fab_whatsapp, ConstraintSet.BOTTOM,
                    getResources().getDimensionPixelSize(R.dimen.fab_margin_default));
        }

        constraintSet.applyTo(mainLayout);
    }

    private void handleOnBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Cek apakah kita sedang berada di halaman utama (start destination)
                if (navController.getCurrentDestination().getId() == navController.getGraph().getStartDestinationId()) {
                    // Jika di halaman utama, tampilkan dialog konfirmasi keluar
                    showExitConfirmationDialog();
                } else {
                    // Jika tidak, lakukan aksi kembali seperti biasa (kembali ke fragment sebelumnya)
                    navController.navigateUp();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Yakin mau keluar?")
                .setMessage("Jangan lupa untuk mengisi kuesioner SKD kalau kamu sudah selesai mencari data ya😇🙏")
                .setPositiveButton("Keluar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton("Isi Kuesioner", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = "https://skd.bps.go.id/SKD2025/web/entri/responden/blok1?token=sIICxSCYO30fIekrDQuoPwsuVWKtdX3ZqqAzf9LfGvcRvBzOkH4huqA853W--5P8ZT0gVCP13DjwkzYLjN9XwGuBcr69-XOC4uXp";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Kembali", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}