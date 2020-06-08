package com.hsae.platform.nfu.router.interfaces;

import com.hsae.platform.nfu.router.listeners.FotaListener;

import org.json.JSONObject;

public interface RouterFota {
    /**
     * 发送升级包文件
     * @param fileId 文件资源ID
     * @param blockIndex 文件块序号
     * @param blockData 文件块内容
     */
    void sendData(int fileId, int blockIndex, byte[] blockData);
    void sendMsg(JSONObject msg);
    void setFotaListener(FotaListener listener);
}
