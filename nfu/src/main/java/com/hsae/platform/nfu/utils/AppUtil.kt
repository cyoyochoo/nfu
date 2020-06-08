package com.hsae.platform.nfu.utils

import android.app.Application
import java.lang.reflect.Field
import java.lang.reflect.Method

object AppUtil {

    @JvmField var application: Application? = null

    init {
        try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val currentActivityThread: Method =
                activityThread.getDeclaredMethod("currentActivityThread")
            val mInitialApplication: Field =
                activityThread.getDeclaredField("mInitialApplication")
            mInitialApplication.isAccessible = true
            val current: Any = currentActivityThread.invoke(null)
            val app: Any = mInitialApplication.get(current)
            application = app as? Application
        } catch (e: Exception) {
            LogUtil.e(e.toString())
        }
    }
}