package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/1/10 16:40
 */
@Data
public class PassCloudMileageReport implements Serializable {
    private static final long serialVersionUID = 4877956938466553266L;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 监控对象名称
     */
    private String monitorName;
    /**
     * 对象类型
     */
    private String objectType;
    /**
     * 标识颜色
     */
    private String signColor;
    /**
     * 所属企业
     */
    private String groupName;

    /**
     * 里程每日详情
     */
    List<PassCloudMileageDailyDetail> detail;
}
