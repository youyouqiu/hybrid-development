package com.zw.platform.domain.vas.f3.sensorparam;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/5/12.
 */
@Data
public class EratureParam {
    private Integer compensatingEnable;//补偿使能
    private Integer smoothing;//滤波系数
    private Integer automaticUploadTime;//自动上传时间
    private Integer compensationFactorK;//输出修正系数K
    private Integer compensationFactorB;//输出修正常数B
    private byte[] keep1;//保留项
    private Integer maxTemperature;//报警上阈值
    private Integer minTemperature;//报警下阈值
    private Integer timeOutValue;//超出阈值时间阈值
    private byte[] keep2;//保留项

}
