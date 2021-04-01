package com.demo.maniva.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.demo.maniva.BuildConfig;
import com.demo.maniva.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class IntentUtil {

    public static Intent getMapboxAutoCompleteSearchIntent(Context context) {
        return new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : BuildConfig.PUBLIC_KEY)
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(ContextCompat.getColor(context, R.color.mapboxWhite))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS))
                .build((Activity) context);
    }

    public static void launchActivityIntentForclass(Context context, Class clx) {
        Intent navigationIntent = new Intent(context, clx);
        context.startActivity(navigationIntent);
    }

    public static void launchActivityForAction(Context context, String action){
        Intent intent = new Intent(action);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
