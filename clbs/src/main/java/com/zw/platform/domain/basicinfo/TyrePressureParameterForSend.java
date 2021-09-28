package com.zw.platform.domain.basicinfo;

import lombok.Data;

/**
 * 胎压监测下发参数
 */
@Data
public class TyrePressureParameterForSend {

    private Integer compensatingEnable; //补传使能  1-使能（缺省值）；2-禁用；

    private Integer smoothing; //滤波系数 01-实时；02-平滑（缺省值）；03-平稳

    private Integer automaticUploadTime; //自动上传时间 1、被动 2、 10 3、 20 4、 30

    private Integer compensationFactorK; //输出修正系数K

    private Integer compensationFactorB; //输出修正系数B

    private Integer number = 0xFF; //0xFF 表示所有轮胎参数均按照此参数设置

    private Double pressure; //正常胎压值

    private Integer pressureThreshold; //胎压不平衡门限

    private Integer slowLeakThreshold; //慢漏气门限

    private Integer highTemperature; //高温阈值

    private Double lowPressure; //低压阈值

    private Double heighPressure; //高压阈值

    private Integer electricityThreshold; //电量报警阈值

    private byte[] keep1; //保留项

    private byte[] keep2; //保留项
}
