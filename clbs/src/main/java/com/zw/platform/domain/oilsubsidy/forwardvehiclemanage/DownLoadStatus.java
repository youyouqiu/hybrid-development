package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

/**
 * @Author: zjc
 * @Description: 下载状态（0代表下载失败,1代表下载中2.代表下载成功）
 * @Date: create in 2020/10/14 9:32
 */
public enum DownLoadStatus {
    FAILED(0, "下载失败"), DOWNLOADING(1, "下载中"), SUCCESS(2, "下载成功");

    DownLoadStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    int code;
    String name;

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
