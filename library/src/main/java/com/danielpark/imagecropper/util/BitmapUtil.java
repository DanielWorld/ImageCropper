package com.danielpark.imagecropper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;

import com.danielworld.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Daniel (2016-01-14 11:52:16): Utility to get efficient Bitmap
 * <br><br>
 * Created by Daniel Park on 2016-01-14.
 */
public class BitmapUtil {

	/** sets when it failed to get screen width size from device */
	private static final int DEFAULT_WIDTH_FOR_SAMPLING = 1280;
	/** sets when if failed to get screen height size from device */
	private static final int DEFAULT_HEIGHT_FOR_SAMPLING = 1280;
	// MININUM_IMAGE_QUALITY * 100 % = When sampling begins, it maintain the minimum image quality.
	private static final float MININUM_IMAGE_QUALITY = 0.75f; // the percentage of maintain image quality (75% ~ 300 % below [75 * 4] is possible max quality)

	/**
	 * Daniel (2016-01-14 12:27:35): Get Bitmap from InputStream
	 * @param context if it null, default value {@value BitmapUtil#DEFAULT_WIDTH_FOR_SAMPLING}x{@value BitmapUtil#DEFAULT_HEIGHT_FOR_SAMPLING}
	 *                is set when sampling begins
	 * @param in if it null, throw exception
	 * @return Bitmap
	 * @throws Exception when InputStream is null
	 */
	public static Bitmap getBitmap(Context context, InputStream in) throws Exception{
		if(in == null)
			throw new Exception("InputStream is null..");
		return getBitmap(context, in, 0, 0, null);
	}

	public static Bitmap getBitmap(Context context, InputStream in, int reqWidth, int reqHeight) throws Exception{
		if(in == null)
			throw new Exception("InputStream is null..");
		return getBitmap(context, in, reqWidth, reqHeight, null);
	}

	public static Bitmap getBitmap(Context context, InputStream in, BitmapFactory.Options options) throws Exception{
		if(in == null)
			throw new Exception("InputStream is null..");
		return getBitmap(context, in, 0, 0, options);
	}

	/**
	 * Get bitmap from resource id
	 * @param context if it null, throw Exception
	 * @param resId
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, int resId) throws Exception{
		return getBitmap(context, resId, 0, 0, null);
	}

	/**
	 * Get bitmap from resource id
	 * @param context if it null, throw Exception
	 * @param resId
	 * @param reqWidth desired width quality ( if it is invalid value, then apply device width )
	 * @param reqHeight	desired height quality ( if it is invalid value, then apply device height )
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, int resId, int reqWidth, int reqHeight) throws Exception{
		return getBitmap(context, resId, reqWidth, reqHeight, null);
	}

	/**
	 * get bitmap from File
	 * @param context
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, File filePath) throws Exception {
		if(filePath == null || filePath.isDirectory() || !filePath.exists())
			throw new Exception("File path is null or incorrect!");

		return getBitmap(context, filePath.getAbsolutePath(), 0, 0, null);
	}

	public static Bitmap getBitmap(Context context, File filePath, int reqWidth, int reqHeight) throws Exception {
		if(filePath == null || filePath.isDirectory() || !filePath.exists())
			throw new Exception("File path is null or incorrect!");

		return getBitmap(context, filePath.getAbsolutePath(), reqWidth, reqHeight, null);
	}

	/**
	 * get scaled bitmap
	 * @param context
	 * @param bitmap
	 * @param isRecycle	whether recycle previous bitmap or not
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, Bitmap bitmap, boolean isRecycle) throws Exception {
		return getBitmap(context, bitmap, 0, 0, isRecycle);
	}

	/**
	 * get scaled bitmap with size requirement
	 * @param context
	 * @param bitmap
	 * @param reqWidth
	 * @param reqHeight
	 * @param isRecycle
	 * @return
     * @throws Exception
     */
	public static Bitmap getBitmap(Context context, Bitmap bitmap, int reqWidth, int reqHeight, boolean isRecycle) throws Exception {
		int sampleSize = calculateInSampleSize(context, bitmap.getWidth(), bitmap.getHeight(), reqWidth, reqHeight);

		if (sampleSize >= 2) {
			Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / sampleSize, bitmap.getHeight() / sampleSize, false);

			try {
				if (newBitmap != bitmap && !bitmap.isRecycled() && isRecycle)
					bitmap.recycle();
			} catch (Exception e){
				e.printStackTrace();
			}
			return newBitmap;
		} else {
			return bitmap;
		}
	}

	/**
	 * Daniel (2016-01-14 12:25:38): Get bitmap from InputStream (It could be bitmap which sampled by device width * height)
	 * @param context if it null, default value {@value BitmapUtil#DEFAULT_WIDTH_FOR_SAMPLING}x{@value BitmapUtil#DEFAULT_HEIGHT_FOR_SAMPLING}
	 *                is applied to bitmap when it starts sampling
	 * @param in if it null, throw Exception
	 * @param reqWidth	desired width quality ( if it is invalid value, then apply device value )
	 * @param reqHeight	desired height quality ( if it is invalid value, then apply device value )
	 * @param options {@link BitmapFactory.Options} (if it null, then create empty options)
	 * @return Bitmap
	 */
	public static Bitmap getBitmap(Context context, InputStream in, int reqWidth, int reqHeight, BitmapFactory.Options options) throws Exception{
		if(in == null)
			throw new Exception("InputStream is null..");

		byte[] dataArray = getSizeData(in);

		if(options == null)
			options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(dataArray, 0, dataArray.length, options);
		options.inSampleSize = calculateInSampleSize(context, options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeByteArray(dataArray, 0, dataArray.length, options);
	}

	/**
	 * get bitmap from byte[] data array
	 * @param context if it null, default value {@value BitmapUtil#DEFAULT_WIDTH_FOR_SAMPLING}x{@value BitmapUtil#DEFAULT_HEIGHT_FOR_SAMPLING}
	 *                is applied to bitmap when it starts sampling
	 * @param dataArray
	 * @param reqWidth desired width quality ( if it is invalid value, then apply device value )
	 * @param reqHeight desired height quality ( if it is invalid value, then apply device value )
	 * @param options {@link BitmapFactory.Options} (if it null, then create empty options)
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, byte[] dataArray, int reqWidth, int reqHeight, BitmapFactory.Options options) throws Exception{
		if(dataArray == null || dataArray.length == 0)
			throw new Exception("No data for sampling Bitmap..");

		if(options == null)
			options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(dataArray, 0, dataArray.length, options);
		options.inSampleSize = calculateInSampleSize(context, options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeByteArray(dataArray, 0, dataArray.length, options);
	}

	/**
	 * get bitmap from File path
	 * @param context if it null, default value {@value BitmapUtil#DEFAULT_WIDTH_FOR_SAMPLING}x{@value BitmapUtil#DEFAULT_HEIGHT_FOR_SAMPLING}
	 *                is applied to bitmap when it starts sampling
	 * @param filePath File path
	 * @param reqWidth desired width quality ( if it is invalid value, then apply device value )
	 * @param reqHeight desired height quality ( if it is invalid value, then apply device value )
	 * @param options {@link BitmapFactory.Options} (if it null, then create empty options)
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, String filePath, int reqWidth, int reqHeight, BitmapFactory.Options options) throws Exception {
		if(StringUtil.isNullorEmpty(filePath))
			throw new Exception("File path is null or empty..");

		try {
			if (!(new File(filePath).exists())) {
				throw new Exception("File from " +filePath + " is not found..");
			}
		}catch (Exception e){
			throw new Exception("Error while getting file from " + filePath);
		}

		if(options == null)
			options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		options.inSampleSize = calculateInSampleSize(context, options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * get bitmap from resource id
	 * @param context if it null, throw Exception
	 * @param resId	image resource id
	 * @param reqWidth desired width quality ( if it is invalid value, then apply device value )
	 * @param reqHeight desired height quality ( if it is invalid value, then apply device value )
	 * @param options {@link BitmapFactory.Options} (if it null, then create empty options)
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, int resId, int reqWidth, int reqHeight, BitmapFactory.Options options) throws Exception {
		if(context == null)
			throw new Exception("Context can't be null..");

		if(options == null)
			options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), resId, options);
		options.inSampleSize = calculateInSampleSize(context, options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeResource(context.getResources(), resId, options);
	}

	/**
	 * Daniel (2016-01-12 16:23:10): applied my own algorithm <br><br>
	 *
	 * @param options
	 * @return
	 */
	private static int calculateInSampleSize(Context context, BitmapFactory.Options options, int w, int h) {
		return calculateInSampleSize(context, options.outWidth, options.outHeight, w, h);
	}

	private static int calculateInSampleSize(Context context, int bitmapW, int bitmapH, int w, int h) {

		int reqWidth = w;
		int reqHeight = h;

		// Raw height and width of image
		final int height = bitmapH;
		final int width = bitmapW;
		int inSampleSize = 1;

		try {
			Display display = DeviceUtil.getResolutionSize(context);

			if (reqWidth * reqHeight > display.getWidth() * display.getHeight() || reqWidth * reqHeight <= 0) {
				reqWidth = display.getWidth();
				reqHeight = display.getHeight();
			}

		} catch (Exception e) {
			if (reqWidth * reqHeight <= 0 || reqWidth * reqHeight > DEFAULT_WIDTH_FOR_SAMPLING * DEFAULT_HEIGHT_FOR_SAMPLING) {
				reqWidth = DEFAULT_WIDTH_FOR_SAMPLING;
				reqHeight = DEFAULT_HEIGHT_FOR_SAMPLING;
			}
		}

		if (height * width > reqHeight * reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) * (halfWidth / inSampleSize) > reqHeight * reqWidth) {
				inSampleSize *= 2;
			}

			if(((float) ((height / (inSampleSize * 2)) * (width / (inSampleSize * 2))) / (float)(reqHeight * reqWidth)) >= MININUM_IMAGE_QUALITY){
				inSampleSize *= 2;
			}

			if(((float) ((height / inSampleSize) * (width / inSampleSize)) / (float)(reqHeight * reqWidth)) < MININUM_IMAGE_QUALITY){
				inSampleSize /= 2;
			}

			if ((float) (height / inSampleSize) > reqHeight * 2) {
				inSampleSize *= 2;
			}

			if ((float) (width / inSampleSize) > reqWidth * 2.5) {
				inSampleSize *= 2;
			}

			Logger.INSTANCE.d("OKAY", "ratio : " + ((float) ((height / inSampleSize) * (width / inSampleSize)) / (float)(reqHeight * reqWidth)));
		}

		Logger.INSTANCE.d("OKAY", "width : " + width);
		Logger.INSTANCE.d("OKAY", "height : " + height);
		Logger.INSTANCE.d("OKAY", "reqWidth : " + reqWidth);
		Logger.INSTANCE.d("OKAY", "reqHeight : " + reqHeight);
		Logger.INSTANCE.d("OKAY", "inSampleSize : " + inSampleSize);

		return inSampleSize;
	}

	/**
	 * Daniel (2016-01-14 12:11:22): get byte[] from InputStream
	 * @param is
	 * @return
	 */
	private static byte[] getSizeData(InputStream is){
		ByteArrayOutputStream byteBuffer;
		try {
			byteBuffer = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];

			int len = 0;
			while ((len = is.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, len);
			}
			return byteBuffer.toByteArray();
		}catch (Exception e){
			return new byte[]{};
		}
	}
}
