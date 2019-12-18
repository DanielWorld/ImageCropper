package com.danielpark.imagecroppersample.interactor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

/**
 * Created by Daniel Park on 2016-06-25.
 */
public class PermissionInterActor {

    public interface OnFinishedListener {
        void onPermissionResult(boolean result, String[] permissions);
    }

    /**
     * Check {@link android.Manifest.permission#READ_EXTERNAL_STORAGE}, {@link android.Manifest.permission#CAMERA} permission
     * @param context
     * @param listener
     */
    public void checkPermissions(Context context, OnFinishedListener listener) {
        boolean storage = false, camera = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                storage = true;

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                camera = true;

            if (storage && camera) {
                if (listener != null)
                    listener.onPermissionResult(true, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
            }
            else if (storage) {
                if (listener != null)
                    listener.onPermissionResult(false, new String[]{Manifest.permission.CAMERA});
            }
            else if (camera) {
                if (listener != null)
                    listener.onPermissionResult(false, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }
            else {
                if (listener != null)
                    listener.onPermissionResult(false, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
            }
        } else {
            if (listener != null)
                listener.onPermissionResult(true, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(Activity activity, int requestCode) {
        activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(Fragment fragment, int requestCode) {
        fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, requestCode);
    }
}
