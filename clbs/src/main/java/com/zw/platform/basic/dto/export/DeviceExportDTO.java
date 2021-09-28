package com.zw.platform.basic.dto.export;

import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.DeviceListDO;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/11/2 17:06
 */
@Data
public class DeviceExportDTO {

    @ExcelField(title = "终端号", required = true, repeatable = false)
    private String deviceNumber;
    @ExcelField(title = "所属企业", required = true)
    private String orgName;

    @ExcelField(title = "通讯类型", required = true)
    private String deviceType;

    @ExcelField(title = "终端厂商", required = true)
    private String terminalManufacturer;

    @ExcelField(title = "终端型号", required = true)
    private String terminalType;

    @ExcelField(title = "功能类型", required = true)
    private String functionalType;

    @ExcelField(title = "终端名称")
    private String deviceName;

    @ExcelField(title = "制造商ID")
    private String manufacturerId;

    @ExcelField(title = "终端型号（注册）")
    private String deviceModelNumber;

    @ExcelField(title = "MAC地址")
    private String macAddress;

    @ExcelField(title = "制造商")
    private String manuFacturer;

    @ExcelField(title = "条码")
    private String barCode;

    @ExcelField(title = "启停状态")
    private String isStarts;

    @ExcelField(title = "监控对象")
    private String brand;

    @ExcelField(title = "安装日期")
    private String installTime;

    @ExcelField(title = "采购日期")
    private String procurementTime;

    @ExcelField(title = "创建日期")
    private String createDataTime;
    @ExcelField(title = "修改日期")
    private String updateDataTime;
    @ExcelField(title = "安装单位")
    private String installCompany;

    @ExcelField(title = "联系人")
    private String contacts;

    @ExcelField(title = "联系方式")
    private String telephone;

    @ExcelField(title = "是否符合要求")
    private String complianceRequirements;

    @ExcelField(title = "备注")
    private String remark;

    private static final Map<Integer, String> crmMap = ImmutableMap.of(1, "是", 0, "否");


    public static DeviceExportDTO build(DeviceListDO listDO, Map<String, String> orgMap,
        Map<String, BaseKvDo<String, String>> monitorIdNameMap) {

        DeviceExportDTO deviceExport = new DeviceExportDTO();
        BeanUtils.copyProperties(listDO, deviceExport);
        deviceExport.orgName = orgMap.get(listDO.getOrgId());
        deviceExport.deviceType = ConstantUtil.getDeviceProtocolType(listDO.getDeviceType());
        deviceExport.functionalType = ConstantUtil.getDeviceFunctionType(listDO.getFunctionalType());

        deviceExport.isStarts = Objects.equals(listDO.getIsStart(), 1) ? "启用" : "停用";
        deviceExport.complianceRequirements = StrUtil.getOrBlank(crmMap.get(listDO.getComplianceRequirements()));
        if (listDO.getInstallTime() != null) {
            deviceExport.installTime = Converter.toString(listDO.getInstallTime(), "yyyy-MM-dd");
        }
        if (listDO.getProcurementTime() != null) {
            deviceExport.procurementTime = Converter.toString(listDO.getProcurementTime(), "yyyy-MM-dd");
        }
        if (listDO.getCreateDataTime() != null) {
            deviceExport.createDataTime = (Converter.toString(listDO.getCreateDataTime(), "yyyy-MM-dd"));
        }
        if (listDO.getUpdateDataTime() != null) {
            deviceExport.updateDataTime = Converter.toString(listDO.getCreateDataTime(), "yyyy-MM-dd");
        }
        BaseKvDo<String, String> kvDo = monitorIdNameMap.get(listDO.getMonitorId());
        if (kvDo != null) {
            deviceExport.brand = kvDo.getFirstVal();
        }
        return deviceExport;
    }

}
