package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.DictionaryType;
import com.zw.platform.basic.constant.RegexKey;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.BusinessScopeDO;
import com.zw.platform.basic.domain.CargoGroupVehicleDO;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.BusinessScopeService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.enums.VehicleColor;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 车辆导入逻辑处理类
 * @author zhangjuan
 */
@Slf4j
public class VehicleImportHandler extends BaseImportHandler {
    private List<VehicleDTO> importList;
    private NewVehicleDao vehicleDao;
    private ConfigHelper configHelper;
    private VehicleService vehicleService;
    private BusinessScopeService businessScopeService;
    private int standard;
    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();
    private List<VehicleDO> vehicleList;
    private List<BusinessScopeDO> businessScopes;
    private List<CargoGroupVehicleDO> cargoVehicleList;

    public VehicleImportHandler(List<VehicleDTO> importList, NewVehicleDao vehicleDao, VehicleService vehicleService,
        ConfigHelper configHelper, BusinessScopeService businessScopeService) {
        this.importList = importList;
        this.standard = importList.get(0).getStandard();
        this.vehicleDao = vehicleDao;
        this.vehicleService = vehicleService;
        this.configHelper = configHelper;
        this.businessScopeService = businessScopeService;
    }

    @Override
    public ImportModule module() {
        return ImportModule.VEHICLE;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_VEHICLE_INFO };
    }

    @Override
    public boolean uniqueValid() {
        //进行参数校验
        validate();
        Set<String> errors = importList.stream().map(VehicleDTO::getErrorMsg).filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
        boolean empty = CollectionUtils.isEmpty(errors);
        if (empty) {
            //进行参数补全
            completeImportInfo();
            //构建插入db的数据库参数
            buildDbData();
            progressBar.setTotalProgress(vehicleList.size() * 3 / 2 + businessScopes.size() + cargoVehicleList.size());
        }
        return empty;
    }

    private void buildDbData() {
        //添加车辆信息
        vehicleList = new ArrayList<>();
        businessScopes = new ArrayList<>();
        cargoVehicleList = new ArrayList<>();

        for (VehicleDTO vehicle : importList) {
            vehicleList.add(VehicleDO.build(vehicle, true));
            if (StringUtils.isBlank(vehicle.getScopeBusinessIds())) {
                continue;
            }
            String[] scopeBusinessIds = vehicle.getScopeBusinessIds().split(",");
            for (String scopeBusinessId : scopeBusinessIds) {
                businessScopes.add(new BusinessScopeDO(vehicle.getId(), scopeBusinessId, "2"));
            }

            if (configHelper.cargoReportSwitch && Objects.equals(standard, Vehicle.Standard.FREIGHT_TRANSPORT)) {
                cargoVehicleList.add(new CargoGroupVehicleDO(vehicle, "add"));
            }
        }

    }

    @Override
    public boolean addMysql() {
        partition(vehicleList, vehicleDao::addByBatch);

        //维护经营范围
        partition(businessScopes, businessScopeService::addBusinessScope);

        partition(cargoVehicleList, vehicleDao::addCargoGroupVehicle);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        vehicleService.addOrUpdateRedis(importList, new HashSet<>());
    }

    /**
     * 校验车辆类型
     */
    private void checkVehicleType() {
        //获对应标准下的车辆类别
        List<VehicleCategoryDTO> categoryList = cacheManger.getVehicleCategories(standard);
        Map<String, String> vehicleCategoryMap =
            AssembleUtil.collectionToMap(categoryList, VehicleCategoryDTO::getCategory, VehicleCategoryDTO::getId);

        //对应标准的车辆类型
        List<VehicleTypeDTO> vehicleTypes = cacheManger.getVehicleTypes(new HashSet<>(vehicleCategoryMap.values()));
        Map<String, String> vehicleTypeMap = new HashMap<>(16);
        vehicleTypes.forEach(o -> vehicleTypeMap.put(o.getCategory() + o.getType(), o.getId()));

        //获取车辆子类型
        Map<String, String> vehicleSubTypeMap = new HashMap<>(16);
        if (Objects.equals(standard, Vehicle.Standard.ENGINEERING)) {
            List<VehicleSubTypeDTO> vehicleSubTypes = cacheManger.getVehicleSubTypes(null);
            vehicleSubTypes.forEach(o -> vehicleSubTypeMap.put(o.getTypeId() + o.getSubType(), o.getId()));
        }

        for (VehicleDTO vehicle : importList) {
            if (StringUtils.isNotBlank(vehicle.getErrorMsg())) {
                continue;
            }
            String categoryId = vehicleCategoryMap.get(vehicle.getVehicleCategoryName());
            if (StringUtils.isBlank(categoryId)) {
                vehicle.setErrorMsg("车辆类别不存在");
                continue;
            }
            vehicle.setVehicleCategoryId(categoryId);

            String vehicleTypeId = vehicleTypeMap.get(vehicle.getVehicleCategoryName() + vehicle.getVehicleTypeName());
            if (Objects.isNull(vehicleTypeId)) {
                vehicle.setErrorMsg("车辆类型类别和类别标准不匹配");
                continue;
            }
            vehicle.setVehicleType(vehicleTypeId);

            if (!Objects.equals(standard, Vehicle.Standard.ENGINEERING)) {
                continue;
            }

            String subTypeId = vehicleSubTypeMap.get(vehicleTypeId + vehicle.getVehicleSubType());
            if (StringUtils.isBlank(subTypeId)) {
                vehicle.setErrorMsg("车辆类型和车辆子类型不匹配");
                continue;
            }

            vehicle.setVehicleSubTypeId(subTypeId);
        }
    }

    /**
     * 补全车辆信息
     */
    private void completeImportInfo() {
        vehicleList = new ArrayList<>();
        Collection<VehiclePurposeDTO> purposeList = cacheManger.getVehiclePurposes();
        Map<String, String> vehiclePurposeMap =
            AssembleUtil.collectionToMap(purposeList, VehiclePurposeDTO::getPurposeCategory, VehiclePurposeDTO::getId);
        String defaultPurpose = null;
        Iterator<String> iterator = vehiclePurposeMap.values().iterator();
        if (iterator.hasNext()) {
            defaultPurpose = iterator.next();
        }
        Map<String, String> businessScopeMap = cacheManger.getDictValueIdMap(DictionaryType.BUSINESS_SCOPE);
        for (VehicleDTO vehicle : importList) {
            //电话是否校验
            vehicle.setPhoneCheck(getIndex(Vehicle.PHONE_CHECKS, vehicle.getPhoneCheckStr()));
            //区域属性
            if (StringUtils.isNotBlank(vehicle.getAreaAttribute())) {
                Integer tempValue = getIndex(Vehicle.AREA_ATTRIBUTES, vehicle.getAreaAttribute());
                if (Objects.isNull(tempValue)) {
                    vehicle.setAreaAttribute(Vehicle.AREA_ATTRIBUTES[0]);
                }
            }

            //车辆状态
            if (Converter.toBlank(vehicle.getIsStartStr()).equals(Vehicle.IS_START[1])) {
                vehicle.setIsStart(0);
            } else {
                vehicle.setIsStart(1);
            }

            vehicle.setPlateColor(PlateColor.getCodeOrDefaultByName(vehicle.getPlateColorStr()));
            vehicle.setAlias(checkLength(vehicle.getAlias(), 20, false));

            String vehicleOwner = vehicle.getVehicleOwner();
            if (StringUtils.isNotBlank(vehicleOwner) && !Pattern.matches(RegexKey.VEHICLE_OWNER_REGEX, vehicleOwner)) {
                vehicle.setVehicleOwner("");
            }

            String ownerPhone = vehicle.getVehicleOwnerPhone();
            if (StringUtils.isNotBlank(ownerPhone) && !RegexUtils.checkOwnerPhone(ownerPhone)) {
                vehicle.setVehicleOwnerPhone("");
            }

            vehicle.setProvince(checkLength(vehicle.getProvince(), 20, false));
            vehicle.setCity(checkLength(vehicle.getCity(), 20, false));

            String purpose = vehiclePurposeMap.getOrDefault(vehicle.getVehiclePurposeName(), defaultPurpose);
            vehicle.setVehiclePurpose(purpose);

            vehicle.setVehicleColor(VehicleColor.getCodeOrDefaultByName(vehicle.getVehicleColorStr()));
            // 车辆营运证号
            vehicle.setVehiclOperationNumber(checkLength(vehicle.getVehiclOperationNumber(), 20, false));
            // 道路运输证号
            vehicle.setRoadTransportNumber(checkLength(vehicle.getRoadTransportNumber(), 24, false));
            // 县
            vehicle.setCounty(checkLength(vehicle.getCounty(), 20, false));
            // 车辆等级
            vehicle.setVehicleLevel(checkLength(vehicle.getVehicleLevel(), 20, false));

            //是否维修
            vehicle.setStateRepair(getIndex(Vehicle.STATE_REPAIRS, vehicle.getStateRepairStr()));

            //核定载人数
            Integer numberLoad = vehicle.getNumberLoad();
            if (!(numberLoad != null && numberLoad <= 9999 && numberLoad >= 0)) {
                vehicle.setNumberLoad(null);
            }

            String loadQuality = vehicle.getLoadingQuality();
            if ((StringUtils.isBlank(loadQuality)) || !Pattern.matches(RegexKey.DOUBLE_REGEX_10_1, loadQuality)) {
                vehicle.setLoadingQuality(null);
            }

            //车辆保险单号
            String vehicleInsuranceNumber = vehicle.getVehicleInsuranceNumber();
            if (StringUtils.isBlank(vehicleInsuranceNumber) || vehicleInsuranceNumber.length() > 50) {
                vehicle.setVehicleInsuranceNumber(null);
            }

            // 道路运输证号
            vehicle.setRoadTransportNumber(checkLength(vehicle.getRoadTransportNumber(), 20, false));
            // 车辆营运证号
            vehicle.setVehiclOperationNumber(checkLength(vehicle.getVehiclOperationNumber(), 20, false));
            vehicle.setScopeBusinessIds(getBusinessScopeId(vehicle.getScopeBusiness(), businessScopeMap));
            // 核发机关
            vehicle.setIssuedAuthority(checkLength(vehicle.getIssuedAuthority(), 20, false));
            //经营权类型
            vehicle.setManagementType(getIndex(Vehicle.MANAGEMENT_TYPES, vehicle.getManagementTypeStr()));

            // 线路牌号
            vehicle.setLineNumber(checkLength(vehicle.getLineNumber(), 20, false));
            // 始发地
            vehicle.setProvenance(checkLength(vehicle.getProvenance(), 20, false));
            // 途经站名
            vehicle.setViaName(checkLength(vehicle.getViaName(), 20, false));
            // 终到地
            vehicle.setDestination(checkLength(vehicle.getDestination(), 20, false));
            // 始发站
            vehicle.setDeparture(checkLength(vehicle.getDeparture(), 20, false));
            // 路线入口
            vehicle.setRouteEntry(checkLength(vehicle.getRouteEntry(), 20, false));
            // 终到站
            vehicle.setDestinationStation(checkLength(vehicle.getDestinationStation(), 20, false));
            // 路线出口
            vehicle.setExportRoute(checkLength(vehicle.getExportRoute(), 20, false));

            //每日发班次数
            Integer dailyNumber = vehicle.getDailyNumber();
            if (!(Objects.nonNull(dailyNumber) && dailyNumber >= 0 && dailyNumber <= 9999)) {
                vehicle.setDailyNumber(null);
            }

            // 运输证提前提醒天数
            Integer managementRemindDays = vehicle.getManagementRemindDays();
            if (!(Objects.nonNull(managementRemindDays) && managementRemindDays >= 0 && managementRemindDays <= 9999)) {
                vehicle.setManagementRemindDays(null);
            }

            // 营运状态
            Integer operatingState = getIndex(Vehicle.OPERATING_STATES, vehicle.getOperatingStateStr());
            vehicle.setOperatingState(operatingState == null ? 0 : operatingState);

            //行驶证号
            vehicle.setLicenseNo(checkLength(vehicle.getLicenseNo(), 20, true));
            //机架号
            vehicle.setChassisNumber(checkLength(vehicle.getChassisNumber(), 50, true));
            //发动机号
            vehicle.setEngineNumber(checkLength(vehicle.getEngineNumber(), 20, true));
            //使用性质
            vehicle.setUsingNature(checkLength(vehicle.getUsingNature(), 20, true));
            //品牌型号
            vehicle.setBrandModel(checkLength(vehicle.getBrandModel(), 20, true));

            //行驶证提前提醒天数
            Integer remindDay = vehicle.getRegistrationRemindDays();
            if (remindDay == null || remindDay < 0 || remindDay > 9999) {
                vehicle.setRegistrationRemindDays(null);
            }
            // 保养里程数(km) 0-6位整数
            Integer maintainMileage = vehicle.getMaintainMileage();
            if (maintainMileage == null || maintainMileage < 0 || maintainMileage > 999999) {
                vehicle.setMaintainMileage(null);
            }

            //补全日期相关字段的信息
            completeDateField(vehicle);
        }
    }

    /**
     * 补全时间字段信息
     * @param vehicle vehicle
     */
    private void completeDateField(VehicleDTO vehicle) {
        String dateStr = vehicle.getVehicleTechnologyValidityStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setVehicleTechnologyValidity(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }

        dateStr = vehicle.getRoadTransportValidityStartStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setRoadTransportValidityStart(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }

        dateStr = vehicle.getRoadTransportValidityStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setRoadTransportValidity(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }

        dateStr = vehicle.getRegistrationStartDateStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setRegistrationStartDate(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }

        dateStr = vehicle.getRegistrationEndDateStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setRegistrationEndDate(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }

        dateStr = vehicle.getLicenseIssuanceDateStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setLicenseIssuanceDate(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }

        dateStr = vehicle.getRegistrationDateStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setRegistrationDate(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }
        dateStr = vehicle.getMaintainValidityStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setMaintainValidity(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }
        dateStr = vehicle.getVehiclePlatformInstallDateStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setVehiclePlatformInstallDate(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }
        dateStr = vehicle.getMachineAgeStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setMachineAge(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM));
        }

        dateStr = vehicle.getVehicleProductionDateStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setVehicleProductionDate(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }
        dateStr = vehicle.getFirstOnlineTimeStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setFirstOnlineTime(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD_HH_MM_SS));
        }

        dateStr = vehicle.getValidEndDateStr();
        if (StringUtils.isNotBlank(dateStr)) {
            vehicle.setValidEndDate(DateUtil.getStringToDate(dateStr, DateFormatKey.YYYY_MM_DD));
        }
    }

    private String getBusinessScopeId(String businessScopeNames, Map<String, String> businessScopeMap) {
        if (StringUtils.isBlank(businessScopeNames)) {
            return businessScopeMap.get("道路旅客运输");
        }
        String[] businessScopeNameArr = businessScopeNames.split(",");
        List<String> ids = new ArrayList<>();
        for (String businessScopeName : businessScopeNameArr) {
            if (businessScopeMap.containsKey(businessScopeName)) {
                ids.add(businessScopeMap.get(businessScopeName));
            }
        }
        return ids.isEmpty() ? businessScopeMap.get("道路旅客运输") : StringUtils.join(ids, ",");
    }

    private Integer getIndex(String[] array, String matchValue) {
        if (StringUtils.isBlank(matchValue)) {
            return null;
        }
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(matchValue, array[i])) {
                return i;
            }
        }
        return null;
    }

    private static String checkLength(String value, int length, boolean isNullAble) {
        if (Converter.toBlank(value).length() > length) {
            return isNullAble ? null : "";
        } else {
            return value;
        }
    }

    /**
     * 参数的静态检查、主要是长度和正则的检查
     */
    private void validate() {
        final List<VehicleDO> vehicles = vehicleDao.findAll();
        Set<String> brandSet = vehicles.stream().map(VehicleDO::getBrand).collect(Collectors.toSet());
        checkVehicleType();
        for (VehicleDTO vehicle : importList) {
            if (StringUtils.isNotBlank(vehicle.getErrorMsg())) {
                continue;
            }
            // 车牌号规则验证
            String brand = Converter.toBlank(vehicle.getName());
            if (!RegexUtils.checkPlateNumber(Converter.toBlank(brand))) {
                if (brand != null && brand.length() <= 20 && brand.length() >= 2) {
                    vehicle.setErrorMsg("车牌号只能由字母、数字、中文和短横杠组成");
                } else {
                    vehicle.setErrorMsg("车牌号长度2-20位");
                }
                continue;
            }

            //判断车牌号是否已经存在
            if (brandSet.contains(brand)) {
                vehicle.setErrorMsg("【车牌号】" + brand + "已存在");
                continue;
            }

            if (Objects.equals(standard, Vehicle.Standard.ENGINEERING)) {
                if (!checkEngineeringParam(vehicle)) {
                    continue;
                }
            }

            if (Objects.equals(standard, Vehicle.Standard.FREIGHT_TRANSPORT)) {
                checkFreightParam(vehicle);
            }

        }
    }

    private void checkFreightParam(VehicleDTO vehicle) {
        vehicle.setVehicleBrand(checkLength(vehicle.getVehicleBrand(), 20, true));
        //车辆型号
        vehicle.setVehicleModel(checkLength(vehicle.getVehicleModel(), 20, true));

        //车辆购置方式
        vehicle.setPurchaseWay(getIndex(Vehicle.PURCHASE_WAY, vehicle.getPurchaseWayStr()));
        //执照上传数
        vehicle.setLicenseNumbers(checkLimit(vehicle.getLicenseNumbers(), 99));

        //总质量(kg)
        String totalQuality = vehicle.getTotalQuality();
        if (StringUtils.isNotBlank(totalQuality) && !Pattern.matches(RegexKey.DOUBLE_REGEX_10_1, totalQuality)) {
            vehicle.setTotalQuality(null);
        }

        //准牵引总质量
        String totalMass = vehicle.getTractionTotalMass();
        if (StringUtils.isNotBlank(totalMass) && !Pattern.matches(RegexKey.DOUBLE_REGEX_10_1, totalMass)) {
            vehicle.setTractionTotalMass(null);
        }

        //外廓尺寸-长(mm)
        vehicle.setProfileSizeLong(checkLimit(vehicle.getProfileSizeLong(), 999999));
        //外廓尺寸-宽(mm)
        vehicle.setProfileSizeWide(checkLimit(vehicle.getProfileSizeWide(), 999999));
        //外廓尺寸-高(mm)
        vehicle.setProfileSizeHigh(checkLimit(vehicle.getProfileSizeHigh(), 999999));
        //货厢内部尺寸-长(mm)
        vehicle.setInternalSizeLong(checkLimit(vehicle.getInternalSizeLong(), 999999));
        //货厢内部尺寸-宽(mm)
        vehicle.setInternalSizeWide(checkLimit(vehicle.getInternalSizeWide(), 999999));
        //货厢内部尺寸-高(mm)
        vehicle.setInternalSizeHigh(checkLimit(vehicle.getInternalSizeHigh(), 999999));
        //轴数
        vehicle.setShaftNumber(checkLimit(vehicle.getShaftNumber(), 9999));
        //轮胎数
        vehicle.setTiresNumber(checkLimit(vehicle.getTiresNumber(), 9999));
        //轮胎规格
        vehicle.setTireSize(checkLength(vehicle.getTireSize(), 20, true));
    }

    private Integer checkLimit(Integer value, int max) {
        if (value == null || value < 0 || value > max) {
            return null;
        } else {
            return value;
        }
    }

    private boolean checkEngineeringParam(VehicleDTO vehicle) {
        String vehicleOwnerName = Converter.toBlank(vehicle.getVehicleOwnerName());
        if (!Pattern.matches(RegexKey.VEHICLE_OWNER_REGEX, vehicleOwnerName)) {
            vehicle.setErrorMsg("【车主姓名】只能填写中文、字母,长度不能超过8");
            return false;
        }

        String ownerPhoneOne = Converter.toBlank(vehicle.getOwnerPhoneOne());
        if (!RegexUtils.checkOwnerPhone(ownerPhoneOne)) {
            vehicle.setErrorMsg("【车主手机1】长度错误, 整数7-13");
            return false;
        }

        String ownerPhoneTwo = vehicle.getOwnerPhoneTwo();
        if (StringUtils.isNotBlank(ownerPhoneTwo) && !RegexUtils.checkOwnerPhone(ownerPhoneTwo)) {
            vehicle.setOwnerPhoneTwo("");
        }

        String ownerPhoneThree = vehicle.getOwnerPhoneThree();
        if (StringUtils.isNotBlank(ownerPhoneThree) && !RegexUtils.checkOwnerPhone(ownerPhoneThree)) {
            vehicle.setOwnerPhoneThree("");
        }

        String landLine = vehicle.getOwnerLandline();
        if (StringUtils.isNotBlank(landLine) && !RegexUtils.checkLandline(landLine)) {
            vehicle.setOwnerLandline("");
        }

        // 自重(T) 需要判断小数(如果车辆类别是工程车辆，且车辆子类型对应的行驶方式是运输，则自重必填)
        Double selfRespect = vehicle.getSelfRespect();
        if (Objects.nonNull(selfRespect)) {
            if (selfRespect < 0 || selfRespect > 9999.9) {
                vehicle.setErrorMsg("【自重】范围0~9999.9");
                return false;
            }
        }

        VehicleSubTypeDTO subType = cacheManger.getVehicleSubType(vehicle.getId());
        if (Objects.equals("工程车辆", vehicle.getVehicleCategoryName()) && subType != null && Objects
            .equals(subType.getDrivingWay(), 1) && Objects.isNull(selfRespect)) {
            vehicle.setErrorMsg("类别为工程车辆，行驶方式为运输时，【自重】必填");
            return false;
        }

        // 工作能力(T)(如果车辆类型是拖车，则工作能力必填)
        Double abilityWork = vehicle.getAbilityWork();
        if (Objects.nonNull(abilityWork)) {
            if (abilityWork < 0 || abilityWork > 9999.9) {
                vehicle.setErrorMsg("【工作能力】范围0~9999.9");
                return false;
            }
        }
        if (Objects.equals("拖车", vehicle.getVehicleType()) && Objects.isNull(abilityWork)) {
            vehicle.setErrorMsg("类别为运输车辆，车辆类型是拖车时,【工作能力】必填");
            return false;
        }
        // 工作半径(m)
        Double workingRadius = vehicle.getWorkingRadius();
        if (Objects.isNull(workingRadius) || workingRadius < 0 || workingRadius > 999.9) {
            vehicle.setWorkingRadius(null);
        }

        Double initialMileage = vehicle.getInitialMileage();
        if (Objects.isNull(initialMileage) || initialMileage < 0 || initialMileage > 9999999.9) {
            vehicle.setInitialMileage(null);
        }
        // 初始工时(h)
        Double initialWorkHours = vehicle.getInitialWorkHours();
        if (Objects.isNull(initialWorkHours) || initialWorkHours < 0 || initialWorkHours > 9999999.9) {
            vehicle.setInitialWorkHours(null);
        }

        return true;
    }

}
