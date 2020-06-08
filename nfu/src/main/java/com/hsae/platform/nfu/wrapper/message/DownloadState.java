package com.hsae.platform.nfu.wrapper.message;

/**
 * 下载状态
 */
public enum DownloadState {
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
     * 发生错误
     */
    error,
    /**
     * 结束
     */
    end
}
