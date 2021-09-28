package com.zw.platform.vo.realTimeVideo;


import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2018年1月2日 下午4:14:05
* 类说明:
*/
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class VideoParamVo implements Serializable {

    private String videoSetting;

    private String videoChannelSettings;

    private String alarmParams;

    private String videoSleepSetting;

    private String recordingSetting;
    
    private String vehicleId;

    // 对象类型 (0:车 1:人 2:物)
    private String monitorType;
}
