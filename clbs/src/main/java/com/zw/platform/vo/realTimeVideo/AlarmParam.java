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
public class AlarmParam implements Serializable {

    private String id;

    @Deprecated
    private Integer ignore; // 屏蔽报警

    private Integer alarmPush; // 报警推送设置（-1、屏蔽 0、无 1、局部 2、全局）

    private String alarmName; // 报警名称

    private String paramValue; // 报警参数设置值

    private String vehicleId; // 监控对象id

    private String alarmParameterId;

    private String pos; // 报警标识
}
