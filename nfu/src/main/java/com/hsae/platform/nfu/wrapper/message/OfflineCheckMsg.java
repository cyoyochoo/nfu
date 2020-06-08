package com.hsae.platform.nfu.wrapper.message;

import java.util.ArrayDeque;

/**
 * 脱机检查消息
 */
public class OfflineCheckMsg {

    private static ArrayDeque<OfflineCheckMsg> stack = new ArrayDeque<>();

    public static synchronized OfflineCheckMsg get() {
        return stack.isEmpty() ? new OfflineCheckMsg() : stack.pop();
    }

    public static synchronized void recycle(OfflineCheckMsg versionMsg) {
        stack.push(versionMsg);
    }

    /**
     * 对象回收复用
     */
    public void recycle() {
        recycle(this);
    }

    private OfflineCheckState state = OfflineCheckState.illegal;
    private Version version = null;
    private String detail = null;

    /**
     * 状态
     * @return {@link OfflineCheckState}
     */
    public OfflineCheckState getState() {
        return state;
    }

    public OfflineCheckMsg setState(OfflineCheckState state) {
        this.state = state;
        return this;
    }

    /**
     * 版本信息
     * @return {@link Version}
     */
    public Version getVersion() {
        return version;
    }

    public OfflineCheckMsg setVersion(Version version) {
        this.version = version;
        return this;
    }

    /**
     * 详情
     * @return 信息描述
     */
    public String getDetail() {
        return detail;
    }

    public OfflineCheckMsg setDetail(String detail) {
        this.detail = detail;
        return this;
    }
}
