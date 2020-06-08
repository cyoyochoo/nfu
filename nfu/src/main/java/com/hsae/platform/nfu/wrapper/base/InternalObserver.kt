package com.hsae.platform.nfu.wrapper.base

interface InternalObserver<T> : Observer<T>, InternalDisposable {

    fun onSubscribe(upstream: InternalDisposable, task: String)
}