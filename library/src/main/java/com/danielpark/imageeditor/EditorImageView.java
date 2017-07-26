package com.danielpark.imageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;

/**
 * Unlike {@link com.danielpark.imagecropper.CropperImageView}, it is used for editing image
 * <br>
 *     1. EditorImageView is actually ViewGroup, it manages lots of other images...
 * <br><br>
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public class EditorImageView extends RelativeLayout implements EditorInterface{

    // Undo / Redo Pen & Eraser Listener
    private OnUndoRedoStateChangeListener mOnUndoRedoStateChangeListener;

    public EditorImageView(Context context) {
        this(context, null);
    }

    public EditorImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(Color.parseColor("#FFFFFF"));    // Set background color

        setOnTouchListener(mTouchListener);     // Add Touch Listener
    }

    OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };

    @Override
    public void addImage(Bitmap bitmap) {

        // Daniel (2017-07-26 14:47:11): Add new ImageView
        RelativeLayout.LayoutParams layoutParams
                = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FingerImageView iv  = new FingerImageView(getContext());
        iv.setLayoutParams(layoutParams);
        iv.imageSet(bitmap);

        iv.setManipulationMode(true);

        addView(iv);

        // And set all FingerImageViews to modifiable
        for (int index = 0; index < getChildCount(); index++) {
            View childView = getChildAt(index);

            if (childView instanceof FingerImageView) {
                ((FingerImageView) childView).setManipulationMode(true);
            }
        }
    }

    @Override
    public void setEditorMode(EditorMode editorMode) {

        if (editorMode == EditorMode.EDIT) {
            // And set all FingerImageViews to modifiable
            for (int index = 0; index < getChildCount(); index++) {
                View childView = getChildAt(index);

                if (childView instanceof FingerImageView) {
                    ((FingerImageView) childView).setManipulationMode(true);
                }
            }
        } else {
            // And set all FingerImageViews to modifiable
            for (int index = 0; index < getChildCount(); index++) {
                View childView = getChildAt(index);

                if (childView instanceof FingerImageView) {
                    ((FingerImageView) childView).setManipulationMode(false);
                }
            }
        }
    }

    @Override
    public void setUndo() {

    }

    @Override
    public void setRedo() {

    }

    @Override
    public void setUndoRedoListener(OnUndoRedoStateChangeListener listener) {
        mOnUndoRedoStateChangeListener = listener;
    }
}
