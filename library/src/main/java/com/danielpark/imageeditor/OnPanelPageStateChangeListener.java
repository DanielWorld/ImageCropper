package com.danielpark.imageeditor;

/**
 * Created by Daniel Park on 2017-07-26.
 */

public interface OnPanelPageStateChangeListener {

    void onPrevPanelPageAvailable(boolean result);

    void onNextPanelPageAvailable(boolean result);
}
