package com.zw.platform.domain.basicinfo;

import lombok.Data;

/**
 * 胎压监测个性参数
 */
@Data
public class TyrePressureParameter {

    private Double pressure; //正常胎压值

    private Integer pressureThreshold; //胎压不平衡门限

    private Integer slowLeakThreshold; //慢漏气门限

    private Integer highTemperature; //高温阈值

    private Double lowPressure; //低压阈值

    private Double heighPressure; //高压阈值

    private Integer electricityThreshold; //电量报警阈值

    private Integer automaticUploadTime; //自动上传时间 1、被动 2、 10 3、 20 4、 30

    private Integer compensationFactorK; //输出修正系数K

    private Integer compensationFactorB; //输出修正系数B

    private String automaticUploadTimeStr;
}
