package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by jiangxiaoqiang on 2016/10/14.
 */
@Data
public class WorkDayDataInfo implements Serializable {
    private Integer duration;//该状态持续时间
    private Integer engineState;//引擎状态
    private Integer shakeRate;//震动频率
}
