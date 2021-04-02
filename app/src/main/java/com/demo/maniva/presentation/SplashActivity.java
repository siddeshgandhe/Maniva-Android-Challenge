package com.demo.maniva.presentation;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.maniva.R;
import com.demo.maniva.utils.IntentUtil;

public class SplashActivity extends AppCompatActivity {

    public static final int SPLASH_TIME_OUT = 2000;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1002;
    private final Runnable runnable = this::checkPermissionForNetworkState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSplashScreenAppearance();
        setContentView(R.layout.activity_splash);
        createSplashTimer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.err_permission_denied, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    launchHomeActivity();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void createSplashScreenAppearance() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        overridePendingTransition(0, 0);
    }

    private void createSplashTimer() {
        Handler handler = new Handler();
        handler.postDelayed(runnable, SPLASH_TIME_OUT);
    }

    private void checkPermissionForNetworkState() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int res = checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);
            if (res != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                launchHomeActivity();
            }
        } else {
            launchHomeActivity();
        }
    }

    private void launchHomeActivity() {
        IntentUtil.launchActivityIntentForClass(this, HomeActivity.class);
        finish();
    }
}