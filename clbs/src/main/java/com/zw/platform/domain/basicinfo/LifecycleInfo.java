package com.zw.platform.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tdz on 2016/8/1.
 */
@Data
public class LifecycleInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 服务周期
     */
    private String id;

    /**
     * 计费日期
     */
    private Date billingDate;

    /**
     * 到期日期
     */
    private Date expireDate;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

}
