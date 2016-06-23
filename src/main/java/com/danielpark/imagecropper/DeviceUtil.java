package com.danielpark.imagecropper;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

/**
 * 디바이스 관련 유틸리티
 * <br><br>
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-01-12.
 */
public class DeviceUtil {

	/**
	 * get device resolution
	 * @param context
	 * @return
	 */
	protected static Display getResolutionSize(Context context) throws Exception{
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		return display;
	}

	/**
	 * get device resolution width. return 0 if it fails <br>
	 *     if it is LANDSCAPE MODE change height to width
	 * @param context
	 * @return
	 */
	protected static int getResolutionWidth(Context context){
		try {
			return getResolutionSize(context).getWidth();
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * get device resolution height. return 0 if it fails <br>
	 *     if it is LANDSCAPE MODE change width to height
	 * @param context
	 * @return
	 */
	protected static int getResolutionHeight(Context context){
		try{
			return getResolutionSize(context).getHeight();
		} catch (Exception e) {
			return 0;
		}
	}
}
