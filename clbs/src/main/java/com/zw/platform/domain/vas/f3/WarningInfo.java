package com.zw.platform.domain.vas.f3;

import lombok.Data;


/**
 * <p>
 * Title:预警数据
 * <p> 0x64
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年08月10日 17:30
 */
@Data
public class WarningInfo {

    private Integer sensorId;//预警类型

    /**
     * 01：开始报警；
     * 02：报警结束；
     * 0x10事件标识
     */
    private Integer status;//报警状态

    private Integer type;//报警类型

    private Integer level;//报警级别

    private Integer roadMarkings;//道路标识类型

    private Integer roadMarkingsData;//道路标识识别数据

    private Integer fatigueLevel;//疲劳等级

    private Integer reserveValue;//驾驶限速值

    private Integer keep;//预留值

    private Integer mediaTotal;//报警附加多媒体信息列表总数

    private String mediaInfo;//多媒体信息列表

    private String startTime;//视频开始时间

    private String endTime;//视频结束时间

}
