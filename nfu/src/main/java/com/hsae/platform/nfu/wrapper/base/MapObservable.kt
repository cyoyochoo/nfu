package com.hsae.platform.nfu.wrapper.base

class MapObservable<T, U>
    (source: ObservableSource<T>, mapTransform: MapTransform<in T, in U>)
    : Observable<U>() {

    private val mapObserver = MapObserver(source, mapTransform)

    override fun subscribeInternal(internalObserver: InternalObserver<in U>) {
        mapObserver.subscribeInternal(internalObserver)
    }
}