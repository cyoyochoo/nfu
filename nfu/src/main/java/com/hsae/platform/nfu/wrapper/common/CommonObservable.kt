package com.hsae.platform.nfu.wrapper.common

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.hsae.platform.nfu.utils.AppUtil
import com.hsae.platform.nfu.wrapper.TASK_LINK_CHECK
import com.hsae.platform.nfu.wrapper.base.InternalObserver
import com.hsae.platform.nfu.wrapper.base.Observable
import java.util.concurrent.TimeUnit

class CommonObservable<T>(vararg args: String) : Observable<T>() {

    private val args = args.toList()
    private val commonObserver = CommonObserver<T>()

    override fun subscribeInternal(internalObserver: InternalObserver<in T>) {
        if (!checkDebounce(args[0])) return
        commonObserver.onSubscribe(internalObserver, args[0])
        dispatchTask()
    }

    private fun dispatchTask() {
        ObserverCenter.register(args[0]).with(commonObserver)
        AppUtil.application?.sendBroadcast(
            Intent(args[0]).setPackage(AppUtil.application?.packageName).apply {
                if (args[0] == TASK_LINK_CHECK) putExtra("ip", args[1])
            }
        )
    }

    companion object {

        private const val defaultDebounce = 1000L // 1 sec - TimeUnit.MILLISECONDS
        private val debounceRecords : MutableMap<String, Long> = mutableMapOf()
        private val runTimeRecords : MutableMap<String, Long> = mutableMapOf()

        fun debounce(task: String, duration: Long, unit: TimeUnit) {
            debounceRecords[task] = unit.toMillis(duration)
            Handler(Looper.getMainLooper()).postDelayed(
                { debounceRecords.remove(task) },
                unit.toMillis(duration)
            )
        }

        fun checkDebounce(task: String): Boolean {
            var check = true
            val runTime = SystemClock.elapsedRealtime()
            runTimeRecords[task]?.let {
                val debounceTime = debounceRecords[task] ?: defaultDebounce
                if (runTime - it < debounceTime) check = false
                else runTimeRecords[task] = runTime
            } ?: run {
                runTimeRecords[task] = runTime
            }

            return check
        }
    }
}