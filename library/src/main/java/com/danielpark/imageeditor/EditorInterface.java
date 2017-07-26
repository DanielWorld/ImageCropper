package com.danielpark.imageeditor;

import android.graphics.Bitmap;

import com.danielpark.imagecropper.CropperImageView;
import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;

/**
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
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
     * Delete image which is selcted
     */
    void deleteImage();

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
     *         because it becomes null when {@link CropperImageView#onDetachedFromWindow()}
     * @param listener
     */
    void setUndoRedoListener(OnUndoRedoStateChangeListener listener);
}
