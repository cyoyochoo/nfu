package com.hsae.platform.nfu.connect

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.hsae.platform.nfu.connect.connector.wifi.DefaultWifiConnectImpl
import com.hsae.platform.nfu.connect.connector.wifi.IWifiConnect
import com.hsae.platform.nfu.connect.context.LinkService
import com.hsae.platform.nfu.connect.fota.Fota
import com.hsae.platform.nfu.utils.AppUtil
import com.hsae.platform.nfu.wrapper.NOTIFY_DISCONNECTED
import com.hsae.platform.nfu.wrapper.ObserverProvider
import com.hsae.platform.nfu.wrapper.base.InternalDisposable
import com.hsae.platform.nfu.wrapper.common.ObserverCenter
import com.hsae.platform.nfu.wrapper.message.LinkCheckMsg
import com.hsae.platform.nfu.wrapper.message.LinkCheckState
import com.hsae.platform.nfu.wrapper.message.UpgradeMsg
import com.hsae.platform.nfu.wrapper.message.UpgradeState

object LinkManager {

    @JvmField val wifiConnect: IWifiConnect = DefaultWifiConnectImpl()
    private var linkService: LinkService? = null

    fun stop(isDisposed: Boolean = true, msg: String? = null) {
        if (!isDisposed) notifyLinkState(LinkCheckState.error, msg)
        AppUtil.application?.sendBroadcast(Intent(NOTIFY_DISCONNECTED).setPackage(
            AppUtil.application?.packageName
        ))
        ObserverCenter.clear()
        Fota.getInstance().unregisterRouter()
        wifiConnect.stop(msg)
        linkService?.suicide()
        linkService = null
    }

    fun connect(linkService: LinkService, ip: String) {
        this.linkService = linkService
        wifiConnect.connect(ip)
    }

    fun isConnected() = wifiConnect.isConnected

    fun notifyLinkState(state: LinkCheckState, detail: String?) {
        ObserverProvider.getLinkCheckObserver()?.apply {
            if (state == LinkCheckState.error) {
                var internalDisposable = this as InternalDisposable
                ObserverProvider.getUpgradeObserver()?.let {
                    it.onAction(UpgradeMsg.get().setState(UpgradeState.error).setDetail(detail))
                    internalDisposable = it as InternalDisposable
                } ?: onAction(LinkCheckMsg.get().setState(state).setDetail(detail))
                Handler(Looper.getMainLooper()).post { internalDisposable.disposeInternal() }
            } else onAction(LinkCheckMsg.get().setState(state).setDetail(detail))
        }
    }
}