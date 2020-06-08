package com.hsae.platform.nfu.wrapper.message;

/**
 * 近场升级状态
 */
public enum UpgradeState {
    /**
     * 不合法
     */
    illegal,
    /**
     * 开始
     */
    start,
    /**
     * 下载中
     */
    downloading,
    /**
     * 下载完成
     */
    downloaded,
    /**
     * 传输中
     */
    transmitting,
    /**
     * 传输完成
     */
    transmitted,
    /**
     * 发生错误
     */
    error,
    /**
     * 结束
     */
    end
}
