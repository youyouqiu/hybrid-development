package com.zw.adas.domain.report.deliveryLine;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Date;

/***
 @Author lijie
 @Date 2021/1/8 16:12
 @Description 线路下发记录
 @version 1.0
 **/
@Data
public class LineRecordDto {

    /**
     * id
     */
    private String id;

    /**
     * 组织id
     */
    private String orgId;

    /**
     * 组织名字
     */
    @ExcelField(title = "所属组织")
    private String orgName;

    /**
     * 车id
     */
    private String vehicleId;

    /**
     * 车牌
     */
    @ExcelField(title = "车牌")
    private String brand;

    /**
     * 车辆颜色
     */
    private Integer vehicleColor;

    /**
     * 车辆颜色
     */
    @ExcelField(title = "车辆颜色")
    private String vehicleColorStr;

    /**
     * 线路id
     */
    @ExcelField(title = "线路id")
    private String lineId;

    /**
     * 线路id
     */
    private String lineUuid;

    /**
     * 风险围栏id
     */
    private String fenceConfigId;

    /**
     * 收到上级平台的路线信息时间
     */
    private Date receiveTime;

    @ExcelField(title = "上级下发时间")
    private String receiveTimeStr;

    /**
     * 流水号
     */
    private Integer swiftNumber;

    /**
     * 逻辑删除标记
     */
    private Integer flag;

    /**
     * 下发状态：0:指令已生效  1:指令未生效  2:参数消息有误  3:参数不支持 4:参数下发中 5:终端离线，未下发 7终端处理中 8终端接收失败
     */
    private Integer dirStatus;

    /**
     * 下发状态：0:指令已生效  1:指令未生效  2:参数消息有误  3:参数不支持 4:参数下发中 5:终端离线，未下发 7终端处理中 8终端接收失败
     */
    @ExcelField(title = "下发至终端状态")
    private String dirStatusStr;

    /**
     * 下发时间
     */
    @ExcelField(title = "下发至终端时间")
    private String sendTime;

}
