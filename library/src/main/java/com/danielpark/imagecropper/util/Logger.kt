package com.danielpark.imagecropper.util

import android.util.Log
import com.danielpark.imagecropper.BuildConfig

/**
 * print log with specific information.
 * <br><br>
 * Created by Daniel Park on 2019-12-18
 */
object Logger {

    private val ENABLE_LOGGER : Boolean = BuildConfig.DEBUG

    private fun buildLogMsg(message: String): String {
        val ste = Thread.currentThread().stackTrace[4]
        val sb = StringBuilder()
        sb.append("(thread : ")
        sb.append(Thread.currentThread().name)
        sb.append(")")
        sb.append("[[")
        sb.append(ste.fileName)
        sb.append(">")
        sb.append(ste.methodName)
        sb.append(">#")
        sb.append(ste.lineNumber)
        sb.append("]] ")
        sb.append(message)
        return sb.toString()
    }

    fun v(tag: String, msg: String) {
        if (ENABLE_LOGGER) {
            Log.v(tag, buildLogMsg(msg))
        }

    }

    fun d(tag: String, msg: String) {
        if (ENABLE_LOGGER) {
            Log.d(tag, buildLogMsg(msg))
        }

    }

    fun w(tag: String, msg: String) {
        if (ENABLE_LOGGER) {
            Log.w(tag, buildLogMsg(msg))
        }

    }

    fun w(tag: String, msg: String, throwable: Throwable) {
        if (ENABLE_LOGGER) {
            Log.w(tag, buildLogMsg(msg), throwable)
        }

    }

    fun w(tag: String, throwable: Throwable) {
        if (ENABLE_LOGGER) {
            Log.w(tag, throwable)
        }

    }

    fun e(tag: String, msg: String) {
        if (ENABLE_LOGGER) {
            Log.e(tag, buildLogMsg(msg))
        }

    }

    fun e(tag: String, msg: String, throwable: Throwable) {
        if (ENABLE_LOGGER) {
            Log.e(tag, buildLogMsg(msg), throwable)
        }

    }

    fun e(tag: String, throwable: Throwable) {
        if (ENABLE_LOGGER) {
            Log.e(tag, "", throwable)
        }

    }

    fun i(tag: String, msg: String) {
        if (ENABLE_LOGGER) {
            Log.i(tag, buildLogMsg(msg))
        }

    }
}