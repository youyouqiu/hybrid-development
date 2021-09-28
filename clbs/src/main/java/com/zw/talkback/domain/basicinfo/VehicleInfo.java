package com.zw.talkback.domain.basicinfo;

import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Title: 车辆实体类
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年7月21日下午6:57:34
 */
@Data
public class VehicleInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 车辆信息
     */
    private String id;

    /**
     * 车辆编号
     */
    @Deprecated
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
     * 车辆类型名称
     */
    private String vehiclet;

    private String vehType;

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
    private Integer plateColor = 2;

    /**
     * 0不显示、1显示
     */
    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
    /**
     * 车辆所属组织
     */
    private String groupId;

    /**
     * 车辆所属企业（组织）
     */
    private String groupName;

    /**
     * 车辆类别
     */
    private String vehicleCategory;

    /**
     * 分组id
     */
    private String assignmentId;

    /**
     * 终端编号
     */
    private String deviceNumber;

    /**
     * sim卡卡号
     */
    private String simcardNumber;

    /**
     * sim卡id
     */
    private String simcardId;

    /**
     * 标识视频
     */
    private Integer isVideo;
    /**
     * 电话号码
     */
    private String phone;
    private String mobile;
    private String professionalsName;

    /**
     * 绑定id
     */
    private String bindId;

    /**
     * 节油产品安装时间
     */
    private String savingProductsInstallTime = "";
    /**
     * 车辆状态
     * 1：启用
     * 0：停用
     */
    private Integer isStart;
    /**
     * 车辆用途id
     */
    private String vehiclePurpose;
    private String vehiclePurposeName;

    /**
     * 车辆营运证号(经营许可证-工程机械修改)
     */
    private String vehiclOperationNumber;
    /**
     * 道路运输证号
     */
    private String roadTransportNumber;
    /**
     * 道路运输证有效期 (道路运输有效期至 -工程机械修改)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehicleTechnologyValidity;
    /**
     * 车辆颜色  (0黑、1白、2红、3蓝、4紫、5黄、6绿、7粉、8棕、9灰)
     */
    private String vehicleColor;

    private String monitorType; // 监控对象类型 0：车；1：人

    /**
     * 终端类型
     */
    private String deviceType;
    /**
     * 从业资格IC卡号
     */
    private String cardNumber;
    /**
     * 个性化图标
     */
    private String vehicleTypeIcon;
    /**
     * 个性化车型图标
     */
    private String categoryIcon;

    private String createDataTimeStr;

    /**
     * 设备类型
     */
    private String deviceId;
    /**
     * 分组名称
     */
    private String assignmentName;

    private String assign;

    /**
     * 保养里程数
     */
    private Integer maintainMileage;

    /**
     * 保养有效期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date maintainValidity;
    private String maintainValidityStr;

    /**
     * 车台安装日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehiclePlatformInstallDate;
    private String vehiclePlatformInstallDateStr;
    /**
     * 备注
     */
    private String remark;

    public static VehicleInfo fromForm(VehicleForm form) {
        VehicleInfo vehicleInfo = new VehicleInfo();
        vehicleInfo.setId(form.getId());
        vehicleInfo.setBrand(form.getBrand());
        vehicleInfo.setVehicleType(form.getVehicleType());
        vehicleInfo.setGroupId(form.getGroupId());
        vehicleInfo.setCreateDataUsername(form.getCreateDataUsername());
        return vehicleInfo;
    }


    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~工程机械新增字段~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    /**
     * 车辆上线时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Deprecated
    private Date onlineTime;

    /**
     * 车辆图标id
     */
    private String vehicleIcon;

    /**
     * 车辆等级
     */
    private String vehicleLevel;

    /**
     * 电话是否校验(0：未检验；1：已校验)
     */
    private Short phoneCheck;

    /**
     * 核定载人数
     */
    private Short numberLoad;

    /**
     * 核定载质量(kg)
     */
    private String loadingQuality;

    /**
     * 维修状态(0：否；1：是)
     */
    private Short stateRepair;

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
    private Short managementType;

    /**
     * 道路运输证有效期起
     */
    //    @DateTimeFormat(pattern = "yyyy-MM-dd")
    //    private Date managementStartDate;

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
     * 行驶证登记日期
     */
    @ExcelField(title = "行驶证登记日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationDate;

    /**
     * 行驶证有效期起
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationStartDate;

    /**
     * 行驶证有效期至
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationEndDate;

    /**
     * 提前提醒天数
     */
    @ExcelField(title = "提前提醒天数")
    private Integer registrationRemindDays;

    /**
     * 备注
     */
    private String registrationRemark;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehicleProductionDate;

    /**
     * 首次上线时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date firstOnlineTime;

    /**
     * 车辆购置方式(0:分期付款;1:一次性付清)
     */
    private Short purchaseWay;

    /**
     * 校验有效期至
     */
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date validEndDate;

    /**
     * 执照上传数
     */
    private Short licenseNumbers;

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
    private Long internalSizeHigh;

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
    @DateTimeFormat(pattern = "yyyy-MM")
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
     * 县
     */
    private String county;

    /**
     * 使用性质
     */
    private String usingNature;

    /**
     * 品牌型号
     */
    private String brandModel;

    /**
     * 行驶证发证日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date licenseIssuanceDate;

    /**
     * 道路运输证有效期起
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date roadTransportValidityStart;

    /**
     * 车辆子类型Id(只用于接收前端传递的值,如果这个值存在，那么vehicle_type存,如果不存在，则存VehicleType)
     */
    private String vehicleSubTypeId;

    /**
     * 车辆子类型名称(编辑使用)
     */
    private String vehicleSubtypes;

    /**
     * 类别名
     */
    private String vehicleCategoryName;

    /**
     * 类别id
     */
    private String vehicleCategoryId;

    private String standard;
    /**
     * 机型名称
     */
    private String modelName;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 省域ID
     */
    private String provinceId;
    /**
     * 市域ID
     */
    private String cityId;

    /**
     * 是否离职  1 离职
     */
    private Integer isLeave;
}
