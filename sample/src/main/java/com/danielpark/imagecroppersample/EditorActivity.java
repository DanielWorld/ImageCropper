package com.danielpark.imagecroppersample;

import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;
import com.danielpark.imagecroppersample.databinding.ActivityEditorBinding;
import com.danielpark.imageeditor.EditorMode;

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

        binding.editorPanelView.setBackgroundColor(Color.parseColor("#772611"));

        binding.editorPanelView.setUndoRedoListener(new OnUndoRedoStateChangeListener() {
            @Override
            public void onUndoAvailable(boolean result) {
                binding.undo.setEnabled(result);
            }

            @Override
            public void onRedoAvailable(boolean result) {
                binding.redo.setEnabled(result);
            }
        });

        binding.prev.setOnClickListener(this);
        binding.next.setOnClickListener(this);
        binding.addImage.setOnClickListener(this);
        binding.addPage.setOnClickListener(this);
        binding.done.setOnClickListener(this);

        binding.edit.setOnClickListener(this);
        binding.pen.setOnClickListener(this);
        binding.eraser.setOnClickListener(this);
        binding.redo.setOnClickListener(this);
        binding.undo.setOnClickListener(this);
        binding.rotate.setOnClickListener(this);
        binding.delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == null) return;

        int id = v.getId();

        switch (id) {
            case R.id.addImage:
                binding.editorPanelView.addImage(
                        BitmapFactory.decodeResource(getResources(), R.drawable.splash)
                );
                break;
            case R.id.addPage:

                break;
            case R.id.prev:

                break;
            case R.id.next:

                break;
            case R.id.done:

                break;
            case R.id.edit:
                binding.editorPanelView.setEditorMode(EditorMode.EDIT);
                break;
            case R.id.pen:
                if (v.isSelected()) {
                    // Daniel (2017-07-26 17:50:44): show Color picker
                } else {
                    v.setSelected(true);
                    binding.editorPanelView.setEditorMode(EditorMode.PEN);
                }
                break;
            case R.id.eraser:
                binding.editorPanelView.setEditorMode(EditorMode.ERASER);
                break;
            case R.id.redo:
                binding.editorPanelView.setRedo();
                break;
            case R.id.undo:
                binding.editorPanelView.setUndo();
                break;
            case R.id.rotate:

                break;
            case R.id.delete:
                binding.editorPanelView.deleteImage();
                break;
        }
    }
}
