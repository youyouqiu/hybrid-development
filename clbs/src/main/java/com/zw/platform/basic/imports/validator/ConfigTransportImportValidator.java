package com.zw.platform.basic.imports.validator;

import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.dto.imports.ConfigTransportImportDTO;
import com.zw.platform.basic.imports.ConfigTransportHolder;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.excel.validator.ImportValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 信息配置货运导入--参数校验
 * @author zhangjuan
 */
public class ConfigTransportImportValidator extends ImportValidator<ConfigTransportImportDTO> {
    private final VehicleService vehicleService;
    private final SimCardNewDao simCardNewDao;
    private final DeviceNewDao deviceNewDao;
    private ConfigTransportHolder holder;
    private static final String VEHICLE_OWNER_REGEX = "^[A-Za-z\\u4e00-\\u9fa5]{1,8}$";
    private static final String DOUBLE_REGEX = "^(?:0\\.[1-9]|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\\.[1-9])$";
    private static final Pattern vehicleDeviceChecker = Pattern.compile("^[0-9a-zA-Z]{7,30}$");

    public ConfigTransportImportValidator(ConfigTransportHolder holder, VehicleService vehicleService,
        SimCardNewDao simCardNewDao, DeviceNewDao deviceNewDao) {
        this.holder = holder;
        this.vehicleService = vehicleService;
        this.simCardNewDao = simCardNewDao;
        this.deviceNewDao = deviceNewDao;
    }

    @Override
    public JsonResultBean validate(List<ConfigTransportImportDTO> configList, boolean isCheckGroupName,
        List<OrganizationLdap> orgList) {
        prepareData(configList);
        Map<String, OrganizationLdap> orgNameMap =
            orgList.stream().collect(Collectors.toMap(OrganizationLdap::getName, Function.identity(), (o, n) -> n));
        holder.setOrgNameMap(orgNameMap);
        holder.setOrgIdMap(AssembleUtil.collectionToMap(orgList, OrganizationLdap::getUuid));
        for (int i = 0; i < configList.size(); i++) {
            ConfigTransportImportDTO importDTO = configList.get(i);
            if (StringUtils.isNotBlank(importDTO.getErrorMsg())) {
                continue;
            }
            checkData(i, importDTO);
        }
        String invalidMessage = getInvalidInfo();
        boolean isSuccess = StringUtils.isBlank(invalidMessage);
        invalidMessage = isSuccess ? "" : "导入文件有以下错误，请修复后重新导入：<br/>" + invalidMessage;
        return new JsonResultBean(isSuccess, invalidMessage);
    }

    private void checkData(Integer index, ConfigTransportImportDTO importDTO) {
        if (!checkDataRight(index, importDTO)) {
            return;
        }

        //校验企业
        if (importDTO.getOrgId() == null && !holder.getOrgNameMap().containsKey(importDTO.getOrgName())) {
            addErrorMsg(index, importDTO, "【企业：" + importDTO.getOrgName() + "】不存在");
            return;
        }

        if (importDTO.getOrgId() == null) {
            String orgId = holder.getOrgNameMap().get(importDTO.getOrgName()).getUuid();
            importDTO.setOrgId(orgId);
        }
        holder.addOrgId(importDTO.getOrgId());
        if (!checkMonitorData(index, importDTO)) {
            return;
        }

        //校验终端数据
        if (!vehicleDeviceChecker.matcher(importDTO.getDeviceNumber()).matches()) {
            addErrorMsg(index, importDTO, "监控对象终端号只能输入7-30位数字字母");
            return;
        }

        //校验终端手机号
        if (!RegexUtils.checkSIM(Converter.toBlank(importDTO.getSimCardNumber()))) {
            addErrorMsg(index, importDTO, "【终端手机号：" + importDTO.getSimCardNumber() + "】数据不规范");
        }
    }

    private boolean checkMonitorData(Integer index, ConfigTransportImportDTO config) {
        boolean hasError = false;
        if (!RegexUtils.checkPlateNumber(config.getBrand())) {
            addErrorMsg(index, config, "【监控对象：" + config.getBrand() + "】车牌号错误,请输入汉字、字母、数字或短横杠，长度2-20位");
            hasError = true;
        }
        if (StringUtils.isNotBlank(config.getChassisNumber()) && config.getChassisNumber().length() > 50) {
            addErrorMsg(index, config, "机架号长度不能超过50");
            hasError = true;
        }

        String vehicleTypeName = config.getVehicleTypeName();
        if (StringUtils.isNotBlank(vehicleTypeName) && vehicleTypeName.length() > 20) {
            addErrorMsg(index, config, "车辆类型不能超过20个字符");
            hasError = true;
        }

        if (StringUtils.isNotBlank(config.getProvince()) && config.getProvince().length() > 20) {
            addErrorMsg(index, config, "所属省不能超过20个字符");
            hasError = true;
        }

        if (StringUtils.isNotBlank(config.getCity()) && config.getCity().length() > 20) {
            addErrorMsg(index, config, "所属市不能超过20个字符");
            hasError = true;
        }
        if (!holder.getTypeNameMap().containsKey(vehicleTypeName)) {
            addErrorMsg(index, config, "车辆类型：" + vehicleTypeName + "】不存在");
            hasError = true;
        }
        /*非必填: 如果参数不合法 则置空*/
        if (StringUtils.isNotBlank(config.getManuFacturer()) && config.getManuFacturer().length() > 50) {
            config.setManuFacturer("");
        }

        if (StringUtils.isNotBlank(config.getDeviceName()) && config.getDeviceName().length() > 50) {
            config.setDeviceName("");
        }
        if (StringUtils.isNotBlank(config.getRoadTransportNumber()) && config.getRoadTransportNumber().length() > 20) {
            config.setRoadTransportNumber("");
        }

        String vehicleOwner = config.getVehicleOwner();
        if (StringUtils.isNotBlank(vehicleOwner) && !Pattern.matches(VEHICLE_OWNER_REGEX, vehicleOwner)) {
            config.setVehicleOwner("");
        }

        String vehicleOwnerPhone = config.getVehicleOwnerPhone();
        if (StringUtils.isNotBlank(vehicleOwnerPhone) && !RegexUtils.checkOwnerPhone(vehicleOwnerPhone)) {
            config.setVehicleOwnerPhone("");
        }
        if (StringUtils.isNotBlank(config.getVehicleBrand()) && config.getVehicleBrand().length() > 20) {
            config.setVehicleBrand(null);
        }

        if (StringUtils.isNotBlank(config.getVehicleModel()) && config.getVehicleModel().length() > 20) {
            config.setVehicleModel(null);
        }

        String totalQuality = config.getTotalQuality();
        if (!Pattern.matches(DOUBLE_REGEX, Converter.toBlank(totalQuality))) {
            config.setTotalQuality(null);
        }

        //核定载质量(kg)
        String loadingQuality = config.getLoadingQuality();
        if ((StringUtils.isNotBlank(loadingQuality)) && !Pattern.matches(DOUBLE_REGEX, loadingQuality)) {
            config.setLoadingQuality(null);
        }
        //准牵引总质量
        String tractionTotalMass = config.getTractionTotalMass();
        if (!Pattern.matches(DOUBLE_REGEX, Converter.toBlank(tractionTotalMass))) {
            config.setTractionTotalMass(null);
        }
        //外廓尺寸-长(mm)
        Integer profileSizeLong = config.getProfileSizeLong();
        if (profileSizeLong != null) {
            if (profileSizeLong < 0 || profileSizeLong > 999999) {
                config.setProfileSizeLong(null);
            }
        }
        //外廓尺寸-宽(mm)
        Integer profileSizeWide = config.getProfileSizeWide();
        if (profileSizeWide != null) {
            if (profileSizeWide < 0 || profileSizeWide > 999999) {
                config.setProfileSizeWide(null);
            }

        }
        //外廓尺寸-高(mm)
        Integer profileSizeHigh = config.getProfileSizeHigh();
        if (profileSizeHigh != null) {
            if (profileSizeHigh < 0 || profileSizeHigh > 999999) {
                config.setProfileSizeHigh(null);
            }
        }
        //货厢内部尺寸-长(mm)
        Integer internalSizeLong = config.getInternalSizeLong();
        if (internalSizeLong != null) {
            if (internalSizeLong < 0 || internalSizeLong > 999999) {
                config.setInternalSizeLong(null);
            }
        }
        //货厢内部尺寸-宽(mm)
        Integer internalSizeWide = config.getInternalSizeWide();
        if (internalSizeWide != null) {
            if (internalSizeWide < 0 || internalSizeWide > 999999) {
                config.setInternalSizeWide(null);
            }
        }
        //货厢内部尺寸-高(mm)
        Integer internalSizeHigh = config.getInternalSizeHigh();
        if (internalSizeHigh != null) {
            if (internalSizeHigh < 0 || internalSizeHigh > 999999) {
                config.setInternalSizeHigh(null);
            }
        }

        //轴数
        Integer shaftNumber = config.getShaftNumber();
        if (shaftNumber != null) {
            if (shaftNumber < 0 || shaftNumber > 9999) {
                config.setShaftNumber(null);
            }
        }
        //轮胎数
        Integer tiresNumber = config.getTiresNumber();
        if (tiresNumber != null) {
            if (tiresNumber < 0 || tiresNumber > 9999) {
                config.setTiresNumber(null);
            }
        }
        //轮胎规格
        String tireSize = config.getTireSize();
        if (StringUtils.isNotBlank(tireSize)) {
            if (tireSize.length() > 20) {
                config.setTireSize(null);
            }
        }
        if (StringUtils.trimToEmpty(config.getScopeBusiness()).length() > 20) {
            config.setScopeBusiness("");
        }
        // 车辆营运证号
        if (StringUtils.trimToEmpty(config.getVehicleOperationNumber()).length() > 20) {
            config.setVehicleOperationNumber("");
        }

        Integer licenseNumbers = config.getLicenseNumbers();
        if (Objects.nonNull(licenseNumbers) && (licenseNumbers < 0 || licenseNumbers > 99)) {
            config.setLicenseNumbers(null);
        }

        return !hasError;
    }

    private boolean checkDataRight(Integer index, ConfigTransportImportDTO importDTO) {
        MonitorBaseDTO monitor = holder.getExistVehicleMap().get(importDTO.getBrand());
        Set<String> orgIds = new HashSet<>();
        //校验监控对象
        if (Objects.nonNull(monitor)) {
            if (!holder.getOrgIdMap().containsKey(monitor.getOrgId())) {
                addErrorMsg(index, importDTO, "【监控对象：" + monitor.getName() + "】已存在,且当前用户无操作权限");
                return false;
            }
            if (Objects.equals(monitor.getBindType(), Vehicle.BindType.HAS_BIND)) {
                addErrorMsg(index, importDTO, "【监控对象：" + monitor.getName() + "】已经存在绑定关系");
                return false;
            }
            orgIds.add(monitor.getOrgId());
        }

        DeviceDTO deviceDTO = holder.getExistDeviceMap().get(importDTO.getDeviceNumber());
        if (Objects.nonNull(deviceDTO)) {
            if (!holder.getOrgIdMap().containsKey(deviceDTO.getOrgId())) {
                addErrorMsg(index, importDTO, "【终端号：" + importDTO.getDeviceNumber() + "】已经存且当前用户无操作权限");
                return false;
            }
            if (StringUtils.isNotBlank(deviceDTO.getBindId())) {
                addErrorMsg(index, importDTO, "【终端号：" + importDTO.getDeviceNumber() + "】已经存在绑定关系");
                return false;
            }
            orgIds.add(deviceDTO.getOrgId());
        }

        SimCardDTO simCardDTO = holder.getExistSimMap().get(importDTO.getSimCardNumber());
        if (Objects.nonNull(simCardDTO)) {
            if (!holder.getOrgIdMap().containsKey(simCardDTO.getOrgId())) {
                addErrorMsg(index, importDTO, "【终端手机号：" + importDTO.getDeviceNumber() + "】已经存且当前用户无操作权限");
                return false;
            }
            if (StringUtils.isNotBlank(simCardDTO.getConfigId())) {
                addErrorMsg(index, importDTO, "【终端手机号：" + importDTO.getSimCardNumber() + "】已经存在绑定关系");
                return false;
            }
            orgIds.add(simCardDTO.getOrgId());
        }

        if (orgIds.size() > 1) {
            addErrorMsg(index, importDTO, "【监控对象：" + importDTO.getBrand() + "】存在多个企业");
            return false;
        }
        if (orgIds.size() == 1) {
            importDTO.setOrgId(new ArrayList<>(orgIds).get(0));
            importDTO.setOrgName(holder.getOrgIdMap().get(importDTO.getOrgId()).getName());
        }
        return true;
    }

    private void addErrorMsg(Integer index, ConfigTransportImportDTO importDTO, String errorMsg) {
        this.recordInvalidInfo(String.format("第%d条数据%s<br/>", index + 1, errorMsg));
        importDTO.setErrorMsg(errorMsg);
    }

    private void prepareData(List<ConfigTransportImportDTO> importList) {
        Set<String> brands = new HashSet<>();
        Set<String> deviceNums = new HashSet<>();
        Set<String> simCardNums = new HashSet<>();
        if (importList.size() <= 1000) {
            importList.forEach(importDTO -> {
                brands.add(importDTO.getBrand());
                deviceNums.add(importDTO.getDeviceNumber());
                simCardNums.add(importDTO.getSimCardNumber());
            });
        }

        //获取已经存在的车辆信息
        List<MonitorBaseDTO> vehicleList = vehicleService.getByNames(brands.isEmpty() ? null : brands);
        holder.setExistVehicleMap(AssembleUtil.collectionToMap(vehicleList, MonitorBaseDTO::getName));
        //获取已经存在的SIM卡信息
        List<SimCardDTO> simCardList = simCardNewDao.getByNumbers(simCardNums.isEmpty() ? null : simCardNums);
        holder.setExistSimMap(AssembleUtil.collectionToMap(simCardList, SimCardDTO::getSimcardNumber));
        //获取已经存在的终端信息
        List<DeviceDTO> deviceList = deviceNewDao.getByNumbers(deviceNums.isEmpty() ? null : deviceNums);
        holder.setExistDeviceMap(AssembleUtil.collectionToMap(deviceList, DeviceDTO::getDeviceNumber));
        //车辆类型名称和车辆类型的map
        Set<String> categoryIds =
            TypeCacheManger.getInstance().getVehicleCategories(Vehicle.Standard.FREIGHT_TRANSPORT).stream()
                .map(VehicleCategoryDTO::getId).collect(Collectors.toSet());
        List<VehicleTypeDTO> typeList = TypeCacheManger.getInstance().getVehicleTypes(categoryIds);
        holder.setTypeNameMap(AssembleUtil.collectionToMap(typeList, VehicleTypeDTO::getType));
    }

}
