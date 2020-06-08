package com.hsae.platform.nfu.wrapper.common

interface CheckableObserver<T> {

    fun onAction(t: T)

    fun isDisposed(): Boolean
}