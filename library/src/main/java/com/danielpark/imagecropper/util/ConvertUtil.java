package com.danielpark.imagecropper.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by Daniel Park on 2016-02-05.
 */
public class ConvertUtil {

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static int convertDpToPixel(float dp) {
        DisplayMetrics metrics;
        try {
            metrics = Resources.getSystem().getDisplayMetrics();
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    metrics);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Daniel (2016-01-05 17:07:11): Convert Pixel to DP <br>
     *     ex) pixel = value(dp) * metrics.density; <br>
     *     pixel / metrics.density = value(dp);
     * @param pixel
     * @return
     */
    public static int convertPixelToDp(int pixel){
        DisplayMetrics metrics;
        try{
            metrics = Resources.getSystem().getDisplayMetrics();
            return Math.round(pixel / metrics.density);
        }catch (Exception e){
            return 0;
        }
    }
}
