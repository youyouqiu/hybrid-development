package com.zw.platform.domain.vas.oilmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * <p>Title: 流量传感器与车的绑定Query</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年9月19日上午9:18:22
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FluxSensorBindQuery extends BaseQueryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 油耗传感器
     */
    private String id;

    /**
     * 传感器编号
     */
    private String oilWearNumber;

    /**
     * 外设ID
     */
    private Integer deviceNumber;

    /**
     * 参数长度
     */
    private String parameterLength;

    /**
     * 波特率
     */
    private String baudRate;

    /**
     * 奇偶校验
     */
    private String parity;

    /**
     * 补偿使能
     */
    private Integer inertiaCompEn;

    /**
     * 滤波系数
     */
    private Integer filterFactor;

    /**
     * 自动上传时间
     */
    private Integer autoUploadTime;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionK;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionB;

    /**
     * 量程
     */
    private Integer ranges;

    /**
     * 燃料选择
     */
    private Integer fuelSelect;

    /**
     * 测量方案
     */
    private Integer meteringSchemes;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private Integer status; // 下发状态

    private String brand; // 车牌号

    private String groupId; // 组织

    private String vehicleType; // 车辆类型

    private String assignmentId; // 分组

    private Integer protocol;//协议类型

    private String vehicleId;//车辆id
}
