package com.hsae.platform.nfu.wrapper.base

import com.hsae.platform.nfu.wrapper.common.CommonObservable
import java.util.concurrent.TimeUnit

class DebounceObservable<T>
    (source: ObservableSource<T>, duration: Long, unit: TimeUnit)
    : Observable<T>() {

    private val debounceObserver = DebounceObserver(source, duration, unit)

    override fun subscribeInternal(internalObserver: InternalObserver<in T>) {
        debounceObserver.subscribeInternal(internalObserver)
    }

    class DebounceObserver<T>(
        private var source: ObservableSource<T>?,
        private val duration: Long,
        private val unit: TimeUnit
    ) : InternalObserver<T> {

        private var internalObserver: InternalObserver<in T>? = null

        fun subscribeInternal(internalObserver: InternalObserver<in T>) {
            this.internalObserver = internalObserver
            source?.subscribeInternal(this)
        }

        override fun onSubscribe(upstream: InternalDisposable, task: String) {
            internalObserver?.onSubscribe(upstream, task)
            CommonObservable.debounce(task, duration, unit)
        }

        override fun disposeInternal() {
            internalObserver?.disposeInternal()
            source = null
            internalObserver = null
        }

        override fun onAction(t: T) {
            internalObserver?.onAction(t)
        }
    }
}