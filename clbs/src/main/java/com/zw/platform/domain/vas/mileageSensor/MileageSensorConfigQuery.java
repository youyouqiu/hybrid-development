package com.zw.platform.domain.vas.mileageSensor;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * Title:
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:15
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MileageSensorConfigQuery extends BaseQueryBean implements Serializable {

    private String groupId;

    private String assignmentId;

    private String brand;//车牌号

    private String sensorType;//传感器类别

    private Integer protocol;//协议类型

    private String vehicleId;

}
