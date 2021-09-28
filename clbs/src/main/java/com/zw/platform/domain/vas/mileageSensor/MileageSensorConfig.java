package com.zw.platform.domain.vas.mileageSensor;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Title:里程传感器配置
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 10:40
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MileageSensorConfig extends BaseFormBean implements Serializable {

    private String mileageSensorId;//里程传感器ID
    private String  vehicleId;//车辆ID
    private Integer measuringScheme;//里程测量方案
    private Integer outputK;//输出修正系数K
    private Integer outputB;//输出修正常数B
    private String tyreSizeId;//轮胎规格ID
    private Integer uploadTime;//自动上传时间
    private Integer rollingRadius;//滚动半径修正系数
    private Double  speedRatio;//速比
    private Double  igRatio;//IG速比
    private Integer  pulseRatio;//脉冲数比例
    private Double  correctionFactor;//修正系数
    private Date nominalTime;//标定时间
    private Integer nominalStatus;//标定状态(0-空闲状态，可以标定；1-占用状态，不能标定)
    private Date enterNominalTime;//进入标定时间
    private String groupName;//车辆所属企业（组织）
    private String monitorType;//对象类型
    //临时字段
    private Integer sendStatus;//下发状态
    private String send8900ParamId;//下发8900编号
    private String send8103ParamId;//下发8103编号
    private String  plate;//车牌号
    private String groups; //所属企业/组织
    private String vehicleType;//车辆类型
    private Integer compEn;//补偿使能
    private String compEnStr;//补偿使能
    private String  tyreName;//轮胎规格
    private Double tireRollingRadius;//滚动半径
    private Integer parityCheck;//奇偶校验
    private Integer  filterFactor;//滤波系数
    private Integer   baudRate;//波特率
    private String sensorType;//型号



}
