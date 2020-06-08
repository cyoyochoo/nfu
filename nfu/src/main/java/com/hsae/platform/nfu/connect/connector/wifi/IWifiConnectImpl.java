package com.hsae.platform.nfu.connect.connector.wifi;


import com.hsae.platform.nfu.connect.LinkListener;
import com.hsae.platform.nfu.connect.connector.ConnectorListener;
import com.hsae.platform.nfu.connect.protocol.wifi.FotaProtocolHandler;
import com.hsae.platform.nfu.connect.protocol.wifi.WifiProtocolHandler;

public class IWifiConnectImpl implements IWifiConnect {
    private static final int COMMAND_PORT = 7600;
    private static final int FOTA_PORT = 9630;
    private volatile WifiProtocolHandler wifiProtocolHandler;
    private volatile FotaProtocolHandler fotaProtocolHandler;
    private volatile LinkListener linkListener;
    private volatile int state = ConnectorListener.State.STATE_NONE;

    public LinkListener getLinkListener() {
        return linkListener;
    }

    public void setLinkListener(LinkListener linkListener) {
        this.linkListener = linkListener;
        if (wifiProtocolHandler != null) wifiProtocolHandler.setLinkListener(linkListener);
        if (fotaProtocolHandler != null) fotaProtocolHandler.setLinkListener(linkListener);
    }

    public int getState() {
        return state;
    }

    public void setState(@ConnectorListener.State int state) {
        this.state = state;
    }

    @Override
    public synchronized void connect(String ip) {
        if (linkListener != null) linkListener.showConnectHint();

        if (wifiProtocolHandler == null) {
            wifiProtocolHandler = new WifiProtocolHandler(ip, COMMAND_PORT, linkListener);
        }
        wifiProtocolHandler.start();

        if (fotaProtocolHandler == null)
            fotaProtocolHandler = new FotaProtocolHandler(ip, FOTA_PORT, linkListener);
        fotaProtocolHandler.start();
    }

    @Override
    public synchronized void stop(String msg) {
        if (wifiProtocolHandler != null) {
            wifiProtocolHandler.stop();
            wifiProtocolHandler = null;
        }
        if (fotaProtocolHandler != null) {
            fotaProtocolHandler.stop();
            fotaProtocolHandler = null;
        }
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public WifiProtocolHandler getWifiProtocolHandler() {
        return wifiProtocolHandler;
    }

    @Override
    public FotaProtocolHandler getFotaProtocolHandler() {
        return fotaProtocolHandler;
    }
}
