package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class OBDManagerSettingQuery extends BaseQueryBean implements Serializable {

    private static final long serialVersionUID = 2291887065168779852L;

    private Integer status; // 下发状态

    private String brand; // 车牌号

    private String groupId; // 组织

    private String vehicleType; // 车辆类型

    private String assignmentId; // 分组

    private String protocol; // 协议类型

    private String vehicleId;
}
