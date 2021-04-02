package com.demo.maniva.utils;

import android.content.Context;
import android.widget.Toast;

public class UiUtility {
    public static void showToast(Context context, String message, int length) {
        Toast.makeText(context, message, length).show();
    }

    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }
}
