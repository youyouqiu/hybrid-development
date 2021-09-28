package com.zw.platform.domain.accessPlatform;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
public class AccessPlatformQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String platformName;

    private Integer status;

    private String ip;

    private String type;

    private Date createDataTime;

    private Date updateDataTime;

    private String createDataUsername;

    private String updateDataUsername;
}
