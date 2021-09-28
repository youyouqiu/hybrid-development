package com.zw.adas.domain.riskManagement.show;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/3/1 15:28
 @Description 监管二期二阶段风控页面
 @version 1.0
 **/
@Data
public class AdasRiskShow {

    private String vehicleId;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 风险id
     */
    private String riskId;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 车辆运营类别
     */
    private String vehiclePurpose;

    /**
     * 风险类型
     */
    private String riskType;

    /**
     * 所属企业
     */
    private String groupName;

    /**
     * 报警时间
     */
    private String warningTime;

    /**
     * 终端视频标志
     */
    private Integer videoFlag;

    /**
     * 终端图片标志
     */
    private Integer picFlag;

    /**
     * 督办状态
     */
    private Integer overseeStatus;

    private String status;

    /**
     * 报警处置截止时限
     */
    private String dealDeadTime;

    /**
     * 风险编号
     */
    private String riskNumber;

}
