package com.zw.platform.dto.platformInspection;

import lombok.Data;

/***
 @Author lijie
 @Date 2020/11/20 16:06
 @Description 平台巡检下发
 @version 1.0
 **/
@Data
public class PlatformInspectionParamDTO {

    private String vehicleId;

    //巡检类型（1.车辆运行监测巡检2.驾驶员驾驶行为监测巡检 3.驾驶员身份识别巡检）
    private Integer inspectionType;

    private String time;

    private String brand;

    //巡检记录id
    private String inspectionId;

    private String idAndMsgSn;

}
