package com.hsae.platform.nfu.wrapper.common

object ObserverCenter {

    private val observers = mutableMapOf<String, Any>()
    private var task: String? = null

    fun register(task: String): ObserverCenter {
        this.task = task
        return this
    }

    fun with(value: Any) {
        task?.let { observers[it] = value }
        task = null
    }

    fun get(task: String): Any? = observers[task]

    fun emit(task: String): Any? = observers.remove(task)

    fun clear() = observers.clear()
}