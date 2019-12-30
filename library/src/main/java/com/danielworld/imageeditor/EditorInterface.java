/*
 * Copyright (c) 2017 DanielWorld.
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
package com.danielworld.imageeditor;

import android.graphics.Bitmap;

import com.danielworld.imagecropper.CropperImageView;
import com.danielworld.imagecropper.listener.OnUndoRedoStateChangeListener;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Namgyu Park on 2017-07-26.
 */

public interface EditorInterface {

    /**
     * Set editor mode
     * @param editorMode
     */
    void setEditorMode(EditorMode editorMode);

    /**
     * Add image to View
     * @param bitmap
     */
    void addImage(Bitmap bitmap);

    /**
     * Delete image which is selected
     */
    void deleteImage();

    /**
     * Add panel page
     */
    void addPanelPage();

    /**
     * Move to previous Panel Page
     */
    void prevPanelPage();

    /**
     * Move to next Panel Page
     */
    void nextPanelPage();

    /**
     * Undo the mRectanglePath (It only works when Crop mode is NONE)
     */
    void setUndo();

    /**
     * Redo the mRectanglePath (It only works when Crop mode is NONE)
     */
    void setRedo();

    /**
     * Rotate image by degree
     * @param degree
     */
    void setRotationBy(int degree);

    /**
     * Delete all pen and eraser marks
     */
    void deletePen();

    void setPenColor(int penColor);

    void setPenWidth(int penWidth);

    ArrayList<File> getEditedFiles();

    /**
     * When Undo or Redo state has changed, notify to observer <br>
     *     BEWARE ! {@link OnUndoRedoStateChangeListener} should be applied to {@link CropperImageView} for each one by one <br>
     *         because it becomes null when CropperImageView.onDetachedFromWindow() was called.
     * @param listener
     */
    void setUndoRedoListener(OnUndoRedoStateChangeListener listener);

    /**
     * When EditorMode state has changed, notify to observer
     */
    void setOnEditorModeStateChangeListener(OnEditorModeStateChangeListener listener);

    /**
     * When page was added or page Prev or Next has changed, notify to observer <br>
     *
     * @param listener
     */
    void setOnPanelPageStateChangeListener(OnPanelPageStateChangeListener listener);
}
