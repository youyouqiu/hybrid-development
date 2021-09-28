package com.zw.adas.domain.riskManagement.show;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/2/18 16:22
 @Description 风险处置记录展现实体实体bean
 @version 1.0
 **/
@Data
public class AdasRiskDisPoseRecordShow {
    private String id;

    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 报警类型
     */
    private String riskType;

    /**
     * 报警等级
     */
    private String riskLevel;

    /**
     * 报警开始时间
     */
    private String warTime;

    /**
     * 报警结束时间
     */
    private String overTime;

    /**
     * 报警位置信息
     */
    private String formattedAddress;

    /**
     * 天气情况
     */
    private String weather;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 司机名称
     */
    private String driver;

    /**
     * 驾驶证号
     */
    private String driverNo;

    /**
     * 企业名称
     */
    private String groupName;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 风险编号
     */
    private String riskNumber;

    /**
     * 处理人
     */
    private String dealUser;

    /**
     * 处理时间
     */
    private String dealTime;

    /**
     * 风控结果
     */
    private String riskResult;

    /**
     * 视频终端证据
     */
    private Boolean hasVideo;

    /**
     * 图片终端证据
     */
    private Boolean hasPic;

    /**
     * 是否有多媒体文件
     */
    private Boolean hasMedia;

    /**
     * 速度
     */

    private String speed;

}
