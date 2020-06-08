package com.hsae.platform.nfu.connect;

import java.nio.charset.Charset;

public class Util {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static byte[] getWifiTypeData(byte[] rawData) {
        int bodyL = rawData.length;
        byte[] wifiData = new byte[bodyL + 4];
        wifiData[0] = (byte) ((bodyL >> 24) & 0xff);
        wifiData[1] = (byte) ((bodyL >> 16) & 0xff);
        wifiData[2] = (byte) ((bodyL >> 8) & 0xff);
        wifiData[3] = (byte) (bodyL & 0xff);
        System.arraycopy(rawData, 0, wifiData, 4, bodyL);
        return wifiData;
    }

}
