package com.danielpark.imageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.danielpark.imagecropper.R;
import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Unlike {@link com.danielpark.imagecropper.CropperImageView}, it is used for editing image
 * <br>
 *     1. EditorImageView is actually ViewGroup, it manages lots of other images...
 * <br><br>
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public class EditorPanelView extends RelativeLayout implements EditorInterface{

    private EditorMode mEditorMode;

    private int mCurrentPanelPageIndex = -1;

    // panel pages list
    private final ArrayList<RelativeLayout> mEditorPanelPage = new ArrayList<>();

    // Editor mode state change listener;
    private OnEditorModeStateChangeListener mOnEditorModeStateChangeListener;

    // Undo / Redo Pen & Eraser Listener
    private OnUndoRedoStateChangeListener mOnUndoRedoStateChangeListener;

    // Prev / Next panel page listener
    private OnPanelPageStateChangeListener mOnPanelPageStateChangeListener;

    public EditorPanelView(Context context) {
        this(context, null);
    }

    public EditorPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        setBackgroundColor(Color.parseColor("#FFFFFF"));    // Set background color

        initRootPanelPage();

        addPanelPage();
    }

    private void initRootPanelPage() {
        RelativeLayout.LayoutParams layoutParams
                = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout rootPanelPage = new RelativeLayout(getContext());
        rootPanelPage.setLayoutParams(layoutParams);

        addView(rootPanelPage);
    }

    private void addPenPage() {
        RelativeLayout.LayoutParams layoutParams
                = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FingerPenView iv  = new FingerPenView(getContext());
        iv.setLayoutParams(layoutParams);
        iv.setUndoRedoListener(mFingerViewUndoRedoListener);

        mEditorPanelPage.get(mCurrentPanelPageIndex).addView(iv);
    }

//    OnTouchListener mTouchListener = new OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            return false;
//        }
//    };

    @Override
    public void setEditorMode(EditorMode editorMode) {

        mEditorMode = editorMode;

        if (editorMode == EditorMode.EDIT) {
            // And set all FingerImageViews to modifiable
            for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
                View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

                if (childView instanceof FingerImageView) {
                    ((FingerImageView) childView).setManipulationMode(true);
                }
                else if (childView instanceof FingerPenView) {
                    ((FingerPenView) childView).setEditorMode(editorMode);
                }
            }
        } else {
            // And set all FingerImageViews to modifiable
            for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
                View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

                if (childView instanceof FingerImageView) {
                    ((FingerImageView) childView).setManipulationMode(false);
                }
                else if (childView instanceof FingerPenView) {
                    ((FingerPenView) childView).setEditorMode(editorMode);
                }
            }
        }

        if (mOnEditorModeStateChangeListener != null)
            mOnEditorModeStateChangeListener.onEditorModeState(editorMode);
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
//        addView(iv, getChildCount() <= 1 ? 0 : getChildCount() - 1);
        mEditorPanelPage.get(mCurrentPanelPageIndex).addView(iv,
                mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount() <= 1 ? 0 :
                        mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount() - 1);

        // And set all FingerImageViews to modifiable
        for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
            View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

            if (childView instanceof FingerImageView) {
                ((FingerImageView) childView).setManipulationMode(true);
            }
        }

        // TODO: if you add image then, make sure that current EditorMode state should be 'EDIT' mode
        setEditorMode(EditorMode.EDIT);
    }

    @Override
    public void deleteImage() {
        for (int index = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount() - 1; index >= 0; index--) {
            if (index < 0) return;

            View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

            if (childView instanceof FingerImageView
                    && ((FingerImageView)childView).isManipulationMode()) {

                mEditorPanelPage.get(mCurrentPanelPageIndex).removeViewAt(index);
            }
        }
    }

    @Override
    public void addPanelPage() {
        //--------------------------------------------------------------------------------
        RelativeLayout rootPanelPage = (RelativeLayout) getChildAt(0);

        RelativeLayout.LayoutParams layoutParams
                = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        // Add first page
        RelativeLayout panelPage = new RelativeLayout(getContext());
        panelPage.setLayoutParams(layoutParams);
        panelPage.setBackgroundColor(Color.parseColor("#772611"));    // Set background color);
//        panelPage.setOnTouchListener(mTouchListener);

        mEditorPanelPage.add(panelPage);
        rootPanelPage.addView(panelPage);

        mCurrentPanelPageIndex = mEditorPanelPage.size() - 1;
        // ------ END add Panel Page --------------------------------------------------------

        // When you add Panel page, you also have to add pen page
        addPenPage();

        // Mode should be Pen
        setEditorMode(EditorMode.PEN);

        // Animation effect
        if (mEditorPanelPage.size() > 1) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exit_slide_left);
            mEditorPanelPage.get(mCurrentPanelPageIndex - 1).startAnimation(animation);

            // When you add page from first page.
            if (mCurrentPanelPageIndex != mEditorPanelPage.size() - 1) {
                Animation animation2 = AnimationUtils.loadAnimation(getContext(), R.anim.enter_slide_right);
                mEditorPanelPage.get(++mCurrentPanelPageIndex).startAnimation(animation2);
            } else {
                Animation animation2 = AnimationUtils.loadAnimation(getContext(), R.anim.enter_slide_right);
                mEditorPanelPage.get(mCurrentPanelPageIndex).startAnimation(animation2);
            }
        }

        onUpdatePanelPageState();

        onUpdateUndoRedoState();
    }

    @Override
    public void prevPanelPage() {
        if (mCurrentPanelPageIndex == 0)
            return;

        // Update current panel page index
        mCurrentPanelPageIndex -= 1;

        // Mode should be Pen
        setEditorMode(EditorMode.PEN);

        // Animation effect
        if (mEditorPanelPage.size() > 1) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exit_slide_right);
            animation.setAnimationListener(prevAnimationListener);
            mEditorPanelPage.get(mCurrentPanelPageIndex + 1).startAnimation(animation);

            Animation animation2 = AnimationUtils.loadAnimation(getContext(), R.anim.enter_slide_left);
            mEditorPanelPage.get(mCurrentPanelPageIndex).startAnimation(animation2);
        }

        onUpdatePanelPageState();

        onUpdateUndoRedoState();
    }

    @Override
    public void nextPanelPage() {
        if (mEditorPanelPage.size() == mCurrentPanelPageIndex + 1)
            return;

        // Update current panel page index
        mCurrentPanelPageIndex += 1;

        // Mode should be Pen
        setEditorMode(EditorMode.PEN);

        // Animation effect
        if (mEditorPanelPage.size() > 1) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exit_slide_left);
            animation.setAnimationListener(nextAnimationListener);
            mEditorPanelPage.get(mCurrentPanelPageIndex - 1).startAnimation(animation);

            Animation animation2 = AnimationUtils.loadAnimation(getContext(), R.anim.enter_slide_right);
            mEditorPanelPage.get(mCurrentPanelPageIndex).startAnimation(animation2);
        }

        onUpdatePanelPageState();

        onUpdateUndoRedoState();
    }

    private void onUpdatePanelPageState() {
        // trigger listener
        if (mCurrentPanelPageIndex == 0) {
            if (mOnPanelPageStateChangeListener != null) {
                mOnPanelPageStateChangeListener.onPrevPanelPageAvailable(false);
            }

            if (mCurrentPanelPageIndex != mEditorPanelPage.size() - 1) {
                if (mOnPanelPageStateChangeListener != null) {
                    mOnPanelPageStateChangeListener.onNextPanelPageAvailable(true);
                }
            } else {
                if (mOnPanelPageStateChangeListener != null) {
                    mOnPanelPageStateChangeListener.onNextPanelPageAvailable(false);
                }
            }
        }
        else if (mCurrentPanelPageIndex == mEditorPanelPage.size() - 1) {
            if (mOnPanelPageStateChangeListener != null) {
                mOnPanelPageStateChangeListener.onNextPanelPageAvailable(false);
            }

            if (mCurrentPanelPageIndex == 0) {
                if (mOnPanelPageStateChangeListener != null) {
                    mOnPanelPageStateChangeListener.onPrevPanelPageAvailable(false);
                }
            } else {
                if (mOnPanelPageStateChangeListener != null) {
                    mOnPanelPageStateChangeListener.onPrevPanelPageAvailable(true);
                }
            }
        } else {
            if (mOnPanelPageStateChangeListener != null) {
                mOnPanelPageStateChangeListener.onPrevPanelPageAvailable(true);
                mOnPanelPageStateChangeListener.onNextPanelPageAvailable(true);
            }
        }
    }

    private void onUpdateUndoRedoState() {
        for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
            View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

            if (childView instanceof FingerPenView) {
                ((FingerPenView) childView).updateUndoRedo();
                break;
            }
        }
    }

    @Override
    public void setUndo() {
        if (mEditorMode != EditorMode.PEN && mEditorMode != EditorMode.ERASER) return;

        for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
            View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

            if (childView instanceof FingerPenView) {
                ((FingerPenView) childView).setUndo();
                break;
            }
        }
    }

    @Override
    public void setRedo() {
        if (mEditorMode != EditorMode.PEN && mEditorMode != EditorMode.ERASER) return;

        for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
            View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

            if (childView instanceof FingerPenView) {
                ((FingerPenView) childView).setRedo();
                break;
            }
        }
    }

    @Override
    public void setRotationBy(int degree) {
        if (mEditorMode != EditorMode.EDIT) return;

        for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
            View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

            if (childView instanceof FingerImageView
                    && ((FingerImageView) childView).isManipulationMode()) {
                ((FingerImageView) childView).setRotation90Image();
//                break;
            }
        }
    }

    @Override
    public void deletePen() {
        if (mEditorMode != EditorMode.PEN && mEditorMode != EditorMode.ERASER) return;

        for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
            View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

            if (childView instanceof FingerPenView) {
                ((FingerPenView) childView).deletePen();
                break;
            }
        }
    }

    @Override
    public void setPenColor(int penColor) {
        if (mEditorMode != EditorMode.PEN && mEditorMode != EditorMode.ERASER) return;

        for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
            View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

            if (childView instanceof FingerPenView) {
                ((FingerPenView) childView).setPenColor(penColor);
                break;
            }
        }
    }

    @Override
    public void setPenWidth(int penWidth) {
        if (mEditorMode != EditorMode.PEN && mEditorMode != EditorMode.ERASER) return;

        for (int index = 0; index < mEditorPanelPage.get(mCurrentPanelPageIndex).getChildCount(); index++) {
            View childView = mEditorPanelPage.get(mCurrentPanelPageIndex).getChildAt(index);

            if (childView instanceof FingerPenView) {
                ((FingerPenView) childView).setPenWidth(penWidth);
                break;
            }
        }
    }

    @Override
    public ArrayList<File> getEditedFiles() {
        final ArrayList<File> fileList = new ArrayList<>();

        if (mEditorPanelPage.isEmpty()) return fileList;

        // TODO: 이미지 가져올 시 기기별 제한 필요 (TODO!!)
        for (RelativeLayout rl : mEditorPanelPage) {
            rl.setDrawingCacheEnabled(true);
            rl.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            // Daniel (2017-08-07 11:17:20): 해당 view bitmap cache 를 recycle 처리할 경우, 해당 view 가 속한 Fragment 는 destroy 후 재생성해서 사용해야 한다.
            fileList.add(saveFile(rl.getDrawingCache(), true));
            // TODO: Recycle 처리를 할 경우 해당 view 사용 못함
//            fileList.add(saveFile(rl.getDrawingCache(), false));
        }

        return fileList;
    }

    @Override
    public void setUndoRedoListener(OnUndoRedoStateChangeListener listener) {
        mOnUndoRedoStateChangeListener = listener;
    }

    @Override
    public void setOnEditorModeStateChangeListener(OnEditorModeStateChangeListener listener) {
        mOnEditorModeStateChangeListener = listener;
    }

    @Override
    public void setOnPanelPageStateChangeListener(OnPanelPageStateChangeListener listener) {
        mOnPanelPageStateChangeListener = listener;
    }

    private File saveFile(Bitmap bitmap, boolean shouldRecycle) {
        File dstFile = null;

        OutputStream output = null;

        // Daniel (2016-06-24 11:52:55): if dstFile is invalid, we create our own file and return it to user!
//            final File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Bapul/");
        // Daniel (2017-01-20 12:07:43): Use cache directory instead
        final File filePath = getContext().getExternalCacheDir();

        if (filePath != null && !filePath.exists()) {
            filePath.mkdirs();
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS")
                .format(new Date());

        dstFile = new File(filePath, "CropperLibrary_" + timeStamp + "_.jpg");
        try {
            dstFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            output = new FileOutputStream(dstFile);

//                bitmap.compress(Bitmap.CompressFormat.PNG, 95, output);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output);

            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (shouldRecycle && bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }

        return dstFile;
    }

    /**
     * First page's undo / redo listener could be null, so make bridge between views
     */
    private OnUndoRedoStateChangeListener mFingerViewUndoRedoListener = new OnUndoRedoStateChangeListener() {
        @Override
        public void onUndoAvailable(boolean result) {
            if (mOnUndoRedoStateChangeListener != null)
                mOnUndoRedoStateChangeListener.onUndoAvailable(result);
        }

        @Override
        public void onRedoAvailable(boolean result) {
            if (mOnUndoRedoStateChangeListener != null)
                mOnUndoRedoStateChangeListener.onRedoAvailable(result);
        }
    };

    private Animation.AnimationListener prevAnimationListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mEditorPanelPage.get(mCurrentPanelPageIndex + 1).setVisibility(View.INVISIBLE);
            mEditorPanelPage.get(mCurrentPanelPageIndex).setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    private Animation.AnimationListener nextAnimationListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mEditorPanelPage.get(mCurrentPanelPageIndex - 1).setVisibility(View.INVISIBLE);
            mEditorPanelPage.get(mCurrentPanelPageIndex).setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
}
