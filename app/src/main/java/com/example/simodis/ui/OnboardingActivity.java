package com.example.simodis.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.simodis.R;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

public class OnboardingActivity extends AppIntro {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(ImageSlideFragment.newInstance(R.drawable.img_appintroslide1));
        addSlide(ImageSlideFragment.newInstance(R.drawable.img_appintroslide2));
        addSlide(ImageSlideFragment.newInstance(R.drawable.img_appintroslide3));
        addSlide(ImageSlideFragment.newInstance(R.drawable.img_appintroslide4));
        addSlide(ImageSlideFragment.newInstance(R.drawable.img_appintroslide5));

        // Customization
        setSkipText("Lewati");
        setDoneText("Mulai");
        setNextArrowColor(ContextCompat.getColor(this, android.R.color.white));
        setColorSkipButton(ContextCompat.getColor(this, android.R.color.white));
        setColorDoneText(ContextCompat.getColor(this, android.R.color.white));
        setIndicatorColor(
                ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, R.color.grey_light)
        );
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finishOnboarding();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finishOnboarding();
    }

    private void finishOnboarding() {
        // Tandai bahwa onboarding sudah selesai
        SharedPreferences prefs = getSharedPreferences("simodis_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_completed", true).apply();

        // Pindah ke MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}