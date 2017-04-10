package com.danielpark.imagecroppersample.util;

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
	 * 기기 Resolution 사이즈 구하는 method
	 * @param context
	 * @return
	 */
	public static Display getResolutionSize(Context context) throws Exception{
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		return display;
	}

	/**
	 * 기기 Resolution width 구하는 method. 실패시 0 리턴 <br>
	 *     만약 LANDSCAPE MODE 일경우 height 가 width 로 변함
	 * @param context
	 * @return
	 */
	public static int getResolutionWidth(Context context){
		try {
			return getResolutionSize(context).getWidth();
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 기기 Resolution height 구하는 method. 실패시 0 리턴 <br>
	 *     만약 LANDSCAPE MODE 일경우 width 가 height 로 변함
	 * @param context
	 * @return
	 */
	public static int getResolutionHeight(Context context){
		try{
			return getResolutionSize(context).getHeight();
		} catch (Exception e) {
			return 0;
		}
	}
}
