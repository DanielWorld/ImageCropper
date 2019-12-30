/*
 * Copyright (c) 2017 DanielWorld.
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
package com.danielpark.imageeditor;

/**
 * Set editor mode after setting a picture <br>
 *     <b> NONE : Nothing! </b>
 *     <b> EDIT : </b> Image Move / Resize mode <br>
 *     <b> PEN : </b> Pen mode <br>
 *     <b> ERASER : </b> Eraser mode <br>
 * <br><br>
 * Created by Namgyu Park on 2017-07-26.
 */

public enum EditorMode {
    NONE, EDIT, PEN, ERASER
}
