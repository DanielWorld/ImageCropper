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
package com.danielworld.imagecropper;

/**
 * Set crop shape before editing a picture <br>
 *     <b> NONE : </b> Do nothing (it only works when {@link CropMode#NONE}) <br>
 *     <b> RECTANGLE : </b> Crop shape is rectangle <br>
 *     <b> CIRCLE : </b> Crop shape is circle <br>
 *
 * <br><br>
 * Created by Namgyu Park on 2016-12-21.
 */

public enum ShapeMode {
    NONE, RECTANGLE, CIRCLE
}
