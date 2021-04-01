package com.demo.maniva.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.maniva.BuildConfig;
import com.demo.maniva.R;
import com.demo.maniva.listener.MapboxListener;
import com.demo.maniva.manager.MapboxManager;
import com.demo.maniva.utils.IntentUtil;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxListener {

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    private MapView mMapView;
    private Button mButtonStartNavigation;
    private Point mDestinationPoint;
    private MapboxManager mMapboxManager;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, BuildConfig.PUBLIC_KEY);
        setContentView(R.layout.activity_home);
        initMapView(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        checkGpsDeviceSettingEnabled();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (mDestinationPoint != null) {

            mDestinationPoint = null;
            mMapboxManager.resetMap();
            resetButton();
        } else if (!doubleBackToExitPressedOnce) {

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.msg_back_again_to_exit, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mMapboxManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mMapboxManager = new MapboxManager(mMapView, mapboxMap, this, this);
        mMapboxManager.initMapbox();
        mapboxMap.addOnMapClickListener(HomeActivity.this);
        mButtonStartNavigation = findViewById(R.id.startButton);
        mButtonStartNavigation.setOnClickListener(v -> mMapboxManager.startNavigation());

        initSearchFab();
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        mDestinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        mMapboxManager.drawMarkerForDestination(mDestinationPoint);
        mMapboxManager.getRoute(mDestinationPoint);
        mButtonStartNavigation.setEnabled(true);
        mButtonStartNavigation.setBackgroundResource(R.color.mapboxBlue);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon
            mDestinationPoint = Point.fromLngLat(((Point) selectedCarmenFeature.geometry()).longitude(), ((Point) selectedCarmenFeature.geometry()).latitude());
            mMapboxManager.drawMarkerFromSelectedAddress(selectedCarmenFeature, mDestinationPoint);
            mMapboxManager.getRoute(mDestinationPoint);
            mButtonStartNavigation.setEnabled(true);
            mButtonStartNavigation.setBackgroundResource(R.color.mapboxBlue);

        }
    }

    @Override
    public void onPermissionDenied() {
        Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void locationEngineError() {
        Toast.makeText(this, R.string.error_location_not_found, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRouteError() {
        Toast.makeText(this, R.string.err_route_not_found, Toast.LENGTH_LONG).show();
    }

    private void initMapView(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
    }

    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(view -> startActivityForResult(IntentUtil.getMapboxAutoCompleteSearchIntent(this), REQUEST_CODE_AUTOCOMPLETE));
    }

    private void resetButton() {
        mButtonStartNavigation.setEnabled(false);
        mButtonStartNavigation.setBackgroundResource(R.color.mapboxGrayLight);
    }

    private void showDialogGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.title_enable_location);
        builder.setMessage(R.string.msg_enable_location);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(R.string.label_Enable, (dialog, which) -> IntentUtil.launchActivityForAction(HomeActivity.this, android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void checkGpsDeviceSettingEnabled() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            showDialogGPS();
        } else {
            if (mMapboxManager != null) {
                mMapboxManager.enableLocationComponent();
            }

        }
    }


}