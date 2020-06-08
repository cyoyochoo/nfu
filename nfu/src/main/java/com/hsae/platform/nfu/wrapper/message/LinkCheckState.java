package com.hsae.platform.nfu.wrapper.message;

/**
 * 互联检查状态
 */
public enum LinkCheckState {
    /**
     * 不合法
     */
    illegal,
    /**
     * 开始连接
     */
    start,
    /**
     * 连接中
     */
    linking,
    /**
     * 已连接
     */
    linked,
    /**
     * 检查更新中
     */
    checking,
    /**
     * 发生错误
     */
    error,
    /**
     * 结束
     */
    end
}
