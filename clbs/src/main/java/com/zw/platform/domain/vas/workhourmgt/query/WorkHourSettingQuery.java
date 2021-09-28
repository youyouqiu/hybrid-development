package com.zw.platform.domain.vas.workhourmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author zhouzongbo on 2018/5/28 16:46
 */
@Data
@EqualsAndHashCode
public class WorkHourSettingQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 2291887065168779852L;

    private Integer status; // 下发状态

    private String brand; // 车牌号

    private String groupId; // 组织

    private String vehicleType; // 车辆类型

    private String assignmentId; // 分组

    private String protocol; // 协议类型

    private String vehicleId;
}
