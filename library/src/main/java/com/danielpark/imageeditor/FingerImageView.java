package com.danielpark.imageeditor;

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

import com.danielpark.imagecropper.R;
import com.danielpark.imagecropper.util.ConvertUtil;

/**
 * This is embed image view in {@link EditorPanelView}
 * <br><br>
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public class FingerImageView extends ImageView {

    private final int DEP;  // 테두리 경계선의 유효폭
    // 이벤트 처리, 현재의 그리기 모드에 따른 점의 위치를 조정
    float _dx = 0, _dy = 0;
    float _oldx, _oldy;
    boolean isLeftTop, isLeftBottom, isRightTop, isRightBottom;
    boolean _bMove = false;
    private Context mContext;
    private Bitmap mBitmap;
    private Paint mPaint;
    private int mPaintReviseSize;  // 테두리 보정을 위한 size 계산 value
    //	private static final int DEP = 30;  // 테두리 경계선의 유효폭
    private int MINIMUM_RANGE;  // 이미지 최소 폭
    private float MINIMUM_WIDTH;  // 이미지 최소 Width
    private float MINIMUM_HEIGHT;  // 이미지 최소 height
    private float _sx = 0, _ex = 0, _sy = 0, _ey = 0;  // left x, right x, top y, bottom y
    private boolean _mode = false;
    private RectF mRect = new RectF();
    private boolean isRotation; //Yang (2015-02-09-21:34:14) : 이미지 회전 value
    private Drawable mResizeLeftTop;
    private Drawable mResizeLeftBottom;
    private Drawable mResizeRightTop;
    private Drawable mResizeRightBottom;
    private float mBitmapRatio; // 이미지 비율 value
    private float tempX;  // _sx or _ex 임시 저장 variable
    private float tempY;  // _sy or _ey 임시 저장 variable

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
        options.inPurgeable = true; // OOM 방지. http://stackoverflow.com/a/8527745/361100
        options.inInputShareable = true;
        //Bitmap photo = BitmapFactory.decodeFile(strPath, options);
        BitmapFactory.decodeFile(strImagePath, options);

        return options;
    }

    /**
     * 이미지 설정
     * @param bitmap
     */
    public boolean imageSet(Bitmap bitmap) {
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        float _screenWidth = display.getWidth();  // 화면 크기 설정
//		_screenHeight = display.getHeight();

        // 초기 테두리선 위치 설정
        // Rect Region : Consider image real size
        int nImageWidth = bitmap.getWidth();
        int nImageHeight = bitmap.getHeight();

        //Yang (2015-02-05-15:53:15) : 이미지 비율 구하기
        mBitmapRatio = (float) nImageWidth / nImageHeight;

        if (nImageWidth == 0 || nImageHeight == 0) {
            return false;
        }

        int nScreenWidth = (int) _screenWidth - ConvertUtil.convertDpToPixel(16);  // margin value 를 빼줌
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

        // 페인트 설정
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#3ba2e1"));
        mPaintReviseSize = ConvertUtil.convertDpToPixel(1);  //Yang (2015-02-06-12:20:31) : Image border Rect Line 보정을 위해
        mPaint.setStrokeWidth(ConvertUtil.convertDpToPixel(2));

        mBitmap = bitmap;
        invalidate();
        return true;
    }

    /**
     * Yang (2015-02-09-22:17:23) : 비율에 따라 최소폭 지정
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
     * 이미지 및 mRect 를 90 Rotate
     * @param bitmap
     */
    private void setRotation90Image(Bitmap bitmap) {
        mBitmap = bitmap;

        mBitmapRatio = 1 / mBitmapRatio;  //Yang (2015-02-09-22:20:13) : 회전하기때문에 비율을 바꿔준다.
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
            matrix.setRotate(90 , centerX, centerY); //Yang (2015-02-09-21:32:24) :  90도, 중심을 기준으로 회전시킨다.
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

        //Yang (2013-11-25 18:46:05) : image enable
        if (_mode) {
            // 사각형 라인 그리기
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

                // 어느 하나라도 선택이 되었다면 move에서 값 변경
                if ((isLeftTop || isLeftBottom || isRightTop || isRightBottom))
                    _bMove = false;
                else if (((touchX > _sx + DEP) && (touchX < _ex - DEP)) && ((touchY > _sy + DEP) && (touchY < _ey - DEP)))
                    _bMove = true;
                else {
//                    setManipulationMode(false);
                    return false;  //Yang (2014-12-18-15:31:25) : 이미지 바깥 Touch 이후 바로 FingerPen 사용 가능하도록
                }
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                //Yang (2015-02-05-14:43:58) : image fixed ratio 하기위해 수정
                _dx = _oldx - touchX;
                _dy = _oldy - touchY;

                if (isLeftTop) {
                    tempX = _sx;
                    tempY = _sy;

                    if ((_dx >= 0 && _dy >= 0) || (_dx < 0 && _dy < 0)) {  //Yang (2015-02-05-15:32:15) : 대각선 구별 (2, 4 분면)
                        _sx -= mBitmapRatio >= 1 ? _dx / mBitmapRatio : _dx * mBitmapRatio;
                        _sy -= _dx;
                    }

                    if (_sx >= _ex - MINIMUM_WIDTH || _sy >= _ey - MINIMUM_HEIGHT) {  // 최소 폭 지정
                        _sx = tempX;
                        _sy = tempY;
                        return true;
                    }
                }
                if (isLeftBottom) {
                    tempX = _sx;
                    tempY = _ey;

                    if ((_dx >= 0 && _dy < 0) || (_dx < 0 && _dy >= 0)) { // (1, 3 분면)
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

                // 움직인 거리 구해서 적용
                if (_bMove) {
                    _sx -= _dx;
                    _ex -= _dx;
                    _sy -= _dy;
                    _ey -= _dy;
                }

                invalidate(); // 움직일때 다시 그려줌
                _oldx = touchX;
                _oldy = touchY;
                return true;
            }

            // ACTION_UP 이면 그리기 종료
            if (event.getAction() == MotionEvent.ACTION_UP) {
                isLeftTop = isLeftBottom = isRightTop = isRightBottom = _bMove = false;
                return true;
            }
        }
        return false;
    }

}
