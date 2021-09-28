package com.zw.platform.domain.vas.f3.sensorparam;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/5/12.
 */
@Data
public class MileageParam {
    private Integer compensatingEnable;//补偿使能
    private Integer keep1 = 0;//保留项
    private Integer automaticUploadTime;//自动上传时间
    private Integer compensationFactorK;//输出修正系数K
    private Integer compensationFactorB;//输出修正常数B
    private byte[] keep2 = new byte[12];//保留项
    private Integer rollingRadius;//轮胎滚动半径
    private Integer rollingRadiusCompensationFactor;//滚动半径修正系数
    private Integer speedRatio;//速比
    private Integer mileageMeasurementScheme;//里程测量方案
    private byte[] keep3 = new byte[26];//保留项

}
