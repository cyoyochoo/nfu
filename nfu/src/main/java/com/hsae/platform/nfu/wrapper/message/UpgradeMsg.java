package com.hsae.platform.nfu.wrapper.message;

import java.util.ArrayDeque;

/**
 * 近场升级消息
 */
public class UpgradeMsg {

    private static ArrayDeque<UpgradeMsg> stack = new ArrayDeque<>();

    public static synchronized UpgradeMsg get() {
        return stack.isEmpty() ? new UpgradeMsg() : stack.pop();
    }

    public static synchronized void recycle(UpgradeMsg upgradeMsg) {
        stack.push(upgradeMsg);
    }

    /**
     * 对象回收复用
     */
    public void recycle() {
        recycle(this);
    }

    private UpgradeState state = UpgradeState.illegal;
    private int progress = -1;
    private String detail = null;

    /**
     * 状态
     * @return {@link UpgradeState}
     */
    public UpgradeState getState() {
        return state;
    }

    public UpgradeMsg setState(UpgradeState state) {
        this.state = state;
        return this;
    }

    /**
     * 进度（0-100），下载进度或传输进度（根据{@link UpgradeState}区分）
     * @return 进度值
     */
    public int getProgress() {
        return progress;
    }

    public UpgradeMsg setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    /**
     * 详情
     * @return 信息描述
     */
    public String getDetail() {
        return detail;
    }

    public UpgradeMsg setDetail(String detail) {
        this.detail = detail;
        return this;
    }
}
