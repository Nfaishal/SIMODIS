package com.example.simodis.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.simodis.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    // Durasi splash screen dalam milidetik (misal: 2.5 detik)
    private static final int SPLASH_DELAY = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menggunakan ViewBinding untuk mengakses layout
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handler untuk menunda perpindahan ke MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Buat Intent untuk memulai MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);

                // Tutup SplashActivity agar tidak bisa kembali dengan tombol back
                finish();
            }
        }, SPLASH_DELAY);
    }
}