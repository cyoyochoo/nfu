package com.hsae.platform.nfu.connect.protocol.wifi;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.hsae.platform.nfu.connect.LinkListener;
import com.hsae.platform.nfu.connect.connector.ConnectorListener;
import com.hsae.platform.nfu.connect.connector.wifi.WifiConnector;
import com.hsae.platform.nfu.connect.protocol.wifi.parser.BaseParser;


public abstract class BaseWifiProtocolHandler implements ConnectorListener {
    private WifiConnector wifiConnector;
    private volatile LinkListener linkListener;
    public HandlerThread writeThread;
    public Handler writeHandler;

    public BaseWifiProtocolHandler(String host, int port, LinkListener linkListener) {
        if (linkListener != null) this.linkListener = linkListener;
        writeThread = new HandlerThread("write_thread", Process.THREAD_PRIORITY_BACKGROUND);
        writeThread.start();
        writeHandler = new Handler(writeThread.getLooper());
        wifiConnector = new WifiConnector(host, port);
        wifiConnector.setConnectorListener(this);
    }

    public void setLinkListener(LinkListener linkListener) {
        this.linkListener = linkListener;
    }

    public LinkListener getLinkListener() {
        return linkListener;
    }

    public WifiConnector getWifiConnector() {
        return wifiConnector;
    }

    public void start() {
        wifiConnector.connect();
    }

    public void stop() {
        linkListener = null;
        wifiConnector.setConnectorListener(null);
        wifiConnector.stop();
        writeHandler.removeCallbacksAndMessages(null);
        writeHandler.getLooper().quit();
    }

    public void send(byte[] data) {
        writeHandler.post(() -> wifiConnector.write(data));
    }

    public void sendSynchronously(byte[] data) {
        wifiConnector.write(data);
    }

    abstract Handler getReadHandler();
    abstract BaseParser getReadParser();

    @Override
    public void onStateChanged(int state, String msg) {
        if (state == State.STATE_CONNECTED
                && getReadHandler() != null
                && getReadParser() != null) {
            getReadHandler().removeCallbacksAndMessages(null);
            getReadHandler().post(() -> getReadParser().reset());
        }
    }

    @Override
    public void onReceiveData(byte[] data, boolean isPartial) {
        if (getReadHandler() != null && getReadParser() != null) {
            getReadHandler().post(() -> getReadParser().process(data, isPartial));
        }
    }
}
