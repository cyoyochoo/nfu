package com.hsae.platform.nfu.connect.connector.wifi

import com.hsae.platform.nfu.connect.DefaultLinkListenerImpl

class DefaultWifiConnectImpl : IWifiConnectImpl() {

    init {
        linkListener = DefaultLinkListenerImpl()
    }

    override fun stop(msg: String?) {
        super.stop(msg)
        linkListener.resetLinkState()
    }

    override fun isConnected(): Boolean {
        return linkListener.isConnected
    }
}