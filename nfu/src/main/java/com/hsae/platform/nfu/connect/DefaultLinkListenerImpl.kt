package com.hsae.platform.nfu.connect

import com.hsae.platform.nfu.connect.connector.ConnectorListener
import com.hsae.platform.nfu.connect.connector.wifi.WifiConnector
import com.hsae.platform.nfu.wrapper.message.LinkCheckState

class DefaultLinkListenerImpl: LinkListener {
    @Volatile private var linkWifi: Boolean = false
    @Volatile private var linkFota: Boolean = false

    private fun checkLinkState() {
        if (isConnected) {
            LinkManager.wifiConnect.wifiProtocolHandler?.notifyPhoneState(0)
        }
    }

    private fun onDisconnected(msg: String?) {
        msg?.apply {
            if (startsWith(WifiConnector.RECONNECT_MARK))
                LinkManager.notifyLinkState(
                    LinkCheckState.linking,
                    substring(WifiConnector.RECONNECT_MARK.length)
                )
            else LinkManager.stop(false, this)
        } ?: LinkManager.stop(false)
    }

    override fun showConnectHint() {
        if (!isConnected)
            LinkManager.notifyLinkState(LinkCheckState.linking, "WIFI通道连接中")
    }

    override fun isConnected() = linkWifi && linkFota

    override fun resetLinkState() {
        linkWifi = false
        linkFota = false
    }

    override fun onWifiStateChanged(state: Int, msg: String?) {
        linkWifi = state == ConnectorListener.State.STATE_CONNECTED
        if (linkWifi) checkLinkState()
        else if (state == ConnectorListener.State.STATE_NONE) onDisconnected(msg)
    }

    override fun onFotaStateChanged(state: Int, msg: String?) {
        linkFota = state == ConnectorListener.State.STATE_CONNECTED
        if (linkFota) checkLinkState()
        else if (state == ConnectorListener.State.STATE_NONE) onDisconnected(msg)
    }
}