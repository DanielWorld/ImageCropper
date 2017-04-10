package com.danielpark.imagecroppersample.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;


/**
 * Daniel (2016-01-14 11:52:16): 비트맵을 효율적으로 가져오기 위한 유틸리티
 * <br><br>
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-01-14.
 */
public class BitmapUtil {

	/** 기기에서 화면 사이즈 가져오는 데 실패할 경우, 지정하는 기본 너비 값*/
	private static final int DEFAULT_WIDTH_FOR_SAMPLING = 1280;
	/** 기기에서 화면 사이즈 가져오는 데 실패할 경우, 지정하는 기본 높이 값*/
	private static final int DEFAULT_HEIGHT_FOR_SAMPLING = 1280;
	// MININUM_IMAGE_QUALITY * 100 % = 샘플링 시 한계 해상도 대비 최소 보존 비율
	private static final float MININUM_IMAGE_QUALITY = 0.75f; // 이미지 보존 비율 (현재 75% ~ 300 % 미만 [75 * 4] 까지 가능)

	/**
	 * Daniel (2016-01-14 12:27:35): InputStream 으로 부터 비트맵 반환. (기기에 맞게 Bitmap 이 변환되어서 나올 수도 있다.)
	 * @param context null 일경우, 기본값 {@value BitmapUtil#DEFAULT_WIDTH_FOR_SAMPLING}x{@value BitmapUtil#DEFAULT_HEIGHT_FOR_SAMPLING}
	 *                을 기준으로 Bitmap 샘플링.
	 * @param in null 일 경우, Exception 발생
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
	 * Resource Id 으로 비트맵 반환
	 * @param context null 일 경우 Exception 발생
	 * @param resId
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, int resId) throws Exception{
		return getBitmap(context, resId, 0, 0, null);
	}

	/**
	 * Resource Id 으로 비트맵 반환
	 * @param context null 일 경우 Exception 발생
	 * @param resId
	 * @param reqWidth 변환시 원하는 width quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @param reqHeight	변환시 원하는 height quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, int resId, int reqWidth, int reqHeight) throws Exception{
		return getBitmap(context, resId, reqWidth, reqHeight, null);
	}

	/**
	 * File 으로 비트맵 반환
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
	 * Bitmap 새로 scaled 처리
	 * @param context
	 * @param bitmap
	 * @param isRecycle	이전 bitmap recycle 실시 여부
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, Bitmap bitmap, boolean isRecycle) throws Exception {
		int sampleSize = calculateInSampleSize(context, bitmap.getWidth(), bitmap.getHeight(), 0, 0);

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
	 * Daniel (2016-01-14 12:25:38): InputStream 으로 부터 비트맵 반환. (기기에 맞게 Bitmap 이 변환되어서 나올 수도 있다.)
	 * @param context null 일경우, 기본값 {@value BitmapUtil#DEFAULT_WIDTH_FOR_SAMPLING}x{@value BitmapUtil#DEFAULT_HEIGHT_FOR_SAMPLING}
	 *                을 기준으로 Bitmap 샘플링.
	 * @param in null 일 경우, Exception 발생
	 * @param reqWidth	변환시 원하는 width quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @param reqHeight	변환시 원하는 height quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @param options {@link BitmapFactory.Options} (null 일 경우 자체적으로 생성)
	 * @return Bitmap
	 */
	public static Bitmap getBitmap(Context context, InputStream in, int reqWidth, int reqHeight, BitmapFactory.Options options) throws Exception{
		if(in == null)
			throw new Exception("InputStream is null..");

		byte[] dataArray = getSizeData(in);

		// Daniel (2016-01-14 12:22:12): 이미지 Sampling 작업 시작.
		if(options == null)
			options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(dataArray, 0, dataArray.length, options);
		options.inSampleSize = calculateInSampleSize(context, options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		// 끝

		return BitmapFactory.decodeByteArray(dataArray, 0, dataArray.length, options);
	}

	/**
	 * byte[] data array 로 부터 비트맵 반환
	 * @param context null 일경우, 기본값 {@value BitmapUtil#DEFAULT_WIDTH_FOR_SAMPLING}x{@value BitmapUtil#DEFAULT_HEIGHT_FOR_SAMPLING}
	 *                을 기준으로 Bitmap 샘플링.
	 * @param dataArray	Stream 으로 부터 byte[] 를 받아온 값 (이미지 byte[] 값)
	 * @param reqWidth 변환시 원하는 width quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @param reqHeight 변환시 원하는 height quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @param options {@link BitmapFactory.Options} (null 일 경우 자체적으로 생성)
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
	 * File path 로 부터 비트맵 반환
	 * @param context null 일경우, 기본값 {@value BitmapUtil#DEFAULT_WIDTH_FOR_SAMPLING}x{@value BitmapUtil#DEFAULT_HEIGHT_FOR_SAMPLING}
	 *                을 기준으로 Bitmap 샘플링.
	 * @param filePath File path
	 * @param reqWidth 변환시 원하는 width quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @param reqHeight 변환시 원하는 height quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @param options {@link BitmapFactory.Options} (null 일 경우 자체적으로 생성)
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmap(Context context, String filePath, int reqWidth, int reqHeight, BitmapFactory.Options options) throws Exception {
		if(StringUtil.isEmpty(filePath))
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
	 * Resource Id 로 부터 비트맵 반환
	 * @param context null 일 경우, Exception 발생
	 * @param resId	이미지 resource id
	 * @param reqWidth 변환시 원하는 width quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @param reqHeight 변환시 원하는 height quality ( 유효하지 않은 값일 경우, 기기에 맞게 변환된다. )
	 * @param options {@link BitmapFactory.Options} (null 일 경우 자체적으로 생성)
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
	 * Daniel (2016-01-12 16:23:10): 기기별 해상도에 맞게 비트맵 샘플링 처리 <br><br>
	 *
	 * 1. 기기의 전체 해상도를 구한다. 예) 1024x1928 <br> 만약 기기의 전체 해상도를 구하는 데 실패할 경우 {@value BitmapUtil#DEFAULT_WIDTH_FOR_SAMPLING}x{@value BitmapUtil#DEFAULT_HEIGHT_FOR_SAMPLING} <br>
	 * 2. 만약 지정된 width, height 값이 들어온다면 ( > 0) 기기의 해상도를 대체한다. 단 해상도 보다 크게 지정할 경우 메모리 낭비 가능성 및 OOM 발생 방지를 위해 해상도로 대체!
	 * 3. Bitmap 새로운 가로 크기 = Bitmap 가로 반 크기, Bitmap 새로운 세로 크기 = Bitmap 세로 반 크기
	 * 3. (Bitmap 새로운 가로 크기/현재 샘플링 값) * (Bitmap 새로운 세로 크기/현재 샘플링 값) > 기기 전체 해상도 (가로 x 세로) 일 경우
	 * + 해당 (Bitmap 새로운 가로 크기/현재 샘플링 값) * (Bitmap 새로운 세로 크기/현재 샘플링 값) / 기기 전체 해상도 (가로 x 세로) > 1.5 이상일 경우
	 * 현재 샘플링 값 *= 2 하여, 위 반복문이 성립하지 않을 때 까지 시도.
	 * 4. 샘플링시 최소 보장 화질 (기준별) 추가 : 현재 최소 75% ~ 최대 149%
	 * 5. 해당 샘플링 값 적용된 비트맵 return
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

			// Daniel (2016-03-16 15:23:11): 유저가 이미지 임의 크기를 지정했더라도, 화면 해상도 보다 클 수는 없다...
			// 또한 0보다 무조건 큰 유효 값이어야 한다.
			if (reqWidth * reqHeight > display.getWidth() * display.getHeight() || reqWidth * reqHeight <= 0) {
				reqWidth = display.getWidth();
				reqHeight = display.getHeight();
			}

		} catch (Exception e) {
			// Daniel (2016-01-12 17:43:39): 현재 화면 사이즈를 반환하는 데 실패할 경우,
			// 1. 기존의 받은 값이 유효할 경우 그 값을,
			// 2. 아닐 경우, 최대 가로/세로 크기를 기본값 이하로 지정.
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

			// Daniel (2016-01-26 11:35:42): 만약 현재 이미지에서 한번 더 샘플링해서 나온 비율이 최소 비율을 맞춘다면 샘플링을 해줘도 된다.
			if(((float) ((height / (inSampleSize * 2)) * (width / (inSampleSize * 2))) / (float)(reqHeight * reqWidth)) >= MININUM_IMAGE_QUALITY){
				inSampleSize *= 2;
			}

			// Daniel (2016-01-26 11:14:34): 최종 샘플링 결정될 시 이미지가 최소 비율을 보전해야 한다.
			if(((float) ((height / inSampleSize) * (width / inSampleSize)) / (float)(reqHeight * reqWidth)) < MININUM_IMAGE_QUALITY){
				inSampleSize /= 2;
			}

			// Daniel (2016-05-16 17:13:33): Bitmap 크기가 Texture max size 를 넘을 경우, render 가 안되는 오류가 발생!
			// 현재 sample size 를 적용한 높이가 제한 높이의 2배를 넘는다면 샘플링 처리
			if ((float) (height / inSampleSize) > reqHeight * 2) {
				inSampleSize *= 2;
			}

			// 현재 sample size 를 적용한 너비가 제한 너비의 2.5 배를 넘는다면 샘플링 처리
			if ((float) (width / inSampleSize) > reqWidth * 2.5) {
				inSampleSize *= 2;
			}

			Log.d("OKAY", "ratio : " + ((float) ((height / inSampleSize) * (width / inSampleSize)) / (float)(reqHeight * reqWidth)));
		}

		Log.d("OKAY", "width : " + width);
		Log.d("OKAY", "height : " + height);
		Log.d("OKAY", "reqWidth : " + reqWidth);
		Log.d("OKAY", "reqHeight : " + reqHeight);
		Log.d("OKAY", "inSampleSize : " + inSampleSize);

		return inSampleSize;
	}

	/**
	 * Daniel (2016-01-14 12:11:22): InputStream 으로 부터 byte[] 얻어냄.
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

	/**
	 * 해당 Url 을 참고함. Bitmap 에 음영 추가하기
	 * http://stackoverflow.com/questions/23657811/how-to-mask-bitmap-with-lineargradient-shader-properly
	 * <br><br>
	 *     PorterDuff.Mode 관련 내용 <br>
	 *     http://baramziny.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EB%B9%84%ED%8A%B8%EB%A7%B5-%EC%85%B0%EC%9D%B4%EB%8D%94%EC%99%80-composeShader
	 * @param src
	 * @param filterColor
	 * @return
	 */
	public static Bitmap addGradient(Bitmap src, int filterColor){

		int w = src.getWidth();
		int h = src.getHeight();
//		Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Bitmap overlay = src.copy(src.getConfig(), true);
		Canvas canvas = new Canvas(overlay);

//		canvas.drawBitmap(src, 0, 0, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, 0, w, h, filterColor, filterColor, Shader.TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawRect(0, 0, w, h, paint);

		return overlay;
	}

	/**
	 * ImageView 에 원본 Bitmap + 특정 색의 음영을 추가한 Bitmap 설정
	 * @param iv			적용할 ImageView
	 * @param srcBitmap		원본 Bitmap
	 * @param filterColor	음영 색
	 */
	public static void addGradient(ImageView iv, Bitmap srcBitmap, int filterColor) throws Exception{
		Bitmap gradientBitmap = addGradient(srcBitmap, filterColor);

		try {
			// Daniel (2016-03-21 17:06:04): Gradient 추가
//			Bitmap currentBitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
//
//			if (currentBitmap != null && !currentBitmap.isRecycled() && gradientBitmap != currentBitmap)
//				currentBitmap.recycle();

			if(srcBitmap != null && !srcBitmap.isRecycled() && gradientBitmap != srcBitmap)
				srcBitmap.recycle();

		}catch (Exception e){
			// Daniel (2016-03-21 17:32:51): Recycle 부분에서 문제 발생시 음영을 추가한 Bitmap 그냥 View 에 설정할 것!
			e.printStackTrace();
		}

		iv.setImageBitmap(gradientBitmap);
	}


	/**
	 * Bitmap 에 blur 효과 주기 <br>
	 *     API 16 이상 버전에서만 적용 <br>
	 *     http://www.kmshack.kr/2013/08/flat%EB%94%94%EC%9E%90%EC%9D%B8%EC%9D%98-%ED%95%B5%EC%8B%AC-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%9D%B4%EB%AF%B8%EC%A7%80-blur-%ED%9A%A8%EA%B3%BC-%EB%82%B4%EA%B8%B0/
	 * @param ctx
	 * @param src
	 * @return
	 */
	public static Bitmap addBlur(Context ctx, Bitmap src){
		if(ctx == null || src == null)
			return src;

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			Bitmap bitmap = src.copy(src.getConfig(), true);

			final RenderScript rs = RenderScript.create(ctx);
			final Allocation input = Allocation.createFromBitmap(rs, src, Allocation.MipmapControl.MIPMAP_NONE,
					Allocation.USAGE_SCRIPT);
			final Allocation output = Allocation.createTyped(rs, input.getType());
			final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//			script.setRadius(radius); //0.0f ~ 25.0f
			script.setInput(input);
			script.forEach(output);
			output.copyTo(bitmap);
			return bitmap;
		}
		return src;
	}
}
