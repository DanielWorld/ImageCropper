/*
 * Copyright (c) 2017 DanielWorld.
 * @Author Namgyu Park
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.danielworld.imageeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import com.danielworld.imagecropper.R;
import com.danielworld.imagecropper.util.ConvertUtil;

/**
 * This is embed image view in {@link EditorPanelView}
 * <br><br>
 * Created by Namgyu Park on 2017-07-26.
 */

@SuppressLint("AppCompatCustomView")
public class FingerImageView extends ImageView {

    private final int DEP;  // Border outline's valid field
    float _dx = 0, _dy = 0;
    float _oldx, _oldy;
    boolean isLeftTop, isLeftBottom, isRightTop, isRightBottom;
    boolean _bMove = false;
    private Context mContext;
    private Bitmap mBitmap;
    private Paint mPaint;
    private int mPaintReviseSize;

    private int MINIMUM_RANGE;  // Image's minimum range
    private float MINIMUM_WIDTH;  // Image's minimum Width
    private float MINIMUM_HEIGHT;  // Image's minimum height
    private float _sx = 0, _ex = 0, _sy = 0, _ey = 0;  // left x, right x, top y, bottom y
    private boolean _mode = false;
    private RectF mRect = new RectF();
    private boolean isRotation; //Yang (2015-02-09-21:34:14) : Image rotation value
    private Drawable mResizeLeftTop;
    private Drawable mResizeLeftBottom;
    private Drawable mResizeRightTop;
    private Drawable mResizeRightBottom;
    private float mBitmapRatio; // Image ratio value
    private float tempX;  // _sx or _ex temp variable
    private float tempY;  // _sy or _ey temp variable

    public FingerImageView(Context context) {
        this(context, null);
    }


    public FingerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        DEP = ConvertUtil.convertDpToPixel(16);
    }

    public FingerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        DEP = ConvertUtil.convertDpToPixel(16);
    }

    public static BitmapFactory.Options getBitmapSize(String strImagePath) {
        //==========================================
        // Loaded the temporary bitmap for getting size.
        //==========================================
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true; // avoid OOM. http://stackoverflow.com/a/8527745/361100
        options.inInputShareable = true;
        //Bitmap photo = BitmapFactory.decodeFile(strPath, options);
        BitmapFactory.decodeFile(strImagePath, options);

        return options;
    }

    /**
     * Set image
     * @param bitmap
     */
    public boolean imageSet(Bitmap bitmap) {
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        float _screenWidth = display.getWidth();  // set screen size
//		_screenHeight = display.getHeight();

        // set first outline stroke position
        // Rect Region : Consider image real size
        int nImageWidth = bitmap.getWidth();
        int nImageHeight = bitmap.getHeight();

        // calculate image ratio
        mBitmapRatio = (float) nImageWidth / nImageHeight;

        if (nImageWidth == 0 || nImageHeight == 0) {
            return false;
        }

        int nScreenWidth = (int) _screenWidth - ConvertUtil.convertDpToPixel(16);  // remove margin value
        int nBoxRadius = nScreenWidth / 3;
        int nCenterX = nScreenWidth / 2;

        if (mBitmapRatio >= 1) {  // Width >= Height
            _sx = 0;
            _sy = 0;
            _ex = nScreenWidth;
            _ey = nImageHeight * (_screenWidth / nImageWidth);
        } else {
            _sx = nCenterX - (nBoxRadius);
            _sy = 0;
            _ex = nCenterX + (nBoxRadius);
            _ey = 2 * nBoxRadius / mBitmapRatio;
        }

        android.content.res.Resources resources = mContext.getResources();

        mResizeLeftTop = resources.getDrawable(R.drawable.img_resize_left_top);
        mResizeLeftBottom = resources.getDrawable(R.drawable.img_resize_left_bottom);
        mResizeRightTop = resources.getDrawable(R.drawable.img_resize_right_top);
        mResizeRightBottom = resources.getDrawable(R.drawable.img_resize_right_bottom);

        MINIMUM_RANGE = 2 * (mResizeLeftTop.getIntrinsicHeight() + DEP);

        setMinimumRange(mBitmapRatio);

        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#3ba2e1"));
        mPaintReviseSize = ConvertUtil.convertDpToPixel(1);  // To adjust Image border Rect Line
        mPaint.setStrokeWidth(ConvertUtil.convertDpToPixel(2));

        mBitmap = bitmap;
        invalidate();
        return true;
    }

    /**
     * set minimum ratio according to ratio
     * @param ratio
     */
    private void setMinimumRange(float ratio) {
        if (ratio >= 1) { // Width >= height
            MINIMUM_WIDTH = MINIMUM_RANGE / ratio;
            MINIMUM_HEIGHT =  MINIMUM_RANGE;
        } else {
            MINIMUM_WIDTH = MINIMUM_RANGE;
            MINIMUM_HEIGHT = MINIMUM_RANGE * ratio;
        }
    }

    /**
     * Rotate by 90 degree from Image and Rect
     * @param bitmap
     */
    private void setRotation90Image(Bitmap bitmap) {
        mBitmap = bitmap;

        mBitmapRatio = 1 / mBitmapRatio;  // Change ratio because of rotation
        setMinimumRange(mBitmapRatio);
        isRotation = true;
        invalidate();
    }

    /**
     * Rotate current image by 90 degree
     */
    public void setRotation90Image() {
        if (mBitmap == null || mBitmap.isRecycled()) return;

        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        setRotation90Image(Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true));
    }

    public void onDraw( Canvas canvas) {

        mRect.set(_sx, _sy, _ex, _ey);

        if (isRotation) {
            float centerX = mRect.centerX();
            float centerY = mRect.centerY();

            Matrix matrix = new Matrix();
            matrix.setRotate(90 , centerX, centerY); // Rotate by 90 degree using center position
            matrix.mapRect(mRect);

            _sx = mRect.left;
            _sy = mRect.top;
            _ex = mRect.right;
            _ey = mRect.bottom;

            isRotation = false;
        }

        if (mBitmap != null) {
            Rect src = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
//			canvas.drawBitmap(mBitmap, src, rect, new Paint());
            canvas.drawBitmap(mBitmap, src, mRect, null);
        }

        // image enable
        if (_mode) {
            // draw Rectangle line
            canvas.drawLine(_sx + mPaintReviseSize, _sy, _sx + mPaintReviseSize, _ey, mPaint);  // left line
            canvas.drawLine(_sx, _sy + mPaintReviseSize, _ex, _sy + mPaintReviseSize, mPaint);  // top line
            canvas.drawLine(_ex - mPaintReviseSize, _sy, _ex - mPaintReviseSize, _ey, mPaint);  // right line
            canvas.drawLine(_sx, _ey - mPaintReviseSize, _ex, _ey - mPaintReviseSize, mPaint);  // bottom line

            int left = (int) mRect.left;
            int right = (int) mRect.right;
            int top = (int) mRect.top;
            int bottom = (int) mRect.bottom;

            int mResizeImageWidth = mResizeLeftTop.getIntrinsicWidth();
            int mResizeImageHeight = mResizeLeftTop.getIntrinsicHeight();

            mResizeLeftTop.setBounds(left, top, left + mResizeImageWidth, top + mResizeImageHeight);
            mResizeLeftTop.draw(canvas);

            mResizeRightTop.setBounds(right - mResizeImageWidth, top, right, top + mResizeImageHeight);
            mResizeRightTop.draw(canvas);

            mResizeLeftBottom.setBounds(left, bottom - mResizeImageHeight, left + mResizeImageWidth, bottom);
            mResizeLeftBottom.draw(canvas);

            mResizeRightBottom.setBounds(right - mResizeImageWidth, bottom - mResizeImageHeight, right, bottom);
            mResizeRightBottom.draw(canvas);

        }

    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setManipulationMode(boolean mode) {
        this._mode = mode;
        invalidate();
    }

    public boolean isManipulationMode() {
        return this._mode;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        if (_mode) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                _oldx = touchX;
                _oldy = touchY;

                if (mResizeLeftTop.getBounds().contains(touchX, touchY)) {
                    isLeftTop = true;
                }
                if (mResizeLeftBottom.getBounds().contains(touchX, touchY)) {
                    isLeftBottom = true;
                }
                if (mResizeRightTop.getBounds().contains(touchX, touchY)) {
                    isRightTop = true;
                }
                if (mResizeRightBottom.getBounds().contains(touchX, touchY)) {
                    isRightBottom = true;
                }

                if ((isLeftTop || isLeftBottom || isRightTop || isRightBottom))
                    _bMove = false;
                else if (((touchX > _sx + DEP) && (touchX < _ex - DEP)) && ((touchY > _sy + DEP) && (touchY < _ey - DEP)))
                    _bMove = true;
                else {
//                    setManipulationMode(false);
                    return false;
                }
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                // Modify to set fixed image ratio
                _dx = _oldx - touchX;
                _dy = _oldy - touchY;

                if (isLeftTop) {
                    tempX = _sx;
                    tempY = _sy;

                    if ((_dx >= 0 && _dy >= 0) || (_dx < 0 && _dy < 0)) {  // (2, 4 )
                        _sx -= mBitmapRatio >= 1 ? _dx / mBitmapRatio : _dx * mBitmapRatio;
                        _sy -= _dx;
                    }

                    if (_sx >= _ex - MINIMUM_WIDTH || _sy >= _ey - MINIMUM_HEIGHT) {  // set minimum field
                        _sx = tempX;
                        _sy = tempY;
                        return true;
                    }
                }
                if (isLeftBottom) {
                    tempX = _sx;
                    tempY = _ey;

                    if ((_dx >= 0 && _dy < 0) || (_dx < 0 && _dy >= 0)) { // (1, 3)
                        _sx -= mBitmapRatio >= 1 ? _dx / mBitmapRatio : _dx * mBitmapRatio;
                        _ey += _dx;
                    }
                    if (_sx >= _ex - MINIMUM_WIDTH || _ey <= _sy + MINIMUM_HEIGHT) {
                        _sx = tempX;
                        _ey = tempY;
                        return true;
                    }
                }
                if (isRightTop) {
                    tempX = _ex;
                    tempY = _sy;

                    if ((_dx >= 0 && _dy < 0) || (_dx < 0 && _dy >= 0)) {
                        _ex -= mBitmapRatio >= 1 ? _dx / mBitmapRatio : _dx * mBitmapRatio;
                        _sy += _dx;
                    }

                    if (_ex <= _sx + MINIMUM_WIDTH || _sy >= _ey - MINIMUM_HEIGHT) {
                        _ex = tempX;
                        _sy = tempY;
                        return true;
                    }
                }
                if (isRightBottom) {
                    tempX = _ex;
                    tempY = _ey;

                    if ((_dx >= 0 && _dy >= 0) || (_dx < 0 && _dy < 0)) {
                        _ex -= mBitmapRatio >= 1 ? _dx / mBitmapRatio : _dx * mBitmapRatio;
                        _ey -= _dx;
                    }

                    if (_ex <= _sx + MINIMUM_WIDTH || _ey <= _sy + MINIMUM_HEIGHT) {
                        _ex = tempX;
                        _ey = tempY;
                        return true;
                    }
                }

                // calculate moved distance
                if (_bMove) {
                    _sx -= _dx;
                    _ex -= _dx;
                    _sy -= _dy;
                    _ey -= _dy;
                }

                invalidate(); // redraw it when moving
                _oldx = touchX;
                _oldy = touchY;
                return true;
            }

            // end drawing
            if (event.getAction() == MotionEvent.ACTION_UP) {
                isLeftTop = isLeftBottom = isRightTop = isRightBottom = _bMove = false;
                return true;
            }
        }
        return false;
    }

}
