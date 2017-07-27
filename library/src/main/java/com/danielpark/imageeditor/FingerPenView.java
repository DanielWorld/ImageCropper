package com.danielpark.imageeditor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;
import com.danielpark.imagecropper.model.DrawInfo;
import com.danielpark.imagecropper.util.ConvertUtil;

import java.util.ArrayList;

/**
 * This is for pen / eraser view
 * <br><br>
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public class FingerPenView extends ImageView implements FingerPenInterface{

    private EditorMode mPenMode = EditorMode.NONE;      // Editor mode

    private float mX, mY;

    private int penColor = Color.BLUE, penWidth = 5, eraserColor = Color.WHITE, eraserWidth = 10;

    private Paint drawPaint;
    private Path drawPath;
    private final ArrayList<DrawInfo> arrayDrawInfo = new ArrayList<>();
    private final ArrayList<DrawInfo> arrayUndoneDrawInfo = new ArrayList<>();

    private OnUndoRedoStateChangeListener onUndoRedoStateChangeListener;

    public FingerPenView(Context context) {
        this(context, null);
    }

    public FingerPenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FingerPenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOnTouchListener(mTouchListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) return;

        if (mPenMode == EditorMode.PEN || mPenMode == EditorMode.ERASER) {

            for (DrawInfo v : arrayDrawInfo) {
                canvas.drawPath(v.getPath(), v.getPaint());
            }
        }
    }

    @Override
    public void setEditorMode(EditorMode editorMode) {
        if (editorMode != null) {
            this.mPenMode = editorMode;

            if (editorMode == EditorMode.PEN) {
                setPenPaint(penColor, penWidth);
            }
            else if (editorMode == EditorMode.ERASER) {
                setEraserPaint(eraserColor, eraserWidth);
            }
        }
    }

    @Override
    public void setPenColor(int penColor) {
        if (this.mPenMode == EditorMode.PEN) {
            this.penColor = penColor;

            if (drawPaint != null)
                drawPaint.setColor(penColor);
        }
    }

    @Override
    public void setPenWidth(int penWidth) {
        if (this.mPenMode == EditorMode.PEN) {
            this.penWidth = penWidth;

            if (drawPaint != null)
                drawPaint.setStrokeWidth(ConvertUtil.convertDpToPixel(penWidth));
        }
    }

    @Override
    public void setEraserColor(int eraserColor) {
        if (this.mPenMode == EditorMode.ERASER) {
            this.eraserColor = eraserColor;

            if (drawPaint != null)
                drawPaint.setColor(eraserColor);
        }
    }

    @Override
    public void setEraserWidth(int eraserWidth) {
        if (this.mPenMode == EditorMode.ERASER) {
            this.eraserWidth = eraserWidth;

            if (drawPaint != null)
                drawPaint.setStrokeWidth(ConvertUtil.convertDpToPixel(eraserWidth));
        }
    }

    @Override
    public void setUndo() {
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
    public void setRedo() {
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
    public void updateUndoRedo() {
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
    public void deletePen() {
        arrayDrawInfo.clear();
        arrayUndoneDrawInfo.clear();

        invalidate();

        updateUndoRedo();
    }

    @Override
    public void setUndoRedoListener(OnUndoRedoStateChangeListener listener) {
        onUndoRedoStateChangeListener = listener;
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

    private void pathInitialize() {
        drawPath = new Path();
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

        invalidate();
    }

    private void drawActionUp() {
        drawPath.lineTo(mX, mY);

        invalidate();

        updateUndoRedo();
    }

    OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (mPenMode == EditorMode.PEN ||
                            mPenMode == EditorMode.ERASER) {
                        float X = event.getX();
                        float Y = event.getY();

                        drawActionDown(X, Y);
                        return true;
                    }
                    return false;
                case MotionEvent.ACTION_MOVE:
                    if (mPenMode == EditorMode.PEN ||
                            mPenMode == EditorMode.ERASER) {
                        float X = event.getX();
                        float Y = event.getY();

//                        Pair<Float, Float> pair = correctCoordinates(X, Y);
//                        X = pair.first;
//                        Y = pair.second;

                        drawActionMove(X, Y);
                        return true;
                    }
                    return false;
                case MotionEvent.ACTION_UP:
                    if (mPenMode == EditorMode.PEN ||
                            mPenMode == EditorMode.ERASER) {
                        drawActionUp();
                        return true;
                    }
                    return false;
            }
            return false;
        }
    };
}
