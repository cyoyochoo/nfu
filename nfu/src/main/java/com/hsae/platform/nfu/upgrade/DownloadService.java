package com.hsae.platform.nfu.upgrade;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import com.hsae.platform.nfu.router.Router;
import com.hsae.platform.nfu.router.interfaces.RouterFota;
import com.hsae.platform.nfu.utils.LogUtil;
import com.hsae.platform.nfu.wrapper.ObserverProvider;
import com.hsae.platform.nfu.wrapper.common.CheckableObserver;
import com.hsae.platform.nfu.wrapper.message.DownloadMsg;
import com.hsae.platform.nfu.wrapper.message.DownloadState;
import com.hsae.platform.nfu.wrapper.message.UpgradeMsg;
import com.hsae.platform.nfu.wrapper.message.UpgradeState;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {
    private static final String ACTION_DOWNLOAD = "fota.action.Download";
    private static final String DOWNLOAD_CONNECTED = "download_connected";
    private CheckableObserver<DownloadMsg> downloadObserver;
    private CheckableObserver<UpgradeMsg> upgradeObserver;
    private DownloadMsg downloadMsg;
    private UpgradeMsg upgradeMsg;
    private boolean netWorkState = false;
    private Intent mIntent;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback callback;
    private BroadcastReceiver netWorkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                connectivityManager = (ConnectivityManager) getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    if (callback == null) {
                        callback = new ConnectivityManager.NetworkCallback() {
                            @Override
                            public void onAvailable(@NotNull Network network) {
                                super.onAvailable(network);
                                LogUtil.d("onAvailable: 网络可用");
                                netWorkState = true;
                            }

                            @Override
                            public void onLost(@NotNull Network network) {
                                super.onLost(network);
                                LogUtil.d("onLost: 网络断开");
                                netWorkState = false;
                            }
                        };
                        connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), callback);
                    }
                }
            } else {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    if (getNetWorkType()) {
                        LogUtil.d("网络可用");
                        netWorkState = true;
                    } else {
                        LogUtil.d("没有网络");
                        netWorkState = false;
                    }
                }
            }
        }
    };

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mIntent = intent;
        startDownload(intent);
    }

    private void startDownload(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                SharedPreferences preferences = getSharedPreferences("versionPreferences", Context.MODE_PRIVATE);
                boolean connected = intent.getBooleanExtra(DOWNLOAD_CONNECTED, false);
                if (!connected) {
                    downloadObserver = ObserverProvider.getDownloadObserver();
                    setDownloadState(DownloadState.start, -1, null);
                } else {
                    upgradeObserver = ObserverProvider.getUpgradeObserver();
                    setUpgradeState(UpgradeState.start, -1, null);
                }
                final String packageUrl = preferences.getString("packageUrl", "");
                if (packageUrl.equals("")) {
                    setDownloadState(DownloadState.error, -1, "没有版本数据");
                    setUpgradeState(UpgradeState.error, -1, "没有版本数据");
                    return;
                }
                final String version = preferences.getString("version", "");
                final String bytes = preferences.getString("bytes", "0");
                final String pmd5 = preferences.getString("pmd5", "");
                final int md5 = preferences.getInt("md5", 0);
                final String fileName = preferences.getString("fileName", "");
                long size = 0;
                try {
                    size = Long.parseLong(bytes);
                } catch (NumberFormatException e) {
                    setDownloadState(DownloadState.error, -1, "OTA服务端升级包文件异常");
                    setUpgradeState(UpgradeState.error, -1, "OTA服务端升级包文件异常");
                }
                final int blockCount = (int) (size / 524288 + ((size % 524288 == 0) ? 0 : 1));
                preferences.edit().putInt("blockCount", blockCount).apply();
                handleActionDownload(packageUrl, pmd5, version, fileName, blockCount, md5);
            }
        }
    }

    private void handleActionDownload(String packageUrl, String pMd5, String pVersion, String fileName,
                                      int blockCount, int hashCode) {
        if (getExternalCacheDir() == null) {
            return;
        }
        File file = new File(FotaUtils.getDiskCacheDir(this) + "/" + fileName);
        if (!file.exists()) {
            if (file.getParentFile() != null) {
                boolean mkdir = file.getParentFile().mkdir();
                LogUtil.d("handleActionDownload: 创建父目录" + mkdir);
            }
            try {
                boolean newFile = file.createNewFile();
                LogUtil.d("handleActionDownload: 创建新文件" + newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long total = 0;
        long current = 0;
        String fileMD5 = FotaUtils.getFileMD5(file);
        if (fileMD5 != null && hashCode == fileMD5.hashCode()) {
            setDownloadState(DownloadState.end, -1, null);
            setUpgradeState(UpgradeState.downloaded, -1, null);
            LogUtil.d("download: 校验完毕，文件加密md5值相同" + hashCode);
            downloadSuccess(pMd5, pVersion, fileName, fileMD5, blockCount);
            return;
        }
        LogUtil.d("download: 文件加密md5值不相同，开始下载文件");
        try {
            java.net.URL Url = new URL(packageUrl);
            HttpURLConnection urlConn = (HttpURLConnection) Url.openConnection();
            urlConn.setConnectTimeout(5 * 1000);
            urlConn.setReadTimeout(5 * 1000);
            urlConn.setUseCaches(true);
            urlConn.setRequestMethod("GET");
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            long start = file.length() - 524288;
            if (start > 0) {
                urlConn.setRequestProperty("Range", "bytes=" + start + "-");
            }
            urlConn.connect();
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                LogUtil.d("download: 接着上回进度下载");
                long unfinishedSize = urlConn.getContentLength();
                total = start + unfinishedSize;
                current = start;
                InputStream inputStream = urlConn.getInputStream();
                RandomAccessFile fileOutputStream = new RandomAccessFile(file, "rw");
                fileOutputStream.seek(start);
                byte[] bytes = new byte[1024];
                int len;
                int progress;
                int last_progress = -1;
                while ((len = inputStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, len);
                    current = current + len;
                    progress = (int) (current * 100 / total);
                    if (last_progress != progress) {
                        last_progress = progress;
                        LogUtil.d("onResponse: progress = " + progress);
                        setDownloadState(DownloadState.downloading, progress, null);
                        setUpgradeState(UpgradeState.downloading, progress, null);
                    }
                }
                fileOutputStream.close();
                inputStream.close();
                fileMD5 = FotaUtils.getFileMD5(file);
                if (fileMD5 != null && hashCode == fileMD5.hashCode()) {
                    setDownloadState(DownloadState.end, -1, null);
                    setUpgradeState(UpgradeState.downloaded, -1, null);
                } else {
                    setDownloadState(DownloadState.error, -1, "安装包校验错误");
                    setUpgradeState(UpgradeState.error, -1, "安装包校验错误");
                    if (file.exists()) {
                        boolean delete = file.delete();
                        LogUtil.e("download: 错误安装包删除" + (delete ? "成功" : "失败"));
                    }
                }
            } else if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                LogUtil.d("download: 从最开始开始下载");
                InputStream inputStream = urlConn.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                total = urlConn.getContentLength();
                byte[] bytes = new byte[1024];
                int len;
                int progress;
                int last_progress = -1;
                while ((len = inputStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, len);
                    fileOutputStream.flush();
                    current = current + len;
                    progress = (int) (current * 100 / total);
                    if (last_progress != progress) {
                        last_progress = progress;
                        LogUtil.d("onResponse: progress = " + progress);
                        setDownloadState(DownloadState.downloading, progress, null);
                        setUpgradeState(UpgradeState.downloading, progress, null);
                    }
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
                fileMD5 = FotaUtils.getFileMD5(file);
                if (fileMD5 != null && hashCode == fileMD5.hashCode()) {
                    setDownloadState(DownloadState.end, -1, null);
                    setUpgradeState(UpgradeState.downloaded, -1, null);
                    downloadSuccess(pMd5, pVersion, fileName, fileMD5, blockCount);
                } else {
                    setDownloadState(DownloadState.error, -1, "安装包校验错误");
                    setUpgradeState(UpgradeState.error, -1, "安装包校验错误");
                    if (file.exists()) {
                        boolean delete = file.delete();
                        LogUtil.d("download: 错误安装包删除" + (delete ? "成功" : "失败"));
                    }
                }
            }
            urlConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("DOWNLOAD EXCEPTION");
            LogUtil.e("升级包 packageUrl = " + packageUrl);
            LogUtil.e("总字节数 total = " + total);
            LogUtil.e("当前字节数 current = " + current);
            setDownloadState(DownloadState.error, -1, "网络异常");
            setUpgradeState(UpgradeState.error, -1, "网络异常");
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            for (int i = 1; i <= 10; i++) {
                if (netWorkState) {
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    LogUtil.d("handleActionDownload: 自动继续下载");
                    startDownload(mIntent);
                    break;
                } else {
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    LogUtil.d("handleActionDownload: 网络不可用，再次检查，检查次数" + i);
                }
            }
        }
    }

    private void downloadSuccess(String pMd5, String version, String fileName, String fileMD5, int blockCount) {
        JSONObject object = new JSONObject();
        JSONObject info = new JSONObject();
        try {
            object.put("modecode", 5);
            object.put("SignalName", "fotaVeriftyResult");
            object.put("pmd5", pMd5);
            object.put("pversion", version);
            object.put("result", true);
            info.put("fileId", 1);
            info.put("fileMD5", fileMD5);
            info.put("fileName", fileName);
            info.put("blockCount", blockCount);
            object.put("fileInfo", info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtil.d("json: " + object.toString());
        RouterFota routerFota = Router.get(RouterFota.class);
        if (routerFota != null) {
            routerFota.sendMsg(object);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d("onDestroy");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(callback);
        }
        unregisterReceiver(netWorkStateReceiver);
    }

    private void setDownloadState(DownloadState state, int progress, String detail) {
        if (downloadObserver != null) {
            if (downloadMsg == null) {
                downloadMsg = DownloadMsg.get();
            }
            downloadMsg.setState(state);
            if (progress != -1) {
                downloadMsg.setProgress(progress);
            }
            if (detail != null) {
                downloadMsg.setDetail(detail);
            }
            downloadObserver.onAction(downloadMsg);
        }
    }

    private void setUpgradeState(UpgradeState state, int progress, String detail) {
        if (upgradeObserver != null) {
            if (upgradeMsg == null) {
                upgradeMsg = UpgradeMsg.get();
            }
            upgradeMsg.setState(state);
            if (progress != -1) {
                upgradeMsg.setProgress(progress);
            }
            if (detail != null) {
                upgradeMsg.setDetail(detail);
            }
            upgradeObserver.onAction(upgradeMsg);
        }
    }

    private boolean getNetWorkType() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (manager != null) {
            networkInfo = manager.getActiveNetworkInfo();
        }
        if (networkInfo != null) {
            return networkInfo.isAvailable() && networkInfo.isConnected();
        } else {
            return false;
        }
    }
}
