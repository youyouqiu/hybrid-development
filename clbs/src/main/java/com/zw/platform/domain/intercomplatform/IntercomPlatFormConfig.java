package com.zw.platform.domain.intercomplatform;

import lombok.Data;

import java.util.Date;

/**
 * Created by LiaoYuecai on 2017/3/7.
 */
@Data
public class IntercomPlatFormConfig {
    private String id;
    private String configId;
    private String intercomPlatformId;
    private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
}
