package com.zw.platform.basic.dto;

import com.zw.platform.basic.domain.SimCardInfoDo;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.SimcardForm;
import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.domain.infoconfig.form.ConfigTransportImportForm;
import com.zw.platform.util.BSJFakeIPUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * sim卡Form
 * @author wangying
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SimCardDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @ExcelField(title = "ICCID")
    private String iccid;

    @ExcelField(title = "IMEI")
    private String imei;

    @ExcelField(title = "IMSI")
    private String imsi;

    /**
     * sim卡号
     */
    @ExcelField(title = "终端手机号")
    @NotEmpty(message = "【终端手机号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【终端手机号】输入错误，请输入合法的sim卡卡号！", regexp = "^(\\d{7,20})$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private String simcardNumber;

    /**
     * 所属企业名称
     */
    @ExcelField(title = "所属企业")
    private String groupNameImport;

    /**
     * 启停状态
     */
    @ExcelField(title = "启停状态")
    private String isStarts;

    /**
     * 运营商
     */
    @Size(max = 50, message = "【运营商】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "运营商")
    private String operator;

    /**
     * 发放地市（1120改动）
     */
    @ExcelField(title = "发放地市")
    @Size(max = 20, message = "【发放地市】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String placementCity;

    /**
     * 套餐流量
     */
    // @Pattern(message = "【预警流量】输入错误，请输入正整数！",regexp = "^\\s*$|^[1-9]\\d*$", groups = { ValidGroupAdd.class,
    // ValidGroupUpdate.class})
    @ExcelField(title = "套餐流量(M)")
    private String simFlow;

    @ExcelField(title = "当日流量(M)")
    private String dayRealValue;

    @ExcelField(title = "当月流量(M)")
    private String monthRealValue;

    @ExcelField(title = "流量最后更新时间")
    private String monthTrafficDeadline;

    /**
     * 预警流量
     */
    @ExcelField(title = "月预警流量(M)")
    private String alertsFlow;

    @ExcelField(title = "流量月结日")
    private String monthlyStatement;

    @ExcelField(title = "修正系数")
    private String correctionCoefficient;

    @ExcelField(title = "预警系数")
    private String forewarningCoefficient;

    @ExcelField(title = "小时流量阈值(M)")
    private String hourThresholdValue;

    @ExcelField(title = "日流量阈值(M)")
    private String dayThresholdValue;

    @ExcelField(title = "月流量阈值(M)")
    private String monthThresholdValue;

    /**
     * 开卡时间
     */
    @ExcelField(title = "激活日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date openCardTime;

    /**
     * 容量
     */
    // @ExcelField(title = "容量")
    private String capacity;

    /**
     * 网络类型
     */
    // @ExcelField(title = "网络类型")
    private String networkType;

    /**
     * 已用流量
     */
    // @Pattern(message = "【预警流量】输入错误，请输入正整数！",regexp = "^\\s*$|^[1-9]\\d*$", groups = { ValidGroupAdd.class,
    // ValidGroupUpdate.class})
    // @ExcelField(title = "已用流量")
    private String useFlow;

    /**
     * 到期时间
     */
    @ExcelField(title = "到期时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    /**
     * 组织id
     */
    @NotEmpty(message = "【所属企业】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String groupId;

    /**
     * 组织
     */
    //    @ExcelField(title = "所属企业")
    private String groupName;

    private String deviceId;

    @ExcelField(title = "终端号")
    private String deviceNumber;

    @ExcelField(title = "监控对象")
    private String brand;

    @ExcelField(title = "创建时间")
    private String createDateFormat;

    @ExcelField(title = "修改时间")
    private String updateDateFormat;

    @ExcelField(title = "真实SIM卡号")
    private String realId;

    @ExcelField(title = "备注信息")
    private String remark;

    @Min(value = 0, message = "【启停状态】输入错误，只能输入0,1,其中0:停用,1:启用！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @Max(value = 1, message = "【启停状态】输入错误，只能输入0,1,其中0:停用,1:启用！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private Integer isStart;

    private String sid;

    private String vid;

    private String paramId;

    /**
     * 伪IP
     */
    private String fakeIP;

    /**
     * 绑定id
     */
    private String bindId;

    private String configId;

    private String vehicleId;

    private String monitorType;

    /**
     * 下发状态
     */
    private Integer status = 0;

    /**
     * 企业id
     */
    private String orgId;

    private String orgName;

    private String authCode;

    public static SimCardDTO of(String simcardNumber, String orgId) {
        SimCardDTO form = new SimCardDTO();
        form.setSimcardNumber(simcardNumber);
        form.setIsStart(1);
        form.setFlag(1);
        form.setOperator("中国移动");
        form.setGroupId(orgId);
        form.setMonthlyStatement("01");
        form.setCorrectionCoefficient("100");
        form.setForewarningCoefficient("90");
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setCreateDataTime(new Date());
        return form;
    }

    public static SimCardDTO initConfigImport(ConfigImportForm config, String orgId) {
        SimCardDTO form = SimCardDTO.of(config.getSimcardNumber(), orgId);
        form.setBrand(config.getCarLicense());
        form.setDeviceNumber(config.getDeviceNumber());
        form.setVid(config.getBrandID());
        form.setBindId(config.getId());
        form.setFakeIP(BSJFakeIPUtil.integerMobileIPAddress(config.getSimcardNumber()));
        form.setRealId(config.getRealId());
        return form;
    }

    public static SimCardDTO initConfigImport(ConfigTransportImportForm config, String orgId) {
        SimCardDTO form = SimCardDTO.of(config.getSimcardNumber(), orgId);
        form.setBrand(config.getBrand());
        form.setDeviceNumber(config.getDeviceNumber());
        form.setVid(config.getBrandID());
        form.setBindId(config.getId());
        form.setFakeIP(BSJFakeIPUtil.integerMobileIPAddress(config.getSimcardNumber()));
        return form;
    }

    @Override
    public void initAdd(String userName) {
        fakeIP = BSJFakeIPUtil.integerMobileIPAddress(simcardNumber);
        if (StringUtils.isBlank(orgId)) {
            orgId = groupId;
        }
        super.initAdd(userName);
    }

    @Override
    public void initUpdate(String userName) {
        orgId = groupId;
        super.initUpdate(userName);
    }

    public static SimCardDTO getInfo(SimCardInfoDo simCardInfoDo, String orgName) {
        SimCardDTO simCardDTO = new SimCardDTO();
        BeanUtils.copyProperties(simCardInfoDo, simCardDTO);
        simCardDTO.simcardNumber = simCardInfoDo.getSimCardNumber();
        simCardDTO.groupId = simCardInfoDo.getOrgId();
        simCardDTO.setGroupName(orgName);
        simCardDTO.setOrgName(orgName);
        return simCardDTO;
    }

    public static SimCardDTO getAddInstance(SimcardForm simcardForm) {

        SimCardDTO simCardDTO = new SimCardDTO();
        BeanUtils.copyProperties(simcardForm, simCardDTO);
        if (StringUtil.isNullOrBlank(simCardDTO.getMonthlyStatement())) {
            simCardDTO.setMonthlyStatement("01");
        }
        return simCardDTO;
    }

    public static SimCardDTO getUpdateInstance(SimcardForm simcardForm) {
        SimCardDTO simCardDTO = new SimCardDTO();
        BeanUtils.copyProperties(simcardForm, simCardDTO);
        return simCardDTO;
    }

}
