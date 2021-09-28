package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.enums.VehicleColor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * zw_m_vehicle_info
 * @author zhangjuan 2020-09-28
 */
@Data
public class VehicleDO {
    /**
     * 车辆信息
     */
    private String id;
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
     * 燃油类型
     */
    private String fuelType;
    /**
     * 车牌颜色（1蓝，2黄，3黑，4白，9其他）
     */
    private Integer plateColor;
    /**
     * 启停状态
     */
    private Integer isStart;
    /**
     * 0不显示、1显示
     */
    private Integer flag;
    /**
     * 车辆上线时间
     */
    private Date onlineTime;
    /**
     * create_data_time
     */
    private Date createDataTime;
    /**
     * create_data_username
     */
    private String createDataUsername;
    /**
     * update_data_time
     */
    private Date updateDataTime;
    /**
     * update_data_username
     */
    private String updateDataUsername;
    /**
     * 车辆用途
     */
    private String vehiclePurpose;
    /**
     * 车辆营运证号
     */
    private String vehiclOperationNumber;
    /**
     * 道路运输证号
     */
    private String roadTransportNumber;
    /**
     * 道路运输证有效期
     */
    private Date roadTransportValidity;
    /**
     * 车辆保险单号
     */
    private String vehicleInsuranceNumber;
    /**
     * 车辆照片
     */
    private String vehiclePhoto;
    /**
     * 车辆技术登记有效期
     */
    private Date vehicleTechnologyValidity;
    /**
     * 车辆颜色
     */
    private String vehicleColor;
    /**
     * 车辆图标id
     */
    private String vehicleIcon;
    /**
     * 车辆备注
     */
    private String remark;
    /**
     * 子类型id
     */
    private String vehicleSubTypeId;
    /**
     * 车辆等级
     */
    private String vehicleLevel;
    /**
     * 电话是否校验(0：未检验；1：已校验)
     */
    private Integer phoneCheck;
    /**
     * 核定载人数
     */
    private Integer numberLoad;
    /**
     * 核定载质量(kg)
     */
    private String loadingQuality;
    /**
     * 维修状态(0：否；1：是)
     */
    private Integer stateRepair;
    /**
     * 县
     */
    private String county;
    /**
     * 经营范围
     */
    private String scopeBusiness;
    /**
     * 核发机关
     */
    private String issuedAuthority;
    /**
     * 经营权类型(0：国有；1：集体；2：私营；3：个体；4：联营；5：股份制；6：外商投资；7：港澳台及其他)
     */
    private Integer managementType;
    /**
     * 道路运输证有效期起
     */
    private Date roadTransportValidityStart;
    /**
     * 线路牌号
     */
    private String lineNumber;
    /**
     * 始发地
     */
    private String provenance;
    /**
     * 途经站名
     */
    private String viaName;
    /**
     * 终到地
     */
    private String destination;
    /**
     * 始发站
     */
    private String departure;
    /**
     * 路线入口
     */
    private String routeEntry;
    /**
     * 终到站
     */
    private String destinationStation;
    /**
     * 路线出口
     */
    private String exportRoute;
    /**
     * 每日发班次数
     */
    private Integer dailyNumber;
    /**
     * 提前提醒天数
     */
    private Integer managementRemindDays;
    /**
     * 营运状态(0:营运;1:停运;2:挂失;3:报废;4:歇业;5:注销;6:迁出(过户);7:迁出(转籍);8:其他)
     */
    private Integer operatingState;
    /**
     * 行驶证号
     */
    private String licenseNo;
    /**
     * 登记日期
     */
    private Date registrationDate;
    /**
     * 行驶证有效期起
     */
    private Date registrationStartDate;
    /**
     * 行驶证有效期至
     */
    private Date registrationEndDate;
    /**
     * 提前提醒天数
     */
    private Integer registrationRemindDays;
    /**
     * 备注
     */
    private String registrationRemark;
    /**
     * 品牌型号
     */
    private String brandModel;
    /**
     * 行驶证发证日期
     */
    private Date licenseIssuanceDate;
    /**
     * 车辆品牌
     */
    private String vehicleBrand;
    /**
     * 车辆型号
     */
    private String vehicleModel;
    /**
     * 车辆出厂日期
     */
    private Date vehicleProductionDate;
    /**
     * 首次上线时间
     */
    private Date firstOnlineTime;
    /**
     * 车辆购置方式(0:分期付款;1:一次性付清)
     */
    private Integer purchaseWay;
    /**
     * 校验有效期至
     */
    private Date validEndDate;
    /**
     * 执照上传数
     */
    private Integer licenseNumbers;
    /**
     * 总质量(kg)
     */
    private String totalQuality;
    /**
     * 准牵引总质量(kg)
     */
    private String tractionTotalMass;
    /**
     * 外廓尺寸-长(mm)
     */
    private Integer profileSizeLong;
    /**
     * 外廓尺寸-宽(mm)
     */
    private Integer profileSizeWide;
    /**
     * 外廓尺寸-高(mm)
     */
    private Integer profileSizeHigh;
    /**
     * 货厢内部尺寸-长(mm)
     */
    private Integer internalSizeLong;
    /**
     * 货厢内部尺寸-宽(mm)
     */
    private Integer internalSizeWide;
    /**
     * 货厢内部尺寸-高(mm)
     */
    private Integer internalSizeHigh;
    /**
     * 轴数
     */
    private Integer shaftNumber;
    /**
     * 轮胎数
     */
    private Integer tiresNumber;
    /**
     * 轮胎规格
     */
    private String tireSize;
    /**
     * 车主名
     */
    private String vehicleOwnerName;
    /**
     * 车主手机1
     */
    private String ownerPhoneOne;
    /**
     * 车主手机2
     */
    private String ownerPhoneTwo;
    /**
     * 车主手机3
     */
    private String ownerPhoneThree;
    /**
     * 车主座机
     */
    private String ownerLandline;
    /**
     * 机龄
     */
    private Date machineAge;
    /**
     * 自重
     */
    private Double selfRespect;
    /**
     * 工作能力
     */
    private Double abilityWork;
    /**
     * 工作半径
     */
    private Double workingRadius;
    /**
     * 品牌机型id
     */
    private String brandModelsId;
    /**
     * 初始里程
     */
    private Double initialMileage;
    /**
     * 初始工时
     */
    private Double initialWorkHours;
    /**
     * 保养里程数(km)
     */
    private Integer maintainMileage;
    /**
     * 保养有效期
     */
    private Date maintainValidity;
    /**
     * 车台安装日期
     */
    private Date vehiclePlatformInstallDate;
    /**
     * 省域id
     */
    private String provinceId;
    /**
     * 市域id
     */
    private String cityId;
    /**
     * 行业类别
     */
    private String tradeName;
    /**
     * 行驶证正本
     */
    private String drivingLicenseFrontPhoto;
    /**
     * 行驶证副本
     */
    private String drivingLicenseDuplicatePhoto;
    /**
     * 运输证照片
     */
    private String transportNumberPhoto;

    /**
     * 车辆所属企业id
     */
    private String orgId;

    /**
     * 使用性质
     */
    private String usingNature;

    public static VehicleDO build(VehicleDTO vehicleDTO, boolean withDefaultValue) {
        VehicleDO vehicleDO = new VehicleDO();
        BeanUtils.copyProperties(vehicleDTO, vehicleDO);
        vehicleDO.setBrand(vehicleDTO.getName());
        vehicleDO.setFlag(1);
        vehicleDO.setAliases(vehicleDTO.getAlias());

        if (withDefaultValue) {
            if (StringUtils.isBlank(vehicleDTO.getId())) {
                String id = UUID.randomUUID().toString();
                vehicleDO.setId(id);
                vehicleDTO.setId(id);
            }

            //设置默认车辆颜色
            if (StringUtils.isBlank(vehicleDTO.getVehicleColor())) {
                vehicleDO.setVehicleColor(VehicleColor.BLACK.getCodeVal());
                vehicleDTO.setVehicleColor(VehicleColor.BLACK.getCodeVal());
            }

            //设置默认车牌颜色
            if (Objects.isNull(vehicleDTO.getPlateColor())) {
                vehicleDO.setPlateColor(PlateColor.YELLOW.getCodeVal());
                vehicleDTO.setPlateColor(PlateColor.YELLOW.getCodeVal());
            }
        }
        return vehicleDO;
    }
}
