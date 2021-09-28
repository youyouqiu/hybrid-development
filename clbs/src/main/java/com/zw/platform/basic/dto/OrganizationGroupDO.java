package com.zw.platform.basic.dto;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @author wanxing
 * @Title: 企业-分组实体
 * @date 2020/12/1617:31
 */
@Data
public class OrganizationGroupDO {

    private String id = UUID.randomUUID().toString();
    /**
     * 组织Id
     */
    private String orgId;
    /**
     * 分组Id
     */
    private String groupId;
    private int flag;
    private Date createDataTime;
    private Date updateDataTime;
    private String createDataUser;
    private String updateDataUser;

}
