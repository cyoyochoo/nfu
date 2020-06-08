package com.hsae.platform.nfu.wrapper.base

import androidx.lifecycle.Lifecycle

class AutoDisposeObservable<T>
    (source: ObservableSource<T>, lifecycle: Lifecycle)
    : Observable<T>() {

    private val autoDisposeObserver = AutoDisposeObserver(source, lifecycle)

    override fun subscribeInternal(internalObserver: InternalObserver<in T>) {
        autoDisposeObserver.subscribeInternal(internalObserver)
    }
}