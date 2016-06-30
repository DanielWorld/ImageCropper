package com.danielpark.imagecropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.danielpark.imagecropper.listener.OnUndoRedoListener;
import com.danielpark.imagecropper.model.DrawInfo;
import com.danielpark.imagecropper.util.BitmapUtil;
import com.danielpark.imagecropper.util.ConvertUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-06-21.
 */
public class CropperImageView extends ImageView implements CropperInterface{

    Paint mPaint = new Paint();
    Path path = new Path();

    int controlBtnSize = 50; // Daniel (2016-06-21 16:40:26): Radius of Control button
    int controlStrokeSize = 50; // Daniel (2016-06-24 14:32:04): width of Control stroke

    boolean isTouch = false;

    private Drawable[] cropButton = new Drawable[4];    // Daniel (2016-06-21 16:51:49): The drawable to represent Control icon

    /**
     * Daniel (2016-06-21 15:35:22): 4 coordinate spot (Start from right-top to clockwise)
     */
    private Point[] coordinatePoints = new Point[4];

    /**
     * the standard point
     */
    private Point centerPoint = new Point();

    private int touchedButtonIndex = -1;    // Daniel (2016-06-21 16:53:58): touched button's index
    // Daniel (2016-06-21 16:54:19): Start from right-top to clockwise 0, 1, 2, 3

    private RectF mCropRect = new RectF();    // Daniel (2016-06-22 14:08:55): Current cropped shape's rectangle scope

    private int mDrawWidth, mDrawHeight;    // Daniel (2016-06-22 14:26:01): Current visible ImageView's width, height

    private CropMode isCropMode = CropMode.CROP_STRETCH;
    private UtilMode isUtilMode = UtilMode.PENCIL;
    private boolean isControlBtnInImage = false;    // Daniel (2016-06-24 14:33:53): whether control button should be inside of Image

    private Path drawPath;
    private Paint drawPaint;
    private ArrayList<DrawInfo> arrayDrawInfo = new ArrayList<>();
    private ArrayList<DrawInfo> arrayUndoneDrawInfo = new ArrayList<>();

    private OnUndoRedoListener onUndoRedoListener;

    private File dstFile;   // Daniel (2016-06-24 11:47:43): if user set dstFile, Cropped Image will be set to this file!

    public CropperImageView(Context context) {
        this (context, null);
    }

    public CropperImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);

        for (int i = 0; i < coordinatePoints.length; i++) {
            coordinatePoints[i] = new Point();
        }

        for (int i = 0; i < cropButton.length; i++) {
            cropButton[i] = getResources().getDrawable(R.drawable.post_question_crop_ic_indicator);
        }

        controlBtnSize = ConvertUtil.convertDpToPixel(20);  // Daniel (2016-06-22 16:26:13): set Control button size
        controlStrokeSize = ConvertUtil.convertDpToPixel(2);    // Daniel (2016-06-24 14:32:31): set Control stroke size

        setOnTouchListener(mTouchListener);
    }

    @Override
    public void setStretchMode(CropMode mode) {
        if (mode != null) {
            this.isCropMode = mode;

            invalidate();
        }
    }

    @Override
    public void setUtilMode(UtilMode mode) {
        if (mode != null) {
            this.isUtilMode = mode;

            if (isUtilMode == UtilMode.PENCIL) {
                setPenPaint(Color.BLUE, 5);
            } else {
                setEraserPaint(Color.WHITE, 10);
            }
        }
    }

    private float mX, mY;

    private void pathInitialize() {
        drawPath = new Path();
    }

    private void setPenPaint(int color, int width) {
        drawPaint = new Paint(Paint.DITHER_FLAG);  // smoothly
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true); // decrease color of Image when device isn't good.
        drawPaint.setColor(color);
        drawPaint.setStyle(Paint.Style.STROKE);  // border
        drawPaint.setStrokeJoin(Paint.Join.ROUND);  // the shape that end of line
        drawPaint.setStrokeCap(Paint.Cap.ROUND);  // the end of line's decoration
        drawPaint.setStrokeWidth(ConvertUtil.convertDpToPixel(width));  // line's width
        pathInitialize();
    }

    private void setEraserPaint(int color, int width) {
        drawPaint = new Paint(Paint.DITHER_FLAG);
        drawPaint.setColor(color);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);  // border
        drawPaint.setStrokeJoin(Paint.Join.ROUND);  // the shape that end of line
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeWidth(ConvertUtil.convertDpToPixel(width));
        pathInitialize();
    }

    private void drawActionDown(float x, float y) {
        drawPath = new Path();
        arrayDrawInfo.add(new DrawInfo(drawPath, drawPaint));
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;

        invalidate();
    }

    private void drawActionMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= 4 || dy >= 4) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);  // draw arc.
            mX = x;
            mY = y;
        }
//
        invalidate();
    }

    private void drawActionUp() {
        drawPath.lineTo(mX, mY);

        invalidate();

        if (arrayDrawInfo.size() > 0 && onUndoRedoListener != null) {
            onUndoRedoListener.onUndoAvailable(true);
        }
    }

    @Override
    public void setControlInImage(boolean result) {
        isControlBtnInImage = result;
    }

    @Override
    public void setCustomImageBitmap(final Bitmap bitmap) {
        initializeDrawSetting();

        setImageBitmap(null);
        setImageBitmap(bitmap);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mSuppMatrix.setRotate(0 % 360);
                        resizeImageToFitScreen(true);

                        isTouch = false;
                        invalidate();
                    }
                });
            } else {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSuppMatrix.setRotate(0 % 360);
                        resizeImageToFitScreen(true);

                        isTouch = false;
                        invalidate();
                    }
                }, 400);
            }

        } catch (Exception e) {
            isTouch = false;
        }
    }

    @Override
    public void setCustomImageBitmap(final Bitmap bitmap, final int degree) {
        initializeDrawSetting();

        setImageBitmap(null);
        setImageBitmap(bitmap);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mSuppMatrix.setRotate(degree % 360);
                        resizeImageToFitScreen(true);

                        isTouch = false;
                        invalidate();
                    }
                });
            } else {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSuppMatrix.setRotate(degree % 360);
                        resizeImageToFitScreen(true);

                        isTouch = false;
                        invalidate();
                    }
                }, 400);
            }

        } catch (Exception e) {
            isTouch = false;
        }
    }

    @Override
    public void setCustomImageFile(File file) {
        try {
            setCustomImageBitmap(BitmapUtil.getBitmap(getContext(), file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCustomImageFile(File file, int degree) {
        try {
            setCustomImageBitmap(BitmapUtil.getBitmap(getContext(), file), degree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize all draw settings
     */
    private void initializeDrawSetting() {
        try {
            arrayDrawInfo.clear();
            arrayUndoneDrawInfo.clear();
        } catch (Exception ignored){}
    }

    @Override
    public synchronized void setRotationTo(float degrees) {
        setPreviousScale();

        mSuppMatrix.setRotate(degrees % 360);
        resizeImageToFitScreen(true);

        setCurrentDegree(degrees, false);

        isTouch = false;
        invalidate();
    }

    @Override
    public synchronized void setRotationBy(float degrees) {
        setPreviousScale();

        mSuppMatrix.postRotate(degrees % 360);
        resizeImageToFitScreen(true);

        setCurrentDegree(degrees, true);

        isTouch = false;
        invalidate();
    }

    float mPreScale = 0.0f;     // Daniel (2016-06-29 14:35:07): This scale is for previous path state
    private void setPreviousScale(){
        mPreScale = mMinScale;
    }

    private void setCurrentDegree(float degree, boolean rotateBy) {
        // Daniel (2016-06-29 14:00:11): Rotate draw path
        for (DrawInfo v : arrayDrawInfo) {
            RectF rectF = getDisplayRect();

            Matrix mMatrix = new Matrix();
            mMatrix.postScale(getScale() / mPreScale, getScale() / mPreScale, rectF.centerX(), rectF.centerY());

            if (rotateBy)
                mMatrix.postRotate(degree, rectF.centerX(), rectF.centerY());
            else
                mMatrix.setRotate(degree, rectF.centerX(), rectF.centerY());

            v.getPath().transform(mMatrix);
        }

        // Daniel (2016-06-29 14:00:11): Rotate unDraw path
        for (DrawInfo v : arrayUndoneDrawInfo) {
            RectF rectF = getDisplayRect();

            Matrix mMatrix = new Matrix();
            mMatrix.postScale(getScale() / mPreScale, getScale() / mPreScale, rectF.centerX(), rectF.centerY());

            if (rotateBy)
                mMatrix.postRotate(degree, rectF.centerX(), rectF.centerY());
            else
                mMatrix.setRotate(degree, rectF.centerX(), rectF.centerY());

            v.getPath().transform(mMatrix);
        }
    }


    @Override
    public synchronized void setReverseUpsideDown() {

        mSuppMatrix.preScale(1, -1);
        checkAndDisplayMatrix();

        isTouch = false;
        invalidate();
    }

    @Override
    public synchronized void setReverseRightToLeft() {

        mSuppMatrix.preScale(-1, 1);
        checkAndDisplayMatrix();

        isTouch = false;
        invalidate();
    }

    @Override
    public synchronized void setUndo() {
        if (arrayDrawInfo.size() > 0){
            arrayUndoneDrawInfo.add(arrayDrawInfo.remove(arrayDrawInfo.size() - 1));

            invalidate();
        }

        if (arrayDrawInfo.size() > 0) {
            if (onUndoRedoListener != null)
                onUndoRedoListener.onUndoAvailable(true);
        }
        else {
            if (onUndoRedoListener != null)
                onUndoRedoListener.onUndoAvailable(false);
        }

        if (arrayUndoneDrawInfo.size() > 0) {
            if (onUndoRedoListener != null)
                onUndoRedoListener.onRedoAvailable(true);
        }
        else {
            if (onUndoRedoListener != null)
                onUndoRedoListener.onRedoAvailable(false);
        }
    }

    @Override
    public synchronized void setRedo() {
        if (arrayUndoneDrawInfo.size() > 0){
            arrayDrawInfo.add(arrayUndoneDrawInfo.remove(arrayUndoneDrawInfo.size() - 1));

            invalidate();
        }

        if (arrayDrawInfo.size() > 0) {
            if (onUndoRedoListener != null)
                onUndoRedoListener.onUndoAvailable(true);
        }
        else {
            if (onUndoRedoListener != null)
                onUndoRedoListener.onUndoAvailable(false);
        }

        if (arrayUndoneDrawInfo.size() > 0) {
            if (onUndoRedoListener != null)
                onUndoRedoListener.onRedoAvailable(true);
        }
        else {
            if (onUndoRedoListener != null)
                onUndoRedoListener.onRedoAvailable(false);
        }
    }

    @Override
    public void setUndoRedoListener(OnUndoRedoListener listener) {
        onUndoRedoListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) return;

        mDrawWidth = canvas.getWidth();
        mDrawHeight = canvas.getHeight();

        if (isCropMode == CropMode.NO_CROP) {

            for (DrawInfo v : arrayDrawInfo) {
                canvas.drawPath(v.getPath(), v.getPaint());
            }
        }
        else {
            canvas.save();
            if (!isTouch) {

                // Daniel (2016-06-22 16:29:33): Crop size should maintain the (ImageView / 2) size
                RectF f = getDisplayRect();
                if (f != null && f.width() != 0 && f.height() != 0) {
                    float width = (f.width() / 2);
                    float height = (f.height() / 2);

                    centerPoint.set((int) (width - (width / 2) + f.left), (int) (height - (height / 2) + f.top));
                    coordinatePoints[0].set((int) (width + (width / 2) + f.left), centerPoint.y);
                    coordinatePoints[1].set(coordinatePoints[0].x, (int) (height + (height / 2) + f.top));
                    coordinatePoints[2].set(centerPoint.x, coordinatePoints[1].y);
                    coordinatePoints[3].set(centerPoint.x, centerPoint.y);
                } else {
                    int width = canvas.getWidth() / 2;
                    int height = canvas.getHeight() / 2;

                    centerPoint.set(width - (width / 2), height - (height / 2));
                    coordinatePoints[0].set(width + (width / 2), centerPoint.y);
                    coordinatePoints[1].set(coordinatePoints[0].x, height + (height / 2));
                    coordinatePoints[2].set(centerPoint.x, coordinatePoints[1].y);
                    coordinatePoints[3].set(centerPoint.x, centerPoint.y);
                }
                path.reset();
                path.moveTo(centerPoint.x, centerPoint.y);
                for (Point p : coordinatePoints) {
                    path.lineTo(p.x, p.y);
                }

                // Daniel (2016-06-22 14:10:05): initialize current mCropRect
                mCropRect.set(getCropLeft(), getCropTop(), getCropRight(), getCropBottom());
            } else {
                path.reset();

                if (touchedButtonIndex == 3) {
                    // Daniel (2016-06-21 16:55:26): if touched button's index == 3, set!
                    // because centerPoint.x, centerPoint.y is standard of touched button's index == 3
                    centerPoint.x = coordinatePoints[3].x;
                    centerPoint.y = coordinatePoints[3].y;
                }

                path.moveTo(centerPoint.x, centerPoint.y);
                path.lineTo(coordinatePoints[0].x, coordinatePoints[0].y);

                path.lineTo(coordinatePoints[1].x, coordinatePoints[1].y);
                path.lineTo(coordinatePoints[2].x, coordinatePoints[2].y);
                path.lineTo(coordinatePoints[3].x, coordinatePoints[3].y);

                // Daniel (2016-06-22 14:10:05): initialize current mCropRect
                mCropRect.set(getCropLeft(), getCropTop(), getCropRight(), getCropBottom());
            }

            canvas.clipPath(path, Region.Op.DIFFERENCE);
            canvas.drawColor(getResources().getColor(R.color.bapul_color_image_cover));

            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(controlStrokeSize);
            canvas.drawPath(path, mPaint);

            canvas.restore();

            // Daniel (2016-06-21 16:34:28): draw control button
            for (int i = 0; i < coordinatePoints.length; i++) {
                cropButton[i].setBounds(coordinatePoints[i].x - controlBtnSize, coordinatePoints[i].y - controlBtnSize, coordinatePoints[i].x + controlBtnSize, coordinatePoints[i].y + controlBtnSize);
                cropButton[i].draw(canvas);
            }
        }
    }

    OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            isTouch = true;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isCropMode == CropMode.NO_CROP) {
                        float X = event.getX();
                        float Y = event.getY();

                        if (isControlBtnInImage) {
                            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
                            if (X <= controlBtnSize)
                                return false;
                            if (Y <= controlBtnSize)
                                return false;

                            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
                            if (X >= mDrawWidth - controlBtnSize)
                                return false;
                            if (Y >= mDrawHeight - controlBtnSize)
                                return false;

                            RectF displayRect = getDisplayRect();

                            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
                            if (displayRect != null) {
                                if (X >= displayRect.right - controlBtnSize)
                                    return false;
                                if (X <= displayRect.left + controlBtnSize)
                                    return false;
                                if (Y >= displayRect.bottom - controlBtnSize)
                                    return false;
                                if (Y <= displayRect.top + controlBtnSize)
                                    return false;
                            }
                        } else {
                            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
                            if (X <= controlStrokeSize)
                                return false;
                            if (Y <= controlStrokeSize)
                                return false;

                            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
                            if (X >= mDrawWidth - controlStrokeSize)
                                return false;
                            if (Y >= mDrawHeight - controlStrokeSize)
                                return false;

                            RectF displayRect = getDisplayRect();

                            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
                            if (displayRect != null) {
                                if (X >= displayRect.right - controlStrokeSize)
                                    return false;
                                if (X <= displayRect.left + controlStrokeSize)
                                    return false;
                                if (Y >= displayRect.bottom - controlStrokeSize)
                                    return false;
                                if (Y <= displayRect.top + controlStrokeSize)
                                    return false;
                            }
                        }
                        drawActionDown(X, Y);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:

                    if (isCropMode == CropMode.NO_CROP) {
                        float X = event.getX();
                        float Y = event.getY();

                        if (isControlBtnInImage) {
                            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
                            if (X <= controlBtnSize)
                                X = controlBtnSize;
                            if (Y <= controlBtnSize)
                                Y = controlBtnSize;

                            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
                            if (X >= mDrawWidth - controlBtnSize)
                                X = mDrawWidth - controlBtnSize;
                            if (Y >= mDrawHeight - controlBtnSize)
                                Y = mDrawHeight - controlBtnSize;

                            RectF displayRect = getDisplayRect();

                            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
                            if (displayRect != null) {
                                if (X >= displayRect.right - controlBtnSize)
                                    X = (displayRect.right - controlBtnSize);
                                if (X <= displayRect.left + controlBtnSize)
                                    X = (displayRect.left + controlBtnSize);
                                if (Y >= displayRect.bottom - controlBtnSize)
                                    Y = (displayRect.bottom - controlBtnSize);
                                if (Y <= displayRect.top + controlBtnSize)
                                    Y = (displayRect.top + controlBtnSize);
                            }
                        }
                        else {
                            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
                            if (X <= controlStrokeSize)
                                X = controlStrokeSize;
                            if (Y <= controlStrokeSize)
                                Y = controlStrokeSize;

                            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
                            if (X >= mDrawWidth - controlStrokeSize)
                                X = mDrawWidth - controlStrokeSize;
                            if (Y >= mDrawHeight - controlStrokeSize)
                                Y = mDrawHeight - controlStrokeSize;

                            RectF displayRect = getDisplayRect();

                            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
                            if (displayRect != null) {
                                if (X >= displayRect.right - controlStrokeSize)
                                    X = (displayRect.right - controlStrokeSize);
                                if (X <= displayRect.left + controlStrokeSize)
                                    X = (displayRect.left + controlStrokeSize);
                                if (Y >= displayRect.bottom - controlStrokeSize)
                                    Y = (displayRect.bottom - controlStrokeSize);
                                if (Y <= displayRect.top + controlStrokeSize)
                                    Y = (displayRect.top + controlStrokeSize);
                            }
                        }
                        drawActionMove(X, Y);
                    }
                    else {
                        int X = (int) event.getX();
                        int Y = (int) event.getY();

                        if (isControlBtnInImage) {

                            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
                            if (X <= controlBtnSize)
                                X = controlBtnSize;
                            if (Y <= controlBtnSize)
                                Y = controlBtnSize;

                            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
                            if (X >= mDrawWidth - controlBtnSize)
                                X = mDrawWidth - controlBtnSize;
                            if (Y >= mDrawHeight - controlBtnSize)
                                Y = mDrawHeight - controlBtnSize;

                            RectF displayRect = getDisplayRect();

                            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
                            if (displayRect != null) {
                                if (X >= displayRect.right - controlBtnSize)
                                    X = (int) (displayRect.right - controlBtnSize);
                                if (X <= displayRect.left + controlBtnSize)
                                    X = (int) (displayRect.left + controlBtnSize);
                                if (Y >= displayRect.bottom - controlBtnSize)
                                    Y = (int) (displayRect.bottom - controlBtnSize);
                                if (Y <= displayRect.top + controlBtnSize)
                                    Y = (int) (displayRect.top + controlBtnSize);
                            }

                            if (Math.sqrt(Math.pow(X - coordinatePoints[0].x, 2) + Math.pow(Y - coordinatePoints[0].y, 2)) <= controlBtnSize) {

                                coordinatePoints[0].x = X;
                                coordinatePoints[0].y = Y;

                                touchedButtonIndex = 0;

                                invalidate();
                            } else if (Math.sqrt(Math.pow(X - coordinatePoints[1].x, 2) + Math.pow(Y - coordinatePoints[1].y, 2)) <= controlBtnSize) {

                                coordinatePoints[1].x = X;
                                coordinatePoints[1].y = Y;

                                touchedButtonIndex = 1;

                                invalidate();
                            } else if (Math.sqrt(Math.pow(X - coordinatePoints[2].x, 2) + Math.pow(Y - coordinatePoints[2].y, 2)) <= controlBtnSize) {

                                coordinatePoints[2].x = X;
                                coordinatePoints[2].y = Y;

                                touchedButtonIndex = 2;

                                invalidate();
                            } else if (Math.sqrt(Math.pow(X - coordinatePoints[3].x, 2) + Math.pow(Y - coordinatePoints[3].y, 2)) <= controlBtnSize) {

                                coordinatePoints[3].x = X;
                                coordinatePoints[3].y = Y;

                                touchedButtonIndex = 3;

                                invalidate();
                            }
                        } else {
                            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
                            if (X <= controlStrokeSize)
                                X = controlStrokeSize;
                            if (Y <= controlStrokeSize)
                                Y = controlStrokeSize;

                            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
                            if (X >= mDrawWidth - controlStrokeSize)
                                X = mDrawWidth - controlStrokeSize;
                            if (Y >= mDrawHeight - controlStrokeSize)
                                Y = mDrawHeight - controlStrokeSize;

                            RectF displayRect = getDisplayRect();

                            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
                            if (displayRect != null) {
                                if (X >= displayRect.right - controlStrokeSize)
                                    X = (int) (displayRect.right - controlStrokeSize);
                                if (X <= displayRect.left + controlStrokeSize)
                                    X = (int) (displayRect.left + controlStrokeSize);
                                if (Y >= displayRect.bottom - controlStrokeSize)
                                    Y = (int) (displayRect.bottom - controlStrokeSize);
                                if (Y <= displayRect.top + controlStrokeSize)
                                    Y = (int) (displayRect.top + controlStrokeSize);
                            }

                            if (Math.sqrt(Math.pow(X - coordinatePoints[0].x, 2) + Math.pow(Y - coordinatePoints[0].y, 2)) <= controlBtnSize) {

                                coordinatePoints[0].x = X;
                                coordinatePoints[0].y = Y;

                                touchedButtonIndex = 0;

                                invalidate();
                            } else if (Math.sqrt(Math.pow(X - coordinatePoints[1].x, 2) + Math.pow(Y - coordinatePoints[1].y, 2)) <= controlBtnSize) {

                                coordinatePoints[1].x = X;
                                coordinatePoints[1].y = Y;

                                touchedButtonIndex = 1;

                                invalidate();
                            } else if (Math.sqrt(Math.pow(X - coordinatePoints[2].x, 2) + Math.pow(Y - coordinatePoints[2].y, 2)) <= controlBtnSize) {

                                coordinatePoints[2].x = X;
                                coordinatePoints[2].y = Y;

                                touchedButtonIndex = 2;

                                invalidate();
                            } else if (Math.sqrt(Math.pow(X - coordinatePoints[3].x, 2) + Math.pow(Y - coordinatePoints[3].y, 2)) <= controlBtnSize) {

                                coordinatePoints[3].x = X;
                                coordinatePoints[3].y = Y;

                                touchedButtonIndex = 3;

                                invalidate();
                            }
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isCropMode == CropMode.NO_CROP){
                        drawActionUp();
                    }
                    break;
            }
            return true;
        }
    };

    @Override
    public File getCropImage() {
        Bitmap originalBitmap = getOriginalBitmap();
        int oriWidth = 0;
        int oriHeight = 0;
        if (originalBitmap != null) {
            oriWidth = originalBitmap.getWidth();
            oriHeight = originalBitmap.getHeight();
        }

        if (oriWidth == 0 || oriHeight == 0) {
            oriWidth = mDrawWidth;
            oriHeight = mDrawHeight;
        }

        Bitmap matrixBitmap = Bitmap.createBitmap(getOriginalBitmap(), 0, 0, oriWidth, oriHeight, getDisplayMatrix(), true);
        Bitmap templateBitmap = null;

        templateBitmap = Bitmap.createBitmap(mDrawWidth, mDrawHeight, Bitmap.Config.ARGB_8888);
//
        Canvas canvas = new Canvas(templateBitmap);
        canvas.drawBitmap(matrixBitmap, ((canvas.getWidth() - matrixBitmap.getWidth()) / 2), ((canvas.getHeight() - matrixBitmap.getHeight()) / 2), null);

        if (isCropMode == CropMode.NO_CROP) {
            for (DrawInfo v : arrayDrawInfo) {
                canvas.drawPath(v.getPath(), v.getPaint());
            }

            RectF rectF = getDisplayRect();

            int width = (int) (rectF.right - rectF.left);
            int height = (int) (rectF.bottom - rectF.top);
            // Daniel (2016-06-29 11:58:18): Okay, To prevent IllegalArgumentException y + height must be <= bitmap.height() or x
            if (width > templateBitmap.getWidth())
                width = templateBitmap.getWidth();

            if (height > templateBitmap.getHeight())
                height = templateBitmap.getHeight();

            Bitmap cropImageBitmap = Bitmap.createBitmap(templateBitmap, (int) rectF.left, (int) rectF.top, width, height);

            // Daniel (2016-06-22 14:50:28): recycle previous image
            if (templateBitmap != null && templateBitmap != cropImageBitmap && !templateBitmap.isRecycled()) {
                templateBitmap.recycle();
                templateBitmap = null;
            }

            if (matrixBitmap != null && matrixBitmap != cropImageBitmap && !matrixBitmap.isRecycled()) {
                matrixBitmap.recycle();
                matrixBitmap = null;
            }
            return saveFile(cropImageBitmap);
        }
        else {
//        // Daniel (2016-06-23 23:37:23): is StretchMode?
            if (isCropMode == CropMode.CROP_STRETCH) {
                Bitmap extra = templateBitmap.copy(templateBitmap.getConfig(), true);

                float[] src = new float[]{
                        centerPoint.x, centerPoint.y,
                        coordinatePoints[0].x, coordinatePoints[0].y,
                        coordinatePoints[1].x, coordinatePoints[1].y,
                        coordinatePoints[2].x, coordinatePoints[2].y
                };

                float[] dsc = new float[]{
                        0, 0,
                        templateBitmap.getWidth(), 0,
                        templateBitmap.getWidth(), templateBitmap.getHeight(),
                        0, templateBitmap.getHeight()
                };

                Matrix matrix = new Matrix();
                boolean transformResult = matrix.setPolyToPoly(src, 0, dsc, 0, 4);

                canvas.drawBitmap(extra, matrix, null);

                // Daniel (2016-06-24 11:13:54): Okay, once you crop-stretch, you need to find perfect ratio width and height!
                double topWidth = Math.sqrt(Math.pow((centerPoint.x - coordinatePoints[0].x), 2) + Math.pow((centerPoint.y - coordinatePoints[0].y), 2));
                double bottomWidth = Math.sqrt(Math.pow((coordinatePoints[1].x - coordinatePoints[2].x), 2) + Math.pow((coordinatePoints[1].y - coordinatePoints[2].y), 2));

                double leftHeight = Math.sqrt(Math.pow((centerPoint.x - coordinatePoints[2].x), 2) + Math.pow((centerPoint.y - coordinatePoints[2].y), 2));
                double rightHeight = Math.sqrt(Math.pow((coordinatePoints[0].x - coordinatePoints[1].x), 2) + Math.pow((coordinatePoints[0].y - coordinatePoints[1].y), 2));

                int perfectWidth = (int) ((topWidth + bottomWidth) / 2);
                int perfectHeight = (int) ((leftHeight + rightHeight) / 2);

                // Daniel (2016-06-24 14:03:23): Improve cropped image quality
                Bitmap perfectBitmap = Bitmap.createScaledBitmap(templateBitmap, templateBitmap.getWidth(), perfectHeight * templateBitmap.getWidth() / perfectWidth, true);

                if (matrixBitmap != null && matrixBitmap != templateBitmap && !matrixBitmap.isRecycled()) {
                    matrixBitmap.recycle();
                    matrixBitmap = null;
                }

                if (templateBitmap != null && templateBitmap != perfectBitmap && !templateBitmap.isRecycled()) {
                    templateBitmap.recycle();
                    templateBitmap = null;
                }

                return saveFile(perfectBitmap);

            } else {

                path.reset();

                path.moveTo(centerPoint.x, centerPoint.y);
                path.lineTo(coordinatePoints[0].x, coordinatePoints[0].y);

                path.lineTo(coordinatePoints[1].x, coordinatePoints[1].y);
                path.lineTo(coordinatePoints[2].x, coordinatePoints[2].y);
                path.lineTo(coordinatePoints[3].x, coordinatePoints[3].y);

                canvas.clipPath(path, Region.Op.DIFFERENCE);
                canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);

                if (isCropMode == CropMode.CROP_SHRINK) {
                    Bitmap cropImageBitmap = Bitmap.createBitmap(templateBitmap, (int) mCropRect.left, (int) mCropRect.top, (int) (mCropRect.right - mCropRect.left), (int) (mCropRect.bottom - mCropRect.top));

                    // Daniel (2016-06-22 14:50:28): recycle previous image
                    if (templateBitmap != null && templateBitmap != cropImageBitmap && !templateBitmap.isRecycled()) {
                        templateBitmap.recycle();
                        templateBitmap = null;
                    }

                    if (matrixBitmap != null && matrixBitmap != cropImageBitmap && !matrixBitmap.isRecycled()) {
                        matrixBitmap.recycle();
                        matrixBitmap = null;
                    }
                    return saveFile(cropImageBitmap);
                } else {
                    if (matrixBitmap != null && matrixBitmap != templateBitmap && !matrixBitmap.isRecycled()) {
                        matrixBitmap.recycle();
                        matrixBitmap = null;
                    }

                    return saveFile(templateBitmap);
                }
            }
        }
    }

    private File saveFile(Bitmap bitmap) {
        OutputStream output = null;

        // Daniel (2016-06-24 11:52:55): if dstFile is invalid, we create our own file and return it to user!
        if (dstFile == null || !dstFile.exists() || !dstFile.isFile()) {
            final File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Bapul/");

            if (!filePath.exists()) {
                filePath.mkdirs();
            }

            dstFile = new File(filePath, "aaaaa.jpg");
            try {
                dstFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            output = new FileOutputStream(dstFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output);

            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();
        }

        return dstFile;
    }

    /**
     * among 4 coordinates the minimum X is Left
     * @return
     */
    private int getCropLeft() {
        int left = coordinatePoints[0].x;

        for (Point p : coordinatePoints) {
            if (p.x <= left)
                left = p.x;
        }

        return left;
    }

    /**
     * among 4 coordinates the minimum y is top
     * @return
     */
    private int getCropTop() {
        int top = coordinatePoints[0].y;

        for (Point p : coordinatePoints) {
            if (p.y <= top)
                top = p.y;
        }

        return top;
    }

    /**
     * among 4 coordinates the maximum x is right
     * @return
     */
    private int getCropRight() {
        int top = coordinatePoints[0].x;

        for (Point p : coordinatePoints) {
            if (p.x >= top)
                top = p.x;
        }

        return top;
    }

    /**
     * among 4 coordinates the maximum y is bottom
     * @return
     */
    private int getCropBottom() {
        int bottom = coordinatePoints[0].y;

        for (Point p : coordinatePoints) {
            if (p.y >= bottom)
                bottom = p.y;
        }

        return bottom;
    }

    // Daniel (2016-06-22 15:21:04): set Matrix

    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private final Matrix mSuppMatrix = new Matrix();
    private final RectF mDisplayRect = new RectF();

    private ScaleType mScaleType = ScaleType.FIT_CENTER;

    static final int EDGE_NONE = -1;
    static final int EDGE_LEFT = 0;
    static final int EDGE_RIGHT = 1;
    static final int EDGE_BOTH = 2;

    private int mScrollEdge = EDGE_BOTH;
    private final float[] mMatrixValues = new float[9];

    /**
     * get current displayed Matrix
     * @return
     */
    public Matrix getDisplayMatrix() {
        return new Matrix(getDrawMatrix());
    }

    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null)
            throw new IllegalArgumentException("Matrix cannot be null!");

        if (null == getDrawable())
            return false;

        mSuppMatrix.set(finalMatrix);
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();

        return true;
    }

    public Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    /**
     * calculate
     * @param d
     */
    private void updateBaseMatrix(Drawable d) {

        final float viewWidth = getImageViewWidth();
        final float viewHeight = getImageViewHeight();
        final int drawableWidth = d.getIntrinsicWidth();
        final int drawableHeight = d.getIntrinsicHeight();

        mBaseMatrix.reset();

        final float widthScale = viewWidth / drawableWidth;
        final float heightScale = viewHeight / drawableHeight;

        if (mScaleType == ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F,
                    (viewHeight - drawableHeight) / 2F);

        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);

        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);

        } else {
            RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
            RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);

            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix
                            .setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER);
                    break;

                case FIT_START:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.START);
                    break;

                case FIT_END:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.END);
                    break;

                case FIT_XY:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.FILL);
                    break;

                default:
                    break;
            }
        }

        resetMatrix();
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays it.s
     */
    private void resetMatrix() {
        mSuppMatrix.reset();
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
    }

    private void setImageViewMatrix(Matrix matrix) {
        checkImageViewScaleType();
        setImageMatrix(matrix);

//        // Call MatrixChangedListener if needed
//        if (mMatrixChangeListener != null) {
//            RectF displayRect = getDisplayRect(matrix);
//            if (displayRect != null) {
//                mMatrixChangeListener.onMatrixChanged(displayRect);
//            }
//        }
    }

    private void checkImageViewScaleType() {

        /**
         * PhotoView's getScaleType() will just divert to this.getScaleType() so
         * only call if we're not attached to a PhotoView.
         */
        if (!ScaleType.MATRIX.equals(getScaleType())) {
            throw new IllegalStateException(
                    "The ImageView's ScaleType has been changed since attaching a PhotoViewAttacher");
        }
    }

    private boolean checkMatrixBounds() {
        final RectF rect = getDisplayRect(getDrawMatrix());
        if (null == rect)
            return false;

        final float height = rect.height(), width = rect.width();
        float deltaX = 0, deltaY = 0;

        final int viewHeight = getImageViewHeight();
        if (height <= viewHeight) {
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top;
                    break;
                case FIT_END:
                    deltaY = viewHeight - height - rect.top;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;
                    break;
            }
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }

        final int viewWidth = getImageViewWidth();
        if (width <= viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;
                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
            mScrollEdge = EDGE_BOTH;
        } else if (rect.left > 0) {
            mScrollEdge = EDGE_LEFT;
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mScrollEdge = EDGE_RIGHT;
        } else {
            mScrollEdge = EDGE_NONE;
        }

        // Finally actually translate the matrix
        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     * @param matrix
     * @return
     */
    private RectF getDisplayRect(Matrix matrix) {
        Drawable d = getDrawable();

        if (d != null) {
            mDisplayRect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }

        return null;
    }

    /**
     * get ImageView's width
     * @return
     */
    private int getImageViewWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    /**
     * get ImageView's height
     * @return
     */
    private int getImageViewHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    /**
     * get original bitmap
     * @return
     */
    private Bitmap getOriginalBitmap() {
        if (getDrawable() != null) {
            return ((BitmapDrawable) getDrawable()).getBitmap();
        }
        return null;
    }
    /**
     * get current scale
     * @return
     */
    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow(getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     - Matrix to unpack
     * @param whichValue - Which value from Matrix.M* to return
     * @return float - returned value
     */
    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    /**
     * fit image to screen when parameter is true
     * @param type
     */
    private void resizeImageToFitScreen(boolean type){
        try {
            if (type) {

                RectF drawableRect = getDisplayRect();
                RectF viewRect = new RectF(0, 0, getImageViewWidth(), getImageViewHeight());

                getDisplayMatrix().setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);

                checkAndDisplayMatrix();

                int up = 0;
                int down = 0;
                boolean flag = false;
                while (!flag) {
                    RectF f = getDisplayRect();

                    if (getImageViewWidth() < f.width() || getImageViewHeight() < f.height()) {
                        if (Math.abs(f.width() - getImageViewWidth()) < 10
                                && Math.abs(f.height() - getImageViewHeight()) < 10) {
                            // okay;
                            flag = true;
                        } else {
                            mSuppMatrix.postScale(0.99f, 0.99f);
                            down += 1;
                        }
                    } else {
                        mSuppMatrix.postScale(1.01f, 1.01f);
                        up += 1;
                    }

                    if ((up + down) > 1000) {
                        flag = true;
                    }

                }
                checkAndDisplayMatrix();

                // Daniel (2016-01-13 19:51:08): to prevent from downscaling image below Screen size.
                float currentScale = getScale();
                setScaleLevels(currentScale, currentScale * 2, currentScale * 3);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * get current displayed image RectF
     * @return
     */
    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    /**
     * Helper method that simply checks the Matrix, and then displays the result
     */
    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        try {
            if(checkZoomLevels(minimumScale, mediumScale, maximumScale)) {
                mMinScale = minimumScale;
                mMidScale = mediumScale;
                mMaxScale = maximumScale;
            }
        }catch (IllegalArgumentException e) {}
    }

    private float mMinScale = 1.0f;
    private float mMidScale = 3.0f;
    private float mMaxScale = 6.0f;

    private static boolean checkZoomLevels(float minZoom, float midZoom,
                                           float maxZoom) throws IllegalArgumentException {
        if (minZoom >= midZoom) {
            throw new IllegalArgumentException(
                    "MinZoom has to be less than MidZoom");
        } else if (midZoom >= maxZoom) {
            throw new IllegalArgumentException(
                    "MidZoom has to be less than MaxZoom");
        }else{
            return true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        onUndoRedoListener = null;
        super.onDetachedFromWindow();
    }
}
