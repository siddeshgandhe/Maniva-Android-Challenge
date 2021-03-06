package com.demo.maniva.manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.demo.maniva.R;
import com.demo.maniva.listener.MapboxListener;
import com.demo.maniva.presentation.NavigationActivity;
import com.demo.maniva.utils.IntentUtil;
import com.demo.maniva.utils.PreferenceUtil;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Looper.getMainLooper;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class MapboxManager implements PermissionsListener {

    public static final String DESTINATION_SYMBOL_LAYER_ID = "destination-symbol-layer-id";
    public static final String DESTINATION_ICON_ID = "destination-icon-id";
    public static final String DESTINATION_SOURCE_ID = "destination-source-id";

    private final MapboxMap mMapboxMap;
    private final MapView mMapView;
    private final Context mContext;
    private final PermissionsManager mPermissionsManager;

    private NavigationMapRoute mNavigationMapRoute;
    private DirectionsRoute mCurrentRoute;
    private Point mOriginPoint;

    private final MapboxListener mMapboxListener;


    public MapboxManager(MapView mapView, MapboxMap mapboxMap, Context context, MapboxListener mapboxListener) {
        this.mMapboxMap = mapboxMap;
        this.mContext = context;
        this.mMapView = mapView;
        this.mMapboxListener = mapboxListener;
        mPermissionsManager = new PermissionsManager(this);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        mMapboxListener.onExplanationNeeded(permissionsToExplain);
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent();
        } else {
            mMapboxListener.onPermissionDenied();
        }
    }


    public void initMapbox() {
        mMapboxMap.getUiSettings().setZoomGesturesEnabled(true);
        mMapboxMap.getUiSettings().setQuickZoomGesturesEnabled(true);
        mMapboxMap.setStyle(mContext.getString(R.string.navigation_guidance_day), style -> {
            enableLocationComponent();
            addDestinationIconSymbolLayer(style);
        });
    }

    @SuppressWarnings({"MissingPermission"})
    public void enableLocationComponent() {
        if (mMapboxMap != null) {
            if (PermissionsManager.areLocationPermissionsGranted(mContext)) {
                LocationComponent locationComponent = mMapboxMap.getLocationComponent();

                LocationComponentActivationOptions locationComponentActivationOptions =
                        LocationComponentActivationOptions.builder(mContext, mMapboxMap.getStyle())
                                .useDefaultLocationEngine(false)
                                .build();

                locationComponent.activateLocationComponent(locationComponentActivationOptions);
                locationComponent.setLocationComponentEnabled(true);
                locationComponent.setCameraMode(CameraMode.TRACKING);
                locationComponent.setRenderMode(RenderMode.COMPASS);
                initLocationEngine();

            } else {
                mPermissionsManager.requestLocationPermissions((Activity) mContext);
            }
        }
    }

    public void getRoute(Point destination) {
        if (mOriginPoint != null) {
            NavigationRoute.builder(mContext)
                    .accessToken(Mapbox.getAccessToken())
                    .origin(mOriginPoint)
                    .destination(destination)
                    .build()
                    .getRoute(new Callback<DirectionsResponse>() {
                        @Override
                        public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                            if (response.body() == null) {
                                return;
                            } else if (response.body().routes().size() < 1) {
                                return;
                            }
                            mCurrentRoute = response.body().routes().get(0);
                            if (mNavigationMapRoute != null) {
                                mNavigationMapRoute.updateRouteVisibilityTo(false);
                                mNavigationMapRoute.updateRouteArrowVisibilityTo(false);
                                mNavigationMapRoute.addRoute(mCurrentRoute);
                            } else {
                                try {
                                    mNavigationMapRoute = new NavigationMapRoute(null, mMapView, mMapboxMap, R.style.NavigationMapRoute);
                                    mNavigationMapRoute.addRoute(mCurrentRoute);
                                } catch (Exception e) {
                                    mMapboxListener.onRouteError();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                            mMapboxListener.onRouteError();
                        }
                    });
        }
    }

    public void drawMarkerFromSelectedAddress(CarmenFeature selectedCarmenFeature, Point destinationPoint) {
        Style style = mMapboxMap.getStyle();
        if (style != null) {
            mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                    ((Point) selectedCarmenFeature.geometry()).longitude()))
                            .zoom(14)
                            .build()), 4000);

            GeoJsonSource source = mMapboxMap.getStyle().getSourceAs(DESTINATION_SOURCE_ID);
            if (source != null) {
                source.setGeoJson(Feature.fromGeometry(destinationPoint));
            }
        }
    }

    public void resetMap() {
        mNavigationMapRoute.updateRouteVisibilityTo(false);
        mNavigationMapRoute.updateRouteArrowVisibilityTo(false);
        mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(mOriginPoint.latitude(),
                                mOriginPoint.longitude()))
                        .zoom(14)
                        .build()), 4000);
    }

    public void startNavigation() {
        PreferenceUtil.getInstance(mContext).setDirectionRoute(mCurrentRoute);
        IntentUtil.launchActivityIntentForClass(mContext, NavigationActivity.class);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPermissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void drawMarkerForDestination(Point mDestinationPoint) {
        GeoJsonSource source = mMapboxMap.getStyle().getSourceAs(MapboxManager.DESTINATION_SOURCE_ID);
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(mDestinationPoint));
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(mContext);

        LocationEngineRequest request = new LocationEngineRequest.Builder(1000)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(5000).build();

        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
        locationEngine.getLastLocation(locationEngineCallback);
    }

    final LocationEngineCallback<LocationEngineResult> locationEngineCallback = new LocationEngineCallback<LocationEngineResult>() {
        @Override
        public void onSuccess(LocationEngineResult result) {
            if (mMapboxMap != null && result.getLastLocation() != null) {
                mMapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                mOriginPoint = Point.fromLngLat(result.getLastLocation().getLongitude(),
                        result.getLastLocation().getLatitude());
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            mMapboxListener.onLocationEngineError();
        }
    };


    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage(DESTINATION_ICON_ID,
                BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource(DESTINATION_SOURCE_ID);
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer(DESTINATION_SYMBOL_LAYER_ID, DESTINATION_SOURCE_ID);
        destinationSymbolLayer.withProperties(
                iconImage(DESTINATION_ICON_ID),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }
}
