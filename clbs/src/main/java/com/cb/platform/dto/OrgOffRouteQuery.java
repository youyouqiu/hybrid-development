package com.cb.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/24 10:08
 */
@Data
public class OrgOffRouteQuery implements Serializable {
    private static final long serialVersionUID = 7945811415964726104L;
    /**
     * 企业id
     */
    private String orgId;
    /**
     * 月份 yyyy-MM
     */
    private String month;
}
