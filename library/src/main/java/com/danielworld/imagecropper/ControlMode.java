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
 * Set crop control mode before editing a picture <br>
 *     <b> NONE : </b> Do nothing (it only works when {@link CropMode#NONE}) <br>
 *     <b> FREE : </b> You control points without any limited shape <br>
 *     <b> FIXED : </b> You can control point but its original shape is nearly fixed
 *
 * <br><br>
 * Copyright (c) 2014-2016 op7773hons@gmail.com
 * Created by Namgyu Park on 2016-10-08.
 */
public enum ControlMode {
    NONE, FREE, FIXED
}
