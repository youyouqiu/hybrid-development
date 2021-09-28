package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import lombok.Data;

import java.util.Date;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/9/30 9:35
 */
@Data
public class OilDownloadUrlInfo {

    private String id;
    /**
     * 对接码组织
     */
    private String dockingCodeOrg;

    /**
     * 809转发平台
     */
    private String forwardingPlatform;

    /**
     * 油补平台下载车辆地址
     */
    private String url;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 对接码
     */
    private String dockingCode;

    /**
     * 对接码组织id
     */
    private String dockingCodeOrgId;

    /**
     * 809转发平台id
     */
    private String forwardingPlatformId;

    /**
     * 下载状态（0代表下载失败,1代表下载中2.代表下载成功）
     */
    private Integer downloadStatus;

    /**
     * 下载日期
     */
    private Date downloadTime;
    /**
     * 下载日期
     */
    private String downloadTimeStr;

}
