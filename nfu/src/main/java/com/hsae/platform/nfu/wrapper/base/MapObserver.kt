package com.hsae.platform.nfu.wrapper.base

class MapObserver<T, U>
    (private var source: ObservableSource<T>?, private var mapTransform: MapTransform<in T, in U>?)
    : InternalObserver<T> {

    private var internalObserver: InternalObserver<in U>? = null

    fun subscribeInternal(internalObserver: InternalObserver<in U>) {
        this.internalObserver = internalObserver
        source?.subscribeInternal(this)
    }

    override fun onSubscribe(upstream: InternalDisposable, task: String) {
        internalObserver?.onSubscribe(upstream, task)
    }

    override fun disposeInternal() {
        internalObserver?.disposeInternal()
        source = null
        mapTransform = null
        internalObserver = null
    }

    override fun onAction(t: T) {
        internalObserver?.let {
            mapTransform?.apply {
                val u: U? = transform(t) as? U?
                u?.run { it.onAction(this) }
            }
        }
    }
}