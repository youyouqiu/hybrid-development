package com.zw.platform.dto.platformInspection;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/***
 @Author lijie
 @Date 2020/11/24 10:22
 @Description 9710下发实体
 @version 1.0
 **/
@Data
public class PlatformInspectionParamSendDTO implements T808MsgBody {

    //巡检类型0x00：车辆运行监测；0x01：驾驶员驾驶行为监测
    private Integer type;

}
