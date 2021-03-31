package com.demo.maniva.presentation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.maniva.BuildConfig;
import com.demo.maniva.Manager.MapboxManager;
import com.demo.maniva.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    private MapView mMapView;
    private MapboxMap mMapboxMap;
    private PermissionsManager mPermissionsManager;
    private Button mButtonStartNavigation;
    private Point mDestinationPoint;
    private MapboxManager mMapboxManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, BuildConfig.PUBLIC_KEY);
        setContentView(R.layout.activity_home);
        initMapView(savedInstanceState);
        mPermissionsManager = new PermissionsManager(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
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
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mMapboxManager.enableLocationComponent(mMapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mMapboxMap = mapboxMap;
        mMapboxManager = new MapboxManager(mMapView, mapboxMap, mPermissionsManager, this);
        mMapboxManager.initMapbox();
        mapboxMap.addOnMapClickListener(HomeActivity.this);
        mButtonStartNavigation = findViewById(R.id.startButton);
        mButtonStartNavigation.setOnClickListener(v -> {
            mMapboxManager.startNavigation();
        });

        initSearchFab();
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        mDestinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());

        GeoJsonSource source = mMapboxMap.getStyle().getSourceAs(MapboxManager.DESTINATION_SOURCE_ID);
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(mDestinationPoint));
        }

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
            if (mMapboxMap != null) {
                mDestinationPoint = Point.fromLngLat(((Point) selectedCarmenFeature.geometry()).longitude(), ((Point) selectedCarmenFeature.geometry()).latitude());
                mMapboxManager.drawMarkerFromSelectedAddress(selectedCarmenFeature, mDestinationPoint);
                mMapboxManager.getRoute(mDestinationPoint);
                mButtonStartNavigation.setEnabled(true);
                mButtonStartNavigation.setBackgroundResource(R.color.mapboxBlue);

            }
        }
    }

    private void initMapView(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
    }

    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(view -> {
            startActivityForResult(mMapboxManager.getSearchIntent(), REQUEST_CODE_AUTOCOMPLETE);
        });
    }

    void resetButton() {
        mButtonStartNavigation.setEnabled(false);
        mButtonStartNavigation.setBackgroundResource(R.color.mapboxGrayLight);
    }
}