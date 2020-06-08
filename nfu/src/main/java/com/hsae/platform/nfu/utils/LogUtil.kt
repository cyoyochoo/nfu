package com.hsae.platform.nfu.utils

import android.util.Log
import com.hsae.platform.nfu.BuildConfig

object LogUtil {

    private const val TAG = "tank"
    @JvmField var log = BuildConfig.DEBUG

    @JvmStatic @JvmOverloads fun d(info: String, tag: String = TAG) {
        if (log) Log.d(tag, info)
    }

    @JvmStatic @JvmOverloads fun i(info: String, tag: String = TAG) {
        if (log) Log.i(tag, info)
    }

    @JvmStatic @JvmOverloads fun e(info: String, tag: String = TAG) {
        if (log) Log.e(tag, info)
    }
}