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

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by Namgyu Park on 2016-06-28.
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

