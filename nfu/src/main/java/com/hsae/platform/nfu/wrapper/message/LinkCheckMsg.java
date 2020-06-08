package com.hsae.platform.nfu.wrapper.message;

import java.util.ArrayDeque;

/**
 * 互联检查消息
 */
public class LinkCheckMsg {

    private static ArrayDeque<LinkCheckMsg> stack = new ArrayDeque<>();

    public static synchronized LinkCheckMsg get() {
        return stack.isEmpty() ? new LinkCheckMsg() : stack.pop();
    }

    public static synchronized void recycle(LinkCheckMsg checkMsg) {
        stack.push(checkMsg);
    }

    /**
     * 对象回收复用
     */
    public void recycle() {
        recycle(this);
    }

    private LinkCheckState state = LinkCheckState.illegal;
    private Version version = null;
    private String detail = null;

    /**
     * 状态
     * @return {@link LinkCheckState}
     */
    public LinkCheckState getState() {
        return state;
    }

    public LinkCheckMsg setState(LinkCheckState state) {
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

    public LinkCheckMsg setVersion(Version version) {
        this.version = version;
        return this;
    }

    /**
     * 详情（比如连接断开原因等）
     * @return 信息描述
     */
    public String getDetail() {
        return detail;
    }

    public LinkCheckMsg setDetail(String detail) {
        this.detail = detail;
        return this;
    }
}
