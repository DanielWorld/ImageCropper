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

import android.graphics.Point;
import android.util.Pair;

/**
 * Created by Namgyu Park on 2016-12-23.
 */

public class CalculationUtil {

    /**
     * Check if x, y coordinates are inside of circle which has radius and center x, y
     * @param x         target x coordinates
     * @param y         target y coordinates
     * @param centerX   center x coordinates of CIRCLE
     * @param centerY   center y coordinates of CIRCLE
     * @param radius    radius of CIRCLE
     * @return  <b>true</b> if x, y coordinates are inside of circle
     */
    public static boolean isInsideCircle(float x, float y, float centerX, float centerY, float radius) {
        return Math.sqrt(Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2)) <= radius;
    }

    /**
     * Make target x, target y into line between (x1, y1) and (x2, y2)
     * @param x1            first x coordinates
     * @param y1            first y coordinates
     * @param x2            second x coordinates
     * @param y2            second y coordinates
     * @param targetX       target x coordinates
     * @param targetY       target y coordinates
     * @param errorRange    how much wrong values from target x or y you allow?
     * @return {@link Pair} which contains rectified target x,y coordinates
     */
    public static Pair<Float, Float> rectifyOnProportionalLine(float x1, float y1, float x2, float y2,
                                                               float targetX, float targetY, float errorRange) {

        float tempX = calculateEquationOfLine(x1, y1, x2, y2, targetY, false);
        float tempY = calculateEquationOfLine(x1, y1, x2, y2, targetX, true);

        if (Math.abs(tempX - targetX) < Math.abs(tempY - targetY)) {
            // Daniel (2017-01-12 14:03:04): temp X is valid
            return new Pair<>(tempX, targetY);
        } else {
            // Daniel (2017-01-12 14:05:07): temp Y is valid
            return new Pair<>(targetX, tempY);
        }
    }

    /**
     * Make target x, target y into line between (x1, y1) and (x2, y2)
     * @param x1            first x coordinates
     * @param y1            first y coordinates
     * @param x2            second x coordinates
     * @param y2            second y coordinates
     * @param targetX       target x coordinates
     * @param targetY       target y coordinates
     * @return rectified target x coordinates
     */
    public static float rectifyOnProportionalLineX(float x1, float y1, float x2, float y2,
                                                   float targetX, float targetY) {

        float tempX = calculateEquationOfLine(x1, y1, x2, y2, targetY, false);
        float tempY = calculateEquationOfLine(x1, y1, x2, y2, targetX, true);

        if (Math.abs(tempX - targetX) < Math.abs(tempY - targetY)) {
            // Daniel (2017-01-12 14:03:04): temp X is valid
            return tempX;
        } else {
            // Daniel (2017-01-12 14:05:07): temp Y is valid
            return targetX;
        }
    }
    /**
     * Make target x, target y into line between (x1, y1) and (x2, y2)
     * @param x1            first x coordinates
     * @param y1            first y coordinates
     * @param x2            second x coordinates
     * @param y2            second y coordinates
     * @param targetX       target x coordinates
     * @param targetY       target y coordinates
     * @return rectified target x coordinates
     */
    public static float rectifyOnProportionalLineY(float x1, float y1, float x2, float y2,
                                                   float targetX, float targetY) {

        float tempX = calculateEquationOfLine(x1, y1, x2, y2, targetY, false);
        float tempY = calculateEquationOfLine(x1, y1, x2, y2, targetX, true);

        if (Math.abs(tempX - targetX) < Math.abs(tempY - targetY)) {
            // Daniel (2017-01-12 14:03:04): temp X is valid
            return targetY;
        } else {
            // Daniel (2017-01-12 14:05:07): temp Y is valid
            return tempY;
        }
    }

    /**
     * Get the result of equation of line <br>
     *     Daniel (2017-01-12 12:18:53): equation of line
     * <p>y - y1 = (y2 - y1) / (x2 - x1) * (x - x1)</p>
     * @param x1            first x coordinates
     * @param y1            first y coordinates
     * @param x2            second x coordinates
     * @param y2            second y coordinates
     * @param target        target coordinates
     * @param isTargetX           is target x coordinates?
     * @return  <b>x</b> : if isTargetX is false. <b>y</b> : if isTargetX is true.
     */
    public static float calculateEquationOfLine(float x1, float y1, float x2, float y2, float target, boolean isTargetX) {

        if (isTargetX) {
            // Daniel (2017-01-12 12:23:27): calculate y
            return (y2 - y1) / (x2 - x1) * (target - x1) + y1;
        } else {
            // Daniel (2017-01-12 12:23:33): calculate x
            return (x2 - x1) / (y2 - y1) * (target - y1) + x1;
        }
    }
}
