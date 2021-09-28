package com.zw.platform.basic.dto;

import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.DeviceListDO;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * @Author: zjc
 * @Description:终端列表信息
 * @Date: create in 2020/10/29 11:11
 */
@Data
public class DeviceListDTO {

    /**
     * 终端厂商
     */
    private String terminalManufacturer;

    /**
     * 安装日期（yyyy-MM-dd）
     */
    private String installTimeStr;

    /**
     * 制造商
     */
    private String manuFacturer;

    /**
     * 终端型号（注册）
     */
    private String deviceModelNumber;

    /**
     * 备注
     */
    private String remark;

    /**
     * 终端号
     */
    private String deviceNumber;

    /**
     * 终端名称
     */
    private String deviceName;

    /**
     * 1:简易型车机；2：行车记录仪； 3：对讲设备；4：手咪设备(需要转换一下)
     */
    private String functionalType;

    /**
     * 是否符合要求（1:是 0:否）
     */
    private Integer complianceRequirements;

    /**
     * 采购日期（yyyy-MM-dd）
     */
    private String procurementTimeStr;

    /**
     * 终端id
     */
    private String id;

    /**
     * 通讯类型
     */
    private String deviceType;

    /**
     * 安装单位
     */
    private String installCompany;

    /**
     * 修改日期
     */
    private String updateDataTimeStr;

    /**
     * 注册信息-制造商ID
     */
    private String manufacturerId;

    /**
     * 联系方式
     */
    private String telephone;

    /**
     * 启停状态0:停用 1:启用
     */
    private Integer isStart;

    /**
     * 条码
     */
    private String barCode;

    /**
     * 终端型号
     */
    private String terminalType;

    /**
     * 企业名称
     */
    private String groupName;

    /**
     * mac地址
     */
    private String macAddress;

    /**
     * 创建日期
     */
    private String createDataTimeStr;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 监控对象
     */
    private String brand;

    public static DeviceListDTO buildList(DeviceListDO listDO, Map<String, BaseKvDo<String, String>> monitorIdNameMap,
        Map<String, String> orgMap) {
        DeviceListDTO deviceListDTO = new DeviceListDTO();
        BeanUtils.copyProperties(listDO, deviceListDTO);
        BaseKvDo<String, String> kvDo = monitorIdNameMap.get(listDO.getMonitorId());
        if (kvDo != null) {
            deviceListDTO.brand = kvDo.getFirstVal();
        }
        deviceListDTO.installTimeStr = DateUtil.formatDate(listDO.getInstallTime(), DateUtil.DATE_Y_M_D_FORMAT);
        deviceListDTO.procurementTimeStr = DateUtil.formatDate(listDO.getProcurementTime(), DateUtil.DATE_Y_M_D_FORMAT);
        deviceListDTO.groupName = orgMap.get(listDO.getOrgId());
        deviceListDTO.updateDataTimeStr = DateUtil.formatDate(listDO.getUpdateDataTime(), DateUtil.DATE_Y_M_D_FORMAT);
        deviceListDTO.createDataTimeStr = DateUtil.formatDate(listDO.getCreateDataTime(), DateUtil.DATE_Y_M_D_FORMAT);
        return deviceListDTO;
    }

}
