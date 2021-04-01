package com.demo.maniva.Manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.demo.maniva.R;
import com.demo.maniva.presentation.NavigationActivity;
import com.demo.maniva.utils.IntentUtil;
import com.demo.maniva.utils.PreferenceUtil;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Looper.getMainLooper;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class MapboxManager {

    public static final String GEO_JSON_SOURCE_LAYER_ID = "geojsonSourceLayerId";
    public static final String DESTINATION_SYMBOL_LAYER_ID = "destination-symbol-layer-id";
    public static final String DESTINATION_ICON_ID = "destination-icon-id";
    public static final String DESTINATION_SOURCE_ID = "destination-source-id";
    public static final String SYMBOL_LAYER_ID = "SYMBOL_LAYER_ID";

    private final MapboxMap mMapboxMap;
    private final MapView mMapView;
    private final Context mContext;
    private final PermissionsManager mPermissionsManager;

    private LocationEngine locationEngine;
    private NavigationMapRoute mNavigationMapRoute;
    private DirectionsRoute mCurrentRoute;
    private Point mOriginPoint;


    public MapboxManager(MapView mapView, MapboxMap mapboxMap, PermissionsManager permissionsManager, Context context) {
        this.mMapboxMap = mapboxMap;
        this.mContext = context;
        this.mMapView = mapView;
        //TODO Move it here completely and callback to home
        this.mPermissionsManager = permissionsManager;
    }


    public void initMapbox() {
        mMapboxMap.getUiSettings().setZoomGesturesEnabled(true);
        mMapboxMap.getUiSettings().setQuickZoomGesturesEnabled(true);
        mMapboxMap.setStyle(mContext.getString(R.string.navigation_guidance_day), style -> {
            enableLocationComponent(style);
            addDestinationIconSymbolLayer(style);
            setUpSource(style);
            setupLayer(style);
        });
    }

    @SuppressWarnings({"MissingPermission"})
    public void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(mContext)) {

            // Get an instance of the component
            LocationComponent locationComponent = mMapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(mContext, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();


        } else {
            mPermissionsManager.requestLocationPermissions((Activity) mContext);
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(mContext);

        LocationEngineRequest request = new LocationEngineRequest.Builder(1000)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(5000).build();

        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
        locationEngine.getLastLocation(locationEngineCallback);
    }

    LocationEngineCallback<LocationEngineResult> locationEngineCallback = new LocationEngineCallback<LocationEngineResult>() {
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
            /*TODO send call back and show toast on activity*/
        }
    };

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
                            // You can get the generic HTTP info about the response
                            if (response.body() == null) {
                                return;
                            } else if (response.body().routes().size() < 1) {
                                return;
                            }
                            mCurrentRoute = response.body().routes().get(0);
                            // Draw the route on the map
                            if (mNavigationMapRoute != null) {
                                mNavigationMapRoute.updateRouteVisibilityTo(false);
                                mNavigationMapRoute.updateRouteArrowVisibilityTo(false);
                                mNavigationMapRoute.addRoute(mCurrentRoute);
                            } else {
                                try {
                                    mNavigationMapRoute = new NavigationMapRoute(null, mMapView, mMapboxMap, R.style.NavigationMapRoute);
                                    mNavigationMapRoute.addRoute(mCurrentRoute);
                                } catch (Exception e) {

                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        }
                    });
        }
    }

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


    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(GEO_JSON_SOURCE_LAYER_ID));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        String symbolIconId = "symbolIconId";
        loadedMapStyle.addLayer(new SymbolLayer(SYMBOL_LAYER_ID, GEO_JSON_SOURCE_LAYER_ID).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[]{0f, -8f})
        ));
    }

    public void drawMarkerFromSelectedAddress(CarmenFeature selectedCarmenFeature, Point destinationPoint) {
        Style style = mMapboxMap.getStyle();
        if (style != null) {
            // Move map camera to the selected location
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
        Toast.makeText(mContext, R.string.msg_drive_mode, Toast.LENGTH_LONG).show();
        PreferenceUtil.getInstance(mContext).setDirectionRoute(mCurrentRoute);
        IntentUtil.launchActivityIntentForclass(mContext, NavigationActivity.class);
    }
}
