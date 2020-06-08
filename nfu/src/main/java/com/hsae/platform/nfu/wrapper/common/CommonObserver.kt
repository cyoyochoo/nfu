package com.hsae.platform.nfu.wrapper.common

import com.hsae.platform.nfu.connect.LinkManager
import com.hsae.platform.nfu.wrapper.TASK_LINK_CHECK
import com.hsae.platform.nfu.wrapper.TASK_UPGRADE
import com.hsae.platform.nfu.wrapper.base.InternalDisposable
import com.hsae.platform.nfu.wrapper.base.InternalObserver

class CommonObserver<T> : CheckableObserver<T>, InternalDisposable {

    private var internalObserver: InternalObserver<in T>? = null
    private var task: String? = null

    override fun onAction(t: T) {
        internalObserver?.onAction(t)
    }

    override fun isDisposed(): Boolean = internalObserver == null

    override fun disposeInternal() {
        internalObserver?.disposeInternal()
        internalObserver = null
        if (task == TASK_LINK_CHECK || task == TASK_UPGRADE) LinkManager.stop()
    }

    fun onSubscribe(internalObserver: InternalObserver<in T>, task: String) {
        this.internalObserver = internalObserver
        this.task = task
        internalObserver.onSubscribe(this, task)
    }
}