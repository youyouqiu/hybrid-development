package com.zw.platform.basic.dto.imports;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/11/2 17:06
 */
@Data
public class DeviceImportDTO extends ImportErrorData {

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

    @ExcelField(title = "安装日期")
    private String installTime;

    @ExcelField(title = "采购日期")
    private String procurementTime;

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

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;


}
