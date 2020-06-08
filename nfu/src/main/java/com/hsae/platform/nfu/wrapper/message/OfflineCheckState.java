package com.hsae.platform.nfu.wrapper.message;

/**
 * 脱机检查状态
 */
public enum OfflineCheckState {
    /**
     * 不合法
     */
    illegal,
    /**
     * 开始检查
     */
    start,
    /**
     * 发生错误
     */
    error,
    /**
     * 结束
     */
    end
}
