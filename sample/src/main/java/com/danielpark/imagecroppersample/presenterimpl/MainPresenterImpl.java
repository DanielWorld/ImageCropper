/*
 * Copyright (c) 2016 DanielWorld.
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
package com.danielpark.imagecroppersample.presenterimpl;

import android.app.Activity;
import android.content.Context;

import com.danielpark.imagecroppersample.interactor.PermissionInterActor;
import com.danielpark.imagecroppersample.presenter.MainPresenter;

/**
 * Created by Namgyu Park on 2016-06-25.
 */
public class MainPresenterImpl extends PresenterImpl implements MainPresenter, PermissionInterActor.OnFinishedListener {

    private View view;
    private PermissionInterActor permissionInterActor;

    public MainPresenterImpl(Context context, View mainView){
        mContext = context;
        this.view = mainView;

        permissionInterActor = new PermissionInterActor();
    }

    @Override
    public void initViews() {
        if (view != null)
            view.setLayout();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void checkPermissions() {
        permissionInterActor.checkPermissions(mContext, this);
    }

    @Override
    public void clickDone() {
        if (view != null)
            view.setPicture();
    }

    @Override
    public void clickCamera() {
        if (view != null)
            view.openCamera();
    }

    @Override
    public void clickClockwise() {
        if (view != null)
            view.setRotateClockwise();
    }

    @Override
    public void clickCounterClockwise() {
        if (view != null)
            view.setRotateCounterClockwise();
    }

    @Override
    public void clickModeChange(int index) {
        if (view != null)
            view.setMode();
    }

    @Override
    public void clickUndo() {
        if (view != null)
            view.setUndo();
    }

    @Override
    public void clickRedo() {
        if (view != null)
            view.setRedo();
    }

    @Override
    public void clickShapeChange(int index) {
        if (view != null)
            view.setShape();
    }

    @Override
    public void showMessage(String message) {
        if (view != null)
            view.showMessage(message);
    }

    @Override
    public void requestPermissions(Activity activity, int requestCode) {
        permissionInterActor.requestPermissions(activity, requestCode);
    }

    @Override
    public void onDestroy() {
        view = null;
        mContext = null;
    }

    @Override
    public void onPermissionResult(boolean result, String[] permissions) {
        if (view != null) {
            view.resultPermissions(result, permissions);
        }
    }
}
