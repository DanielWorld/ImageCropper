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
package com.danielworld.image_cropper_sample.presenter;

import android.app.Activity;

/**
 * Created by Namgyu Park on 2016-06-25.
 */
public interface MainPresenter extends Presenter{

    @Override
    void initViews();

    /**
     * Check permissions
     */
    void checkPermissions();

    /**
     * Click done button
     */
    void clickDone();

    /**
     * Click Camera button
     */
    void clickCamera();

    /**
     * Rotate image to clockwise
     */
    void clickClockwise();

    /**
     * Rotate image to counter-clockwise
     */
    void clickCounterClockwise();

    /**
     * Change mode (crop vs pen vs eraser)
     */
    void clickModeChange(int index);

    /**
     * undo path (It works only when Crop mode is NO_CROP)
     */
    void clickUndo();

    /**
     * redo path (It works only when Crop mode is NO_CROP)
     */
    void clickRedo();

    /**
     * Change shape (default vs rectangle)
     * @param index
     */
    void clickShapeChange(int index);

    /**
     * Show toast message
     */
    void showMessage(String message);

    /**
     * Request permissions
     * @param activity
     * @param requestCode
     */
    void requestPermissions(Activity activity, int requestCode);

    @Override
    void onDestroy();

    interface View {

        /**
         * Set default View Layout
         */
        void setLayout();

        /**
         * Handler for permissions
         * @param result
         * @param permissions
         */
        void resultPermissions(boolean result, String[] permissions);

        void setPicture();

        void setRotateClockwise();

        void setRotateCounterClockwise();

        void setUndo();

        void setRedo();

        void setMode();

        void openCamera();

        void setShape();

        void showMessage(String message);
    }
}
