package com.danielpark.imagecroppersample

import android.app.Application
import android.os.StrictMode


/**
 * Created by Daniel Park on 2019-12-19
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