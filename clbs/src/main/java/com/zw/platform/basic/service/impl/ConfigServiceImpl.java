package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.zw.app.util.ConfigEditUtil;
import com.zw.platform.basic.constant.DictionaryType;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.InputTypeEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.MessageConfig;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.domain.ConfigProfessionalDO;
import com.zw.platform.basic.domain.DictionaryDO;
import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.ConfigDetailDTO;
import com.zw.platform.basic.dto.ConfigUpdateDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.export.ConfigExportDTO;
import com.zw.platform.basic.dto.imports.ConfigImportDTO;
import com.zw.platform.basic.dto.imports.ConfigTransportImportDTO;
import com.zw.platform.basic.dto.query.BasePageQuery;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.event.ConfigUpdateEvent;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.imports.ConfigTransportHolder;
import com.zw.platform.basic.imports.handler.ConfigDeviceImportHandler;
import com.zw.platform.basic.imports.handler.ConfigGroupImportHandler;
import com.zw.platform.basic.imports.handler.ConfigImportHandler;
import com.zw.platform.basic.imports.handler.ConfigPeopleImportHandler;
import com.zw.platform.basic.imports.handler.ConfigSimCardImportHandler;
import com.zw.platform.basic.imports.handler.ConfigThingImportHandler;
import com.zw.platform.basic.imports.handler.ConfigTransportHandler;
import com.zw.platform.basic.imports.handler.ConfigVehicleImportHandler;
import com.zw.platform.basic.imports.handler.ConfigVideoChannelHandler;
import com.zw.platform.basic.imports.validator.ConfigImportValidator;
import com.zw.platform.basic.imports.validator.ConfigTransportImportValidator;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.ConfigMessageService;
import com.zw.platform.basic.service.ConfigService;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.LifecycleService;
import com.zw.platform.basic.service.MonitorBaseService;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.domain.netty.DeviceUnbound;
import com.zw.platform.event.SimNumberUpdateEvent;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.service.realTimeVideo.VideoParamSettingService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.topspeed.TopSpeedService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.excel.validator.ImportValidator;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.ProgressDetails;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportLock;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 信息配置管理
 * @author zhangjuan
 */
@Service("configService")
@Order(0)
public class ConfigServiceImpl implements ConfigService, CacheService, IpAddressService {
    private static final RedisKey FUZZY_KEY = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();
    private static final Logger log = LogManager.getLogger(ConfigServiceImpl.class);
    @Autowired
    private MonitorFactory monitorFactory;

    @Autowired
    private GroupMonitorService groupMonitorService;

    @Autowired
    private DeviceService deviceService;

    /**
     * 信息配置导入专用
     */
    @Autowired
    private DeviceNewDao deviceNewDao;

    @Autowired
    private NewConfigDao configDao;

    @Autowired
    private LifecycleService lifecycleService;

    @Autowired
    private LogSearchService logService;

    @Autowired
    private MessageConfig msgConfig;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ConfigMessageService configMessageService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private SimCardService simCardService;

    @Autowired
    private ProfessionalService professionalService;

    /**
     * 信息配置导入专用
     */
    @Autowired
    private SimCardNewDao simCardNewDao;

    @Autowired
    private NewProfessionalsDao professionalsDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    private VideoParamSettingService videoParamSettingService;

    @Autowired
    private TopSpeedService topSpeedService;

    @Autowired
    private RoleService roleService;

    @Override
    public void initCache() {
        log.info("开始进行信息配置的redis初始化,包含信息配置相关缓存的清理和顺序缓存的维护");
        List<String> sortList = configDao.getSortList(null);
        //1、清除监控对象信息配置绑定相关的监控对象缓存
        //1.1、删除信息配置绑定监控对象的排序缓存
        RedisHelper.delete(RedisKeyEnum.CONFIG_SORT_LIST.of());
        //1.2、删除信息配置模糊搜索监控对象缓存
        RedisHelper.delete(RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of());
        //1.3、删除对讲信息模糊搜索缓存
        RedisHelper.delete(RedisKeyEnum.FUZZY_INTERCOM.of());
        //1.4、删除协议与监控对象关系缓存
        RedisHelper.delByPattern(RedisKeyEnum.MONITOR_PROTOCOL_PATTERN.of());
        //1.5、删除监控对象基础信息缓存
        RedisHelper.delByPattern(RedisKeyEnum.MONITOR_INFO_PATTERN.of());

        //2 维护信息配置相关缓存
        //2.1 信息配置列表顺序缓存
        RedisHelper.addToListTop(RedisKeyEnum.CONFIG_SORT_LIST.of(), sortList);
        //2.2 模糊搜索（信息列表+对讲信息列表）、详情缓存、协议与监控对象缓存在对应的监控对象的实现类中实现
        log.info("完成信息配置的redis初始化");
    }

    @Override
    @ImportLock(ImportModule.CONFIG)
    public boolean add(ConfigDTO configDTO) throws BusinessException {
        log.info("信息配置录入-转后的入参：{}", JSON.toJSONString(configDTO));
        if (checkIsBind(configDTO)) {
            throw new BusinessException("监控对象、终端或sim卡存在绑定关系");
        }
        configDTO.setAccessNetwork(0);
        // 获取当前用户下的所有组织ID
        List<String> userOrgIds = userService.getCurrentUserOrgIds();
        //1、添加监控对象
        monitorFactory.addMonitor(configDTO, userOrgIds);

        //2、维护监控对象分组信息
        updateMonitorGroup(configDTO, null);

        //3、进行终端的添加
        addDevice(configDTO, userOrgIds);

        //4、进行SIM卡的添加
        addSimCard(configDTO, userOrgIds);

        //5、进行从业人员的绑定
        String configId = UUID.randomUUID().toString();
        configDTO.setConfigId(configId);
        if (StringUtils.isNotBlank(configDTO.getProfessionalIds())) {
            updateProfessional(configDTO, "add");
        }

        //6、进行计费周期的绑定
        if (StringUtils.isNotBlank(configDTO.getBillingDate()) && StringUtils.isNotBlank(configDTO.getExpireDate())) {
            String serviceLifecycleId = lifecycleService.add(configDTO.getBillingDate(), configDTO.getExpireDate());
            configDTO.setServiceLifecycleId(serviceLifecycleId);
        }

        //7、信息配置相关数据添加
        BindDTO bindDTO = new BindDTO();
        BeanUtils.copyProperties(configDTO, bindDTO);
        addConfig(bindDTO);

        // 8、按照终端类型设置默认音视频通道参数
        videoParamSettingService.addVideoChannelParam(configDTO.getTerminalTypeId(), configDTO.getId());

        // 9、下发监控对象绑定信息到协议端
        configMessageService.sendToF3(configDTO, null);

        //10、通知flink
        configMessageService.sendToStorm(configDTO.getMonitorType(), configDTO.getId());

        //11、若是极速录入删除本次绑定的注册终端信息
        if (Objects.equals(configDTO.getInputType(), InputTypeEnum.TOP_SPEED_INPUT)) {
            deleteUnknowDevice(configDTO.getUniqueNumber());
        }
        // 12、记录日志
        addLog("add", configDTO, null);
        return true;
    }

    private void deleteUnknowDevice(String uniqueNumber) {
        // 过滤框内时间
        if (uniqueNumber.contains("（")) {
            uniqueNumber = uniqueNumber.substring(0, uniqueNumber.indexOf("（"));
        }
        topSpeedService.deleteByDeviceId(uniqueNumber);

    }

    /**
     * 检查是否绑定
     * @param bindDTO 信息配置信息
     * @return false 未被其他配置绑定， true:以及被其他配置进行绑定
     */
    private boolean checkIsBind(ConfigDTO bindDTO) {
        if (StringUtils.isNotBlank(bindDTO.getId()) || StringUtils.isNotBlank(bindDTO.getDeviceId()) || StringUtils
            .isNotBlank(bindDTO.getSimCardId())) {
            Set<String> configIds = configDao.getIsBind(bindDTO.getId(), bindDTO.getSimCardId(), bindDTO.getDeviceId());
            if (CollectionUtils.isEmpty(configIds)) {
                return false;
            }

            //若是进行修改，只要出的config一致即可
            String configId = bindDTO.getConfigId();
            return !(StringUtils.isNotBlank(configId) && configIds.size() == 1 && configIds.contains(configId));
        }
        return false;
    }

    @Override
    @ImportLock(ImportModule.CONFIG)
    public boolean update(ConfigDTO configDTO, String oldMonitorId) throws BusinessException {
        if (checkIsBind(configDTO)) {
            throw new BusinessException("监控对象、终端或sim卡存在绑定关系");
        }

        try {
            //校验是否有其他用户进行修改
            checkOtherUserOperate(configDTO);
            //获取历史的信息配置
            BindDTO oldBindDTO = getByMonitorId(oldMonitorId);
            if (Objects.isNull(oldBindDTO)) {
                throw new BusinessException("原来信息配置不存在");
            }

            //修改监控对象信息
            monitorFactory.updateName(configDTO, oldBindDTO);

            //修改终端信息
            updateDevice(configDTO, oldBindDTO);

            // 修改sim卡信息
            updateSimCard(configDTO, oldBindDTO);

            //修改分组信息,监控对象信息改变或者分组发生改变再进行修改
            // 不删除自己无权访问的分组
            final Set<String> nonAccessibleGroupIds = this.nonAccessibleGroups(oldBindDTO.getGroupId());
            configDTO.setGroupId(this.buildNewGroupId(configDTO.getGroupId(), nonAccessibleGroupIds));
            String monitorId = configDTO.getId();
            String groupId = configDTO.getGroupId();
            boolean groupIsChange = isChangeGroup(groupId, oldBindDTO.getGroupId());
            if (!Objects.equals(monitorId, oldMonitorId) || groupIsChange) {
                updateMonitorGroup(configDTO, oldBindDTO);
            }

            //修改从业人员信息
            String professionalId = Converter.toBlank(configDTO.getProfessionalIds());
            if (!Objects.equals(professionalId, Converter.toBlank(oldBindDTO.getProfessionalIds()))) {
                updateProfessional(configDTO, "update");
            } else {
                //赋值之前的从业人员名称
                configDTO.setProfessionalNames(oldBindDTO.getProfessionalNames());
            }

            //修改服务周期
            String lifecycleId = lifecycleService.update(configDTO, oldBindDTO);
            configDTO.setServiceLifecycleId(lifecycleId);

            //修改信息配置
            BindDTO bindDTO = new BindDTO();
            BeanUtils.copyProperties(configDTO, bindDTO);
            configDTO.setAccessNetwork(getAccessNetwork(bindDTO, oldBindDTO, true));
            updateConfig(bindDTO, oldBindDTO);

            //清除其他缓存
            clearOtherCache(oldBindDTO);

            //配置信息更新事件通知,是为了后面便于比较
            if (!groupIsChange) {
                oldBindDTO.setGroupName(configDTO.getGroupName());
            }
            publisher.publishEvent(new ConfigUpdateEvent(this, bindDTO, oldBindDTO));

            //通知web端,监控对象状态发生改变
            configMessageService.sendToWeb(bindDTO, oldBindDTO);

            //下发到协议端 --只有修改了监控对象、终端以及SIM卡才下发到协议端
            boolean isSend = configMessageService.sendToF3(configDTO, oldBindDTO);

            // 音视频参数设置更改 todo 暂时复用老的接口
            if (isSend) {
                videoChannelSettingDao.delete(oldBindDTO.getId());
                videoParamSettingService.addVideoChannelParam(bindDTO.getTerminalTypeId(), bindDTO.getId());
            }

            //通知storm
            configMessageService.sendToStorm(bindDTO, oldBindDTO);

            //记录日志
            addLog("update", configDTO, oldBindDTO);
        } finally {
            clearUserOperate(configDTO);
        }
        return true;
    }

    /**
     * 字符串拼接
     */
    private String buildNewGroupId(String groupId, Set<String> nonAccessibleGroupIds) {
        StringBuilder groupIdBuilder = new StringBuilder();
        if (StringUtils.isNotEmpty(groupId)) {
            groupIdBuilder.append(groupId).append(",");
        }
        for (String nonAccessibleGroupId : nonAccessibleGroupIds) {
            groupIdBuilder.append(nonAccessibleGroupId).append(",");
        }
        return groupIdBuilder.length() > 0 ? groupIdBuilder.substring(0, groupIdBuilder.length() - 1) :
            groupIdBuilder.toString();
    }

    /**
     * 查询无权访问的分组
     * @param currentGroupIds 当前分组
     * @return 无权访问的分组id
     */
    private Set<String> nonAccessibleGroups(String currentGroupIds) {
        // 只显示自己有权访问的分组
        final RedisKey userGroupKey = RedisKeyEnum.USER_GROUP.of(SystemHelper.getCurrentUsername());
        final Set<String> userGroupIds = RedisHelper.getSet(userGroupKey);
        return Arrays.stream(currentGroupIds.split(",")).filter(StringUtils::isNotBlank)
            .filter(groupId -> !userGroupIds.contains(groupId)).collect(Collectors.toSet());
    }

    /**
     * 清除其他缓存
     * 1、F3使用的
     * 2、未知不确定的缓存，但原有逻辑中有清除的
     * @param oldBindDTO 绑定信息
     */
    private void clearOtherCache(BindDTO oldBindDTO) {
        RedisHelper.delete(HistoryRedisKeyEnum.DEVICE_VEHICLE_INFO.of(oldBindDTO.getDeviceNumber()));
    }

    @Override
    @ImportLock(ImportModule.CONFIG)
    public Map<String, Object> unbind(Collection<String> configIds) throws BusinessException {
        if (hasOtherUserOperate(configIds)) {
            throw new BusinessException("存在正在修改的绑定关系，请刷新确认！");
        }

        Map<String, Object> result = new HashMap<>(16);
        try {
            List<String> monitorIds = configDao.getSortList(configIds);
            if (monitorIds.isEmpty()) {
                result.put("success", false);
                return result;
            }
            //进行解绑
            ConfigUpdateDTO updateDTO = unbindByMonitor(monitorIds);

            //解绑监控对象类型和id集合Map并构建返回结果
            Map<String, Set<BindDTO>> moTypeIdMap = updateDTO.getConfigList().stream()
                .collect(Collectors.groupingBy(BindDTO::getMonitorType, Collectors.toSet()));
            result.put("success", true);
            result.put("vehicleId", StringUtils.join(monitorIds, ","));
            result.put("vehicleFlag", moTypeIdMap.containsKey(MonitorTypeEnum.VEHICLE.getType()));
            result.put("peopleFlag", moTypeIdMap.containsKey(MonitorTypeEnum.PEOPLE.getType()));
            result.put("thingFlag", moTypeIdMap.containsKey(MonitorTypeEnum.THING.getType()));
            // 记录日志
            String operation = configIds.size() > 1 ? "信息列表：批量解除绑定关系" : "信息列表：解除绑定关系";
            String module = configIds.size() > 1 ? "batch" : "more";
            logService.addLog(getIpAddress(), updateDTO.getDeleteMsg().toString(), "3", module, operation);
        } finally {
            ConfigEditUtil.removeEditIds(new ArrayList<>(configIds));
        }
        return result;
    }

    private boolean hasOtherUserOperate(Collection<String> configIds) {
        Set<String> editConfigIds = ConfigEditUtil.getConfigEditIds();
        //求待操作的信息配置ID和编辑中的配置ID的交集
        Set<String> checkIds = new HashSet<>(configIds);
        checkIds.retainAll(editConfigIds);
        if (CollectionUtils.isNotEmpty(checkIds)) {
            return true;
        }
        editConfigIds.addAll(configIds);
        return false;
    }

    @Override
    @ImportLock(ImportModule.CONFIG)
    public ConfigUpdateDTO unbindByMonitor(Collection<String> monitorIds) throws BusinessException {
        if (CollectionUtils.isEmpty(monitorIds)) {
            return null;
        }
        List<BindDTO> bindList = getByMonitorIds(monitorIds, false);
        ConfigUpdateDTO updateDTO = buildUpdateMsg(bindList);
        //1、删除数据库的数据
        //1.1 触发监控对象绑定监听，删除监控对象与其他的绑定，若关联较多，比较耗时 使用线程跑
        pushDeleteEvent(bindList, updateDTO);
        //1.2 删除分组
        groupMonitorService.deleteByMonitorIds(monitorIds, false);
        //1.3 删除从业人员
        configDao.unBindProfessional(updateDTO.getConfigIds());
        //1.4 删除服务周期
        if (CollectionUtils.isNotEmpty(updateDTO.getLifecycleSet())) {
            lifecycleService.delete(updateDTO.getLifecycleSet());
        }
        //1.5 删除信息配置db数据
        configDao.delete(updateDTO.getConfigIds());

        //1.6 原逻辑中在接口中直接删除的以下逻辑使用解绑监听处理
        // 808/809/风险定义设置（deletePlatformSetting）、联动和音视频(deleteLinkageAndVideoSetting)、指令参数

        // 2.等待数据库删除完成后, 删除缓存数据
        try {
            updateDTO.getBatchDeleteSuccess().await(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(msgConfig.getSysErrorMsg());
        }

        //2.1 维护模糊搜索缓存
        RedisHelper.hdel(FUZZY_KEY, updateDTO.getDelFuzzyField());
        RedisHelper.addToHash(FUZZY_KEY, updateDTO.getAddFuzzyMap());

        //2.2 维护企业下未绑定缓存
        RedisHelper.batchAddToSet(updateDTO.getAddOrgUnbindSetMap());
        RedisHelper.batchAddToHash(updateDTO.getAddOrgUnbindHashMap());

        //2.3 维护监控对象信息缓存--删除绑定相关字段，更新bindType未未绑定
        RedisHelper.hdel(updateDTO.getUnBindMonitorKeyList(), monitorFactory.getBindField());
        Map<String, String> bindType = ImmutableMap.of("bindType", Vehicle.BindType.UNBIND);
        RedisHelper.batchAddToHash(updateDTO.getUnBindMonitorKeyList(), bindType);

        //2.4 删除信息配置的顺序缓存中的监控对象Id和协议类型与监控对象的映射缓存
        RedisHelper.delListItem(RedisKeyEnum.CONFIG_SORT_LIST.of(), monitorIds);
        RedisHelper.hdel(updateDTO.getDelHashRedisMap());

        //2.5 通知web端
        configMessageService.sendToWeb(bindList);

        //2.6 批量删除需要删除的缓存
        RedisHelper.deleteForDiffDb(updateDTO.getDelRedisKeyList());
        RedisHelper.batchDelSet(updateDTO.getDelSetRedisMap());

        //2.7 下发到协议端
        configMessageService.sendUnBindToF3(updateDTO.getDeviceUnboundList());

        // 2.8 维护订阅列表
        WebSubscribeManager.getInstance().updateSubStatus(updateDTO.getMonitorIds());

        // 2.9 通知storm
        configMessageService
            .sendUnBindToStorm(updateDTO.getVehicleIds(), updateDTO.getPeopleIds(), updateDTO.getThingIds());
        log.info("信息配置解绑成功");
        return updateDTO;
    }

    @Override
    @ImportLock(ImportModule.CONFIG)
    public boolean delete(String configId) throws BusinessException {
        if (!ConfigEditUtil.putEditId(configId)) {
            throw new BusinessException(msgConfig.getConfigDeleteMsg());
        }
        try {
            List<String> monitorIds = configDao.getSortList(Collections.singletonList(configId));
            if (CollectionUtils.isEmpty(monitorIds)) {
                return false;
            }
            //先进行解绑
            ConfigUpdateDTO updateDTO = unbindByMonitor(monitorIds);
            if (updateDTO == null) {
                return false;
            }

            //删除监控对象
            BindDTO bindDTO = updateDTO.getConfigList().get(0);
            monitorFactory.create(bindDTO.getMonitorType()).delete(bindDTO.getId());

            //删除终端
            deviceService.delete(bindDTO.getDeviceId());

            //删除sim卡
            simCardService.delete(bindDTO.getSimCardId());

        } finally {
            ConfigEditUtil.removeEditId(configId);
        }
        return true;
    }

    @Override
    public ConfigDetailDTO getDetailById(String configId) throws BusinessException {
        ConfigDO configDO = configDao.getById(configId);
        if (Objects.isNull(configDO)) {
            throw new BusinessException("信息配置不存在");
        }

        //获取监控对象信息
        ConfigDetailDTO<BindDTO> configDTO = new ConfigDetailDTO<>();
        BindDTO bindDTO = monitorFactory.create(configDO.getMonitorType()).getById(configDO.getMonitorId());
        if (Objects.isNull(bindDTO)) {
            throw new BusinessException("该信息配置的监控对象不存在");
        }
        bindDTO.setVehiclePassword(configDO.getVehiclePassword());
        bindDTO.setServiceLifecycleId(configDO.getServiceLifecycleId());
        configDTO.setMonitor(bindDTO);

        //获取终端信息
        DeviceDTO deviceDTO = deviceService.findById(configDO.getDeviceId());
        configDTO.setDevice(deviceDTO);

        // 分组信息
        String groupId = bindDTO.getGroupId();
        if (StringUtils.isNotBlank(groupId)) {
            Set<String> groupIds = new HashSet<>(Arrays.asList(groupId.split(",")));
            // 只显示自己有权访问的分组
            final RedisKey userGroupKey = RedisKeyEnum.USER_GROUP.of(SystemHelper.getCurrentUsername());
            final Set<String> userGroupIds = RedisHelper.getSet(userGroupKey);
            groupIds.retainAll(userGroupIds);
            if (CollectionUtils.isNotEmpty(groupIds)) {
                List<GroupDTO> groupList = groupService.getGroupsById(groupIds);
                configDTO.setGroupList(groupList);
                // 前端实际使用的是bindDTO中的分组信息
                StringJoiner groupIdJoiner = new StringJoiner(",");
                StringJoiner groupNameJoiner = new StringJoiner(",");
                for (GroupDTO groupDTO : groupList) {
                    groupIdJoiner.add(groupDTO.getId());
                    groupNameJoiner.add(groupDTO.getName());
                }
                bindDTO.setGroupId(groupIdJoiner.toString());
                bindDTO.setGroupName(groupNameJoiner.toString());

            }
        }

        //从业人员信息
        if (StringUtils.isNotBlank(bindDTO.getProfessionalIds())) {
            List<String> professionalIds = new ArrayList<>(Arrays.asList(bindDTO.getProfessionalIds().split(",")));
            List<ProfessionalDTO> professionals = professionalService.getProfessionalByIds(professionalIds);
            configDTO.setProfessionalList(professionals);
        }

        //SIM卡信息
        configDTO.setSimCard(simCardService.getById(configDO.getSimCardId()));

        return configDTO;
    }

    @Override
    public List<BindDTO> getByMonitorIds(Collection<String> monitorIds, boolean filterGroup) {
        List<RedisKey> redisKeys = RedisKeyEnum.MONITOR_INFO.ofs(monitorIds);
        List<Map<String, String>> monitorMapList = RedisHelper.batchGetHashMap(redisKeys);
        if (CollectionUtils.isEmpty(monitorMapList)) {
            return null;
        }

        //获取用户权限下分组的信息
        Map<String, String> groupMap = filterGroup ? userGroupService.getGroupMap() : new HashMap<>(16);
        List<BindDTO> bindList = new ArrayList<>();
        MonitorBaseService monitorService;
        for (Map<String, String> monitorMap : monitorMapList) {
            BindDTO bindDTO = MapUtil.mapToObj(monitorMap, BindDTO.class);
            if (filterGroup) {
                monitorService = monitorFactory.create(bindDTO.getMonitorType());
                Map filterMap = monitorService.filterGroup(bindDTO.getGroupId(), groupMap);
                if (Objects.nonNull(filterMap.get("groupId"))) {
                    bindDTO.setGroupId(String.valueOf(filterMap.get("groupId")));
                    bindDTO.setGroupName(String.valueOf(filterMap.get("groupName")));
                }
            }
            bindDTO.setPlateColorStr(PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor()));
            bindList.add(bindDTO);
        }
        return bindList;
    }

    @Override
    public BindDTO getByMonitorId(String monitorId) {
        Map<String, String> monitorMap = RedisHelper.hgetAll(RedisKeyEnum.MONITOR_INFO.of(monitorId));
        if (monitorMap == null || monitorMap.isEmpty()) {
            return null;
        }
        return MapUtil.mapToObj(monitorMap, BindDTO.class);
    }

    @Override
    public BindDTO getByConfigId(String configId) {
        ConfigDO configDO = configDao.getById(configId);
        if (Objects.isNull(configDO)) {
            return null;
        }
        return getByMonitorId(configDO.getMonitorId());
    }

    @Override
    public Page<BindDTO> getByKeyword(BasePageQuery configQuery) {
        //模糊搜索到所有的监控对象ID集合
        Set<String> monitorIdSet = getByKeyWord(configQuery.getSimpleQueryParam(), "all");
        //进行排序
        List<String> sortList = sortMonitor(monitorIdSet);

        return getPageList(sortList, configQuery);
    }

    @Override
    public Set<String> getByKeyWord(String keyword, String type) {
        //获取用户权限下的监控对象
        Set<String> monitorIdSet = userService.getCurrentUserMonitorIds();
        if (StringUtils.isBlank(keyword) || CollectionUtils.isEmpty(monitorIdSet)) {
            return monitorIdSet;
        }
        return fuzzyQuery(monitorIdSet, keyword, type);
    }

    @Override
    public List<String> getSortListByKeyWord(String keyword, String type) {
        Set<String> monitorIds = getByKeyWord(keyword, type);
        return sortMonitor(monitorIds);
    }

    @Override
    public Page<BindDTO> getByOrg(String orgId, BasePageQuery query) {
        Set<String> monitorIds = groupMonitorService.getMonitorIdsByOrgId(Collections.singletonList(orgId));
        Set<String> fuzzyIdSet = fuzzyQuery(monitorIds, query.getSimpleQueryParam(), "all");
        //进行排序
        List<String> sortList = sortMonitor(fuzzyIdSet);
        return getPageList(sortList, query);
    }

    @Override
    public Page<BindDTO> getByGroup(String groupId, BasePageQuery query) {
        Set<String> monitorIds = groupMonitorService.getMonitorIdsByGroupId(Collections.singletonList(groupId));
        Set<String> fuzzyIdSet = fuzzyQuery(monitorIds, query.getSimpleQueryParam(), "all");
        List<String> sortList = sortMonitor(fuzzyIdSet);
        return getPageList(sortList, query);
    }

    @Override
    public Page<BindDTO> getByPage(ConfigQuery query) {
        BasePageQuery basePageQuery = new BasePageQuery();
        BeanUtils.copyProperties(query, basePageQuery);
        Page<BindDTO> bindList;
        switch (query.getGroupType()) {
            case "group":
                bindList = getByOrg(query.getGroupName(), basePageQuery);
                break;
            case "assignment":
                bindList = getByGroup(query.getGroupName(), basePageQuery);
                break;
            default:
                bindList = getByKeyword(basePageQuery);
                break;
        }
        return bindList;
    }

    @Override
    public boolean export(HttpServletResponse response, ConfigQuery query) throws Exception {
        query.setPage(1L);
        query.setLimit(1000000L);
        final List<BindDTO> bindList = getByPage(query);
        List<ConfigExportDTO> configExportList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(bindList)) {
            bindList.forEach(o -> configExportList.add(new ConfigExportDTO(o)));
        }

        //写入文件
        ExportExcel export = new ExportExcel(null, ConfigExportDTO.class, 1);
        export.setDataList(configExportList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
        return true;
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        //获取模板所有的表头字段
        List<String> headList = getTemplateHeader();

        //获取所有必填字段
        List<String> requiredList = getRequiredField();

        //获取下拉框的枚举类型值
        Map<String, String[]> selectMap = getSelectMap();

        //获样例数据
        List<Object> exportList = getExampleData(selectMap);

        //写入文件
        ExportExcelUtil.writeTemplateToFile(headList, requiredList, selectMap, exportList, response);
        return true;
    }

    @Override
    @ImportLock(ImportModule.CONFIG)
    public boolean importExcel(MultipartFile file) throws Exception {
        //解析文件
        ImportExcel importExcel;
        try {
            importExcel = new ImportExcel(file, 1, 0);
        } catch (IOException e) {
            throw new BusinessException("解析导入Excel文件异常.");
        }

        //获取用户权限下的企业
        List<OrganizationLdap> orgList = userService.getCurrentUseOrgList();
        if (orgList.isEmpty()) {
            throw new BusinessException("用户无所属企业!");
        }

        //进行最初的参数校验
        ConfigImportHolder holder = getHolder(orgList);
        ImportValidator<ConfigImportDTO> validator = new ConfigImportValidator(monitorFactory, holder);
        importExcel.setImportValidator(validator);
        List<ConfigImportDTO> importList = importExcel.getDataListNew(ConfigImportDTO.class);
        if (CollectionUtils.isEmpty(importList)) {
            throw new BusinessException("导入数据不能为空!");
        }
        //并进行数据大小校验
        if (importList.size() > 10000) {
            throw new BusinessException("最大导入10000条数据!");
        }

        JsonResultBean resultBean = validator.validate(importList, false, orgList);
        if (!resultBean.isSuccess()) {
            ImportErrorUtil.putDataToRedis(importList, ImportModule.CONFIG);
            return false;
        }
        //统一转换成BindDTO便于后续处理
        List<BindDTO> configList = new ArrayList<>();
        importList.forEach(o -> configList.add(o.convertToBindDTO()));
        holder.setImportList(configList);

        List<BaseImportHandler> handlers = getConfigImportHandlers(holder);

        try (ImportCache ignored = new ImportCache(ImportModule.CONFIG, SystemHelper.getCurrentUsername(), handlers)) {
            final boolean isSuccess = handle(handlers, holder, importList);
            if (!isSuccess) {
                return false;
            }
            // 下发清空f3异常注册指令.
            WebSubscribeManager.getInstance().sendMsgToAll("", ConstantUtil.DELETE_F3_INVALID_CACHE);
            if (holder.getImportVehicleNum() > 0) {
                ZMQFencePub.pubChangeFence("1");
            }
            if (holder.getImportPeopleNum() > 0) {
                ZMQFencePub.pubChangeFence("2");
            }
            if (holder.getImportThingNum() > 0) {
                ZMQFencePub.pubChangeFence("17");
            }
        }
        return true;
    }

    @Override
    @ImportLock(ImportModule.CONFIG)
    public JsonResultBean importTransport(MultipartFile file, ProgressDetails progress, HttpServletRequest request)
        throws Exception {
        // 判断当前用户是否有分组的可写权限
        if (!roleService.isGroupWritePower()) {
            return new JsonResultBean(false, "当前用户没有新增分组权限!");
        }

        //读取导入数据模板
        ImportExcel importExcel = new ImportExcel(file, 2, 0);
        short lastCellNum = importExcel.getRow(1).getLastCellNum();
        if (lastCellNum != 43) {
            return new JsonResultBean(false, "货运平台数据模板不正确!");
        }

        //进行数据数据解析读取
        ConfigTransportHolder holder = new ConfigTransportHolder();
        ConfigTransportImportValidator validator =
            new ConfigTransportImportValidator(holder, monitorFactory.getVehicleService(), simCardNewDao, deviceNewDao);
        importExcel.setImportValidator(validator);
        List<ConfigTransportImportDTO> importList = importExcel.getDataListNew(ConfigTransportImportDTO.class);
        if (CollectionUtils.isEmpty(importList)) {
            return new JsonResultBean(false, "导入数据为空");
        }

        //进行参数校验
        List<OrganizationLdap> orgList = userService.getCurrentUseOrgList();
        JsonResultBean resultBean = validator.validate(importList, true, orgList);
        if (!resultBean.isSuccess()) {
            return resultBean;
        }
        progress.addProgress(5);
        holder.setProgress(progress);
        ConfigTransportHandler handler = getTransportHandler(holder, importList);
        try (ImportCache ignored = new ImportCache(ImportModule.CONFIG, SystemHelper.getCurrentUsername(), handler)) {
            JsonResultBean jsonResultBean = handler.execute();
            if (!jsonResultBean.isSuccess()) {
                return new JsonResultBean(JsonResultBean.FAULT, "导入失败");
            }
        }
        int total = holder.getConfigList().size();
        StringBuilder message = new StringBuilder();
        message.append("成功导入").append(total).append("条货运数据");
        logService.addLog(getIpAddress(), message.toString(), "3", "信息配置", "批量导入");
        Set<String> orgSet = holder.getOrgNameSet();
        if (!orgSet.isEmpty()) {
            message.append(";").append("企业【 ").append(Joiner.on(",").join(orgSet)).append("】最多只能有100个分组");
        }
        if (total > 0) {
            ZMQFencePub.pubChangeFence("1");
            // 下发清空f3异常注册指令.
            WebSubscribeManager.getInstance().sendMsgToAll("", ConstantUtil.DELETE_F3_INVALID_CACHE);
        }

        return new JsonResultBean(JsonResultBean.SUCCESS, message.toString());
    }

    private ConfigTransportHandler getTransportHandler(ConfigTransportHolder holder,
        List<ConfigTransportImportDTO> importList) {
        holder.setGroupMaxMonitorNum(configHelper.getMaxNumberAssignmentMonitor());
        holder.setCargoReportSwitch(configHelper.isCargoReportSwitch());
        ConfigTransportHandler handler =
            new ConfigTransportHandler(monitorFactory.getVehicleService(), groupService, groupMonitorService,
                userGroupService, deviceNewDao, simCardNewDao, configDao, lifecycleService, videoParamSettingService);
        handler.setImportList(importList);
        handler.setHolder(holder);
        return handler;
    }

    private boolean handle(List<BaseImportHandler> handlers, ConfigImportHolder holder,
        List<ConfigImportDTO> importList) {
        for (BaseImportHandler handler : handlers) {
            final JsonResultBean result = handler.execute();
            if (!result.isSuccess()) {
                List<BindDTO> configList = holder.getImportList();
                List<String> errorMessages = configList.stream().map(BindDTO::getErrorMsg).collect(Collectors.toList());
                log.info("信息配置导入错误信息：{}", errorMessages);
                for (int i = 0; i < configList.size(); i++) {
                    importList.get(i).setErrorMsg(configList.get(i).getErrorMsg());
                }
                ImportErrorUtil.putDataToRedis(importList, ImportModule.CONFIG);
                return false;
            }
        }
        return true;
    }

    @Override
    public void addOrUpdateRedis(List<BindDTO> bindList) {
        //1.删除监控对象、终端、sim卡、模糊搜索缓存
        Set<String> fuzzyDelFields = new HashSet<>();
        //2.维护信息配置绑定模糊搜索缓存
        int initialCapacity = Math.max((int) (bindList.size() / .75f) + 1, 16);
        Map<String, String> filedValueMap = new HashMap<>(initialCapacity);
        //3.删除的企业下相关监控对象的未绑定缓存(企业数量一般不会多，初始16即可)
        Map<RedisKey, Collection<String>> delOrgUnbindMonitor = new HashMap<>(16);
        //4.删除企业下相关sim卡和终端的未绑定
        Map<RedisKey, Collection<String>> delOrgUnbindSimDevice = new HashMap<>(16);
        //5.绑定监控对象的顺序缓存
        List<String> sortList = new ArrayList<>(bindList.size());
        //6.更新信息监控对象信息缓存
        Map<RedisKey, Map<String, String>> monitorRedisMap = new HashMap<>(initialCapacity);
        //7.维护分组与监控对象绑定的redis缓存
        Integer groupMaxMonitorNun = configHelper.getMaxNumberAssignmentMonitor();
        int groupCapacity = Math.max((int) (bindList.size() / groupMaxMonitorNun / .75f) + 1, 16);
        Map<RedisKey, Collection<String>> groupMonitorSetMap = new HashMap<>(groupCapacity);
        //8.维护终端类型分类的监控对象id和name的映射关系缓存
        Map<RedisKey, Map<String, String>> protocolMap = new HashMap<>(16);
        for (BindDTO config : bindList) {
            String moType = config.getMonitorType();
            String monitorId = config.getId();
            String deviceNum = config.getDeviceNumber();
            String simNum = config.getSimCardNumber();
            //0、对讲特有逻辑，定位对象转换成对讲对象时，只更新信息监控对象缓存的分组信息
            if (Objects.equals(Vehicle.BindType.HAS_BIND, config.getBindType())) {
                Map<String, String> updateInfoMap =
                    ImmutableMap.of("groupId", config.getGroupId(), "groupName", config.getGroupName());
                monitorRedisMap.put(RedisKeyEnum.MONITOR_INFO.of(monitorId), MapUtil.objToMap(updateInfoMap));
                continue;
            }
            //1.删除监控对象、终端、sim卡、模糊搜索缓存
            config.setBindType(Vehicle.BindType.HAS_BIND);
            fuzzyDelFields.add(FuzzySearchUtil.buildMonitorField(moType, config.getName()));
            fuzzyDelFields.add(FuzzySearchUtil.buildDeviceField(deviceNum));
            fuzzyDelFields.add(FuzzySearchUtil.buildSimCardField(simNum));
            //2.维护信息配置绑定模糊搜索缓存
            String fuzzyFiled = FuzzySearchUtil.buildField(moType, config.getName(), deviceNum, simNum);
            String fuzzyValue = FuzzySearchUtil.buildValue(monitorId, config.getDeviceId(), config.getSimCardId());
            filedValueMap.put(fuzzyFiled, fuzzyValue);
            //3.删除的企业下相关监控对象的未绑定缓存
            RedisKey orgUnbindKey = monitorFactory.getOrgUnbindKey(moType, config.getOrgId());
            Collection<String> unbindSet = delOrgUnbindMonitor.getOrDefault(orgUnbindKey, new HashSet<>());
            unbindSet.add(monitorId);
            delOrgUnbindMonitor.put(orgUnbindKey, unbindSet);
            //4.1 删除企业下相关终端的未绑定
            orgUnbindKey = RedisKeyEnum.ORG_UNBIND_DEVICE.of(config.getDeviceOrgId());
            unbindSet = delOrgUnbindSimDevice.getOrDefault(orgUnbindKey, new HashSet<>());
            unbindSet.add(config.getDeviceId());
            delOrgUnbindSimDevice.put(orgUnbindKey, unbindSet);
            //4.2 删除企业下相关sim卡的未绑定
            orgUnbindKey = RedisKeyEnum.ORG_UNBIND_SIM.of(config.getSimCardOrgId());
            unbindSet = delOrgUnbindSimDevice.getOrDefault(orgUnbindKey, new HashSet<>());
            unbindSet.add(config.getDeviceId());
            delOrgUnbindSimDevice.put(orgUnbindKey, unbindSet);
            //5.绑定监控对象的顺序缓存
            sortList.add(monitorId);
            //6.更新信息监控对象信息缓存
            monitorRedisMap.put(RedisKeyEnum.MONITOR_INFO.of(monitorId), MapUtil.objToMap(config));
            //7.维护分组与监控对象绑定的redis缓存
            buildGroupMonitorMap(groupMonitorSetMap, monitorId, config.getGroupId());
            //8.维护终端类型分类的监控对象id和name的映射关系缓存,一般导入的终端类型是相同的,避免rehash，初值按最大来设置
            RedisKey redisKey = RedisKeyEnum.MONITOR_PROTOCOL.of(config.getDeviceType());
            Map<String, String> monitorMap = protocolMap.getOrDefault(redisKey, new HashMap<>(initialCapacity));
            monitorMap.put(monitorId, config.getName());
            protocolMap.put(redisKey, monitorMap);
        }
        //维护redis缓存
        RedisHelper.hdel(FUZZY_KEY, fuzzyDelFields);
        RedisHelper.addToHash(FUZZY_KEY, filedValueMap);
        RedisHelper.hdel(delOrgUnbindMonitor);
        RedisHelper.batchDelSet(delOrgUnbindSimDevice);
        RedisHelper.addToListTop(RedisKeyEnum.CONFIG_SORT_LIST.of(), sortList);
        RedisHelper.batchAddToHash(monitorRedisMap);
        RedisHelper.batchAddToSet(groupMonitorSetMap);
        RedisHelper.batchAddToHash(protocolMap);
    }

    @Override
    public String getRandomMonitorName(String simNum, String monitorType) {
        if (simNum.length() <= 6 || simNum.length() >= 21) {
            return "";
        }
        simNum = simNum.substring(simNum.length() - 5);
        // 设置生成随机数区间
        int minNum = 65;
        int maxNum = 91;
        // A-Z字母随机数
        char randomChar = (char) (new Random().nextInt((maxNum - minNum)) + minNum);
        StringBuffer monitorName = new StringBuffer("扫").append(randomChar).append(simNum);

        //获取以终端手机号后5位相同扫描入库的监控对象名称
        List<String> monitorNames = monitorFactory.create(monitorType).getScanByName(simNum);
        if (monitorNames.size() >= 26) {
            monitorName.append(26);
            return monitorName.toString();
        }
        // 判断是否重复
        while (monitorNames.contains(monitorName.toString())) {
            monitorName = new StringBuffer("");
            randomChar = (char) (new Random().nextInt((maxNum - minNum)) + minNum);
            monitorName.append("扫").append(randomChar).append(simNum);
        }

        return monitorName.toString();
    }

    @Override
    public boolean checkIsBound(String inputId, String inputValue, Integer monitorType) {
        boolean isBind;
        switch (inputId) {
            case "brands":
                BindDTO bindDTO = monitorFactory.create(String.valueOf(monitorType)).getByName(inputValue);
                isBind = Objects.nonNull(bindDTO) && Objects.equals(bindDTO.getBindType(), Vehicle.BindType.HAS_BIND);
                break;
            case "devices":
                DeviceDTO deviceDTO = deviceService.getByNumber(inputValue);
                isBind = Objects.nonNull(deviceDTO) && StringUtils.isNotBlank(deviceDTO.getBindId());
                break;
            case "sims":
                SimCardDTO simCardDTO = simCardService.getByNumber(inputValue);
                isBind = Objects.nonNull(simCardDTO) && StringUtils.isNotBlank(simCardDTO.getConfigId());
                break;
            default:
                isBind = false;
                break;
        }
        return isBind;
    }

    @Override
    public String getConfigId(String monitorId) {
        ConfigDO configDO = configDao.getByMonitorId(monitorId);
        return Objects.isNull(configDO) ? null : configDO.getId();
    }

    @Override
    public List<String> getConfigIds(Collection<String> monitorIds) {
        List<ConfigDO> configList = configDao.getByMonitorIds(monitorIds);
        return configList.stream().map(ConfigDO::getId).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> checkGroupMonitorCount(String id, int type) {
        List<GroupDTO> groupList;
        if (type == 2) {
            groupList = groupService.getMonitorCountOrgId(id);
        } else {
            groupList = new ArrayList<>();
            GroupDTO groupDTO = groupService.getMonitorCountById(id);
            if (Objects.nonNull(groupDTO)) {
                groupList.add(groupDTO);
            }
        }
        List<String> usableGroupIds = new ArrayList<>();
        List<String> unUsableGroupNames = new ArrayList<>();
        for (GroupDTO groupDTO : groupList) {
            if (groupDTO.getMonitorCount() < configHelper.getMaxNumberAssignmentMonitor()) {
                usableGroupIds.add(groupDTO.getId());
            } else {
                unUsableGroupNames.add(groupDTO.getName());
            }
        }
        return ImmutableMap.of("ais", usableGroupIds, "overLimitAssignmentName", unUsableGroupNames);
    }

    @Override
    public List<Map<String, String>> getProfessionalSelect(String configId, String keyword) {
        //获取从业人员下拉框列表
        List<Map<String, String>> professionalList = professionalService.getSelectList(keyword);
        if (StringUtils.isBlank(configId)) {
            return professionalList;
        }

        //获取该信息配置的从业人员
        List<String> professionalIds = configDao.getProfessionalIdByConfigId(configId);
        if (CollectionUtils.isEmpty(professionalIds)) {
            return professionalList;
        }

        //获取下拉框中没有该信息配置的从业人员
        Set<String> professionalIdSet = professionalList.stream().map(o -> o.get("id")).collect(Collectors.toSet());
        List<String> includeIds =
            professionalIds.stream().filter(professionalIdSet::contains).collect(Collectors.toList());
        professionalIds.removeAll(includeIds);
        if (CollectionUtils.isEmpty(professionalIds)) {
            return professionalList;
        }

        List<ProfessionalDTO> professionals = professionalService.getProfessionalByIds(professionalIds);
        for (ProfessionalDTO professional : professionals) {
            professionalList.add(ImmutableMap.of("id", professional.getId(), "name", professional.getName(),
                    "identity", professional.getIdentity()));
        }
        return professionalList;
    }

    private List<BaseImportHandler> getConfigImportHandlers(ConfigImportHolder holder) {
        return Lists.newArrayList(new ConfigVehicleImportHandler(holder, monitorFactory.getVehicleService()),
            new ConfigPeopleImportHandler(holder, monitorFactory.getPeopleService()),
            new ConfigThingImportHandler(holder, monitorFactory.getThingService()),
            new ConfigDeviceImportHandler(holder, deviceService, deviceNewDao),
            new ConfigSimCardImportHandler(holder, simCardService, simCardNewDao),
            new ConfigGroupImportHandler(holder, groupService, groupMonitorService, userGroupService),
            new ConfigImportHandler(holder, configDao, lifecycleService, groupMonitorService, professionalsDao, this),
            new ConfigVideoChannelHandler(holder, deviceNewDao, videoChannelSettingDao));
    }

    /**
     * 导入的一些初始值准备
     * @param orgList 用户权限下的企业列表
     * @return ConfigImportHolder
     */
    private ConfigImportHolder getHolder(List<OrganizationLdap> orgList) {
        Map<String, String> orgNameIdMap = new HashMap<>(CommonUtil.ofMapCapacity(orgList.size()));
        Map<String, String> orgIdNameMap = new HashMap<>(CommonUtil.ofMapCapacity(orgList.size()));
        for (OrganizationLdap organizationLdap : orgList) {
            if (Objects.equals("organization", organizationLdap.getOu())) {
                continue;
            }
            orgNameIdMap.put(organizationLdap.getName(), organizationLdap.getUuid());
            orgIdNameMap.put(organizationLdap.getUuid(), organizationLdap.getName());
        }
        ConfigImportHolder holder = new ConfigImportHolder();
        holder.setOrgMap(orgNameIdMap);
        holder.setOrgIdNameMap(orgIdNameMap);
        holder.setGroupMaxMonitorNum(configHelper.getMaxNumberAssignmentMonitor());
        List<TerminalTypeInfo> terminalTypeList = deviceNewDao.getAllTerminalType();
        Map<String, String> terminalTypeMap = new HashMap<>(Math.max((int) (terminalTypeList.size() / .75f) + 1, 16));
        terminalTypeList.forEach(
            type -> terminalTypeMap.put(type.getTerminalManufacturer() + "_" + type.getTerminalType(), type.getId()));
        holder.setTerminalTypeMap(terminalTypeMap);
        holder.setTerminalTypeInfoList(terminalTypeList);
        return holder;
    }

    private void buildGroupMonitorMap(Map<RedisKey, Collection<String>> groupMap, String monitorId, String groupIds) {
        String[] groupIdArr = groupIds.split(",");
        RedisKeyEnum redisKeyEnum = RedisKeyEnum.GROUP_MONITOR;
        for (String groupId : groupIdArr) {
            Collection<String> monitorSet = groupMap.getOrDefault(redisKeyEnum.of(groupId), new HashSet<>());
            monitorSet.add(monitorId);
            groupMap.put(redisKeyEnum.of(groupId), monitorSet);
        }
    }

    /**
     * 更新监控对象分组信息
     * @param bindDTO    当前绑定信息
     * @param oldBindDTO 修改前绑定信息
     */
    private void updateMonitorGroup(ConfigDTO bindDTO, BindDTO oldBindDTO) {
        Set<String> monitorIds = new HashSet<>();
        if (Objects.nonNull(oldBindDTO)) {
            monitorIds.add(oldBindDTO.getId());
        }
        monitorIds.add(bindDTO.getId());
        groupMonitorService.deleteByMonitorIds(monitorIds, true);

        //分组ID进行去重
        List<GroupMonitorDO> groupMonitorList = new ArrayList<>();
        String userName = SystemHelper.getCurrentUsername();
        //使用hashSet进行去重
        List<GroupDTO> groupList = groupService.getGroupsById(Arrays.asList(bindDTO.getGroupId().split(",")));
        int knobNo = 0;
        List<String> groupIds = new ArrayList<>();
        List<String> groupNames = new ArrayList<>();
        for (GroupDTO groupDTO : groupList) {
            GroupMonitorDO groupMonitorDO =
                new GroupMonitorDO(bindDTO.getId(), bindDTO.getMonitorType(), groupDTO.getId());
            groupMonitorDO.setCreateDataUsername(userName);
            if (StringUtils.isNotBlank(bindDTO.getIntercomDeviceNumber())) {
                knobNo++;
                groupMonitorDO.setKnobNo(knobNo);
            }
            groupMonitorList.add(groupMonitorDO);
            groupIds.add(groupDTO.getId());
            groupNames.add(groupDTO.getName());
        }
        if (!groupMonitorList.isEmpty()) {
            groupMonitorService.add(groupMonitorList, true);
        }
        bindDTO.setGroupId(StringUtils.join(groupIds, ","));
        bindDTO.setGroupName(StringUtils.join(groupNames, ","));
    }

    /**
     * 添加设备类型
     * @param bindDTO 绑定信息
     * @param userOrgIds 用户拥有的组织ID集合
     */
    private void addDevice(ConfigDTO bindDTO, Collection<String> userOrgIds) throws BusinessException {
        bindDTO.setDeviceId(UUID.randomUUID().toString());
        // 根据终端编号查找终端信息
        DeviceDTO deviceDTO = deviceService.getByNumber(bindDTO.getDeviceNumber());

        //若终端不存在，添加新的终端
        if (Objects.isNull(deviceDTO)) {
            deviceDTO = deviceService.getDefaultInfo(bindDTO);
            deviceService.add(deviceDTO);
        }
        if (StringUtils.isNotBlank(deviceDTO.getBindId()) || !userOrgIds.contains(deviceDTO.getOrgId())) {
            throw new BusinessException("不好意思，你来晚了！终端号【" + bindDTO.getDeviceNumber() + "】已被使用");
        }
        //补全bindDTO里终端相关信息，用于维护绑定缓存信息
        bindDTO.setDeviceOrgId(deviceDTO.getOrgId());
        bindDTO.setDeviceType(deviceDTO.getDeviceType());
        bindDTO.setFunctionalType(deviceDTO.getFunctionalType());
        bindDTO.setTerminalTypeId(deviceDTO.getTerminalTypeId());
        bindDTO.setTerminalType(deviceDTO.getTerminalType());
        bindDTO.setTerminalManufacturer(deviceDTO.getTerminalManufacturer());
        bindDTO.setDeviceId(deviceDTO.getId());
        bindDTO.setIsVideo(deviceDTO.getIsVideo());
        bindDTO.setManufacturerId(deviceDTO.getManufacturerId());
    }

    /**
     * 添加sim卡
     * @param bindDTO 绑定信息
     * @param userOrgIds 用户拥有的组织ID集合
     */
    private void addSimCard(ConfigDTO bindDTO, Collection<String> userOrgIds) throws BusinessException {
        //根据sim卡号查询sim卡
        SimCardDTO simCardDTO = simCardService.getByNumber(bindDTO.getSimCardNumber());
        //若不存在，根据绑定信息构造sim卡默认信息，并进行SIM卡添加
        if (Objects.isNull(simCardDTO)) {
            simCardDTO = simCardService.getDefaultInfo(bindDTO);
            simCardService.add(simCardDTO);
        }
        if (StringUtils.isNotBlank(simCardDTO.getConfigId()) || !userOrgIds.contains(simCardDTO.getOrgId())) {
            throw new BusinessException("不好意思，你来晚了！终端手机号【" + bindDTO.getSimCardNumber() + "】已被使用");
        }
        //封装绑定信息里面SIM卡的详情信息，用于缓存
        bindDTO.setSimCardOrgId(simCardDTO.getOrgId());
        bindDTO.setSimCardId(simCardDTO.getId());
        bindDTO.setRealSimCardNumber(simCardDTO.getRealId());
        bindDTO.setAuthCode(simCardDTO.getAuthCode());
    }

    private void removeUnBind(BindDTO bindDTO) {
        //删除监控对象绑定前的模糊搜索缓存和企业下未绑定的监控对象
        String monitorType = bindDTO.getMonitorType();
        RedisHelper.hdel(FUZZY_KEY, FuzzySearchUtil.buildMonitorField(monitorType, bindDTO.getName()));
        RedisHelper.hdel(monitorFactory.getOrgUnbindKey(monitorType, bindDTO.getOrgId()), bindDTO.getId());

        //删除终端的绑定前的模糊搜索缓存和企业下未绑定终端
        RedisHelper.hdel(FUZZY_KEY, FuzzySearchUtil.buildDeviceField(bindDTO.getDeviceNumber()));
        RedisHelper.delSetItem(RedisKeyEnum.ORG_UNBIND_DEVICE.of(bindDTO.getDeviceOrgId()), bindDTO.getDeviceId());

        //删除sim卡绑定前的模糊搜索缓存和企业下未绑定SIM卡
        RedisHelper.hdel(FUZZY_KEY, FuzzySearchUtil.buildSimCardField(bindDTO.getSimCardNumber()));
        RedisHelper.delSetItem(RedisKeyEnum.ORG_UNBIND_SIM.of(bindDTO.getSimCardOrgId()), bindDTO.getSimCardId());
    }

    private void updateProfessional(ConfigDTO configDTO, String operation) {
        String configId = configDTO.getConfigId();
        if ("update".equals(operation)) {
            // 删除信息配置原有的从业人员
            configDao.unBindProfessional(Collections.singletonList(configId));
        }

        if (StringUtils.isBlank(configDTO.getProfessionalIds())) {
            return;
        }
        String[] professionalIds = configDTO.getProfessionalIds().split(",");

        List<ProfessionalDTO> professionals = professionalService.getProfessionalByIds(Arrays.asList(professionalIds));
        List<ConfigProfessionalDO> professionalList = new ArrayList<>();
        List<String> professionalNames = new ArrayList<>();
        List<String> professionalIdList = new ArrayList<>();
        for (ProfessionalDTO professional : professionals) {
            ConfigProfessionalDO configProfessional = new ConfigProfessionalDO(configId, professional.getId());
            configProfessional.setCreateDataUsername(SystemHelper.getCurrentUsername());
            professionalList.add(configProfessional);
            professionalIdList.add(professional.getId());
            professionalNames.add(professional.getName());
        }

        if (professionalList.isEmpty()) {
            configDTO.setProfessionalIds(null);
            return;
        }

        configDao.bindProfessional(professionalList);

        //保持从业人员的ID和名称字符串顺序是一一对应的映射关系
        configDTO.setProfessionalIds(StringUtils.join(professionalIdList, ","));
        configDTO.setProfessionalNames(StringUtils.join(professionalNames, ","));
        String cardNum = professionals.isEmpty() ? null : professionals.get(0).getCardNumber();
        configDTO.setCardNumber(cardNum);

    }

    private void addConfig(BindDTO bindDTO) {
        //信息配置绑定信息入库
        ConfigDO configDO = new ConfigDO(bindDTO);
        configDO.setVehiclePassword("000000");
        configDao.insert(configDO);

        //删除相关的未绑定缓存
        removeUnBind(bindDTO);

        //维护信息缓存以及顺序缓存
        String monitorId = bindDTO.getId();
        RedisHelper.addToListTop(RedisKeyEnum.CONFIG_SORT_LIST.of(), monitorId);
        Map<String, String> bindMap = MapUtil.objToMap(bindDTO);
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(monitorId), bindMap);

        //维护模糊搜索缓存
        addFuzzyCache(bindDTO);

        //维护终端类型分类的监控对象id和name的映射关系缓存
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_PROTOCOL.of(bindDTO.getDeviceType()), monitorId, bindDTO.getName());
    }

    private void addLog(String operation, ConfigDTO bindDTO, BindDTO oldBind) {
        String ip = getIpAddress();
        String msg;
        String name = bindDTO.getName();
        String deviceNum = bindDTO.getDeviceNumber();
        String simNum = bindDTO.getSimCardNumber();
        String plateColor = Objects.isNull(bindDTO.getPlateColor()) ? "" : String.valueOf(bindDTO.getPlateColor());
        switch (operation) {
            case "add":
                String inputType = Objects.nonNull(bindDTO.getInputType()) ? bindDTO.getInputType().getName() : null;
                if (StringUtils.isNotBlank(inputType)) {
                    msg = String.format("%s：添加 %s(监控对象),%s(终端号)%s(终端手机号)的绑定关系", inputType, name, deviceNum, simNum);
                    logService.addLog(ip, msg, "3", "more", inputType + "：添加绑定关系");
                }
                break;
            case "update":
                Objects.requireNonNull(oldBind);
                boolean monitorNoChange = Objects.equals(name, oldBind.getName());
                boolean deviceNoChange = Objects.equals(deviceNum, oldBind.getDeviceNumber());
                boolean groupNoChange = !isChangeGroup(bindDTO.getGroupId(), oldBind.getGroupId());
                boolean simNoChange = Objects.equals(simNum, oldBind.getSimCardNumber());
                String formatStr = "监控对象(%s)/分组(%s)/终端(%s)/终端手机号(%s)";
                msg = "修改绑定关系 " + String.format(formatStr, name, bindDTO.getGroupName(), deviceNum, simNum);
                if (!(monitorNoChange && deviceNoChange && groupNoChange && simNoChange)) {
                    String updateMsg = String
                        .format(formatStr, oldBind.getName(), oldBind.getGroupName(), oldBind.getDeviceNumber(),
                            oldBind.getSimCardNumber());
                    msg = msg + "为" + updateMsg;
                }
                logService.addLog(ip, msg, "3", "", name, plateColor);
                break;
            default:
                msg = null;
                break;
        }
    }

    private void updateConfig(BindDTO curBind, BindDTO oldBind) {
        ConfigDO configDO = new ConfigDO(curBind);

        //处理车辆密码
        String vehiclePassword = configDO.getVehiclePassword();
        if (StringUtils.isBlank(vehiclePassword) || vehiclePassword.length() > 6) {
            configDO.setVehiclePassword("");
        }

        configDO.setAccessNetwork(getAccessNetwork(curBind, oldBind, false));
        configDao.update(configDO);

        if (Objects.equals(curBind.getMonitorType(), MonitorTypeEnum.VEHICLE.getType())) {
            log.info("更新信息配置模块：车辆--分组关系：车辆id:{}, 分组id:{},操作用户：{}", curBind.getId(), curBind.getGroupId(),
                configDO.getUpdateDataUsername());
        }
        String monitorId = curBind.getId();
        if (!Objects.equals(monitorId, oldBind.getId())) {
            this.deleteBinding(Collections.singletonList(oldBind), "update", getIpAddress());
            //若监控对象的ID进行了改变，从新维护顺序缓存
            List<String> sortList = RedisHelper.getList(RedisKeyEnum.CONFIG_SORT_LIST.of());
            //老的监控对象Id的位置替换成新的监控对象ID
            for (int i = 0; i < sortList.size(); i++) {
                if (Objects.equals(oldBind.getId(), sortList.get(i))) {
                    sortList.set(i, monitorId);
                    break;
                }
            }
            RedisHelper.delete(RedisKeyEnum.CONFIG_SORT_LIST.of());
            RedisHelper.addToListTail(RedisKeyEnum.CONFIG_SORT_LIST.of(), sortList);
        }

        //维护监控对象绑定信息缓存
        deleteFuzzyCache(oldBind);
        addFuzzyCache(curBind);

        //维护当前绑定监控对象的缓存
        curBind.setBindDate(oldBind.getBindDate());
        RedisHelper.hdel(RedisKeyEnum.MONITOR_INFO.of(monitorId), monitorFactory.getBindField());
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(monitorId), MapUtil.objToMap(curBind));

        //维护终端类型与监控对象映射关系的缓存
        RedisHelper.hdel(RedisKeyEnum.MONITOR_PROTOCOL.of(oldBind.getDeviceType()), oldBind.getId());
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_PROTOCOL.of(curBind.getDeviceType()), monitorId, curBind.getName());

    }

    private Integer getAccessNetwork(BindDTO curBind, BindDTO oldBind, boolean isGetOld) {
        //处理监控对象入网标识,若监控对象id、sim卡id或终端id发生便跟
        if (!Objects.equals(curBind.getId(), oldBind.getId()) || !Objects
            .equals(curBind.getSimCardId(), oldBind.getSimCardId()) || !Objects
            .equals(curBind.getDeviceId(), oldBind.getDeviceId())) {
            return 0;

        }
        if (isGetOld) {
            return configDao.getById(curBind.getConfigId()).getAccessNetwork();
        }
        return null;
    }

    private void addFuzzyCache(BindDTO bindDTO) {
        String fuzzyField = FuzzySearchUtil
            .buildField(bindDTO.getMonitorType(), bindDTO.getName(), bindDTO.getDeviceNumber(),
                bindDTO.getSimCardNumber());
        String fuzzyValue = FuzzySearchUtil.buildValue(bindDTO.getId(), bindDTO.getDeviceId(), bindDTO.getSimCardId());
        RedisHelper.addToHash(FUZZY_KEY, fuzzyField, fuzzyValue);
    }

    private void deleteFuzzyCache(BindDTO bindDTO) {
        String oldFuzzyField = FuzzySearchUtil
            .buildField(bindDTO.getMonitorType(), bindDTO.getName(), bindDTO.getDeviceNumber(),
                bindDTO.getSimCardNumber());
        RedisHelper.hdel(FUZZY_KEY, oldFuzzyField);
    }

    /**
     * 删除监控对象与其他绑定关系
     */
    private void deleteBinding(Collection<BindDTO> bindList, String opration, String ipAddress) {
        String currentUsername = SystemHelper.getCurrentUsername();
        String orgId = userService.getCurrentUserOrg().getUuid();
        publisher.publishEvent(new ConfigUnBindEvent(this, bindList, opration, ipAddress, currentUsername, orgId));
    }

    private boolean isChangeGroup(String curGroupId, String oldGroupId) {
        String[] curGroupIds = curGroupId.split(",");
        Set<String> curGroupIdSet = new HashSet<>();
        for (String groupId : curGroupIds) {
            if (StringUtils.isBlank(groupId)) {
                continue;
            }
            curGroupIdSet.add(groupId);
        }
        Set<String> oldGroupIds = new HashSet<>(Arrays.asList(oldGroupId.split(",")));
        Set<String> oldGroupIdSet =
            oldGroupIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        if (!Objects.equals(curGroupIdSet.size(), oldGroupIdSet.size())) {
            return true;
        }
        for (String groupId : curGroupIdSet) {
            if (!oldGroupIdSet.contains(groupId)) {
                return true;
            }
        }
        return false;
    }

    private void updateDevice(ConfigDTO bindDTO, BindDTO oldBind) throws BusinessException {
        String deviceNum = bindDTO.getDeviceNumber();
        String deviceId = bindDTO.getDeviceId();
        boolean updateDevice = StringUtils.isNotBlank(deviceNum) && !deviceNum.equals(oldBind.getDeviceNumber());
        if (Objects.equals(deviceId, oldBind.getDeviceId()) && updateDevice) {
            deviceService.updateNumber(deviceId, deviceNum);
        }

        // 根据ID获取设备信息
        DeviceDTO deviceDTO = deviceService.findById(bindDTO.getDeviceId());
        if (Objects.isNull(deviceDTO)) {
            throw new BusinessException("该终端已删除，请确认");
        }

        //补全bindDTO里终端相关信息，用于维护绑定缓存信息
        bindDTO.setDeviceOrgId(deviceDTO.getOrgId());
        bindDTO.setDeviceType(deviceDTO.getDeviceType());
        bindDTO.setTerminalType(deviceDTO.getTerminalType());
        bindDTO.setTerminalManufacturer(deviceDTO.getTerminalManufacturer());
        bindDTO.setFunctionalType(deviceDTO.getFunctionalType());
        bindDTO.setTerminalTypeId(deviceDTO.getTerminalTypeId());
        bindDTO.setDeviceNumber(deviceDTO.getDeviceNumber());
        bindDTO.setIsVideo(deviceDTO.getIsVideo());
        bindDTO.setManufacturerId(deviceDTO.getManufacturerId());

        //若从一个存在的终端修改成另外一个存在的终端，维护未绑定信息
        String oldDeviceId = oldBind.getDeviceId();
        if (!Objects.equals(deviceId, oldDeviceId)) {
            //取消当前终端的未绑定缓存
            RedisHelper.hdel(FUZZY_KEY, FuzzySearchUtil.buildDeviceField(bindDTO.getDeviceNumber()));
            RedisHelper.delSetItem(RedisKeyEnum.ORG_UNBIND_DEVICE.of(bindDTO.getDeviceOrgId()), deviceId);

            //维护原来绑定终端的未绑定信息
            RedisHelper.addToHash(FUZZY_KEY, FuzzySearchUtil.buildDevice(oldBind.getDeviceNumber(), oldDeviceId));
            RedisHelper.addToSet(RedisKeyEnum.ORG_UNBIND_DEVICE.of(oldBind.getDeviceOrgId()), oldDeviceId);
        }
    }

    private void updateSimCard(ConfigDTO bindDTO, BindDTO oldBind) throws BusinessException {
        String curSimCardId = bindDTO.getSimCardId();
        String oldSimCardId = oldBind.getSimCardId();

        String newSim = bindDTO.getSimCardNumber();
        String oldSim = oldBind.getSimCardNumber();
        if (Objects.equals(curSimCardId, oldSimCardId)) {
            boolean realNumChange = !Objects.equals(bindDTO.getRealSimCardNumber(), oldBind.getRealSimCardNumber());
            boolean simNumChange = StringUtils.isNotBlank(newSim) && !newSim.equals(oldSim);
            if (realNumChange || simNumChange) {
                simCardService.updateNumber(curSimCardId, newSim, bindDTO.getRealSimCardNumber());
                publisher.publishEvent(new SimNumberUpdateEvent(oldSim, newSim)); //修改SIM卡号
            }
        }

        SimCardDTO simCardDTO = simCardService.getById(curSimCardId);
        if (Objects.isNull(simCardDTO)) {
            throw new BusinessException("该sim卡已删除，请确认");
        }
        bindDTO.setRealSimCardNumber(simCardDTO.getRealId());
        bindDTO.setSimCardOrgId(simCardDTO.getOrgId());
        bindDTO.setSimCardNumber(simCardDTO.getSimcardNumber());
        bindDTO.setAuthCode(simCardDTO.getAuthCode());
        if (Objects.equals(curSimCardId, oldSimCardId)) {
            return;
        }

        newSim = bindDTO.getSimCardNumber();
        //取消当前sim卡的未绑定模糊搜索和企业下未绑定
        RedisHelper.hdel(FUZZY_KEY, FuzzySearchUtil.buildSimCardField(newSim));
        RedisHelper.delSetItem(RedisKeyEnum.ORG_UNBIND_SIM.of(bindDTO.getSimCardOrgId()), curSimCardId);
        //历史sim卡取消绑定，维护对应的未绑定缓存
        RedisHelper.addToHash(FUZZY_KEY, FuzzySearchUtil.buildSimCard(oldSim, oldSimCardId));
        RedisHelper.addToSet(RedisKeyEnum.ORG_UNBIND_SIM.of(oldBind.getSimCardOrgId()), oldSimCardId);
        publisher.publishEvent(new SimNumberUpdateEvent(oldSim, newSim)); //修改绑定关系的SIM对象，SIM卡号前后不一致
    }

    /**
     * 检查信息配置是否有其他用户在操作
     * 以全局变量的方式暂时处理多用户同时修改造成的问题，有更好的处理方法时可优化
     * 与原来的逻辑保持一致--- 历史逻辑使用字段，在web端的修改传递的值都是Id
     * @param bindDTO 绑定信息
     * @throws BusinessException 异常
     */
    private void checkOtherUserOperate(ConfigDTO bindDTO) throws BusinessException {
        if (!ConfigEditUtil.putEditId(bindDTO.getConfigId())) {
            throw new BusinessException("", msgConfig.getConfigDeleteMsg());
        }
        if (!ConfigEditUtil.putEditBrand(bindDTO.getId() + "-" + bindDTO.getMonitorType())) {
            throw new BusinessException("", msgConfig.getConfigDeleteBrandMsg());
        }
        if (!ConfigEditUtil.putEditSim(bindDTO.getSimCardId())) {
            throw new BusinessException("", msgConfig.getConfigDeleteSimMsg());
        }
        if (!ConfigEditUtil.putEditDevice(bindDTO.getDeviceId())) {
            throw new BusinessException("", msgConfig.getConfigDeleteDeviceMsg());
        }
    }

    /**
     * 清除用户操作缓存
     * @param bindDTO 绑定信息
     */
    private void clearUserOperate(ConfigDTO bindDTO) {
        ConfigEditUtil.removeEditId(bindDTO.getConfigId());
        ConfigEditUtil.removeEditBrand(bindDTO.getId() + "-" + bindDTO.getMonitorType());
        ConfigEditUtil.removeEditSim(bindDTO.getSimCardId());
        ConfigEditUtil.removeEditDevice(bindDTO.getDeviceId());
    }

    private void pushDeleteEvent(List<BindDTO> bindList, ConfigUpdateDTO updateDTO) {
        String ipAddress = getIpAddress();
        try {
            deleteBinding(bindList, "unbind", ipAddress);
        } finally {
            updateDTO.countDown();
        }
    }

    private ConfigUpdateDTO buildUpdateMsg(List<BindDTO> bindList) {
        ConfigUpdateDTO configUpdateDTO = new ConfigUpdateDTO();
        configUpdateDTO.setConfigList(bindList);
        String logFormat = "解除绑定关系 ： 监控对象(%s)/分组(%s)/终端(%s)/终端手机号(%s) <br/>";
        for (BindDTO config : bindList) {
            String monitorId = config.getId();
            String monitorName = config.getName();
            String moType = config.getMonitorType();

            // 监控对象解绑 -- 维护模糊搜索、企业下未绑定及监控对象信息缓存
            configUpdateDTO.addUnBindMonitorKey(monitorId);
            configUpdateDTO.putToAddFuzzyMap(FuzzySearchUtil.buildMonitor(moType, monitorId, monitorName));
            RedisKey moUnbindKey = monitorFactory.getOrgUnbindKey(moType, config.getOrgId());
            configUpdateDTO.putToAddOrgUnbindHashMap(moUnbindKey, monitorId, monitorFactory.getUnbindValue(config));

            //终端解绑-维护模糊搜索、企业下未绑定缓存
            String deviceId = config.getDeviceId();
            String deviceNum = config.getDeviceNumber();
            configUpdateDTO.putToAddFuzzyMap(FuzzySearchUtil.buildDevice(deviceNum, deviceId));
            configUpdateDTO.putToOrgUnbindSetMap(RedisKeyEnum.ORG_UNBIND_DEVICE.of(config.getDeviceOrgId()), deviceId);

            //sim卡解绑---维护未绑定SIM卡的模糊搜索和企业下未绑定缓存
            String simNum = config.getSimCardNumber();
            String simId = config.getSimCardId();
            configUpdateDTO.putToAddFuzzyMap(FuzzySearchUtil.buildSimCard(simNum, simId));
            configUpdateDTO.putToOrgUnbindSetMap(RedisKeyEnum.ORG_UNBIND_SIM.of(config.getSimCardOrgId()), simId);

            // 分组解绑-- 删除分组与监控对象的缓存和db
            configUpdateDTO.addMonitor(monitorId, moType);
            String[] groupArr = Converter.toBlank(config.getGroupId()).split(",");
            for (String groupId : groupArr) {
                configUpdateDTO.addDelSetRedisMap(RedisKeyEnum.GROUP_MONITOR.of(groupId), monitorId);
            }

            // 服务周期
            if (StringUtils.isNotBlank(config.getServiceLifecycleId())) {
                configUpdateDTO.addLifecycle(config.getServiceLifecycleId());
            }
            //从业人员
            configUpdateDTO.addConfig(config.getConfigId());
            //信息配置本身相关缓存
            String deviceType = config.getDeviceType();
            configUpdateDTO.addDelFuzzyField(FuzzySearchUtil.buildField(moType, monitorName, deviceNum, simNum));
            configUpdateDTO.addDelHashRedisMap(RedisKeyEnum.MONITOR_PROTOCOL.of(deviceType), monitorId);

            //删除F3相关缓存--最后一条告警和位置信息、根据终端号的绑定信息
            configUpdateDTO.addDeleteRedis(HistoryRedisKeyEnum.MONITOR_LOCATION.of(monitorId));
            configUpdateDTO.addDeleteRedis(HistoryRedisKeyEnum.DEVICE_VEHICLE_INFO.of(deviceNum));
            configUpdateDTO.addDeleteRedis(HistoryRedisKeyEnum.MONITOR_LAST_ALARM.of(monitorId));
            configUpdateDTO.addDeleteRedis(HistoryRedisKeyEnum.MONITOR_STATUS.of(monitorId));

            // 通知F3解绑信息
            DeviceUnbound unbound = configMessageService.getDeviceUnbound(deviceId, deviceNum, deviceType, simNum);
            configUpdateDTO.addDeviceUnbound(unbound);
            configUpdateDTO.addDeleteRedis(HistoryRedisKeyEnum.DEVICE_BIND.of(unbound.getIdentification(), deviceType));

            //联动策略相关
            if (Objects.equals(moType, MonitorTypeEnum.VEHICLE.getType())) {
                configUpdateDTO.addDeleteRedis(HistoryRedisKeyEnum.MONITOR_SEND_ALARM.of(monitorId));
            }

            configUpdateDTO.addDeleteRedis(HistoryRedisKeyEnum.SUB_ONE_DEVICE_ID.of(deviceId));

            //日志
            configUpdateDTO.addLog(String.format(logFormat, monitorName, config.getGroupName(), deviceNum, simNum));
        }
        return configUpdateDTO;
    }

    private Page<BindDTO> getPageList(List<String> sortList, BasePageQuery query) {
        Page<BindDTO> result = new Page<>(query.getPage().intValue(), query.getLimit().intValue());

        if (CollectionUtils.isEmpty(sortList)) {
            return result;
        }
        int start = result.getStartRow();
        int end = Math.min(result.getEndRow(), sortList.size());

        if (start > end) {
            return RedisQueryUtil.getListToPage(new ArrayList<>(), query, sortList.size());
        }

        List<String> subList = sortList.subList(start, end);
        List<BindDTO> bindDTOS = getByMonitorIds(subList, true);
        return RedisQueryUtil.getListToPage(bindDTOS, query, sortList.size());
    }

    private List<String> sortMonitor(Set<String> monitorIdSet) {
        if (CollectionUtils.isEmpty(monitorIdSet)) {
            return null;
        }
        List<String> sortList = RedisHelper.getList(RedisKeyEnum.CONFIG_SORT_LIST.of());
        if (CollectionUtils.isEmpty(sortList)) {
            return null;
        }
        List<String> filterSortIds = new ArrayList<>();
        for (String id : sortList) {
            if (monitorIdSet.contains(id)) {
                filterSortIds.add(id);
            }
        }
        return filterSortIds;
    }

    /**
     * @param userOwnIds 用户权限下或按某个条件查询出来的监控对象ID集合
     * @param keyword    关键字（监控对象名称） 可为空
     * @param type       monitor 根据监控对象名字查找， all 根据监控对象、sim卡终端查找
     * @return 模糊搜索出的结果
     */
    private Set<String> fuzzyQuery(Set<String> userOwnIds, String keyword, String type) {
        if (CollectionUtils.isEmpty(userOwnIds)) {
            return null;
        }
        if (StringUtils.isBlank(keyword)) {
            return userOwnIds;
        }
        //根据关键字搜索绑定的监控对象
        Set<String> fuzzyIdSet;
        if (Objects.equals("monitor", type)) {
            fuzzyIdSet = FuzzySearchUtil.scanByMonitor(null, keyword, Vehicle.BindType.HAS_BIND);
        } else {
            fuzzyIdSet = FuzzySearchUtil.scanBindMonitor(keyword);
        }

        //求两个集合的并集
        fuzzyIdSet.retainAll(userOwnIds);
        return fuzzyIdSet;
    }

    private List<Object> getExampleData(Map<String, String[]> selectMap) {
        List<Object> exportList = new ArrayList<>();
        exportList.add("名称");
        exportList.add("车");
        exportList.add("黄色");
        String[] orgName = selectMap.get("所属企业");
        exportList.add(orgName == null || orgName.length == 0 ? "zwkj" : orgName[0]);
        exportList.add("分组名称");
        exportList.add("20160808001");
        exportList.add("交通部JT/T808-2013");
        exportList.add("[f]F3");
        exportList.add("F3-default");
        exportList.add("简易型车机");
        exportList.add("13562584562");
        exportList.add("13562584566");
        String date = DateUtil.formatDate(new Date(), DateUtil.DATE_Y_M_D_FORMAT);
        exportList.add(date);
        exportList.add(date);
        exportList.add("张三");
        return exportList;
    }

    private Map<String, String[]> getSelectMap() {
        Map<String, String[]> selectMap = new HashMap<>(16);
        String[] orgNames =
            userService.getCurrentUseOrgList().stream().filter(ldap -> !"organization".equals(ldap.getOu()))
                .map(OrganizationLdap::getName).toArray(String[]::new);
        selectMap.put("所属企业", orgNames);

        String[] deviceTypes = ProtocolEnum.DEVICE_TYPE_NAMES.toArray(new String[0]);
        selectMap.put("通讯类型", deviceTypes);

        String[] monitorType = { "车", "人", "物" };
        selectMap.put("监控对象类型", monitorType);

        String[] functionalType = { "简易型车机", "行车记录仪", "对讲设备", "手咪设备", "超长待机设备", "定位终端" };
        selectMap.put("功能类型", functionalType);

        //终端型号
        List<TerminalTypeInfo> terminalTypes = deviceNewDao.getAllTerminalType();
        String[] terminalTypeArr =
            terminalTypes.stream().map(TerminalTypeInfo::getTerminalType).distinct().toArray(String[]::new);
        selectMap.put("终端型号", terminalTypeArr);

        List<DictionaryDO> manufacturers =
            TypeCacheManger.getInstance().getDictionaryList(DictionaryType.TERMINAL_MANUFACTURER);
        if (CollectionUtils.isNotEmpty(manufacturers)) {
            selectMap.put("终端厂商", manufacturers.stream().map(DictionaryDO::getValue).toArray(String[]::new));
        }

        return selectMap;
    }

    private List<String> getTemplateHeader() {
        List<String> headList = new ArrayList<>();
        headList.add("监控对象");
        headList.add("监控对象类型");
        headList.add("车牌颜色(仅车辆时必填)");
        headList.add("所属企业");
        headList.add("分组(多个分组用逗号分隔)");
        headList.add("终端号");
        headList.add("通讯类型");
        headList.add("终端厂商");
        headList.add("终端型号");
        headList.add("功能类型");
        headList.add("终端手机号");
        headList.add("真实SIM卡号");
        headList.add("计费日期");
        headList.add("到期日期");
        headList.add("从业人员(多个从业人员用逗号分隔)");
        return headList;
    }

    private List<String> getRequiredField() {
        List<String> requiredList = new ArrayList<>();
        // 必填字段
        requiredList.add("监控对象");
        requiredList.add("监控对象类型");
        requiredList.add("车牌颜色(仅车辆时必填)");
        requiredList.add("所属企业");
        requiredList.add("终端号");
        requiredList.add("通讯类型");
        requiredList.add("功能类型");
        requiredList.add("终端手机号");
        return requiredList;
    }

    /**
     * 通过车辆id，获取配置信息
     * @param vid 车辆id
     * @return list l
     */
    @Override
    public List<Map<String, String>> getConfigByVehicle(String vid) {
        return configDao.getConfigByVehicle(vid);
    }
}
