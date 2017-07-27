package com.danielpark.imageeditor.colorpicker;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.danielpark.imagecropper.R;
import com.danielpark.imagecropper.util.ConvertUtil;
import com.danielpark.imageeditor.customview.CheckBoxRadioButton;

/**
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-27.
 */

public class ColorPickerDialogFragment extends DialogFragment implements View.OnClickListener, ColorPickerView.OnColorChangedListener {

    private LinearLayout colorPickerToolbar;
    private CheckBoxRadioButton radioButtonFirstSwatchColor;
    private CheckBoxRadioButton radioButtonSecondSwatchColor;
    private CheckBoxRadioButton radioButtonThirdSwatchColor;
    private CheckBoxRadioButton radioButtonPickerColor;
    private ColorPickerView colorPickerView;
    private PenWidthView penWidthView;
    private SeekBar seekBarPenWidth;

    //ColorPicker
    private int mPaintColor;
    private int mPenWidth = 1;

    public static ColorPickerDialogFragment newInstance() {
        return new ColorPickerDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_color_picker, container, false);
        colorPickerToolbar = (LinearLayout) v.findViewById(R.id.colorPickerToolbar);
        radioButtonFirstSwatchColor = (CheckBoxRadioButton) v.findViewById(R.id.radioButtonFirstSwatchColor);
        radioButtonSecondSwatchColor = (CheckBoxRadioButton) v.findViewById(R.id.radioButtonSecondSwatchColor);
        radioButtonThirdSwatchColor = (CheckBoxRadioButton) v.findViewById(R.id.radioButtonThirdSwatchColor);
        radioButtonPickerColor = (CheckBoxRadioButton) v.findViewById(R.id.radioButtonPickerColor);
        colorPickerView = (ColorPickerView) v.findViewById(R.id.colorPickerView);
        penWidthView = (PenWidthView) v.findViewById(R.id.penWidthView);
        seekBarPenWidth = (SeekBar) v.findViewById(R.id.seekBarPenWidth);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        colorPickerView.setOnColorChangedListener(this);
        // 색 초기 설정.
        mPaintColor = Color.parseColor("#000000"); // default 검정색.
        colorPickerView.setColor(mPaintColor);

        radioButtonFirstSwatchColor.setBackgroundColor(Color.parseColor("#ff4958"));
        radioButtonSecondSwatchColor.setBackgroundColor(Color.parseColor("#4cb053"));
        radioButtonThirdSwatchColor.setBackgroundColor(Color.parseColor("#3671d9"));
        radioButtonPickerColor.setBackgroundColor(mPaintColor);

        //---------------------------------------------------------------------------------
        // Setting check box
        if (mPaintColor == radioButtonPickerColor.getBackgroundColor())
            radioButtonPickerColor.setChecked(true);
        else
            radioButtonPickerColor.setChecked(false);
        if (mPaintColor == radioButtonFirstSwatchColor.getBackgroundColor())
            radioButtonFirstSwatchColor.setChecked(true);
        else
            radioButtonFirstSwatchColor.setChecked(false);
        if (mPaintColor == radioButtonSecondSwatchColor.getBackgroundColor())
            radioButtonSecondSwatchColor.setChecked(true);
        else
            radioButtonSecondSwatchColor.setChecked(false);
        if (mPaintColor == radioButtonThirdSwatchColor.getBackgroundColor())
            radioButtonThirdSwatchColor.setChecked(true);
        else
            radioButtonThirdSwatchColor.setChecked(false);
        //---------------------------------------------------------------------------------

        if (mPenWidth < 1) mPenWidth = 1;

        radioButtonFirstSwatchColor.setOnClickListener(this);
        radioButtonSecondSwatchColor.setOnClickListener(this);
        radioButtonThirdSwatchColor.setOnClickListener(this);
        radioButtonPickerColor.setOnClickListener(this);

        seekBarPenWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = Math.max(1, progress);
                penWidthView.setPaintStrokeWidth(progress);

                mPenWidth = progress;
                if (mPenWidth < 1) mPenWidth = 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null)
            return;

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ConvertUtil.convertDpToPixel(264);
        getDialog().getWindow().setAttributes(params);
    }

    @Override
    public void onClick(View v) {
        if (v == null) return;

        int id = v.getId();

        if (id == R.id.radioButtonFirstSwatchColor) {
            mPaintColor = radioButtonFirstSwatchColor.getBackgroundColor();
        }
        else if (id == R.id.radioButtonSecondSwatchColor) {
            mPaintColor = radioButtonSecondSwatchColor.getBackgroundColor();
        }
        else if (id == R.id.radioButtonThirdSwatchColor) {
            mPaintColor = radioButtonThirdSwatchColor.getBackgroundColor();
        }
        else if (id == R.id.radioButtonPickerColor) {
            mPaintColor = colorPickerView.getColor();
        }
    }

    @Override
    public void onColorChanged(int newColor) {
        radioButtonPickerColor.setBackgroundColor(newColor);
        mPaintColor = newColor;

        if (mOnColorPickerListener != null)
            mOnColorPickerListener.onSelectedColor(newColor);
    }

    private OnColorPickerListener mOnColorPickerListener;

    public interface OnColorPickerListener {
        void onSelectedColor(int newColor);
    }

    public void setOnColorPickerListener(OnColorPickerListener listener) {
        mOnColorPickerListener = listener;
    }
}
