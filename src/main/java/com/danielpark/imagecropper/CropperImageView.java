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

import com.danielpark.imagecropper.listener.OnThumbnailChangeListener;
import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;
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

    private RectF mCropRect = new RectF();    // Daniel (2016-06-22 14:08:55): Current cropped shape's rectangle scope

    private int mDrawWidth, mDrawHeight;    // Daniel (2016-06-22 14:26:01): Current visible ImageView's width, height

    private ShapeMode isShapeMode = ShapeMode.FREE;
    private CropMode isCropMode = CropMode.CROP_STRETCH;
    private UtilMode isUtilMode = UtilMode.NONE;
    private boolean isControlBtnInImage = false;    // Daniel (2016-06-24 14:33:53): whether control button should be inside of Image

    private Path drawPath;
    private Paint drawPaint;
    private ArrayList<DrawInfo> arrayDrawInfo = new ArrayList<>();
    private ArrayList<DrawInfo> arrayUndoneDrawInfo = new ArrayList<>();

    private OnUndoRedoStateChangeListener onUndoRedoStateChangeListener;
    private OnThumbnailChangeListener onThumbnailChangeListener;

    private File dstFile;   // Daniel (2016-06-24 11:47:43): if user set dstFile, Cropped Image will be set to this file!

	private int imageDegree = 0; // Daniel (2016-07-25 15:10:14): Get degree when image was set!

	private float insetRatio = 0.2f;	// Daniel (2016-08-31 14:07:18): margin between outside border of Bitmap and 4 Crop rectangle border

    public CropperImageView(Context context) {
        this (context, null);
    }

    public CropperImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Daniel (2016-07-15 18:09:16): below 4.0.4 there is issue with clip path java.lang.UnsupportedOperationException
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

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
    public void setShapeMode(ShapeMode mode) {
        if (mode != null) {
            this.isShapeMode = mode;

            invalidate();
        }
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

        if (arrayDrawInfo.size() > 0 && onUndoRedoStateChangeListener != null) {
            onUndoRedoStateChangeListener.onUndoAvailable(true);
        }
    }

    @Override
    public void setControlInImage(boolean result) {
        isControlBtnInImage = result;
    }

	@Override
	public void setCropInsetRatio(float percent) {
		if (percent < 10f || percent > 90f)
			return;

		insetRatio = percent / 200f;
	}

	@Override
    public void setCustomImageBitmap(final Bitmap bitmap) {
        setCustomImageBitmap(bitmap, 0);
    }

    @Override
    public void setCustomImageBitmap(final Bitmap bitmap, final int degree) {
		imageDegree = degree % 360;	// get degree parameter

        initializeDrawSetting();

        setImageBitmap(bitmap);

        try {
			post(new Runnable() {
				@Override
				public void run() {
					// 1. Update base Matrix to fit ImageView
					updateBaseMatrix(getDrawable());
					// 2. Rotate ImageView with degree
					mSuppMatrix.setRotate(degree % 360);
					checkAndDisplayMatrix();    // applied
					// 3. Resize Bitmap to fit ImageView Screen
					resizeImageToFitScreen();

					isTouch = false;
					invalidate();
				}
			});
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

		// Daniel (2016-07-27 19:12:21): update current image degree
		imageDegree = (int) (degrees % 360);

        mSuppMatrix.setRotate(degrees % 360);
		checkAndDisplayMatrix(); // applied

        resizeImageToFitScreen();

        setCurrentDegree(degrees, false);

        isTouch = false;
        invalidate();
    }

    @Override
    public synchronized void setRotationBy(float degrees) {
        setPreviousScale();

		// Daniel (2016-07-27 18:09:28): update current image degree
		imageDegree = (int) (imageDegree + degrees) % 360;

        mSuppMatrix.postRotate(degrees % 360);
		checkAndDisplayMatrix(); // applied

        resizeImageToFitScreen();

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
            if (onUndoRedoStateChangeListener != null)
                onUndoRedoStateChangeListener.onUndoAvailable(true);
        }
        else {
            if (onUndoRedoStateChangeListener != null)
                onUndoRedoStateChangeListener.onUndoAvailable(false);
        }

        if (arrayUndoneDrawInfo.size() > 0) {
            if (onUndoRedoStateChangeListener != null)
                onUndoRedoStateChangeListener.onRedoAvailable(true);
        }
        else {
            if (onUndoRedoStateChangeListener != null)
                onUndoRedoStateChangeListener.onRedoAvailable(false);
        }
    }

    @Override
    public synchronized void setRedo() {
        if (arrayUndoneDrawInfo.size() > 0){
            arrayDrawInfo.add(arrayUndoneDrawInfo.remove(arrayUndoneDrawInfo.size() - 1));

            invalidate();
        }

        if (arrayDrawInfo.size() > 0) {
            if (onUndoRedoStateChangeListener != null)
                onUndoRedoStateChangeListener.onUndoAvailable(true);
        }
        else {
            if (onUndoRedoStateChangeListener != null)
                onUndoRedoStateChangeListener.onUndoAvailable(false);
        }

        if (arrayUndoneDrawInfo.size() > 0) {
            if (onUndoRedoStateChangeListener != null)
                onUndoRedoStateChangeListener.onRedoAvailable(true);
        }
        else {
            if (onUndoRedoStateChangeListener != null)
                onUndoRedoStateChangeListener.onRedoAvailable(false);
        }
    }

    @Override
    public void setUndoRedoListener(OnUndoRedoStateChangeListener listener) {
        onUndoRedoStateChangeListener = listener;
    }

    @Override
    public void setThumbnailChangeListener(OnThumbnailChangeListener listener) {
        onThumbnailChangeListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) return;

        mDrawWidth = canvas.getWidth();
        mDrawHeight = canvas.getHeight();

        if (isCropMode == CropMode.NO_CROP && isUtilMode != UtilMode.NONE) {

            for (DrawInfo v : arrayDrawInfo) {
                canvas.drawPath(v.getPath(), v.getPaint());
            }
        }
        else if (isCropMode != CropMode.NO_CROP) {
            canvas.save();
            if (!isTouch) {

                // Daniel (2016-06-22 16:29:33): Crop size should maintain the (ImageView / 2) size
                RectF f = getDisplayRect();
                if (f != null && f.width() != 0 && f.height() != 0) {
					float width = f.width();
					float height = f.height();

                    float marginWidth = (f.width() * insetRatio);
                    float marginHeight = (f.height() * insetRatio);

                    centerPoint.set((int) (marginWidth + f.left), (int) (marginHeight + f.top));
                    coordinatePoints[0].set((int) (width - marginWidth + f.left), centerPoint.y);
                    coordinatePoints[1].set(coordinatePoints[0].x, (int) (height - marginHeight + f.top));
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

                // Daniel (2016-06-21 16:55:26): centerPoint.x, centerPoint.y is standard of coordinatePoints[3]
                centerPoint.x = coordinatePoints[3].x;
                centerPoint.y = coordinatePoints[3].y;

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
                    if (isCropMode == CropMode.NO_CROP && isUtilMode != UtilMode.NONE) {
                        float X = event.getX();
                        float Y = event.getY();

                        int borderSize;    // borderSize

                        if (isControlBtnInImage)
                            borderSize = controlBtnSize;
                        else
                            borderSize = controlStrokeSize;

                        // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
                        if (X <= borderSize)
                            return false;
                        if (Y <= borderSize)
                            return false;

                        // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
                        if (X >= mDrawWidth - borderSize)
                            return false;
                        if (Y >= mDrawHeight - borderSize)
                            return false;

                        RectF displayRect = getDisplayRect();

                        // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
                        if (displayRect != null) {
                            if (X >= displayRect.right - borderSize)
                                return false;
                            if (X <= displayRect.left + borderSize)
                                return false;
                            if (Y >= displayRect.bottom - borderSize)
                                return false;
                            if (Y <= displayRect.top + borderSize)
                                return false;
                        }
                        drawActionDown(X, Y);
                    } else if (isCropMode != CropMode.NO_CROP) {
                        float X = event.getX();
                        float Y = event.getY();
                        controlTouchInCropDown(X, Y);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:

                    if (isCropMode == CropMode.NO_CROP && isUtilMode != UtilMode.NONE) {
                        float X = event.getX();
                        float Y = event.getY();

                        int borderSize;    // borderSize

                        if (isControlBtnInImage)
                            borderSize = controlBtnSize;
                        else
                            borderSize = controlStrokeSize;

                        // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
                        if (X <= borderSize)
                            X = borderSize;
                        if (Y <= borderSize)
                            Y = borderSize;

                        // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
                        if (X >= mDrawWidth - borderSize)
                            X = mDrawWidth - borderSize;
                        if (Y >= mDrawHeight - borderSize)
                            Y = mDrawHeight - borderSize;

                        RectF displayRect = getDisplayRect();

                        // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
                        if (displayRect != null) {
                            if (X >= displayRect.right - borderSize)
                                X = (displayRect.right - borderSize);
                            if (X <= displayRect.left + borderSize)
                                X = (displayRect.left + borderSize);
                            if (Y >= displayRect.bottom - borderSize)
                                Y = (displayRect.bottom - borderSize);
                            if (Y <= displayRect.top + borderSize)
                                Y = (displayRect.top + borderSize);
                        }
                        drawActionMove(X, Y);
                    }
                    else if (isCropMode != CropMode.NO_CROP) {
                        for (int index = 0; index < event.getPointerCount(); index++) {
                            int X = (int) event.getX(index);
                            int Y = (int) event.getY(index);

                            controlTouchInCropMove(X, Y);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isCropMode == CropMode.NO_CROP && isUtilMode != UtilMode.NONE){
                        drawActionUp();
                    } else if (isCropMode != CropMode.NO_CROP) {
                        if (onThumbnailChangeListener != null)
                            onThumbnailChangeListener.onThumbnailChanged(getCropStretchThumbnailBitmap());
                    }
                    break;
            }
            return true;
        }
    };


    float cropDownX, cropDownY;
    /**
     * It only works in Crop Mode for touch event!!
     * @param X
     * @param Y
     */
    private void controlTouchInCropDown(float X, float Y) {
        cropDownX = -1; cropDownY = -1;

        if (isControlBtnInImage) {
            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
            if (X <= controlBtnSize)
                return;
            if (Y <= controlBtnSize)
                return;

            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
            if (X >= mDrawWidth - controlBtnSize)
                return;
            if (Y >= mDrawHeight - controlBtnSize)
                return;

            RectF displayRect = getDisplayRect();

            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
            if (displayRect != null) {
                if (X >= displayRect.right - controlBtnSize)
                    return;
                if (X <= displayRect.left + controlBtnSize)
                    return;
                if (Y >= displayRect.bottom - controlBtnSize)
                    return;
                if (Y <= displayRect.top + controlBtnSize)
                    return;
            }

            if (X >= mCropRect.right - controlBtnSize)
                return;
            if (X <= mCropRect.left + controlBtnSize)
                return;
            if (Y >= mCropRect.bottom - controlBtnSize)
                return;
            if (Y <= mCropRect.top + controlBtnSize)
                return;
        }
        else {
            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
            if (X <= controlStrokeSize)
                return;
            if (Y <= controlStrokeSize)
                return;

            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
            if (X >= mDrawWidth - controlStrokeSize)
                return;
            if (Y >= mDrawHeight - controlStrokeSize)
                return;

            RectF displayRect = getDisplayRect();

            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
            if (displayRect != null) {
                if (X >= displayRect.right - controlStrokeSize)
                    return;
                if (X <= displayRect.left + controlStrokeSize)
                    return;
                if (Y >= displayRect.bottom - controlStrokeSize)
                    return;
                if (Y <= displayRect.top + controlStrokeSize)
                    return;
            }

            if (X >= mCropRect.right - controlStrokeSize)
                return;
            if (X <= mCropRect.left + controlStrokeSize)
                return;
            if (Y >= mCropRect.bottom - controlStrokeSize)
                return;
            if (Y <= mCropRect.top + controlStrokeSize)
                return;
        }

        cropDownX = X;
        cropDownY = Y;
    }


    /**
     * It only works in Crop Mode for touch event!!
     * @param X
     * @param Y
     */
    private void controlTouchInCropMove(int X, int Y) {
        int borderSize;    // borderSize

        if (isControlBtnInImage)
            borderSize = controlBtnSize;
        else
            borderSize = controlStrokeSize;

        // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
        if (X <= borderSize)
            X = borderSize;
        if (Y <= borderSize)
            Y = borderSize;

        // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
        if (X >= mDrawWidth - borderSize)
            X = mDrawWidth - borderSize;
        if (Y >= mDrawHeight - borderSize)
            Y = mDrawHeight - borderSize;

        RectF displayRect = getDisplayRect();

        // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
        if (displayRect != null) {
            if (X >= displayRect.right - borderSize)
                X = (int) (displayRect.right - borderSize);
            if (X <= displayRect.left + borderSize)
                X = (int) (displayRect.left + borderSize);
            if (Y >= displayRect.bottom - borderSize)
                Y = (int) (displayRect.bottom - borderSize);
            if (Y <= displayRect.top + borderSize)
                Y = (int) (displayRect.top + borderSize);
        }

        if (Math.sqrt(Math.pow(X - coordinatePoints[0].x, 2) + Math.pow(Y - coordinatePoints[0].y, 2)) <= controlBtnSize) {

            if (isShapeMode == ShapeMode.FIXED) {
                // Rectangle position
                // moveX = the distance last point X - previous point X
                // moveY = the distance last point Y - previous point Y
                int moveX = X - coordinatePoints[0].x;
                int moveY = Y - coordinatePoints[0].y;

                // Daniel (2016-10-08 23:09:36): Each point should not interfere with other points
                int distanceWidth = Math.abs(X - coordinatePoints[3].x);
                int distanceHeight = Math.abs(Y - coordinatePoints[1].y);
                if (distanceWidth < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[0].x;
                    moveX = 0;
                }
                if (distanceHeight < controlBtnSize + controlStrokeSize) {
                    Y = coordinatePoints[0].y;
                    moveY = 0;
                }
                coordinatePoints[1].x += moveX;
                coordinatePoints[3].y += moveY;
            }
            else if (isShapeMode == ShapeMode.FREE) {
                // Daniel (2016-10-08 23:09:36): Each point should not interfere with other points
                int distanceWidth = Math.abs(X - coordinatePoints[3].x);
                int distanceHeight = Math.abs(Y - coordinatePoints[1].y);
                if (distanceWidth < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[0].x;
                }
                if (distanceHeight < controlBtnSize + controlStrokeSize) {
                    Y = coordinatePoints[0].y;
                }
                int distanceAcross = (int) Math.sqrt(Math.pow(X - coordinatePoints[2].x, 2) + Math.pow(Y - coordinatePoints[2].y, 2));
                if (distanceAcross < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[0].x;
                    Y = coordinatePoints[0].y;
                }
            }

            coordinatePoints[0].x = X;
            coordinatePoints[0].y = Y;

            invalidate();
        } else if (Math.sqrt(Math.pow(X - coordinatePoints[1].x, 2) + Math.pow(Y - coordinatePoints[1].y, 2)) <= controlBtnSize) {

            if (isShapeMode == ShapeMode.FIXED) {
                // Rectangle position
                // moveX = the distance last point X - previous point X
                // moveY = the distance last point Y - previous point Y
                int moveX = X - coordinatePoints[1].x;
                int moveY = Y - coordinatePoints[1].y;

                // Daniel (2016-10-08 23:09:36): Each point should not interfere with other points
                int distanceWidth = Math.abs(X - coordinatePoints[2].x);
                int distanceHeight = Math.abs(Y - coordinatePoints[0].y);
                if (distanceWidth < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[1].x;
                    moveX = 0;
                }
                if (distanceHeight < controlBtnSize + controlStrokeSize) {
                    Y = coordinatePoints[1].y;
                    moveY = 0;
                }
                coordinatePoints[0].x += moveX;
                coordinatePoints[2].y += moveY;
            }
            else if (isShapeMode == ShapeMode.FREE) {
                // Daniel (2016-10-08 23:09:36): Each point should not interfere with other points
                int distanceWidth = Math.abs(X - coordinatePoints[2].x);
                int distanceHeight = Math.abs(Y - coordinatePoints[0].y);
                if (distanceWidth < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[1].x;
                }
                if (distanceHeight < controlBtnSize + controlStrokeSize) {
                    Y = coordinatePoints[1].y;
                }
                int distanceAcross = (int) Math.sqrt(Math.pow(X - coordinatePoints[3].x, 2) + Math.pow(Y - coordinatePoints[3].y, 2));
                if (distanceAcross < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[1].x;
                    Y = coordinatePoints[1].y;
                }
            }

            coordinatePoints[1].x = X;
            coordinatePoints[1].y = Y;

            invalidate();
        } else if (Math.sqrt(Math.pow(X - coordinatePoints[2].x, 2) + Math.pow(Y - coordinatePoints[2].y, 2)) <= controlBtnSize) {

            if (isShapeMode == ShapeMode.FIXED) {
                // Rectangle position
                // moveX = the distance last point X - previous point X
                // moveY = the distance last point Y - previous point Y
                int moveX = X - coordinatePoints[2].x;
                int moveY = Y - coordinatePoints[2].y;

                // Daniel (2016-10-08 23:09:36): Each point should not interfere with other points
                int distanceWidth = Math.abs(X - coordinatePoints[1].x);
                int distanceHeight = Math.abs(Y - coordinatePoints[3].y);
                if (distanceWidth < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[2].x;
                    moveX = 0;
                }
                if (distanceHeight < controlBtnSize + controlStrokeSize) {
                    Y = coordinatePoints[2].y;
                    moveY = 0;
                }
                coordinatePoints[3].x += moveX;
                coordinatePoints[1].y += moveY;
            }
            else if (isShapeMode == ShapeMode.FREE) {
                // Daniel (2016-10-08 23:09:36): Each point should not interfere with other points
                int distanceWidth = Math.abs(X - coordinatePoints[1].x);
                int distanceHeight = Math.abs(Y - coordinatePoints[3].y);
                if (distanceWidth < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[2].x;
                }
                if (distanceHeight < controlBtnSize + controlStrokeSize) {
                    Y = coordinatePoints[2].y;
                }
                int distanceAcross = (int) Math.sqrt(Math.pow(X - coordinatePoints[0].x, 2) + Math.pow(Y - coordinatePoints[0].y, 2));
                if (distanceAcross < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[2].x;
                    Y = coordinatePoints[2].y;
                }
            }

            coordinatePoints[2].x = X;
            coordinatePoints[2].y = Y;

            invalidate();
        } else if (Math.sqrt(Math.pow(X - coordinatePoints[3].x, 2) + Math.pow(Y - coordinatePoints[3].y, 2)) <= controlBtnSize) {

            if (isShapeMode == ShapeMode.FIXED) {
                // Rectangle position
                // moveX = the distance last point X - previous point X
                // moveY = the distance last point Y - previous point Y
                int moveX = X - coordinatePoints[3].x;
                int moveY = Y - coordinatePoints[3].y;

                // Daniel (2016-10-08 23:09:36): Each point should not interfere with other points
                int distanceWidth = Math.abs(X - coordinatePoints[0].x);
                int distanceHeight = Math.abs(Y - coordinatePoints[2].y);
                if (distanceWidth < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[3].x;
                    moveX = 0;
                }
                if (distanceHeight < controlBtnSize + controlStrokeSize) {
                    Y = coordinatePoints[3].y;
                    moveY = 0;
                }
                coordinatePoints[2].x += moveX;
                coordinatePoints[0].y += moveY;
            }
            else if (isShapeMode == ShapeMode.FREE) {
                // Daniel (2016-10-08 23:09:36): Each point should not interfere with other points
                int distanceWidth = Math.abs(X - coordinatePoints[0].x);
                int distanceHeight = Math.abs(Y - coordinatePoints[2].y);
                if (distanceWidth < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[3].x;
                }
                if (distanceHeight < controlBtnSize + controlStrokeSize) {
                    Y = coordinatePoints[3].y;
                }
                int distanceAcross = (int) Math.sqrt(Math.pow(X - coordinatePoints[1].x, 2) + Math.pow(Y - coordinatePoints[1].y, 2));
                if (distanceAcross < controlBtnSize + controlStrokeSize) {
                    X = coordinatePoints[3].x;
                    Y = coordinatePoints[3].y;
                }
            }

            coordinatePoints[3].x = X;
            coordinatePoints[3].y = Y;

            invalidate();
        } else if (isTouchInCropRect(X, Y)) {
            invalidate();
        }
    }

    private boolean isTouchInCropRect(int X, int Y) {
        if (cropDownX < 0 || cropDownY < 0)
            return false;

        int xMove = (int) (X - cropDownX);
        int yMove = (int) (Y - cropDownY);

        boolean invalidX = false;
        boolean invalidY = false;

        // Daniel (2016-07-11 15:22:50): Check if point is valid!
        for (Point p : coordinatePoints) {
            int pX = p.x;
            int pY = p.y;

            switch (isValidPoint(pX + xMove, pY + yMove)){
                case 0:
                    invalidX = true;
                    break;
                case 1:
                    invalidY = true;
                    break;
                case 2:
                    invalidX = true;
                    invalidY = true;
                    break;
                case 3:
                    break;
            }

        }


        if (invalidX && invalidY)
            return false;

        for (Point p : coordinatePoints) {
            if (!invalidX)
                p.x = p.x + xMove;
            if (!invalidY)
                p.y = p.y + yMove;
        }

        cropDownX = X;
        cropDownY = Y;

        return true;
    }

    /**
     * Check if point is valid <br>
     * <code>0</code> X is invalid<br> <code>1</code> Y is invalid<br> <code>2</code> all invalid<br> <code>3</code> Nothing is invalid
     * @param X
     * @param Y
     * @return <code>0</code> X is invalid<br> <code>1</code> Y is invalid<br> <code>2</code> all invalid<br> <code>3</code> Nothing is invalid
     */
    private int isValidPoint(float X, float Y) {

        boolean xInvalid = false;
        boolean yInvalid = false;

        if (isControlBtnInImage) {
            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
            if (X <= controlBtnSize)
                xInvalid = true;
            if (Y <= controlBtnSize)
                yInvalid = true;

            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
            if (X >= mDrawWidth - controlBtnSize)
                xInvalid = true;
            if (Y >= mDrawHeight - controlBtnSize)
                yInvalid = true;

            RectF displayRect = getDisplayRect();

            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
            if (displayRect != null) {
                if (X >= displayRect.right - controlBtnSize)
                    xInvalid = true;
                if (X <= displayRect.left + controlBtnSize)
                    xInvalid = true;
                if (Y >= displayRect.bottom - controlBtnSize)
                    yInvalid = true;
                if (Y <= displayRect.top + controlBtnSize)
                    yInvalid = true;
            }
        }
        else {
            // Daniel (2016-06-21 19:03:45): touch event should not go outside of screen
            if (X <= controlStrokeSize)
                xInvalid = true;
            if (Y <= controlStrokeSize)
                yInvalid = true;

            // Daniel (2016-06-22 14:26:45): touch Event should not right or bottom outside of screen
            if (X >= mDrawWidth - controlStrokeSize)
                xInvalid = true;
            if (Y >= mDrawHeight - controlStrokeSize)
                yInvalid = true;

            RectF displayRect = getDisplayRect();

            // Daniel (2016-06-22 16:19:05): touch event should not go outside of visible image
            if (displayRect != null) {
                if (X >= displayRect.right - controlStrokeSize)
                    xInvalid = true;
                if (X <= displayRect.left + controlStrokeSize)
                    xInvalid = true;
                if (Y >= displayRect.bottom - controlStrokeSize)
                    yInvalid = true;
                if (Y <= displayRect.top + controlStrokeSize)
                    yInvalid = true;
            }
        }

        if (xInvalid && yInvalid)
            return 2;
        else if (xInvalid)
            return 0;
        else if (yInvalid)
            return 1;
        else
            return 3;
    }

	/**
	 * {@link CropMode#CROP_STRETCH} mode
	 * @return
	 */
	private File getCropStretch() {
		Bitmap originalBitmap = getOriginalBitmap();
		int oriWidth = 0;
		int oriHeight = 0;
		if (originalBitmap != null) {
			oriWidth = originalBitmap.getWidth();
			oriHeight = originalBitmap.getHeight();
        } else {
            // Daniel (2016-09-03 23:49:15): If Original Image is null then just leave it!
            return null;
        }

		if (oriWidth == 0 || oriHeight == 0) {
			oriWidth = mDrawWidth;
			oriHeight = mDrawHeight;
		}

//        Log.d("OKAY2", "oriWidth : " + oriWidth);
//        Log.d("OKAY2", "oriHeight : " + oriHeight);

		RectF displayRect = getDisplayRect();

		float X_Factor = (float) oriWidth / displayRect.width();
		float Y_Factor = (float) oriHeight / displayRect.height();

		// if there is rotation issue than you must recalculate Factor
		if (imageDegree % 360 == 90 || imageDegree % 360 == 270) {
			X_Factor = (float) oriHeight / displayRect.width();
			Y_Factor = (float) oriWidth / displayRect.height();
		}

		Matrix mMatrix = new Matrix();
		mMatrix.setRotate(imageDegree, oriWidth * 0.5f, oriHeight * 0.5f);

		Bitmap matrixBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, oriWidth, oriHeight, mMatrix, true);

		float widthGap = Math.abs(mDrawWidth - displayRect.width());
		float heightGap = Math.abs(mDrawHeight - displayRect.height());

		float removeX = displayRect.left;
		float removeY = displayRect.top;

		if (widthGap > heightGap)
			removeY = 0;
		else
			removeX = 0;

		float[] src = new float[]{
				(centerPoint.x - removeX) * X_Factor, (centerPoint.y - removeY) * Y_Factor,
				(coordinatePoints[0].x - removeX) * X_Factor, (coordinatePoints[0].y - removeY) * Y_Factor,
				(coordinatePoints[1].x - removeX) * X_Factor, (coordinatePoints[1].y - removeY) * Y_Factor,
				(coordinatePoints[2].x - removeX) * X_Factor, (coordinatePoints[2].y - removeY) * Y_Factor
		};

		// Daniel (2016-07-01 18:21:54): Find perfect ratio of IMAGE
		double L1 = Math.sqrt(Math.pow(src[0] - src[2], 2) + Math.pow(src[1] - src[3], 2));
		double L2 = Math.sqrt(Math.pow(src[6] - src[4], 2) + Math.pow(src[7] - src[5], 2));

		double M1 = Math.sqrt(Math.pow(src[0] - src[6], 2) + Math.pow(src[1] - src[7], 2));
		double M2 = Math.sqrt(Math.pow(src[2] - src[4], 2) + Math.pow(src[3] - src[5], 2));

		double h = (M1 + M2) / 2;
		double w = (L1 + L2) / 2;

		double diff = Math.abs(L1 - L2) / 2;

		float X2 = src[0];
		float Y2 = src[1];
		float X1 = src[4];
		float Y1 = src[5];
		float CX = src[6];
		float CY = src[7];

		double leftTop = Math.atan((Y2 - CY) / (X2 - CX));
		double leftBottom = Math.atan((Y1 - CY) / (X1 - CX));

		double radian = leftTop - leftBottom;

//                double angle = Math.abs(radian * 180 / Math.PI);
//                Log.d("OKAY2", "angle : " + angle);

		double factor = Math.abs(90 / (radian * 180 / Math.PI));
		double diffFactor = (1 + diff * 1.5 / w);

//                Log.d("OKAY2", "factor : " + factor);
//                Log.d("OKAY2", "diffFactor : " + diffFactor);
//                Log.d("OKAY2", " f / 2 : " +  ((factor + diffFactor) / 2));

		h = h * ((factor + diffFactor) / 2);

		// Daniel (2016-08-06 18:18:14): If h is bigger than original image, then it should be fixed
		// It might happened to be higher than original picture...
        // Daniel (2016-08-08 17:11:30): consider Image's degree!
        if (imageDegree % 360 == 90 || imageDegree % 360 == 270) {
            double wRatio = oriHeight / w;
            double hRatio = oriWidth / h;

            if (h > oriWidth) {
                w = w * hRatio;
                h = oriWidth;    // h = h * (oriWidth / h);
            } else if (w > oriHeight) {
                w = oriHeight;  // w = w * (oriHeight / w);
                h = h * wRatio;
            }
        } else {
            double wRatio = oriWidth / w;
            double hRatio = oriHeight / h;

            if (h > oriHeight) {
                w = w * hRatio;
                h = oriHeight;    // h = h * (oriHeight / h);
            } else if (w > oriWidth) {
                w = oriWidth;      // w = w * (oriWidth / w);
                h = h * wRatio;
            }
        }

		float[] dsc = new float[]{
				0, 0,
				(float) w, 0,
				(float) w, (float) h,
				0, (float) h
		};

		Bitmap perfectBitmap = Bitmap.createBitmap((int) w, (int) h, Bitmap.Config.ARGB_8888);

		Matrix matrix = new Matrix();
		matrix.setPolyToPoly(src, 0, dsc, 0, 4);

		Canvas canvas = new Canvas(perfectBitmap);
		canvas.drawBitmap(matrixBitmap, matrix, null);

		if (originalBitmap != matrixBitmap && matrixBitmap != perfectBitmap && matrixBitmap != null && !matrixBitmap.isRecycled()) {
			matrixBitmap.recycle();
			matrixBitmap = null;
		}

//        Log.d("OKAY2", "bitmap width : " + perfectBitmap.getWidth());
//        Log.d("OKAY2", "bitmap height : " + perfectBitmap.getHeight());

		if (originalBitmap != perfectBitmap) {
			return saveFile(perfectBitmap, true);
		} else {
			return saveFile(perfectBitmap, false);
		}
	}

	private File getNoCrop() {
		Bitmap originalBitmap = getOriginalBitmap();
		int oriWidth = 0;
		int oriHeight = 0;
		if (originalBitmap != null) {
			oriWidth = originalBitmap.getWidth();
			oriHeight = originalBitmap.getHeight();
        } else {
            // Daniel (2016-09-03 23:49:15): If Original Image is null then just leave it!
            return null;
        }

		if (oriWidth == 0 || oriHeight == 0) {
			oriWidth = mDrawWidth;
			oriHeight = mDrawHeight;
		}

		// Daniel (2016-07-27 18:54:07): check if there are any draw & eraser
		if (arrayDrawInfo != null && arrayDrawInfo.size() > 0) {

			Bitmap matrixBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, oriWidth, oriHeight, getDisplayMatrix(), true);
			Bitmap templateBitmap = Bitmap.createBitmap(mDrawWidth, mDrawHeight, Bitmap.Config.ARGB_8888);
//
			Canvas canvas = new Canvas(templateBitmap);
			canvas.drawBitmap(matrixBitmap, ((canvas.getWidth() - matrixBitmap.getWidth()) / 2), ((canvas.getHeight() - matrixBitmap.getHeight()) / 2), null);

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
			if (originalBitmap != templateBitmap && templateBitmap != null && templateBitmap != cropImageBitmap && !templateBitmap.isRecycled()) {
				templateBitmap.recycle();
				templateBitmap = null;
			}

			if (originalBitmap != matrixBitmap && matrixBitmap != null && matrixBitmap != cropImageBitmap && !matrixBitmap.isRecycled()) {
				matrixBitmap.recycle();
				matrixBitmap = null;
			}

			if (originalBitmap != cropImageBitmap)
				return saveFile(cropImageBitmap, true);
			else
				return saveFile(cropImageBitmap, false);

		} else {
			// No arrayDraw stuff...
			Matrix mMatrix = new Matrix();
			mMatrix.setRotate(imageDegree, oriWidth * 0.5f, oriHeight * 0.5f);

			Bitmap matrixBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, oriWidth, oriHeight, mMatrix, true);

			if (originalBitmap != matrixBitmap)
				return saveFile(matrixBitmap, true);
			else
				return saveFile(matrixBitmap, false);
		}
	}

	private File getCropElse() {
		Bitmap originalBitmap = getOriginalBitmap();
		int oriWidth = 0;
		int oriHeight = 0;
		if (originalBitmap != null) {
			oriWidth = originalBitmap.getWidth();
			oriHeight = originalBitmap.getHeight();
        } else {
            // Daniel (2016-09-03 23:49:15): If Original Image is null then just leave it!
            return null;
        }

		if (oriWidth == 0 || oriHeight == 0) {
			oriWidth = mDrawWidth;
			oriHeight = mDrawHeight;
		}

		Bitmap matrixBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, oriWidth, oriHeight, getDisplayMatrix(), true);
		Bitmap templateBitmap = Bitmap.createBitmap(mDrawWidth, mDrawHeight, Bitmap.Config.ARGB_8888);
//
		Canvas canvas = new Canvas(templateBitmap);
		canvas.drawBitmap(matrixBitmap, ((canvas.getWidth() - matrixBitmap.getWidth()) / 2), ((canvas.getHeight() - matrixBitmap.getHeight()) / 2), null);

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
			if (originalBitmap != templateBitmap && templateBitmap != null && templateBitmap != cropImageBitmap && !templateBitmap.isRecycled()) {
				templateBitmap.recycle();
				templateBitmap = null;
			}

			if (originalBitmap != matrixBitmap && matrixBitmap != null && matrixBitmap != cropImageBitmap && !matrixBitmap.isRecycled()) {
				matrixBitmap.recycle();
				matrixBitmap = null;
			}

			if (originalBitmap != cropImageBitmap)
				return saveFile(cropImageBitmap, true);
			else
				return saveFile(cropImageBitmap, false);
		} else {
			if (originalBitmap != matrixBitmap && matrixBitmap != null && matrixBitmap != templateBitmap && !matrixBitmap.isRecycled()) {
				matrixBitmap.recycle();
				matrixBitmap = null;
			}

			if (originalBitmap != templateBitmap)
				return saveFile(templateBitmap, true);
			else
				return saveFile(templateBitmap, false);
		}
	}

    /**
     * it returns crop-stretch thumbnail bitmap <br>
     *     current size is 80dp
     */
    private Bitmap getCropStretchThumbnailBitmap() {
        Bitmap originalBitmap = getOriginalBitmap();
        int oriWidth = 0;
        int oriHeight = 0;
        if (originalBitmap != null) {
            oriWidth = originalBitmap.getWidth();
            oriHeight = originalBitmap.getHeight();
        } else {
            // Daniel (2016-09-03 23:49:15): If Original Image is null then just leave it!
            return null;
        }

        if (oriWidth == 0 || oriHeight == 0) {
            oriWidth = mDrawWidth;
            oriHeight = mDrawHeight;
        }

        RectF displayRect = getDisplayRect();

        float X_Factor = (float) oriWidth / displayRect.width();
        float Y_Factor = (float) oriHeight / displayRect.height();

        // if there is rotation issue than you must recalculate Factor
        if (imageDegree % 360 == 90 || imageDegree % 360 == 270) {
            X_Factor = (float) oriHeight / displayRect.width();
            Y_Factor = (float) oriWidth / displayRect.height();
        }

        Matrix mMatrix = new Matrix();
        mMatrix.setRotate(imageDegree, oriWidth * 0.5f, oriHeight * 0.5f);

        Bitmap matrixBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, oriWidth, oriHeight, mMatrix, true);

        float widthGap = Math.abs(mDrawWidth - displayRect.width());
        float heightGap = Math.abs(mDrawHeight - displayRect.height());

        float removeX = displayRect.left;
        float removeY = displayRect.top;

        if (widthGap > heightGap)
            removeY = 0;
        else
            removeX = 0;

        float[] src = new float[]{
                (centerPoint.x - removeX) * X_Factor, (centerPoint.y - removeY) * Y_Factor,
                (coordinatePoints[0].x - removeX) * X_Factor, (coordinatePoints[0].y - removeY) * Y_Factor,
                (coordinatePoints[1].x - removeX) * X_Factor, (coordinatePoints[1].y - removeY) * Y_Factor,
                (coordinatePoints[2].x - removeX) * X_Factor, (coordinatePoints[2].y - removeY) * Y_Factor
        };

        // Daniel (2016-07-01 18:21:54): Find perfect ratio of IMAGE
        double L1 = Math.sqrt(Math.pow(src[0] - src[2], 2) + Math.pow(src[1] - src[3], 2));
        double L2 = Math.sqrt(Math.pow(src[6] - src[4], 2) + Math.pow(src[7] - src[5], 2));

        double M1 = Math.sqrt(Math.pow(src[0] - src[6], 2) + Math.pow(src[1] - src[7], 2));
        double M2 = Math.sqrt(Math.pow(src[2] - src[4], 2) + Math.pow(src[3] - src[5], 2));

        double h = (M1 + M2) / 2;
        double w = (L1 + L2) / 2;

        double diff = Math.abs(L1 - L2) / 2;

        float X2 = src[0];
        float Y2 = src[1];
        float X1 = src[4];
        float Y1 = src[5];
        float CX = src[6];
        float CY = src[7];

        double leftTop = Math.atan((Y2 - CY) / (X2 - CX));
        double leftBottom = Math.atan((Y1 - CY) / (X1 - CX));

        double radian = leftTop - leftBottom;

//                double angle = Math.abs(radian * 180 / Math.PI);
//                Log.d("OKAY2", "angle : " + angle);

        double factor = Math.abs(90 / (radian * 180 / Math.PI));
        double diffFactor = (1 + diff * 1.5 / w);

//                Log.d("OKAY2", "factor : " + factor);
//                Log.d("OKAY2", "diffFactor : " + diffFactor);
//                Log.d("OKAY2", " f / 2 : " +  ((factor + diffFactor) / 2));

        h = h * ((factor + diffFactor) / 2);

        // Daniel (2016-08-06 18:18:14): If h is bigger than specified size, then it should be fixed
        // It might happened to be higher than specified size...
        // or vice versa
        int customSize = ConvertUtil.convertDpToPixel(80);

        double wRatio = customSize / w;
        double hRatio = customSize / h;

        if (w > customSize && h > customSize) {
            if (wRatio > hRatio) {
                w = w * hRatio;
                h = customSize; // h = h * (customSize / h);
            }
            else {
                w = customSize;
                h = h * wRatio;
            }
        } else if (h > customSize) {
            w = w * hRatio;
            h = customSize;	// h = h * (oriHeight / h);
        } else if (w > customSize) {
            w = customSize;
            h = h * wRatio;
        }

        float[] dsc = new float[]{
                0, 0,
                (float) w, 0,
                (float) w, (float) h,
                0, (float) h
        };

        Bitmap perfectBitmap = Bitmap.createBitmap((int) w, (int) h, Bitmap.Config.ARGB_8888);

        Matrix matrix = new Matrix();
        matrix.setPolyToPoly(src, 0, dsc, 0, 4);

        Canvas canvas = new Canvas(perfectBitmap);
        canvas.drawBitmap(matrixBitmap, matrix, null);

        if (originalBitmap != matrixBitmap && matrixBitmap != perfectBitmap && matrixBitmap != null && !matrixBitmap.isRecycled()) {
            matrixBitmap.recycle();
            matrixBitmap = null;
        }

        return perfectBitmap;
    }

    @Override
    public File getCropImage() {

		switch (isCropMode) {
			case CROP_STRETCH:
				return getCropStretch();
			case NO_CROP:
				return getNoCrop();
			default:
				return getCropElse();
		}
    }

    @Override
    public Bitmap getCropImageThumbnail() {

        switch (isCropMode) {
            case CROP_STRETCH:
                return getCropStretchThumbnailBitmap();
        }
        return null;
    }

    @Override
	public void onRecycleBitmap() {
		try {
			getOriginalBitmap().recycle();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private File saveFile(Bitmap bitmap, boolean shouldRecycle) {
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

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

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
	 */
    private void resizeImageToFitScreen(){
        try {
			// 3. Adjust Image to fit ImageView
			final float viewWidth = getImageViewWidth();
			final float viewHeight = getImageViewHeight();
			RectF displayRect = getDisplayRect();
			final int drawableWidth = (int) displayRect.width();
			final int drawableHeight = (int) displayRect.height();

//			Log.d("OKAY2", "viewWidth : " + viewWidth);
//			Log.d("OKAY2", "viewHeight : " + viewHeight);
//
//			Log.d("OKAY2", "drawableWidth : " + drawableWidth);
//			Log.d("OKAY2", "drawableHeight : " + drawableHeight);

			final float widthScale = viewWidth / drawableWidth;
			final float heightScale = viewHeight / drawableHeight;

			final float scale = Math.min(widthScale, heightScale);

//					RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
//					RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);

//                        mSuppMatrix.postScale(widthScale, heightScale, displayRect.centerX(), displayRect.centerY());
			mSuppMatrix.postScale(scale, scale, displayRect.centerX(), displayRect.centerY());
			checkAndDisplayMatrix();    // applied

			// Daniel (2016-01-13 19:51:08): to prevent from downscaling image below Screen size.
			float currentScale = getScale();
			setScaleLevels(currentScale, currentScale * 2, currentScale * 3);
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
        onUndoRedoStateChangeListener = null;
        onThumbnailChangeListener = null;
        super.onDetachedFromWindow();
    }
}