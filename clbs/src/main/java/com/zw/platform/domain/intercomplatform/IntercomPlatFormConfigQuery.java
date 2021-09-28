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
public class IntercomPlatFormConfigQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
    private String id;
    private String configId;
    private String intercomPlatformId;
    private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
}
