package com.hsae.platform.nfu.connect.protocol.wifi;

import android.os.AsyncTask;
import android.os.Handler;

import com.hsae.platform.nfu.connect.LinkListener;
import com.hsae.platform.nfu.connect.protocol.wifi.parser.BaseParser;


public class FotaProtocolHandler extends BaseWifiProtocolHandler {
    public FotaProtocolHandler(String host, int port, LinkListener linkListener) {
        super(host, port, linkListener);
    }

    @Override
    Handler getReadHandler() {
        return null;
    }

    @Override
    BaseParser getReadParser() {
        return null;
    }

    @Override
    public void onStateChanged(int state, String msg) {
        super.onStateChanged(state, msg);
        AsyncTask.SERIAL_EXECUTOR.execute(() -> {
            if (getLinkListener() != null)
                getLinkListener().onFotaStateChanged(state, msg);
        });
    }
}
