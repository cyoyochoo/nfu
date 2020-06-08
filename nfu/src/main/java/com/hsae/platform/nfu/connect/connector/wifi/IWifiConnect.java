package com.hsae.platform.nfu.connect.connector.wifi;


import com.hsae.platform.nfu.connect.protocol.wifi.FotaProtocolHandler;
import com.hsae.platform.nfu.connect.protocol.wifi.WifiProtocolHandler;

public interface IWifiConnect {
    void connect(String ip);
    void stop(String msg);
    boolean isConnected();
    WifiProtocolHandler getWifiProtocolHandler();
    FotaProtocolHandler getFotaProtocolHandler();
}
