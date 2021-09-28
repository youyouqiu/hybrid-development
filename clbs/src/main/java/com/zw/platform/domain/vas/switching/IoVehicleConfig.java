package com.zw.platform.domain.vas.switching;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 信号位绑定表
 *
 * @author zhangsq
 * @date 2018/6/28 9:39
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IoVehicleConfig extends BaseFormBean {

    //IO位置
    private Integer ioSite;

    //功能类型id
    private String functionId;

    //车辆ID
    private String vehicleId;

    //io类型（1：终端io；2：io采集1；3:io采集2）
    private Integer ioType;

    //高电平状态类型（状态1，状态2）
    private Integer highSignalType;

    //低电平状态类型（状态1，状态2）
    private Integer lowSignalType;

}
