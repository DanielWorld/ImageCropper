package com.danielpark.imagecropper.util.log;

/**
 * Copyright (C) 2014-2016 daniel@bapul.net
 * Created by Daniel on 2016-12-07.
 */

public class ImageCropperLogger {

    private static boolean mLogFlag = false;

    public static void enable() {
        mLogFlag = true;
    }

    public static void disable() {
        mLogFlag = false;
    }

    public static boolean getLogState(){
        return mLogFlag;
    }
}
