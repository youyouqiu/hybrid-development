package com.zw.platform.domain.vas.f3.sensorparam;

import lombok.Data;

/**
 * <p>
 * Title:湿度参数下发
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年07月06日 14:56
 */
@Data
public class HumidityParam {
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
