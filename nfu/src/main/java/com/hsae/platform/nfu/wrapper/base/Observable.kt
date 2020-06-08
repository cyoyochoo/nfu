package com.hsae.platform.nfu.wrapper.base

import androidx.lifecycle.Lifecycle
import java.util.concurrent.TimeUnit

/**
 * 可观察对象，用于订阅
 */
abstract class Observable<T> : ObservableSource<T> {

    /**
     * 数据类型变换操作符
     * @param mapTransform 变换接口
     * @return [Observable] 用于订阅
     */
    fun <U> map(mapTransform: MapTransform<in T, in U>): Observable<U>
            = MapObservable(this, mapTransform)

    /**
     * 数据类型变换操作符
     * @param action 变换 Lambda
     * @return [Observable] 用于订阅
     */
    fun <U> map(action: (t: T) -> U): Observable<U> {
        return map(object : MapTransform<T, U> {
            override fun transform(t: T) = action(t)
        })
    }

    /**
     * 防抖动， [duration] 时长内只响应第一次请求
     * @param duration 时长
     * @param unit 时长单位
     * @return [Observable] 用于订阅
     */
    fun debounce(duration: Long, unit: TimeUnit): Observable<T>
            = DebounceObservable(this, duration, unit)

    /**
     * 使用 [lifecycle] 自动管理生命周期
     * @param lifecycle 用于接收 onDestroy 事件来取消订阅
     * @return [Observable] 用于订阅
     */
    fun autoDispose(lifecycle: Lifecycle): Observable<T>
            = AutoDisposeObservable(this, lifecycle)

    /**
     * 订阅
     * @param observer 观察者，用于接收消息
     * @return [Disposable] 用于取消订阅操作
     */
    fun subscribe(observer: Observer<in T>): Disposable {
        val internalObserver = InternalObserverImpl(observer)
        subscribeInternal(internalObserver)
        return internalObserver
    }

    /**
     * 订阅
     * @param action 订阅 Lambda，用于接收消息
     * @return [Disposable] 用于取消订阅操作
     */
    fun subscribe(action: (t: T) -> Unit): Disposable {
        return subscribe(object : Observer<T> {
            override fun onAction(t: T) = action(t)
        })
    }
}