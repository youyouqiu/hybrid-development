package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.MessageConfig;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.BusinessScopeDO;
import com.zw.platform.basic.domain.CargoGroupVehicleDO;
import com.zw.platform.basic.domain.FuelTypeDO;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.AdministrativeDivisionDTO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.dto.export.VehicleExportDTO;
import com.zw.platform.basic.dto.imports.VehicleImportDTO;
import com.zw.platform.basic.dto.imports.VehicleImportEngineeringDTO;
import com.zw.platform.basic.dto.imports.VehicleImportFreightDTO;
import com.zw.platform.basic.dto.query.VehiclePageQuery;
import com.zw.platform.basic.event.VehicleDeleteEvent;
import com.zw.platform.basic.event.VehicleUpdateEvent;
import com.zw.platform.basic.imports.handler.VehicleImportHandler;
import com.zw.platform.basic.repository.AdministrativeDivisionDao;
import com.zw.platform.basic.repository.FuelTypeDao;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.BusinessScopeService;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.CargoGroupVehicleLogService;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleCategoryService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.enums.VehicleColor;
import com.zw.platform.domain.basicinfo.form.BrandForm;
import com.zw.platform.domain.basicinfo.form.BrandModelsForm;
import com.zw.platform.domain.basicinfo.query.VehicleQuery;
import com.zw.platform.domain.core.Operations;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.core.OperationDao;
import com.zw.platform.repository.modules.BrandDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.CommonTypeUtils;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.excel.validator.ImportValidator;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.ImportErrorData;
import com.zw.platform.util.imports.ZwImportException;
import com.zw.platform.util.imports.lock.ImportLock;
import com.zw.platform.util.imports.lock.ImportModule;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 车辆管理实现类
 * @author zhangjuan
 * @date 2020/9/25
 */
@Service("vehicleService")
@Order(1)
public class VehicleServiceImpl implements VehicleService, CacheService, IpAddressService {
    private static final Logger log = LogManager.getLogger(VehicleServiceImpl.class);
    private static final RedisKey FUZZY_KEY = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();
    private static final RedisKeyEnum UNBIND_KEY = RedisKeyEnum.ORG_UNBIND_VEHICLE;
    /**
     * 查询全部类型
     */
    private static final Integer QUERY_ALL = 0;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMonitorService groupMonitorService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private AdministrativeDivisionDao divisionDao;

    @Autowired
    private CargoGroupVehicleLogService cargoGroupVehicleLogService;

    @Autowired
    private BusinessScopeService businessScopeService;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @Autowired
    private MessageConfig messageConfig;

    @Autowired
    private LogSearchService logService;

    @Autowired
    private VehicleCategoryService vehicleCategoryService;

    @Autowired
    private FuelTypeDao fuelTypeDao;

    /**
     * todo 暂时先使用原来的DAO类
     */
    @Autowired
    private OperationDao operationDao;
    @Autowired
    private BrandDao brandDao;

    @Autowired
    private FastDFSClient fastDFSClient;

    @MethodLog(name = "添加", description = "车辆添加")
    @Override
    public boolean add(VehicleDTO vehicleDTO) {
        //检查车辆编号是否重复
        if (isExistNumber(null, vehicleDTO.getName())) {
            return false;
        }
        vehicleDTO.setId(UUID.randomUUID().toString());

        // 获取车辆类别
        completeVehicleType(vehicleDTO);

        //封装行政区划
        setAdministrativeDivision(vehicleDTO);

        //设置默认运营类别，若运营类别为空 获取一个运营类别作为默认运营类别
        setDefaultPurpose(vehicleDTO);

        //上传图片处理
        dealPhoto(vehicleDTO);

        VehicleDO vehicleDO = VehicleDO.build(vehicleDTO, true);
        vehicleDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean isSuccess = newVehicleDao.insert(vehicleDO);
        if (!isSuccess) {
            return false;
        }
        if (configHelper.isCargoReportSwitch()) {
            cargoGroupVehicleLogService.add(vehicleDTO);
        }

        String id = vehicleDO.getId();
        //维护redis缓存 维护顺序和监控对象信息缓存
        RedisHelper.addToListTop(RedisKeyEnum.VEHICLE_SORT_LIST.of(), id);
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(id), buildRedisInfo(vehicleDTO));

        //检查监控对象是否绑定,未绑定维护企业下未绑定缓存
        if (Objects.equals(Vehicle.BindType.UNBIND, vehicleDTO.getBindType())) {
            RedisHelper.addToHash(UNBIND_KEY.of(vehicleDTO.getOrgId()), id, bindUnbindValue(vehicleDTO));
        }

        //维护模糊查询缓存
        RedisHelper.addToHash(FUZZY_KEY, buildFuzzyField(vehicleDTO), buildFuzzyValue(vehicleDTO));

        //维护企业下每月企业下新增车辆数的redis缓存
        Map<String, Integer> orgNumMap = ImmutableMap.of(vehicleDTO.getOrgId(), 1);
        addMonthAddNumRedis(orgNumMap);

        //添加经营范围
        addBusinessScope(vehicleDTO.getScopeBusinessIds(), id);
        // 添加日志
        String msg = String.format("新增车辆：%s( @%s )", vehicleDTO.getName(), vehicleDTO.getOrgName());
        String plateColor = vehicleDTO.getPlateColor().toString();
        logService.addLog(getIpAddress(), msg, "3", "", vehicleDTO.getName(), plateColor);
        return true;
    }

    private void completeVehicleType(VehicleDTO vehicleDTO) {
        if (StringUtils.isNotBlank(vehicleDTO.getVehicleSubTypeId())) {
            VehicleSubTypeDTO subType = cacheManger.getVehicleSubType(vehicleDTO.getVehicleSubTypeId());
            if (Objects.isNull(subType)) {
                return;
            }
            vehicleDTO.setVehicleSubType(subType.getSubType());
            vehicleDTO.setVehicleType(subType.getTypeId());
            vehicleDTO.setVehicleTypeName(subType.getType());
            vehicleDTO.setVehicleCategoryId(subType.getCategoryId());
            vehicleDTO.setVehicleCategoryName(subType.getCategory());
            VehicleCategoryDTO vehicleCategory = cacheManger.getVehicleCategory(subType.getCategoryId());
            vehicleDTO.setStandard(vehicleCategory.getStandard());
        } else {
            VehicleTypeDTO vehicleType = cacheManger.getVehicleType(vehicleDTO.getVehicleType());
            if (Objects.nonNull(vehicleType)) {
                vehicleDTO.setVehicleTypeName(vehicleType.getType());
                vehicleDTO.setVehicleCategoryId(vehicleType.getCategoryId());
                vehicleDTO.setVehicleCategoryName(vehicleType.getCategory());
                VehicleCategoryDTO vehicleCategory = cacheManger.getVehicleCategory(vehicleType.getCategoryId());
                vehicleDTO.setVehicleIconName(vehicleCategory.getIconName());
                vehicleDTO.setStandard(vehicleCategory.getStandard());
            }
        }

    }

    private void addBusinessScope(String businessIds, String vehicleId) {
        if (StringUtils.isNotBlank(businessIds)) {
            List<BusinessScopeDO> businessScopes = new ArrayList<>();
            for (String businessId : businessIds.split(",")) {
                BusinessScopeDO businessScopeDO = new BusinessScopeDO(vehicleId, businessId, "2");
                businessScopes.add(businessScopeDO);
            }
            businessScopeService.addBusinessScope(businessScopes);
        }
    }

    private void setDefaultPurpose(VehicleDTO vehicle) {
        if (StringUtils.isNotBlank(vehicle.getVehiclePurpose())) {
            return;
        }
        Collection<VehiclePurposeDTO> purposes = cacheManger.getVehiclePurposes();
        if (purposes.isEmpty()) {
            return;
        }
        Iterator<VehiclePurposeDTO> iterator = purposes.iterator();
        if (iterator.hasNext()) {
            VehiclePurposeDTO purpose = iterator.next();
            vehicle.setVehiclePurpose(purpose.getId());
            vehicle.setVehiclePurposeName(purpose.getPurposeCategory());
        }
    }

    @MethodLog(name = "修改车辆", description = "修改车辆")
    @Override
    public boolean update(VehicleDTO vehicleDTO) {
        String id = vehicleDTO.getId();
        VehicleDTO oldVehicle = newVehicleDao.getDetailById(id);
        if (Objects.isNull(oldVehicle)) {
            return false;
        }
        //检查车辆编号是否重复
        if (isExistNumber(id, vehicleDTO.getName())) {
            return false;
        }
        //上传图片处理
        dealPhoto(vehicleDTO);
        //封装行政区划
        setAdministrativeDivision(vehicleDTO);
        //设置默认运营类别
        setDefaultPurpose(vehicleDTO);

        completeVehicleType(vehicleDTO);

        //判断类别对应的标准是否发生改变，改变原来的专属字段值
        standardChange(vehicleDTO, oldVehicle);

        VehicleDO vehicleDO = VehicleDO.build(vehicleDTO, true);
        vehicleDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean success = newVehicleDao.update(vehicleDO);
        if (!success) {
            return false;
        }

        //经营范围若发生改变，同步维护车辆的经营范围
        businessScopeService.deleteById(id);
        addBusinessScope(vehicleDTO.getScopeBusinessIds(), id);

        //更新监控对象信息缓存
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(id), updateMonitorInfo(vehicleDTO));

        //未绑定，同步更新组织下未绑定车辆
        boolean unBind = Objects.equals(oldVehicle.getBindType(), Vehicle.BindType.UNBIND);
        vehicleDTO.setBindType(oldVehicle.getBindType());
        if (unBind) {
            RedisHelper.addToHash(UNBIND_KEY.of(vehicleDTO.getOrgId()), id, bindUnbindValue(vehicleDTO));
        }

        //组织发生改变，同步删除原来组织下的未绑定关系
        boolean orgIsChange = !Objects.equals(oldVehicle.getOrgId(), vehicleDTO.getOrgId());
        if (orgIsChange && unBind) {
            RedisHelper.hdel(UNBIND_KEY.of(oldVehicle.getOrgId()), id);
        }

        //若车牌号发生改变，同步维护模糊查询
        VehicleDTO curVehicle = getById(id);
        if (!Objects.equals(oldVehicle.getName(), curVehicle.getName())) {
            RedisHelper.hdel(FUZZY_KEY, buildFuzzyField(oldVehicle));
            RedisHelper.addToHash(FUZZY_KEY, buildFuzzyField(curVehicle), buildFuzzyValue(curVehicle));
        }

        // _monitorScore 缓存处理 保留原有逻辑
        if (!orgIsChange || Objects.equals(vehicleDTO.getVehiclePurpose(), oldVehicle.getVehiclePurpose())) {
            RedisHelper.delByPattern(HistoryRedisKeyEnum.ORG_MONITOR_SCORE_PATTERN.of(oldVehicle.getOrgId()));
        }

        //车辆更新的时候，插入一条货运车辆报表需要的更新记录
        if (configHelper.isCargoReportSwitch()) {
            cargoGroupVehicleLogService.update(curVehicle, oldVehicle);
        }
        //通知storm
        ZMQFencePub.pubChangeFence("1," + id);
        if (StringUtils.isNotBlank(oldVehicle.getDeviceNumber()) && !oldVehicle.getOrgId()
            .equals(curVehicle.getOrgId())) {
            String builder = "16,1," + oldVehicle.getOrgId() + "," + curVehicle.getOrgId() + "," + id;
            ZMQFencePub.pubChangeFence(builder);
        }
        //推送车辆修改事件
        VehicleUpdateEvent updateEvent =
            new VehicleUpdateEvent(this, Collections.singletonList(curVehicle), Collections.singletonList(oldVehicle));
        publisher.publishEvent(updateEvent);

        final boolean plateNumberChanged = !Objects.equals(oldVehicle.getName(), vehicleDTO.getName());
        // 同步协议下监控对象摘要信息缓存
        if (plateNumberChanged) {
            final String deviceType = curVehicle.getDeviceType();
            if (StringUtils.isNotEmpty(deviceType)) {
                final RedisKey protocolKey = RedisKeyEnum.MONITOR_PROTOCOL.of(deviceType);
                RedisHelper.addToHash(protocolKey, id, vehicleDTO.getName());
            }
        }

        //记录日志
        final String msg;
        if (plateNumberChanged) {
            boolean orgNotChange = Objects.equals(oldVehicle.getOrgId(), vehicleDTO.getOrgId());
            String orgName =
                orgNotChange ? vehicleDTO.getOrgName() : organizationService.getOrgNameByUuid(oldVehicle.getOrgId());
            msg = "修改车辆：" + oldVehicle.getName() + "( @" + orgName + " )" + "修改为：" + vehicleDTO.getName() + "( @"
                + vehicleDTO.getOrgName() + " )";
        } else {
            msg = "修改车辆：" + vehicleDTO.getName() + "( @" + vehicleDTO.getOrgName() + " )";
        }
        String plateColor =
            Objects.isNull(oldVehicle.getPlateColor()) ? "" : String.valueOf(oldVehicle.getPlateColor());
        logService.addLog(getIpAddress(), msg, "3", "", oldVehicle.getName(), plateColor);
        return true;
    }

    private void dealPhoto(VehicleDTO vehicleDTO) {
        String webServerUrl = fdfsWebServer.getWebServerUrl();
        String vehiclePhoto = vehicleDTO.getVehiclePhoto();
        if (StringUtils.isNotBlank(vehiclePhoto) && vehiclePhoto.contains(webServerUrl)) {
            vehicleDTO.setVehiclePhoto(vehiclePhoto.split(webServerUrl)[1]);
        }

        String transportNumberPhoto = vehicleDTO.getTransportNumberPhoto();
        if (StringUtils.isNotBlank(transportNumberPhoto) && transportNumberPhoto.contains(webServerUrl)) {
            vehicleDTO.setTransportNumberPhoto(transportNumberPhoto.split(webServerUrl)[1]);
        }

        String drivingLicenseFrontPhoto = vehicleDTO.getDrivingLicenseFrontPhoto();
        if (StringUtils.isNotBlank(drivingLicenseFrontPhoto) && drivingLicenseFrontPhoto.contains(webServerUrl)) {
            vehicleDTO.setDrivingLicenseFrontPhoto(drivingLicenseFrontPhoto.split(webServerUrl)[1]);
        }

        String drivingLicenseDuplicatePhoto = vehicleDTO.getDrivingLicenseDuplicatePhoto();
        if (StringUtils.isNotBlank(drivingLicenseDuplicatePhoto) && drivingLicenseDuplicatePhoto
            .contains(webServerUrl)) {
            vehicleDTO.setDrivingLicenseDuplicatePhoto(drivingLicenseDuplicatePhoto.split(webServerUrl)[1]);
        }
    }

    private void standardChange(VehicleDTO vehicle, VehicleDTO oldVehicle) {
        //类别未发生改变,不做处理
        if (Objects.equals(vehicle.getVehicleCategoryId(), oldVehicle.getVehicleCategoryId())) {
            return;
        }
        VehicleCategoryDTO curCategory = cacheManger.getVehicleCategory(vehicle.getVehicleCategoryId());
        VehicleCategoryDTO oldCategory = cacheManger.getVehicleCategory(oldVehicle.getVehicleCategoryId());
        int oldStandard = oldCategory.getStandard();
        //标准未发生改变或之前为通用标准，不做处理
        if (Vehicle.Standard.COMMON == oldStandard || curCategory.getStandard() == oldStandard) {
            return;
        }
        if (Vehicle.Standard.ENGINEERING == oldStandard) {
            cleanEngineering(vehicle);
        } else {
            cleanFreightTransport(vehicle);
        }
    }

    private void cleanFreightTransport(VehicleDTO vehicle) {
        vehicle.setVehicleBrand(null);
        vehicle.setVehicleModel(null);
        vehicle.setVehicleProductionDate(null);
        vehicle.setFirstOnlineTime(null);
        vehicle.setPurchaseWay(null);
        vehicle.setValidEndDate(null);
        vehicle.setLicenseNumbers(null);
        vehicle.setTotalQuality(null);
        vehicle.setTractionTotalMass(null);
        vehicle.setProfileSizeLong(null);
        vehicle.setProfileSizeWide(null);
        vehicle.setProfileSizeHigh(null);
        vehicle.setInternalSizeHigh(null);
        vehicle.setInternalSizeLong(null);
        vehicle.setInternalSizeWide(null);
        vehicle.setShaftNumber(null);
        vehicle.setTiresNumber(null);
        vehicle.setTireSize(null);
    }

    private void cleanEngineering(VehicleDTO vehicleDTO) {
        vehicleDTO.setVehicleOwnerName(null);
        vehicleDTO.setOwnerPhoneOne(null);
        vehicleDTO.setOwnerPhoneTwo(null);
        vehicleDTO.setOwnerPhoneThree(null);
        vehicleDTO.setOwnerLandline(null);
        vehicleDTO.setVehicleSubTypeId(null);
        vehicleDTO.setVehicleSubType(null);
        vehicleDTO.setSelfRespect(null);
        vehicleDTO.setAbilityWork(null);
        vehicleDTO.setWorkingRadius(null);
        vehicleDTO.setMachineAge(null);
        vehicleDTO.setBrandModelsId(null);
        vehicleDTO.setModelName(null);
        vehicleDTO.setBrandName(null);
    }

    private Map<String, String> updateMonitorInfo(VehicleDTO vehicleDTO) {
        Map<String, String> map = new HashMap<>(16);
        map.put("operatingState", String.valueOf(vehicleDTO.getOperatingState()));
        map.put("vehicleType", vehicleDTO.getVehicleType());
        map.put("vehicleTypeName", vehicleDTO.getVehicleTypeName());
        if (Objects.nonNull(vehicleDTO.getVehicleSubTypeId())) {
            map.put("vehicleSubTypeId", vehicleDTO.getVehicleSubTypeId());
        }
        if (Objects.nonNull(vehicleDTO.getAlias())) {
            map.put("alias", vehicleDTO.getAlias());
        }
        map.put("orgId", vehicleDTO.getOrgId());
        if (StringUtils.isNotBlank(vehicleDTO.getOrgName())) {
            map.put("orgName", vehicleDTO.getOrgName());
        }
        map.put("name", vehicleDTO.getName());
        Integer isStart = vehicleDTO.getIsStart();
        map.put("isStart", Objects.isNull(isStart) ? "1" : String.valueOf(isStart));
        map.put("plateColor", String.valueOf(vehicleDTO.getPlateColor()));
        if (Objects.nonNull(vehicleDTO.getStateRepair())) {
            map.put("stateRepair", String.valueOf(vehicleDTO.getStateRepair()));
        }
        map.put("vehicleCategoryId", vehicleDTO.getVehicleCategoryId());
        if (Objects.nonNull(vehicleDTO.getVehiclePurpose())) {
            String purposeName = vehicleDTO.getVehiclePurposeName();
            if (StringUtils.isBlank(purposeName)) {
                VehiclePurposeDTO vehiclePurpose = cacheManger.getVehiclePurpose(vehicleDTO.getVehiclePurpose());
                purposeName = vehiclePurpose == null ? "" : vehiclePurpose.getPurposeCategory();
            }
            map.put("vehiclePurpose", vehicleDTO.getVehiclePurpose());
            map.put("vehiclePurposeName", purposeName);
        }
        if (Objects.nonNull(vehicleDTO.getChassisNumber())) {
            map.put("chassisNumber", vehicleDTO.getChassisNumber());
        }
        if (Objects.nonNull(vehicleDTO.getProvinceId())) {
            map.put("provinceId", vehicleDTO.getProvinceId());
        }
        if (Objects.nonNull(vehicleDTO.getProvince())) {
            map.put("province", vehicleDTO.getProvince());
        }
        if (Objects.nonNull(vehicleDTO.getCityId())) {
            map.put("cityId", vehicleDTO.getCityId());
        }
        if (Objects.nonNull(vehicleDTO.getCity())) {
            map.put("city", vehicleDTO.getCity());
        }
        if (Objects.nonNull(vehicleDTO.getCounty())) {
            map.put("county", vehicleDTO.getCounty());
        }

        //删除可能为空的字段
        String[] deleteFields =
            { "vehicleSubTypeId", "alias", "vehiclePurpose", "vehiclePurposeName", "chassisNumber", "stateRepair",
                "provinceId", "province", "cityId", "city", "county" };
        RedisHelper.hdel(RedisKeyEnum.MONITOR_INFO.of(vehicleDTO.getId()), Arrays.asList(deleteFields));
        return map;
    }

    @MethodLog(name = "更新", description = "车辆名称更新")
    @Override
    public boolean update(String id, String name) {
        return newVehicleDao.updateBrand(id, name);
    }

    @Override
    public List<String> getUserOwnIds(String keyword, List<String> orgIds) {
        //获取用户权限下所有绑定的监控对象ID
        Set<String> bindIdSet = userService.getCurrentUserMonitorIds();

        //获取用户权限下的企业，以及未绑定的车辆
        Set<String> unBindIdSet = getUnbindIds(orgIds);

        //用户权限下所有的车辆
        Set<String> userOwnSet = new HashSet<>();
        userOwnSet.addAll(bindIdSet);
        userOwnSet.addAll(unBindIdSet);
        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的车辆ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(keyword, userOwnSet, MonitorTypeEnum.VEHICLE);
        //进行排序和过滤
        return sortList(fuzzyVehicleIds, RedisKeyEnum.VEHICLE_SORT_LIST);
    }

    /**
     * 获取未绑定的车辆ID
     * @param orgIds 组织ID 若为null，获取用户权限下的组织id
     * @return 未绑定的车辆ID集合
     */
    private Set<String> getUnbindIds(List<String> orgIds) {
        List<String> orgIdList = null;
        if (orgIds == null) {
            // 获取用户权限的组织ID
            orgIdList = userService.getCurrentUserOrgIds();
        } else {
            orgIdList = orgIds;
        }

        if (CollectionUtils.isEmpty(orgIdList)) {
            return new HashSet<>();
        }

        Set<String> idSet = new HashSet<>();
        for (String orgId : orgIdList) {
            Set<String> tempSet = RedisHelper.hkeys(UNBIND_KEY.of(orgId));
            if (CollectionUtils.isNotEmpty(tempSet)) {
                idSet.addAll(tempSet);
            }
        }
        return idSet;
    }

    @Override
    public List<String> getUserOwnBindIds(String keyword) {
        // 获取用户权限下所有绑定的监控对象ID
        Set<String> bindIdSet = userService.getCurrentUserMonitorIds();

        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的车辆ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(keyword, bindIdSet, MonitorTypeEnum.VEHICLE);
        //进行排序和过滤
        return sortList(fuzzyVehicleIds, RedisKeyEnum.VEHICLE_SORT_LIST);
    }

    @Override
    public Page<VehicleDTO> getByPage(VehicleQuery query) {
        VehiclePageQuery vehicleQuery = query.convert();
        Page<VehicleDTO> pageResult;
        switch (Converter.toBlank(query.getGroupType())) {
            case "group":
                String orgId = query.getGroupName();
                pageResult = getListByOrg(orgId, vehicleQuery);
                break;
            case "assignment":
                String groupId = query.getGroupName();
                pageResult = getListByGroup(Collections.singletonList(groupId), vehicleQuery);
                break;
            default:
                pageResult = getListByKeyWord(vehicleQuery);
                break;
        }
        return pageResult;
    }

    @Override
    public Page<VehicleDTO> getListByKeyWord(VehiclePageQuery query) {
        //获取用户权限下所有绑定的监控对象ID
        Set<String> ownIdSet = userService.getCurrentUserMonitorIds();

        //获取用户权限下的企业，以及未绑定的车辆
        ownIdSet.addAll(getUnbindIds(null));

        //进行高级过滤
        Set<String> result = getAdvancedSearch(query, ownIdSet);

        //进行模糊搜索
        Set<String> fuzzyVehicleIds = fuzzyKeyword(query.getSimpleQueryParam(), result, MonitorTypeEnum.VEHICLE);

        return sortVehicleList(fuzzyVehicleIds, query);
    }

    /**
     * 进行高级条件过滤，包含模糊搜索
     * @param query    查询条件
     * @param ownIdSet 拥有权限的车辆集合
     * @return 符合高级过滤条件的监控对象
     */
    private Set<String> getAdvancedSearch(VehiclePageQuery query, Set<String> ownIdSet) {
        if (CollectionUtils.isEmpty(ownIdSet)) {
            return new HashSet<>();
        }

        //若高级条件筛选都是全部，则不进行下一步筛选
        Integer drivingLicenseType = query.getDrivingLicenseType();
        Integer roadTransportType = query.getRoadTransportType();
        Integer maintenanceType = query.getMaintenanceType();
        if (Objects.equals(QUERY_ALL, drivingLicenseType) && Objects.equals(QUERY_ALL, roadTransportType) && Objects
            .equals(QUERY_ALL, maintenanceType)) {
            return ownIdSet;
        }

        //过滤行驶证
        Set<String> result = new HashSet<>(ownIdSet);
        RedisKey redisKey = drivingLicenseType == 1 ? HistoryRedisKeyEnum.EXPIRE_DRIVING_LICENSE.of() :
            drivingLicenseType == 2 ? HistoryRedisKeyEnum.ALREADY_EXPIRE_DRIVING_LICENSE.of() : null;
        if (!isNextQuery(result, redisKey)) {
            return result;
        }

        //过滤道路运输证
        redisKey = roadTransportType == 1 ? HistoryRedisKeyEnum.EXPIRE_ROAD_TRANSPORT.of() :
            roadTransportType == 2 ? HistoryRedisKeyEnum.ALREADY_EXPIRE_ROAD_TRANSPORT.of() : null;
        if (!isNextQuery(result, redisKey)) {
            return result;
        }

        redisKey = maintenanceType == 2 ? HistoryRedisKeyEnum.EXPIRE_MAINTENANCE.of() : null;
        isNextQuery(result, redisKey);
        return result;
    }

    /**
     * 根据某一个过滤条件获取车辆ID
     * @param result   得到的车辆
     * @param redisKey 要获取的redis缓存
     * @return true 是否需要进行下一步查询，true 需要 false 不需要
     */
    private boolean isNextQuery(Set<String> result, RedisKey redisKey) {
        if (Objects.isNull(redisKey)) {
            return true;
        }
        //求两者的交集
        List<String> vehicleIds = RedisHelper.getSetFromString(redisKey);
        result.retainAll(vehicleIds);
        return !result.isEmpty();
    }

    private Page<VehicleDTO> sortVehicleList(Set<String> filterIds, BaseQueryBean query) {
        if (CollectionUtils.isEmpty(filterIds)) {
            return new Page<>();
        }
        //进行排序和过滤
        List<String> ids = sortList(filterIds, RedisKeyEnum.VEHICLE_SORT_LIST);
        //获取用户权限的分组信息
        Map<String, String> groupMap = userGroupService.getGroupMap();

        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));

        //维护用户导出车辆ID缓存中，只用于导出
        String userName = SystemHelper.getCurrentUsername();
        RedisHelper.setString(RedisKeyEnum.USER_VEHICLE_EXPORT.of(userName), JSON.toJSONString(ids));

        //进行分页
        Long start = query.getStart();
        Long end = Math.min(start + query.getLength(), ids.size());
        List<String> subList = ids.subList(Integer.valueOf(start + ""), Integer.valueOf(end + ""));
        if (CollectionUtils.isEmpty(subList)) {
            return RedisQueryUtil.getListToPage(new ArrayList<>(), query, ids.size());
        }
        List<VehicleDTO> vehicleDTOList = newVehicleDao.getDetailByIds(subList);
        Map<String, VehicleDTO> vehicleMap = new HashMap<>(16);

        //组装分组信息和企业信息
        for (VehicleDTO vehicleDTO : vehicleDTOList) {
            //封装用户权限下的分组，非用户权限下的分组过滤掉
            Map<String, String> group = filterGroup(vehicleDTO.getGroupId(), groupMap);
            vehicleDTO.setGroupName(group.get("groupName"));
            vehicleDTO.setGroupId(group.get("groupId"));
            vehicleDTO.setOrgName(orgMap.get(vehicleDTO.getOrgId()));
            vehicleMap.put(vehicleDTO.getId(), vehicleDTO);
        }

        //进行排序
        List<VehicleDTO> sortList = new ArrayList<>();
        for (String id : ids) {
            VehicleDTO vehicleDTO = vehicleMap.get(id);
            if (vehicleDTO != null) {
                sortList.add(vehicleDTO);
            }
        }
        return RedisQueryUtil.getListToPage(sortList, query, ids.size());
    }

    @Override
    public Page<VehicleDTO> getListByOrg(String orgId, VehiclePageQuery query) {
        //获取组织下未绑定的监控对象
        Set<String> unBindIdSet = getUnbindIds(Collections.singletonList(orgId));

        // 获取组织下的分组及分组下的监控对象
        Set<String> bindSet = groupMonitorService.getMonitorIdsByOrgId(Collections.singletonList(orgId));
        unBindIdSet.addAll(bindSet);
        //进行高级过滤
        Set<String> filterIds = getAdvancedSearch(query, unBindIdSet);

        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的车辆ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(query.getSimpleQueryParam(), filterIds, MonitorTypeEnum.VEHICLE);

        return sortVehicleList(fuzzyVehicleIds, query);
    }

    @Override
    public Page<VehicleDTO> getListByGroup(Collection<String> groupIds, VehiclePageQuery query) {
        //  获取分组下的监控对象
        Set<String> idSet = groupMonitorService.getMonitorIdsByGroupId(groupIds);

        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的车辆ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(query.getSimpleQueryParam(), idSet, MonitorTypeEnum.VEHICLE);

        //进行高级过滤
        Set<String> advancedSearchIds = getAdvancedSearch(query, fuzzyVehicleIds);
        return sortVehicleList(advancedSearchIds, query);
    }

    @Override
    public boolean isExistNumber(String id, String brand) {
        VehicleDO vehicleDO = newVehicleDao.getByBrand(brand);
        return Objects.nonNull(vehicleDO) && !Objects.equals(id, vehicleDO.getId());
    }

    @Override
    public boolean isBind(String brand) {
        VehicleDO vehicleDO = newVehicleDao.getByBrand(brand);
        if (Objects.isNull(vehicleDO)) {
            return false;
        }

        VehicleDTO vehicleDTO = newVehicleDao.getDetailById(vehicleDO.getId());
        return Objects.equals(vehicleDTO.getBindType(), Vehicle.BindType.HAS_BIND);
    }

    @MethodLog(name = "导出", description = "车辆信息导出")
    @Override
    public boolean export(HttpServletResponse response) throws Exception {
        //获取要导出的顺序车辆ID
        String jsonStr = RedisHelper.getString(RedisKeyEnum.USER_VEHICLE_EXPORT.of(SystemHelper.getCurrentUsername()));
        List<String> ids = StringUtils.isBlank(jsonStr) ? new ArrayList<>() : JSON.parseArray(jsonStr, String.class);
        List<VehicleExportDTO> vehicleList = new ArrayList<>(ids.size());
        if (CollectionUtils.isNotEmpty(ids)) {
            // 获取所有的企业的id和name的映射关系
            Map<String, String> orgMap = organizationService.getAllOrganization().stream()
                .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
            //获取用户权限的分组信息
            Map<String, String> groupMap = userGroupService.getGroupMap();

            //批量进行获取数据
            List<List<String>> cutList = cutList(ids);
            for (List<String> subList : cutList) {
                vehicleList.addAll(batchQuery(subList, orgMap, groupMap));
            }
        }
        ExportExcel export = new ExportExcel(null, VehicleExportDTO.class, 1);
        export.setDataList(vehicleList);
        // 输出导文件
        OutputStream out = response.getOutputStream();
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();

        return true;
    }

    @MethodLog(name = "批量更新", description = "批量更新车辆信息")
    @Override
    public boolean batchUpdate(Collection<String> ids, VehicleDTO vehicleDTO) {
        if (CollectionUtils.isEmpty(ids) || Objects.isNull(vehicleDTO)) {
            return false;
        }
        //获取车辆原来的详情
        List<VehicleDTO> oldVehicleList = newVehicleDao.getDetailByIds(ids);
        if (CollectionUtils.isEmpty(oldVehicleList)) {
            return false;
        }
        //判断车辆类类别是否进行类修改
        if (StringUtils.isNotBlank(vehicleDTO.getVehicleType())) {
            VehicleTypeDTO vehicleType = cacheManger.getVehicleType(vehicleDTO.getVehicleType());
            VehicleCategoryDTO categoryDTO = cacheManger.getVehicleCategory(vehicleType.getCategoryId());
            vehicleDTO.setStandard(categoryDTO.getStandard());
            vehicleDTO.setVehicleCategoryName(categoryDTO.getCategory());
            vehicleDTO.setVehicleTypeName(vehicleType.getType());
        }

        //若修改了行政区划，进行封装封装行政区划的其他信息
        if (StringUtils.isNotBlank(vehicleDTO.getProvince())) {
            setAdministrativeDivision(vehicleDTO);
        }
        VehicleDO vehicleDO = VehicleDO.build(vehicleDTO, false);
        vehicleDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        vehicleDO.setUpdateDataTime(new Date());

        //若修改了经营范围，进行同步修改
        updateBusinessScope(ids, vehicleDTO.getScopeBusinessIds());

        newVehicleDao.batchUpdate(ids, vehicleDO, vehicleDTO.getStandard());
        List<VehicleDTO> curVehicleList = newVehicleDao.getDetailByIds(ids);

        //初始化监控对象信息缓存和未绑定详情 由于车牌号不允许修改，所有不用维护模糊搜索缓存
        Map<RedisKey, Map<String, String>> infoRedisKeyMap = new HashMap<>(16);
        Map<RedisKey, Collection<String>> deleteUnBindMap = new HashMap<>(16);
        Map<RedisKey, Map<String, String>> updateUnbindMap = new HashMap<>(16);
        Map<String, VehicleDTO> oldVehicleMap =
            oldVehicleList.stream().collect(Collectors.toMap(VehicleDTO::getId, Function.identity()));

        //封装未绑定缓存和监控对象信息缓存，若未修改组织或车辆已经绑定则无需更新未绑定车辆缓存
        StringBuilder message = new StringBuilder();
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        for (VehicleDTO vehicle : curVehicleList) {
            String id = vehicle.getId();
            VehicleDTO oldVehicle = oldVehicleMap.get(id);
            message.append(String.format("修改车辆:%s(@%s)<br/>", vehicle.getName(), orgMap.get(oldVehicle.getOrgId())));

            vehicle.setOrgName(orgMap.get(vehicle.getOrgId()));
            infoRedisKeyMap.put(RedisKeyEnum.MONITOR_INFO.of(vehicle.getId()), updateMonitorInfo(vehicle));
            if (Objects.equals(vehicle.getBindType(), Vehicle.BindType.HAS_BIND)) {
                continue;
            }

            //更新未绑定缓存
            Map<String, String> updateMap = updateUnbindMap.get(UNBIND_KEY.of(vehicle.getOrgId()));
            updateMap = updateMap == null ? new HashMap<>(16) : updateMap;
            updateMap.put(id, bindUnbindValue(vehicle));
            updateUnbindMap.put(UNBIND_KEY.of(vehicle.getOrgId()), updateMap);

            //若企业发生改变，删除原来组织下未绑定的车辆
            if (!Objects.equals(vehicle.getOrgId(), oldVehicle.getOrgId())) {
                Collection<String> deleteList = deleteUnBindMap.get(UNBIND_KEY.of(oldVehicle.getOrgId()));
                deleteList = deleteList == null ? new ArrayList<>() : deleteList;
                deleteList.add(id);
                deleteUnBindMap.put(UNBIND_KEY.of(oldVehicle.getOrgId()), deleteList);
            }
        }

        if (configHelper.isCargoReportSwitch()) {
            cargoGroupVehicleLogService.updateBatch(curVehicleList, oldVehicleMap);
        }
        //维护监控对象信息缓存
        RedisHelper.batchAddToHash(infoRedisKeyMap);

        //维护监控对象未绑定缓存
        RedisHelper.hdel(deleteUnBindMap);
        RedisHelper.batchAddToHash(updateUnbindMap);

        //推送车辆修改事件
        publisher.publishEvent(new VehicleUpdateEvent(this, curVehicleList, oldVehicleList));

        //记录修改日志
        logService.addLog(getIpAddress(), message.toString(), "", "batch", "批量修改车辆");
        return true;
    }

    private void updateBusinessScope(Collection<String> ids, String businessScopeIds) {
        if (StringUtils.isBlank(businessScopeIds)) {
            return;
        }

        businessScopeService.deleteByIds(ids);
        List<BusinessScopeDO> businessScopes = new ArrayList<>();
        for (String vehicleId : ids) {
            for (String businessId : businessScopeIds.split(",")) {
                BusinessScopeDO businessScopeDO = new BusinessScopeDO(vehicleId, businessId, "2");
                businessScopes.add(businessScopeDO);
            }
        }
        if (!businessScopes.isEmpty()) {
            businessScopeService.addBusinessScope(businessScopes);
        }
    }

    private List<VehicleExportDTO> batchQuery(List<String> ids, Map<String, String> orgMap,
        Map<String, String> groupMap) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<VehicleExportDTO> vehicleList = newVehicleDao.getExportList(ids);
        int initialCapacity = (int) (vehicleList.size() / 0.75) + 1;
        Map<String, VehicleExportDTO> vehicleMap = new HashMap<>(initialCapacity);
        for (VehicleExportDTO vehicle : vehicleList) {
            vehicle.setOrgName(orgMap.get(vehicle.getOrgId()));
            //封装用户权限下的分组，非用户权限下的分组过滤掉
            Map<String, String> group = filterGroup(vehicle.getGroupId(), groupMap);
            vehicle.setGroupName(group.get("groupName"));
            vehicle.setGroupId(group.get("groupId"));
            vehicle.setVehicleColorStr(VehicleColor.getNameOrBlankByCode(vehicle.getVehicleColor()));
            vehicle.setPlateColorStr(PlateColor.getNameOrBlankByCode(vehicle.getPlateColor()));
            vehicleMap.put(vehicle.getId(), vehicle);
        }

        List<VehicleExportDTO> sortList = new ArrayList<>();
        for (String id : ids) {
            VehicleExportDTO vehicle = vehicleMap.get(id);
            if (Objects.nonNull(vehicle)) {
                sortList.add(vehicle);
            }
        }
        return sortList;
    }

    @Override
    public VehicleDTO getById(String id) {
        VehicleDTO vehicleDTO = newVehicleDao.getDetailById(id);
        if (Objects.isNull(vehicleDTO)) {
            return null;
        }

        //封装分组名称
        String groupIdStr = vehicleDTO.getGroupId();
        if (StringUtils.isNotBlank(groupIdStr)) {
            List<GroupDTO> groups = groupService.getGroupsById(Arrays.asList(groupIdStr.split(",")));
            Map<String, String> groupMap = AssembleUtil.collectionToMap(groups, GroupDTO::getId, GroupDTO::getName);
            Map<String, String> filterGroupMap = filterGroup(vehicleDTO.getGroupId(), groupMap);
            vehicleDTO.setGroupId(filterGroupMap.get("groupId"));
            vehicleDTO.setGroupName(filterGroupMap.get("groupName"));
        }

        //补全车辆相关图片的路径
        String serverUrl;
        if (configHelper.isSslEnabled()) {
            serverUrl = "/";
        } else {
            serverUrl = fdfsWebServer.getWebServerUrl();
        }
        if (StringUtils.isNotBlank(vehicleDTO.getVehiclePhoto())) {
            vehicleDTO.setVehiclePhoto(serverUrl + vehicleDTO.getVehiclePhoto());
        }

        if (StringUtils.isNotBlank(vehicleDTO.getTransportNumberPhoto())) {
            vehicleDTO.setTransportNumberPhoto(serverUrl + vehicleDTO.getTransportNumberPhoto());
        }

        if (StringUtils.isNotBlank(vehicleDTO.getDrivingLicenseFrontPhoto())) {
            vehicleDTO.setDrivingLicenseFrontPhoto(serverUrl + vehicleDTO.getDrivingLicenseFrontPhoto());
        }
        if (StringUtils.isNotBlank(vehicleDTO.getDrivingLicenseDuplicatePhoto())) {
            vehicleDTO.setDrivingLicenseDuplicatePhoto(serverUrl + vehicleDTO.getDrivingLicenseDuplicatePhoto());
        }

        //补全企业信息
        OrganizationLdap oldOrg = organizationService.getOrganizationByUuid(vehicleDTO.getOrgId());
        vehicleDTO.setOrgName(oldOrg == null ? "" : oldOrg.getName());

        return vehicleDTO;
    }

    @Override
    public List<VehicleDTO> getByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<VehicleDTO> vehicleList = newVehicleDao.getDetailByIds(ids);
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        for (VehicleDTO vehicleDTO : vehicleList) {
            //补全车辆相关图片的路径
            String serverUrl = fdfsWebServer.getWebServerUrl();
            if (StringUtils.isNotBlank(vehicleDTO.getVehiclePhoto())) {
                vehicleDTO.setVehiclePhoto(serverUrl + vehicleDTO.getVehiclePhoto());
            }

            if (StringUtils.isNotBlank(vehicleDTO.getTransportNumberPhoto())) {
                vehicleDTO.setTransportNumberPhoto(serverUrl + vehicleDTO.getTransportNumberPhoto());
            }

            if (StringUtils.isNotBlank(vehicleDTO.getDrivingLicenseFrontPhoto())) {
                vehicleDTO.setDrivingLicenseFrontPhoto(serverUrl + vehicleDTO.getDrivingLicenseFrontPhoto());
            }
            if (StringUtils.isNotBlank(vehicleDTO.getDrivingLicenseDuplicatePhoto())) {
                vehicleDTO.setDrivingLicenseDuplicatePhoto(serverUrl + vehicleDTO.getDrivingLicenseDuplicatePhoto());
            }
            vehicleDTO.setOrgName(orgMap.get(vehicleDTO.getOrgId()));
        }
        return vehicleList;
    }

    @Override
    public VehicleDTO getByName(String monitorName) {
        return newVehicleDao.getDetailByBrand(monitorName);
    }

    @MethodLog(name = "删除", description = "车辆信息删除")
    @Override
    public boolean delete(String id) throws BusinessException {
        VehicleDTO vehicleDTO = newVehicleDao.getDetailById(id);
        //已经绑定的不允许删除
        if (Objects.isNull(vehicleDTO)) {
            return true;
        }

        if (Objects.equals(vehicleDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
            throw new BusinessException(messageConfig.getVehicleBrandBound());
        }

        //维护货运车辆报表
        if (configHelper.isCargoReportSwitch()) {
            cargoGroupVehicleLogService.delete(Collections.singletonList(vehicleDTO));
        }
        //删除运营范围
        businessScopeService.deleteById(id);
        //进行车辆删除
        int count = newVehicleDao.delete(Collections.singletonList(id));
        if (count <= 0) {
            return false;
        }
        deleteVehiclePhotos(vehicleDTO);
        //删除监控对象信息缓存
        RedisHelper.delete(RedisKeyEnum.MONITOR_INFO.of(id));

        //删除模糊搜索缓存
        RedisHelper.hdel(FUZZY_KEY, buildFuzzyField(vehicleDTO));

        //删除顺序
        RedisHelper.delListItem(RedisKeyEnum.VEHICLE_SORT_LIST.of(), id);

        //同步删除未绑定缓存
        RedisHelper.hdel(UNBIND_KEY.of(vehicleDTO.getOrgId()), id);

        //删除车辆事件推送
        VehicleDeleteEvent deleteEvent = new VehicleDeleteEvent(this, Collections.singletonList(id), getIpAddress());

        //删除图标缓存
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), id);
        publisher.publishEvent(deleteEvent);
        return true;
    }

    @MethodLog(name = "批量删除", description = "车辆信息批量删除,只删除未绑定的车辆")
    @Override
    public Map<String, String> batchDel(Collection<String> ids) {
        List<VehicleDTO> vehicleList = newVehicleDao.getDetailByIds(ids);
        Map<String, String> result = new HashMap<>(16);
        if (CollectionUtils.isEmpty(vehicleList)) {
            return result;
        }

        //封装车辆管理相关的缓存，过滤掉已经绑定的监控对象，只能删除未绑定的监控对象
        List<RedisKey> monitorInfoRedisKey = new ArrayList<>();
        List<String> fuzzyFieldList = new ArrayList<>();
        Map<RedisKey, Collection<String>> orgUnbindMap = new HashMap<>(16);
        List<String> unbindList = new ArrayList<>();
        List<VehicleDTO> unBindVehicle = new ArrayList<>();
        Set<String> bindIds = new HashSet<>();
        Set<String> bindVehicles = new HashSet<>();
        for (VehicleDTO vehicleDTO : vehicleList) {
            if (Objects.equals(vehicleDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                bindIds.add(vehicleDTO.getId());
                bindVehicles.add(vehicleDTO.getName());
                continue;
            }
            unbindList.add(vehicleDTO.getId());
            unBindVehicle.add(vehicleDTO);
            monitorInfoRedisKey.add(RedisKeyEnum.MONITOR_INFO.of(vehicleDTO.getId()));
            fuzzyFieldList.add(buildFuzzyField(vehicleDTO));
            if (Objects.equals(vehicleDTO.getBindType(), Vehicle.BindType.UNBIND)) {
                Collection<String> idList = orgUnbindMap.get(UNBIND_KEY.of(vehicleDTO.getOrgId()));
                if (idList == null) {
                    idList = new ArrayList<>();
                }
                idList.add(vehicleDTO.getId());
                orgUnbindMap.put(UNBIND_KEY.of(vehicleDTO.getOrgId()), idList);
            }
            deleteVehiclePhotos(vehicleDTO);
        }
        result.put("boundBrands", StringUtils.join(bindVehicles, ","));
        result.put("boundBrandIds", StringUtils.join(bindIds, ","));
        result.put("infoMsg", messageConfig.getVehicleBrandBound());
        if (CollectionUtils.isEmpty(unbindList)) {
            return result;
        }

        result.put("infoMsg", "");
        //删除运营范围
        businessScopeService.deleteByIds(unbindList);

        //进行车辆删除
        newVehicleDao.delete(unbindList);

        //维护货运车辆报表
        if (configHelper.isCargoReportSwitch()) {
            cargoGroupVehicleLogService.delete(unBindVehicle);
        }
        //删除sort缓存中的车辆
        RedisHelper.delListItem(RedisKeyEnum.VEHICLE_SORT_LIST.of(), unbindList);

        //删除监控对象信息缓存
        RedisHelper.delete(monitorInfoRedisKey);

        //删除模糊搜索缓存
        RedisHelper.hdel(FUZZY_KEY, fuzzyFieldList);

        //删除组织下未绑定的车辆
        RedisHelper.hdel(orgUnbindMap);

        //删除图标缓存
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), unbindList);

        //推送车辆删除事件
        publisher.publishEvent(new VehicleDeleteEvent(this, unbindList, getIpAddress()));

        return result;
    }

    private void deleteVehiclePhotos(VehicleDTO vehicleDTO) {
        String transportNumberPhoto = vehicleDTO.getTransportNumberPhoto();
        if (StringUtils.isNotBlank(transportNumberPhoto)) {
            fastDFSClient.deleteFile(transportNumberPhoto);
        }

        String vehiclePhoto = vehicleDTO.getVehiclePhoto();
        if (StringUtils.isNotBlank(vehiclePhoto)) {
            fastDFSClient.deleteFile(vehiclePhoto);
        }
        String drivingLicenseFrontPhoto = vehicleDTO.getDrivingLicenseFrontPhoto();
        if (StringUtils.isNotBlank(drivingLicenseFrontPhoto)) {
            fastDFSClient.deleteFile(drivingLicenseFrontPhoto);
        }
        String drivingLicenseDuplicatePhoto = vehicleDTO.getDrivingLicenseDuplicatePhoto();
        if (StringUtils.isNotBlank(drivingLicenseDuplicatePhoto)) {
            fastDFSClient.deleteFile(drivingLicenseDuplicatePhoto);
        }
    }

    @Override
    public List<MonitorBaseDTO> getByCategoryName(String categoryName) {
        //根据类别名称获取类别ID
        VehicleCategoryDTO category = vehicleCategoryService.getByName(categoryName);
        if (Objects.isNull(category)) {
            return null;
        }
        //获取用户权限下绑定和未绑定的车辆
        List<String> vehicleIds = getUserOwnIds(null, null);
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return null;
        }

        String categoryId = category.getId();

        //获取要查询的字段
        Field[] fields = MonitorBaseDTO.class.getDeclaredFields();
        List<String> fieldList = new ArrayList<>();
        for (Field field : fields) {
            fieldList.add(field.getName());
        }
        fieldList.add("vehicleCategoryId");

        List<RedisKey> vehicleRedisKeys = new ArrayList<>(vehicleIds.size());
        vehicleIds.forEach(id -> vehicleRedisKeys.add(RedisKeyEnum.MONITOR_INFO.of(id)));

        //到缓存中查询车辆出信息，并返回符合查询类别的车辆数据
        List<Map<String, String>> vehicleMapList = RedisHelper.batchGetHashMap(vehicleRedisKeys, fieldList);
        List<MonitorBaseDTO> vehicleList = new ArrayList<>();
        for (Map<String, String> vehicleMap : vehicleMapList) {
            if (!Objects.equals(categoryId, vehicleMap.get("vehicleCategoryId"))) {
                continue;
            }
            vehicleList.add(MapUtil.mapToObj(vehicleMap, MonitorBaseDTO.class));
        }

        return vehicleList;
    }

    @Override
    public JsonResultBean saveMaintained(String vehicleType, String id, boolean execute) {
        VehicleTypeDTO vehicleTypeDTO = cacheManger.getVehicleType(vehicleType);
        if (Objects.isNull(vehicleTypeDTO) || Objects.isNull(vehicleTypeDTO.getServiceCycle())) {
            return new JsonResultBean(JsonResultBean.FAULT, "请先在车辆类型中设置保养里程间隔数");
        }

        VehicleDO vehicleDO = newVehicleDao.getById(id);
        if (Objects.isNull(vehicleDO)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆信息不存在，请检查");
        }

        Integer maintainMileage = Objects.isNull(vehicleDO.getMaintainMileage()) ? 0 : vehicleDO.getMaintainMileage();
        Integer total = maintainMileage + vehicleTypeDTO.getServiceCycle();
        if (total >= Vehicle.BIGEST_MAINTAIN_MEILAGE) {
            return new JsonResultBean(JsonResultBean.FAULT, "保养里程更新失败,此次更新会使保养里程数超过100万公里限制");
        }

        if (execute) {
            VehicleDO updateVehicleDO = new VehicleDO();
            updateVehicleDO.setMaintainMileage(total);
            newVehicleDao.batchUpdate(Collections.singletonList(id), updateVehicleDO, null);
            return new JsonResultBean(ImmutableMap.of("maintainMileage", total));
        }
        String msg = String.format("将对“%s”车辆保养里程数更新为%dKM,请确认", vehicleDO.getBrand(), total);
        return new JsonResultBean(JsonResultBean.SUCCESS, msg);
    }

    @Override
    public void updateDivision(String id, String provinceId, String cityId) {
        if (StringUtils.isBlank(provinceId) || StringUtils.isBlank(cityId)) {
            return;
        }
        AdministrativeDivisionDTO divisionDTO = divisionDao.getByDivisionCode(provinceId + cityId);
        if (Objects.isNull(divisionDTO)) {
            return;
        }

        VehicleDO vehicleDO = new VehicleDO();
        vehicleDO.setProvinceId(provinceId);
        vehicleDO.setCityId(cityId);
        vehicleDO.setProvince(divisionDTO.getProvinceName());
        vehicleDO.setCity(divisionDTO.getCityName());
        vehicleDO.setCounty(divisionDTO.getCountyName());
        vehicleDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        vehicleDO.setUpdateDataTime(new Date());

        //更新车辆的行政区划地址
        newVehicleDao.batchUpdate(Collections.singletonList(id), vehicleDO, null);

        //更新缓存
        Map<String, String> cacheMap = new HashMap<>(16);
        cacheMap.put("provinceId", provinceId);
        cacheMap.put("cityId", cityId);

        if (Objects.nonNull(divisionDTO.getProvinceName())) {
            cacheMap.put("province", divisionDTO.getProvinceName());
        }
        if (Objects.nonNull(divisionDTO.getCityName())) {
            cacheMap.put("city", divisionDTO.getCityName());
        }
        if (Objects.nonNull(divisionDTO.getCountyName())) {
            cacheMap.put("county", divisionDTO.getCountyName());
        }
        RedisKey redisKey = RedisKeyEnum.MONITOR_INFO.of(id);
        RedisHelper.hdel(redisKey, Arrays.asList("provinceId", "cityId", "province", "city", "county"));
        RedisHelper.addToHash(redisKey, cacheMap);
    }

    @Override
    public Map<String, Integer> getOperatingAndRepairNum() {
        //获取用户权限下绑定的车辆
        List<String> ids = getUserOwnBindIds(null);
        //批量获取车辆信息
        List<RedisKey> redisKeys = RedisKeyEnum.MONITOR_INFO.ofs(ids);
        List<Map<String, String>> vehicleList =
            RedisHelper.batchGetHashMap(redisKeys, Arrays.asList("stateRepair", "operatingState"));
        //进行营运车辆数量和维修车辆的数量统计
        int repairNum = 0;
        int operatingNum = 0;
        for (Map<String, String> vehicle : vehicleList) {
            if (Objects.equals(vehicle.get("stateRepair"), Vehicle.NEED_REPAIR)) {
                repairNum++;
            }
            if (Objects.equals(vehicle.get("operatingState"), Vehicle.IN_OPERATING_SATTE)) {
                operatingNum++;
            }
        }

        return ImmutableMap.of("repairNum", repairNum, "operatingNum", operatingNum);
    }

    @Override
    public List<String> getUserOwnIdsByUser(String keyword, UserDTO userDTO) {

        //获取用户权限下所有绑定的监控对象ID
        Set<String> bindIdSet = userService.getMonitorIdsByUser(userDTO.getUsername());

        //获取用户权限下的企业，以及未绑定的车辆
        List<String> orgIds = organizationService.getOrgUuidsByUser(userDTO.getId().toString());
        Set<String> unBindIdSet = getUnbindIds(orgIds);

        //用户权限下所有的车辆
        Set<String> userOwnSet = new HashSet<>();
        userOwnSet.addAll(bindIdSet);
        userOwnSet.addAll(unBindIdSet);
        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的车辆ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(keyword, userOwnSet, MonitorTypeEnum.VEHICLE);
        //进行排序和过滤
        return sortList(fuzzyVehicleIds, RedisKeyEnum.VEHICLE_SORT_LIST);
    }

    @Override
    public String getIdByBrand(String brand) {
        return newVehicleDao.getIdByBrand(brand);
    }

    @Override
    public String getIdByBrandAndColor(String brand, Integer vehicleColor) {
        return newVehicleDao.getIdByBrandAndColor(brand, vehicleColor);
    }

    @Override
    public VehicleDTO getVehicleDTOByDeviceNumber(String deviceNumber) {
        if (StringUtils.isBlank(deviceNumber)) {
            return null;
        }
        return newVehicleDao.getVehicleDTOByDeviceNumber(deviceNumber);
    }

    @Override
    public VehicleDTO getBindVehicleDTOByBrand(String brand) {
        return newVehicleDao.getBindVehicleDTOByBrand(brand);
    }

    @Override
    public String getOrgIdByBrand(String vehicleNo) {
        return newVehicleDao.getOrgIdByBrand(vehicleNo);
    }

    @Override
    public Set<String> getVehicleIdsByOrgId(String orgId) {
        return newVehicleDao.getVehicleIdsByOrgId(orgId);
    }

    @Override
    public VehicleDTO getVehicleInfoByDeviceId(String deviceId) {
        return newVehicleDao.getVehicleInfoByDeviceId(deviceId);
    }

    @Override
    public VehicleDTO getPartFieldById(String vehicleId) {
        return newVehicleDao.getPartFieldById(vehicleId);
    }

    @Override
    public List<Map<String, Object>> getUbBindSelectList() {
        return getUbBindSelectList(RedisHelper.getList(RedisKeyEnum.VEHICLE_SORT_LIST.of()));
    }

    @Override
    public List<Map<String, Object>> getUbBindSelectList(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return getUbBindSelectList();
        }

        //获取模糊搜索的到的车辆ID
        Set<String> fuzzyIds =
            FuzzySearchUtil.scanByMonitor(MonitorTypeEnum.VEHICLE.getType(), keyword, Vehicle.BindType.UNBIND);
        if (CollectionUtils.isEmpty(fuzzyIds)) {
            return new ArrayList<>();
        }

        //把模糊搜索的结果进行排序
        List<String> ids = RedisHelper.getList(RedisKeyEnum.VEHICLE_SORT_LIST.of());
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<String> sortIds = new ArrayList<>();
        for (String id : ids) {
            if (fuzzyIds.contains(id)) {
                sortIds.add(id);
            }
        }

        return getUbBindSelectList(sortIds);
    }

    private List<Map<String, Object>> getUbBindSelectList(List<String> sortIds) {
        if (CollectionUtils.isEmpty(sortIds)) {
            return new ArrayList<>();
        }
        //获取用户权限下的组织
        List<String> orgIds = userService.getCurrentUserOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }

        //获取未绑定的车辆列表
        List<RedisKey> redisKeys = orgIds.stream().map(UNBIND_KEY::of).collect(Collectors.toList());
        Map<String, String> unBindMap = RedisHelper.hgetAll(redisKeys);
        if (unBindMap.isEmpty()) {
            return new ArrayList<>();
        }

        //对未绑定的车辆列表进行排序
        List<Map<String, Object>> unBindList = new ArrayList<>();
        for (String id : sortIds) {
            String value = unBindMap.get(id);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            Map<String, Object> vehicleMap = JSON.parseObject(value);
            String brand = String.valueOf(vehicleMap.get("brand"));
            if (brand.startsWith("扫")) {
                continue;
            }
            vehicleMap.put("id", id);
            //限制下拉框返回数量
            if (Objects.equals(unBindList.size(), Vehicle.UNBIND_SELECT_SHOW_NUMBER)) {
                break;
            }
            unBindList.add(vehicleMap);
        }
        return unBindList;
    }

    @Override
    public void addOrUpdateRedis(List<VehicleDTO> vehicleList, Set<String> updateIds) {
        if (CollectionUtils.isEmpty(vehicleList)) {
            return;
        }
        int initialCapacity = (int) (vehicleList.size() / 0.75) + 1;

        Map<RedisKey, Collection<String>> delOrgUnbindMap = new HashMap<>(16);
        List<String> deleteFuzzy = new ArrayList<>();
        List<String> addVehicleIds = new ArrayList<>();
        Map<String, String> addFuzzyMap = new HashMap<>(initialCapacity);
        Map<RedisKey, Map<String, String>> addInfoRedisMap = new HashMap<>(initialCapacity);
        Map<RedisKey, Map<String, String>> addOrgUnbindMap = new HashMap<>(16);
        Map<String, Integer> orgNewAddNumMap = new HashMap<>(16);
        Map<RedisKey, Collection<String>> cargoReportMap = new HashMap<>(16);

        for (VehicleDTO vehicle : vehicleList) {
            String id = vehicle.getId();
            String orgId = vehicle.getOrgId();
            //判断车辆是否属于新增
            boolean isAdd = CollectionUtils.isEmpty(updateIds) || !updateIds.contains(id);
            //判断监控对象是否是要进行绑定
            boolean isBind = Objects.equals(vehicle.getBindType(), Vehicle.BindType.HAS_BIND);
            if (isAdd) {
                addVehicleIds.add(id);

                //企业下新增车辆数
                Integer orgAddNum = orgNewAddNumMap.get(orgId);
                orgNewAddNumMap.put(orgId, orgAddNum == null ? 1 : orgAddNum + 1);

                if (Objects.equals(Vehicle.Standard.FREIGHT_TRANSPORT, vehicle.getStandard()) && configHelper
                    .isCargoReportSwitch()) {
                    Collection<String> orgVehicleIds =
                            cargoReportMap.get(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(orgId));
                    if (orgVehicleIds == null) {
                        orgVehicleIds = new ArrayList<>();
                    }
                    orgVehicleIds.add(id);
                    cargoReportMap.put(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(orgId), orgVehicleIds);
                }
            }

            if (!isBind) {
                Map<String, String> unBindMap = addOrgUnbindMap.get(UNBIND_KEY.of(orgId));
                if (unBindMap == null) {
                    unBindMap = new HashMap<>(initialCapacity);
                }
                unBindMap.put(id, bindUnbindValue(vehicle));
                addOrgUnbindMap.put(UNBIND_KEY.of(orgId), unBindMap);
            }

            //监控对象已经存在，只是进行绑定，需要删除原来的企业下未绑定缓存和模糊搜索缓存
            if (!isAdd && isBind) {
                Collection<String> vehicleIds = delOrgUnbindMap.get(UNBIND_KEY.of(orgId));
                if (vehicleIds == null) {
                    vehicleIds = new ArrayList<>();
                }
                vehicleIds.add(id);
                delOrgUnbindMap.put(UNBIND_KEY.of(orgId), vehicleIds);
                deleteFuzzy.add(FuzzySearchUtil.VEHICLE_TYPE + vehicle.getName());
            }

            //模糊搜索和监控对象信息维护
            addFuzzyMap.put(buildFuzzyField(vehicle), buildFuzzyValue(vehicle));
            addInfoRedisMap.put(RedisKeyEnum.MONITOR_INFO.of(id), buildRedisInfo(vehicle));
        }

        RedisHelper.hdel(delOrgUnbindMap);
        RedisHelper.hdel(FUZZY_KEY, deleteFuzzy);
        //维护顺序缓存
        RedisHelper.addToListTop(RedisKeyEnum.VEHICLE_SORT_LIST.of(), addVehicleIds);
        //维护模糊搜索缓存
        RedisHelper.addToHash(FUZZY_KEY, addFuzzyMap);
        //维护监控对象信息缓存
        RedisHelper.batchAddToHash(addInfoRedisMap);
        //维护组织下未绑定车辆缓存
        RedisHelper.batchAddToHash(addOrgUnbindMap);
        //维护货运报表缓存
        RedisHelper.batchAddToSet(cargoReportMap);
        //维护，每月新增车辆数缓存
        addMonthAddNumRedis(orgNewAddNumMap);
    }

    @Override
    public VehicleDTO getDefaultInfo(ConfigDTO bindDTO) {
        VehicleDTO vehicle = new VehicleDTO();
        BeanUtils.copyProperties(bindDTO, vehicle);
        if (StringUtils.isBlank(bindDTO.getOrgId())) {
            OrganizationLdap currentUserOrg = userService.getCurUserOrgAdminFirstOrg();
            vehicle.setOrgId(currentUserOrg.getUuid());
            vehicle.setOrgName(currentUserOrg.getName());
        }
        vehicle.setIsStart(1);
        if (StringUtils.isBlank(bindDTO.getVehicleTypeId())) {
            vehicle.setVehicleType("default");
        } else {
            vehicle.setVehicleType(bindDTO.getVehicleTypeId());
        }
        if (StringUtils.isBlank(vehicle.getVehicleCategoryId())) {
            vehicle.setVehicleCategoryId("default");
        }
        if (Objects.isNull(vehicle.getOperatingState())) {
            vehicle.setOperatingState(0);
        }
        return vehicle;
    }

    @Override
    public MonitorInfo getF3Data(String id) {
        VehicleDO vehicleDO = newVehicleDao.getById(id);
        if (Objects.isNull(vehicleDO)) {
            return null;
        }

        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.setMonitorType(Integer.valueOf(MonitorTypeEnum.VEHICLE.getType()));
        monitorInfo.setMonitorId(vehicleDO.getId());
        monitorInfo.setMonitorName(vehicleDO.getBrand());
        monitorInfo.setGroupId(vehicleDO.getOrgId());
        monitorInfo.setProvinceId(vehicleDO.getProvinceId());
        monitorInfo.setCityId(vehicleDO.getCityId());
        OrganizationLdap org = organizationService.getOrganizationByUuid(vehicleDO.getOrgId());
        if (Objects.nonNull(org)) {
            monitorInfo.setGroupName(org.getName());
            monitorInfo.setOwersName(org.getName());
            monitorInfo.setOwersTel(org.getPhone());
        }
        // 燃油类型
        if (StringUtils.isNotBlank(vehicleDO.getFuelType())) {
            FuelTypeDO fuelTypeDO = fuelTypeDao.getById(vehicleDO.getFuelType());
            monitorInfo.setFuelType(Objects.isNull(fuelTypeDO) ? null : fuelTypeDO.getFuelType());
        }
        monitorInfo.setPlateColor(vehicleDO.getPlateColor());
        monitorInfo.setPlateColorName(PlateColor.getNameOrBlankByCode(vehicleDO.getPlateColor()));
        monitorInfo.setVehicleNumber(vehicleDO.getBrand());
        VehicleTypeDTO vehicleTypeDTO = cacheManger.getVehicleType(vehicleDO.getVehicleType());
        if (!Objects.isNull(vehicleTypeDTO)) {
            monitorInfo.setVehicleType(vehicleTypeDTO.getType());
            monitorInfo.setVehicleTypeCode(vehicleTypeDTO.getCodeNum());
            VehicleCategoryDTO categoryDTO = cacheManger.getVehicleCategory(vehicleTypeDTO.getCategoryId());
            if (Objects.nonNull(categoryDTO)) {
                monitorInfo.setTransType(CommonTypeUtils.getTransTypeByPurposeType(categoryDTO.getCategory()));
            }
        }
        monitorInfo.setVehicleOwner(vehicleDO.getVehicleOwner());
        monitorInfo.setVehicleOwnerPhone(vehicleDO.getVehicleOwnerPhone());
        return monitorInfo;
    }

    @Override
    public MonitorInfo getF3Data(VehicleDTO vehicleDTO) {
        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.setMonitorType(Integer.valueOf(MonitorTypeEnum.VEHICLE.getType()));
        monitorInfo.setMonitorId(vehicleDTO.getId());
        monitorInfo.setMonitorName(vehicleDTO.getName());
        monitorInfo.setGroupId(vehicleDTO.getOrgId());
        monitorInfo.setProvinceId(vehicleDTO.getProvinceId());
        monitorInfo.setCityId(vehicleDTO.getCityId());
        OrganizationLdap org = organizationService.getOrganizationByUuid(vehicleDTO.getOrgId());
        if (Objects.nonNull(org)) {
            monitorInfo.setGroupName(org.getName());
            monitorInfo.setOwersName(org.getName());
            monitorInfo.setOwersTel(org.getPhone());
        }
        monitorInfo.setPlateColor(vehicleDTO.getPlateColor());
        monitorInfo.setPlateColorName(PlateColor.getNameOrBlankByCode(vehicleDTO.getPlateColor()));
        monitorInfo.setVehicleNumber(vehicleDTO.getName());
        VehicleTypeDTO vehicleTypeDTO = cacheManger.getVehicleType(vehicleDTO.getVehicleType());
        if (!Objects.isNull(vehicleTypeDTO)) {
            monitorInfo.setVehicleTypeCode(vehicleTypeDTO.getCodeNum());
            monitorInfo.setVehicleType(vehicleTypeDTO.getType());
            VehicleCategoryDTO categoryDTO = cacheManger.getVehicleCategory(vehicleTypeDTO.getCategoryId());
            if (Objects.nonNull(categoryDTO)) {
                monitorInfo.setTransType(CommonTypeUtils.getTransTypeByPurposeType(categoryDTO.getCategory()));
            }
        }
        monitorInfo.setVehicleOwner(vehicleDTO.getVehicleOwner());
        monitorInfo.setVehicleOwnerPhone(vehicleDTO.getVehicleOwnerPhone());
        monitorInfo.setAuthCode(vehicleDTO.getAuthCode());
        monitorInfo.setSimcardNumber(vehicleDTO.getSimCardNumber());
        monitorInfo.setDeviceId(vehicleDTO.getDeviceId());
        monitorInfo.setDeviceNumber(vehicleDTO.getDeviceNumber());
        monitorInfo.setPhone(vehicleDTO.getSimCardNumber());
        monitorInfo.setSimcardNumber(vehicleDTO.getSimCardNumber());
        monitorInfo.setAssignmentId(vehicleDTO.getGroupId());
        monitorInfo.setAssignmentName(vehicleDTO.getGroupName());
        monitorInfo.setProfessionalsName(vehicleDTO.getProfessionalNames());
        monitorInfo.setTerminalType(vehicleDTO.getTerminalType());
        monitorInfo.setTerminalManufacturer(vehicleDTO.getTerminalManufacturer());
        monitorInfo.setAccessNetwork(vehicleDTO.getAccessNetwork());
        return monitorInfo;
    }

    @Override
    public List<MonitorBaseDTO> getByNames(Collection<String> monitorNames) {
        return newVehicleDao.getByBrands(monitorNames);
    }

    @Override
    public List<String> getScanByName(String afterName) {
        return newVehicleDao.getScanByName(afterName);
    }

    @Override
    public boolean updateIcon(Collection<String> ids, String iconId, String iconName) {
        newVehicleDao.updateIcon(ids, iconId);
        updateIconCache(ids, iconName);
        return true;
    }

    @Override
    public boolean deleteIcon(Collection<String> ids) {
        newVehicleDao.updateIcon(ids, "");
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), ids);
        return false;
    }

    @Override
    public void initIconCache() {
        List<VehicleDTO> vehicleList = newVehicleDao.getIconList();
        if (CollectionUtils.isEmpty(vehicleList)) {
            return;
        }
        Map<String, String> iconMap = new HashMap<>(CommonUtil.ofMapCapacity(vehicleList.size()));
        for (VehicleDTO vehicleDTO : vehicleList) {
            iconMap.put(vehicleDTO.getId(), vehicleDTO.getVehicleIconName());
        }
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_ICON.of(), iconMap);
    }

    private void addMonthAddNumRedis(Map<String, Integer> orgNewAddNumMap) {
        if (orgNewAddNumMap == null || orgNewAddNumMap.isEmpty()) {
            return;
        }
        String month = DateUtil.getDateToString(new Date(), DateFormatKey.YYYY_MM);
        RedisKey monthAddNumRedisKey = HistoryRedisKeyEnum.ORG_VEHICLE_NUM.of(month);
        Map<String, String> historyNumMap = RedisHelper.hgetAll(monthAddNumRedisKey);
        Map<String, String> orgAddNumMap = new HashMap<>(16);
        for (Map.Entry<String, Integer> entry : orgNewAddNumMap.entrySet()) {
            String orgId = entry.getKey();
            Integer addNum = entry.getValue();
            if (historyNumMap != null && StringUtils.isNotBlank(historyNumMap.get(orgId))) {
                addNum = addNum + Integer.parseInt(historyNumMap.get(orgId));
            }
            orgAddNumMap.put(orgId, String.valueOf(addNum));
        }
        RedisHelper.addToHash(monthAddNumRedisKey, orgAddNumMap);
    }

    @Override
    public void initCache() {
        log.info("开始进行车辆管理的redis初始化.");
        //维护车辆顺序缓存
        List<String> sortList = newVehicleDao.getSortList();
        cleanRedisCache();
        if (sortList.isEmpty()) {
            return;
        }
        RedisHelper.addToListTop(RedisKeyEnum.VEHICLE_SORT_LIST.of(), sortList);

        //获取所有的企业id和名称的映射关系map
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));

        //避免sql慢查询，导致激增mysql的cpu，采用分批处理
        List<List<String>> cutSortList = cutList(sortList);
        Map<RedisKey, Map<String, String>> unBindMap = new HashMap<>(256);
        for (List<String> subList : cutSortList) {
            //查询车辆绑定新
            List<VehicleDTO> tempList = newVehicleDao.initCacheList(subList);

            int initialCapacity = (int) (tempList.size() / 0.75) + 1;
            Map<String, String> fuzzyMap = new HashMap<>(initialCapacity);
            Map<String, String> intercomFuzzyMap = new HashMap<>(initialCapacity);
            Map<RedisKey, Map<String, String>> monitorInfoRedisMap = new HashMap<>(initialCapacity);
            Map<RedisKey, Map<String, String>> protocolMap = new HashMap<>(16);
            for (VehicleDTO vehicleDTO : tempList) {
                vehicleDTO.setOrgName(orgMap.get(vehicleDTO.getOrgId()));
                //封装未绑定的车辆缓存数据
                if (Objects.equals(vehicleDTO.getBindType(), Vehicle.BindType.UNBIND)) {
                    buildOrgUnbindMap(unBindMap, vehicleDTO);
                }
                fuzzyMap.put(buildFuzzyField(vehicleDTO), buildFuzzyValue(vehicleDTO));

                //车辆信息缓存维护
                RedisKey redisKey = RedisKeyEnum.MONITOR_INFO.of(vehicleDTO.getId());
                monitorInfoRedisMap.put(redisKey, buildRedisInfo(vehicleDTO));

                //维护对讲对象的模糊搜索
                if (StringUtils.isNotBlank(vehicleDTO.getIntercomDeviceNumber())) {
                    String key = String.format("%s%s&%s&%s", FuzzySearchUtil.VEHICLE_TYPE, vehicleDTO.getName(),
                        vehicleDTO.getIntercomDeviceNumber(), vehicleDTO.getSimCardNumber());
                    intercomFuzzyMap.put(key, buildFuzzyValue(vehicleDTO));
                }

                //维护车辆与协议类型的关系
                if (StringUtils.isNotBlank(vehicleDTO.getDeviceType())) {
                    RedisKey protocolRedisKey = RedisKeyEnum.MONITOR_PROTOCOL.of(vehicleDTO.getDeviceType());
                    Map<String, String> monitorMap =
                        protocolMap.getOrDefault(protocolRedisKey, new HashMap<>(initialCapacity));
                    monitorMap.put(vehicleDTO.getId(), vehicleDTO.getName());
                    protocolMap.put(protocolRedisKey, monitorMap);
                }
            }
            //维护模糊搜索
            RedisHelper.addToHash(FUZZY_KEY, fuzzyMap);
            //维护车辆信息
            RedisHelper.batchAddToHash(monitorInfoRedisMap);
            //维护对讲模糊搜索
            RedisHelper.addToHash(RedisKeyEnum.FUZZY_INTERCOM.of(), intercomFuzzyMap);
            //维护终端类型与车辆的关系
            RedisHelper.batchAddToHash(protocolMap);
        }

        //维护企业下未绑定车辆的缓存
        RedisHelper.batchAddToHash(unBindMap);
        log.info("结束车辆管理的redis初始化.");
    }

    private void buildOrgUnbindMap(Map<RedisKey, Map<String, String>> unBindMap, VehicleDTO vehicleDTO) {
        Map<String, String> unBindVehicleMap =
            unBindMap.getOrDefault(UNBIND_KEY.of(vehicleDTO.getOrgId()), new HashMap<>(16));
        unBindVehicleMap.put(vehicleDTO.getId(), bindUnbindValue(vehicleDTO));
        unBindMap.put(UNBIND_KEY.of(vehicleDTO.getOrgId()), unBindVehicleMap);
    }

    private String bindUnbindValue(VehicleDTO vehicleDTO) {
        Map<String, Object> vehicleMap =
            ImmutableMap.of("brand", vehicleDTO.getName(), "plateColor", vehicleDTO.getPlateColor());
        return JSON.toJSONString(vehicleMap);
    }

    /**
     * 构建车辆信息redis缓存的Map
     * @param vehicleDTO vehicleDTO
     * @return Map
     */
    private Map<String, String> buildRedisInfo(VehicleDTO vehicleDTO) {
        BindDTO bindDTO = new BindDTO();
        BeanUtils.copyProperties(vehicleDTO, bindDTO);
        bindDTO.setMonitorType(MonitorTypeEnum.VEHICLE.getType());

        Map<String, String> map = MapUtil.objToMap(bindDTO);
        if (Objects.nonNull(vehicleDTO.getOperatingState())) {
            map.put("operatingState", String.valueOf(vehicleDTO.getOperatingState()));
        }
        if (Objects.nonNull(vehicleDTO.getVehicleSubTypeId())) {
            map.put("vehicleSubTypeId", vehicleDTO.getVehicleSubTypeId());
        }

        if (StringUtils.isNotBlank(vehicleDTO.getVehicleType())) {
            map.put("vehicleType", vehicleDTO.getVehicleType());
            String categoryId = vehicleDTO.getVehicleCategoryId();
            if (StringUtils.isBlank(categoryId)) {
                VehicleTypeDTO vehicleTypeDTO = cacheManger.getVehicleType(vehicleDTO.getVehicleType());
                categoryId = Objects.nonNull(vehicleTypeDTO) ? vehicleTypeDTO.getCategoryId() : "";
            }
            map.put("vehicleCategoryId", categoryId);
            String vehicleTypeName = vehicleDTO.getVehicleTypeName();
            if (StringUtils.isBlank(vehicleTypeName)) {
                VehicleTypeDTO typeDTO = cacheManger.getVehicleType(vehicleDTO.getVehicleType());
                vehicleTypeName = Objects.isNull(typeDTO) ? "" : typeDTO.getType();
                vehicleDTO.setVehicleTypeName(vehicleTypeName);
            }
            map.put("vehicleTypeName", vehicleTypeName);
        }

        Integer isStart = vehicleDTO.getIsStart();
        map.put("isStart", Objects.isNull(isStart) ? "1" : String.valueOf(isStart));
        if (Objects.nonNull(vehicleDTO.getStateRepair())) {
            map.put("stateRepair", String.valueOf(vehicleDTO.getStateRepair()));
        }
        if (Objects.nonNull(vehicleDTO.getVehiclePurpose())) {
            map.put("vehiclePurpose", vehicleDTO.getVehiclePurpose());
            String purposeName = vehicleDTO.getVehiclePurposeName();
            if (StringUtils.isBlank(purposeName)) {
                VehiclePurposeDTO vehiclePurpose = cacheManger.getVehiclePurpose(vehicleDTO.getVehiclePurpose());
                purposeName = vehiclePurpose == null ? "" : vehiclePurpose.getPurposeCategory();
            }
            map.put("vehiclePurposeName", purposeName);
        }
        if (Objects.nonNull(vehicleDTO.getChassisNumber())) {
            map.put("chassisNumber", vehicleDTO.getChassisNumber());
        }
        if (Objects.nonNull(vehicleDTO.getProvinceId())) {
            map.put("provinceId", vehicleDTO.getProvinceId());
        }
        if (Objects.nonNull(vehicleDTO.getProvince())) {
            map.put("province", vehicleDTO.getProvince());
        }
        if (Objects.nonNull(vehicleDTO.getCityId())) {
            map.put("cityId", vehicleDTO.getCityId());
        }
        if (Objects.nonNull(vehicleDTO.getCity())) {
            map.put("city", vehicleDTO.getCity());
        }
        if (Objects.nonNull(vehicleDTO.getCounty())) {
            map.put("county", vehicleDTO.getCounty());
        }

        return map;
    }

    private void cleanRedisCache() {
        //删除车辆排序缓存
        RedisHelper.delete(RedisKeyEnum.VEHICLE_SORT_LIST.of());
        //删除未绑定的车辆缓存
        RedisHelper.delByPattern(RedisKeyEnum.ORG_UNBIND_VEHICLE_PATTERN.of());
    }

    @Override
    @MethodLog(name = "生成通用车辆列表模板", description = "生成通用车辆列表模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        return generateTemplate(response, Vehicle.Standard.COMMON);
    }

    @Override
    @MethodLog(name = "生成工程机械列表模板", description = "生成工程机械列表模板")
    public boolean generateTemplateEngineering(HttpServletResponse response) throws Exception {
        return generateTemplate(response, Vehicle.Standard.ENGINEERING);
    }

    @Override
    @MethodLog(name = "生成货运车辆列表模板", description = "生成货运车辆列表模板")
    public boolean generateTemplateFreight(HttpServletResponse response) throws Exception {
        return generateTemplate(response, Vehicle.Standard.FREIGHT_TRANSPORT);
    }

    @MethodLog(name = "批量导入", description = "车辆信息批量导入")
    @ImportLock(ImportModule.VEHICLE)
    @Override
    public JsonResultBean importExcel(MultipartFile file) throws Exception {
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        //获取导入数据
        List<? extends ImportErrorData> importList = getImportData(importExcel);
        if (CollectionUtils.isEmpty(importList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆列表数据导入为空!");
        }

        //转换成VehicleDTO实体
        int standard = getImportStandard(importExcel.getRow(0).getLastCellNum());
        List<VehicleDTO> vehicleList = convert2VehicleDTO(importList, standard);
        if (CollectionUtils.isEmpty(vehicleList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆列表数据导入为空!");
        }

        VehicleImportHandler handler =
            new VehicleImportHandler(vehicleList, newVehicleDao, this, configHelper, businessScopeService);

        final String username = SystemHelper.getCurrentUsername();
        try (ImportCache ignored = new ImportCache(ImportModule.VEHICLE, username, handler)) {
            final JsonResultBean jsonResultBean = handler.execute();
            if (!jsonResultBean.isSuccess()) {
                for (int i = 0; i < importList.size(); i++) {
                    importList.get(i).setErrorMsg(vehicleList.get(i).getErrorMsg());
                }
                ImportErrorUtil.putDataToRedis(importList, ImportModule.VEHICLE);
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        }

        StringBuilder message = new StringBuilder();
        for (VehicleDTO vehicle : vehicleList) {
            message.append(String.format("导入车辆 :%s ( @%s) <br/>", vehicle.getName(), vehicle.getOrgName()));
        }

        //记录日志
        logService.addLog(getIpAddress(), message.toString(), "3", "batch", "导入车辆");
        return new JsonResultBean(JsonResultBean.SUCCESS, "导入成功" + importList.size() + "数据.");
    }

    @Override
    public boolean addBatch(List<VehicleDO> vehicleList) {
        return newVehicleDao.addByBatch(vehicleList);
    }

    private List<VehicleDTO> convert2VehicleDTO(List<? extends ImportErrorData> importList, int standard) {
        //获取组织名称与name映射关系
        List<OrganizationLdap> orgList = userService.getCurrentUseOrgList();
        Map<String, String> orgNameIdMap =
            AssembleUtil.collectionToMap(orgList, OrganizationLdap::getName, OrganizationLdap::getUuid);
        List<VehicleDTO> vehicleList = new ArrayList<>();
        //获取所有燃油类型
        Map<String, String> fuelTypeMap =
            AssembleUtil.collectionToMap(fuelTypeDao.getAll(), FuelTypeDO::getFuelType, FuelTypeDO::getId);
        Iterator<String> iterator = fuelTypeMap.values().iterator();
        String defaultFuel = null;
        if (iterator.hasNext()) {
            defaultFuel = iterator.next();
        }

        //获取工程机械品牌和品牌型号
        Map<String, String> brandMap = new HashMap<>(16);
        Map<String, String> brandModelMap = new HashMap<>(16);
        if (Objects.equals(standard, Vehicle.Standard.ENGINEERING)) {
            brandMap =
                AssembleUtil.collectionToMap(brandDao.findBrandExport(), BrandForm::getBrandName, BrandForm::getId);
            brandModelMap = AssembleUtil
                .collectionToMap(brandDao.findBrandModelsExport(), BrandModelsForm::getModelName,
                    BrandModelsForm::getId);
        }

        int hasDivisionCount = 0;
        for (ImportErrorData importDTO : importList) {
            VehicleDTO vehicle = new VehicleDTO();
            BeanUtils.copyProperties(importDTO, vehicle);
            vehicle.setStandard(standard);
            vehicle.setBindType(Vehicle.BindType.UNBIND);
            vehicle.setMonitorType(MonitorTypeEnum.VEHICLE.getType());
            //补全企业信息
            isExist(vehicle, orgNameIdMap, vehicle.getOrgName(), "所属企业");
            vehicle.setOrgId(orgNameIdMap.get(vehicle.getOrgName()));

            String fuelType = fuelTypeMap.getOrDefault(Converter.toBlank(vehicle.getFuelTypeName()), defaultFuel);
            vehicle.setFuelType(fuelType);

            isExist(vehicle, brandMap, vehicle.getBrandName(), "品牌名称");
            isExist(vehicle, brandModelMap, vehicle.getModelName(), "机型");
            vehicle.setBrandModelsId(brandModelMap.get(Converter.toBlank(vehicle.getModelName())));
            if (StringUtils.isNotBlank(vehicle.getProvince())) {
                hasDivisionCount++;
            }
            vehicleList.add(vehicle);
        }
        // 设置省、市、区ID
        if (hasDivisionCount > 0) {
            setAdministrativeDivision(vehicleList);
        }
        return vehicleList;
    }

    @Override
    public void setAdministrativeDivision(VehicleDTO vehicle) {
        String provinceId = vehicle.getProvinceId();
        String cityId = vehicle.getCityId();
        if (StringUtils.isNotBlank(provinceId) && StringUtils.isNotBlank(cityId)) {
            AdministrativeDivisionDTO divisionDTO = divisionDao.getByDivisionCode(provinceId + cityId);
            if (Objects.nonNull(divisionDTO)) {
                vehicle.setProvince(divisionDTO.getProvinceName());
                vehicle.setCounty(divisionDTO.getCountyName());
                vehicle.setCity(divisionDTO.getCityName());
            } else {
                vehicle.setProvince(null);
                vehicle.setProvinceId(null);
                vehicle.setCounty(null);
                vehicle.setCity(null);
                vehicle.setCityId(null);
            }
            return;
        }
        String province = vehicle.getProvince();
        String city = vehicle.getCity();
        String county = vehicle.getCounty();
        if (StringUtils.isBlank(province) && StringUtils.isBlank(city) && StringUtils.isBlank(county)) {
            vehicle.setProvince(null);
            vehicle.setCounty(null);
            vehicle.setCity(null);
            vehicle.setProvinceId(null);
            vehicle.setCityId(null);
            return;
        }

        //特殊处理，直辖市在数据库中不存在city=xxx市郊县的数据
        if (province.endsWith("市") && city.endsWith("市郊县")) {
            city = province + "市辖区";
        }
        AdministrativeDivisionDTO divisionDTO = divisionDao.getByName(province, city, county);
        if (Objects.nonNull(divisionDTO)) {
            String divisionCode = divisionDTO.getDivisionsCode();
            vehicle.setProvinceId(divisionCode.substring(0, 2));
            vehicle.setCityId(divisionCode.substring(2));
        } else {
            vehicle.setProvince(null);
            vehicle.setCounty(null);
            vehicle.setCity(null);
            vehicle.setProvinceId(null);
            vehicle.setCityId(null);
        }
    }

    @Override
    public List<VehicleDO> getVehicleListByIds(Collection<String> vehicleIds) {
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return new ArrayList<>(1);
        }
        return newVehicleDao.getVehicleListByIds(vehicleIds);
    }

    @Override
    public void setAdministrativeDivision(List<VehicleDTO> vehicleList) {
        List<AdministrativeDivisionDTO> allCode = divisionDao.getAll();
        Map<String, List<AdministrativeDivisionDTO>> countyMap =
            allCode.stream().filter(o -> StringUtils.isNotBlank(o.getCountyName()))
                .collect(Collectors.groupingBy(AdministrativeDivisionDTO::getCountyName));

        for (VehicleDTO vehicle : vehicleList) {
            String county = vehicle.getCounty();
            String city = vehicle.getCity();
            String province = vehicle.getProvince();
            if (StringUtils.isBlank(county) && StringUtils.isBlank(city) && StringUtils.isBlank(province)) {
                continue;
            }
            //若车辆填写所属区域字段时，省市区必须都要填写
            if (StringUtils.isBlank(province) || StringUtils.isBlank(city) || StringUtils.isBlank(county)) {
                vehicle.setErrorMsg("车辆的区域【省、直辖市】、【市、区】、【县】若填写必须都填写，既必须到县");
                continue;
            }
            //直辖市特殊处理
            if (province.endsWith("市") && province.equals(city)) {
                city = province + (county.endsWith("县") ? "市郊县" : "市辖区");
            }

            //特殊处理，直辖市在数据库中不存在city=xxx市郊县的数据
            String queryCity = city;
            if (province.endsWith("市") && city.endsWith("市郊县")) {
                queryCity = province + "市辖区";
            }
            String divisionsCode = null;
            List<AdministrativeDivisionDTO> divisions = countyMap.getOrDefault(county, new ArrayList<>());
            for (AdministrativeDivisionDTO division : divisions) {
                if (Objects.equals(division.getProvinceName(), province) && Objects
                    .equals(division.getCityName(), queryCity)) {
                    divisionsCode = division.getDivisionsCode();
                    break;
                }
            }

            if (StringUtils.isNotBlank(divisionsCode)) {
                vehicle.setProvinceId(divisionsCode.substring(0, 2));
                vehicle.setCityId(divisionsCode.substring(2));
                vehicle.setCity(city);
            } else {
                vehicle.setErrorMsg(String.format("未在数据库中找到到%s%s%s的行政区划代码", province, city, county));
            }
        }
    }

    private boolean isExist(VehicleDTO vehicle, Map<String, String> nameIdMap, String name, String fieldName) {
        if (StringUtils.isNotBlank(name) && !nameIdMap.containsKey(name)) {
            vehicle.setErrorMsg("【" + fieldName + "】不存在");
            return false;
        }
        return true;
    }

    private List<? extends ImportErrorData> getImportData(ImportExcel importExcel) throws Exception {
        short lastCellNum = importExcel.getRow(0).getLastCellNum();
        List<? extends ImportErrorData> importList;
        switch (lastCellNum) {
            case Vehicle.COMMON_IMPORT_CELL:
                //加入校验器是为了校验非空字符串和重复数据，后面的就可以不做这两种校验
                importExcel.setImportValidator(new ImportValidator<VehicleImportDTO>());
                importList = importExcel.getDataListNew(VehicleImportDTO.class);
                break;
            case Vehicle.ENGINEER_IMPORT_CELL:
                importExcel.setImportValidator(new ImportValidator<VehicleImportEngineeringDTO>());
                importList = importExcel.getDataListNew(VehicleImportEngineeringDTO.class);
                break;
            case Vehicle.FREIGHT_IMPORT_CELL:
                importExcel.setImportValidator(new ImportValidator<VehicleImportFreightDTO>());
                importList = importExcel.getDataListNew(VehicleImportFreightDTO.class);
                break;
            default:
                throw new ZwImportException("车辆信息导入模板不正确！");
        }
        return importList;
    }

    private int getImportStandard(short lastCellNum) {
        int standard;
        switch (lastCellNum) {
            case Vehicle.COMMON_IMPORT_CELL:
                standard = Vehicle.Standard.COMMON;
                break;
            case Vehicle.ENGINEER_IMPORT_CELL:
                standard = Vehicle.Standard.ENGINEERING;
                break;
            case Vehicle.FREIGHT_IMPORT_CELL:
                standard = Vehicle.Standard.FREIGHT_TRANSPORT;
                break;
            default:
                standard = Vehicle.Standard.COMMON;
        }
        return standard;
    }

    private boolean generateTemplate(HttpServletResponse response, int standard) throws Exception {
        //获取模板的表头字段
        List<String> headList = getExcelHeader(standard);

        //获取模板的必填字段
        List<String> requiredList = getRequiredFields(standard);

        //获取枚举类型的下拉框数据
        Map<String, String[]> selectMap = getSelectMap(standard);

        //获取样例数据
        List<Object> exportList = getExampleDataMap(selectMap, headList, standard);

        //写入文件
        ExportExcelUtil.writeTemplateToFile(headList, requiredList, selectMap, exportList, response);
        return true;
    }

    /**
     * 获取模板的必填字段
     * @param standard 标准（0：通用；1：货运；2：工程机械）
     * @return 必填字段
     */
    private List<String> getRequiredFields(int standard) {
        List<String> requiredList = new ArrayList<>();
        requiredList.add("车牌号");
        requiredList.add("所属企业");
        requiredList.add("类别标准");
        requiredList.add("车辆类别");
        requiredList.add("车辆类型");
        if (Objects.equals(standard, Vehicle.Standard.ENGINEERING)) {
            requiredList.add("车主姓名");
            requiredList.add("车主手机1");
            requiredList.add("车辆子类型");
        }
        return requiredList;
    }

    /**
     * 获取模板的表头
     * @param standard 标准（0：通用；1：货运；2：工程机械）
     * @return 全部表头
     */
    private List<String> getExcelHeader(int standard) {
        List<String> headList = new ArrayList<>();
        headList.add("车牌号");
        headList.add("所属企业");
        headList.add("类别标准");
        headList.add("车辆类别");
        headList.add("车辆类型");
        headList.add("车辆别名");
        headList.add("车主");
        headList.add("车主电话");
        headList.add("车辆等级");
        headList.add("电话是否校验");
        headList.add("区域属性");
        headList.add("省、直辖市");
        headList.add("市、区");
        headList.add("县");
        headList.add("燃料类型");
        headList.add("车辆颜色");
        headList.add("车牌颜色");
        headList.add("车辆状态");
        headList.add("核定载人数");
        headList.add("核定载质量");
        headList.add("车辆保险单号");
        headList.add("运营类别");
        headList.add("所属行业");
        headList.add("是否维修");
        headList.add("车辆照片");
        headList.add("车辆技术等级有效期");
        headList.add("道路运输证号");
        headList.add("经营许可证号");
        headList.add("经营范围");
        headList.add("核发机关");
        headList.add("经营权类型");
        headList.add("道路运输证有效期起");
        headList.add("道路运输证有效期至");
        headList.add("线路牌号");
        headList.add("始发地");
        headList.add("途经站名");
        headList.add("终到地");
        headList.add("始发站");
        headList.add("路线入口");
        headList.add("终到站");
        headList.add("路线出口");
        headList.add("每日发班次数");
        headList.add("运输证提前提醒天数");
        headList.add("营运状态");
        headList.add("行驶证号");
        headList.add("车架号");
        headList.add("发动机号");
        headList.add("使用性质");
        headList.add("品牌型号");
        headList.add("行驶证有效期起");
        headList.add("行驶证有效期至");
        headList.add("行驶证发证日期");
        headList.add("行驶证登记日期");
        headList.add("行驶证提前提醒天数");

        //货运独有的表头字段
        if (Objects.equals(standard, Vehicle.Standard.FREIGHT_TRANSPORT)) {
            headList.add("车辆品牌");
            headList.add("车辆型号");
            headList.add("车辆出厂日期");
            headList.add("首次上线时间");
            headList.add("车辆购置方式");
            headList.add("校验有效期至");
            headList.add("执照上传数");
            headList.add("总质量(kg)");
            headList.add("准牵引总质量(kg)");
            headList.add("外廓尺寸-长(mm)");
            headList.add("外廓尺寸-宽(mm)");
            headList.add("外廓尺寸-高(mm)");
            headList.add("货厢内部尺寸-长(mm)");
            headList.add("货厢内部尺寸-宽(mm)");
            headList.add("货厢内部尺寸-高(mm)");
            headList.add("轴数");
            headList.add("轮胎数");
            headList.add("轮胎规格");
        }

        //工程机械独有的表头字段
        if (Objects.equals(standard, Vehicle.Standard.ENGINEERING)) {
            headList.add("车主姓名");
            headList.add("车主手机1");
            headList.add("车主手机2");
            headList.add("车主手机3");
            headList.add("车主座机");
            headList.add("车辆子类型");
            headList.add("自重(T)");
            headList.add("工作能力(T)");
            headList.add("工作半径(m)");
            headList.add("机龄");
            headList.add("品牌");
            headList.add("机型");
            headList.add("初始里程(km)");
            headList.add("初始工时(h)");
        }

        headList.add("保养里程数(km)");
        headList.add("保养有效期");
        headList.add("车台安装日期");
        return headList;
    }

    /**
     * 获取模板始化数据
     * @param standard 标准（0：通用；1：货运；2：工程机械）
     * @return 模板初始化数据
     */
    private Map<String, String[]> getSelectMap(int standard) {
        Map<String, String[]> selectMap = new HashMap<>(16);

        // 获取用户权限下的组织名称
        String[] orgNames =
            userService.getCurrentUseOrgList().stream().map(OrganizationLdap::getName).toArray(String[]::new);
        selectMap.put("所属企业", orgNames);

        //类型标准
        String[] standards = { "通用" };
        selectMap.put("类别标准", standards);

        //电话是否校验
        selectMap.put("电话是否校验", Vehicle.PHONE_CHECKS);

        // 车辆类别
        List<VehicleCategoryDTO> categoryList = cacheManger.getVehicleCategories(standard);
        selectMap.put("车辆类别", categoryList.stream().map(VehicleCategoryDTO::getCategory).toArray(String[]::new));

        // 车辆类型
        Set<String> categoryIds = categoryList.stream().map(VehicleCategoryDTO::getId).collect(Collectors.toSet());
        List<VehicleTypeDTO> types = cacheManger.getVehicleTypes(categoryIds);
        selectMap.put("车辆类型", types.stream().map(VehicleTypeDTO::getType).toArray(String[]::new));

        // 区域属性
        selectMap.put("区域属性", Vehicle.AREA_ATTRIBUTES);

        //燃料类型
        String[] fuelTypes = fuelTypeDao.getAll().stream().map(FuelTypeDO::getFuelType).toArray(String[]::new);
        selectMap.put("燃料类型", fuelTypes);

        // 车牌颜色
        selectMap.put("车牌颜色", PlateColor.getPalteColorNames());

        // 车辆状态
        selectMap.put("车辆状态", Vehicle.IS_START);

        // 车辆颜色
        String[] vehicleColor = VehicleColor.getVehicleNames();
        selectMap.put("车辆颜色", vehicleColor);

        //运营类别
        Collection<VehiclePurposeDTO> purposes = TypeCacheManger.getInstance().getVehiclePurposes();
        String[] purposeArr = purposes.stream().map(VehiclePurposeDTO::getPurposeCategory).toArray(String[]::new);
        selectMap.put("运营类别", purposeArr);

        //是否维修
        selectMap.put("是否维修", Vehicle.STATE_REPAIRS);

        //经营权类型
        selectMap.put("经营权类型", Vehicle.MANAGEMENT_TYPES);

        //营运状态
        selectMap.put("营运状态", Vehicle.OPERATING_STATES);

        String[] tradeNames =
            operationDao.findAllOperation("").stream().map(Operations::getOperationType).toArray(String[]::new);
        selectMap.put("所属行业", tradeNames);

        Map<String, String[]> diffSelectMap = null;
        if (Objects.equals(standard, Vehicle.Standard.ENGINEERING)) {
            diffSelectMap = getEngineeringOwnSelectMap();
        } else if (Objects.equals(standard, Vehicle.Standard.FREIGHT_TRANSPORT)) {
            diffSelectMap = getFreightOwnSelectMap();
        }
        if (diffSelectMap != null && !diffSelectMap.isEmpty()) {
            selectMap.putAll(diffSelectMap);
        }
        return selectMap;
    }

    /**
     * 工程机械独有的工程机械数据
     * @return 模板初始化数据
     */
    private Map<String, String[]> getEngineeringOwnSelectMap() {
        Map<String, String[]> selectMap = new HashMap<>(16);

        //类型标准
        String[] standards = { "工程机械" };
        selectMap.put("类别标准", standards);

        // 品牌
        String[] brands = brandDao.findBrandExport().stream().map(BrandForm::getBrandName).toArray(String[]::new);
        selectMap.put("品牌", brands);

        String[] brandModels =
            brandDao.findBrandModelsExport().stream().map(BrandModelsForm::getModelName).toArray(String[]::new);
        selectMap.put("机型", brandModels);

        // 车辆子类型
        List<VehicleSubTypeDTO> subTypes = cacheManger.getVehicleSubTypes(null);
        selectMap.put("车辆子类型", subTypes.stream().map(VehicleSubTypeDTO::getSubType).toArray(String[]::new));
        return selectMap;
    }

    /**
     * 货运车辆独有的初始化数据
     * @return 模板初始化数据
     */
    private Map<String, String[]> getFreightOwnSelectMap() {
        Map<String, String[]> selectMap = new HashMap<>(16);
        //类型标准
        String[] standards = { "货运" };
        selectMap.put("类别标准", standards);

        //车辆购置方式
        selectMap.put("车辆购置方式", Vehicle.PURCHASE_WAY);

        return selectMap;
    }

    /**
     * 获取导入模板下载数据的样例数据
     * @param selectMap 初始化枚举类型的数据
     * @param standard  标准（0：通用；1：货运；2：工程机械）
     * @param headList  头字段
     * @return 表头字段与样例的关系映射
     */
    private List<Object> getExampleDataMap(Map<String, String[]> selectMap, List<String> headList, int standard) {
        //存放表头和样例的map，主要使代码更容读
        Map<String, Object> exportMap = new HashMap<>(64);
        Date date = new Date();
        exportMap.put("车牌号", "渝BBB111");
        exportMap.put("所属企业", getArrFirstValue(selectMap.get("所属企业"), ""));

        String standardStr = selectMap.get("类别标准")[0];
        exportMap.put("类别标准", standardStr);

        String[] category = selectMap.get("车辆类别");
        String defaultCategory = "请先添加类别标准为" + standardStr + "的车辆类别,否则导入" + standardStr + "车辆将会失败";
        exportMap.put("车辆类别", getArrFirstValue(category, defaultCategory));

        String defaultType = "请先添加类别标准为" + standardStr + "的车辆类型,否则导入" + standardStr + "车辆将会失败";
        defaultType = category == null || category.length == 0 ? defaultCategory : defaultType;
        exportMap.put("车辆类型", getArrFirstValue(selectMap.get("车辆类型"), defaultType));
        exportMap.put("车辆别名", "红旗");
        exportMap.put("车主", "张三");
        exportMap.put("车主电话", "13658965874");
        exportMap.put("车辆等级", "高级");
        exportMap.put("电话是否校验", "已校验");
        exportMap.put("区域属性", "省内");
        exportMap.put("省、直辖市", "重庆市");
        exportMap.put("市、区", "重庆市市辖区");
        exportMap.put("县", "城口县");
        exportMap.put("燃料类型", getArrFirstValue(selectMap.get("燃料类型"), "请先添加燃料类型"));
        exportMap.put("车辆颜色", "黑色");
        exportMap.put("车牌颜色", "蓝色");
        exportMap.put("车辆状态", "启用");
        exportMap.put("核定载人数", "22");
        exportMap.put("核定载质量", "99.9");
        exportMap.put("车辆保险单号", "51235");
        exportMap.put("运营类别", "道路旅客运输");
        exportMap.put("所属行业", getArrFirstValue(selectMap.get("所属行业"), ""));
        exportMap.put("是否维修", "否");
        exportMap.put("车辆照片", "照片");
        exportMap.put("车辆技术等级有效期", Converter.toString(date, "yyyy-MM-dd"));
        exportMap.put("道路运输证号", "500123");
        exportMap.put("经营许可证号", "500123");
        String businessScope = Objects.equals(Vehicle.Standard.COMMON, standard) ? "拉人" : "道路旅客运输";
        exportMap.put("经营范围", businessScope);
        exportMap.put("核发机关", "重庆");
        exportMap.put("经营权类型", "国有");
        exportMap.put("道路运输证有效期起", Converter.toString(date, "yyyy-MM-dd"));
        exportMap.put("道路运输证有效期至", Converter.toString(date, "yyyy-MM-dd"));
        exportMap.put("线路牌号", "123");
        exportMap.put("始发地", "重庆");
        exportMap.put("途经站名", "成都");
        exportMap.put("终到地", "绵阳");
        exportMap.put("始发站", "重庆");
        exportMap.put("路线入口", "318");
        exportMap.put("终到站", "绵阳");
        exportMap.put("路线出口", "518");
        exportMap.put("每日发班次数", "3");
        exportMap.put("运输证提前提醒天数", "5");
        exportMap.put("营运状态", "营运");
        exportMap.put("行驶证号", "5123");
        exportMap.put("车架号", "5123");
        exportMap.put("发动机号", "5123");
        exportMap.put("使用性质", businessScope);
        exportMap.put("品牌型号", "acz");
        exportMap.put("行驶证有效期起", Converter.toString(date, "yyyy-MM-dd"));
        exportMap.put("行驶证有效期至", Converter.toString(date, "yyyy-MM-dd"));
        exportMap.put("行驶证发证日期", Converter.toString(date, "yyyy-MM-dd"));
        exportMap.put("行驶证登记日期", Converter.toString(date, "yyyy-MM-dd"));
        exportMap.put("行驶证提前提醒天数", "5");

        //货运模板独有字段
        if (Objects.equals(standard, Vehicle.Standard.FREIGHT_TRANSPORT)) {
            exportMap.put("车辆品牌", "品牌名称");
            exportMap.put("车辆型号", "型号名称");
            exportMap.put("车辆出厂日期", Converter.toString(date, "yyyy-MM-dd"));
            exportMap.put("首次上线时间", Converter.toString(date, "yyyy-MM-dd HH:mm:ss"));
            exportMap.put("车辆购置方式", "分期付款");
            exportMap.put("校验有效期至", Converter.toString(date, "yyyy-MM-dd"));
            exportMap.put("执照上传数", "3");
            exportMap.put("总质量(kg)", "1000");
            exportMap.put("准牵引总质量(kg)", "1200");
            exportMap.put("外廓尺寸-长(mm)", "500");
            exportMap.put("外廓尺寸-宽(mm)", "300");
            exportMap.put("外廓尺寸-高(mm)", "200");
            exportMap.put("货厢内部尺寸-长(mm)", "400");
            exportMap.put("货厢内部尺寸-宽(mm)", "250");
            exportMap.put("货厢内部尺寸-高(mm)", "150");
            exportMap.put("轴数", "4");
            exportMap.put("轮胎数", "14");
            exportMap.put("轮胎规格", "175/70R");
        }

        //工程机械模板独有字段
        if (Objects.equals(standard, Vehicle.Standard.ENGINEERING)) {
            exportMap.put("车主姓名", "李四");
            exportMap.put("车主手机1", "15222334569");
            exportMap.put("车主手机2", "15222334569");
            exportMap.put("车主手机3", "15222334569");
            exportMap.put("车主座机", "023-88888888");
            exportMap.put("车辆子类型", getArrFirstValue(selectMap.get("车辆子类型"), "请先添加车辆子类型,否则将导入失败"));
            exportMap.put("自重(T)", "8.9");
            exportMap.put("工作能力(T)", "12.8");
            exportMap.put("工作半径(m)", "5.8");
            exportMap.put("机龄", "2010-04");
            exportMap.put("品牌", getArrFirstValue(selectMap.get("品牌"), "如果你要选择品牌,请先添加"));
            exportMap.put("机型", getArrFirstValue(selectMap.get("机型"), "如果你要选择机型,请先添加"));
            exportMap.put("初始里程(km)", "1002.8");
            exportMap.put("初始工时(h)", "88.2");
        }

        exportMap.put("保养里程数(km)", "6666");
        exportMap.put("保养有效期", Converter.toString(date, "yyyy-MM-dd"));
        exportMap.put("车台安装日期", Converter.toString(date, "yyyy-MM-dd"));

        List<Object> exportList = new ArrayList<>();
        for (String head : headList) {
            exportList.add(exportMap.get(head));
        }
        return exportList;
    }

    @Override
    public List<String> getVehicleIdsByAlreadyExpireLicense() {
        return newVehicleDao.getVehicleIdsByAlreadyExpireLicense();
    }

    @Override
    public List<String> getVehicleIdsByWillExpireLicense() {
        return newVehicleDao.getVehicleIdsByWillExpireLicense();

    }

    @Override
    public List<String> getVehicleIdsByAlreadyExpireRoadTransport() {
        return newVehicleDao.getVehicleIdsByAlreadyExpireRoadTransport();
    }

    @Override
    public List<String> getVehicleIdsByWillExpireRoadTransport() {
        return newVehicleDao.getVehicleIdsByWillExpireRoadTransport();
    }

    @Override
    public List<String> getVehicleIdsByMaintenanceExpired() {
        return newVehicleDao.getVehicleIdsByMaintenanceExpired();
    }

    @Override
    public Map<String, BaseKvDo<String, Integer>> getVehicleIdsByMaintenanceMileageIsNotNull() {
        return newVehicleDao.getVehicleIdsByMaintenanceMileageIsNotNull();
    }

    @Override
    public void deleteNeverOnlineVehicle() {
        List<String> allVehicleId = newVehicleDao.findAllVehicleId();
        List<String> vehicleLocationKey = RedisHelper.scanKeys(HistoryRedisKeyEnum.MONITOR_LOCATION.of("*"));
        if (CollectionUtils.isEmpty(vehicleLocationKey) || CollectionUtils.isEmpty(allVehicleId)) {
            return;
        }
        Map<String, String> map = Maps.newHashMap();
        for (String key : vehicleLocationKey) {
            if (StringUtils.isEmpty(key)) {
                continue;
            }
            String[] split = key.split("-location");
            if (split.length == 0) {
                continue;
            }
            map.put(split[0], key);
        }
        List<String> list = Lists.newArrayList();

        List<RedisKey> noLoactionMonitorKeys = new ArrayList<>();

        for (String vehicleId : allVehicleId) {
            if (map.get(vehicleId) == null) {
                list.add(vehicleId);
                noLoactionMonitorKeys.add(RedisKeyEnum.MONITOR_INFO.of(vehicleId));
            }
        }
        //删除表 zw_m_assignment_vehicle，zw_m_vehicle_info，zw_m_config
        newVehicleDao.delete(list);
        newVehicleDao.deleteAssignmentByVehicleId(list);
        newVehicleDao.deleteConfigByVehicleId(list);
        RedisHelper.delete(noLoactionMonitorKeys);

        log.info("删除从未上线车辆，操作用户：{}，车辆ID：{}", SystemHelper.getCurrentUsername(), JSON.toJSONString(list));
    }

    @Override
    public boolean addCargoGroupVehicle(List<CargoGroupVehicleDO> cargoGroupVehicleDOS) {
        return newVehicleDao.addCargoGroupVehicle(cargoGroupVehicleDOS);
    }

}
