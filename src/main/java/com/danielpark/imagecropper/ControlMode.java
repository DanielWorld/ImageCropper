package com.danielpark.imagecropper;

/**
 * Set crop control mode before editing a picture <br>
 *     <b> NONE : </b> Do nothing (it only works when {@link CropMode#NONE}) <br>
 *     <b> FREE : </b> You control points without any limited shape <br>
 *     <b> FIXED : </b> You can control point but its original shape is nearly fixed
 *
 * <br><br>
 * Copyright (c) 2014-2016 op7773hons@gmail.com
 * Created by Daniel Park on 2016-10-08.
 */
public enum ControlMode {
    NONE, FREE, FIXED
}
