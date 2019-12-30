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

import android.media.ExifInterface;
import android.widget.ImageView;

/**
 * Created by Namgyu Park on 2016-03-31.
 */
public class ImageUtil {

	/**
	 * Convert image angle to real degree
	 *
	 * @param exifOrientation
	 * @return
	 */
	public static int exifToDegrees(int exifOrientation) {
		// http://stackoverflow.com/a/11081918/361100
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}

	/**
	 * Resize ImageView
	 * @param iv
	 */
	public static void resizeImageView(ImageView iv, int w, int h) {
		if (iv != null && w > 0 && h > 0) {
			iv.requestLayout();
			iv.getLayoutParams().width = w;
			iv.getLayoutParams().height = h;
		}
	}

	/**
	 * Fit ImageView size with width and height to display screen
	 * @param iv
	 * @param bitmapWidth
	 * @param bitmapHeight
	 */
	public static void resizeFitToScreen(ImageView iv, int bitmapWidth, int bitmapHeight){
		resizeFitToScreen(iv, bitmapWidth, bitmapHeight, null);
	}

	/**
	 * Fit ImageView to device width screen
	 * @param iv
	 * @param bitmapWidth
	 * @param bitmapHeight
	 * @param margin [top, left, right, bottom]
	 */
	public static void resizeFitToScreen(ImageView iv, int bitmapWidth, int bitmapHeight, int[] margin) {
		if (iv != null) {
			int displayWidth;
//			int displayHeight;

			if (margin != null && margin.length == 4) {
				displayWidth = DeviceUtil.getResolutionWidth(iv.getContext()) - margin[1] - margin[2];
//				displayHeight = DeviceUtil.getResolutionHeight(iv.getContext()) - margin[0] - margin[3];
			} else {
				displayWidth = DeviceUtil.getResolutionWidth(iv.getContext());
//				displayHeight = DeviceUtil.getResolutionHeight(iv.getContext());
			}

			// Bitmap's width is larger than display resolution
			if (bitmapWidth >= displayWidth)
				return;

			// 1) enlarge bitmap width to fit device width

			int newX = displayWidth;
			int newY = (int) ((double) bitmapHeight * (double) displayWidth / (double) bitmapWidth);

			// OKAY
			resizeImageView(iv, newX, newY);
		}
	}
}
