package com.zw.platform.basic.dto;

import com.zw.platform.basic.domain.DeviceInfoDo;
import com.zw.platform.domain.basicinfo.form.DeviceForm;
import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.domain.infoconfig.form.ConfigTransportImportForm;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.ws.common.PublicVariable;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @Author: zjc
 * @Description:终端类
 * @Date: create in 2020/10/22 9:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceDTO extends BaseDTO {

    @NotEmpty(message = "【终端号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【终端号】格式错误，只能输入字母数字下划线！", regexp = "^[A-Za-z0-9_-]+$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @Size(max = 30, message = "【终编号】长度不超过30！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "终端号", required = true, repeatable = false)
    private String deviceNumber; // 终端编号

    /**
     * 所属企业名称
     */
    @ExcelField(title = "所属企业", required = true)
    private String groupNameImport;

    @NotEmpty(message = "【通讯类型】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【通讯类型】输入错误，只能输入1,2,3,5,6,7,8,9,10,11,12,13 其中0:：交通部2011版,1:交通部2013-F3版,2:GV320,3:TH,"
        + "5:北斗天地协议,6:康凯斯有线,7:康凯斯无线,8:博实结,9:艾赛欧超长待机,10:F3超长待机设备,11:交通部JT/T808-2019,"
        + "12:交通部JT/T808-2013(川标),13:交通部JT/T808-2013(冀标)", regexp = "^[0-9]\\d?|2$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
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

    @ExcelField(title = "功能类型", required = true)
    private String functionalType; // 功能类型

    @ExcelField(title = "终端名称")
    @Size(max = 50, message = "【终端名称】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
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
    @Size(max = 17, message = "【MAC地址】长度为17！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String macAddress;

    @Size(max = 100, message = "【制造商】长度不超过100！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "制造商")
    private String manuFacturer; // 制造商

    /**
     * 组织
     */
    private String groupName;

    @Size(max = 64, message = "【条码】长度不超过64！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "条码")
    private String barCode; // 条码

    @ExcelField(title = "启停状态")
    private String isStarts; // 启停状态

    /**
     * 车牌号
     */
    @ExcelField(title = "监控对象", type = 1)
    private String brand;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date installTime; //安装时间

    @ExcelField(title = "安装日期")
    private String installTimeStr; // 安装时间Str

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date procurementTime;//采购时间

    @ExcelField(title = "采购日期")
    private String procurementTimeStr;//采购时间

    @ExcelField(title = "创建日期", type = 1)
    private String createDataTimeStr; //录入时间str

    @ExcelField(title = "修改日期", type = 1)
    private String updateDataTimeStr;

    @Min(value = 1, message = "【通道数】输入错误，只能输入1,2,3,4;其中1:4,2:5,3:8,4:16！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @Max(value = 4, message = "【通道数】输入错误，只能输入1,2,3,4;其中1:4,2:5,3:8,4:16！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private Integer channelNumber; // 通道数

    private String isVideos; // 是否视频

    private Date createDataTime; //录入时间

    @Min(value = 0, message = "【启停状态】输入错误，只能输入0,1,其中0:停用,1:启用！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @Max(value = 1, message = "【启停状态】输入错误，只能输入0,1,其中0:停用,1:启用！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private Integer isStart; // 启停状态
    @Min(value = 0, message = "【是否视频】输入错误，只能输入0,1,其中0:否,1:是！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Max(value = 1, message = "【是否视频】输入错误，只能输入0,1,其中0:否,1:是！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer isVideo; // 是否视频

    /**
     * 组织id
     */
    @NotEmpty(message = "【所属企业】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String groupId;

    /**
     * 绑定id
     */
    private String bindId;

    /**
     * 鉴权码
     */
    private String authCode;

    /**
     * 安装单位
     */
    @ExcelField(title = "安装单位")
    @Size(max = 50, message = "【安装单位】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String installCompany;

    /**
     * 联系人
     */
    @ExcelField(title = "联系人")
    @Size(max = 20, message = "【联系人】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String contacts;
    /**
     * 联系方式
     */
    @ExcelField(title = "联系方式")
    @Size(max = 50, message = "【联系方式】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String telephone;

    @Min(value = 0, message = "【是否符合要求】输入错误，只能输入0,1,其中0:否,1:是！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @Max(value = 1, message = "【是否符合要求】输入错误，只能输入0,1,其中0:否,1:是！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private Integer complianceRequirements;
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

    @ApiParam(value = "是否可用")
    private Integer enabled = 1;

    /**
     * 企业id
     */
    private String orgId;

    /**
     * 企业名称
     */
    private String orgName;

    public static DeviceDTO getAddInstance(DeviceForm form) {
        DeviceDTO deviceDTO = new DeviceDTO();
        BeanUtils.copyProperties(form, deviceDTO);
        return deviceDTO;
    }

    public static DeviceDTO getUpdateInstance(DeviceForm form) {
        DeviceDTO deviceDTO = new DeviceDTO();
        BeanUtils.copyProperties(form, deviceDTO);
        return deviceDTO;
    }

    @Override
    public void initAdd(String username) {
        initData();
        super.initAdd(username);
    }

    private void initData() {
        final String installTimeStr = getInstallTimeStr();
        if (StringUtils.isNotEmpty(installTimeStr)) {
            setInstallTime(Converter.toDate(installTimeStr, "yyyy-MM-dd"));
        }
        final String procurementTimeStr = getProcurementTimeStr();
        if (StringUtils.isNotEmpty(procurementTimeStr)) {
            setProcurementTime(Converter.toDate(procurementTimeStr, "yyyy-MM-dd"));
        }
        //转换企业id
        if (StringUtils.isBlank(orgId)) {
            orgId = groupId;
        }
    }

    @Override
    public void initUpdate(String username) {
        initData();
        super.initUpdate(username);
    }

    public static DeviceDTO getInfo(DeviceInfoDo deviceInfoDo, String orgName) {
        DeviceDTO deviceDTO = new DeviceDTO();
        BeanUtils.copyProperties(deviceInfoDo, deviceDTO);
        deviceDTO.initInfo();
        deviceDTO.setGroupName(orgName);
        deviceDTO.setOrgName(orgName);
        return deviceDTO;
    }

    private void initInfo() {
        installTimeStr = Converter.toString(installTime, "yyyy-MM-dd");
        procurementTimeStr = Converter.toString(procurementTime, "yyyy-MM-dd");
        groupId = orgId;
    }

    public static DeviceDTO buildData(DeviceForm deviceForm) {
        DeviceDTO deviceDTO = new DeviceDTO();
        BeanUtils.copyProperties(deviceForm, deviceDTO);
        return deviceDTO;
    }

    public static DeviceForm initConfigImportForm(ConfigImportForm config, String userName) {

        Date createDate = new Date();
        DeviceForm df = new DeviceForm();
        df.setDeviceNumber(config.getDeviceNumber());
        df.setBrand(config.getCarLicense());
        df.setIsStart(1);
        df.setIsVideo(1);
        df.setDeviceType(PublicVariable.getDeviceTypeId(config.getDeviceType()));
        df.setFunctionalType(PublicVariable.getFunctionTypeId(config.getFunctionalType()));
        df.setFlag(1);
        df.setGroupId(config.getGroupId());
        df.setCreateDataUsername(userName);
        df.setCreateDataTime(createDate);
        df.setCreateDataTimeStr(DateFormatUtils.format(createDate, "yyyy-MM-dd"));
        df.setTerminalTypeId(config.getTerminalTypeId());
        df.setTerminalType(config.getTerminalType());
        df.setTerminalManufacturer(config.getTerminalManufacturer());
        return df;
    }

    public static DeviceForm initConfigImport(ConfigTransportImportForm config, String userName) {
        Date createDate = new Date();
        DeviceForm df = new DeviceForm();
        df.setDeviceNumber(config.getDeviceNumber());
        df.setBrand(config.getBrand());
        df.setIsStart(1);
        df.setIsVideo(1);
        df.setDeviceType(config.getDeviceType());
        df.setFunctionalType(config.getFunctionalType());
        df.setFlag(1);
        df.setGroupId(config.getGroupId());
        df.setCreateDataUsername(userName);
        df.setCreateDataTime(createDate);
        df.setCreateDataTimeStr(DateFormatUtils.format(createDate, "yyyy-MM-dd"));
        df.setDeviceName(config.getDeviceName());
        df.setManuFacturer(config.getManuFacturer());
        df.setTerminalTypeId("default");   //默认数据
        df.setTerminalManufacturer("[f]F3");   //默认数据
        df.setTerminalType("F3-default");
        config.setTerminalTypeId("default");
        config.setTerminalManufacturer("[f]F3");
        config.setTerminalType("F3-default");
        return df;
    }

}
