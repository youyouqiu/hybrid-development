package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/9/8
 **/
@Data
public class DeviceImportForm extends ImportErrorData {

    @ExcelField(title = "终端号", required = true, repeatable = false)
    private String deviceNumber; // 终端编号

    /**
     * 所属企业名称
     */
    @ExcelField(title = "所属企业", required = true)
    private String groupNameImport;

    private String groupName;

    private String groupId;

    @ExcelField(title = "通讯类型", required = true)
    private String deviceType; // 通讯类型

    /**
     * 终端厂商
     */
    @ExcelField(title = "终端厂商", required = true)
    private String terminalManufacturer;

    /**
     * 终端型号id
     */
    private String terminalTypeId;

    /**
     * 终端型号
     */
    @ExcelField(title = "终端型号", required = true)
    private String terminalType;

    private Integer channelNumber; // 通道数

    private String isVideos; // 是否视频

    private Integer isVideo;

    @ExcelField(title = "功能类型", required = true)
    private String functionalType; // 功能类型

    @ExcelField(title = "终端名称")
    private String deviceName; // 终端名称

    /**
     * 注册信息-制造商ID
     */
    @ExcelField(title = "制造商ID")
    private String manufacturerId;

    /**
     * 注册信息-终端型号
     */
    @ExcelField(title = "终端型号（注册）")
    private String deviceModelNumber;

    @ExcelField(title = "MAC地址")
    private String macAddress;

    @ExcelField(title = "制造商")
    private String manuFacturer; // 制造商

    @ExcelField(title = "条码")
    private String barCode; // 条码

    @ExcelField(title = "启停状态")
    private String isStarts; // 启停状态

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date installTime; //安装时间

    @ExcelField(title = "安装日期")
    private String installTimeStr; // 安装时间Str

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date procurementTime;//采购时间

    @ExcelField(title = "采购日期")
    private String procurementTimeStr;//采购时间

    /**
     * 安装单位
     */
    @ExcelField(title = "安装单位")
    private String installCompany;

    /**
     * 联系人
     */
    @ExcelField(title = "联系人")
    private String contacts;
    /**
     * 联系方式
     */
    @ExcelField(title = "联系方式")
    private String telephone;

    /**
     * 是否符合要求,用于报表导入导出。
     * 1:是
     * 0:否
     */
    @ExcelField(title = "是否符合要求")
    private String complianceRequirementsStr;

    @ExcelField(title = "备注")
    private String remark;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
}
