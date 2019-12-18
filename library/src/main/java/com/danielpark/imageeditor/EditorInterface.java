package com.danielpark.imageeditor;

import android.graphics.Bitmap;

import com.danielpark.imagecropper.CropperImageView;
import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;

import java.io.File;
import java.util.ArrayList;

/**
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
