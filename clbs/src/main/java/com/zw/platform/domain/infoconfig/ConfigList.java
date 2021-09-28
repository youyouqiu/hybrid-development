package com.zw.platform.domain.infoconfig;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.domain.infoconfig.form.ConfigTransportImportForm;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * 信息列表
 * <p>Title: ConfigList.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年8月3日上午11:20:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ConfigList extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String configId = ""; // 信息配置id
    private String carLicense = ""; // 车牌号
    private String color;        //车牌颜色
    private String peopleName = ""; // 人名
    private String vehicleId = "";    //车辆ID
    private String thingName = ""; // 物名
    private String groupId = ""; // 所属企业id
    private String groupName = ""; // 所属企业名称
    private String groupPhone = "";//企业电话
    private String assignmentName = ""; // 分组名称
    private String assignmentId = ""; // 分组id
    private String deviceNumber = ""; // 终端编号
    private String deviceType = ""; // 通讯类型
    private String functionalType = ""; // 功能类型
    private String simcardNumber = ""; // SIM卡号
    private String billingDate = ""; // 计费日期
    private String expireDate = ""; // 到期日期
    private String createDateTime = "";//添加时间
    private String updateDateTime = "";//修改时间
    private String professionalNames = ""; // 从业人员姓名
    private String professionalIds = ""; // 从业人员id
    private String peripheralsId = ""; // 外设id
    private Integer plateColor;//车辆颜色
    private String vehicleType;//车辆类型
    private String vehType;//车辆类型名称
    private String monitorType; // 监控对象类型
    private String deviceId = "";    //设备ID
    private String simcardId = "";    //sim卡ID
    private String realId;// 真实sim卡号
    private Integer isVideo;    //是否视频
    private String serviceLifecycleId = ""; //服务周期id
    private String type;// 物品类型
    private String typeName;// 物品类型名称
    private String category;// 物品类别
    private String categoryName;// 物品类别名称
    private String terminalManufacturer; //终端厂商
    private String terminalType; // 终端型号
    private String terminalTypeId;
    //车辆的运营类别
    private String vehiclePurpose;
    private String province; // 监控对象所属省份
    private String provinceId; // 监控对象所属省份id
    private String city; // 监控对象所属市
    private String cityId; // 监控对象所属市id
    private String county; // 监控对象所属县
    private String transType; // 809车辆静态数据 运输行业编码
    private String vehicleTypeCode; // 809车辆静态数据 车辆类型编码
    private String owersName; // 业户名称
    private String owersTel; // 业户电话
    //设置车辆营运状态，默认值为营运
    private Integer operatingState = 0;
    //车辆状态，4.3.7加入信息配置缓存，flink计算使用
    private Integer isStart = 1;

    /**
     * 安装时间
     */
    private Long installTime;
    /**
     * 安装单位
     */
    private String installCompany;
    /**
     * 联系电话
     */
    private String telephone;
    /**
     * 联系人
     */
    private String contacts;
    /**
     * 是否符合要求，0：否，1：是
     */
    private Integer complianceRequirements;

    /**
     * 物品的物品类型
     */
    private String thingType;

    private Long userId;

    /**
     * 车辆密码   字母、数字   最长6位
     */
    private String vehiclePassword;

    public static ConfigList initConfigImport(ConfigImportForm form) {
        ConfigList config = new ConfigList();
        config.setConfigId(form.getId());
        config.setCarLicense(form.getCarLicense());
        config.setPeopleName("");
        config.setVehicleId(form.getBrandID());
        config.setThingName("");
        config.setGroupId(form.getGroupId());
        config.setGroupName(form.getCompanyName());
        config.setAssignmentName(form.getAssignNames());
        config.setAssignmentId(form.getAssignIds());
        config.setDeviceNumber(form.getDeviceNumber());
        config.setDeviceType(form.getDeviceType());
        config.setFunctionalType(form.getFunctionalType());
        config.setSimcardNumber(form.getSimcardNumber());
        config.setBillingDate(form.getBillingDateStr());
        config.setExpireDate(form.getDurDateStr());
        config.setCreateDateTime(DateUtil.getDateToString(form.getCreateDataTime(), ""));
        config.setUpdateDateTime(null);
        config.setProfessionalNames(form.getProfessionalNames());
        config.setProfessionalIds(form.getProfessionals());
        config.setPeripheralsId(form.getPeripheralsId());
        config.setPlateColor(form.getPlateColor());
        config.setVehicleType(form.getVehicleType());
        config.setMonitorType(form.getMonitorType());
        config.setDeviceId(form.getDeviceID());
        config.setSimcardId(form.getSimID());
        config.setServiceLifecycleId(form.getServiceLifecycleId());
        config.setCategory(form.getCategory());
        config.setType(form.getType());
        config.setTypeName(form.getTypeName());
        config.setCategoryName(form.getCategoryName());
        config.setTerminalManufacturer(form.getTerminalManufacturer());
        config.setTerminalType(form.getTerminalType());
        config.setTerminalTypeId(form.getTerminalTypeId());
        config.setRealId(form.getRealId());
        return config;
    }

    public static ConfigList initTransportConfigImport(ConfigTransportImportForm form) {
        ConfigList config = new ConfigList();
        config.setConfigId(form.getId());
        config.setCarLicense(form.getBrand());
        config.setPeopleName("");
        config.setVehicleId(form.getBrandID());
        config.setThingName("");
        config.setGroupId(form.getGroupId());
        // config.setGroupName(orgName);
        config.setGroupName(form.getGroupName());
        config.setAssignmentName(form.getAssignNames());
        config.setAssignmentId(form.getAssignIds());
        config.setDeviceNumber(form.getDeviceNumber());
        config.setDeviceType(form.getDeviceType());
        config.setFunctionalType(form.getFunctionalType());
        config.setSimcardNumber(form.getSimcardNumber());
        config.setBillingDate(form.getBillingDateStr());
        config.setExpireDate(form.getExpireTimeStr());
        config.setCreateDateTime(DateUtil.getDateToString(form.getCreateDataTime(), ""));
        config.setUpdateDateTime(null);
        config.setProfessionalNames(form.getProfessionalNames());
        // config.setProfessionalIds(form.getProfessionals());
        // config.setPeripheralsId(form.getPeripheralsId());
        config.setPlateColor(form.getPlateColor());
        config.setVehicleType(form.getVehicleType());
        config.setMonitorType(form.getMonitorType());
        config.setDeviceId(form.getDeviceID());
        config.setSimcardId(form.getSimID());
        config.setServiceLifecycleId(form.getServiceLifecycleId());
        config.setCategory(form.getVehicleCategoryId());
        config.setType(form.getType());
        config.setTypeName(form.getTypeName());
        config.setCategoryName(form.getVehicleCategoryName());
        config.setTerminalManufacturer(form.getTerminalManufacturer());
        config.setTerminalType(form.getTerminalType());
        return config;
    }

    public ConfigList(BindDTO bindDTO) {
        this.configId = bindDTO.getConfigId();
        this.carLicense = bindDTO.getName();
        this.vehicleId = bindDTO.getId();
        this.groupId = bindDTO.getOrgId();
        this.groupName = bindDTO.getOrgName();
        this.assignmentId = bindDTO.getGroupId();
        this.assignmentName = bindDTO.getGroupName();
        this.simcardId = bindDTO.getSimCardId();
        this.simcardNumber = bindDTO.getSimCardNumber();
        this.realId = bindDTO.getRealSimCardNumber();
        this.deviceId = bindDTO.getDeviceId();
        this.deviceNumber = bindDTO.getDeviceNumber();
        this.deviceType = bindDTO.getDeviceType();
        this.functionalType = bindDTO.getFunctionalType();
        this.terminalType = bindDTO.getTerminalType();
        this.terminalManufacturer = bindDTO.getTerminalManufacturer();
        this.terminalTypeId = bindDTO.getTerminalTypeId();
        if (StringUtils.isNotBlank(bindDTO.getBillingDate())) {
            this.billingDate = bindDTO.getBillingDate() + " 0:00:00";
        }
        if (StringUtils.isNotBlank(bindDTO.getExpireDate())) {
            this.expireDate = bindDTO.getExpireDate() + " 0:00:00";
        }
        if (StringUtils.isNotBlank(bindDTO.getBindDate())) {
            this.createDateTime = bindDTO.getBindDate();
        }
        if (StringUtils.isNotBlank(bindDTO.getUpdateBindDate())) {
            this.updateDateTime = bindDTO.getUpdateBindDate();
        }
        this.professionalIds = bindDTO.getProfessionalIds();
        this.professionalNames = bindDTO.getProfessionalNames();
        this.plateColor = bindDTO.getPlateColor();
        this.monitorType = bindDTO.getMonitorType();
        this.serviceLifecycleId = bindDTO.getServiceLifecycleId();
        this.owersName = bindDTO.getOrgName();
    }

    public void setVehicleInfo(Map<String, ConfigList> vehicleConfigMap) {
        ConfigList vehicleConfig = vehicleConfigMap.get(vehicleId);
        if (vehicleConfig == null) {
            return;
        }
        //设置车辆运营类别放到运营类别到信息配置中
        vehiclePurpose = vehicleConfig.getVehiclePurpose();
        //设置车辆状态信息配置中
        isStart = vehicleConfig.getIsStart();
        province = vehicleConfig.getProvince();
        provinceId = vehicleConfig.getProvinceId();
        city = vehicleConfig.getCity();
        cityId = vehicleConfig.getCityId();
        county = vehicleConfig.getCounty();
    }

    /**
     * 填充车辆的行政区划信息
     */
    public void initAreaInfo(JSONObject vehicleDetail) {
        if (vehicleDetail == null) {
            return;
        }
        province = vehicleDetail.getString("province");
        provinceId = vehicleDetail.getString("provinceId");
        city = vehicleDetail.getString("city");
        cityId = vehicleDetail.getString("cityId");
        county = vehicleDetail.getString("county");
    }

    /**
     * 填充车辆的行政区划信息
     */
    public void initAreaInfo(VehicleForm vehicleForm) {
        if (vehicleForm == null) {
            return;
        }
        this.province = vehicleForm.getProvince();
        this.provinceId = vehicleForm.getProvinceId();
        this.city = vehicleForm.getCity();
        this.cityId = vehicleForm.getCityId();
        this.county = vehicleForm.getCounty();
    }
}
