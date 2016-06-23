package com.danielpark.imagecropper;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.widget.ImageView;

/**
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-03-31.
 */
public class ImageUtil {

	/**
	 * Convert image angle to real degree
	 *
	 * @param exifOrientation
	 * @return
	 */
	protected static int exifToDegrees(int exifOrientation) {
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
	protected static void resizeImageView(ImageView iv, int w, int h) {
		if (iv != null && w > 0 && h > 0) {
			iv.requestLayout();
			iv.getLayoutParams().width = w;
			iv.getLayoutParams().height = h;
		}
	}

	/**
	 * Fit ImageView size with width & height to display screen
	 * @param iv
	 * @param bitmapWidth
	 * @param bitmapHeight
	 */
	protected static void resizeFitToScreen(ImageView iv, int bitmapWidth, int bitmapHeight){
		resizeFitToScreen(iv, bitmapWidth, bitmapHeight, null);
	}

	/**
	 * Fit ImageView to device width screen
	 * @param iv
	 * @param bitmapWidth
	 * @param bitmapHeight
	 * @param margin [top, left, right, bottom]
	 */
	protected static void resizeFitToScreen(ImageView iv, int bitmapWidth, int bitmapHeight, int[] margin) {
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
