package com.hsae.platform.nfu.wrapper.base

import android.os.Handler
import android.os.Looper

class InternalObserverImpl<T>(var observer: Observer<in T>?)
    : InternalObserver<T>, Disposable {

    private val uiHandler = Handler(Looper.getMainLooper())
    private var upstream: InternalDisposable? = null

    override fun onSubscribe(upstream: InternalDisposable, task: String) {
        this.upstream = upstream
    }

    override fun disposeInternal() {
        uiHandler.removeCallbacksAndMessages(null)
        observer = null
        upstream = null
    }

    override fun onAction(t: T) {
        observer?.let { uiHandler.post { it.onAction(t) } }
    }

    override fun dispose() {
        upstream?.disposeInternal()
    }
}