package com.zw.platform.domain.forwardplatform;

import lombok.Data;

import java.util.Date;

/**
 * Created by LiaoYuecai on 2017/2/28.
 */
@Data
public class ThirdPlatForm {
    private String id;
    private Integer status;//状态
    private String platformIp;//IP地址
    private Integer platformPort;//端口
    private String description;//描述
    private Date createDataTime;
    private Date updateDataTime;
    private String createDataUsername;
    private String updateDataUsername;
}
