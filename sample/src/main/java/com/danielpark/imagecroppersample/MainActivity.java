package com.danielpark.imagecroppersample;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.danielpark.imagecroppersample.databinding.ActivityMainBinding;

/**
 * Copyright (c) 2014-2017 daniel@bapul.net
 * Created by Daniel Park on 2017-07-26.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.cropImageButton.setOnClickListener(this);
        binding.editImageButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == null) return;

        int id = v.getId();

        switch (id) {
            case R.id.cropImageButton:
                startActivity(new Intent(this, CropperActivity.class));
                break;
            case R.id.editImageButton:
                startActivity(new Intent(this, EditorActivity.class));
                break;
        }
    }
}
