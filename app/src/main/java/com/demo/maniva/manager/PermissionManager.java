package com.demo.maniva.manager;

import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.maniva.listener.PermissionListener;

public class PermissionManager {

    public static final int REQUEST_CODE_ASK_PERMISSIONS = 1002;

    private PermissionListener mPermissionListener;

    public PermissionManager(PermissionListener mPermissionListener) {
        this.mPermissionListener = mPermissionListener;
    }

    public void checkPermissionForNetworkState(AppCompatActivity appCompatActivity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int res = appCompatActivity.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);
            if (res != PackageManager.PERMISSION_GRANTED) {
                appCompatActivity.requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                mPermissionListener.onPermissionGranted();
            }
        } else {
            mPermissionListener.onPermissionGranted();
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    mPermissionListener.onPermissionDenied();
                } else {
                    mPermissionListener.onPermissionGranted();
                }
                break;

        }
    }

}
