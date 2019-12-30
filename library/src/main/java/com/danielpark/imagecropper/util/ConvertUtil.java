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
package com.danielpark.imagecropper.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by Namgyu Park on 2016-02-05.
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
