package com.danielpark.imagecroppersample;

import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.danielpark.imagecroppersample.databinding.ActivityEditorBinding;

/**
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityEditorBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        binding =
                DataBindingUtil.setContentView(this, R.layout.activity_editor);

        binding.editorImageView.setBackgroundColor(Color.parseColor("#772611"));

        binding.prev.setOnClickListener(this);
        binding.next.setOnClickListener(this);
        binding.addImage.setOnClickListener(this);

        binding.edit.setOnClickListener(this);
        binding.pen.setOnClickListener(this);
        binding.eraser.setOnClickListener(this);
        binding.redo.setOnClickListener(this);
        binding.undo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == null) return;

        int id = v.getId();

        switch (id) {
            case R.id.addImage:
                binding.editorImageView.addImage(
                        BitmapFactory.decodeResource(getResources(), R.drawable.splash)
                );

//                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                ImageView iv = new ImageView(this);
//                iv.setLayoutParams(layoutParams);
//                iv.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.splash)
//                );
//
//                binding.editorImageView.addView(iv, 0); // mFingerImageView 가장 바닥에 지정
                break;
            case R.id.prev:

                break;
            case R.id.next:

                break;
            case R.id.edit:

                break;
            case R.id.pen:

                break;
            case R.id.eraser:

                break;
            case R.id.redo:

                break;
            case R.id.undo:

                break;
        }
    }
}
