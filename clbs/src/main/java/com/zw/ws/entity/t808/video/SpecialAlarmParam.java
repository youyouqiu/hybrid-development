package com.zw.ws.entity.t808.video;

import lombok.Data;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;

/**
 * 图像分析参数下发
 * @author: lifudong
 */
@Data
public class SpecialAlarmParam {
    private Integer thresholdValue; //特殊报警录像存储阀值；特殊报警录像占用主存储器存储阀值百分比，取值1~99，默认值为20
    private Integer keepTime; //特殊报警录像持续时间；特殊报警录像的最长持续时间，单位为分钟(min)，默认值为5
    private Integer startTime; //特殊报警标识起始时间;特殊报警发生前进行标记的录像时间，单位为分钟(min)，默认值为1
}
