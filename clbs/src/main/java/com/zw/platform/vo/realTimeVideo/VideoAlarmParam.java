package com.zw.platform.vo.realTimeVideo;


import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2017年12月28日 下午4:35:50
* 类说明:视频报警参数页面显示实体类
*/
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class VideoAlarmParam implements Serializable {
    
    private String id;
    
    private String alarmParam;

    private String vehicleId;

    private Integer thresholdValue;

    private Integer keepTime;

    private Integer startTime;

}
