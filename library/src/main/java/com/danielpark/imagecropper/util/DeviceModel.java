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
package com.danielpark.imagecropper.util;

import android.graphics.Bitmap;
import android.os.Build;

import com.danielpark.imagecropper.CropperImageView;

/**
 * There are certain devices which cannot distinguish image rectangle <br>
 *     in this case, library calls twice when setBitmap <br>
 *         If you guys know anything, help us out!
 *     <br><br>
 * Created by Namgyu Park on 2016-07-01.
 */
public class DeviceModel {

    /**
     * Some devices need to be called {@link CropperImageView#setCustomImageBitmap(Bitmap)} or {@link CropperImageView#setCustomImageBitmap(Bitmap, int)} twice
     * <br> call first and then waits for 1 second and call another
     * @return
     */
    public static boolean isDeviceCallTwice() {
//        String[] Nexus5XLG = {"H790", "H791", "H798", "NEXUS 5X"};
//        String[] Nexus5LG = {"D820", "D821", "NEXUS 5"};
//        String[] Nexus6Motorola = {"XT1100", "XT1103", "NEXUS 6"};
//        String[] Nexus6PHuawei = {"H1511", "H1512", "NEXUS 6P"};
//
//        if (Build.MANUFACTURER.toUpperCase().contains("LG")) {
//            String currentModel = Build.MODEL.toUpperCase();
//            for (String model : Nexus5XLG) {
//                if (currentModel.contains(model))
//                    return true;
//            }
//            for (String model : Nexus5LG) {
//                if (currentModel.contains(model))
//                    return true;
//            }
//        }
//
//        if (Build.MANUFACTURER.toUpperCase().contains("MOTOROLA")) {
//            String currentModel = Build.MODEL.toLowerCase();
//            for (String model : Nexus6Motorola) {
//                if (currentModel.contains(model))
//                    return true;
//            }
//        }
//
//        if (Build.MANUFACTURER.toUpperCase().contains("HUAWEI")) {
//            String currentModel = Build.MODEL.toLowerCase();
//            for (String model : Nexus6PHuawei) {
//                if (currentModel.contains(model))
//                    return true;
//            }
//        }

        return true;
    }
}
