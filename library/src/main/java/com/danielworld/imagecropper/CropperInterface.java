/*
 * Copyright (c) 2016 DanielWorld.
 * @Author Namgyu Park
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.danielworld.imagecropper;

import android.graphics.Bitmap;

import com.danielworld.imagecropper.listener.OnThumbnailChangeListener;
import com.danielworld.imagecropper.listener.OnUndoRedoStateChangeListener;
import com.danielworld.imagecropper.model.CropSetting;

import java.io.File;

/**
 * Created by Namgyu Park on 2016-06-23.
 */
public interface CropperInterface {

    /**
     * Set crop setting
     * @param cropSetting
     */
    void setCropSetting(CropSetting cropSetting);

    /**
     * Default mode is {@link CropMode#CROP_STRETCH}
     * @param mode
     */
    void setCropMode(CropMode mode);

    /**
     * Only works when CropMode is {@link CropMode#NONE}
     * @param mode
     */
    void setUtilMode(UtilMode mode);

    /**
     * Set shape mode
     * @param mode
     */
    void setShapeMode(ShapeMode mode);

    /**
     * Set crop control mode
     * @param mode
     */
    void setControlMode(ControlMode mode);

    /**
     * Set crop file extension
     * @param mode
     */
    void setCropExtension(CropExtension mode);

    /**
     * Whether control button should be in image or not <br>
     *     default is false
     * @param result <code>true</code> control button should be inside of Image
     */
//    void setControlInImage(boolean result);

	/**
	 * Set margin between outside border of Bitmap and 4 Crop rectangle border <br>
	 *
	 * @param percent 10.0% ~ 90.0 % (default value is 20%)
	 */
	void setCropInsetRatio(float percent);

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
     * Set Image File to CropImageView with degree
     * @param file
     * @param degree <b>true</b> if you wanna create new file when you crop image
     */
    void setCustomImageFile(File file, int degree, boolean isNewFile);

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
     * Undo the mRectanglePath (It only works when Crop mode is NONE)
     */
    void setUndo();

    /**
     * Redo the mRectanglePath (It only works when Crop mode is NONE)
     */
    void setRedo();

    /**
     * When Undo or Redo state has changed, notify to observer <br>
     *     BEWARE ! {@link OnUndoRedoStateChangeListener} should be applied to {@link CropperImageView} for each one by one <br>
     *         because it becomes null when CropperImageView.onDetachedFromWindow() was called.
     * @param listener
     */
    void setUndoRedoListener(OnUndoRedoStateChangeListener listener);

    /**
     * When user left touch down and move, it will return thumbnail bitmap
     * @param listener
     */
    void setThumbnailChangeListener(OnThumbnailChangeListener listener);

    /**
     * Try to crop Image from original image. <br>
     *     it might return null
     */
    File getCropImage();

    /**
     * Daniel (2016-08-08 11:58:50): Get thumbnail crop image bitmap
     * @return
     */
    Bitmap getCropImageThumbnail();

	/**
	 * Try to recycle current rendered bitmap
	 */
	void onRecycleBitmap();
}
