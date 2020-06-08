package com.hsae.platform.nfu.wrapper.base

/**
 * 订阅消息接口
 */
interface Observer<T> {

    /**
     * 订阅后收到消息
     * @param t 消息对象
     */
    fun onAction(t: T)
}