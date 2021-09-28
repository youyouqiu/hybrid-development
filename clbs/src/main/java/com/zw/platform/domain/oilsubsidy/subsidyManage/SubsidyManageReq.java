package com.zw.platform.domain.oilsubsidy.subsidyManage;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 补发管理补发入参实体
 * @author tianzhangxu
 * @Date 2021/3/25 18:01
 */
@Data
public class SubsidyManageReq implements Serializable {
    private static final long serialVersionUID = 6139465219411985655L;

    /**
     * 转发车辆id
     */
    private List<String> forwardVehicleIds;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
