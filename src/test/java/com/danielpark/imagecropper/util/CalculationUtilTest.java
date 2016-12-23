package com.danielpark.imagecropper.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyright (C) 2014-2016 daniel@bapul.net
 * Created by Daniel on 2016-12-23.
 */

public class CalculationUtilTest {

    @Test
    public void isInsideCircleTest() {

        float targetX = 0;
        float targetY = 5;

        float centerX = 0;
        float centerY = 0;

        float radius = 3;

        assertEquals(CalculationUtil.isInsideCircle(targetX, targetY, centerX, centerY, radius), false);

        radius = 6;
        assertEquals(CalculationUtil.isInsideCircle(targetX, targetY, centerX, centerY, radius), true);

        targetX = 3;
        targetY = 5.0f;
        assertEquals(CalculationUtil.isInsideCircle(targetX, targetY, centerX, centerY, radius), true);
    }
}
