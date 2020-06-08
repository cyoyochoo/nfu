package com.hsae.platform.nfu.upgrade;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.hsae.platform.nfu.router.Router;
import com.hsae.platform.nfu.router.interfaces.RouterFota;
import com.hsae.platform.nfu.router.listeners.FotaListener;
import com.hsae.platform.nfu.utils.LogUtil;
import com.hsae.platform.nfu.wrapper.ObserverProvider;
import com.hsae.platform.nfu.wrapper.common.CheckableObserver;
import com.hsae.platform.nfu.wrapper.message.UpgradeMsg;
import com.hsae.platform.nfu.wrapper.message.UpgradeState;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class TransmittService extends Service {
    public TransmittService() {
    }

    private RouterFota routerFota;
    private CheckableObserver<UpgradeMsg> upgradeObserver;
    private UpgradeMsg upgradeMsg;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.e("TransmittService onCreate");
        routerFota = Router.get(RouterFota.class);
        preferences = getSharedPreferences("versionPreferences", Context.MODE_PRIVATE);
        if (routerFota != null) {
            routerFota.setFotaListener(new FotaListener() {
                @Override
                public void onFotaResponse(boolean response, String md5, String version, String projectId) {

                }

                @Override
                public void onFotaDownload(int fileId, String fileMD5, int blockIndex) {
                    if (upgradeObserver == null) {
                        upgradeObserver = ObserverProvider.getUpgradeObserver();
                    }
                    String pathname = FotaUtils.getDiskCacheDir(getApplicationContext()) + "/" +
                            preferences.getString("fileName", "");
                    sendFile(pathname, blockIndex);
                }

                @SuppressLint("HardwareIds")
                @Override
                public void onFotaResult(int result, String reason) {
                    if (upgradeObserver != null) {
                        if (result == 0) {
                            upgradeObserver.onAction(UpgradeMsg.get().setState(UpgradeState.end).setDetail("升级成功"));
                            AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
                                SharedPreferences vinPreferences = getApplicationContext()
                                        .getSharedPreferences("vinPreferences", Context.MODE_PRIVATE);
                                String vin = vinPreferences.getString("connected_vin", "");
                                HashMap<String, String> map = new HashMap<>();
                                map.put("pUniqueCode", Settings.Secure.getString(
                                        getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
                                map.put("tUniqueCode", vin);
                                String version = preferences.getString("version", "");
                                map.put("version", version);
                                JSONObject requestPost = HttpUtils.requestPost(
                                        "http://fota.yzhsae.com:30005/api/version/bind", map);
                                LogUtil.e(requestPost.toString());
                                stopSelf();
                            });
                        } else {
                            upgradeObserver.onAction(UpgradeMsg.get().setState(UpgradeState.error).setDetail(reason));
                            stopSelf();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.e("TransmittService onDestroy");
        if (routerFota != null) {
            routerFota.setFotaListener(null);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendFile(String pathname, int blockIndex) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            File file = new File(pathname);
            long length = file.length();
            long size = (length / 524288 + ((length % 524288 == 0L) ? 0 : 1));
            if (blockIndex == -1) {
                upgradeObserver.onAction(upgradeMsg.setState(UpgradeState.end).setDetail("取消升级"));
                return;
            }
            if ((blockIndex + 1) > size || blockIndex < 0) {
                return;
            }
            int len = 524288;
            if (blockIndex + 1 == size) {
                len = (int) (length % 524288);
            }
            byte[] readByte = FotaUtils.readByte(file, blockIndex, len);
            if (routerFota != null) {
                routerFota.sendData(1, blockIndex, readByte);
            }
            int progress = (int) ((blockIndex + 1) * 100 / size);
            if (upgradeObserver != null) {
                if (upgradeMsg == null) {
                    upgradeMsg = UpgradeMsg.get();
                }
                upgradeObserver.onAction(upgradeMsg.setState(UpgradeState.transmitting).setProgress(progress));
                if (progress == 100) {
                    upgradeObserver.onAction(upgradeMsg.setState(UpgradeState.transmitted));
                }
            }
        });
    }
}
