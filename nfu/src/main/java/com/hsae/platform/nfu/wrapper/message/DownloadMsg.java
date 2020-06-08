package com.hsae.platform.nfu.wrapper.message;

import java.util.ArrayDeque;

/**
 * 预下载消息
 */
public class DownloadMsg {

    private static ArrayDeque<DownloadMsg> stack = new ArrayDeque<>();

    public static synchronized DownloadMsg get() {
        return stack.isEmpty() ? new DownloadMsg() : stack.pop();
    }

    public static synchronized void recycle(DownloadMsg downloadMsg) {
        stack.push(downloadMsg);
    }

    /**
     * 对象回收复用
     */
    public void recycle() {
        recycle(this);
    }

    private DownloadState state = DownloadState.illegal;
    private int progress = -1;
    private String detail = null;

    /**
     * 获取状态
     * @return {@link DownloadState}
     * @see DownloadState
     */
    public DownloadState getState() {
        return state;
    }

    public DownloadMsg setState(DownloadState state) {
        this.state = state;
        return this;
    }

    /**
     * 下载进度（0-100）
     * @return 进度值
     */
    public int getProgress() {
        return progress;
    }

    public DownloadMsg setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    /**
     * 具体信息（比如发生异常时）
     * @return 信息描述
     */
    public String getDetail() {
        return detail;
    }

    public DownloadMsg setDetail(String detail) {
        this.detail = detail;
        return this;
    }
}
