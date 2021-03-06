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
 * ??????????????????
 * ???????????????????????????????????????sim?????????
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
     * ??????????????????????????????
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
        log.info("?????????????????????????????????redis?????????.");
        //?????????????????????????????????????????????????????????????????????
        List<String> sortMonitorIds = intercomDao.getSortList();
        //??????????????????????????????
        RedisHelper.delete(RedisKeyEnum.INTERCOM_SORT_LIST.of());
        //??????????????????????????????
        RedisHelper.addToListTop(RedisKeyEnum.INTERCOM_SORT_LIST.of(), sortMonitorIds);
        log.info("???????????????????????????redis?????????.");
    }

    @Override
    public boolean add(IntercomDTO intercom) throws Exception {
        //??????????????????
        checkAdd(intercom);

        // ?????????????????? ??????????????????????????????????????????????????????????????????
        intercom.setCallNumber(callNumberService.updateAndReturnPersonCallNumber());
        String id = UUID.randomUUID().toString();
        FirstCustomer firstCustomer = talkCallUtil.getFirstCustomerInfo();
        intercom.setCustomerCode(Objects.isNull(firstCustomer) ? 1L : firstCustomer.getCustId());

        //?????????????????????
        bindConfig(intercom, id);

        //???????????????????????????????????????????????????????????????????????????
        if (MonitorTypeEnum.PEOPLE.getType().equals(intercom.getMonitorType()) && null != intercom.getId()) {
            monitorFactory.getPeopleService().updateIncumbency(intercom.getId(), 2);
        }

        //???????????????????????????????????????
        addIntercomToMysql(intercom, id);

        //??????????????????
        RedisHelper.addToListTop(RedisKeyEnum.INTERCOM_SORT_LIST.of(), intercom.getId());
        //????????????????????????
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_INTERCOM.of(), buildFuzzyMap(intercom));

        //????????????
        String msg = String.format("????????????????????? %s(????????????),%s(??????????????????),%s(???????????????)???????????????", intercom.getName(),
            intercom.getIntercomDeviceNumber(), intercom.getSimCardNumber());
        logService.addLog(getIpAddress(), msg, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, intercom.getName(), "?????????????????????????????????");
        return true;
    }

    private void bindConfig(IntercomDTO intercom, String id) throws BusinessException {
        //???????????????????????????
        ConfigDTO config = new ConfigDTO();
        BeanUtils.copyProperties(intercom, config);
        config.setIntercomInfoId(id);
        configService.add(config);

        //??????????????????sim????????????
        intercom.setSimCardId(config.getSimCardId());
        intercom.setId(config.getId());
        intercom.setOrgId(config.getOrgId());
        intercom.setDeviceId(config.getDeviceId());
    }

    private void checkAdd(IntercomDTO intercom) throws BusinessException {
        checkGroup(intercom.getGroupId());
        //???????????????????????????????????????id???name????????????????????????
        OriginalModelInfo originalModel = originalModelDao.getOriginalModelByModelId(intercom.getOriginalModelId());
        if (Objects.isNull(originalModel)) {
            throw new BusinessException("?????????????????????!");
        }
        intercom.setIntercomDeviceNumber(originalModel.getModelId() + intercom.getDeviceNumber());
        //????????????????????????????????????????????????
        if (Objects.nonNull(intercomDao.getByIntercomDeviceNum(intercom.getIntercomDeviceNumber()))) {
            throw new BusinessException("?????????????????????????????????!");
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
        // todo ?????????????????????????????????????????????????????????????????????????????????????????????
        return false;
    }

    @Override
    public Map<String, Object> checkIsBind(IntercomDTO bindDTO) {
        String moName = bindDTO.getName();
        String simNum = bindDTO.getSimCardNumber();
        String deviceNum = bindDTO.getDeviceNumber();
        String moType = bindDTO.getMonitorType();

        //?????????????????????????????????
        BindDTO monitor = monitorFactory.create(moType).getByName(moName);
        Map<String, Object> result = ImmutableMap.of("isBindLocateObject", false, "isBindIntercom", false);
        //????????????????????????
        if (Objects.nonNull(monitor) && Objects.equals(monitor.getBindType(), Vehicle.BindType.HAS_BIND)) {
            result.put("monitor", monitor);
            result.put("isBindLocateObject", true);
            if (StringUtils.isNotBlank(monitor.getIntercomDeviceNumber())) {
                result.put("msg", "????????????" + moName + "????????????");
                result.put("isBindIntercom", true);
                return result;
            }
            if (simNum.equals(monitor.getSimCardNumber()) && deviceNum.equals(monitor.getDeviceNumber())) {
                return result;
            }
            result.put("msg", "???????????????" + moName + "????????????????????????????????????????????????????????????????????????????????????");
            return result;
        }

        // ??????????????????????????????
        if (deviceService.checkIsBind(deviceNum)) {
            result.put("isBindLocateObject", true);
            result.put("msg", "????????????" + deviceNum + "???????????????,??????????????????????????????????????????????????????????????????");
            return result;
        }

        // ??????SIM?????????????????????
        if (simCardService.checkIsBind(simNum)) {
            result.put("isBindLocateObject", true);
            result.put("msg", "??????????????????" + simNum + "???????????????,????????????????????????????????????????????????????????????");
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
        String msg = isBindLocateObject ? inputValue + "??????????????????????????????" : "";
        msg = isBindIntercom ? inputValue + "???????????????????????????" : msg;

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
        //???????????????????????????????????????
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
        //????????????id????????????????????????
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
        //??????????????????????????????
        IntercomDTO oldIntercom = intercomDao.getDetailByConfigId(intercom.getConfigId());
        if (Objects.isNull(oldIntercom)) {
            throw new BusinessException("?????????????????????");
        }

        //?????????????????????????????????
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
        //??????????????????????????? todo zj ?????????????????????????????????????????????,??????????????????
        int successCount = unBind(Collections.singletonList(oldIntercom), false).size();
        if (successCount == 0) {
            throw new BusinessException("?????????????????????????????????");
        }
        //??????????????????
        intercom.setCallNumber(oldIntercom.getCallNumber());
        //????????????????????????
        if (isUpdateConfig) {
            ConfigDTO bindDTO = new ConfigDTO();
            BeanUtils.copyProperties(intercom, bindDTO);
            bindDTO.setIntercomInfoId(oldIntercom.getIntercomInfoId());
            configService.update(bindDTO, oldIntercom.getId());
        }

        //??????????????????????????????
        addIntercomToMysql(intercom, oldIntercom.getIntercomInfoId());

        //???????????????????????????????????????????????????????????????????????????
        if (MonitorTypeEnum.PEOPLE.getType().equals(intercom.getMonitorType())) {
            monitorFactory.getPeopleService().updateIncumbency(intercom.getId(), 2);
        }

        //????????????
        updateIntercomCache(intercom, oldIntercom);
        return true;
    }

    private void addUpdateLog(IntercomDTO curIntercom, IntercomDTO oldIntercom) {
        String lastInfo = String
            .format("??????????????????:<br/>????????????(%s)/??????(%s)/??????????????????(%s)/???????????????(%s) /?????????(%s)/??????(%s????????????,%s????????????,%s??????????????????)???<br/>",
                oldIntercom.getName(), oldIntercom.getGroupName(), oldIntercom.getIntercomDeviceNumber(),
                oldIntercom.getSimCardNumber(), oldIntercom.getPriority(), switchSuport(oldIntercom.getTextEnable()),
                switchSuport(oldIntercom.getImageEnable()), switchSuport(oldIntercom.getAudioEnable()));
        String newInfo = String.format("????????????(%s)/??????(%s)/??????????????????(%s)/???????????????(%s) /?????????(%s)/??????((%s????????????,(%s????????????,(%s??????????????????)",
            curIntercom.getName(), curIntercom.getGroupName(), curIntercom.getIntercomDeviceNumber(),
            curIntercom.getSimCardNumber(), curIntercom.getPriority(), switchSuport(curIntercom.getTextEnable()),
            switchSuport(curIntercom.getImageEnable()), switchSuport(curIntercom.getAudioEnable()));
        logService
            .addMoreLog(getIpAddress(), lastInfo + newInfo, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, oldIntercom.getName(),
                "???????????????????????????????????????");
    }

    private String switchSuport(Integer num) {
        if (num != null && num == 1) {
            return "??????";
        }
        return "?????????";
    }

    private void updateIntercomCache(IntercomDTO curIntercom, IntercomDTO oldIntercom) {
        //?????????????????????????????????
        String oldField = FuzzySearchUtil
            .buildField(oldIntercom.getMonitorType(), oldIntercom.getName(), oldIntercom.getIntercomDeviceNumber(),
                oldIntercom.getSimCardNumber());
        RedisHelper.hdel(RedisKeyEnum.FUZZY_INTERCOM.of(), oldField);
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_INTERCOM.of(), buildFuzzyMap(curIntercom));

        String newMonitorId = curIntercom.getId();
        //????????????????????????????????????????????????
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
        //???????????????????????????????????????id???name????????????????????????
        OriginalModelInfo originalModel = originalModelDao.getOriginalModelByModelId(curIntercom.getOriginalModelId());
        if (Objects.isNull(originalModel)) {
            throw new BusinessException("?????????????????????!");
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
                throw new BusinessException("?????????????????????????????????!");
            }
        }
        //??????????????????????????????
        boolean moIsChang = !curMoName.equals(oldMoName);
        if (moIsChang) {
            BindDTO bindDTO = monitorFactory.create(curIntercom.getMonitorType()).getByName(curMoName);
            if (Objects.nonNull(bindDTO) && Objects.equals(bindDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                throw new BusinessException("???????????????" + curMoName + "??????????????????");
            }
            //?????????????????????????????????????????????ID???????????????????????????????????????????????????
            String monitorId = Objects.nonNull(bindDTO) ? bindDTO.getId() : oldIntercom.getId();
            String orgId = Objects.nonNull(bindDTO) ? bindDTO.getOrgId() : oldIntercom.getOrgId();
            curIntercom.setId(monitorId);
            curIntercom.setOrgId(orgId);
        }

        //????????????
        boolean deviceIsChange = !curDeviceNum.equals(oldDeviceNum);
        if (deviceIsChange) {
            DeviceDTO deviceDTO = deviceService.getByNumber(curDeviceNum);
            String deviceId = Objects.isNull(deviceDTO) ? oldIntercom.getDeviceId() : deviceDTO.getId();
            if (Objects.nonNull(deviceDTO) && StringUtils.isNotBlank(deviceDTO.getBindId())) {
                throw new BusinessException("????????????" + curDeviceNum + "??????????????????");
            }
            curIntercom.setDeviceId(deviceId);
        } else {
            curIntercom.setDeviceId(oldIntercom.getDeviceId());
        }

        //??????SIM??????
        boolean simIsChange = !curSimNum.equals(oldSimNum);
        if (simIsChange) {
            SimCardDTO simCardDTO = simCardService.getByNumber(curSimNum);
            String simCardId = Objects.isNull(simCardDTO) ? oldIntercom.getSimCardId() : simCardDTO.getId();
            if (Objects.nonNull(simCardDTO) && StringUtils.isNotBlank(simCardDTO.getBindId())) {
                throw new BusinessException("??????????????????" + curSimNum + "??????????????????");
            }
            curIntercom.setSimCardId(simCardId);
        }

        boolean groupIsChange = isGroupChange(curIntercom.getGroupId(), oldIntercom.getGroupId());
        return moIsChang || deviceIsChange || simIsChange || groupIsChange;
    }

    private boolean isGroupChange(String curGroupId, String oldGroupId) {
        //??????????????????????????????
        Set<String> curGroupIds = new HashSet<>(Arrays.asList(curGroupId.split(",")));
        Set<String> oldGroupIds = new HashSet<>(Arrays.asList(oldGroupId.split(",")));
        int curGroupSize = curGroupIds.size();
        int oldGroupSize = oldGroupIds.size();

        //?????????????????????????????????????????????????????????????????????
        return curGroupSize != oldGroupSize || curGroupIds.retainAll(oldGroupIds);
    }

    private void checkGroup(String groupId) throws BusinessException {
        if (StringUtils.isBlank(groupId)) {
            throw new BusinessException("???????????????!");
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
            throw new BusinessException("?????????????????????");
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
            operation = "???????????????????????????????????????????????????";
        } else {
            operation = "?????????????????????????????????????????????";
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
            throw new BusinessException("?????????????????????");
        }
        return unBind(intercomList, true).size();
    }

    @Override
    public List<String> getUserOwnIds(String keyword) {
        Set<String> userOwnIds = userService.getCurrentUserMonitorIds();
        if (CollectionUtils.isEmpty(userOwnIds)) {
            return new ArrayList<>();
        }
        //??????????????????
        if (StringUtils.isNotBlank(keyword)) {
            Set<String> filterIds = FuzzySearchUtil.scan(RedisKeyEnum.FUZZY_INTERCOM.of(), keyword);
            //?????????
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
        //????????????
        Long start = query.getStart();
        Long end = Math.min(start + query.getLength(), monitorIds.size());
        if (start > end) {
            return RedisQueryUtil.getListToPage(new ArrayList<>(), query, monitorIds.size());
        }
        List<String> subList = monitorIds.subList(Integer.valueOf(start + ""), Integer.valueOf(end + ""));

        //?????????????????????id???name???????????????
        List<OrganizationLdap> orgList = organizationService.getAllOrganization();
        Map<String, String> orgMap =
            AssembleUtil.collectionToMap(orgList, OrganizationLdap::getUuid, OrganizationLdap::getName);

        //??????????????????????????????id???name???????????????
        Map<String, String> groupMap = getGroupMap();
        List<IntercomDTO> sortList = getSortList(subList, orgMap, groupMap);

        return RedisQueryUtil.getListToPage(sortList, query, monitorIds.size());
    }

    private List<IntercomDTO> getSortList(List<String> sortIds, Map<String, String> orgMap,
        Map<String, String> groupMap) {
        List<IntercomDTO> intercomList = intercomDao.getDetailByMonitorIds(sortIds);
        Map<String, IntercomDTO> intercomMap = AssembleUtil.collectionToMap(intercomList, IntercomDTO::getId);
        //?????????????????????????????????????????????
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
        //???????????????????????????????????????
        List<String> monitorIds = getUserOwnIds(null);
        if (CollectionUtils.isEmpty(monitorIds)) {
            throw new BusinessException("??????????????????");
        }

        //?????????????????????id????????????????????????
        List<OrganizationLdap> orgList = organizationService.getAllOrganization();
        Map<String, String> orgMap =
            AssembleUtil.collectionToMap(orgList, OrganizationLdap::getUuid, OrganizationLdap::getName);

        //??????????????????????????????id???name???????????????
        Map<String, String> groupMap = getGroupMap();

        List<List<String>> cutList = cutList(monitorIds);
        List<IntercomExportDTO> exportList = new ArrayList<>();
        for (List<String> subList : cutList) {
            List<IntercomDTO> sortList = getSortList(subList, orgMap, groupMap);
            for (IntercomDTO intercomDTO : sortList) {
                exportList.add(new IntercomExportDTO(intercomDTO));
            }
        }
        //????????????
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
        //????????????
        msg.put("originalModelList", originalModelDao.findAllOriginalModelInfo());
        // ????????????
        msg.put("vehicleInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.VEHICLE.getType()));
        // ????????????
        msg.put("peopleInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.PEOPLE.getType()));
        // ????????????
        msg.put("thingInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.THING.getType()));

        // sim?????????
        msg.put("simCardInfoList", simCardService.getUbBindSelectList(null));
        return msg;
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        // ??????
        List<String> headList = new ArrayList<>();
        headList.add("???????????????");
        headList.add("????????????");
        headList.add("????????????(7???)");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("??????????????????");
        headList.add("????????????");
        headList.add("??????(???????????????????????????)");
        headList.add("?????????");

        // ????????????
        List<String> requiredList = new ArrayList<>();
        requiredList.add("???????????????");
        requiredList.add("????????????");
        requiredList.add("????????????(7???)");
        requiredList.add("????????????");
        requiredList.add("????????????");
        requiredList.add("??????????????????");
        requiredList.add("????????????");
        requiredList.add("??????(???????????????????????????)");

        // ????????????????????????
        List<Object> exportList = new ArrayList<>();
        exportList.add("18600222931");
        exportList.add("4A11G");
        exportList.add("0002055");
        exportList.add("ik9u5hme");
        exportList.add("IW2055");
        exportList.add("???");

        // ????????????????????????????????????
        String[] orgNames = userService.getCurrentUseOrgList().stream().filter(o -> !"organization".equals(o.getOu()))
            .map(OrganizationLdap::getName).toArray(String[]::new);
        exportList.add(orgNames[0]);
        exportList.add("????????????@??????????????????");
        exportList.add("1");

        // ?????????????????????map
        Map<String, String[]> selectMap = new HashMap<>(16);
        String[] monitorType = { "???", "???", "???" };
        selectMap.put("??????????????????", monitorType);
        // ????????????
        String[] priority = { "1", "2", "3", "4", "5" };
        selectMap.put("?????????", priority);
        selectMap.put("????????????", orgNames);

        String[] originalModels =
            originalModelDao.findAllOriginalModelInfo().stream().map(OriginalModelInfo::getModelId)
                .toArray(String[]::new);
        selectMap.put("????????????", originalModels);
        //????????????
        ExportExcelUtil.writeTemplateToFile(headList, requiredList, selectMap, exportList, response);
        return true;
    }

    @Override
    @ImportLock(ImportModule.CONFIG)
    public JsonResultBean importFile(MultipartFile file) throws Exception {
        //????????????
        ImportExcel importExcel = new ImportExcel(file, 1, 0);

        //??????????????????????????????
        List<GroupDTO> groupList = userService.getCurrentUserGroupList().stream()
            .filter(o -> Objects.equals(o.getTypes(), INTERCOM_GROUP_TYPE)).collect(Collectors.toList());
        if (groupList.isEmpty()) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????");
        }

        //???????????????????????????????????????
        ConfigImportHolder holder = getHolder();
        ImportValidator<IntercomImportDTO> validator =
            new IntercomImportValidator(holder, monitorFactory, deviceNewDao, simCardNewDao, intercomDao,
                originalModelDao, groupMonitorService, groupList);
        importExcel.setImportValidator(validator);

        //??????????????????
        List<IntercomImportDTO> importList = importExcel.getDataListNew(IntercomImportDTO.class);
        if (CollectionUtils.isEmpty(importList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????");
        }

        //??????????????????
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
            // ????????????f3??????????????????.
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
        String sucMessage = String.format("????????????%d?????????", importList.size());
        logService.addLog(getIpAddress(), sucMessage, "3", "??????????????????", "????????????");
        return resultBean;
    }

    @Override
    public JsonResultBean addToIntercomPlatform(Collection<String> configIds) {
        List<IntercomDTO> intercomList = intercomDao.getDetailByConfigIds(configIds);
        if (intercomList.isEmpty()) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
        }
        StringBuilder errorMsg = new StringBuilder();
        Map<RedisKey, Map<String, String>> monitorCache = new HashMap<>(CommonUtil.ofMapCapacity(intercomList.size()));
        for (IntercomDTO intercom : intercomList) {
            String errMsg = addOrUpdateIntercomObject(intercom);
            if (StringUtils.isBlank(errMsg)) {
                Map<String, String> userIdMap = ImmutableMap.of("userId", String.valueOf(intercom.getUserId()));
                monitorCache.put(RedisKeyEnum.MONITOR_INFO.of(intercom.getId()), userIdMap);
            } else {
                errorMsg.append(String.format("???????????????%s???????????????:%s<br/>", intercom.getName(), errMsg));
            }
        }
        //??????????????????
        RedisHelper.batchAddToHash(monitorCache);
        //??????????????????
        addGeneratorMsg(intercomList);
        boolean isSuccess = errorMsg.length() == 0;
        return new JsonResultBean(isSuccess, isSuccess ? "????????????????????????" : errorMsg.toString());
    }

    @Override
    public boolean updateRecordStatus(String configId, Integer recordEnable) throws BusinessException {
        IntercomDTO intercomDTO = intercomDao.getDetailByConfigId(configId);
        if (Objects.isNull(intercomDTO)) {
            throw new BusinessException("????????????????????????");
        }
        if (Objects.isNull(intercomDTO.getUserId())) {
            throw new BusinessException("?????????????????????");
        }
        // ?????????????????????
        JSONObject resultBody = talkCallUtil.updateRecordStatus(recordEnable, intercomDTO.getUserId());
        if (Objects.equals(resultBody.getInteger("result"), 0)) {
            throw new BusinessException(resultBody.getString("message"));
        }
        //????????????????????????
        IntercomDO intercomDO = new IntercomDO();
        intercomDO.setId(intercomDTO.getId());
        intercomDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        intercomDO.setRecordEnable(recordEnable);
        intercomDao.update(intercomDO);
        //????????????
        String message =
            String.format("%s???%s?????????", IntercomDTO.getRecordEnableFormat(recordEnable), intercomDTO.getName());
        logService.addLog(getIpAddress(), message, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, "");
        return true;
    }

    @Override
    public JSONArray getIntercomBaseTree(String queryType, String type, String keyword, Integer status,
        boolean isFilterNullOrg) {
        //??????????????????????????????
        List<OrganizationLdap> userOrgList = userService.getCurrentUseOrgList();
        boolean keywordNoBlank = StringUtils.isNotBlank(keyword);
        if (Objects.equals(queryType, "org") && keywordNoBlank) {
            userOrgList = userOrgList.stream().filter(o -> o.getName().contains(keyword)).collect(Collectors.toList());
        }
        // todo zj ?????????????????????????????? ????????????????????????
        List<GroupDTO> userGroupList =
            userService.getCurrentUserGroupList().stream().filter(o -> Objects.equals(o.getTypes(), "1"))
                .collect(Collectors.toList());
        //????????????????????????????????????
        if (isFilterNullOrg) {
            Set<String> orgIds = userGroupList.stream().map(GroupDTO::getOrgId).collect(Collectors.toSet());
            userOrgList = OrganizationUtil.filterOrgListByUuid(userOrgList, orgIds);
        }
        if (userOrgList.isEmpty() || userGroupList.isEmpty()) {
            return new JSONArray();
        }
        //??????????????????
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
        //??????????????????????????????
        List<OrganizationLdap> orgList = userService.getCurrentUseOrgList();

        //?????????????????????????????????????????????
        List<UserDTO> userBeans;
        if (StringUtils.isNotBlank(keyword)) {
            //todo zj ????????????????????????
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
        //?????????????????????????????????????????????????????????
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
            log.error("???????????????????????????msg???{}???", friendObj);
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
            throw new BusinessException("????????????????????????");
        }
        //???????????????????????????
        deleteFriend(userId);

        //??????????????????????????????
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
        //??????????????????????????????
        friendDao.insert(friends);
        String message = String.format("???????????????: %s ????????????", monitorName);
        logService.addLog(getIpAddress(), message, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, "", monitorName, null);
        return true;
    }

    private void deleteFriend(Long userId) throws BusinessException {
        //????????????????????????
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
            logService.addLog(getIpAddress(), String.format("??????????????????: %s", moName), logSource, "", moName, plateColor);
            return;
        }
        StringBuilder message = new StringBuilder();
        intercomList.forEach(intercom -> {
            String status = Objects.equals(intercom.getStatus(), IntercomDTO.Status.SUCCESS_STATUS) ? "??????" : "??????";
            message.append(String.format("??????????????????:%s%s<br/>", intercom.getName(), status));
        });
        logService.addMoreLog(getIpAddress(), message.toString(), logSource, "-", "?????????????????????????????????????????????");
    }

    private String addOrUpdateIntercomObject(IntercomDTO intercom) {
        Map<String, String> intercomParams = intercom.convertToAddRequestParams();
        Long userId = intercom.getUserId();
        JSONObject resultMap;
        if (Objects.isNull(userId)) {
            //????????????????????????, ???????????????????????????(userId??????), ??????????????????
            intercomParams.put("device.deviceId", intercom.getIntercomDeviceNumber());
            intercomParams.put("device.password", intercom.getDevicePassword());
            resultMap = talkCallUtil.addIntercomObject(intercomParams);
            if (Objects.equals(resultMap.getIntValue("result"), ErrorMessageEnum.SUCCESS_CODE)) {
                userId = resultMap.getJSONObject("data").getLong("userId");
                intercom.setUserId(userId);
                if (Objects.nonNull(userId)) {
                    //???????????????????????????
                    JSONObject userGroupJson = getAddUserGroupReq(intercom);
                    resultMap = talkCallUtil.addUserGroupList(userGroupJson, userId, intercomParams.get("custId"));
                }
            }
        } else {
            //????????????????????????, ?????????????????????(userId?????????), ??????????????????;
            //????????????????????????-??????????????????????????????????????????????????????????????????????????????
            intercomParams.put("ms.id", String.valueOf(userId));
            resultMap = talkCallUtil.updateIntercomObject(intercomParams);
        }
        intercom.setUserId(userId);
        int resultCode = resultMap.getIntValue("result");
        boolean isSuccess = Objects.equals(resultCode, ErrorMessageEnum.SUCCESS_CODE);
        Integer status = isSuccess ? IntercomDTO.Status.SUCCESS_STATUS : IntercomDTO.Status.FAILED_STATUS;

        //?????????????????????userId???????????????
        IntercomDO intercomDO = new IntercomDO();
        intercomDO.setId(intercom.getIntercomInfoId());
        intercomDO.setUserId(userId);
        intercomDO.setStatus(status);
        intercom.setStatus(status);
        intercomDao.update(intercomDO);

        //??????????????????
        if (isSuccess) {
            return null;
        }
        String errorMsg = 1003 == resultCode ? "???????????????????????????" :
            1015 == resultCode ? "????????????????????????" : ErrorMessageEnum.getMessage(resultCode);
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
        //?????????????????????????????????????????????ID
        List<BindDTO> importList = holder.getImportList();
        Set<String> bindMonitorIds =
            importList.stream().filter(o -> o.getBindType().equals(Vehicle.BindType.HAS_BIND)).map(BindDTO::getId)
                .collect(Collectors.toSet());
        for (BaseImportHandler handler : handlers) {
            if (handler instanceof ConfigImportHandler) {
                getImportGroupMonitor(holder, bindMonitorIds);
            }
            if (handler instanceof IntercomImportHandler) {
                //????????????????????????
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
                return new JsonResultBean(JsonResultBean.FAULT, getOperationStep(handler.stage()) + "??????");
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, String.format("????????????%d???,????????????0???", importList.size()));
    }

    private String getOperationStep(int stage) {
        String step;
        switch (stage) {
            case 0:
                step = "??????????????????";
                break;
            case 1:
                step = "??????????????????";
                break;
            case 2:
                step = "??????SIM?????????";
                break;
            case 4:
                step = "????????????????????????";
                break;
            case 5:
                step = "????????????????????????";
                break;
            default:
                step = "????????????????????????";
                break;
        }
        return step;
    }

    private void getImportGroupMonitor(ConfigImportHolder holder, Set<String> bindMonitorIds) {
        List<GroupMonitorDTO> bindList = groupMonitorService.getByMonitorIds(bindMonitorIds);
        Map<String, List<GroupMonitorDTO>> monitorGroupMap =
            bindList.stream().collect(Collectors.groupingBy(GroupMonitorDTO::getMonitorId));

        //????????????????????????????????????
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
                        //?????????????????????????????????????????????????????????????????????
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
        //???????????????????????????
        TerminalTypeInfo terminalTypeInfo = deviceNewDao.getTerminalTypeInfo("default");
        holder.setTerminalTypeMap(ImmutableMap.of("[f]F3_F3-default", "default"));
        holder.setTerminalTypeInfoList(Collections.singletonList(terminalTypeInfo));
        return holder;
    }

    /**
     * ??????????????????
     * @param intercomList ??????????????????
     * @param isUnBind     true ??????????????????  false:??????????????????
     * @return ??????????????????
     */
    private List<String> unBind(List<IntercomDTO> intercomList, boolean isUnBind) {
        Set<String> fuzzyFields = new HashSet<>();
        Set<String> monitorIds = new HashSet<>();
        Set<String> configIds = new HashSet<>();
        Set<String> intercomInfoIds = new HashSet<>();
        Set<String> peopleIds = new HashSet<>();
        Set<RedisKey> monitorRedisKeys = new HashSet<>();
        Set<String> callNumbers = new HashSet<>();

        String format = "???????????????????????????????????????(%s)/??????(%s)/??????????????????(%s)/SIM???(%s)<br/>";
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
        // ??????????????????
        intercomDao.deleteByIds(intercomInfoIds);
        if (isUnBind) {
            //?????????????????????????????????
            groupMonitorDao.clearKnobNo(monitorIds);
            //??????????????????????????????????????????
            intercomDao.clearConfigIntercomId(configIds);
            // ??????????????????
            callNumberService.updateAndRecyclePersonCallNumberBatch(callNumbers);
        }
        if (!peopleIds.isEmpty()) {
            monitorFactory.getPeopleService().updateIncumbency(peopleIds, 1);
        }

        //??????????????????
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
        // ????????????
        String format = "????????????:%s????????????,????????????:%s";
        return String.format(format, intercom.getName(), result.getString("message"));
    }

    /**
     * ????????????id????????????map
     * @return map
     */
    private Map<String, String> getGroupMap() {
        List<GroupDTO> groupList = userService.getCurrentUserGroupList();
        return groupList.stream().filter(o -> Objects.equals(o.getTypes(), INTERCOM_GROUP_TYPE))
            .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
    }

    /**
     * ????????????????????????
     * @param configUnBindEvent ????????????????????????
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
        //????????????????????????????????????????????????????????????
        if (StringUtils.isNotBlank(curBind.getIntercomInfoId())) {
            return;
        }
        BindDTO oldBind = updateEvent.getOldBindDTO();

        //????????????????????????????????????
        if (StringUtils.isBlank(oldBind.getIntercomInfoId())) {
            return;
        }
        //?????????????????????sim???????????????????????????????????????????????????????????????????????????????????????
        boolean monitorIsChange = !Objects.equals(curBind.getName(), oldBind.getName());
        boolean simIsChange = !Objects.equals(curBind.getSimCardNumber(), oldBind.getSimCardNumber());
        boolean groupIsChange = isGroupChange(curBind.getGroupId(), oldBind.getGroupId());
        if (groupIsChange) {
            //todo ???????????????????????????????????????
        }
        if (!(monitorIsChange || simIsChange || groupIsChange)) {
            return;
        }

        //????????????
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
            log.error("??????????????????????????????????????????????????????", e);
        }

        // todo ???????????????????????????????????????????????????????????????
        // todo ????????????????????????????????????????????????
    }

}
