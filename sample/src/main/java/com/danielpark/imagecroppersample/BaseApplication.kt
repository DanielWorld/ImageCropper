/*
 * Copyright (c) 2019 DanielWorld.
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
package com.danielpark.imagecroppersample

import android.app.Application
import android.os.StrictMode


/**
 * Created by Namgyu Park on 2019-12-19
 */
class BaseApplication : Application() {

    override fun onCreate() {

        // @namgyu.park (2019-12-19) :
        // https://developer.android.com/reference/android/os/StrictMode.html
        if (BuildConfig.DEBUG) {

            // Set thread, vm policy to StrictMode
            // https://developer.android.com/reference/android/os/StrictMode.ThreadPolicy.html
            StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            // Strict mode's disk violation is too heavy. skip it.
                            .permitDiskReads()
                            .penaltyLog()
//                            .penaltyDeath()
                            .build()
            )

            // https://developer.android.com/reference/android/os/StrictMode.VmPolicy.html
            StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
//                            .penaltyDeath()
                            .build()
            )
        }
        super.onCreate()
    }

}