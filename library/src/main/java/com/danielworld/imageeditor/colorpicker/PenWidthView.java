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
package com.danielworld.imageeditor.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.danielworld.imagecropper.util.ConvertUtil;

/**
 * Created by Namgyu Park on 2017-07-27.
 */

public class PenWidthView extends View {
    private Path _path;
    private Paint _paint;
    private Context _context;

    public PenWidthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PenWidthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        _context = getContext();

        _path = new Path();
        _paint = new Paint();
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeCap(Paint.Cap.ROUND);
        _paint.setAntiAlias(true);
//		_paint.setStrokeWidth(5);
        _paint.setStrokeWidth(ConvertUtil.convertDpToPixel(1));

        _path.moveTo(getPxToDp(25), getPxToDp(30));
        _path.cubicTo(getPxToDp(70), getPxToDp(10), getPxToDp(120), getPxToDp(40), getPxToDp(170), getPxToDp(20));

    }


    /**
     * Convert px to dp
     * @param px
     * @return dp
     */
    public float getPxToDp(int px) {
//        float density = 0.0f;
//        density = _context.getResources().getDisplayMetrics().density;
        DisplayMetrics metrics = _context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, metrics);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(_path, _paint);
        super.onDraw(canvas);
    }

//    public void setPaint(Paint paint) {
//        this._paint = paint;
//    }

    public void setPaintStrokeColor(int color) {
        _paint.setColor(color);
        invalidate();
    }

    public void setPaintStrokeWidth(int width) {
        _paint.setStrokeWidth(ConvertUtil.convertDpToPixel(width));
        invalidate();
    }
}
