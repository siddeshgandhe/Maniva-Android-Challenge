package com.demo.maniva.presentation;


import android.app.PictureInPictureParams;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Rational;
import android.view.Display;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.maniva.R;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;


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
        // If the navigation view didn't need to do anything, call super
        if (!mNavigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
        // Intentionally empty
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
            PictureInPictureParams params = null;
            params = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio).build();
            enterPictureInPictureMode(params);
        }
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

    private void initialize() {
        this.mRoute = extractRoute();
        mNavigationView.initialize(this);
    }


    private void finishNavigation() {
        finish();
    }

    private DirectionsRoute extractRoute() {
        //TODO move to pref util
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String directionsRouteJson = preferences.getString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, "");
        return DirectionsRoute.fromJson(directionsRouteJson);
    }
}
