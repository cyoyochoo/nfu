package com.hsae.platform.nfu.wrapper.base

interface ObservableSource<T> {

    fun subscribeInternal(internalObserver: InternalObserver<in T>)
}