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
package com.danielworld.imageeditor.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RadioButton;

import com.danielworld.imagecropper.R;

/**
 * Created by Namgyu Park on 2017-07-27.
 */

@SuppressLint("AppCompatCustomView")
public class CheckBoxRadioButton extends RadioButton {

    private Drawable mDrawable;
    int mColor;

    public CheckBoxRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDrawable = getResources().getDrawable(R.drawable.selector_color_picker_checked);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawable != null) {
            mDrawable.setState(getDrawableState());
            final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
            final int height = mDrawable.getIntrinsicHeight();

            int y = 0;

            switch (verticalGravity) {
                case Gravity.BOTTOM:
                    y = getHeight() - height;
                    break;
                case Gravity.CENTER_VERTICAL:
                    y = (getHeight() - height) / 2;
                    break;
            }

            int buttonWidth = mDrawable.getIntrinsicWidth();
            int buttonLeft = (getWidth() - buttonWidth) / 2;
            mDrawable.setBounds(buttonLeft, y, buttonLeft+buttonWidth, y + height);
            mDrawable.draw(canvas);
        }
    }

    public void setBackgroundColor(int color){
        mColor = color;
        super.setBackgroundColor(color);
    }

    public int getBackgroundColor(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
            return ((ColorDrawable)getBackground()).getColor();
        return mColor;
    }
}
