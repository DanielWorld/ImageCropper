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
package com.danielworld.image_cropper_sample;

import androidx.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.danielworld.imagecropper.listener.OnUndoRedoStateChangeListener;
import com.danielworld.image_cropper_sample.databinding.ActivityEditorBinding;
import com.danielworld.imageeditor.EditorMode;
import com.danielworld.imageeditor.OnEditorModeStateChangeListener;
import com.danielworld.imageeditor.OnPanelPageStateChangeListener;
import com.danielworld.imageeditor.colorpicker.ColorPickerDialogFragment;

import java.io.File;

/**
 * Created by Namgyu Park on 2017-07-26.
 */

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityEditorBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        binding =
                DataBindingUtil.setContentView(this, R.layout.activity_editor);

        binding.editorPanelView.setEditorMode(EditorMode.PEN);

        binding.editorPanelView.setOnPanelPageStateChangeListener(new OnPanelPageStateChangeListener() {
            @Override
            public void onPrevPanelPageAvailable(boolean result) {
                binding.prev.setEnabled(result);
            }

            @Override
            public void onNextPanelPageAvailable(boolean result) {
                binding.next.setEnabled(result);
            }
        });

        binding.editorPanelView.setOnEditorModeStateChangeListener(new OnEditorModeStateChangeListener() {
            @Override
            public void onEditorModeState(EditorMode currentEditorMode) {
                // TODO: Do not invoke click event trigger

                if (currentEditorMode == EditorMode.PEN ||
                        currentEditorMode == EditorMode.ERASER) {
                    binding.redo.setVisibility(View.VISIBLE);
                    binding.undo.setVisibility(View.VISIBLE);
                    binding.rotate.setVisibility(View.GONE);
                    binding.deleteImage.setVisibility(View.GONE);
                    binding.deletePen.setVisibility(View.VISIBLE);
                } else {
                    binding.redo.setVisibility(View.GONE);
                    binding.undo.setVisibility(View.GONE);
                    binding.rotate.setVisibility(View.VISIBLE);
                    binding.deleteImage.setVisibility(View.VISIBLE);
                    binding.deletePen.setVisibility(View.GONE);
                }
            }
        });
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
        binding.deleteImage.setOnClickListener(this);
        binding.deletePen.setOnClickListener(this);
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
                binding.editorPanelView.addPanelPage();
                break;
            case R.id.prev:
                binding.editorPanelView.prevPanelPage();
                break;
            case R.id.next:
                binding.editorPanelView.nextPanelPage();
                break;
            case R.id.done:
                for (File f : binding.editorPanelView.getEditedFiles()) {
                    Log.d("ImageCropperLibrary", f.getAbsolutePath());
                }
                break;
            case R.id.edit:
                binding.editorPanelView.setEditorMode(EditorMode.EDIT);
                break;
            case R.id.pen:
                if (v.isSelected()) {
//                    // Daniel (2017-07-26 17:50:44): show Color picker
                    ColorPickerDialogFragment colorPickerDialogFragment
                            = ColorPickerDialogFragment.newInstance();
                    colorPickerDialogFragment.setOnColorPickerListener(new ColorPickerDialogFragment.OnColorPickerListener() {
                        @Override
                        public void onSelectedPenColor(int newColor) {
                            binding.editorPanelView.setPenColor(newColor);
                        }

                        @Override
                        public void onSelectedPenWidth(int newWidth) {
                            binding.editorPanelView.setPenWidth(newWidth);
                        }
                    });
                    colorPickerDialogFragment.show(getSupportFragmentManager(), "");
                    v.setSelected(false);
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
                binding.editorPanelView.setRotationBy(90);
                break;
            case R.id.deleteImage:
                binding.editorPanelView.deleteImage();
                break;
            case R.id.deletePen:
                binding.editorPanelView.deletePen();
                break;
        }
    }
}
