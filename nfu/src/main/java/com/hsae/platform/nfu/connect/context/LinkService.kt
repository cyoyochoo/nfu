package com.hsae.platform.nfu.connect.context

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.hsae.platform.nfu.connect.LinkManager
import com.hsae.platform.nfu.utils.LogUtil
import com.hsae.platform.nfu.wrapper.message.LinkCheckState

class LinkService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ip = intent?.getStringExtra("ip")
        ip?.let {
            if (LinkManager.isConnected())
                LinkManager.notifyLinkState(LinkCheckState.linked, "已连接")
            else
                LinkManager.connect(this, it)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i("LinkService: onDestroy")
    }

    fun suicide() = stopSelf()
}
