package com.danielpark.imagecropper;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-06-23.
 */
public interface CropperInterface {

    /**
     * If it is true, then when creating output file, crop image will be filled in rectangle.
     * @param result
     */
    void setStretchMode(boolean result);

    /**
     * Set Image bitmap to CropImageView
     * @param bitmap
     */
    void setCustomImageBitmap(Bitmap bitmap);

    /**
     * Set Image bitmap to CropImageView with degree
     * @param bitmap
     * @param degree
     */
    void setCustomImageBitmap(Bitmap bitmap, int degree);

    /**
     * Set degree
     * @param degrees
     */
    void setRotationTo(float degrees);

    /**
     * add degree
     * @param degrees
     */
    void setRotationBy(float degrees);

    /**
     * upside down
     */
    void setReverseUpsideDown();

    /**
     * change left to right and vice versa
     */
    void setReverseRightToLeft();

    /**
     * Daniel (2016-06-21 17:25:44): Try to crop Image from original image
     */
    File getCropImage();
}
