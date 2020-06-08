package com.hsae.platform.nfu.connect.connector;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ConnectorListener {

    @IntDef({State.STATE_WAIT, State.STATE_NONE, State.STATE_CONNECTING, State.STATE_CONNECTED})
    @Retention(RetentionPolicy.SOURCE)
    @interface State {
        int STATE_WAIT = -1;
        int STATE_NONE = 0;
        int STATE_CONNECTING = 1;
        int STATE_CONNECTED = 2;
    }

    void onStateChanged(@State int state, String msg);
    void onReceiveData(byte[] data, boolean isPartial);

}
