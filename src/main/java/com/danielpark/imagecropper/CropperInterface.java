package com.danielpark.imagecropper;

import android.graphics.Bitmap;

import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;

import java.io.File;

/**
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-06-23.
 */
public interface CropperInterface {

    /**
     * If it is true, then when creating output file, crop image will be filled in rectangle. <br>
     *     defautl mode is {@link CropMode#CROP_STRETCH}
     * @param mode
     */
    void setStretchMode(CropMode mode);

    /**
     * Only works when CropMode is {@link CropMode#NO_CROP}
     * @param mode
     */
    void setUtilMode(UtilMode mode);

    /**
     * Whether control button should be in image or not <br>
     *     default is false
     * @param result <code>true</code> control button should be inside of Image
     */
    void setControlInImage(boolean result);

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
     * Set Image File to CropImageView
     * @param file
     */
    void setCustomImageFile(File file);

    /**
     * Set Image File to CropImageView with degree
     * @param file
     * @param degree
     */
    void setCustomImageFile(File file, int degree);

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
     * Undo the path (It only works when Crop mode is NO_CROP)
     */
    void setUndo();

    /**
     * Redo the path (It only works when Crop mode is NO_CROP)
     */
    void setRedo();

    /**
     * When Undo or Redo state has changed, notify to observer <br>
     *     BEWARE ! {@link OnUndoRedoStateChangeListener} should be applied to {@link CropperImageView} for each one by one <br>
     *         because it becomes null when {@link CropperImageView#onDetachedFromWindow()}
     * @param listener
     */
    void setUndoRedoListener(OnUndoRedoStateChangeListener listener);

    /**
     * Daniel (2016-06-21 17:25:44): Try to crop Image from original image
     */
    File getCropImage();

	/**
	 * Try to recycle current rendered bitmap
	 */
	void onRecycleBitmap();
}
