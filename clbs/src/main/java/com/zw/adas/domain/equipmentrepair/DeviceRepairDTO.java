package com.zw.adas.domain.equipmentrepair;

import com.zw.adas.constant.FaultTypeEnum;
import com.zw.adas.domain.equipmentrepair.paas.DeviceRequestRepairDTO;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 终端维修上报信息--web端使用
 * @author zhangjuan
 */
@Data
@NoArgsConstructor
public class DeviceRepairDTO {
    /**
     * 企业ID
     */
    private String orgId;
    /**
     * 企业名称
     */
    private String orgName;

    /**
     * 监控对象ID
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 设备厂商
     */
    private String terminalVendor;

    /**
     * 终端型号
     */
    private String terminalType;

    /**
     * 终端编号
     */
    private String deviceNumber;

    /**
     * 终端Id
     */
    private String deviceId;

    /**
     * 报修时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    private String reportRepairTime;

    /**
     * 车牌颜色
     * 1:蓝色 2:黄色 3:黑色 4:白色 5:绿色 9:其他 93:黄绿色 94:渐变绿色
     */
    private Integer plateColor;

    /**
     * 故障类型
     * 0:主存储器异常
     * 1:备用存储器异常
     * 2:卫星信号异常
     * 3:通信信息号异常
     * 4:备用电池欠压
     * 5:备用电池失效
     * 6:IC卡从业资格证模块故障
     */
    private Integer faultType;

    /**
     * 故障类型 名称
     */
    private String faultTypeName;

    /**
     * 故障处理状态  0:未确认 1:已确认 2:已完成 3:误报
     */
    private Integer handleStatus;

    /**
     * 修理日期(格式:yyyy-MM-dd)
     */
    private String repairDate;

    /**
     * 主键(企业id_报修时间_故障类型_车辆id)
     */
    private String primaryKey;

    /**
     * 备注
     */
    private String remark;

    public DeviceRepairDTO(DeviceRequestRepairDTO repairDTO) throws Exception {
        this.orgId = repairDTO.getGroupId();
        this.orgName = repairDTO.getGroupName();
        this.monitorId = repairDTO.getMonitorId();
        this.monitorName = repairDTO.getMonitorName();
        this.terminalVendor = repairDTO.getTerminalVendor();
        this.terminalType = repairDTO.getTerminalType();
        this.deviceId = repairDTO.getDeviceId();
        this.deviceNumber = repairDTO.getDeviceNumber();
        String beforeFormat = DateFormatKey.YYYYMMDDHHMMSS;
        String afterFormat = DateFormatKey.YYYY_MM_DD_HH_MM_SS;
        this.reportRepairTime = DateUtil.formatDate(repairDTO.getTime(), beforeFormat, afterFormat);
        this.faultType = repairDTO.getType();
        this.faultTypeName = FaultTypeEnum.getNameByCode(repairDTO.getType());
        this.handleStatus = repairDTO.getHandleStatus();
        if (StringUtils.isNotBlank(repairDTO.getRepairDate())) {
            this.repairDate =
                DateUtil.formatDate(repairDTO.getRepairDate(), DateFormatKey.YYYYMMDD, DateFormatKey.YYYY_MM_DD);
        }
        this.primaryKey = repairDTO.getPrimaryKey();
        this.remark = repairDTO.getRemark();
        this.plateColor = repairDTO.getPlateColor();
    }

}
