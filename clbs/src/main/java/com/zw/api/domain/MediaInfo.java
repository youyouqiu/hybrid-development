package com.zw.api.domain;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

public class MediaInfo {
    @ApiModelProperty("多媒体资源URL")
    private String url;

    @ApiModelProperty("多媒体资源类型 0:图片 1:音频 2:视频")
    private int type;

    @ApiModelProperty("多媒体资源创建时间")
    private LocalDateTime createTime;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
