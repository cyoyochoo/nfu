package com.hsae.platform.nfu.wrapper.base

/**
 * 数据类型变换接口
 */
interface MapTransform<T, U> {

    /**
     * 变换数据类型
     */
    fun transform(t: T): U
}