package com.zw.platform.domain.intercomplatform;

import lombok.Data;

import java.util.Date;

/**
 * Created by LiaoYuecai on 2017/3/15.
 */
@Data
public class IntercomPlatForm {
    private String id;
    private String platformName;
    private Integer status;//状态
    private String platformIp;//IP地址
    private Integer platformPort;//端口
    private String description;//描述
    private Date createDataTime;
    private Date updateDataTime;
    private String createDataUsername;
    private String updateDataUsername;
}
