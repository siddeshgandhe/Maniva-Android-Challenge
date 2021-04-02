package com.demo.maniva.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;

public class PreferenceUtil {

    private static final String PREF_NAME = "maniva_pref";

    private static PreferenceUtil sInstance;
    private final SharedPreferences mSharedPreferences;
    private final SharedPreferences.Editor mEditor;

    @SuppressLint("CommitPrefEdits")
    private PreferenceUtil(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
        mEditor = mSharedPreferences.edit();
    }

    public static PreferenceUtil getInstance(Context context) {
        if (sInstance == null) {
            return sInstance = new PreferenceUtil(context);
        }
        return sInstance;
    }

    public DirectionsRoute getDirectionRoute() {
        String directionsRouteJson = mSharedPreferences.getString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, "");
        return DirectionsRoute.fromJson(directionsRouteJson);
    }

    public void setDirectionRoute(DirectionsRoute directionsRoute) {
        try {
            String value = directionsRoute.toJson();
            mEditor.putString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, value);
            mEditor.apply();
            mEditor.commit();
        } catch (NullPointerException exception) { }
    }
}
