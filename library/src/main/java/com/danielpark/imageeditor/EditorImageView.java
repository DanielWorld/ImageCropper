package com.danielpark.imageeditor;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Unlike {@link com.danielpark.imagecropper.CropperImageView}, it is used for editing image
 * <br>
 *     1. EditorImageView is actually ViewGroup, it manages lots of other images...
 * <br><br>
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public class EditorImageView extends ViewGroup {

    public EditorImageView(Context context) {
        this(context, null);
    }

    public EditorImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(Color.parseColor("#ff4958"));    // Set background color

        setOnTouchListener(mTouchListener);     // Add Touch Listener
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };
}
