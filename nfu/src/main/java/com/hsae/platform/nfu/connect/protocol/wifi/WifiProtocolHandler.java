package com.hsae.platform.nfu.connect.protocol.wifi;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.hsae.platform.nfu.connect.LinkListener;
import com.hsae.platform.nfu.connect.Util;
import com.hsae.platform.nfu.connect.protocol.ProtocolConstant;
import com.hsae.platform.nfu.connect.protocol.wifi.parser.BaseParser;
import com.hsae.platform.nfu.connect.protocol.wifi.parser.WifiParser;

import org.json.JSONObject;

public class WifiProtocolHandler extends BaseWifiProtocolHandler {
    /*private static final long HEART_BEAT_INTERVAL = 4000L;*/
    private Handler readHandler;
    private WifiParser wifiParser;

    public WifiProtocolHandler(String host, int port, LinkListener linkListener) {
        super(host, port, linkListener);
        HandlerThread readThread = new HandlerThread(
                "readThread", Process.THREAD_PRIORITY_BACKGROUND
        );
        readThread.start();
        readHandler = new Handler(readThread.getLooper());
        wifiParser = new WifiParser();
    }

    /*private Runnable heartBeatRunnable = () -> {
        getWifiConnector().heartBeat();
        writeHandler.postDelayed(this.heartBeatRunnable, HEART_BEAT_INTERVAL);
    };*/

    @Override
    Handler getReadHandler() {
        return readHandler;
    }

    @Override
    BaseParser getReadParser() {
        return wifiParser;
    }

    @Override
    public void send(byte[] data) {
        super.send(data);
        /*writeHandler.removeCallbacks(heartBeatRunnable);
        writeHandler.postDelayed(heartBeatRunnable, HEART_BEAT_INTERVAL);*/
    }

    @Override
    public void onStateChanged(int state, String msg) {
        super.onStateChanged(state, msg);
        AsyncTask.SERIAL_EXECUTOR.execute(() -> {
            if (getLinkListener() != null)
                getLinkListener().onWifiStateChanged(state, msg);
        });
    }

    @Override
    public void stop() {
        super.stop();
        readHandler.removeCallbacksAndMessages(null);
        readHandler.getLooper().quit();
    }

    /**
     * 手机app主动上报状态
     * @param state 0 准备好 1 退出
     */
    public void notifyPhoneState(int state) {
        try {
            JSONObject json = new JSONObject();
            json.put(ProtocolConstant.KEY_MODE, 5);
            json.put(ProtocolConstant.TYPE_REPLY, "revephonestate");
            JSONObject args = new JSONObject();
            args.put("state", state);
            json.put(ProtocolConstant.KEY_ARGS, args);

            byte[] body = json.toString().getBytes(Util.UTF_8);
            send(Util.getWifiTypeData(body));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
