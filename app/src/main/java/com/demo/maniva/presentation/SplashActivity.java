package com.demo.maniva.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.maniva.R;

public class SplashActivity extends AppCompatActivity {

    public static int SPLASH_TIME_OUT = 1000;
    private final Runnable runnable = this::launchHomeScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSplashScreenAppearance();
        setContentView(R.layout.activity_splash);
        createSplashTimer();
    }

    private void createSplashScreenAppearance() {
        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Override animation
        overridePendingTransition(0, 0);
    }

    private void createSplashTimer() {
        Handler handler = new Handler();
        handler.postDelayed(runnable, SPLASH_TIME_OUT);
    }

    /*launch Home screen*/
    private void launchHomeScreen() {
       /* startActivity(new Intent(this, HomeActivity.class));
        finish();*/
    }

}