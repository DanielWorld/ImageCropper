package com.danielpark.imageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.danielpark.imagecropper.UtilMode;
import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;

/**
 * Unlike {@link com.danielpark.imagecropper.CropperImageView}, it is used for editing image
 * <br>
 *     1. EditorImageView is actually ViewGroup, it manages lots of other images...
 * <br><br>
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public class EditorPanelView extends RelativeLayout implements EditorInterface{

    // Undo / Redo Pen & Eraser Listener
    private OnUndoRedoStateChangeListener mOnUndoRedoStateChangeListener;

    public EditorPanelView(Context context) {
        this(context, null);
    }

    public EditorPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(Color.parseColor("#FFFFFF"));    // Set background color

        // Daniel (2017-07-26 14:47:11): Add new pen view
        addPenPage();

        setOnTouchListener(mTouchListener);     // Add Touch Listener

    }

    OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };

    @Override
    public void setEditorMode(EditorMode editorMode) {

        if (editorMode == EditorMode.EDIT) {
            // And set all FingerImageViews to modifiable
            for (int index = 0; index < getChildCount(); index++) {
                View childView = getChildAt(index);

                if (childView instanceof FingerImageView) {
                    ((FingerImageView) childView).setManipulationMode(true);
                }
                else if (childView instanceof FingerPenView) {
                    ((FingerPenView) childView).setEditorMode(editorMode);
                }
            }
        } else {
            // And set all FingerImageViews to modifiable
            for (int index = 0; index < getChildCount(); index++) {
                View childView = getChildAt(index);

                if (childView instanceof FingerImageView) {
                    ((FingerImageView) childView).setManipulationMode(false);
                }
                else if (childView instanceof FingerPenView) {
                    ((FingerPenView) childView).setEditorMode(editorMode);
                }
            }
        }
    }

    @Override
    public void addImage(Bitmap bitmap) {

        // Daniel (2017-07-26 14:47:11): Add new ImageView
        RelativeLayout.LayoutParams layoutParams
                = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FingerImageView iv  = new FingerImageView(getContext());
        iv.setLayoutParams(layoutParams);
        iv.imageSet(bitmap);

        iv.setManipulationMode(true);

        // Daniel (2017-07-26 17:04:42): Image should be lower than pen view
        addView(iv, getChildCount() <= 1 ? 0 : getChildCount() - 1);

        // And set all FingerImageViews to modifiable
        for (int index = 0; index < getChildCount(); index++) {
            View childView = getChildAt(index);

            if (childView instanceof FingerImageView) {
                ((FingerImageView) childView).setManipulationMode(true);
            }
        }
    }

    private void addPenPage() {
        RelativeLayout.LayoutParams layoutParams
                = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FingerPenView iv  = new FingerPenView(getContext());
        iv.setLayoutParams(layoutParams);
        iv.setUndoRedoListener(mOnUndoRedoStateChangeListener);

        addView(iv);
    }

    @Override
    public void deleteImage() {

        // TODO: 현재는 manipulated 된 것을 모두 삭제처리
        for (int index = getChildCount() - 1; index >= 0; index--) {
            if (index < 0) return;

            View childView = getChildAt(index);

            if (childView instanceof FingerImageView
                    && ((FingerImageView)childView).isManipulationMode()) {

               removeViewAt(index);
            }
        }
    }

    @Override
    public void setUndo() {
        for (int index = 0; index < getChildCount(); index++) {
            View childView = getChildAt(index);

            if (childView instanceof FingerPenView) {
                ((FingerPenView) childView).setUndo();
                break;
            }
        }
    }

    @Override
    public void setRedo() {
        for (int index = 0; index < getChildCount(); index++) {
            View childView = getChildAt(index);

            if (childView instanceof FingerPenView) {
                ((FingerPenView) childView).setRedo();
                break;
            }
        }
    }

    @Override
    public void setUndoRedoListener(OnUndoRedoStateChangeListener listener) {
        mOnUndoRedoStateChangeListener = listener;
    }
}
