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
package com.danielworld.imagecropper.util;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

/**
 * Utility for Device
 * <br><br>
 * Created by Namgyu Park on 2016-01-12.
 */
public class DeviceUtil {

	/**
	 * get device resolution
	 * @param context
	 * @return
	 */
	public static Display getResolutionSize(Context context) throws Exception {
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
	public static int getResolutionWidth(Context context){
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
	public static int getResolutionHeight(Context context){
		try{
			return getResolutionSize(context).getHeight();
		} catch (Exception e) {
			return 0;
		}
	}
}
