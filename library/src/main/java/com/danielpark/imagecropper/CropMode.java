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
package com.danielpark.imagecropper;

/**
 * Set crop mode before editing a picture <br>
 *     <b> NONE : </b> No crop mode <br>
 *     <b> CROP : </b> Normal Crop <br>
 *     <b> CROP_SHRINK : </b> Crop but image should be shrink <br>
 *     <b> CROP_STRETCH : </b> Crop but stretch image
 *
 * <br><br>
 * Created by Namgyu Park on 2016-06-24.
 */
public enum CropMode {
    NONE, CROP, CROP_SHRINK, CROP_STRETCH
}
