package com.demo.maniva.presentation;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.maniva.R;
import com.demo.maniva.listener.PermissionListener;
import com.demo.maniva.manager.PermissionManager;
import com.demo.maniva.utils.IntentUtil;
import com.demo.maniva.utils.UiUtility;

public class SplashActivity extends AppCompatActivity implements PermissionListener {

    public static final int SPLASH_TIME_OUT = 2000;
    private PermissionManager mPermissionManager;
    private final Runnable mRunnable = this::run;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSplashScreenAppearance();
        setContentView(R.layout.activity_splash);
        createPermissionManager();
        createSplashTimer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted() {
        launchHomeActivity();
    }

    @Override
    public void onPermissionDenied() {
        UiUtility.showToast(this, getString(R.string.err_permission_denied));
        finish();
    }

    private void createSplashScreenAppearance() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        overridePendingTransition(0, 0);
    }

    private void createPermissionManager() {
        mPermissionManager = new PermissionManager(this);
    }

    private void createSplashTimer() {
        Handler handler = new Handler();
        handler.postDelayed(mRunnable, SPLASH_TIME_OUT);
    }

    private void run() {
        mPermissionManager.checkPermissionForNetworkState(SplashActivity.this);
    }

    private void launchHomeActivity() {
        IntentUtil.launchActivityIntentForClass(this, HomeActivity.class);
        finish();
    }


}