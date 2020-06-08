package com.hsae.platform.nfu

import com.hsae.platform.nfu.wrapper.TASK_DOWNLOAD
import com.hsae.platform.nfu.wrapper.TASK_LINK_CHECK
import com.hsae.platform.nfu.wrapper.TASK_OFFLINE_CHECK
import com.hsae.platform.nfu.wrapper.TASK_UPGRADE
import com.hsae.platform.nfu.wrapper.base.Observable
import com.hsae.platform.nfu.wrapper.common.CommonObservable
import com.hsae.platform.nfu.wrapper.message.DownloadMsg
import com.hsae.platform.nfu.wrapper.message.LinkCheckMsg
import com.hsae.platform.nfu.wrapper.message.OfflineCheckMsg
import com.hsae.platform.nfu.wrapper.message.UpgradeMsg

/**
 * 近场升级入口类
 *
 * NFU: Near Field Upgrade
 */
object NFU {

    /**
     * 脱机检测绑定的车机是否有新版本
     *
     * 至少需要互联一次车机才可以查询，否则返回错误消息
     * @return [Observable] 用于订阅
     */
    @JvmStatic fun offlineCheck(): Observable<OfflineCheckMsg>
            = CommonObservable(TASK_OFFLINE_CHECK)

    /**
     * 预下载车机升级包，然后和车机连接升级时就不用下载了
     *
     * 在使用 [offlineCheck] 查询到新版本后，可以调用此方法预下载
     * @return [Observable] 用于订阅
     */
    @JvmStatic fun offlineDownload(): Observable<DownloadMsg>
            = CommonObservable(TASK_DOWNLOAD)

    /**
     * 根据 [ip] 地址连接车机，连接后绑定并检测是否有更新
     *
     * 注意：如果取消订阅会断开连接，可以在升级成功后再取消订阅，建议使用 Lifecycle 管理
     * @param ip 扫描车机二维码得到 ip 地址
     * @return [Observable] 用于订阅
     */
    @JvmStatic fun linkCheck(ip: String): Observable<LinkCheckMsg>
            = CommonObservable(TASK_LINK_CHECK, ip)

    /**
     * 连接成功后进行车机升级工作（下载升级包、传输升级包）
     * @return [Observable] 用于订阅
     */
    @JvmStatic fun linkUpgrade(): Observable<UpgradeMsg>
            = CommonObservable(TASK_UPGRADE)
}