package com.hsae.platform.nfu.wrapper.message

/**
 * 版本详情
 */
class Version(
    /**
     * 是否有新版本
     */
    val isNewer: Boolean,
    /**
     * 当前车机版本号
     */
    val pVersion: String,
    /**
     * 升级包版本号（没有新版本为空）
     */
    val newVersion: String?,
    /**
     * 升级包类型（全量包或差分包）
     */
    val packageType: String?,
    /**
     * 升级包大小（单位MB，例如：1.23MB）
     */
    val size: String?,
    /**
     * 升级包大小（单位字节数，例如：12345）
     */
    val bytes: String?,
    /**
     * 升级包更新日志
     */
    val resume: String?
) {
    override fun toString(): String {
        return "Version(isNewer=$isNewer, pVersion='$pVersion', newVersion=$newVersion, packageType=$packageType, size=$size, bytes=$bytes, resume=$resume)"
    }
}