package com.demo.maniva.presentation;


import android.app.PictureInPictureParams;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.Display;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.maniva.R;
import com.demo.maniva.utils.PreferenceUtil;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;

import org.jetbrains.annotations.NotNull;


public class NavigationActivity extends AppCompatActivity implements OnNavigationReadyCallback,
        NavigationListener {

    private NavigationView mNavigationView;
    private DirectionsRoute mRoute;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mNavigationView = findViewById(R.id.navigationView);
        mNavigationView.onCreate(savedInstanceState);
        initialize();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initialize();
    }

    @Override
    public void onStart() {
        super.onStart();
        mNavigationView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mNavigationView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mNavigationView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        if (!mNavigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        mNavigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mNavigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mNavigationView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mNavigationView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNavigationView.onDestroy();
    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        NavigationViewOptions.Builder options = NavigationViewOptions.builder();
        options.navigationListener(this);
        options.directionsRoute(mRoute);
        options.shouldSimulateRoute(true);
        options.navigationOptions(MapboxNavigationOptions.builder().build());
        mNavigationView.startNavigation(options.build());
    }

    @Override
    public void onCancelNavigation() {
        finishNavigation();
    }

    @Override
    public void onNavigationFinished() {
        finishNavigation();
    }

    @Override
    public void onNavigationRunning() {
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);

        if (isInPictureInPictureMode && getSupportActionBar() != null) {
            getSupportActionBar().hide();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
        }
    }

    @Override
    protected void onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            Rational aspectRatio = new Rational(width, height);
            PictureInPictureParams params;
            params = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio).build();
            enterPictureInPictureMode(params);
        }
    }


    private void initialize() {
        this.mRoute = PreferenceUtil.getInstance(this).getDirectionRoute();
        PreferenceUtil.getInstance(this).setDirectionRoute(null);
        mNavigationView.initialize(this);
    }

    private void finishNavigation() {
        finish();
    }

}
