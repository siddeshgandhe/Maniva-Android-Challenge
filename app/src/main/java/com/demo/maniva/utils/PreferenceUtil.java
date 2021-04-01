package com.demo.maniva.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;

public class PreferenceUtil {

    private static final String PREF_NAME = "maniva_pref";

    private static PreferenceUtil mInstance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    private PreferenceUtil(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
        editor = sharedPreferences.edit();
    }

    public static PreferenceUtil getInstance(Context context) {
        if (mInstance == null) {
            return mInstance = new PreferenceUtil(context);
        }
        return mInstance;
    }

    public DirectionsRoute getDirectionRoute() {
        String directionsRouteJson = sharedPreferences.getString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, "");
        return DirectionsRoute.fromJson(directionsRouteJson);
    }

    public void setDirectionRoute(DirectionsRoute directionsRoute) {
        String value = "";
        if (directionsRoute != null) {
            value = directionsRoute.toJson();
        }
        editor.putString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, value);
        editor.apply();
        editor.commit();
    }
}
