package com.zw.adas.domain.riskManagement.show;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/3/4 9:39
 @Description 风险监管事件信息
 @version 1.0
 **/
@Data
public class AdasEventShow {

    private transient String vehicleId;

    /**
     * 事件id
     */
    private String id;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 企业名称
     */

    private String groupName;

    /**
     * 车辆运营类别
     */

    private String vehiclePurpose;

    /**
     * 风险类型
     */
    private String riskType;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 速度
     */
    private Double speed;

    /**
     * 天气情况
     */
    private String weather;

    /**
     * 事件报警时间
     */

    private String eventTime;

    /**
     * 报警位置
     */
    private String address;

    /**
     * 报警名称
     */

    private String eventName;

    /**
     * 是否有终端报警图片
     */
    private Integer picFlag;

    /**
     * 是否有终端报警视频
     */
    private Integer videoFlag;

    /**
     * 车辆状态
     */
    private String vehicleStatus;

    /**
     * 风险等级
     */
    private Integer level;

    /**
     * 协议类型(车辆实时)
     */
    private Integer deviceType;


    /**
     * 协议类型
     */
    private Integer protocolType;

    /**
     * 历史(事件中存的，用于前端控制车辆状态的展现，黑标车不展现)
     */
    private Integer eventProtocolType;
}
