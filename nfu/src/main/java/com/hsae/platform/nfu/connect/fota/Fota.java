package com.hsae.platform.nfu.connect.fota;


import com.hsae.platform.nfu.connect.LinkManager;
import com.hsae.platform.nfu.connect.Util;
import com.hsae.platform.nfu.connect.protocol.wifi.FotaProtocolHandler;
import com.hsae.platform.nfu.connect.protocol.wifi.WifiProtocolHandler;
import com.hsae.platform.nfu.router.Router;
import com.hsae.platform.nfu.router.interfaces.RouterFota;
import com.hsae.platform.nfu.router.listeners.FotaListener;

import org.json.JSONObject;

public class Fota implements RouterFota {
    private FotaListener listener;
    private Fota() {
    }

    public void registerRouter() {
        Router.registerInstance(this);
    }

    public void unregisterRouter() {
        Router.unregisterInstance(this);
        listener = null;
    }

    public void onFotaResponse(boolean response, String md5, String version, String projectId) {
        if (listener != null) listener.onFotaResponse(response, md5, version, projectId);
    }

    public void onFotaDownload(int fileId, String fileMD5, int blockIndex) {
        if (listener != null) listener.onFotaDownload(fileId, fileMD5, blockIndex);
    }

    public void onFotaResult(int result, String reason) {
        if (listener != null) listener.onFotaResult(result, reason);
    }

    @Override
    public void sendData(int fileId, int blockIndex, byte[] blockData) {
        //get fota data
        int blockDataL = blockData.length;
        byte[] fotaData = new byte[12 + blockDataL];
        fotaData[0] = (byte) ((fileId >> 24) & 0xff);
        fotaData[1] = (byte) ((fileId >> 16) & 0xff);
        fotaData[2] = (byte) ((fileId >> 8)  & 0xff);
        fotaData[3] = (byte) ( fileId        & 0xff);

        fotaData[4] = (byte) ((blockIndex >> 24) & 0xff);
        fotaData[5] = (byte) ((blockIndex >> 16) & 0xff);
        fotaData[6] = (byte) ((blockIndex >> 8)  & 0xff);
        fotaData[7] = (byte) ( blockIndex        & 0xff);

        fotaData[8] = (byte) ((blockDataL >> 24) & 0xff);
        fotaData[9] = (byte) ((blockDataL >> 16) & 0xff);
        fotaData[10] =(byte) ((blockDataL >> 8)  & 0xff);
        fotaData[11] =(byte) ( blockDataL        & 0xff);
        System.arraycopy(blockData, 0, fotaData, 12, blockDataL);

        FotaProtocolHandler fotaProtocolHandler = LinkManager.wifiConnect.getFotaProtocolHandler();
        if (fotaProtocolHandler != null) fotaProtocolHandler.send(fotaData);
    }

    @Override
    public void sendMsg(JSONObject msg) {
        WifiProtocolHandler wifiProtocolHandler = LinkManager.wifiConnect.getWifiProtocolHandler();
        if (wifiProtocolHandler != null)
            wifiProtocolHandler.send(Util.getWifiTypeData(msg.toString().getBytes(Util.UTF_8)));
    }

    @Override
    public void setFotaListener(FotaListener fotaListener) {
        listener = fotaListener;
    }

    private static final class HOLDER {
        private static final Fota INSTANCE = new Fota();
    }

    public static Fota getInstance() {
        return HOLDER.INSTANCE;
    }
}
