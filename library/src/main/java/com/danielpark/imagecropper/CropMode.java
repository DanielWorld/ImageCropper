package com.danielpark.imagecropper;

/**
 * Set crop mode before editing a picture <br>
 *     <b> NONE : </b> No crop mode <br>
 *     <b> CROP : </b> Normal Crop <br>
 *     <b> CROP_SHRINK : </b> Crop but image should be shrink <br>
 *     <b> CROP_STRETCH : </b> Crop but stretch image
 *
 * <br><br>
 * Created by Daniel Park on 2016-06-24.
 */
public enum CropMode {
    NONE, CROP, CROP_SHRINK, CROP_STRETCH
}
