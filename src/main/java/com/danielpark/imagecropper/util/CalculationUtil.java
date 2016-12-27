package com.danielpark.imagecropper.util;

/**
 * Copyright (C) 2014-2016 daniel@bapul.net
 * Created by Daniel on 2016-12-23.
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
}
