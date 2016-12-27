package com.danielpark.imagecropper.model;

import com.danielpark.imagecropper.ControlMode;
import com.danielpark.imagecropper.CropMode;
import com.danielpark.imagecropper.ShapeMode;
import com.danielpark.imagecropper.UtilMode;

/**
 * Builder for crop setting
 * <br><br>
 * Copyright (C) 2014-2016 daniel@bapul.net
 * Created by Daniel on 2016-12-27.
 */

public class CropSetting {

    private final CropMode cropMode;
    private final ShapeMode shapeMode;
    private final ControlMode controlMode;
    private final UtilMode utilMode;

    private final float cropInsetRatio;

    private CropSetting(CropBuilder builder) {
        cropMode = builder.cropMode;
        shapeMode = builder.shapeMode;
        controlMode = builder.controlMode;
        utilMode = builder.utilMode;

        cropInsetRatio = builder.cropInsetRatio;
    }

    public CropMode getCropMode() {
        return cropMode;
    }

    public ShapeMode getShapeMode() {
        return shapeMode;
    }

    public ControlMode getControlMode() {
        return controlMode;
    }

    public UtilMode getUtilMode() {
        return utilMode;
    }

    public float getCropInsetRatio() {
        return cropInsetRatio;
    }

    public static class CropBuilder {
        private final CropMode cropMode;
        private ShapeMode shapeMode;
        private ControlMode controlMode;
        private UtilMode utilMode;

        private float cropInsetRatio = 20f;   // it should be between 10f and 90f

        public CropBuilder(CropMode cropMode) {
            this.cropMode = cropMode;
        }

        public CropBuilder setShapeMode(ShapeMode shapeMode) {
            this.shapeMode = shapeMode;
            return this;
        }

        public CropBuilder setControlMode(ControlMode controlMode) {
            this.controlMode = controlMode;
            return this;
        }

        public CropBuilder setUtilMode(UtilMode utilMode) {
            this.utilMode = utilMode;
            return this;
        }

        /**
         * set crop inset percent. it doesn't work when {@link CropMode#NONE}
         *      <p>It should be between 10% and 90% </p>
         *      default value 20%
         * @param cropPercent   crop inset percent
         * @return
         */
        public CropBuilder setCropInsetRatio(float cropPercent) {
            if (cropPercent < 10f || cropPercent > 90f) {
                this.cropInsetRatio = 20f;
                return this;
            }

            this.cropInsetRatio = cropPercent;
            return this;
        }

        public CropSetting build() {
            if (checkNullNone(cropMode)) {
                shapeMode = ShapeMode.NONE;
                controlMode = ControlMode.NONE;
            }
            // Daniel (2016-12-27 11:33:05): Crop mode is available
            else {
                if (checkNullNone(shapeMode))
                    shapeMode = ShapeMode.RECTANGLE;

                if (checkNullNone(controlMode))
                    controlMode = ControlMode.FIXED;

                utilMode = UtilMode.NONE;
            }

            return new CropSetting(this);
        }

        private boolean checkNullNone(Object obj) {
            if (obj == null) return true;

            if (obj instanceof CropMode)
                return obj == CropMode.NONE;

            if (obj instanceof ShapeMode)
                return obj == ShapeMode.NONE;

            if (obj instanceof ControlMode)
                return obj == ControlMode.NONE;

//            if (obj instanceof UtilMode)
//                return obj == UtilMode.NONE;
            return obj instanceof UtilMode && obj == UtilMode.NONE;
        }
    }
}
