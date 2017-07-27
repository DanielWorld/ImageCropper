package com.danielpark.imageeditor.colorpicker;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.danielpark.imagecropper.R;
import com.danielpark.imagecropper.util.ConvertUtil;
import com.danielpark.imageeditor.customview.CheckBoxRadioButton;

/**
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-27.
 */

public class ColorPickerDialogFragment extends DialogFragment implements View.OnClickListener, ColorPickerView.OnColorChangedListener {

    public final static String TAG_PEN_COLOR = "com.danielpark.imageeditor.colorpicker_penColor";
    public final static String TAG_PEN_WIDTH = "com.danielpark.imageeditor.colorpicker_penWidth";

//    private LinearLayout colorPickerToolbar;
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

    private SharedPreferences mSharedPref;

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
//        colorPickerToolbar = (LinearLayout) v.findViewById(R.id.colorPickerToolbar);
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

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        colorPickerView.setOnColorChangedListener(this);

        mPaintColor = mSharedPref.getInt(TAG_PEN_COLOR, Color.parseColor("#000000")); // default color = black.
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

        // Load pen width size
        mPenWidth = mSharedPref.getInt(TAG_PEN_WIDTH, 1);
        if (mPenWidth < 1 || mPenWidth > 100) mPenWidth = 1;

        // Set pen width and color
        penWidthView.setPaintStrokeColor(mPaintColor);
        penWidthView.setPaintStrokeWidth(mPenWidth);
        seekBarPenWidth.setProgress(mPenWidth);

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
                if (mPenWidth < 1 || mPenWidth > 100) mPenWidth = 1;

                // save picked width
                mSharedPref.edit().putInt(TAG_PEN_WIDTH, mPenWidth).apply();

                if (mOnColorPickerListener != null)
                    mOnColorPickerListener.onSelectedPenWidth(mPenWidth);
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

            // save picked color
            mSharedPref.edit().putInt(TAG_PEN_COLOR, mPaintColor).apply();

            // apply color to pen stroke
            penWidthView.setPaintStrokeColor(mPaintColor);

            if (mOnColorPickerListener != null)
                mOnColorPickerListener.onSelectedPenColor(mPaintColor);
        }
        else if (id == R.id.radioButtonSecondSwatchColor) {
            mPaintColor = radioButtonSecondSwatchColor.getBackgroundColor();

            // save picked color
            mSharedPref.edit().putInt(TAG_PEN_COLOR, mPaintColor).apply();

            // apply color to pen stroke
            penWidthView.setPaintStrokeColor(mPaintColor);

            if (mOnColorPickerListener != null)
                mOnColorPickerListener.onSelectedPenColor(mPaintColor);
        }
        else if (id == R.id.radioButtonThirdSwatchColor) {
            mPaintColor = radioButtonThirdSwatchColor.getBackgroundColor();

            // save picked color
            mSharedPref.edit().putInt(TAG_PEN_COLOR, mPaintColor).apply();

            // apply color to pen stroke
            penWidthView.setPaintStrokeColor(mPaintColor);

            if (mOnColorPickerListener != null)
                mOnColorPickerListener.onSelectedPenColor(mPaintColor);
        }
        else if (id == R.id.radioButtonPickerColor) {
            mPaintColor = colorPickerView.getColor();

            // save picked color
            mSharedPref.edit().putInt(TAG_PEN_COLOR, mPaintColor).apply();

            // apply color to pen stroke
            penWidthView.setPaintStrokeColor(mPaintColor);

            if (mOnColorPickerListener != null)
                mOnColorPickerListener.onSelectedPenColor(mPaintColor);
        }
    }

    @Override
    public void onColorChanged(int newColor) {
        radioButtonPickerColor.setChecked(true);
        radioButtonPickerColor.setBackgroundColor(newColor);
        mPaintColor = newColor;

        // save picked color
        mSharedPref.edit().putInt(TAG_PEN_COLOR, newColor).apply();

        // apply color to pen stroke
        penWidthView.setPaintStrokeColor(newColor);

        if (mOnColorPickerListener != null)
            mOnColorPickerListener.onSelectedPenColor(newColor);
    }

    private OnColorPickerListener mOnColorPickerListener;

    public interface OnColorPickerListener {
        void onSelectedPenColor(int newColor);

        void onSelectedPenWidth(int newWidth);
    }

    public void setOnColorPickerListener(OnColorPickerListener listener) {
        mOnColorPickerListener = listener;
    }
}
