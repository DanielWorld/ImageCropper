package com.danielpark.imagecroppersample.presenter;

/**
 * Created by Daniel Park on 2016-06-25.
 */
public interface Presenter {

    void initViews();

    void onResume();

    void onPause();

    /**
     * It is called to disconnect with View (interface) <br><br>
     *     It should be called before super.onDestroy() is called.
     */
    void onDestroy();
}
