package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.MessageConfig;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.FriendDO;
import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.domain.IntercomDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.FriendDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.basic.dto.IntercomDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.export.IntercomExportDTO;
import com.zw.platform.basic.dto.imports.IntercomImportDTO;
import com.zw.platform.basic.dto.query.IntercomQuery;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.event.ConfigUpdateEvent;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.imports.handler.ConfigDeviceImportHandler;
import com.zw.platform.basic.imports.handler.ConfigImportHandler;
import com.zw.platform.basic.imports.handler.ConfigPeopleImportHandler;
import com.zw.platform.basic.imports.handler.ConfigSimCardImportHandler;
import com.zw.platform.basic.imports.handler.ConfigThingImportHandler;
import com.zw.platform.basic.imports.handler.ConfigVehicleImportHandler;
import com.zw.platform.basic.imports.handler.ConfigVideoChannelHandler;
import com.zw.platform.basic.imports.handler.IntercomImportHandler;
import com.zw.platform.basic.imports.validator.IntercomImportValidator;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.basic.repository.FriendDao;
import com.zw.platform.basic.repository.GroupMonitorDao;
import com.zw.platform.basic.repository.IntercomDao;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.ConfigService;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.IntercomService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.OrganizationUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.core.LogInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.MagicNumbers;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.excel.validator.ImportValidator;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportLock;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.talkback.domain.intercom.ErrorMessageEnum;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.domain.lyxj.FirstCustomer;
import com.zw.talkback.repository.mysql.OriginalModelDao;
import com.zw.talkback.service.baseinfo.IntercomCallNumberService;
import com.zw.talkback.util.JsonUtil;
import com.zw.talkback.util.TalkCallUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 对讲信息列表
 * 对讲模块的监控对象、终端、sim卡绑定
 * @author zhangjuan
 */
@Service("intercomService")
public class IntercomServiceImpl implements IntercomService, CacheService, IpAddressService {
    private static final Logger log = LogManager.getLogger(IntercomServiceImpl.class);
    private static final String INTERCOM_GROUP_TYPE = "1";

    @Autowired
    private IntercomDao intercomDao;

    @Autowired
    private MonitorFactory monitorFactory;

    @Autowired
    private ConfigService configService;

    @Autowired
    private OriginalModelDao originalModelDao;

    @Autowired
    private TalkCallUtil talkCallUtil;

    @Autowired
    private IntercomCallNumberService callNumberService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private SimCardService simCardService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private GroupMonitorService groupMonitorService;

    @Autowired
    private GroupMonitorDao groupMonitorDao;

    @Autowired
    private LogSearchService logService;

    @Autowired
    private MessageConfig messageConfig;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private FriendDao friendDao;

    /**
     * 对讲信息配置导入专用
     */
    @Autowired
    private DeviceNewDao deviceNewDao;

    @Autowired
    private SimCardNewDao simCardNewDao;

    @Autowired
    private NewConfigDao configDao;

    @Autowired
    private VideoChannelSettingDao videoChannelSettingDao;

    @Override
    public void initCache() {
        log.info("开始进行对讲信息列表的redis初始化.");
        //模糊搜索的缓存在对应类型的监控对象中进行初始化
        List<String> sortMonitorIds = intercomDao.getSortList();
        //进行历史缓存信息清理
        RedisHelper.delete(RedisKeyEnum.INTERCOM_SORT_LIST.of());
        //存储对讲信的缓存列表
        RedisHelper.addToListTop(RedisKeyEnum.INTERCOM_SORT_LIST.of(), sortMonitorIds);
        log.info("结束对讲信息列表的redis初始化.");
    }

    @Override
    public boolean add(IntercomDTO intercom) throws Exception {
        //进行参数校验
        checkAdd(intercom);

        // 获取个呼号码 暂时沿用原来的逻辑，个呼号码管理暂时不做改变
        intercom.setCallNumber(callNumberService.updateAndReturnPersonCallNumber());
        String id = UUID.randomUUID().toString();
        FirstCustomer firstCustomer = talkCallUtil.getFirstCustomerInfo();
        intercom.setCustomerCode(Objects.isNull(firstCustomer) ? 1L : firstCustomer.getCustId());

        //绑定成定位对象
        bindConfig(intercom, id);

        //对讲绑定时，人员已经存在更新人员的在离职状态为在职
        if (MonitorTypeEnum.PEOPLE.getType().equals(intercom.getMonitorType()) && null != intercom.getId()) {
            monitorFactory.getPeopleService().updateIncumbency(intercom.getId(), 2);
        }

        //封装对讲对象并插入数据库的
        addIntercomToMysql(intercom, id);

        //维护顺序缓存
        RedisHelper.addToListTop(RedisKeyEnum.INTERCOM_SORT_LIST.of(), intercom.getId());
        //维护模糊搜索缓存
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_INTERCOM.of(), buildFuzzyMap(intercom));

        //记录日志
        String msg = String.format("快速录入：添加 %s(监控对象),%s(对讲设备标识),%s(终端手机号)的绑定关系", intercom.getName(),
            intercom.getIntercomDeviceNumber(), intercom.getSimCardNumber());
        logService.addLog(getIpAddress(), msg, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, intercom.getName(), "对讲信息录入：快速录入");
        return true;
    }

    private void bindConfig(IntercomDTO intercom, String id) throws BusinessException {
        //先绑定成为定位关系
        ConfigDTO config = new ConfigDTO();
        BeanUtils.copyProperties(intercom, config);
        config.setIntercomInfoId(id);
        configService.add(config);

        //回填对讲信息sim卡等信息
        intercom.setSimCardId(config.getSimCardId());
        intercom.setId(config.getId());
        intercom.setOrgId(config.getOrgId());
        intercom.setDeviceId(config.getDeviceId());
    }

    private void checkAdd(IntercomDTO intercom) throws BusinessException {
        checkGroup(intercom.getGroupId());
        //校验机型，也防止传入的机型id与name不一致，进行校正
        OriginalModelInfo originalModel = originalModelDao.getOriginalModelByModelId(intercom.getOriginalModelId());
        if (Objects.isNull(originalModel)) {
            throw new BusinessException("原始机型不存在!");
        }
        intercom.setIntercomDeviceNumber(originalModel.getModelId() + intercom.getDeviceNumber());
        //校验对讲终端号是否绑定过对讲对象
        if (Objects.nonNull(intercomDao.getByIntercomDeviceNum(intercom.getIntercomDeviceNumber()))) {
            throw new BusinessException("对讲设备标识已经被绑定!");
        }
    }

    private Map<String, String> buildFuzzyMap(IntercomDTO intercom) {
        String field = FuzzySearchUtil
            .buildField(intercom.getMonitorType(), intercom.getName(), intercom.getIntercomDeviceNumber(),
                intercom.getSimCardNumber());
        String value = FuzzySearchUtil.buildValue(intercom.getId(), intercom.getDeviceId(), intercom.getSimCardId());
        return ImmutableMap.of(field, value);
    }

    @Override
    public boolean convertToIntercom(IntercomDTO bindDTO) throws BusinessException {
        // todo 定位对象转换成对讲对象，前端目前不支持该功能，后面有需要再实现
        return false;
    }

    @Override
    public Map<String, Object> checkIsBind(IntercomDTO bindDTO) {
        String moName = bindDTO.getName();
        String simNum = bindDTO.getSimCardNumber();
        String deviceNum = bindDTO.getDeviceNumber();
        String moType = bindDTO.getMonitorType();

        //检查监控对象是否被绑定
        BindDTO monitor = monitorFactory.create(moType).getByName(moName);
        Map<String, Object> result = ImmutableMap.of("isBindLocateObject", false, "isBindIntercom", false);
        //检查对讲设备标识
        if (Objects.nonNull(monitor) && Objects.equals(monitor.getBindType(), Vehicle.BindType.HAS_BIND)) {
            result.put("monitor", monitor);
            result.put("isBindLocateObject", true);
            if (StringUtils.isNotBlank(monitor.getIntercomDeviceNumber())) {
                result.put("msg", "监控对象" + moName + "已被绑定");
                result.put("isBindIntercom", true);
                return result;
            }
            if (simNum.equals(monitor.getSimCardNumber()) && deviceNum.equals(monitor.getDeviceNumber())) {
                return result;
            }
            result.put("msg", "监控对象【" + moName + "】已被绑定，但与输入的终端手机号和终端号的绑定关系不一致");
            return result;
        }

        // 检查终端是否被绑定过
        if (deviceService.checkIsBind(deviceNum)) {
            result.put("isBindLocateObject", true);
            result.put("msg", "终端号【" + deviceNum + "】已被绑定,但与输入的终端手机号和监控对象绑定关系不一致");
            return result;
        }

        // 检查SIM卡是否被绑定过
        if (simCardService.checkIsBind(simNum)) {
            result.put("isBindLocateObject", true);
            result.put("msg", "终端手机号【" + simNum + "】已被绑定,但与输入的终端号和监控对象绑定关系不一致");
            return result;
        }
        return result;
    }

    @Override
    public Map<String, Object> checkIsBind(String inputId, String inputValue, String monitorType) {
        boolean isBindIntercom = false;
        boolean isBindLocateObject = false;
        String configId = null;
        BindDTO bindDTO;
        switch (inputId) {
            case "brands":
                bindDTO = monitorFactory.create(monitorType).getByName(inputValue);
                isBindLocateObject =
                    Objects.nonNull(bindDTO) && Objects.equals(bindDTO.getBindType(), Vehicle.BindType.HAS_BIND);
                isBindIntercom = Objects.nonNull(bindDTO) && StringUtils.isNotBlank(bindDTO.getIntercomInfoId());
                break;
            case "devices":
                DeviceDTO deviceDTO = deviceService.getByNumber(inputValue);
                isBindLocateObject = Objects.nonNull(deviceDTO) && StringUtils.isNotBlank(deviceDTO.getBindId());
                configId = Objects.nonNull(deviceDTO) ? deviceDTO.getBindId() : null;
                break;
            case "sims":
                SimCardDTO simCard = simCardService.getByNumber(inputValue);
                isBindLocateObject = Objects.nonNull(simCard) && StringUtils.isNotBlank(simCard.getConfigId());
                configId = Objects.nonNull(simCard) ? simCard.getConfigId() : null;
                break;
            default:
                break;
        }
        if (StringUtils.isNotBlank(configId)) {
            bindDTO = configService.getByConfigId(configId);
            isBindIntercom = Objects.nonNull(bindDTO) && StringUtils.isNotBlank(bindDTO.getIntercomInfoId());
        }
        String msg = isBindLocateObject ? inputValue + "已经被绑定了定位对象" : "";
        msg = isBindIntercom ? inputValue + "已经被绑定对讲对象" : msg;

        return ImmutableMap.of(
                "isBoundTalkback", isBindIntercom,
                "isBoundLocateObject", isBindLocateObject,
                "boundName", inputValue,
                "msg", msg
        );
    }

    @Override
    public IntercomDTO getDetailByConfigId(String configId) {
        IntercomDTO intercom = intercomDao.getDetailByConfigId(configId);
        if (Objects.isNull(intercom)) {
            return null;
        }
        //获取监控对象的所属企业名称
        intercom.setOrgName(organizationService.getOrgNameByUuid(intercom.getOrgId()));
        String intercomDeviceId = intercom.getIntercomDeviceNumber().substring(MagicNumbers.INT_FIVE);
        intercom.setIntercomDeviceId(intercomDeviceId);
        return intercom;
    }

    @Override
    public List<IntercomDTO> getDetailByConfigIds(Collection<String> configIds) {
        List<IntercomDTO> intercoms = intercomDao.getDetailByConfigIds(configIds);
        if (intercoms.isEmpty()) {
            return new ArrayList<>();
        }
        //获取企业id与名称的映射关系
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));

        intercoms.forEach(intercom -> {
            String intercomDeviceId = intercom.getIntercomDeviceNumber().substring(MagicNumbers.INT_FIVE);
            intercom.setIntercomDeviceId(intercomDeviceId);
            intercom.setOrgName(orgMap.get(intercom.getOrgId()));
        });
        return intercoms;
    }

    private void completeGroup(Map<String, String> groupMap, IntercomDTO intercom) {
        if (StringUtils.isBlank(intercom.getGroupId())) {
            intercom.setCurrentGroupNum(0);
            return;
        }

        String[] groupIdArr = intercom.getGroupId().split(",");
        intercom.setCurrentGroupNum(groupIdArr.length);
        List<String> groupIds = new ArrayList<>();
        List<String> groupNames = new ArrayList<>();
        for (String groupId : groupIdArr) {
            if (groupMap.containsKey(groupId)) {
                groupIds.add(groupId);
                groupNames.add(groupMap.get(groupId));
            }
        }
        intercom.setGroupId(StringUtils.join(groupIds, ","));
        intercom.setGroupName(StringUtils.join(groupNames, ","));
    }

    @Override
    public boolean update(IntercomDTO intercom) throws Exception {
        //获取修改前的对讲信息
        IntercomDTO oldIntercom = intercomDao.getDetailByConfigId(intercom.getConfigId());
        if (Objects.isNull(oldIntercom)) {
            throw new BusinessException("对讲对象不存在");
        }

        //校验分组和校验绑定关系
        checkGroup(intercom.getGroupId());
        boolean isUpdateConfig = checkBind(intercom, oldIntercom);
        boolean isSuccess = update(intercom, oldIntercom, isUpdateConfig);
        if (isSuccess) {
            addUpdateLog(intercom, oldIntercom);
        }
        return isSuccess;
    }

    private boolean update(IntercomDTO intercom, IntercomDTO oldIntercom, boolean isUpdateConfig)
        throws BusinessException {
        //解绑原来的对讲关系 todo zj 后面有时间可以优化成不进行解绑,直接进行修改
        int successCount = unBind(Collections.singletonList(oldIntercom), false).size();
        if (successCount == 0) {
            throw new BusinessException("解除原来的绑定关系失败");
        }
        //获取个呼号码
        intercom.setCallNumber(oldIntercom.getCallNumber());
        //修改定位绑定关系
        if (isUpdateConfig) {
            ConfigDTO bindDTO = new ConfigDTO();
            BeanUtils.copyProperties(intercom, bindDTO);
            bindDTO.setIntercomInfoId(oldIntercom.getIntercomInfoId());
            configService.update(bindDTO, oldIntercom.getId());
        }

        //替换掉原来的对讲信息
        addIntercomToMysql(intercom, oldIntercom.getIntercomInfoId());

        //对讲绑定时，人员已经存在更新人员的在离职状态为在职
        if (MonitorTypeEnum.PEOPLE.getType().equals(intercom.getMonitorType())) {
            monitorFactory.getPeopleService().updateIncumbency(intercom.getId(), 2);
        }

        //更新缓存
        updateIntercomCache(intercom, oldIntercom);
        return true;
    }

    private void addUpdateLog(IntercomDTO curIntercom, IntercomDTO oldIntercom) {
        String lastInfo = String
            .format("修改绑定关系:<br/>监控对象(%s)/群组(%s)/对讲设备标识(%s)/终端手机号(%s) /优先级(%s)/功能(%s文本信息,%s图片信息,%s离线语音信息)为<br/>",
                oldIntercom.getName(), oldIntercom.getGroupName(), oldIntercom.getIntercomDeviceNumber(),
                oldIntercom.getSimCardNumber(), oldIntercom.getPriority(), switchSuport(oldIntercom.getTextEnable()),
                switchSuport(oldIntercom.getImageEnable()), switchSuport(oldIntercom.getAudioEnable()));
        String newInfo = String.format("监控对象(%s)/群组(%s)/对讲设备标识(%s)/终端手机号(%s) /优先级(%s)/功能((%s文本信息,(%s图片信息,(%s离线语音信息)",
            curIntercom.getName(), curIntercom.getGroupName(), curIntercom.getIntercomDeviceNumber(),
            curIntercom.getSimCardNumber(), curIntercom.getPriority(), switchSuport(curIntercom.getTextEnable()),
            switchSuport(curIntercom.getImageEnable()), switchSuport(curIntercom.getAudioEnable()));
        logService
            .addMoreLog(getIpAddress(), lastInfo + newInfo, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, oldIntercom.getName(),
                "对讲信息录入：修改绑定关系");
    }

    private String switchSuport(Integer num) {
        if (num != null && num == 1) {
            return "支持";
        }
        return "不支持";
    }

    private void updateIntercomCache(IntercomDTO curIntercom, IntercomDTO oldIntercom) {
        //删除历史的模糊搜索缓存
        String oldField = FuzzySearchUtil
            .buildField(oldIntercom.getMonitorType(), oldIntercom.getName(), oldIntercom.getIntercomDeviceNumber(),
                oldIntercom.getSimCardNumber());
        RedisHelper.hdel(RedisKeyEnum.FUZZY_INTERCOM.of(), oldField);
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_INTERCOM.of(), buildFuzzyMap(curIntercom));

        String newMonitorId = curIntercom.getId();
        //监控对象信息里面新增对讲相关属性
        Map<String, String> intercomInfo = ImmutableMap
            .of("intercomInfoId", oldIntercom.getIntercomInfoId(), "callNumber", curIntercom.getCallNumber(),
                "intercomDeviceNumber", curIntercom.getIntercomDeviceNumber());
        if (Objects.nonNull(curIntercom.getUserId())) {
            intercomInfo.put("userId", String.valueOf(curIntercom.getUserId()));
        }

        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(newMonitorId), intercomInfo);
        RedisHelper.addToListTop(RedisKeyEnum.INTERCOM_SORT_LIST.of(), newMonitorId);
    }

    private void addIntercomToMysql(IntercomDTO intercom, String id) {
        IntercomDO intercomDO = new IntercomDO(intercom);
        intercomDO.setId(id);
        intercomDao.insert(intercomDO);
    }

    private boolean checkBind(IntercomDTO curIntercom, IntercomDTO oldIntercom) throws BusinessException {
        //校验机型，也防止传入的机型id与name不一致，进行校正
        OriginalModelInfo originalModel = originalModelDao.getOriginalModelByModelId(curIntercom.getOriginalModelId());
        if (Objects.isNull(originalModel)) {
            throw new BusinessException("原始机型不存在!");
        }

        String curMoName = curIntercom.getName();
        String curDeviceNum = curIntercom.getDeviceNumber();
        String curSimNum = curIntercom.getSimCardNumber();
        String curIntercomDevice = originalModel.getModelId() + curDeviceNum;
        curIntercom.setIntercomDeviceNumber(curIntercomDevice);

        String oldMoName = oldIntercom.getName();
        String oldDeviceNum = oldIntercom.getDeviceNumber();
        String oldSimNum = oldIntercom.getSimCardNumber();
        String oldIntercomDevice = oldIntercom.getIntercomDeviceNumber();
        if (!curIntercomDevice.equals(oldIntercomDevice)) {
            IntercomDO intercomDO = intercomDao.getByIntercomDeviceNum(curIntercomDevice);
            if (Objects.nonNull(intercomDO)) {
                throw new BusinessException("对讲设备标识已经被绑定!");
            }
        }
        //检查监控对象是否绑定
        boolean moIsChang = !curMoName.equals(oldMoName);
        if (moIsChang) {
            BindDTO bindDTO = monitorFactory.create(curIntercom.getMonitorType()).getByName(curMoName);
            if (Objects.nonNull(bindDTO) && Objects.equals(bindDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                throw new BusinessException("监控对象【" + curMoName + "】已经被绑定");
            }
            //修改了监控对象名称时，监控对象ID不变，只修对应监控对象信息里的名称
            String monitorId = Objects.nonNull(bindDTO) ? bindDTO.getId() : oldIntercom.getId();
            String orgId = Objects.nonNull(bindDTO) ? bindDTO.getOrgId() : oldIntercom.getOrgId();
            curIntercom.setId(monitorId);
            curIntercom.setOrgId(orgId);
        }

        //校验终端
        boolean deviceIsChange = !curDeviceNum.equals(oldDeviceNum);
        if (deviceIsChange) {
            DeviceDTO deviceDTO = deviceService.getByNumber(curDeviceNum);
            String deviceId = Objects.isNull(deviceDTO) ? oldIntercom.getDeviceId() : deviceDTO.getId();
            if (Objects.nonNull(deviceDTO) && StringUtils.isNotBlank(deviceDTO.getBindId())) {
                throw new BusinessException("终端号【" + curDeviceNum + "】已经被绑定");
            }
            curIntercom.setDeviceId(deviceId);
        } else {
            curIntercom.setDeviceId(oldIntercom.getDeviceId());
        }

        //校验SIM卡号
        boolean simIsChange = !curSimNum.equals(oldSimNum);
        if (simIsChange) {
            SimCardDTO simCardDTO = simCardService.getByNumber(curSimNum);
            String simCardId = Objects.isNull(simCardDTO) ? oldIntercom.getSimCardId() : simCardDTO.getId();
            if (Objects.nonNull(simCardDTO) && StringUtils.isNotBlank(simCardDTO.getBindId())) {
                throw new BusinessException("终端手机号【" + curSimNum + "】已经被绑定");
            }
            curIntercom.setSimCardId(simCardId);
        }

        boolean groupIsChange = isGroupChange(curIntercom.getGroupId(), oldIntercom.getGroupId());
        return moIsChang || deviceIsChange || simIsChange || groupIsChange;
    }

    private boolean isGroupChange(String curGroupId, String oldGroupId) {
        //检查分组是否发生改变
        Set<String> curGroupIds = new HashSet<>(Arrays.asList(curGroupId.split(",")));
        Set<String> oldGroupIds = new HashSet<>(Arrays.asList(oldGroupId.split(",")));
        int curGroupSize = curGroupIds.size();
        int oldGroupSize = oldGroupIds.size();

        //群组数量发生改变或着两者求交集后集合发生了改变
        return curGroupSize != oldGroupSize || curGroupIds.retainAll(oldGroupIds);
    }

    private void checkGroup(String groupId) throws BusinessException {
        if (StringUtils.isBlank(groupId)) {
            throw new BusinessException("请选择群组!");
        }

        Set<String> groupIds = new HashSet<>(Arrays.asList(groupId.split(",")));
        if (groupIds.size() > MagicNumbers.INT_EIGHT) {
            throw new BusinessException(messageConfig.getIntercomObjectMaxAssignment());
        }
    }

    @Override
    public int unbindByConfigId(Collection<String> configIds) throws Exception {
        List<IntercomDTO> intercomList = intercomDao.getDetailByConfigIds(configIds);
        if (intercomList.isEmpty()) {
            throw new BusinessException("对讲对象不存在");
        }
        List<String> successMsg = unBind(intercomList, true);
        if (successMsg.isEmpty()) {
            return 0;
        }
        StringBuilder message = new StringBuilder();
        successMsg.forEach(message::append);
        String operation;
        String moName = "-";
        if (configIds.size() > 1) {
            operation = "对讲信息录入：批量解除对讲绑定关系";
        } else {
            operation = "对讲信息录入：解除对讲绑定关系";
            moName = intercomList.get(0).getName();
        }
        String ipAddress = getIpAddress();
        logService.addMoreLog(ipAddress, message.toString(), LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, moName, operation);
        return successMsg.size();
    }

    @Override
    public int unbindByMonitorIds(Collection<String> monitorIds) throws Exception {
        List<IntercomDTO> intercomList = intercomDao.getDetailByMonitorIds(monitorIds);
        if (intercomList.isEmpty()) {
            throw new BusinessException("对讲对象不存在");
        }
        return unBind(intercomList, true).size();
    }

    @Override
    public List<String> getUserOwnIds(String keyword) {
        Set<String> userOwnIds = userService.getCurrentUserMonitorIds();
        if (CollectionUtils.isEmpty(userOwnIds)) {
            return new ArrayList<>();
        }
        //进行模糊搜索
        if (StringUtils.isNotBlank(keyword)) {
            Set<String> filterIds = FuzzySearchUtil.scan(RedisKeyEnum.FUZZY_INTERCOM.of(), keyword);
            //求并集
            userOwnIds.retainAll(filterIds);
        }

        if (CollectionUtils.isEmpty(userOwnIds)) {
            return new ArrayList<>();
        }
        return sortIdList(userOwnIds);
    }

    private List<String> sortIdList(Set<String> userOwnIds) {
        List<String> sortList = RedisHelper.getList(RedisKeyEnum.INTERCOM_SORT_LIST.of());
        List<String> result = new ArrayList<>();
        for (String monitorId : sortList) {
            if (userOwnIds.contains(monitorId)) {
                result.add(monitorId);
            }
        }
        return result;
    }

    @Override
    public Page<IntercomDTO> getByKeyword(IntercomQuery query) {
        List<String> monitorIds = getUserOwnIds(query.getSimpleQueryParam());
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new Page<>();
        }
        //进行分页
        Long start = query.getStart();
        Long end = Math.min(start + query.getLength(), monitorIds.size());
        if (start > end) {
            return RedisQueryUtil.getListToPage(new ArrayList<>(), query, monitorIds.size());
        }
        List<String> subList = monitorIds.subList(Integer.valueOf(start + ""), Integer.valueOf(end + ""));

        //获取所有的企业id和name的映射关系
        List<OrganizationLdap> orgList = organizationService.getAllOrganization();
        Map<String, String> orgMap =
            AssembleUtil.collectionToMap(orgList, OrganizationLdap::getUuid, OrganizationLdap::getName);

        //获取用户权限下群组的id和name的映射关系
        Map<String, String> groupMap = getGroupMap();
        List<IntercomDTO> sortList = getSortList(subList, orgMap, groupMap);

        return RedisQueryUtil.getListToPage(sortList, query, monitorIds.size());
    }

    private List<IntercomDTO> getSortList(List<String> sortIds, Map<String, String> orgMap,
        Map<String, String> groupMap) {
        List<IntercomDTO> intercomList = intercomDao.getDetailByMonitorIds(sortIds);
        Map<String, IntercomDTO> intercomMap = AssembleUtil.collectionToMap(intercomList, IntercomDTO::getId);
        //进行重新排序和完善监控对象信息
        List<IntercomDTO> sortList = new ArrayList<>();
        for (String monitorId : sortIds) {
            IntercomDTO intercom = intercomMap.get(monitorId);
            intercom.setOrgName(orgMap.get(intercom.getOrgId()));
            completeGroup(groupMap, intercom);
            sortList.add(intercom);
        }
        return sortList;
    }

    @Override
    public boolean export(HttpServletResponse response) throws Exception {
        //获取用户权限下对讲信息列表
        List<String> monitorIds = getUserOwnIds(null);
        if (CollectionUtils.isEmpty(monitorIds)) {
            throw new BusinessException("导出数据为空");
        }

        //获取所有的组织id与名称的映射关系
        List<OrganizationLdap> orgList = organizationService.getAllOrganization();
        Map<String, String> orgMap =
            AssembleUtil.collectionToMap(orgList, OrganizationLdap::getUuid, OrganizationLdap::getName);

        //获取用户权限下群组的id和name的映射关系
        Map<String, String> groupMap = getGroupMap();

        List<List<String>> cutList = cutList(monitorIds);
        List<IntercomExportDTO> exportList = new ArrayList<>();
        for (List<String> subList : cutList) {
            List<IntercomDTO> sortList = getSortList(subList, orgMap, groupMap);
            for (IntercomDTO intercomDTO : sortList) {
                exportList.add(new IntercomExportDTO(intercomDTO));
            }
        }
        //写入文件
        ExportExcel export = new ExportExcel(null, IntercomExportDTO.class, 1);
        export.setDataList(exportList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
        return true;
    }

    @Override
    public JSONObject getAddPageInitData() {
        JSONObject msg = new JSONObject();
        OrganizationLdap org = userService.getCurrentUserOrg();
        msg.put("orgId", org != null ? org.getUuid() : "");
        msg.put("orgName", org != null ? org.getName() : "");
        //原始机型
        msg.put("originalModelList", originalModelDao.findAllOriginalModelInfo());
        // 车辆信息
        msg.put("vehicleInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.VEHICLE.getType()));
        // 人员信息
        msg.put("peopleInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.PEOPLE.getType()));
        // 物品信息
        msg.put("thingInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.THING.getType()));

        // sim卡信息
        msg.put("simCardInfoList", simCardService.getUbBindSelectList(null));
        return msg;
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        // 表头
        List<String> headList = new ArrayList<>();
        headList.add("终端手机号");
        headList.add("原始机型");
        headList.add("设备标识(7位)");
        headList.add("设备密码");
        headList.add("监控对象");
        headList.add("监控对象类型");
        headList.add("所属组织");
        headList.add("群组(多个分组用逗号分隔)");
        headList.add("优先级");

        // 必填字段
        List<String> requiredList = new ArrayList<>();
        requiredList.add("终端手机号");
        requiredList.add("原始机型");
        requiredList.add("设备标识(7位)");
        requiredList.add("设备密码");
        requiredList.add("监控对象");
        requiredList.add("监控对象类型");
        requiredList.add("所属组织");
        requiredList.add("群组(多个分组用逗号分隔)");

        // 默认设置一条数据
        List<Object> exportList = new ArrayList<>();
        exportList.add("18600222931");
        exportList.add("4A11G");
        exportList.add("0002055");
        exportList.add("ik9u5hme");
        exportList.add("IW2055");
        exportList.add("人");

        // 获取用户权限下的组织名称
        String[] orgNames = userService.getCurrentUseOrgList().stream().filter(o -> !"organization".equals(o.getOu()))
            .map(OrganizationLdap::getName).toArray(String[]::new);
        exportList.add(orgNames[0]);
        exportList.add("分组名称@所属组织名称");
        exportList.add("1");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>(16);
        String[] monitorType = { "车", "人", "物" };
        selectMap.put("监控对象类型", monitorType);
        // 功能类型
        String[] priority = { "1", "2", "3", "4", "5" };
        selectMap.put("优先级", priority);
        selectMap.put("所属组织", orgNames);

        String[] originalModels =
            originalModelDao.findAllOriginalModelInfo().stream().map(OriginalModelInfo::getModelId)
                .toArray(String[]::new);
        selectMap.put("原始机型", originalModels);
        //写入文件
        ExportExcelUtil.writeTemplateToFile(headList, requiredList, selectMap, exportList, response);
        return true;
    }

    @Override
    @ImportLock(ImportModule.CONFIG)
    public JsonResultBean importFile(MultipartFile file) throws Exception {
        //解析文件
        ImportExcel importExcel = new ImportExcel(file, 1, 0);

        //获取用户权限下的群组
        List<GroupDTO> groupList = userService.getCurrentUserGroupList().stream()
            .filter(o -> Objects.equals(o.getTypes(), INTERCOM_GROUP_TYPE)).collect(Collectors.toList());
        if (groupList.isEmpty()) {
            return new JsonResultBean(JsonResultBean.FAULT, "请先添加对讲群组");
        }

        //构建对讲信息列表导入校验器
        ConfigImportHolder holder = getHolder();
        ImportValidator<IntercomImportDTO> validator =
            new IntercomImportValidator(holder, monitorFactory, deviceNewDao, simCardNewDao, intercomDao,
                originalModelDao, groupMonitorService, groupList);
        importExcel.setImportValidator(validator);

        //获取导入数据
        List<IntercomImportDTO> importList = importExcel.getDataListNew(IntercomImportDTO.class);
        if (CollectionUtils.isEmpty(importList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "导入文件数据不能为空");
        }

        //进行参数校验
        JsonResultBean resultBean = validator.validate(importList, false, null);
        if (!resultBean.isSuccess()) {
            ImportErrorUtil.putDataToRedis(importList, ImportModule.INTERCOM);
            return resultBean;
        }

        List<BaseImportHandler> handlers = getConfigImportHandlers(holder, importList);
        try (ImportCache ignored = new ImportCache(ImportModule.CONFIG, SystemHelper.getCurrentUsername(), handlers)) {
            resultBean = handle(handlers, holder);
            if (!resultBean.isSuccess()) {
                return resultBean;
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
        String sucMessage = String.format("成功导入%d条数据", importList.size());
        logService.addLog(getIpAddress(), sucMessage, "3", "对讲信息配置", "批量导入");
        return resultBean;
    }

    @Override
    public JsonResultBean addToIntercomPlatform(Collection<String> configIds) {
        List<IntercomDTO> intercomList = intercomDao.getDetailByConfigIds(configIds);
        if (intercomList.isEmpty()) {
            return new JsonResultBean(JsonResultBean.FAULT, "对讲对象不存在");
        }
        StringBuilder errorMsg = new StringBuilder();
        Map<RedisKey, Map<String, String>> monitorCache = new HashMap<>(CommonUtil.ofMapCapacity(intercomList.size()));
        for (IntercomDTO intercom : intercomList) {
            String errMsg = addOrUpdateIntercomObject(intercom);
            if (StringUtils.isBlank(errMsg)) {
                Map<String, String> userIdMap = ImmutableMap.of("userId", String.valueOf(intercom.getUserId()));
                monitorCache.put(RedisKeyEnum.MONITOR_INFO.of(intercom.getId()), userIdMap);
            } else {
                errorMsg.append(String.format("监控对象【%s】生成失败:%s<br/>", intercom.getName(), errMsg));
            }
        }
        //批量维护缓存
        RedisHelper.batchAddToHash(monitorCache);
        //记录生成日志
        addGeneratorMsg(intercomList);
        boolean isSuccess = errorMsg.length() == 0;
        return new JsonResultBean(isSuccess, isSuccess ? "生成对讲对象成功" : errorMsg.toString());
    }

    @Override
    public boolean updateRecordStatus(String configId, Integer recordEnable) throws BusinessException {
        IntercomDTO intercomDTO = intercomDao.getDetailByConfigId(configId);
        if (Objects.isNull(intercomDTO)) {
            throw new BusinessException("该对讲对象不存在");
        }
        if (Objects.isNull(intercomDTO.getUserId())) {
            throw new BusinessException("监控对象未生成");
        }
        // 调用第三方接口
        JSONObject resultBody = talkCallUtil.updateRecordStatus(recordEnable, intercomDTO.getUserId());
        if (Objects.equals(resultBody.getInteger("result"), 0)) {
            throw new BusinessException(resultBody.getString("message"));
        }
        //更新数据库的数据
        IntercomDO intercomDO = new IntercomDO();
        intercomDO.setId(intercomDTO.getId());
        intercomDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        intercomDO.setRecordEnable(recordEnable);
        intercomDao.update(intercomDO);
        //记录日志
        String message =
            String.format("%s【%s】录音", IntercomDTO.getRecordEnableFormat(recordEnable), intercomDTO.getName());
        logService.addLog(getIpAddress(), message, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, "");
        return true;
    }

    @Override
    public JSONArray getIntercomBaseTree(String queryType, String type, String keyword, Integer status,
        boolean isFilterNullOrg) {
        //获取用户权限下的组织
        List<OrganizationLdap> userOrgList = userService.getCurrentUseOrgList();
        boolean keywordNoBlank = StringUtils.isNotBlank(keyword);
        if (Objects.equals(queryType, "org") && keywordNoBlank) {
            userOrgList = userOrgList.stream().filter(o -> o.getName().contains(keyword)).collect(Collectors.toList());
        }
        // todo zj 获取用户权限下的群组 可以支持模糊搜索
        List<GroupDTO> userGroupList =
            userService.getCurrentUserGroupList().stream().filter(o -> Objects.equals(o.getTypes(), "1"))
                .collect(Collectors.toList());
        //过滤掉下面没有分组的组织
        if (isFilterNullOrg) {
            Set<String> orgIds = userGroupList.stream().map(GroupDTO::getOrgId).collect(Collectors.toSet());
            userOrgList = OrganizationUtil.filterOrgListByUuid(userOrgList, orgIds);
        }
        if (userOrgList.isEmpty() || userGroupList.isEmpty()) {
            return new JSONArray();
        }
        //获取对讲对象
        String monitorKeyword = Objects.equals(queryType, "monitor") && keywordNoBlank ? keyword : null;
        JSONArray monitorList = getIntercomList(monitorKeyword, status, userGroupList);

        JSONArray result = new JSONArray();
        result.addAll(getGroupTree(userGroupList, userOrgList, type, false));
        result.addAll(JsonUtil.getGroupTree(userOrgList, type, false));
        result.addAll(monitorList);
        return result;
    }

    @Override
    public JSONArray getTreeNodeByGroupId(String groupId) {
        List<GroupDTO> groupList = groupService.getGroupsById(Collections.singletonList(groupId));
        return getIntercomList(null, IntercomDTO.Status.SUCCESS_STATUS, groupList);
    }

    @Override
    public JSONArray getDispatcherTree(String keyword) {
        //获取用户权限下的组织
        List<OrganizationLdap> orgList = userService.getCurrentUseOrgList();

        //获取用户组织权限下的所有调度员
        List<UserDTO> userBeans;
        if (StringUtils.isNotBlank(keyword)) {
            //todo zj 支持模糊搜索用户
            userBeans = new ArrayList<>();
        } else {
            userBeans = userService.getUserByOrgDn(orgList.get(0).getCid(), SearchScope.SUBTREE);
        }
        if (CollectionUtils.isEmpty(userBeans)) {
            return new JSONArray();
        }

        JSONArray result = new JSONArray();
        Set<String> hasUserOrgDn = new HashSet<>();
        for (UserDTO userDTO : userBeans) {
            if (StringUtils.isBlank(userDTO.getDispatcherId())) {
                continue;
            }
            JSONObject treeNode = new JSONObject();
            String userDn = userDTO.getId().toString();
            String orgDn = userService.getUserOrgDnByDn(userDn);
            treeNode.put("id", userDn);
            treeNode.put("pId", orgDn);
            treeNode.put("name", userDTO.getUsername());
            treeNode.put("type", "user");
            treeNode.put("uuid", userDTO.getUuid());
            treeNode.put("iconSkin", "userSkin");
            treeNode.put("userId", userDTO.getDispatcherId());
            result.add(treeNode);
            hasUserOrgDn.add(orgDn);
        }
        //过滤掉下面没有用户的组织并组装成树节点
        orgList = OrganizationUtil.filterOrgListByDn(orgList, hasUserOrgDn);
        result.addAll(JsonUtil.getGroupTree(orgList, "multiple", false));
        return result;
    }

    @Override
    public List<FriendDTO> getFriends(Long userId) {
        JSONObject friendObj = talkCallUtil.findFriendList(userId);
        if (friendObj == null) {
            return Lists.newArrayList();
        }
        Integer result = friendObj.getInteger("result");
        if (!Objects.equals(result, 0)) {
            log.error("获取好友列表失败，msg【{}】", friendObj);
            return Lists.newArrayList();
        }

        List<FriendDTO> resultList =
            JSON.parseArray(friendObj.getJSONObject("data").getString("records"), FriendDTO.class);
        if (CollectionUtils.isEmpty(resultList)) {
            return Lists.newArrayList();
        }
        Map<Long, FriendDTO> friendMap =
            AssembleUtil.collectionToMap(friendDao.getByUserId(userId), FriendDTO::getUserId);
        for (FriendDTO friend : resultList) {
            Long friendId = friend.getUserId();
            friend.setFriendId(friendId);
            friend.setName(friend.getUserName());
            FriendDTO friendDTO = friendMap.get(friendId);
            if (Objects.equals(FriendDTO.TYPE_INTERCOM_OBJECT, friend.getType())) {
                MonitorTypeEnum moType = null;
                if (Objects.nonNull(friendDTO) && Objects.nonNull(friendDTO.getMonitorType())) {
                    moType = MonitorTypeEnum.getByType(friendDTO.getMonitorType());
                }
                moType = Objects.isNull(moType) ? MonitorTypeEnum.PEOPLE : moType;
                friendDTO.setIconSkin(moType.getIconSkin());
            } else {
                friend.setIconSkin("userSkin");
                friend.setType(FriendDTO.TYPE_DISPATCHER);
            }
        }
        return resultList;
    }

    @Override
    public boolean addFriend(String friendJsonArr, String monitorName, Long userId) throws BusinessException {
        List<FriendDO> friends = JSON.parseArray(friendJsonArr, FriendDO.class);
        if (CollectionUtils.isEmpty(friends)) {
            throw new BusinessException("好友列表参数异常");
        }
        //删除用户原来的好友
        deleteFriend(userId);

        //先到对讲平台进行添加
        List<Map<String, Object>> friendList = new ArrayList<>();
        String username = SystemHelper.getCurrentUsername();
        friends.forEach(friend -> {
            friend.setCreateDataUsername(username);
            friendList.add(ImmutableMap.of("userId", friend.getFriendId()));
        });
        JSONObject resultBody = talkCallUtil.addFriends(friendList, userId);
        if (resultBody.getInteger("result") != 0) {
            throw new BusinessException(resultBody.getString("message"));
        }
        //同步到我们自己的平台
        friendDao.insert(friends);
        String message = String.format("给监控对象: %s 设置好友", monitorName);
        logService.addLog(getIpAddress(), message, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, "", monitorName, null);
        return true;
    }

    private void deleteFriend(Long userId) throws BusinessException {
        //获取用户好友列表
        List<FriendDTO> friends = friendDao.getByUserId(userId);
        if (friends.isEmpty()) {
            return;
        }
        List<Long> friendIds = friends.stream().map(FriendDTO::getFriendId).collect(Collectors.toList());
        JSONObject resultBody = talkCallUtil.deleteFriends(friendIds, userId);
        if (resultBody.getInteger("result") != 0) {
            throw new BusinessException(resultBody.getString("message"));
        }
        friendDao.deleteByUserId(userId);
    }

    private JSONArray getGroupTree(List<GroupDTO> groupList, List<OrganizationLdap> orgList, String type,
        boolean isOpen) {
        Map<String, OrganizationLdap> orgMap = AssembleUtil.collectionToMap(orgList, OrganizationLdap::getUuid);
        JSONArray result = new JSONArray();
        for (GroupDTO group : groupList) {
            if (!orgMap.containsKey(group.getOrgId())) {
                continue;
            }
            JSONObject treeNode = new JSONObject();
            treeNode.put("id", group.getId());
            treeNode.put("pId", orgMap.get(group.getOrgId()).getCid());
            treeNode.put("name", group.getName());
            treeNode.put("type", "assignment");
            treeNode.put("iconSkin", "assignmentSkin");
            treeNode.put("pName", orgMap.get(group.getOrgId()).getName());
            if ("single".equals(type)) {
                treeNode.put("nocheck", true);
            }
            treeNode.put("isParent", true);
            treeNode.put("open", isOpen);
            result.add(treeNode);
        }
        return result;

    }

    private JSONArray getIntercomList(String keyword, Integer status, List<GroupDTO> userGroupList) {
        List<String> monitorIds;
        Map<String, GroupDTO> groupMap;
        if (Objects.isNull(status) || Objects.equals(status, -1) || StringUtils.isNotBlank(keyword)) {
            monitorIds = getUserOwnIds(keyword);
            groupMap = AssembleUtil.collectionToMap(userGroupList, GroupDTO::getId);
        } else {
            GroupDTO groupDTO = userGroupList.get(0);
            Set<String> monitorIdSet =
                groupMonitorService.getMonitorIdsByGroupId(Collections.singletonList(groupDTO.getId()));
            monitorIds = sortIdList(monitorIdSet);
            groupMap = ImmutableMap.of(groupDTO.getId(), groupDTO);
        }
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new JSONArray();
        }
        List<RedisKey> monitorRedisKeys =
            monitorIds.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toList());
        String[] fields =
            { "id", "name", "monitorType", "userId", "intercomDeviceNumber", "simCardNumber", "callNumber", "groupId" };
        List<Map<String, String>> bindList = RedisHelper.batchGetHashMap(monitorRedisKeys, Arrays.asList(fields));
        JSONArray result = new JSONArray();
        for (Map<String, String> bindMap : bindList) {
            BindDTO bindDTO = MapUtil.mapToObj(bindMap, BindDTO.class);
            boolean isSucGenerate = Objects.nonNull(bindDTO.getUserId());
            if (Objects.equals(IntercomDTO.Status.SUCCESS_STATUS, status) && !isSucGenerate) {
                continue;
            }
            if (Objects.equals(IntercomDTO.Status.NOT_GENERATE_STATUS, status) && isSucGenerate) {
                continue;
            }
            String groupIdStr = bindDTO.getGroupId();
            if (StringUtils.isBlank(groupIdStr)) {
                continue;
            }
            String[] groupIds = groupIdStr.split(",");
            for (String groupId : groupIds) {
                if (!groupMap.containsKey(groupId)) {
                    continue;
                }
                JSONObject treeNode = bindDTO.convertToTreeNode(groupId);
                treeNode.put("deviceNumber", bindDTO.getIntercomDeviceNumber());
                treeNode.put("simcardNumber", bindDTO.getSimCardNumber());
                treeNode.put("open", true);
                result.add(treeNode);
            }
        }
        return result;
    }

    private void addGeneratorMsg(List<IntercomDTO> intercomList) {
        String logSource = LogInfo.LOG_SOURCE_PLATFORM_OPERATOR;
        if (intercomList.size() == 1) {
            IntercomDTO intercom = intercomList.get(0);
            String plateColor =
                Objects.isNull(intercom.getPlateColor()) ? null : String.valueOf(intercom.getPlateColor());
            String moName = intercom.getName();
            logService.addLog(getIpAddress(), String.format("生成对讲对象: %s", moName), logSource, "", moName, plateColor);
            return;
        }
        StringBuilder message = new StringBuilder();
        intercomList.forEach(intercom -> {
            String status = Objects.equals(intercom.getStatus(), IntercomDTO.Status.SUCCESS_STATUS) ? "成功" : "失败";
            message.append(String.format("生成对讲对象:%s%s<br/>", intercom.getName(), status));
        });
        logService.addMoreLog(getIpAddress(), message.toString(), logSource, "-", "对讲信息录入：批量生成对讲对象");
    }

    private String addOrUpdateIntercomObject(IntercomDTO intercom) {
        Map<String, String> intercomParams = intercom.convertToAddRequestParams();
        Long userId = intercom.getUserId();
        JSONObject resultMap;
        if (Objects.isNull(userId)) {
            //如果存在绑定关系, 但是未生成对讲对象(userId为空), 调用新增接口
            intercomParams.put("device.deviceId", intercom.getIntercomDeviceNumber());
            intercomParams.put("device.password", intercom.getDevicePassword());
            resultMap = talkCallUtil.addIntercomObject(intercomParams);
            if (Objects.equals(resultMap.getIntValue("result"), ErrorMessageEnum.SUCCESS_CODE)) {
                userId = resultMap.getJSONObject("data").getLong("userId");
                intercom.setUserId(userId);
                if (Objects.nonNull(userId)) {
                    //用户加入对应的群组
                    JSONObject userGroupJson = getAddUserGroupReq(intercom);
                    resultMap = talkCallUtil.addUserGroupList(userGroupJson, userId, intercomParams.get("custId"));
                }
            }
        } else {
            //如果存在绑定关系, 已生成对讲对象(userId不为空), 调用修改接口;
            //群组信息不做修改-对讲对象修改时，是先解绑在添加，状态会变成未生成状态
            intercomParams.put("ms.id", String.valueOf(userId));
            resultMap = talkCallUtil.updateIntercomObject(intercomParams);
        }
        intercom.setUserId(userId);
        int resultCode = resultMap.getIntValue("result");
        boolean isSuccess = Objects.equals(resultCode, ErrorMessageEnum.SUCCESS_CODE);
        Integer status = isSuccess ? IntercomDTO.Status.SUCCESS_STATUS : IntercomDTO.Status.FAILED_STATUS;

        //更新对讲对象的userId和生成状态
        IntercomDO intercomDO = new IntercomDO();
        intercomDO.setId(intercom.getIntercomInfoId());
        intercomDO.setUserId(userId);
        intercomDO.setStatus(status);
        intercom.setStatus(status);
        intercomDao.update(intercomDO);

        //返回生成结果
        if (isSuccess) {
            return null;
        }
        String errorMsg = 1003 == resultCode ? "监控对象名称已存在" :
            1015 == resultCode ? "个呼号码已被使用" : ErrorMessageEnum.getMessage(resultCode);
        return StringUtils.isBlank(errorMsg) ? resultMap.getString("message") : errorMsg;
    }

    private JSONObject getAddUserGroupReq(IntercomDTO intercomDTO) {
        int withKnob = Objects.nonNull(intercomDTO.getKnobNum()) && intercomDTO.getKnobNum() > 0 ? 1 : 0;
        List<GroupMonitorDTO> groupMonitors =
            groupMonitorService.getByMonitorIds(Collections.singletonList(intercomDTO.getId()));
        List<Map<String, Object>> groupList = new ArrayList<>();
        for (GroupMonitorDTO groupMonitor : groupMonitors) {
            Map<String, Object> param = new HashMap<>(16);
            param.put("groupId", groupMonitor.getIntercomGroupId());
            if (withKnob == MagicNumbers.INT_ONE) {
                param.put("knobNo", groupMonitor.getKnobNo());
            }
            groupList.add(param);
        }
        JSONObject requestParam = new JSONObject();
        requestParam.put("withKnob", withKnob);
        requestParam.put("groupList", groupList);
        return requestParam;
    }

    private List<BaseImportHandler> getConfigImportHandlers(ConfigImportHolder holder,
        List<IntercomImportDTO> importList) {
        List<BaseImportHandler> handlerList = new ArrayList<>();
        handlerList.add(new ConfigVehicleImportHandler(holder, monitorFactory.getVehicleService()));
        handlerList.add(new ConfigPeopleImportHandler(holder, monitorFactory.getPeopleService()));
        handlerList.add(new ConfigThingImportHandler(holder, monitorFactory.getThingService()));
        handlerList.add(new ConfigDeviceImportHandler(holder, deviceService, deviceNewDao));
        handlerList.add(new ConfigSimCardImportHandler(holder, simCardService, simCardNewDao));
        handlerList.add(new ConfigImportHandler(holder, configDao, null, groupMonitorService, null, configService));
        handlerList.add(new ConfigVideoChannelHandler(holder, deviceNewDao, videoChannelSettingDao));
        handlerList.add(new IntercomImportHandler(holder, importList, monitorFactory.getPeopleService(), intercomDao,
            originalModelDao, groupMonitorDao, configDao));
        return handlerList;
    }

    private JsonResultBean handle(List<BaseImportHandler> handlers, ConfigImportHolder holder) {
        //获取已经绑定定位对象的监控对象ID
        List<BindDTO> importList = holder.getImportList();
        Set<String> bindMonitorIds =
            importList.stream().filter(o -> o.getBindType().equals(Vehicle.BindType.HAS_BIND)).map(BindDTO::getId)
                .collect(Collectors.toSet());
        for (BaseImportHandler handler : handlers) {
            if (handler instanceof ConfigImportHandler) {
                getImportGroupMonitor(holder, bindMonitorIds);
            }
            if (handler instanceof IntercomImportHandler) {
                //批量生成个呼号码
                List<String> numbers;
                try {
                    numbers = callNumberService.updateAndReturnPersonCallNumbers(holder.getImportList().size());
                } catch (Exception e) {
                    return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
                }
                holder.setNumbers(numbers);
                FirstCustomer customer = talkCallUtil.getFirstCustomerInfo();
                holder.setCustomCode(Objects.isNull(customer) ? 1L : customer.getCustId());

            }
            JsonResultBean result = handler.execute();
            if (!result.isSuccess()) {
                return new JsonResultBean(JsonResultBean.FAULT, getOperationStep(handler.stage()) + "失败");
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, String.format("导入成功%d条,导入失败0条", importList.size()));
    }

    private String getOperationStep(int stage) {
        String step;
        switch (stage) {
            case 0:
                step = "导入监控对象";
                break;
            case 1:
                step = "导入终端信息";
                break;
            case 2:
                step = "导入SIM卡信息";
                break;
            case 4:
                step = "导入定位绑定信息";
                break;
            case 5:
                step = "导入对讲绑定信息";
                break;
            default:
                step = "导入对讲信息列表";
                break;
        }
        return step;
    }

    private void getImportGroupMonitor(ConfigImportHolder holder, Set<String> bindMonitorIds) {
        List<GroupMonitorDTO> bindList = groupMonitorService.getByMonitorIds(bindMonitorIds);
        Map<String, List<GroupMonitorDTO>> monitorGroupMap =
            bindList.stream().collect(Collectors.groupingBy(GroupMonitorDTO::getMonitorId));

        //构建分组与监控对象的关系
        List<GroupMonitorDO> groupMonitorList = new ArrayList<>();
        List<GroupMonitorDO> existGroup = new ArrayList<>();

        for (BindDTO bindDTO : holder.getImportList()) {
            Set<String> groupIds = new HashSet<>(Arrays.asList(bindDTO.getGroupId().split(",")));
            List<GroupMonitorDTO> groupList = monitorGroupMap.get(bindDTO.getId());
            Set<String> existGroupIds = new HashSet<>();
            int knobNo = 0;
            if (CollectionUtils.isNotEmpty(groupList)) {
                for (GroupMonitorDTO groupMonitorDTO : groupList) {
                    if (!groupIds.contains(groupMonitorDTO.getGroupId())) {
                        bindDTO.setGroupId(bindDTO.getGroupId() + "," + groupMonitorDTO.getGroupId());
                        bindDTO.setGroupName(bindDTO.getGroupName() + "," + groupMonitorDTO.getGroupName());
                    } else {
                        //若是导入的分组关系原本已经存在，更新组旋转按钮
                        knobNo++;
                        GroupMonitorDO groupMonitorDO =
                            new GroupMonitorDO(bindDTO.getId(), bindDTO.getMonitorType(), groupMonitorDTO.getGroupId());
                        groupMonitorDO.setKnobNo(knobNo);
                        groupMonitorDO.setId(groupMonitorDTO.getId());
                        existGroup.add(groupMonitorDO);
                        existGroupIds.add(groupMonitorDTO.getGroupId());
                    }
                }
            }
            for (String groupId : groupIds) {
                if (existGroupIds.contains(groupId)) {
                    continue;
                }
                GroupMonitorDO groupMonitorDO = new GroupMonitorDO(bindDTO.getId(), bindDTO.getMonitorType(), groupId);
                knobNo++;
                groupMonitorDO.setKnobNo(knobNo);
                groupMonitorList.add(groupMonitorDO);
            }
        }
        holder.setNewGroupMonitorList(groupMonitorList);
        holder.setUpdateGroupMonitorList(existGroup);
    }

    private ConfigImportHolder getHolder() {
        List<OrganizationLdap> orgList =
            userService.getCurrentUseOrgList().stream().filter(ldap -> !"organization".equals(ldap.getOu()))
                .collect(Collectors.toList());
        Map<String, String> orgNameIdMap =
            AssembleUtil.collectionToMap(orgList, OrganizationLdap::getName, OrganizationLdap::getUuid);
        Map<String, String> orgIdNameMap =
            AssembleUtil.collectionToMap(orgList, OrganizationLdap::getUuid, OrganizationLdap::getName);
        ConfigImportHolder holder = new ConfigImportHolder();
        holder.setOrgMap(orgNameIdMap);
        holder.setOrgIdNameMap(orgIdNameMap);
        holder.setGroupMaxMonitorNum(configHelper.getMaxNumberAssignmentMonitor());
        //终端类型为默认类型
        TerminalTypeInfo terminalTypeInfo = deviceNewDao.getTerminalTypeInfo("default");
        holder.setTerminalTypeMap(ImmutableMap.of("[f]F3_F3-default", "default"));
        holder.setTerminalTypeInfoList(Collections.singletonList(terminalTypeInfo));
        return holder;
    }

    /**
     * 解绑对讲对象
     * @param intercomList 对讲对象列表
     * @param isUnBind     true 解绑对讲对象  false:更新解绑对象
     * @return 解绑成功条数
     */
    private List<String> unBind(List<IntercomDTO> intercomList, boolean isUnBind) {
        Set<String> fuzzyFields = new HashSet<>();
        Set<String> monitorIds = new HashSet<>();
        Set<String> configIds = new HashSet<>();
        Set<String> intercomInfoIds = new HashSet<>();
        Set<String> peopleIds = new HashSet<>();
        Set<RedisKey> monitorRedisKeys = new HashSet<>();
        Set<String> callNumbers = new HashSet<>();

        String format = "解除对讲绑定关系：监控对象(%s)/群组(%s)/对讲设备标识(%s)/SIM卡(%s)<br/>";
        List<String> successMsg = new ArrayList<>();
        for (IntercomDTO intercom : intercomList) {
            String deleteMsg = deleteUserToPlatform(intercom);
            if (StringUtils.isNotBlank(deleteMsg)) {
                intercom.setErrorMsg(deleteMsg);
                continue;
            }
            String moType = intercom.getMonitorType();
            String moName = intercom.getName();
            String intercomDeviceNum = intercom.getIntercomDeviceNumber();
            String simNum = intercom.getSimCardNumber();

            String fuzzyFiled = FuzzySearchUtil.buildField(moType, moName, intercomDeviceNum, simNum);
            fuzzyFields.add(fuzzyFiled);
            monitorIds.add(intercom.getId());
            configIds.add(intercom.getConfigId());
            intercomInfoIds.add(intercom.getIntercomInfoId());
            if (Objects.equals(moType, MonitorTypeEnum.PEOPLE.getType())) {
                peopleIds.add(intercom.getId());
            }
            successMsg.add(String.format(format, moName, intercom.getGroupName(), intercomDeviceNum, simNum));
            monitorRedisKeys.add(RedisKeyEnum.MONITOR_INFO.of(intercom.getId()));
            callNumbers.add(intercom.getCallNumber());
        }
        if (monitorIds.isEmpty()) {
            return successMsg;
        }
        // 删除对讲对象
        intercomDao.deleteByIds(intercomInfoIds);
        if (isUnBind) {
            //删除群组组中的旋钮按钮
            groupMonitorDao.clearKnobNo(monitorIds);
            //删除信息配置与对讲的绑定关系
            intercomDao.clearConfigIntercomId(configIds);
            // 释放个呼号码
            callNumberService.updateAndRecyclePersonCallNumberBatch(callNumbers);
        }
        if (!peopleIds.isEmpty()) {
            monitorFactory.getPeopleService().updateIncumbency(peopleIds, 1);
        }

        //删除对讲缓存
        RedisHelper.delListItem(RedisKeyEnum.INTERCOM_SORT_LIST.of(), monitorIds);
        RedisHelper.hdel(RedisKeyEnum.FUZZY_INTERCOM.of(), fuzzyFields);
        List<String> fields = Arrays.asList("intercomInfoId", "intercomDeviceNumber", "userId", "callNumber");
        RedisHelper.hdel(monitorRedisKeys, fields);
        return successMsg;
    }

    private String deleteUserToPlatform(IntercomDTO intercom) {
        Long userId = intercom.getUserId();
        if (Objects.isNull(userId)) {
            return null;
        }
        JSONObject deleteObject = new JSONObject();
        deleteObject.put("userId", userId);
        JSONObject result = talkCallUtil.deleteIntercomObject(deleteObject);
        int deleteResult = result.getIntValue("result");
        if (deleteResult == ErrorMessageEnum.SUCCESS_CODE) {
            return null;
        }
        // 删除失败
        String format = "对讲对象:%s删除异常,异常信息:%s";
        return String.format(format, intercom.getName(), result.getString("message"));
    }

    /**
     * 获取群组id和名称的map
     * @return map
     */
    private Map<String, String> getGroupMap() {
        List<GroupDTO> groupList = userService.getCurrentUserGroupList();
        return groupList.stream().filter(o -> Objects.equals(o.getTypes(), INTERCOM_GROUP_TYPE))
            .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
    }

    /**
     * 监听定位对讲解绑
     * @param configUnBindEvent 监控对象解绑事件
     */
    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent configUnBindEvent) {
        if (!"unbind".equals(configUnBindEvent.getOperation())) {
            return;
        }
        Collection<BindDTO> unbindList = configUnBindEvent.getUnbindList();
        List<IntercomDTO> intercomList = new ArrayList<>();
        for (BindDTO bindDTO : unbindList) {
            if (StringUtils.isBlank(bindDTO.getIntercomInfoId())) {
                continue;
            }
            IntercomDTO intercomDTO = new IntercomDTO();
            BeanUtils.copyProperties(bindDTO, intercomDTO);
            intercomList.add(intercomDTO);
        }
        if (!intercomList.isEmpty()) {
            unBind(intercomList, true);
        }
    }

    @EventListener
    public void listenConfigUpdate(ConfigUpdateEvent updateEvent) {
        BindDTO curBind = updateEvent.getCurBindDTO();
        //当是对讲信息配置本身触发的，不做任何处理
        if (StringUtils.isNotBlank(curBind.getIntercomInfoId())) {
            return;
        }
        BindDTO oldBind = updateEvent.getOldBindDTO();

        //若不是对讲对象，直接返回
        if (StringUtils.isBlank(oldBind.getIntercomInfoId())) {
            return;
        }
        //检查监控对象、sim卡、群组是否发生改变，未发生改变，对讲信息列表也无需做改变
        boolean monitorIsChange = !Objects.equals(curBind.getName(), oldBind.getName());
        boolean simIsChange = !Objects.equals(curBind.getSimCardNumber(), oldBind.getSimCardNumber());
        boolean groupIsChange = isGroupChange(curBind.getGroupId(), oldBind.getGroupId());
        if (groupIsChange) {
            //todo 检查是否是对讲群组发生变化
        }
        if (!(monitorIsChange || simIsChange || groupIsChange)) {
            return;
        }

        //进行更新
        IntercomDTO oldIntercom = new IntercomDTO();
        BeanUtils.copyProperties(oldBind, oldIntercom);

        IntercomDTO intercom = new IntercomDTO();
        BeanUtils.copyProperties(intercom, oldIntercom);
        intercom.setSimCardId(curBind.getSimCardId());
        intercom.setSimCardNumber(curBind.getSimCardNumber());
        intercom.setDeviceId(curBind.getDeviceId());
        intercom.setDeviceNumber(curBind.getDeviceNumber());
        intercom.setGroupId(curBind.getGroupId());
        intercom.setGroupName(curBind.getGroupName());
        intercom.setName(curBind.getName());
        intercom.setId(curBind.getName());
        try {
            update(intercom, oldIntercom, false);
        } catch (BusinessException e) {
            log.error("监听到信息配置变更同步修改对讲信失败", e);
        }

        // todo 分组发生改变，重新维护分组的组旋钮位置编号
        // todo 若原对讲对象是生成状态，进行生成
    }

}
