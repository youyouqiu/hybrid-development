package com.zw.platform.domain.vas.loadmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/***
 @Author gfw
 @Date 2018/9/10 9:38
 @Description 载重接口参数
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class LoadVehicleSettingQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer status; // 下发状态

    private String brand; // 车牌号

    private String groupId; // 组织

    private String vehicleType; // 车辆类型

    private String assignmentId; // 分组

    private String protocol; // 协议类型

    private String vehicleId;


}
