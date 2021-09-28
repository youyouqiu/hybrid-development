package com.zw.platform.dto.alarm;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/10/21 11:26
 */
@Data
public class AlarmPageInfoDto {
    private String id;

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象
     */
    private String monitorName;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 所属企业
     */
    private String name;

    /**
     * 所属分组
     */
    private String assignmentName;

    /**
     * 从业人员
     */
    private String employeeName;

    /**
     * 处理状态 0:未处理 1:已处理
     */
    private Integer status;

    /**
     * 报警类型
     */
    private Integer alarmType;
    private String description;

    /**
     * 严重程度
     */
    private Double severity;

    /**
     * 报警来源
     */
    private Integer alarmSource;

    /**
     * 报警开始速度
     */
    private String speed;

    /**
     * 道路类型
     * 1：高速路 2：都市高速路 3：国道 4：省道
     * 5：县道 6：乡村道路 7：其他道路
     */
    private Integer roadType;

    /**
     * 行车记录仪速度
     */
    private String recorderSpeed;

    /**
     * 平台限速
     */
    private Double speedLimit;

    /**
     * 路网限速
     */
    private Double roadNetSpeedLimit;

    /**
     * 超速时长
     */
    private Integer speedTime;

    /**
     * 报警持续时间
     * 过检需要
     * 接口未返回该字段,用超速时长赋值
     */
    private Integer continuousTime;

    /**
     * 报警开始时间 格式:yyyy-MM-dd HH:mm:ss
     */
    private String startTime;
    /**
     * 报警开始时间 格式:yyyyMMddHHmmssSSS
     */
    private String alarmStartTime;

    /**
     * 报警结束时间 格式:yyyy-MM-dd HH:mm:ss
     */
    private String endTime;
    /**
     * 报警结束时间 格式:yyyyMMddHHmmssSSS
     */
    private String alarmEndTime;

    /**
     * 报警开始位置(经度,纬度)
     */
    private String alarmStartLocation;
    /**
     * 报警开始经度
     */
    private String alarmStartLongitude;
    /**
     * 报警开始纬度
     */
    private String alarmStartLatitude;

    /**
     * 报警结束位置(经度,纬度)
     */
    private String alarmEndLocation;
    /**
     * 报警结束位置
     */
    private String alarmEndLongitude;
    /**
     * 报警结束纬度
     */
    private String alarmEndLatitude;

    /**
     * 报警开始位置(具体地址)
     */
    private String alarmStartAddress;
    private String alarmStartSpecificLocation;

    /**
     * 报警结束位置(具体地址)
     */
    private String alarmEndAddress;
    private String alarmEndSpecificLocation;

    /**
     * 围栏类型
     */
    private String fenceType;

    /**
     * 围栏名称
     */
    private String fenceName;

    /**
     * 处理人
     */
    private String personName;

    private String handleTimeStr;
    /**
     *
     * 处理时间 格式:yyyyMMddHHmmss
     */
    private String handleTime;

    /**
     * 处理方式
     */
    private String handleType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 报警查询提供协议类型，前端进行区分KKS-EV25不做处理
     * 协议类型
     */
    private Integer deviceType;

    /**
     * 方向
     */
    private String angle;

    /**
     * 位置表id
     */
    private String positionalId;

    /**
     * 处理人id
     */
    private String personId;

    /**
     * 高度
     */
    private String height;

    /**
     * 监控对象类型 0:车 1:人 2:物
     */
    private Integer monitorType;

    /**
     * 协议类型
     */
    private Integer protocolType;

    /**
     * 流水号
     */
    private String swiftNumber;

    /**
     * 推送方式
     */
    private Integer pushType;

    /**
     * 报警计算标准：0普通标准 1山西标准 2四川标准
     */
    private Integer calStandard;

    /**
     * 限速类型
     */
    private Integer speedType;

    /**
     * 最高速度
     */
    private Double maxSpeed;

    /**
     * 风险id
     */
    private String riskId;

    /**
     * 所属企业id-字符串
     */
    private String groupId;

    /**
     * 处理人所属企业名称
     */
    private String personGroupName;

    /**
     * 短信内容
     */
    private String sendOfMsg;

    /**
     * 短息内容
     */
    private String dealOfMsg;

    /**
     * 监控对象所属企业
     */
    private String groupName;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 驾驶员名称
     */
    private String driverName;

    /**
     * 从业资格证号
     */
    private String certificationId;

    /**
     * 主键(格式: monitorId_alarmStartTime_alarmType_alarmEndTime)
     */
    private String primaryKey;

    /**
     * 报警处理结果 0:处理中 1:已处理完毕 2:不作处理 3:将来处理
     */
    private Integer handleResult;
}
