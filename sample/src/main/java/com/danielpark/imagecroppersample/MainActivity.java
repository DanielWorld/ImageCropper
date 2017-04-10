package com.danielpark.imagecroppersample;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.util.List;

import com.danielpark.imagecropper.ControlMode;
import com.danielpark.imagecropper.CropMode;
import com.danielpark.imagecropper.CropperImageView;
import com.danielpark.imagecropper.ShapeMode;
import com.danielpark.imagecropper.UtilMode;
import com.danielpark.imagecropper.listener.OnThumbnailChangeListener;
import com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener;
import com.danielpark.imagecropper.model.CropSetting;
import com.danielpark.imagecroppersample.presenter.MainPresenter;
import com.danielpark.imagecroppersample.presenterimpl.MainPresenterImpl;
import com.danielpark.imagecroppersample.util.ImageUtil;

public class MainActivity extends AppCompatActivity implements MainPresenter.View, View.OnClickListener, OnUndoRedoStateChangeListener, OnThumbnailChangeListener {

    TextView modeTitle;
    CropperImageView iv, result;
    ImageView thumbnailView;

    Toast toast;
    Button btnDone, btnChangeMode, shapeChanger;
    ImageButton btnCamera, btnRotate, btnReRotate, btnUndo, btnRedo;
    File file;

    int mode = 0;
    int shapeMode = 0;
    long backKeyPressedTime = 0L;

    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modeTitle = (TextView) findViewById(R.id.modeTitle);
        iv = (CropperImageView) findViewById(R.id.imageView1);
        btnDone = (Button) findViewById(R.id.btnConfirm);
        btnCamera = (ImageButton) findViewById(R.id.takeCamera);
        btnRotate = (ImageButton) findViewById(R.id.rotateClockwise);
        btnChangeMode = (Button) findViewById(R.id.modeChanger);
        btnReRotate = (ImageButton) findViewById(R.id.rotateCountClockwise);
        btnUndo = (ImageButton) findViewById(R.id.undo);
        btnRedo = (ImageButton) findViewById(R.id.redo);
        shapeChanger = (Button) findViewById(R.id.shapeChanger);
        thumbnailView = (ImageView) findViewById(R.id.thumbnailView);

        result = (CropperImageView) findViewById(R.id.result);

        presenter = new MainPresenterImpl(this, this);
        presenter.initViews();
    }

    @Override
    public void setLayout() {
        modeTitle.setText(CropMode.CROP_STRETCH.name());

        CropSetting cropSetting = new CropSetting.CropBuilder(CropMode.CROP_STRETCH)
                .setShapeMode(ShapeMode.RECTANGLE)
                .setControlMode(ControlMode.FIXED)
                .setUtilMode(UtilMode.NONE)
                .setCropInsetRatio(20f)
                .build();

        iv.setCropSetting(cropSetting);

        iv.setCustomImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.splash));
        iv.setUndoRedoListener(this);
        iv.setThumbnailChangeListener(this);

        File filePath = getExternalCacheDir();  // 카메라인텐트를 쓸 때 getCacheDir()로 하면 안됨. http://stackoverflow.com/q/18711525/361100

        if (filePath != null && !filePath.exists())
            filePath.mkdirs();

        file = new File(filePath, "aaaaa.jpg");

        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        btnDone.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnChangeMode.setOnClickListener(this);
        btnRotate.setOnClickListener(this);
        btnReRotate.setOnClickListener(this);
        btnUndo.setOnClickListener(this);
        btnRedo.setOnClickListener(this);
        shapeChanger.setOnClickListener(this);

        // set undo / redo disabled at first
        btnUndo.setEnabled(false);
        btnRedo.setEnabled(false);

        presenter.checkPermissions();
    }

    @Override
    public void resultPermissions(boolean result, String[] permissions) {
        if (!result) {
            presenter.requestPermissions(this, 500);
        }
    }

    @Override
    public void setPicture() {
        if (iv != null) {
            File path  = iv.getCropImage();

            try {
                if (path == null) {
                    Toast.makeText(MainActivity.this, "Invalid File", Toast.LENGTH_SHORT).show();
                } else {
                    CropSetting cropSetting = new CropSetting.CropBuilder(CropMode.NONE)
                            .setShapeMode(ShapeMode.NONE)
                            .setControlMode(ControlMode.NONE)
                            .setUtilMode(UtilMode.PENCIL)
                            .build();

                    result.setCropSetting(cropSetting);

                    result.setCustomImageFile(path);
                    result.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setRotateClockwise() {
        if (iv != null)
            iv.setRotationBy(90);
    }

    @Override
    public void setRotateCounterClockwise() {
        if (iv != null)
            iv.setRotationBy(-90);
    }

    @Override
    public void setUndo() {
        if (iv != null)
            iv.setUndo();
    }

    @Override
    public void setRedo() {
        if (iv != null)
            iv.setRedo();
    }

    @Override
    public void setMode() {
        switch (mode) {
            case 0:
                if (iv != null) {
                    iv.setCropMode(CropMode.CROP);
                    modeTitle.setText(CropMode.CROP.name());
                    mode++;
                }
                break;
            case 1:
                if (iv != null) {
                    iv.setCropMode(CropMode.CROP_STRETCH);
                    modeTitle.setText(CropMode.CROP_STRETCH.name());
                    mode++;
                }
                break;
            case 2:
                if (iv != null) {
                    iv.setCropMode(CropMode.CROP_SHRINK);
                    modeTitle.setText(CropMode.CROP_SHRINK.name());
                    mode++;
                }
                break;
            case 3:
                if (iv != null) {
                    iv.setCropMode(CropMode.NONE);
                    iv.setUtilMode(UtilMode.PENCIL);
                    modeTitle.setText(UtilMode.PENCIL.name());
                    mode++;
                }
                break;
            case 4:
                if (iv != null) {
                    iv.setCropMode(CropMode.NONE);
                    iv.setUtilMode(UtilMode.ERASER);
                    modeTitle.setText(UtilMode.ERASER.name());
                    mode++;
                }
                break;
            case 5:
                if (iv != null) {
                    iv.setCropMode(CropMode.NONE);
                    iv.setUtilMode(UtilMode.NONE);
                    modeTitle.setText(UtilMode.NONE.name());
                    mode++;
                }
                break;
            default:
                if (iv != null) {
                    iv.setCropMode(CropMode.CROP_STRETCH);
                    modeTitle.setText(CropMode.CROP_STRETCH.name());
                    mode = 0;
                }
                break;
        }
    }

    @Override
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Nougat 7 에서 targetSdkVersion 24 이상인 앱이 file:// 사용시 발생하는 이슈 //
        // http://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed //
        // https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en //
        Uri outputUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
            ClipData clip=
                    ClipData.newUri(getContentResolver(), "A photo", outputUri);

            intent.setClipData(clip);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        else {
            List<ResolveInfo> resInfoList=
                    getPackageManager()
                            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, outputUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }

        startActivityForResult(intent, 1000);
    }

    @Override
    public void setShape() {
        switch (shapeMode) {
            case 0:
                if (iv != null) {
                    iv.setControlMode(ControlMode.FIXED);
                    shapeChanger.setText(ControlMode.FIXED.name());
                    shapeMode++;
                }
                break;
            case 1:
                if (iv != null) {
                    iv.setControlMode(ControlMode.FREE);
                    shapeChanger.setText(ControlMode.FREE.name());
                    shapeMode++;
                }
                break;
            case 2:
                if (iv != null) {
                    iv.setControlMode(ControlMode.FIXED);
                    shapeChanger.setText(ControlMode.FIXED.name());
                    shapeMode++;
                }
                break;
            default:
                if (iv != null) {
                    iv.setControlMode(ControlMode.FREE);
                    shapeChanger.setText(ControlMode.FREE.name());
                    shapeMode = 0;
                }
                break;
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == 1000) {
            Uri uri = null;
            if (data != null && data.getData() != null){
                uri = data.getData();
            } else {
                if (file != null) {
                    uri = Uri.parse(file.toURI().toString());
                }
            }

            if (uri == null) {
                Toast.makeText(MainActivity.this, "uri null!", Toast.LENGTH_SHORT).show();
                return;
            }

            int degree = 0;
            try {
                ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                degree = ImageUtil.exifToDegrees(rotation);
                Log.d("OKAY", "file exif degree : " + degree);
            } catch (Exception e) {
                e.printStackTrace();
            }

            iv.setCustomImageFile(file, degree);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 500) {
            for (int index = 0; index < grantResults.length; index++) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    if (!shouldShowRequestPermissionRationale(permissions[index])) {
                        Toast.makeText(MainActivity.this, "Sorry, You gotta approve these permissions in Setting page", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    else {
                        finish();
                        return;
                    }
                }
            }

            presenter.checkPermissions();
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {

        if (result != null && result.getVisibility() == View.VISIBLE) {
            result.setVisibility(View.GONE);
            return;
        }

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "Are you sure to finish?", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            if (toast != null)
                toast.cancel();
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (v == null) return;

        final int id = v.getId();

        switch (id) {
            case R.id.btnConfirm:
                presenter.clickDone();
                break;
            case R.id.takeCamera:
                presenter.clickCamera();
                break;
            case R.id.rotateClockwise:
                presenter.clickClockwise();
                break;
            case R.id.modeChanger:
                presenter.clickModeChange(mode % 3);
                break;
            case R.id.rotateCountClockwise:
                presenter.clickCounterClockwise();
                break;
            case R.id.undo:
                presenter.clickUndo();
                break;
            case R.id.redo:
                presenter.clickRedo();
                break;
            case R.id.shapeChanger:
                presenter.clickShapeChange(shapeMode % 3);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onUndoAvailable(boolean result) {
        if (btnUndo != null) {
            btnUndo.setEnabled(result);
        }
    }

    @Override
    public void onRedoAvailable(boolean result) {
        if (btnRedo != null) {
            btnRedo.setEnabled(result);
        }
    }

    @Override
    public void onThumbnailChanged(Bitmap bitmap) {
        if (thumbnailView != null) {
            thumbnailView.setImageBitmap(null);
            thumbnailView.setImageBitmap(bitmap);
        }
    }
}
