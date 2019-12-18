package com.danielpark.imageeditor;

/**
 * Created by Daniel Park on 2017-07-26.
 */

public interface OnEditorModeStateChangeListener {

    /**
     * Deliver current EditorMode state
     * @param currentEditorMode
     */
    void onEditorModeState(EditorMode currentEditorMode);
}
