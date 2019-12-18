package com.danielpark.imagecropper.model;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by Daniel Park on 2016-06-28.
 */
public class DrawInfo {
    Path path;
    Paint paint;

    public DrawInfo(Path path, Paint paint) {
        this.path = path;
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    public Paint getPaint() {
        return paint;
    }
}

