package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by LiaoYuecai on 2017/4/11.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceParam extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String vid;
    private Integer heartSpace;
    private Integer tcpAckTimeOut;
    private Integer tcpReUpTimes;
    private Integer udpAckTimeOut;
    private Integer udpReUpTimes;
    private Integer smsAckTimeOut;
    private Integer smsReUpTimes;
    /**
     * 拐点补传角度
     */
    private Integer inflectionPointAdditional;
    /**
     * 电子围栏半径（非法位移阈值），单位为米
     */
    private Integer electronicFenceRadius;
}
