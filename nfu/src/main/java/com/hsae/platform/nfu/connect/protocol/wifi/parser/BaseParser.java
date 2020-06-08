package com.hsae.platform.nfu.connect.protocol.wifi.parser;

public abstract class BaseParser {
    private final static int HEAD_LEN = 4;
    private int headEaten;
    private byte[] headPacket = new byte[HEAD_LEN];
    private int bodyLen;
    private int bodyEaten;
    private byte[] bodyPacket;

    private void getBodyLen() {
        bodyLen  = (headPacket[0] << 24) & 0xff000000;
        bodyLen |= (headPacket[1] << 16) & 0xff0000;
        bodyLen |= (headPacket[2] << 8)  & 0xff00;
        bodyLen |=  headPacket[3]        & 0xff;
    }

    public void reset() {
        headEaten = 0;
        bodyEaten = 0;
        bodyPacket = null;
    }

    public void process(byte[] data, boolean isPartial) {
        int dataLen = data.length;

        int headLeftLen = HEAD_LEN - headEaten;
        if (headLeftLen > 0) {//process head

            if (headLeftLen >= dataLen) {//all are head

                System.arraycopy(data, 0, headPacket, headEaten, dataLen);
                headEaten += dataLen;

                //get bodyLen
                if (headEaten == HEAD_LEN) {
                    getBodyLen();
                }


            } else {//contains body

                System.arraycopy(data, 0, headPacket, headEaten, headLeftLen);
                headEaten = HEAD_LEN;
                getBodyLen();
                //body
                int dataLeftLen = dataLen - headLeftLen;
                int leftLen = dataLeftLen - bodyLen;
                if (leftLen >= 0) {//get
                    byte[] body = new byte[bodyLen];
                    System.arraycopy(data, headLeftLen, body, 0, bodyLen);
                    parse(body);
                    reset();
                    if (leftLen > 0) {
                        byte[] leftData = new byte[leftLen];
                        System.arraycopy(data, headLeftLen + bodyLen, leftData, 0, leftLen);
                        process(leftData, true);
                    }
                } else {//cache
                    bodyPacket = new byte[dataLeftLen];
                    System.arraycopy(data, headLeftLen, bodyPacket, 0, dataLeftLen);
                    bodyEaten = dataLeftLen;
                }
            }



        } else {//process body
            int bodyLeftLen = bodyLen - bodyEaten;
            int leftLen = dataLen - bodyLeftLen;

            if (leftLen >= 0) {//get
                byte[] body;
                if (bodyPacket == null) {
                    body = new byte[bodyLeftLen];
                    System.arraycopy(data, 0, body, 0, bodyLeftLen);
                } else {
                    int oldLen = bodyPacket.length;
                    body = new byte[oldLen + bodyLeftLen];
                    System.arraycopy(bodyPacket, 0, body, 0, oldLen);
                    System.arraycopy(data, 0, body, oldLen, bodyLeftLen);
                }
                parse(body);
                reset();

                if (leftLen > 0) {
                    byte[] leftData = new byte[leftLen];
                    System.arraycopy(data, bodyLeftLen, leftData, 0, leftLen);
                    process(leftData, true);
                }

            } else if (bodyPacket == null) {//cache
                bodyPacket = new byte[dataLen];
                System.arraycopy(data, 0, bodyPacket, 0, dataLen);
                bodyEaten = dataLen;
            } else {
                byte[] old = bodyPacket;
                int oldLen = old.length;
                bodyPacket = new byte[oldLen + dataLen];
                System.arraycopy(old, 0, bodyPacket, 0, oldLen);
                System.arraycopy(data, 0, bodyPacket, oldLen, dataLen);
                bodyEaten += dataLen;
            }
        }
    }

    public abstract void parse(byte[] data);
}
