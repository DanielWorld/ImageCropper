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

import com.danielworld.imagecropper.CropperImageView;
import com.danielworld.imagecropper.listener.OnUndoRedoStateChangeListener;

/**
 * Created by Namgyu Park on 2017-07-26.
 */

public interface FingerPenInterface {

    /**
     * Set editor mode
     * @param editorMode
     */
    void setEditorMode(EditorMode editorMode);

    void setPenColor(int penColor);

    void setPenWidth(int penWidth);

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
     *         because it becomes null when CropperImageView.onDetachedFromWindow() was called.
     * @param listener
     */
    void setUndoRedoListener(OnUndoRedoStateChangeListener listener);
}
