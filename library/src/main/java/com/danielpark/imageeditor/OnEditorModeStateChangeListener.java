package com.danielpark.imageeditor;

/**
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public interface OnEditorModeStateChangeListener {

    /**
     * Deliver current EditorMode state
     * @param currentEditorMode
     */
    void onEditorModeState(EditorMode currentEditorMode);
}
