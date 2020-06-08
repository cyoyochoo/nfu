package com.hsae.platform.nfu.connect.protocol.wifi.parser

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.hsae.platform.nfu.connect.LinkManager
import com.hsae.platform.nfu.connect.Util
import com.hsae.platform.nfu.connect.fota.Fota
import com.hsae.platform.nfu.connect.protocol.ProtocolConstant
import com.hsae.platform.nfu.utils.AppUtil
import com.hsae.platform.nfu.utils.LogUtil
import com.hsae.platform.nfu.wrapper.NOTIFY_CONNECTED
import com.hsae.platform.nfu.wrapper.message.LinkCheckState
import org.json.JSONObject

class WifiParser: BaseParser() {

    override fun parse(data: ByteArray) {
        val jsonStr = String(data, Util.UTF_8)
        LogUtil.i(jsonStr, "WifiParser")

        try {
            val json = JSONObject(jsonStr)

            when (json.getString(ProtocolConstant.TYPE_READ)) {

                "fotaResponse" -> Fota.getInstance().onFotaResponse(
                    json.getBoolean("response"),
                    json.optString("md5", ""),
                    json.getString("version"),
                    json.getString("projectId")
                )

                "download" -> Fota.getInstance().onFotaDownload(
                    json.getInt("fileId"),
                    json.getString("fileMD5"),
                    json.getInt("blockIndex")
                )

                "fotaResult" -> {
                    val args = json.getJSONObject("args")
                    Fota.getInstance().onFotaResult(
                        args.getInt("result"),
                        args.optString("reason")
                    )
                }

                "reveDAWifiReady" -> {
                    Handler(Looper.getMainLooper()).post {
                        LinkManager.notifyLinkState(LinkCheckState.linked, "连接成功")
                        Fota.getInstance().registerRouter()
                        AppUtil.application?.sendBroadcast(
                            Intent(NOTIFY_CONNECTED)
                                .setPackage(AppUtil.application?.packageName)
                                .apply {
                                    json.optJSONObject("args")?.let {
                                        putExtra("version", it.optString("version"))
                                        putExtra("vin", it.optString("vin"))
                                    }
                                }
                        )
                    }
                }

            }
        } catch (e: Exception) {
            LinkManager.wifiConnect.wifiProtocolHandler?.wifiConnector?.close()
            e.printStackTrace()
        }
    }

}