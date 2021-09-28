package com.zw.platform.domain.infoconfig.query;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.basic.constant.GenderEnum;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDetailDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Objects;

/**
 * Created by Tdz on 2016/8/3.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ConfigDetailsQuery {

    private String id;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 车辆编号
     */
    private String vehicleNumber;

    /**
     * 车主
     */
    private String vehicleOwner;

    /**
     * 车主电话
     */
    private String vehicleOwnerPhone;

    /**
     * 别名
     */
    private String aliases;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 车辆类别
     */
    private String category;

    /**
     * 机架号
     */
    private String chassisNumber;

    /**
     * 发动机号
     */
    private String engineNumber;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 区域属性
     */
    private String areaAttribute;

    /**
     * 省、直辖市
     */
    private String province;

    /**
     * 市、区
     */
    private String city;

    /**
     * 县
     */
    private String county;

    /**
     * 燃油类型
     */
    private String fuelType;

    /**
     * 车牌颜色（1蓝、2黄、3黑、4白）
     */
    private Integer plateColor; // 车牌颜色
    private String plateColorStr = "";
    private String deviceId; // 终端id
    private String deviceNumber; // 终端编号
    private String deviceName; // 设备名称
    private Integer isStart; // 启停状态
    private String isStartStr = "";
    private String deviceType; // 通讯类型
    private String functionalType = ""; // 功能类型
    private Integer channelNumber; // 通道数
    private String channelNumberStr = "";
    private Integer isVideo; // 是否视频
    private String isVideoStr = "";
    private String barCode; // 条码
    private String manuFacturer; // 制造商
    private String peripheralsId; // 外设id
    private String iccid = ""; // sim卡iccid
    private String imsi = ""; // sim卡imsi
    private String realId; // 真实sim卡号
    private Date endTime; // sim到期时间
    private String openCardTimeStr;
    private String endTimeStr;
    private String terminalManufacturer; // 终端厂商
    private String terminalType; // 终端类型
    private Date installTime; // 终端安装日期
    private String installTimeStr;
    private String vehiclePurpose; // 车辆的运营类别

    /**
     * sim卡号
     */
    private String simcardNumber;

    /**
     * 启停状态
     */
    private Integer simIsStart;

    /**
     * 运营商
     */
    private String operator;

    /**
     * 开卡时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date openCardTime;

    /**
     * 容量
     */
    private String capacity;

    /**
     * 网络类型
     */
    private String networkType;

    /**
     * 套餐流量
     */
    private String simFlow;

    /**
     * 已用流量
     */
    private String useFlow;
    private String professionalsId;
    private String groupId;

    private JSONArray jsonArray;

    private String parentGroupid = ""; // 车辆所属企业id
    private String parentGroupname = ""; // 车辆所属企业名称

    private String deviceParentGroupid = "";
    private String deviceParentGroupname = "";

    private String simParentGroupid = "";
    private String simParentGroupname = "";

    private String monitorType; // 监控对象类型
    /**
     * 人员信息
     */
    private String peopleName = "";//姓名
    private String identity = "";//身份证号
    private String gender = "";//性别
    private String phone = "";//电话号码
    private String email = "";//电子邮件
    /**
     * 物品信息
     */
    private String thingName;//物品名称
    private String thingCategory;//物品类别
    private String thingCategoryName;//物品类别名称
    private String thingType;//物品类型
    private String thingTypeName;//物品类型名称
    private String model;//型号
    private String manufacture;//制造商

    public ConfigDetailsQuery(ConfigDetailDTO configDetailDTO) {
        BindDTO monitor = configDetailDTO.getMonitor();
        this.id = monitor.getConfigId();
        this.parentGroupid = monitor.getOrgId();
        this.parentGroupname = monitor.getOrgName();
        this.monitorType = monitor.getMonitorType();
        if (monitor instanceof VehicleDTO) {
            VehicleDTO vehicle = (VehicleDTO) monitor;
            this.vehicleId = vehicle.getId();
            this.vehicleOwner = vehicle.getVehicleOwner();
            this.vehicleOwnerPhone = vehicle.getVehicleOwnerPhone();
            this.aliases = vehicle.getAlias();
            this.vehicleType = vehicle.getVehicleTypeName();
            this.category = vehicle.getVehicleCategoryName();
            this.chassisNumber = vehicle.getChassisNumber();
            this.engineNumber = vehicle.getEngineNumber();
            this.brand = vehicle.getName();
            this.areaAttribute = vehicle.getAreaAttribute();
            this.province = vehicle.getProvince();
            this.city = vehicle.getCity();
            this.county = vehicle.getCounty();
            this.fuelType = vehicle.getFuelTypeName();
            this.plateColor = vehicle.getPlateColor();
            this.plateColorStr = PlateColor.getNameOrBlankByCode(vehicle.getPlateColor());
            this.vehiclePurpose = vehicle.getVehiclePurposeName();
        } else if (monitor instanceof PeopleDTO) {
            PeopleDTO peopleDTO = (PeopleDTO) monitor;
            this.peopleName = peopleDTO.getAlias();
            this.brand = peopleDTO.getName();
            this.identity = peopleDTO.getIdentity();
            this.gender = GenderEnum.getName(peopleDTO.getGender());
            this.phone = peopleDTO.getPhone();
            this.email = peopleDTO.getEmail();
        } else if (monitor instanceof ThingDTO) {
            ThingDTO thingDTO = (ThingDTO) monitor;
            this.thingName = thingDTO.getAlias();
            this.brand = thingDTO.getName();
            this.thingCategory = thingDTO.getCategory();
            this.thingCategoryName = thingDTO.getCategoryName();
            this.thingType = thingDTO.getType();
            this.thingTypeName = thingDTO.getTypeName();
            this.model = thingDTO.getModel();
            this.manufacture = thingDTO.getManufacture();
        }

        //封装终端信息
        if (Objects.nonNull(configDetailDTO.getDevice())) {
            DeviceDTO deviceDTO = configDetailDTO.getDevice();
            this.deviceId = deviceDTO.getId();
            this.deviceNumber = deviceDTO.getDeviceNumber();
            this.deviceName = deviceDTO.getDeviceName();
            this.isStart = deviceDTO.getIsStart();
            this.isStartStr = Objects.isNull(this.isStart) ? "" : this.isStart == 1 ? "启用" : "停用";
            this.deviceType = ProtocolEnum.getDeviceNameByDeviceType(deviceDTO.getDeviceType());
            this.functionalType = ConstantUtil.getDeviceFunctionType(deviceDTO.getFunctionalType());
            this.channelNumber = deviceDTO.getChannelNumber();
            this.channelNumberStr = Objects.isNull(channelNumber) ? "" : channelNumber == 1 ? "4" :
                channelNumber == 2 ? "5" : channelNumber == 3 ? "8" : channelNumber == 4 ? "16" : "";
            this.manuFacturer = deviceDTO.getManuFacturer();
            this.terminalManufacturer = deviceDTO.getTerminalManufacturer();
            this.terminalType = deviceDTO.getTerminalType();
            this.installTime = deviceDTO.getInstallTime();
            if (Objects.nonNull(this.installTime)) {
                this.installTimeStr = DateUtil.getDayStr(this.installTime);
            }
            this.isVideo = deviceDTO.getIsVideo();
            this.isVideoStr = Objects.isNull(this.isVideo) ? "" : this.isVideo == 1 ? "是" : "否";
            this.barCode = deviceDTO.getBarCode();
            this.deviceParentGroupid = deviceDTO.getOrgId();
            this.deviceParentGroupname = deviceDTO.getOrgName();
        }

        //封装SIM卡信息
        if (Objects.nonNull(configDetailDTO.getSimCard())) {
            SimCardDTO simCard = configDetailDTO.getSimCard();
            this.imsi = simCard.getImsi();
            this.iccid = simCard.getIccid();
            this.realId = simCard.getRealId();
            this.endTime = simCard.getEndTime();
            this.openCardTime = simCard.getOpenCardTime();
            if (Objects.nonNull(this.openCardTime)) {
                this.openCardTimeStr = DateUtil.getDayStr(this.openCardTime);
            }
            if (Objects.nonNull(this.endTime)) {
                this.endTimeStr = DateUtil.getDayStr(this.endTime);
            }
            this.simcardNumber = simCard.getSimcardNumber();
            this.simIsStart = simCard.getIsStart();
            this.operator = simCard.getOperator();
            this.capacity = simCard.getCapacity();
            this.networkType = simCard.getNetworkType();
            this.simFlow = simCard.getSimFlow();
            this.useFlow = simCard.getUseFlow();
            this.simParentGroupid = simCard.getOrgId();
            this.simParentGroupname = simCard.getOrgName();
        }
    }
}