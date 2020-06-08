package com.hsae.platform.nfu.upgrade;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.hsae.platform.nfu.utils.LogUtil;
import com.hsae.platform.nfu.wrapper.ObserverProvider;
import com.hsae.platform.nfu.wrapper.common.CheckableObserver;
import com.hsae.platform.nfu.wrapper.message.LinkCheckMsg;
import com.hsae.platform.nfu.wrapper.message.LinkCheckState;
import com.hsae.platform.nfu.wrapper.message.OfflineCheckMsg;
import com.hsae.platform.nfu.wrapper.message.OfflineCheckState;
import com.hsae.platform.nfu.wrapper.message.Version;

import org.json.JSONObject;

import java.util.HashMap;

public class VersionService extends IntentService {
    private static final String ACTION_CHECKVERSION = "fota.action.CheckVersion";
    private static final String BASE_URL = "http://fota.yzhsae.com:30005/api/package/";
    private static final String PACKAGE_URL = "https://hsae-fota.oss-cn-shanghai.aliyuncs.com/";
    private static final String DOWNLOAD_VERSION = "download_version";
    private static final String DOWNLOAD_CONNECTED = "download_connected";
    private CheckableObserver<OfflineCheckMsg> checkObserver;
    private CheckableObserver<LinkCheckMsg> linkObserver;
    private SharedPreferences preferences;

    public VersionService() {
        super("VersionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECKVERSION.equals(action)) {
                boolean connected = intent.getBooleanExtra(DOWNLOAD_CONNECTED, false);
                preferences = getSharedPreferences("versionPreferences", Context.MODE_PRIVATE);
                preferences.edit().clear().apply();
                if (!connected) {
                    checkObserver = ObserverProvider.getOfflineCheckObserver();
                    setCheckState(OfflineCheckState.start, null, null);
                } else {
                    linkObserver = ObserverProvider.getLinkCheckObserver();
                    setLinkState(LinkCheckState.checking, null, null);
                }

                String version = intent.getStringExtra(DOWNLOAD_VERSION);
                if (version == null || "".equals(version)) {
                    if (!connected) {
                        setCheckState(OfflineCheckState.error, null, "请先绑定车机");
                        return;
                    }
                }
                handleActionCheckVersion(version);
            }
        }
    }

    private void handleActionCheckVersion(String pVersion) {
        HashMap<String, String> map = new HashMap<>();
        map.put("version", pVersion);
        map.put("projectId", "e0ef610f-a43b-4c5c-9cb8-80979bd3a9ec");
        JSONObject requestObject = HttpUtils.requestPost(BASE_URL + "version", map);
        if (requestObject != null) {
            String code = requestObject.optString("code");
            String msg = requestObject.optString("msg");
            JSONObject datas = requestObject.optJSONObject("datas");
            if ("1".equals(code) && "success".equals(msg)) {
                if (datas != null) {
                    String id = datas.optString("id");
                    String newVersion = datas.optString("version");
                    String packageType = datas.optString("packageType");
                    String size = datas.optString("size");
                    String bytes = datas.optString("bytes");
                    String packageUrl = datas.optString("packageUrl");
                    String resume = datas.optString("resume");
                    HashMap<String, String> map2 = new HashMap<>();
                    map2.put("id", id);
                    JSONObject md5Object = HttpUtils.requestPost(BASE_URL + "getMd5", map2);
                    if (md5Object != null) {
                        LogUtil.d("handleActionCheckVersion: " + md5Object.toString());
                        String code2 = md5Object.optString("code");
                        String msg2 = md5Object.optString("msg");
                        if ("1".equals(code2) && "success".equals(msg2)) {
                            JSONObject datas2 = md5Object.optJSONObject("datas");
                            if (datas2 != null) {
                                int md5 = datas2.optInt("md5");
                                LogUtil.d("handleActionCheckVersion: md5 " + md5);
                                setCheckState(OfflineCheckState.end, new Version(true, pVersion,
                                        newVersion, packageType, size, bytes, resume), null);
                                setLinkState(LinkCheckState.end, new Version(true, pVersion,
                                        newVersion, packageType, size, bytes, resume), null);
                                preferences.edit()
                                        .putString("pVersion", pVersion)
                                        .putString("version", newVersion)
                                        .putString("bytes", bytes)
                                        .putString("packageType", packageType)
                                        .putString("packageUrl", packageUrl)
                                        .putString("fileName", packageUrl.replace(PACKAGE_URL, ""))
                                        .putString("resume", resume)
                                        .putString("pmd5", "")
                                        .putInt("md5", md5)
                                        .apply();
                            }
                        } else {
                            setCheckState(OfflineCheckState.error, null, msg);
                            setLinkState(LinkCheckState.error, null, msg);
                        }
                    } else {
                        setCheckState(OfflineCheckState.error, null, "获取md5异常");
                        setLinkState(LinkCheckState.error, null, "获取md5异常");
                    }
                } else {
                    setCheckState(OfflineCheckState.end, new Version(false, pVersion, null,
                            null, null, null, null), null);
                    setLinkState(LinkCheckState.end, new Version(false, pVersion, null,
                            null, null, null, null), null);
                }
            } else {
                setCheckState(OfflineCheckState.error, null, msg);
                setLinkState(LinkCheckState.error, null, msg);
            }
        } else {
            setCheckState(OfflineCheckState.error, null, "获取版本信息异常");
            setLinkState(LinkCheckState.error, null, "获取版本信息异常");
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.d("onDestroy");
        super.onDestroy();
    }

    private void setCheckState(OfflineCheckState state, Version version, String detail) {
        if (checkObserver != null) {
            OfflineCheckMsg offlineCheck = OfflineCheckMsg.get().setState(state);
            if (detail != null) {
                offlineCheck.setDetail(detail);
            }
            if (version != null) {
                offlineCheck.setVersion(version);
            }
            checkObserver.onAction(offlineCheck);
        }
    }

    private void setLinkState(LinkCheckState state, Version version, String detail) {
        if (linkObserver != null) {
            LinkCheckMsg linkCheck = LinkCheckMsg.get().setState(state);
            if (detail != null) {
                linkCheck.setDetail(detail);
            }
            if (version != null) {
                linkCheck.setVersion(version);
            }
            linkObserver.onAction(linkCheck);
        }
    }
}
