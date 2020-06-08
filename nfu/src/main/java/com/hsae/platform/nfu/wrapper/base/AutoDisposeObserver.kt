package com.hsae.platform.nfu.wrapper.base

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class AutoDisposeObserver<T>
    (private var source: ObservableSource<T>?, private var lifecycle: Lifecycle?)
    : InternalObserver<T> {

    private val defaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            upstream?.disposeInternal()
        }
    }
    private var upstream: InternalDisposable? = null
    private var internalObserver: InternalObserver<in T>? = null
    init {
        lifecycle?.addObserver(defaultLifecycleObserver)
    }

    fun subscribeInternal(internalObserver: InternalObserver<in T>) {
        this.internalObserver = internalObserver
        source?.subscribeInternal(this)
    }

    override fun onSubscribe(upstream: InternalDisposable, task: String) {
        internalObserver?.onSubscribe(upstream, task)
        this.upstream = upstream
    }

    override fun disposeInternal() {
        internalObserver?.disposeInternal()
        lifecycle?.removeObserver(defaultLifecycleObserver)
        source = null
        lifecycle = null
        upstream = null
        internalObserver = null
    }

    override fun onAction(t: T) {
        internalObserver?.onAction(t)
    }
}