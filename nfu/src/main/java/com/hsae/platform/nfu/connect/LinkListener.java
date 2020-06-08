package com.hsae.platform.nfu.connect;

import com.hsae.platform.nfu.connect.connector.ConnectorListener;

public interface LinkListener {
    boolean isConnected();
    void resetLinkState();
    void onWifiStateChanged(@ConnectorListener.State int state, String msg);
    void onFotaStateChanged(@ConnectorListener.State int state, String msg);
    void showConnectHint();
}
