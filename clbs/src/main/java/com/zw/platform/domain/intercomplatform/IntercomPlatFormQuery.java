package com.zw.platform.domain.intercomplatform;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * <p>
 * Title: 对讲设备配置Query
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * 
 * @author: wangying
 * @date 2016年10月9日下午6:15:22
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class IntercomPlatFormQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
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
