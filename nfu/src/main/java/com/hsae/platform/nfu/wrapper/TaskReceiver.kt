package com.hsae.platform.nfu.wrapper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hsae.platform.nfu.connect.context.LinkService
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.AsyncTask
import android.provider.Settings
import com.hsae.platform.nfu.upgrade.DownloadService
import com.hsae.platform.nfu.upgrade.TransmittService
import com.hsae.platform.nfu.upgrade.VersionService
import com.hsae.platform.nfu.utils.LogUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

const val TASK_OFFLINE_CHECK  = "nfu.task.offline.check"
const val TASK_DOWNLOAD       = "nfu.task.download"
const val TASK_LINK_CHECK     = "nfu.task.link.check"
const val TASK_UPGRADE        = "nfu.task.upgrade"
const val NOTIFY_CONNECTED    = "nfu.notify.connected"
const val NOTIFY_DISCONNECTED = "nfu.notify.disconnected"

class TaskReceiver : BroadcastReceiver() {

    @SuppressLint("HardwareIds")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TASK_LINK_CHECK -> intent.getStringExtra("ip")?.let {
                context.startService(
                    Intent(context, LinkService::class.java)
                        .putExtra("ip", it)
                )
            }

            TASK_OFFLINE_CHECK -> {
                val preferences: SharedPreferences =
                    context.getSharedPreferences("vinPreferences", Context.MODE_PRIVATE)
                val connectedVin = preferences.getString("connected_vin", "")
                val connectedVersion = preferences.getString("connected_version ", "")
                LogUtil.e("connectedVin = $connectedVin")
                LogUtil.e("connectedVersion = $connectedVersion")
                AsyncTask.THREAD_POOL_EXECUTOR.execute {
                    val map = HashMap<String, String?>()
                    var version = ""
                    map["pUniqueCode"] = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    map["tUniqueCode"] = connectedVin
                    map["version"] = connectedVersion
                    val requestPost =
                        requestPost("http://fota.yzhsae.com:30005/api/version/find", map)
                    LogUtil.e(requestPost.toString())
                    requestPost?.let {
                        val code = it.getString("code")
                        val msg = it.getString("msg")
                        if (code == "1" && msg == "success") {
                            val datas = it.getJSONObject("datas")
                            datas.let {
                                version = datas.getString("version")
                            }
                        }
                    }
                    context.startService(
                        Intent(context, VersionService::class.java)
                            .setAction("fota.action.CheckVersion")
                            .putExtra("download_version", version)
                            .putExtra("download_connected", false)
                    )
                }
            }
            TASK_DOWNLOAD -> {
                context.startService(
                    Intent(context, DownloadService::class.java)
                        .setAction("fota.action.Download")
                        .putExtra("download_connected", false)
                )
            }
            TASK_UPGRADE -> {
                context.startService(
                    Intent(context, DownloadService::class.java)
                        .setAction("fota.action.Download")
                        .putExtra("download_connected", true)
                )
            }
            NOTIFY_CONNECTED -> {
                val vin = intent.getStringExtra("vin")
                val version = intent.getStringExtra("version")
                AsyncTask.THREAD_POOL_EXECUTOR.execute {
                    val map = HashMap<String, String?>()
                    map["pUniqueCode"] = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    map["tUniqueCode"] = vin
                    map["version"] = version
                    val requestPost =
                        requestPost("http://fota.yzhsae.com:30005/api/version/bind", map)
                    LogUtil.e(requestPost.toString())
                }
                val preferences: SharedPreferences =
                    context.getSharedPreferences("vinPreferences", Context.MODE_PRIVATE)
                preferences.edit().putString("connected_vin", vin)
                    .putString("connected_version", version).apply()
                context.startService(
                    Intent(context, VersionService::class.java)
                        .setAction("fota.action.CheckVersion")
                        .putExtra("download_version", version)
                        .putExtra("download_connected", true)
                )
                context.startService(Intent(context, TransmittService::class.java))
            }
            NOTIFY_DISCONNECTED -> {
                context.stopService(Intent(context, TransmittService::class.java))
            }
        }
    }

    private fun requestPost(
        baseUrl: String,
        paramsMap: java.util.HashMap<String, String?>
    ): JSONObject? {
        var result: String? = ""
        try {
            val tempParams = StringBuilder()
            for ((pos, key) in paramsMap.keys.withIndex()) {
                if (pos > 0) tempParams.append("&")
                tempParams.append(
                    String.format(
                        "%s=%s",
                        key,
                        URLEncoder.encode(paramsMap[key], "utf-8")
                    )
                )
            }
            val params = tempParams.toString()
            val postData = params.toByteArray()
            val url = URL(baseUrl)
            val urlConn =
                url.openConnection() as HttpURLConnection
            urlConn.connectTimeout = 5 * 1000
            urlConn.readTimeout = 5 * 1000
            urlConn.doOutput = true
            urlConn.doInput = true
            urlConn.useCaches = false
            urlConn.requestMethod = "POST"
            urlConn.instanceFollowRedirects = true
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            urlConn.connect()
            val dos = DataOutputStream(urlConn.outputStream)
            dos.write(postData)
            dos.flush()
            dos.close()
            if (urlConn.responseCode == 200) {
                result = streamToString(urlConn.inputStream)
            }
            urlConn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var jsonObject: JSONObject? = null
        try {
            if (result != null) {
                jsonObject = JSONObject(result)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

    /**
     * 将输入流转换成字符串
     */
    private fun streamToString(inputStream: InputStream): String? {
        return try {
            val baos = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                baos.write(buffer, 0, len)
            }
            baos.close()
            inputStream.close()
            val byteArray = baos.toByteArray()
            String(byteArray)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }
}
