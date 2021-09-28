package com.zw.platform.domain.realTimeVideo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Parameter {
    /* socket 需要的token */
    private String accessToken;

    /* ftp服务器名称 */
    private String ftpName;

    /* 视频请求地址 */
    private String videoUrl;

    /* 实时视频请求端口 */
    private String videoPort;

    /* 音频请求端口 */
    private String audioPort;

    /* 视频回放请求端口 */
    private String resourcePort;

    /* ftp请求端口 */
    private String ftpResourcePort;

    /* 企业id */
    private String groupId;

    /* 有权限的路径 */
    private String permissionUrls;
}

