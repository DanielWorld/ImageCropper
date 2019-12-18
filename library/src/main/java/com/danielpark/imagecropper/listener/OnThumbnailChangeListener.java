package com.danielpark.imagecropper.listener;

import android.graphics.Bitmap;

/**
 * Created by Daniel on 2016-08-08.
 */
public interface OnThumbnailChangeListener {

    /**
     * When user left touch down and move then it will return thumbnail bitmap
     * @param bitmap
     */
    void onThumbnailChanged(Bitmap bitmap);
}
