package com.zw.platform.domain.vas.f3.sensorparam;

import lombok.Data;

/**
 * <p>
 * Title:正反转
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年07月06日 17:47
 */
@Data
public class PositiveParam {

    private Integer compensatingEnable;//补偿使能
    private byte[] keep1;//保留项
    private Integer automaticUploadTime;//自动上传时间
    private Integer compensationFactorK;//输出修正系数K
    private Integer compensationFactorB;//输出修正常数B
    private byte[] keep2;//保留项
}
