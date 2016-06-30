package com.danielpark.imagecropper.listener;

/**
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-06-30.
 */
public interface OnUndoRedoListener {

    void onUndoAvailable(boolean result);

    void onRedoAvailable(boolean result);
}
