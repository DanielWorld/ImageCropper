package com.danielpark.imagecroppersample.presenterimpl;

import android.app.Activity;
import android.content.Context;

import com.danielpark.imagecroppersample.interactor.PermissionInterActor;
import com.danielpark.imagecroppersample.presenter.MainPresenter;

/**
 * Created by Daniel Park on 2016-06-25.
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
