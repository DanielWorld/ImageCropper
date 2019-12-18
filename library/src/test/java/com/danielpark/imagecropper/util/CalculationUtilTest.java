package com.danielpark.imagecropper.util;

import android.util.Pair;

import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertEquals;

/**
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

    @Test
    public void rectifyOnProportionalLineTest() {
        float x1 = 1.2f;
        float y1 = -2.4f;

        float x2 = 3.2f;
        float y2 = -1.3f;

        float targetX = 5.3f;
        float targetY = -3.6f;

        // Daniel (2017-01-12 14:17:19): Sorry, this Pair is null for now!

//        Pair<Float, Float> result = CalculationUtil.rectifyOnProportionalLine(x1, y1, x2, y2, targetX, targetY, 1.0f);
//
//        assertEquals(5.3f, CalculationUtil.rectifyOnProportionalLine(x1, y1, x2, y2, targetX, targetY, 0).first);
//        assertEquals(-3.6f, CalculationUtil.rectifyOnProportionalLine(x1, y1, x2, y2, targetX, targetY, 0).second);
    }

    @Test
    public void calculateEquationOfLineTest() {
        float x1 = 1.2f;
        float y1 = -2.4f;

        float x2 = 3.2f;
        float y2 = -1.3f;

        assertEquals(x1, CalculationUtil.calculateEquationOfLine(x1, y1, x2, y2, y1, false));
        assertEquals(y1, CalculationUtil.calculateEquationOfLine(x1, y1, x2, y2, x1, true));

        assertEquals(x2, CalculationUtil.calculateEquationOfLine(x1, y1, x2, y2, y2, false));
        assertEquals(y2, CalculationUtil.calculateEquationOfLine(x1, y1, x2, y2, x2, true));
    }
}
