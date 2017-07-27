package com.danielpark.imageeditor;

import com.danielpark.imagecropper.CropperImageView;
import com.danielpark.imagecropper.UtilMode;
import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;

/**
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public interface FingerPenInterface {

    /**
     * Set editor mode
     * @param editorMode
     */
    void setEditorMode(EditorMode editorMode);

    void setPenColor(int penColor);

    void setPenWidth(int penWidth);

    void setEraserColor(int eraserColor);

    void setEraserWidth(int eraserWidth);

    /**
     * Undo the mRectanglePath (It only works when Crop mode is NONE)
     */
    void setUndo();

    /**
     * Redo the mRectanglePath (It only works when Crop mode is NONE)
     */
    void setRedo();

    /**
     * Update undo / redo and receive events
     */
    void updateUndoRedo();

    /**
     * Delete all pen and eraser
     */
    void deletePen();

    /**
     * When Undo or Redo state has changed, notify to observer <br>
     *     BEWARE ! {@link OnUndoRedoStateChangeListener} should be applied to {@link CropperImageView} for each one by one <br>
     *         because it becomes null when {@link CropperImageView#onDetachedFromWindow()}
     * @param listener
     */
    void setUndoRedoListener(OnUndoRedoStateChangeListener listener);
}
