package com.danielpark.imageeditor.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RadioButton;

import com.danielpark.imagecropper.R;

/**
 * Created by Daniel Park on 2017-07-27.
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
