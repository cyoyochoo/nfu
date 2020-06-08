package com.hsae.platform.nfu.router.listeners;

public interface FotaListener {
    void onFotaResponse(boolean response, String md5, String version, String projectId);
    void onFotaDownload(int fileId, String fileMD5, int blockIndex);
    void onFotaResult(int result, String reason);
}
