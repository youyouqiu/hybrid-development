package com.zw.platform.domain.infoconfig.form;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.ConfigDetailDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.bval.constraints.NotEmpty;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 信息配置直接选择已经存在的信息的form <p>Title: Config1Form.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年8月1日上午10:29:49
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Config1Form extends BaseFormBean implements Serializable, ConverterDateUtil {

    private static final long serialVersionUID = 1L;

    /**
     * 信息列表导出
     */
    @ExcelField(title = "监控对象")
    private String carLicense; // 车牌号

    @ExcelField(title = "监控对象类型")
    private String monitorType; // 监控对象类型

    @ExcelField(title = "车牌颜色")
    private String plateColorStr; // 车牌颜色

    @ExcelField(title = "所属企业")
    private String groupName; // 所属企业

    @ExcelField(title = "分组")
    private String assignmentName; // 组名

    @ExcelField(title = "终端号")
    private String deviceNumber; // 终端编号

    // 1 ：交通部JTB808；2：移为GV320；3：天禾
    @Pattern(message = "【通讯类型】填值错误！", regexp = "^\\s*$|^[1-3]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "通讯类型")
    private String deviceType; // 通讯类型

    @ExcelField(title = "终端厂商")
    private String terminalManufacturer; //终端厂商

    @ExcelField(title = "终端型号")
    private String terminalType; // 终端型号

    @ExcelField(title = "功能类型")
    private String functionalType = "1"; // 功能类型

    @ExcelField(title = "终端手机号")
    private String simcardNumber; // SIM卡号

    @ExcelField(title = "真实SIM卡号")
    private String realId;// 真实sim卡号

    private String peripheralsId; // 外设

    @ExcelField(title = "计费日期")
    private String billingDateStr;

    @ExcelField(title = "到期日期")
    private String durDateStr;

    @ExcelField(title = "从业人员")
    private String professionals; // 从业人员

    /**
     * 车牌号
     */
    @NotEmpty(message = "【车牌号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【车牌号】格式错误，不是真实的车牌号！", regexp = "^[\u4eac\u6d25\u5180\u664b\u8499\u8fbd\u5409\u9ed1"
        + "\u6caa\u82cf\u6d59\u7696\u95fd\u8d63\u9c81\u8c6b" + "\u9102\u6e58\u7ca4\u6842\u743c\u5ddd\u8d35\u4e91\u6e1d"
        + "\u85cf\u9655\u7518\u9752\u5b81\u65b0]" + "{1}[A-Z]{1}[A-Z_0-9]{5}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private String brands;

    private String car_groupId = ""; // 车辆所属企业id

    private String car_groupName = ""; // 车辆所属企业名称

    private String peo_groupId = ""; // 人员所属企业id

    private String peo_groupName = ""; // 人员所属企业名称

    private String thing_groupId = ""; // 物品所属企业id

    private String thing_groupName = ""; // 物品所属企业名称

    /**
     * 终端编号
     */
    @NotEmpty(message = "【终端号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【终端号】包含不合法字符！", regexp = "^[A-Za-z0-9_-]+$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private String devices;

    private String device_groupId = ""; // 终端所属企业id

    private String deviceGroupName = ""; // 终端所属企业名称

    /**
     * SIM卡号
     */
    @NotEmpty(message = "【终端手机号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【终端手机号】输入错误，请输入合法的sim卡卡号！", regexp = "^[a-zA-Z0-9]{7,20}$",
            groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String sims;

    private String sim_groupId = ""; // SIM卡所属企业

    /**
     * 车辆id
     */
    private String brandID;

    private String vehicleType = ""; // 车辆类型

    private String vehicleTypeId = ""; //车辆类型id

    private String vehicleOwnerPhone = ""; // 车主电话

    /**
     * 分组id
     */
    private String citySel;

    private String citySelID;

    private String groupid; // 分组ID

    /**
     * 终端id
     */
    private String deviceID;

    private String manufacturerId; // 注册信息-制造商ID

    private String deviceModelNumber; // 注册信息-终端型号

    private String terminalTypeId; // 终端厂商、终端型号对应id

    /**
     * Sim卡id
     */
    private String simID;

    private String deviceName;

    private String manuFacturer;

    private String operator;

    private String simFlow;

    private String useFlow;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date openCardTime; // 激活日期

    private String iccid_sim = "";

    private String simParentGroupid = ""; // sim卡所属企业id

    private String simParentGroupname = ""; // sim卡所属企业名称

    private String isStart_sim = ""; // sim卡是否启用

    /**
     * 计费日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date billingDate; // 计费日期

    /**
     * 到期日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dueDate; // 到期日期

    /**
     * 服务周期id
     */
    private String serviceLifecycleId = "";

    private String configId; // 信息配置id

    private String professionalsID; // 从业人员id

    private String peopleName; // 人名

    private String thingName; // 物名

    private List<GroupForConfigForm> groupForConfigList; // config对应的分组

    private List<ProfessionalForConfigFrom> professionalFroConfigList; // config对应的从业人员

    private Integer vehicleIsStart;// 车辆启停状态

    private Integer deviceIsStart;// 终端启停状态

    private Integer scanType = 0;// 扫码录入判断(0:不是,1:是)

    private Integer plateColor = 2;// 车牌颜色，默认黄色

    private Integer isVideo = 1;// 是否视频，默认是

    /**
     * 车辆新增数据
     */

    private String aliases;// 别名

    private String vehicleOwner;// 车主

    private String fuelType;// 燃油类型

    private String areaAttribute;// 区域属性

    private String chassisNumber;// 机架号

    private String engineNumber;// 发动机号

    private String provinceId; // 省市id

    private String cityId; // 市域id

    private String province; // 省、直辖市

    private String city; // 市、区

    private String county; // 县

    /**
     * 人员新增数据
     */
    private String gender;// 性别

    private String name;// 姓名

    private String identity;// 身份证号

    private String phone;// 电话号码

    private String email;// 邮箱

    /**
     * 物品新增数据
     */
    private String thingNumber;// 物品编号

    private String thingCategory;// 物品类别

    private String thingCategoryName;// 物品类别名称

    private String thingType;// 物品类型

    private String thingTypeName;// 物品类型名称

    private String thingModel;// 物品型号

    private String thingManufacturer;// 制造商

    /**
     * 终端新增数据
     */
    private Integer channelNumber; // 通道数

    private String isVideos; // 是否视频

    private String barCode; // 条码

    private Integer isStart; // 启停状态

    private String installTimeStr; // 安装时间str

    /**
     * sim卡新增数据
     */
    private String iccid;

    private String imei;

    private String imsi;

    private String dayRealValue;// 当日流量

    private String monthRealValue;// 当月流量

    private String monthTrafficDeadline;// 流量最后更新时间

    private String monthlyStatement;// 流量月结日

    private String correctionCoefficient;// 修正系数

    private String forewarningCoefficient;// 预警系数

    private String hourThresholdValue;// 小时流量阈值

    private String dayThresholdValue;// 日流量阈值

    private String monthThresholdValue;// 月流量阈值

    private String alertsFlow;// 月预警流量

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;// 到期时间

    private String uniqueNumber; // 极速录入唯一标识

    //车辆的运营类别
    private String vehiclePurpose;

    private String vehiclePassword;

    /**
     * 转换成BindDTO 用于快速录入、扫码录入、急速录入、流程录入
     * @return 信息绑定
     */
    public ConfigDTO convertAddConfig() {
        ConfigDTO config = new ConfigDTO();
        config.setId(this.getBrandID());
        config.setName(this.brands);
        config.setAlias(this.getName());
        config.setMonitorType(this.monitorType);
        config.setPlateColor(this.plateColor);
        config.setDeviceNumber(this.devices);
        config.setDeviceId(this.deviceID);
        config.setSimCardId(this.simID);
        config.setSimCardNumber(this.sims);
        config.setGroupId(this.citySelID.replace(";", ","));
        if (StringUtils.isNotBlank(this.groupid)) {
            config.setGroupName(this.groupid.replace(";", ","));
        }
        config.setDeviceType(this.deviceType);

        //急速录入
        config.setUniqueNumber(this.uniqueNumber);
        config.setProvinceId(this.provinceId);
        config.setCityId(this.cityId);
        config.setManufacturerId(this.manufacturerId);
        config.setDeviceModelNumber(this.deviceModelNumber);

        //流程录入 人车物时car_groupId和car_groupName都有值
        config.setOrgId(this.car_groupId);
        config.setOrgName(this.car_groupName);
        //流程录入 车辆特有字段
        config.setVehicleTypeId(this.vehicleType);
        config.setProvince(this.province);
        config.setCity(this.city);
        config.setCounty(this.county);
        config.setVehiclePurpose(this.vehiclePurpose);
        if (StringUtils.isNotBlank(this.citySel)) {
            config.setGroupName(this.citySel.replace(";", ","));
        }
        //流程录入 人
        config.setGender(this.gender);
        config.setIdentity(this.identity);
        config.setPhone(this.phone);
        config.setEmail(this.email);
        //流程录入 物
        config.setThingType(this.thingType);
        config.setThingCategory(this.thingCategory);
        config.setThingModel(this.thingModel);
        config.setThingManufacturer(this.thingManufacturer);
        //流程录入 终端
        config.setDeviceOrgId(this.device_groupId);
        config.setTerminalTypeId(this.terminalTypeId);
        config.setFunctionalType(this.functionalType);
        config.setDeviceIsStart(this.isStart);
        config.setInstallTimeStr(this.installTimeStr);
        //流程录入 SIM卡
        config.setSimCardOrgId(this.sim_groupId);
        if (StringUtils.isNotBlank(this.isStart_sim)) {
            config.setSimIsStart(Integer.parseInt(this.isStart_sim));
        }
        config.setIccid(this.iccid);
        config.setImsi(this.imsi);
        config.setRealSimCardNumber(this.realId);
        config.setOperator(this.operator);
        config.setSimFlow(this.simFlow);
        config.setSimEndTime(this.endTime);
        if (Objects.nonNull(this.billingDate)) {
            config.setBillingDate(DateUtil.getDayStr(this.billingDate));
        }
        if (Objects.nonNull(this.dueDate)) {
            config.setExpireDate(DateUtil.getDayStr(this.dueDate));
        }
        if (StringUtils.isNotBlank(this.professionalsID)) {
            config.setProfessionalIds(this.professionalsID.replace(";", ","));
        }
        if (StringUtils.isNotBlank(this.professionals)) {
            config.setProfessionalNames(this.professionals.replace(";", ","));
        }
        return config;
    }

    public Config1Form(ConfigDetailDTO configDetailDTO) {
        BindDTO bindDTO = configDetailDTO.getMonitor();
        this.monitorType = bindDTO.getMonitorType();
        this.billingDateStr = bindDTO.getBillingDate();
        this.durDateStr = bindDTO.getExpireDate();
        this.brandID = bindDTO.getId();
        this.serviceLifecycleId = bindDTO.getServiceLifecycleId();
        this.configId = bindDTO.getConfigId();
        if (StringUtils.isNotBlank(bindDTO.getProfessionalNames())) {
            this.professionals = bindDTO.getProfessionalNames().replace(",", "#");
            this.professionalsID = bindDTO.getProfessionalIds().replace(",", "#");
        }

        this.thingName = bindDTO.getAlias();
        this.brands = bindDTO.getName();
        this.car_groupId = bindDTO.getOrgId();
        this.car_groupName = bindDTO.getOrgName();
        if (StringUtils.isNotBlank(bindDTO.getGroupName())) {
            this.groupName = bindDTO.getGroupName().replace(",", "#");
            this.groupid = bindDTO.getGroupId().replace(",", "#");
        }
        this.vehiclePassword = "000000";
        if (bindDTO instanceof VehicleDTO) {
            VehicleDTO vehicle = (VehicleDTO) bindDTO;
            this.vehicleTypeId = vehicle.getVehicleType();
            this.vehicleType = vehicle.getVehicleTypeName();
            this.vehiclePassword = vehicle.getVehiclePassword();
        }
        if (bindDTO instanceof PeopleDTO) {
            PeopleDTO people = (PeopleDTO) bindDTO;
            this.peo_groupId = people.getOrgId();
            this.peo_groupName = people.getOrgName();
        }

        if (bindDTO instanceof ThingDTO) {
            ThingDTO thing = (ThingDTO) bindDTO;
            this.thing_groupId = thing.getOrgId();
            this.thing_groupName = thing.getOrgName();
            this.thingNumber = thing.getName();
            this.thingName = thing.getAlias();
        }

        DeviceDTO deviceDTO = configDetailDTO.getDevice();
        if (Objects.nonNull(deviceDTO)) {
            this.devices = deviceDTO.getDeviceNumber();
            this.device_groupId = deviceDTO.getOrgId();
            this.deviceGroupName = deviceDTO.getOrgName();
            this.terminalTypeId = deviceDTO.getTerminalTypeId();
            this.deviceType = deviceDTO.getDeviceType();
            this.terminalManufacturer = deviceDTO.getTerminalManufacturer();
            this.terminalType = deviceDTO.getTerminalType();
            this.functionalType = deviceDTO.getFunctionalType();
            this.deviceID = deviceDTO.getId();
        }

        this.groupForConfigList = new ArrayList<>();
        List<GroupDTO> groups = configDetailDTO.getGroupList();
        for (GroupDTO groupDTO : groups) {
            GroupForConfigForm form = new GroupForConfigForm();
            form.setConfigid(bindDTO.getConfigId());
            form.setGroupid(groupDTO.getId());
            form.setGroupName(groupDTO.getName());
            this.groupForConfigList.add(form);
        }
        this.groupid = bindDTO.getGroupId().replace(",", "#");
        SimCardDTO simCardDTO = configDetailDTO.getSimCard();
        if (Objects.nonNull(simCardDTO)) {
            this.simID = simCardDTO.getId();
            this.sims = simCardDTO.getSimcardNumber();
            this.simParentGroupid = simCardDTO.getOrgId();
            this.simParentGroupname = simCardDTO.getOrgName();
            this.iccid = simCardDTO.getIccid();
            this.operator = simCardDTO.getOperator();
            this.realId = simCardDTO.getRealId();
            this.iccid_sim = simCardDTO.getIccid();
        }

        List<ProfessionalDTO> professionalList = configDetailDTO.getProfessionalList();
        if (CollectionUtils.isEmpty(professionalList)) {
            return;
        }

        this.professionalFroConfigList = new ArrayList<>();
        for (ProfessionalDTO professionalDTO : professionalList) {
            ProfessionalForConfigFrom configFrom = new ProfessionalForConfigFrom();
            configFrom.setConfigid(bindDTO.getConfigId());
            configFrom.setProfessionalsid(professionalDTO.getId());
            configFrom.setProfessionalsName(professionalDTO.getName());
            professionalFroConfigList.add(configFrom);
        }
    }

}
