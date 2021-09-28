package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TyrePressureSettingQuery extends BaseQueryBean {

    private String groupId;

    private String assignmentId;

    private String brand;//车牌号

    private String sensorType;//传感器类别

    private Integer protocol;//协议类型

    private String vehicleId;
}
