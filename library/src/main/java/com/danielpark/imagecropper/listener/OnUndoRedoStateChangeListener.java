package com.danielpark.imagecropper.listener;

/**
 * Created by Daniel Park on 2016-06-30.
 */
public interface OnUndoRedoStateChangeListener {

    void onUndoAvailable(boolean result);

    void onRedoAvailable(boolean result);
}
