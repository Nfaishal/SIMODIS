package com.example.simodis.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.simodis.R;
import com.example.simodis.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        handleOnBackPressed();
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