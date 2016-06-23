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
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-06-21.
 */
public class CropperImageView extends ImageView {

    private static String TAG = "OKAY";
    private Context mContext;
    Paint mPaint = new Paint();
    Path path = new Path();


    int controlBtnSize = 50; // Daniel (2016-06-21 16:40:26): Radius of Control button

    boolean isTouch = false;
    boolean startCanvasDraw = false;

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

    public CropperImageView(Context context) {
        this (context, null);
    }

    public CropperImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);

        this.mContext = context;

        for (int i = 0; i < coordinatePoints.length; i++) {
            coordinatePoints[i] = new Point();
        }

        for (int i = 0; i < cropButton.length; i++) {
            cropButton[i] = getResources().getDrawable(R.drawable.ic_image_edit_crop_a);
        }

        controlBtnSize = ConvertUtil.convertDpToPixel(20);  // Daniel (2016-06-22 16:26:13): set Control button size

        setOnTouchListener(mTouchListener);
    }

    /**
     * Set Image bitmap to CropImageView
     * @param bitmap
     */
    public void setCustomImageBitmap(final Bitmap bitmap) {
        startCanvasDraw = false;    // Daniel (2016-06-23 16:02:13): Stop drawing on canvas

        setImageBitmap(null);
        setImageBitmap(bitmap);

        try {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSuppMatrix.setRotate(0 % 360);
                    resizeImageToFitScreen(true);
                }
            }, 300);

            isTouch = false;
            startCanvasDraw = true;
            invalidate();
        } catch (Exception e) {
            isTouch = false;
            startCanvasDraw = true;
        }
    }

    /**
     * Set Image bitmap to CropImageView with degree
     * @param bitmap
     * @param degree
     */
    public void setCustomImageBitmap(final Bitmap bitmap, final int degree) {
        startCanvasDraw = false;    // Daniel (2016-06-23 16:02:13): Stop drawing on canvas

        setImageBitmap(null);
        setImageBitmap(bitmap);

        try {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSuppMatrix.setRotate(degree % 360);
                    resizeImageToFitScreen(true);
                }
            }, 300);

            isTouch = false;
            startCanvasDraw = true;
            invalidate();
        } catch (Exception e) {
            isTouch = false;
            startCanvasDraw = true;
        }
    }

    /**
     * Set degree
     * @param degrees
     */
    public synchronized void setRotationTo(float degrees) {
        startCanvasDraw = false;    // Daniel (2016-06-23 16:02:13): Stop drawing on canvas

        mSuppMatrix.setRotate(degrees % 360);
        resizeImageToFitScreen(true);

        isTouch = false;
        startCanvasDraw = true;
        invalidate();
    }

    /**
     * add degree
     * @param degrees
     */
    public synchronized void setRotationBy(float degrees) {
        startCanvasDraw = false;    // Daniel (2016-06-23 16:02:13): Stop drawing on canvas

        mSuppMatrix.postRotate(degrees % 360);
        resizeImageToFitScreen(true);

        isTouch = false;
        startCanvasDraw = true;
        invalidate();
    }

    /**
     * upside down
     */
    public synchronized void setReverseUpsideDown() {
        startCanvasDraw = false;    // Daniel (2016-06-23 16:02:13): Stop drawing on canvas

        mSuppMatrix.preScale(1, -1);
        checkAndDisplayMatrix();

        isTouch = false;
        startCanvasDraw = true;
        invalidate();
    }

    /**
     * change left to right and vice versa
     */
    public synchronized void setReverseRightToLeft() {
        startCanvasDraw = false;    // Daniel (2016-06-23 16:02:13): Stop drawing on canvas

        mSuppMatrix.preScale(-1, 1);
        checkAndDisplayMatrix();

        isTouch = false;
        startCanvasDraw = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) return;
        canvas.save();

        if (!startCanvasDraw) return;

        Log.d("OKAY", "onDraw()");

        if (!isTouch) {
//            Drawable drawable = getDrawable();
//you should call after the bitmap drawn
//            Rect bounds = drawable.getBounds();
//            int width = bounds.width();
//            int height = bounds.height();

//            int width = 0;
//            int height = 0;

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
            }
            else {
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

        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
        canvas.drawPath(path, mPaint);

        canvas.restore();

        // Daniel (2016-06-21 16:34:28): draw control button
        for (int i = 0; i < coordinatePoints.length; i++) {
            cropButton[i].setBounds(coordinatePoints[i].x - controlBtnSize, coordinatePoints[i].y - controlBtnSize, coordinatePoints[i].x + controlBtnSize, coordinatePoints[i].y + controlBtnSize);
            cropButton[i].draw(canvas);
        }

        mDrawWidth = canvas.getWidth();
        mDrawHeight = canvas.getHeight();
    }

    OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            isTouch = true;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
//                    Log.d(TAG, "X : " + event.getX() + " Y : " + event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    int X = (int) event.getX();
                    int Y = (int) event.getY();

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
                    return true;
                case MotionEvent.ACTION_UP:
//                    Log.d(TAG, "X : " + event.getX() + " Y : " + event.getY());

                    break;
            }
            return true;
        }
    };

    /**
     * Daniel (2016-06-21 17:25:44): Try to crop Image from original image
     */
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

        if (matrixBitmap.getWidth() > matrixBitmap.getHeight()) {
            // Daniel (2016-06-22 19:00:16): original image is landscape
            templateBitmap = Bitmap.createBitmap(matrixBitmap.getWidth(), mDrawHeight, Bitmap.Config.ARGB_8888);

        } else if (matrixBitmap.getWidth() < matrixBitmap.getHeight()) {
            // Daniel (2016-06-22 19:00:32): original image is portrait
            templateBitmap = Bitmap.createBitmap(mDrawWidth, matrixBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            // Daniel (2016-06-22 19:02:16): original image is square
            templateBitmap = Bitmap.createBitmap(matrixBitmap.getWidth(), matrixBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        }


        Log.d("OKAY", "oriWidth width : " + oriWidth);
        Log.e("OKAY", "oriHeight width : " + oriHeight);
        Log.d("OKAY", "templateBitmap width : " + templateBitmap.getWidth());
        Log.e("OKAY", "templateBitmap height : " + templateBitmap.getHeight());
        Log.d("OKAY", "matrixBitmap width : " + matrixBitmap.getWidth());
        Log.e("OKAY", "matrixBitmap height : " + matrixBitmap.getHeight());

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

        Bitmap cropImageBitmap = Bitmap.createBitmap(templateBitmap, (int) mCropRect.left, (int) mCropRect.top, (int) (mCropRect.right - mCropRect.left), (int) (mCropRect.bottom - mCropRect.top));

        // Daniel (2016-06-22 14:50:28): recycle previous image
        if (templateBitmap != null && templateBitmap != cropImageBitmap && !templateBitmap.isRecycled()) {
            templateBitmap.recycle();
            templateBitmap = null;
        }

        if (matrixBitmap != null && matrixBitmap != cropImageBitmap && !matrixBitmap.isRecycled()){
            matrixBitmap.recycle();
            matrixBitmap = null;
        }

        return saveFile(cropImageBitmap);
    }

    private File saveFile(Bitmap bitmap) {
        OutputStream output = null;
        final File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Bapul/");

        if (!filePath.exists()) {
            filePath.mkdirs();
        }

        File file = new File(filePath, "aaaaa.jpg");
        try {
            file.createNewFile();
            output = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output);

            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();
        }

        return file;
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
}
