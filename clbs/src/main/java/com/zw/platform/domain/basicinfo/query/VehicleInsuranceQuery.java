package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 车辆保险查询实体
 * @author zhouzongbo on 2018/5/10 9:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleInsuranceQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = -1237790828191870049L;

    /**
     *
     */
    private Integer insuranceTipType = 0;

    /**
     * 保险单id
     */
    private List<String> insuranceList = new ArrayList<>();

    /**
     * 组织id
     */
    private List<String> groupList = new ArrayList<>();
    /**
     * userId
     */
    private String userUUID;
}
