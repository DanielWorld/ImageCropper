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
package com.danielworld.imagecropper.model;

import com.danielworld.imagecropper.ControlMode;
import com.danielworld.imagecropper.CropExtension;
import com.danielworld.imagecropper.CropMode;
import com.danielworld.imagecropper.ShapeMode;
import com.danielworld.imagecropper.UtilMode;

/**
 * Builder for crop setting
 * <br><br>
 * Created by Namgyu Park on 2016-12-27.
 */

public class CropSetting {

    private final CropMode cropMode;
    private final ShapeMode shapeMode;
    private final ControlMode controlMode;
    private final UtilMode utilMode;
    private final CropExtension cropExtension;

    private final float cropInsetRatio;
    private final float thumbnailSizeRatio;

    private CropSetting(CropBuilder builder) {
        cropMode = builder.cropMode;
        shapeMode = builder.shapeMode;
        controlMode = builder.controlMode;
        utilMode = builder.utilMode;
        cropExtension = builder.cropExtension;

        cropInsetRatio = builder.cropInsetRatio;
        thumbnailSizeRatio = builder.thumbnailSizeRatio;
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

    public CropExtension getCropExtension() {
        return cropExtension;
    }

    public float getCropInsetRatio() {
        return cropInsetRatio;
    }

    public float getThumbnailSizeRatio() {
        return thumbnailSizeRatio;
    }

    public static class CropBuilder {
        private final CropMode cropMode;
        private ShapeMode shapeMode;
        private ControlMode controlMode;
        private UtilMode utilMode;
        private CropExtension cropExtension;

        private float cropInsetRatio = 20f;   // it should be between 10f and 90f
        private float thumbnailSizeRatio = 30f;     // it should be between 1f and 100f

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

        public CropBuilder setCropExtension(CropExtension cropExtension) {
            this.cropExtension = cropExtension;
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

        /**
         * Set thumbnail size percentage of original image
         * <p>It should be between 1% and 100% </p>
         * default value is 30%
         * @param thumbnailSizePercent
         * @return
         */
        public CropBuilder setThumbnailSizeRatio(float thumbnailSizePercent) {
            if (thumbnailSizePercent < 1f || thumbnailSizePercent > 100f) {
                this.thumbnailSizeRatio = 30f;
                return this;
            }

            this.thumbnailSizeRatio = thumbnailSizePercent;
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

            if (checkNullNone(cropExtension))
                cropExtension = CropExtension.jpg;

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
