package com.danielpark.imageeditor.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * https://code.google.com/p/color-picker-view/
 * <br><br>
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-27.
 */

public class ColorPickerView extends View {

    public interface OnColorChangedListener {
        void onColorChanged(int newColor);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        _colorChangedListener = listener;
    }

    private static float mDensity = 1f;
    private final static float BORDER_WIDTH_PX = 1;
    private float HUE_PANEL_WIDTH = 30f;
    private float ALPHA_PANEL_HEIGHT = 20f;
    private float PANEL_SPACING = 10f;
    private float PALETTE_CIRCLE_TRACKER_RADIUS = 5f;
    private float RECTANGLE_TRACKER_OFFSET = 2f;
    private OnColorChangedListener _colorChangedListener;
    private Paint mSatValPaint;  // 사각 gradient paint
    private Paint mSatValTrackerPaint;  // 사각 위치표시 paint
    private Paint mHuePaint;  // 막대 gradient paint
    private Paint mHueAlphaTrackerPaint;  // 막대 위치표시 paint
    private Paint mBorderPaint; // 테두리 paint
    private Shader mValShader;
    private Shader mSatShader;
    private Shader mHueShader;
    private BitmapCache mSatValBackgroundCache;
    private int mAlpha = 0xff;
    private float mHue = 29f;  // mDrawingRect y 위치값
    private float mSat = 1.0f;  // mSatValRect circle x 위치값
    private float mVal = 1.0f;  // mSatValRect circle y 위치값
    private int mSliderTrackerColor = 0xFFBDBDBD;
    private int mBorderColor = 0xFF6E6E6E;
    private boolean mShowAlphaPanel = false;
    private int mDrawingOffset;
    private RectF mDrawingRect;  // mHueRect 위치 표시 rect
    private RectF mSatValRect;
    private RectF mHueRect;
    private Point mStartTouchPoint = null;

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        PALETTE_CIRCLE_TRACKER_RADIUS *= mDensity;
        RECTANGLE_TRACKER_OFFSET *= mDensity;
        HUE_PANEL_WIDTH *= mDensity;
        ALPHA_PANEL_HEIGHT *= mDensity;
        PANEL_SPACING = PANEL_SPACING * mDensity;
        mDrawingOffset = calculateRequiredOffset();
        initPaintTools();
        // Needed for receiving trackball motion events.
        setFocusable(true);
        setFocusableInTouchMode(true);

    }

    private void initPaintTools() {
        mSatValPaint = new Paint();
        mSatValTrackerPaint = new Paint();
        mHuePaint = new Paint();
        mHueAlphaTrackerPaint = new Paint();
        mBorderPaint = new Paint();
        mBorderPaint.setColor(mBorderColor);
        mSatValTrackerPaint.setStyle(Paint.Style.STROKE);
        mSatValTrackerPaint.setStrokeWidth(2f * mDensity);
        mSatValTrackerPaint.setAntiAlias(true);
        mHueAlphaTrackerPaint.setColor(mSliderTrackerColor);
        mHueAlphaTrackerPaint.setStyle(Paint.Style.STROKE);
        mHueAlphaTrackerPaint.setStrokeWidth(2f * mDensity);
        mHueAlphaTrackerPaint.setAntiAlias(true);
    }

    private int calculateRequiredOffset() {
        float offset = Math.max(PALETTE_CIRCLE_TRACKER_RADIUS, RECTANGLE_TRACKER_OFFSET);
        offset = Math.max(offset, BORDER_WIDTH_PX * mDensity);
        return (int) (offset * 1.5f);
    }

    private int[] buildHueColorArray() {
        int[] hue = new int[361];
        int count = 0;
        for (int i = hue.length - 1; i >= 0; i--, count++) {
            hue[count] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }
        return hue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawingRect.width() <= 0 || mDrawingRect.height() <= 0) {
            return;
        }

        drawSatValPanel(canvas); // 사각
        drawHuePanel(canvas);   // 막대
        Log.d("mHue value ", "  :   " + mHue);
    }

    private void drawSatValPanel(Canvas canvas) {
        final RectF rect = mSatValRect;

        canvas.drawRect(mDrawingRect.left, mDrawingRect.top, rect.right + BORDER_WIDTH_PX, rect.bottom + BORDER_WIDTH_PX, mBorderPaint);

        if (mValShader == null) {
            mValShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, 0xffffffff, 0xff000000, Shader.TileMode.CLAMP);
        }

        if (mSatValBackgroundCache == null || mSatValBackgroundCache.value != mHue) {
            if (mSatValBackgroundCache == null) {
                mSatValBackgroundCache = new BitmapCache();
            }

            if (mSatValBackgroundCache.bitmap == null) {
                mSatValBackgroundCache.bitmap = Bitmap.createBitmap((int) rect.width(), (int) rect.height(), Bitmap.Config.ARGB_8888);
            }

            if (mSatValBackgroundCache.canvas == null) {
                mSatValBackgroundCache.canvas = new Canvas(mSatValBackgroundCache.bitmap);
            }

            int rgb = Color.HSVToColor(new float[]{mHue, 1f, 1f});
            mSatShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, 0xffffffff, rgb, Shader.TileMode.CLAMP);
            ComposeShader mShader = new ComposeShader(mValShader, mSatShader, PorterDuff.Mode.MULTIPLY);
            mSatValPaint.setShader(mShader);
            mSatValBackgroundCache.canvas.drawRect(0, 0, mSatValBackgroundCache.bitmap.getWidth(), mSatValBackgroundCache.bitmap.getHeight(), mSatValPaint);
            mSatValBackgroundCache.value = mHue;
        }

        canvas.drawBitmap(mSatValBackgroundCache.bitmap, null, rect, null);

        Point p = satValToPoint(mSat, mVal);
        mSatValTrackerPaint.setColor(0xff000000);
        canvas.drawCircle(p.x, p.y, PALETTE_CIRCLE_TRACKER_RADIUS - 1f * mDensity, mSatValTrackerPaint);
        mSatValTrackerPaint.setColor(0xffededed);
        canvas.drawCircle(p.x, p.y, PALETTE_CIRCLE_TRACKER_RADIUS - 1.5f, mSatValTrackerPaint);
    }

    private Point satValToPoint(float sat, float val) {
        final RectF rect = mSatValRect;
        final float height = rect.height();
        final float width = rect.width();
        Point p = new Point();
        p.x = (int) (sat * width + rect.left);
        p.y = (int) ((1f - val) * height + rect.top);
        return p;
    }

    private void drawHuePanel(Canvas canvas) {
        final RectF rect = mHueRect;

        mBorderPaint.setColor(mBorderColor);
        canvas.drawRect(rect.left - BORDER_WIDTH_PX, rect.top - BORDER_WIDTH_PX, rect.right + BORDER_WIDTH_PX, rect.bottom + BORDER_WIDTH_PX,
                mBorderPaint);

        if (mHueShader == null) {
            // The hue shader has either not yet been created or the view has
            // been resized.
            mHueShader = new LinearGradient(0, 0, 0, rect.height(), buildHueColorArray(), null, Shader.TileMode.CLAMP);
            mHuePaint.setShader(mHueShader);
        }

        canvas.drawRect(rect, mHuePaint);
        float rectHeight = 4 * mDensity / 2;
        Point p = hueToPoint(mHue);

        RectF r = new RectF();
        r.left = rect.left - RECTANGLE_TRACKER_OFFSET;
        r.right = rect.right + RECTANGLE_TRACKER_OFFSET;
        r.top = p.y - rectHeight;
        r.bottom = p.y + rectHeight;

        canvas.drawRoundRect(r, 2, 2, mHueAlphaTrackerPaint);

    }

    private Point hueToPoint(float hue) {
        final RectF rect = mHueRect;
        final float height = rect.height();
        Point p = new Point();
        p.y = (int) (height - (hue * height / 360f) + rect.top);
        p.x = (int) rect.left;
        return p;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean update = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartTouchPoint = new Point((int) event.getX(), (int) event.getY());
                update = moveTrackersIfNeeded(event);
                break;
            case MotionEvent.ACTION_MOVE:
                update = moveTrackersIfNeeded(event);
                break;
            case MotionEvent.ACTION_UP:
                mStartTouchPoint = null;
                update = moveTrackersIfNeeded(event);
                break;
        }

        if (update) {
            if (_colorChangedListener != null) {
                _colorChangedListener.onColorChanged(Color.HSVToColor(mAlpha, new float[]{mHue, mSat, mVal}));
            }
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean moveTrackersIfNeeded(MotionEvent event) {
        if (mStartTouchPoint == null) {
            return false;
        }

        boolean update = false;
        int startX = mStartTouchPoint.x;
        int startY = mStartTouchPoint.y;

        if (mHueRect.contains(startX, startY)) {
            mHue = pointToHue(event.getY());
            update = true;
        } else if (mSatValRect.contains(startX, startY)) {
            float[] result = pointToSatVal(event.getX(), event.getY());
            mSat = result[0];
            mVal = result[1];
            update = true;
        }

        return update;
    }

    private float[] pointToSatVal(float x, float y) {
        final RectF rect = mSatValRect;
        float[] result = new float[2];
        float width = rect.width();
        float height = rect.height();

        if (x < rect.left) {
            x = 0f;
        } else if (x > rect.right) {
            x = width;
        } else {
            x = x - rect.left;
        }

        if (y < rect.top) {
            y = 0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y = y - rect.top;
        }

        result[0] = 1.f / width * x;
        result[1] = 1.f - (1.f / height * y);

        return result;
    }

    private float pointToHue(float y) {
        final RectF rect = mHueRect;
        float height = rect.height();

        if (y < rect.top) {
            y = 0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y = y - rect.top;
        }

        return 360f - (y * 360f / height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int finalWidth = 0;
        int finalHeight = 0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthAllowed = MeasureSpec.getSize(widthMeasureSpec);
        int heightAllowed = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY) {
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                int h = (int) (widthAllowed - PANEL_SPACING - HUE_PANEL_WIDTH);

                if (mShowAlphaPanel) {
                    h += PANEL_SPACING + ALPHA_PANEL_HEIGHT;
                }
                if (h > heightAllowed) {
                    finalHeight = heightAllowed;
                } else {
                    finalHeight = h;
                }
                finalWidth = widthAllowed;

            } else if (widthMode != MeasureSpec.EXACTLY) {
                int w = (int) (heightAllowed + PANEL_SPACING + HUE_PANEL_WIDTH);

                if (mShowAlphaPanel) {
                    w -= PANEL_SPACING - ALPHA_PANEL_HEIGHT;
                }
                if (w > widthAllowed) {
                    finalWidth = widthAllowed;
                } else {
                    finalWidth = w;
                }
                finalHeight = heightAllowed;

            } else {
                finalWidth = widthAllowed;
                finalHeight = heightAllowed;
            }

        } else {
            int widthNeeded = (int) (heightAllowed + PANEL_SPACING + HUE_PANEL_WIDTH);
            int heightNeeded = (int) (widthAllowed - PANEL_SPACING - HUE_PANEL_WIDTH);

            if (mShowAlphaPanel) {
                widthNeeded -= (PANEL_SPACING + ALPHA_PANEL_HEIGHT);
                heightNeeded += PANEL_SPACING + ALPHA_PANEL_HEIGHT;
            }

            if (widthNeeded <= widthAllowed) {
                finalWidth = widthNeeded;
                finalHeight = heightAllowed;
            } else if (heightNeeded <= heightAllowed) {
                finalHeight = heightNeeded;
                finalWidth = widthAllowed;
            }
        }

        setMeasuredDimension(finalWidth, finalHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawingRect = new RectF();

        mDrawingRect.left = mDrawingOffset + getPaddingLeft();
        mDrawingRect.right = w - mDrawingOffset - getPaddingRight();
        mDrawingRect.top = mDrawingOffset + getPaddingTop();
        mDrawingRect.bottom = h - mDrawingOffset - getPaddingBottom();
        // The need to be recreated because they depend on the size of the view.

        mValShader = null;
        mSatShader = null;
        mHueShader = null;

        setUpSatValRect();
        setUpHueRect();
    }

    private void setUpSatValRect() {
        // Calculate the size for the big color rectangle.
        final RectF dRect = mDrawingRect;
        float left = dRect.left + BORDER_WIDTH_PX;
        float top = dRect.top + BORDER_WIDTH_PX;
        float right = dRect.right - BORDER_WIDTH_PX - PANEL_SPACING - HUE_PANEL_WIDTH;
        float bottom = dRect.bottom - BORDER_WIDTH_PX;

        if (mShowAlphaPanel) {
            bottom -= (ALPHA_PANEL_HEIGHT + PANEL_SPACING);
        }

        mSatValRect = new RectF(left, top, right, bottom);
    }

    private void setUpHueRect() {
        // Calculate the size for the hue slider on the left.
        final RectF dRect = mDrawingRect;
        float left = dRect.right - HUE_PANEL_WIDTH + BORDER_WIDTH_PX;
        float top = dRect.top + BORDER_WIDTH_PX;
        float right = dRect.right - BORDER_WIDTH_PX;
        float bottom = dRect.bottom - BORDER_WIDTH_PX - (mShowAlphaPanel ? (PANEL_SPACING + ALPHA_PANEL_HEIGHT) : 0);
        mHueRect = new RectF(left, top, right, bottom);
    }

    public int getColor() {
        return Color.HSVToColor(mAlpha, new float[]{mHue, mSat, mVal});
    }

    public void setColor(int color) {
        setColor(color, false);
    }

    public void setColor(int color, boolean callback) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);
        float[] hsv = new float[3];

        Color.RGBToHSV(red, green, blue, hsv);
        mAlpha = alpha;
        mHue = hsv[0];
        mSat = hsv[1];
        mVal = hsv[2];

        if (callback && _colorChangedListener != null) {
            _colorChangedListener.onColorChanged(Color.HSVToColor(mAlpha, new float[]{
                    mHue, mSat, mVal}));
        }

        invalidate();
    }

    private class BitmapCache {
        public Canvas canvas;
        public Bitmap bitmap;
        public float value;
    }

}
