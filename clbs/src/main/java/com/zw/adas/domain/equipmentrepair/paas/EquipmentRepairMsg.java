package com.zw.adas.domain.equipmentrepair.paas;

import com.zw.adas.constant.FaultTypeEnum;
import com.zw.adas.domain.equipmentrepair.DeviceRepairDTO;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 设备维修上报实体
 *
 * @author zhangjuan
 */
@Data
@NoArgsConstructor
public class EquipmentRepairMsg {
    /**
     * 设备厂商名
     */
    private String producer;

    /**
     * 型号
     */
    private String terminalModel;

    /**
     * 设备 ID 号
     */
    private String terminalId;

    /**
     * 设备报修日期，用 UTC 时间表示 --到秒的时间戳
     */
    private Long repartTime;

    /**
     * 故障类型，定义如下：
     * 0x00：主存储器异常；
     * 0x01：备用存储器异常；
     * 0x02：卫星信号异常；
     * 0x03：通信信号异常；
     * 0x04：备用电池欠压；
     * 0x05：备用电池失效；
     * 0x06：IC 卡从业资格证模块故障
     */
    private Integer faultType;

    /**
     * 运输企业
     */
    private String transportationEnterprises;

    /**
     * 设备完成维修日期，用 UTC 时间表示 --到秒的时间戳
     */
    private Long repartFinishTime;

    public EquipmentRepairMsg(DeviceRepairDTO repairDTO) {
        this.producer = repairDTO.getTerminalVendor();
        this.terminalModel = repairDTO.getTerminalType();
        this.terminalId = repairDTO.getDeviceNumber();
        this.repartTime =
                DateUtil.localToUTCTime(repairDTO.getReportRepairTime(), DateFormatKey.YYYY_MM_DD_HH_MM_SS) / 1000;
        this.faultType = FaultTypeEnum.getHexCodeByCode(repairDTO.getFaultType());
        this.transportationEnterprises = repairDTO.getOrgName();
        if (StringUtils.isNotBlank(repairDTO.getRepairDate())) {
            this.repartFinishTime = DateUtil.localToUTCTime(repairDTO.getRepairDate(), DateFormatKey.YYYY_MM_DD) / 1000;
        }
    }


}
