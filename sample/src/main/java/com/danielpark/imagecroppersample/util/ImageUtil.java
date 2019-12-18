package com.danielpark.imagecroppersample.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.widget.ImageView;

/**
 * Created by Daniel Park on 2016-03-31.
 */
public class ImageUtil {

	/**
	 * 이미지의 각도를 실제 각도 수치로 변환.
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
	 * 이미지 회전 기능	(Matrix 를 통해서 좌우 반전 처리 가능하게 할 수는 없나..)
	 * @param bitmap
	 * @param degree 회전할 각도
	 * @param doRecycle <code>true</code> 일 경우 recycle 시도
	 * @return
	 */
	@Deprecated
	public static Bitmap rotateImage(Bitmap bitmap, float degree, boolean doRecycle) {
		// create new matrix
		Matrix matrix = new Matrix();
		// setup rotation degree
		matrix.postRotate(degree);
		// Daniel (2016-01-23 10:36:17): TODO: 이미지를 계속 회전시 filter 적용으로 인해 뿌옇게 됨. 추후 수정 필요.
		Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		try{
			if(bitmap != null && !bitmap.isRecycled() && doRecycle && bitmap != bmp){
				bitmap.recycle();
			}
		}catch (Exception e){}
		return bmp;
	}

	/**
	 * Daniel (2016-01-21 18:45:40): 이미지 좌우 반전 기능 (Matrix 를 통해서 좌우 반전 처리 가능하게 할 수는 없나..)
	 * @param bitmap
	 * @param doRecycle	<code>true</code> 일 경우 recycle 시도
	 * @return
	 */
	@Deprecated
	public static Bitmap reverseImage(Bitmap bitmap, boolean doRecycle){
		// create new matrix
		Matrix matrix = new Matrix();
		// setup rotation degree
		matrix.preScale(-1, 1);
		// Daniel (2016-01-23 10:36:17): TODO: 이미지를 계속 반전시 filter 적용으로 인해 뿌옇게 됨. 추후 수정 필요.
		Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		try {
			if (bitmap != null && !bitmap.isRecycled() && doRecycle && bitmap != bmp) {
				bitmap.recycle();
			}
		}catch (Exception e){}
		return bmp;
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
	 * Fit ImageView size with width & height to display screen
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
