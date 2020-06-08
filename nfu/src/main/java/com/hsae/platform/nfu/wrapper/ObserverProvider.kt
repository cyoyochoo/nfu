package com.hsae.platform.nfu.wrapper

import com.hsae.platform.nfu.wrapper.common.CheckableObserver
import com.hsae.platform.nfu.wrapper.common.ObserverCenter
import com.hsae.platform.nfu.wrapper.message.DownloadMsg
import com.hsae.platform.nfu.wrapper.message.LinkCheckMsg
import com.hsae.platform.nfu.wrapper.message.OfflineCheckMsg
import com.hsae.platform.nfu.wrapper.message.UpgradeMsg

class ObserverProvider<T> {

    companion object {

        @JvmStatic fun getOfflineCheckObserver()
                = ObserverProvider<OfflineCheckMsg>().robObserver(TASK_OFFLINE_CHECK)

        @JvmStatic fun getDownloadObserver()
                = ObserverProvider<DownloadMsg>().robObserver(TASK_DOWNLOAD)

        @JvmStatic fun getLinkCheckObserver()
                = ObserverProvider<LinkCheckMsg>().getObserver(TASK_LINK_CHECK)

        @JvmStatic fun getUpgradeObserver()
                = ObserverProvider<UpgradeMsg>().getObserver(TASK_UPGRADE)
    }

    private fun robObserver(task: String): CheckableObserver<T>?
            = ObserverCenter.emit(task) as? CheckableObserver<T>?

    private fun getObserver(task: String): CheckableObserver<T>?
            = ObserverCenter.get(task) as? CheckableObserver<T>?

}