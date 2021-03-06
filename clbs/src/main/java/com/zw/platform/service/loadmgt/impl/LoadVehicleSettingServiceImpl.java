package com.zw.platform.service.loadmgt.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.loadmgt.LoadSettingParam;
import com.zw.platform.domain.vas.loadmgt.LoadVehicleSettingInfo;
import com.zw.platform.domain.vas.loadmgt.PersonLoadParam;
import com.zw.platform.domain.vas.loadmgt.ZwMCalibration;
import com.zw.platform.domain.vas.loadmgt.ZwMSensorInfo;
import com.zw.platform.domain.vas.loadmgt.form.AdValueForm;
import com.zw.platform.domain.vas.loadmgt.form.LoadVehicleSettingSensorForm;
import com.zw.platform.domain.vas.loadmgt.query.LoadVehicleSettingQuery;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.LoadAdDao;
import com.zw.platform.repository.vas.LoadVehicleSettingDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.loadmgt.LoadVehicleSettingService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisSensorQuery;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportOilBoxExcel;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.common.PublicVariable;
import com.zw.ws.entity.t808.oil.PeripheralMessageItem;
import com.zw.ws.entity.t808.oil.SensorParam;
import com.zw.ws.entity.t808.oil.T808_0x8900;
import com.zw.ws.impl.SensorService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zw.platform.push.cache.SendModule.LOAD;

/***
 @Author gfw
 @Date 2018/9/10 9:20
 @Description ?????????????????? ??????
 @version 1.0
 **/
@Service
public class LoadVehicleSettingServiceImpl implements LoadVehicleSettingService {
    /**
     * ???
     */
    private static final Integer Monitor_Type_One = 1;
    /**
     * ???
     */
    private static final Integer Monitor_Type_Two = 2;
    /**
     * ???
     */
    private static final Integer Monitor_Type_Three = 3;
    /**
     * ??????????????? 1
     */
    public static final int LOAD_SENSOR_SEQUENCE_ID = 0xf370;
    /**
     * ??????????????? 2
     */
    public static final int LOAD_SENSOR_SEQUENCE_TWO_ID = 0xf371;

    private static final String import_ERROR_MESSAGE = "?????????????????????????????????????????????????????????";

    private static final String import_ERROR_INFO = "?????????????????????????????????????????????????????????";

    /**
     * ???????????????????????????
     */
    private static final Integer SENSOR_NUMBER_MAX_LENGTH = 10;
    private static final String import_ERROR_MSG = "????????????0?????????!";
    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    ParamSendingCache paramSendingCache;

    private static Logger log = LogManager.getLogger(LoadVehicleSettingServiceImpl.class);

    @Autowired
    UserService userService;

    @Autowired
    private RedisVehicleService redisVehicleService;

    @Autowired
    LoadVehicleSettingDao loadVehicleSettingDao;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private SensorService sensorService;

    @Autowired
    private NewVehicleDao vehicleDao;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private LoadAdDao loadAdDao;
    private static final String TEMPLATE_COMMENT = "???????????????????????????????????????'0'";

    @Override
    public Page<LoadVehicleSettingInfo> findLoadVehicleList(LoadVehicleSettingQuery query) {
        Page<LoadVehicleSettingInfo> page = new Page<>();
        try {
            /**
             * ????????????????????????
             */
            page = getLoadVehicleFromCache(query);
        } catch (Exception e) {
            if (e instanceof RedisException) {
                String userId = userService.getCurrentUserUuid();
                // ?????????????????????????????????????????????
                List<String> userOrgListId = userService.getCurrentUserOrgIds();
                if (StringUtils.isNotBlank(userId) && CollectionUtils.isNotEmpty(userOrgListId)) {
                    page = PageHelperUtil
                        .doSelect(query, () -> loadVehicleSettingDao.findLoadVehicleList(query, userId, userOrgListId));
                }
            }
            log.error("????????????--->????????????????????????????????????", e);
        }

        // ??????result??????groupId?????????groupName???result??????????????????
        // ?????????????????????????????????
        setGroupNameByGroupId(page);
        return page;
    }

    /**
     * ??????????????????????????????????????? ??????
     * @param vehicleId
     * @return
     * @throws Exception
     */
    @Override
    public LoadVehicleSettingInfo findLoadVehicleByVid(String vehicleId) throws Exception {
        if (StringUtils.isEmpty(vehicleId)) {
            return null;
        }

        List<LoadVehicleSettingInfo> loadVehicleSettingInfos = new ArrayList<>();
        LoadVehicleSettingQuery query = new LoadVehicleSettingQuery();
        query.setVehicleId(vehicleId);

        // ??????????????????????????????????????????
        String userUuid = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> userOrgListId = userService.getCurrentUserOrgIds();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(userUuid) && CollectionUtils.isNotEmpty(userOrgListId)) {
            loadVehicleSettingInfos = loadVehicleSettingDao.findLoadVehicleList(query, userUuid, userOrgListId);
        }

        if (loadVehicleSettingInfos.size() == 0) {
            return null;
        }

        LoadVehicleSettingInfo loadVehicleSettingInfo = loadVehicleSettingInfos.get(0);
        if (StringUtils.isNotBlank(loadVehicleSettingInfo.getVehicleId()) && StringUtils
            .isNotBlank(loadVehicleSettingInfo.getId())) {
            // ????????????
            String paramType = "F3-8103-loadSensor" + loadVehicleSettingInfo.getSensorSequence();
            List<Directive> paramlist1 = parameterDao
                .findParameterByType(loadVehicleSettingInfo.getVehicleId(), loadVehicleSettingInfo.getId(), paramType);
            // ????????????
            String paramType1 = "F3-8103-loadCalibration" + loadVehicleSettingInfo.getSensorSequence();
            List<Directive> paramlist2 = parameterDao
                .findParameterByType(loadVehicleSettingInfo.getVehicleId(), loadVehicleSettingInfo.getId(), paramType1);
            Directive param1 = null;
            Directive param2 = null;
            if (paramlist1 != null && paramlist1.size() > 0) {
                param1 = paramlist1.get(0);
            }
            if (paramlist2 != null && paramlist2.size() > 0) {
                param2 = paramlist2.get(0);
            }
            if (param1 != null && param2 != null) {
                loadVehicleSettingInfo.setParamId(param1.getId());
                loadVehicleSettingInfo.setCalibrationParamId(param2.getId());
                if (param1.getStatus().equals(param2.getStatus())) {
                    loadVehicleSettingInfo.setStatus(param1.getStatus());
                } else if (param1.getStatus() == 4 || param2.getStatus() == 4) { // ???????????????????????????????????????????????????
                    loadVehicleSettingInfo.setStatus(4);
                } else if (param1.getStatus() == 7 || param2.getStatus() == 7) { // ???????????????????????????????????????????????????
                    loadVehicleSettingInfo.setStatus(7);
                } else {
                    loadVehicleSettingInfo.setStatus(1);
                }
            } else if (param1 != null) {
                loadVehicleSettingInfo.setParamId(param1.getId());
                loadVehicleSettingInfo.setStatus(param1.getStatus());
            }
        }
        return loadVehicleSettingInfo;
    }

    /**
     * ????????????????????????????????????
     * @param sensorType
     * @return
     */
    @Override
    public List<ZwMSensorInfo> findSensorInfo(String sensorType) {
        return loadVehicleSettingDao.findSensor(sensorType);
    }

    /**
     * ?????????????????????id??????????????????
     * @param vehicleId
     * @return
     */
    @Override
    public LoadVehicleSettingInfo findLoadBingInfo(String vehicleId) {
        LoadVehicleSettingInfo loadVehicleSettingInfo = null;
        if (StringUtils.isNotBlank(vehicleId)) {

            List<LoadVehicleSettingInfo> list = getLoadSettingInfos(vehicleId);

            if (CollectionUtils.isNotEmpty(list)) {
                if (list.size() == 1) {
                    // ???????????????
                    loadVehicleSettingInfo = list.get(0);
                    dealLoadVehicle(loadVehicleSettingInfo);
                    //??????????????????
                    ZwMCalibration calibration = loadAdDao.findBySensorVehicleId(loadVehicleSettingInfo.getId());
                    if (calibration != null) {
                        loadVehicleSettingInfo.setAdParamJson(calibration.getCalibration());
                    }
                } else if (list.size() == 2) {
                    //???????????????
                    loadVehicleSettingInfo = list.get(0);
                    dealLoadVehicle(loadVehicleSettingInfo);
                    //??????????????????
                    ZwMCalibration calibration = loadAdDao.findBySensorVehicleId(loadVehicleSettingInfo.getId());
                    if (calibration != null) {
                        loadVehicleSettingInfo.setAdParamJson(calibration.getCalibration());
                    }
                    LoadVehicleSettingInfo twoLoadVehicleSettingInfo = list.get(1);
                    dealLoadVehicle(twoLoadVehicleSettingInfo);
                    //??????????????????
                    ZwMCalibration twoCalibration = loadAdDao.findBySensorVehicleId(twoLoadVehicleSettingInfo.getId());
                    if (twoCalibration != null) {
                        twoLoadVehicleSettingInfo.setAdParamJson(twoCalibration.getCalibration());
                    }
                    buildDoubleLoadSetting(loadVehicleSettingInfo, twoLoadVehicleSettingInfo);
                }
                return loadVehicleSettingInfo;
            }
        }
        return null;
    }

    private void buildDoubleLoadSetting(LoadVehicleSettingInfo loadVehicleSettingInfo,
        LoadVehicleSettingInfo twoLoadVehicleSettingInfo) {
        loadVehicleSettingInfo.setTwoId(twoLoadVehicleSettingInfo.getId());
        loadVehicleSettingInfo.setTwoBaudRate(twoLoadVehicleSettingInfo.getBaudRate());
        loadVehicleSettingInfo.setTwoBaudRateStr(twoLoadVehicleSettingInfo.getBaudRateStr());
        loadVehicleSettingInfo.setTwoCompensate(twoLoadVehicleSettingInfo.getCompensate());
        loadVehicleSettingInfo.setTwoCompensateStr(twoLoadVehicleSettingInfo.getCompensateStr());
        loadVehicleSettingInfo.setTwoFilterFactor(twoLoadVehicleSettingInfo.getFilterFactor());
        loadVehicleSettingInfo.setTwoFilterFactorStr(twoLoadVehicleSettingInfo.getFilterFactorStr());
        loadVehicleSettingInfo.setTwoOddEvenCheck(twoLoadVehicleSettingInfo.getOddEvenCheck());
        loadVehicleSettingInfo.setTwoOddEvenCheckStr(twoLoadVehicleSettingInfo.getOddEvenCheckStr());
        loadVehicleSettingInfo.setTwoSensorId(twoLoadVehicleSettingInfo.getSensorId());
        loadVehicleSettingInfo.setTwoSensorSequence(twoLoadVehicleSettingInfo.getSensorSequence());
        loadVehicleSettingInfo.setTwoSensorNumber(twoLoadVehicleSettingInfo.getSensorNumber());
        loadVehicleSettingInfo.setTwoSensorType(twoLoadVehicleSettingInfo.getSensorType());
        loadVehicleSettingInfo.setTwoPersonLoadParam(twoLoadVehicleSettingInfo.getPersonLoadParam());
        loadVehicleSettingInfo.setTwoAdParamJson(twoLoadVehicleSettingInfo.getTwoAdParamJson());
    }

    /**
     * ????????????id?????????????????????????????????
     * @param vehicleId
     * @return
     */
    private List<LoadVehicleSettingInfo> getLoadSettingInfos(String vehicleId) {
        List<LoadVehicleSettingInfo> list;
        final String monitorType =
            RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "monitorType").get("monitorType");
        if ("0".equals(monitorType)) {
            list = loadVehicleSettingDao.findVehicleLoadSettingByVid(vehicleId);
        } else if ("1".equals(monitorType)) {
            list = loadVehicleSettingDao.findPeopleLoadSettingByVid(vehicleId);
        } else {
            list = loadVehicleSettingDao.findThingLoadSettingByVid(vehicleId);
        }
        return list;
    }

    /**
     * ??????????????????
     * @return
     */
    @Override
    public List<LoadVehicleSettingInfo> findReferenceVehicle() {
        List<LoadVehicleSettingInfo> list = new ArrayList<>();
        String userId = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (userId != null && !"".equals(userId) && orgList != null && orgList.size() > 0) {
            
            list = loadVehicleSettingDao.findLoadVehicle(userId, orgList, "1");
            for (LoadVehicleSettingInfo loadVehicleSettingInfo : list) {
                dealLoadVehicle(loadVehicleSettingInfo);
            }
            for (LoadVehicleSettingInfo loadVehicleSettingInfo : list) {
                dealLoadVehicle(loadVehicleSettingInfo);
            }
        }
        return list;
    }

    /**
     * ????????????????????????
     * @param form
     * @param ipAddress
     * @return
     */
    @Override
    public JsonResultBean addLoadVehicleSetting(LoadVehicleSettingSensorForm form, String ipAddress) throws Exception {
        String message = "";
        // ????????????????????????????????? 1??? 2??? 3???
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        Objects.requireNonNull(bindInfo);
        String plateNumber = bindInfo.getName();
        String orgId = bindInfo.getOrgId();
        // ?????????????????????
        // ????????????????????????????????????
        LoadVehicleSettingSensorForm twoForm = null;
        // ????????????????????????????????????
        if (StringUtils.isNotBlank(form.getTwoSensorId())
            && form.getTwoSensorSequence() == LoadVehicleSettingInfo.SENSOR_LOAD_TWO) {
            twoForm = buildLodSettingTwo(form);
            twoForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        }
        form.setSensorOutId("70");
        form.setPersonLoadParamJSON(JSONObject.toJSONString(form.getPersonLoadParam()));
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());

        boolean flag = loadVehicleSettingDao.addLoadSetting(form);
        loadAdDao.addByBatch(UUID.randomUUID().toString(), "1", form.getId(), form.getSensorId(), form.getVehicleId(),
            SystemHelper.getCurrentUsername(), form.getAdParamJson());
        boolean flag1 = true;
        if (Objects.nonNull(twoForm)) {
            flag1 = loadVehicleSettingDao.addLoadSetting(twoForm);
            loadAdDao.addByBatch(UUID.randomUUID().toString(), "1", twoForm.getId(), twoForm.getSensorId(),
                twoForm.getVehicleId(), SystemHelper.getCurrentUsername(), twoForm.getAdParamJson());
        }
        if (flag && flag1) {
            OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(orgId);
            message = "????????????: " + plateNumber + "( @" + organizationLdap.getName() + ") ??????????????????";
            logSearchService.addLog(ipAddress, message, "3", "", "", "");
        }
        String value = getAddLoadRedis(form.getVehicleId());

        // ???redis
        if (StringUtils.isNotBlank(value)) {
            RedisHelper.addToHash(RedisKeyEnum.LOAD_SETTING_MONITORY_LIST.of(), form.getVehicleId(), value);
        }
        if (StringUtils.isNotBlank(message)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return null;
    }

    /**
     * @param form
     * @param ipAddress
     * @return
     */
    @Override
    public JsonResultBean updateLoadSetting(LoadVehicleSettingSensorForm form, String ipAddress) throws Exception {
        // ?????????????????????????????????
        String vehicleId = form.getVehicleId();
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        Objects.requireNonNull(bindInfo);
        String plateNumber = bindInfo.getName();
        String orgId = bindInfo.getOrgId();
        LoadVehicleSettingInfo loadVehicleSettingInfo = findLoadBingInfo(vehicleId);
        // ????????????????????????????????????????????????????????????id????????????????????????;  1?????????????????????????????????????????????;
        if (Objects.isNull(loadVehicleSettingInfo) || StringUtils.isBlank(loadVehicleSettingInfo.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // ?????????2
        LoadVehicleSettingSensorForm twoForm = buildLodSettingTwo(form);
        if (StringUtils.isNotBlank(form.getTwoId())) {
            twoForm.setSensorOutId("71");
            twoForm.setId(form.getTwoId());
            twoForm.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        }
        form.setSensorOutId("70");
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        form.setPersonLoadParamJSON(JSONObject.toJSONString(form.getPersonLoadParam()));
        String groupName = organizationService.getOrgNameByUuid(orgId);
        // ??????redis??????
        String redisValue = "";
        // ????????????
        String message = "";
        if (StringUtils.isBlank(loadVehicleSettingInfo.getTwoId()) && StringUtils.isBlank(twoForm.getSensorId())) {
            // ????????????????????????????????????,????????????????????????????????????????????????
            boolean flag = loadVehicleSettingDao.updateLoadSetting(form);
            if (!org.springframework.util.StringUtils.isEmpty(loadAdDao.findBySensorVehicleId(form.getId()))) {
                loadAdDao.updateCalibration(form.getId(), form.getSensorId(), form.getAdParamJson());
            } else {
                loadAdDao.addByBatch(UUID.randomUUID().toString(), "1", form.getId(), form.getSensorId(),
                    form.getVehicleId(), SystemHelper.getCurrentUsername(), form.getAdParamJson());
            }
            if (flag) {
                message = "???????????? ???" + plateNumber + "( @" + groupName + ") ????????????????????????";
            }
            /**
             * ?????????????????????????????????
             */
            clearSendStatus(vehicleId, form.getId(), null);

            redisValue = getLoadBindForm(form);
        } else if (StringUtils.isBlank(loadVehicleSettingInfo.getTwoId()) && StringUtils
            .isNotBlank(twoForm.getSensorId())) {
            // ????????????????????????????????????????????????????????????????????????????????????1?????????2??????
            boolean flag = loadVehicleSettingDao.updateLoadSetting(form);
            if (!org.springframework.util.StringUtils.isEmpty(loadAdDao.findBySensorVehicleId(form.getId()))) {
                loadAdDao.updateCalibration(form.getId(), form.getSensorId(), form.getAdParamJson());
            } else {
                loadAdDao.addByBatch(UUID.randomUUID().toString(), "1", form.getId(), form.getSensorId(),
                    form.getVehicleId(), SystemHelper.getCurrentUsername(), form.getAdParamJson());
            }
            twoForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
            boolean flag1 = loadVehicleSettingDao.addLoadSetting(twoForm);
            loadAdDao.addByBatch(UUID.randomUUID().toString(), "1", form.getVehicleId(), form.getSensorId(),
                form.getVehicleId(), SystemHelper.getCurrentUsername(), form.getTwoAdParamJson());
            if (flag && flag1) {
                message = "???????????? ???" + plateNumber + "( @" + groupName + ") ????????????????????????1?????????,??????2?????????";
            }
            clearSendStatus(vehicleId, form.getId(), null);
            redisValue = getAddLoadRedis(vehicleId);
        } else if (StringUtils.isNotBlank(loadVehicleSettingInfo.getTwoId()) && StringUtils
            .isNotBlank(twoForm.getSensorId())) {
            // ???????????????????????????????????????????????????????????????????????????????????????
            boolean flag = loadVehicleSettingDao.updateLoadSetting(form);
            if (!org.springframework.util.StringUtils.isEmpty(loadAdDao.findBySensorVehicleId(form.getId()))) {
                loadAdDao.updateCalibration(form.getId(), form.getSensorId(), form.getAdParamJson());
            } else {
                loadAdDao.addByBatch(UUID.randomUUID().toString(), "1", form.getId(), form.getSensorId(),
                    form.getVehicleId(), SystemHelper.getCurrentUsername(), form.getAdParamJson());
            }
            boolean flag1 = loadVehicleSettingDao.updateLoadSetting(twoForm);
            if (!org.springframework.util.StringUtils.isEmpty(loadAdDao.findBySensorVehicleId(twoForm.getId()))) {
                loadAdDao.updateCalibration(twoForm.getId(), twoForm.getSensorId(), twoForm.getAdParamJson());
            } else {
                loadAdDao.addByBatch(UUID.randomUUID().toString(), "1", twoForm.getId(), twoForm.getSensorId(),
                    twoForm.getVehicleId(), SystemHelper.getCurrentUsername(), twoForm.getAdParamJson());
            }
            if (flag && flag1) {
                message = "???????????? ???" + plateNumber + "( @" + groupName + ") ???????????????????????????????????????";
            }
            clearSendStatus(vehicleId, form.getId(), twoForm.getId());
            redisValue = getDoubleLoadForm(form, twoForm);
        } else if (StringUtils.isNotBlank(loadVehicleSettingInfo.getTwoId()) && StringUtils
            .isBlank(twoForm.getSensorId())) {
            // ????????????????????????????????? ????????????????????????????????????????????????2??????????????????1
            boolean flag = loadVehicleSettingDao.updateLoadSetting(form);
            boolean flag1 = loadVehicleSettingDao.deleteLoadSetting(twoForm.getId());
            if (flag && flag1) {
                message = "???????????? ???" + plateNumber + "( @" + groupName + ") ????????????????????????1??????????????????2?????????";
            }
            clearSendStatus(vehicleId, form.getId(), twoForm.getId());
            redisValue = getLoadBindForm(form);
        }

        //??????redis??????
        if (StringUtils.isNotBlank(redisValue)) {
            RedisHelper.addToHash(RedisKeyEnum.LOAD_SETTING_MONITORY_LIST.of(), form.getVehicleId(), redisValue);
        }
        if (StringUtils.isNotBlank(message)) {
            logSearchService.addLog(ipAddress, message, "3", "", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ??????id????????????
     * @param id
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean deleteLoadSettingBind(String id, String ipAddress) throws Exception {
        boolean flag = deleteLoadIsFlag(false, id, 0, ipAddress);

        if (flag) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ??????????????????????????????
     * @param ids
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean deleteMoreLoadSettingBind(String ids, String ipAddress) throws Exception {
        String[] idArray = ids.split(",");
        boolean flag = false;
        for (String id : idArray) {
            flag = deleteLoadIsFlag(flag, id, 1, ipAddress);
        }

        if (flag) {
            logSearchService.addLog(ipAddress, "??????????????????????????????", "3", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ???????????????id?????????????????????
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public LoadVehicleSettingInfo getSensorVehicleByBindId(String id) throws Exception {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        LoadVehicleSettingInfo sensorVehicle = loadVehicleSettingDao.findSensorVehicleByBindId(id);

        if (Objects.isNull(sensorVehicle)) {
            return null;
        }
        sensorVehicle.setBaudRateStr(BaudRateUtil.getBaudRateVal(Integer.valueOf(sensorVehicle.getBaudRate())));
        sensorVehicle.setCompensateStr(CompEnUtil.getCompEnVal(sensorVehicle.getCompensate()));
        sensorVehicle.setFilterFactorStr(FilterFactorUtil.getFilterFactorVal(sensorVehicle.getFilterFactor()));
        sensorVehicle
            .setOddEvenCheckStr(ParityCheckUtil.getParityCheckVal(Integer.valueOf(sensorVehicle.getOddEvenCheck())));
        sensorVehicle.setSensorPeripheralID("7" + sensorVehicle.getSensorSequence());
        return sensorVehicle;
    }

    /**
     * ??????????????????
     * @param paramList
     * @param ipAddress
     */
    @Override
    public void sendLoadSetting(List<JSONObject> paramList, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        String vehicleId = "";
        for (JSONObject obj : paramList) {
            // ??????
            String sensorVehicleId = "";
            vehicleId = "";
            String paramId = "";
            String calibrationParamId = "";
            if (obj.get("sensorVehicleId") != null) {
                sensorVehicleId = obj.get("sensorVehicleId").toString();
            }
            if (obj.get("vehicleId") != null) {
                vehicleId = obj.get("vehicleId").toString();
            }
            if (obj.get("paramId") != null && !"".equals(obj.get("paramId"))) {
                paramId = obj.get("paramId").toString();
            }
            if (obj.get("calibrationParamId") != null) {
                calibrationParamId = obj.get("calibrationParamId").toString(); // ????????????
            }
            if (StringUtils.isNotBlank(sensorVehicleId) && StringUtils.isNotBlank(vehicleId)) {
                LoadVehicleSettingInfo sensorVehicle = loadVehicleSettingDao.findSensorVehicleByBindId(sensorVehicleId);

                if (sensorVehicle != null) {
                    dealLoadVehicle(sensorVehicle);
                    // ???????????? F3-8103-loadSensor0: ??????; F3-8103-loadSensor1: ??????2
                    String paramType = "F3-8103-loadSensor" + sensorVehicle.getSensorSequence();
                    String calibrationParamType = "F3-8103-loadCalibration" + sensorVehicle.getSensorSequence();
                    int mark = 0;
                    int markv = 0;
                    if (sensorVehicle.getSensorSequence() == 0) {
                        mark = 0xf370;
                        markv = 0x70;
                    }
                    if (sensorVehicle.getSensorSequence() == 1) {
                        mark = 0xf371;
                        markv = 0x71;
                    }
                    sendLoadSensor(paramId, vehicleId, paramType, sensorVehicleId, sensorVehicle, mark);
                    // ??????????????????
                    ZwMCalibration bySensorVehicleId = loadAdDao.findBySensorVehicleId(sensorVehicle.getId());
                    if (!org.springframework.util.StringUtils.isEmpty(bySensorVehicleId)
                        && !org.springframework.util.StringUtils.isEmpty(bySensorVehicleId.getCalibration()) && !"[]"
                        .equals(bySensorVehicleId.getCalibration())) {
                        sendLoadCalibration(calibrationParamId, vehicleId, calibrationParamType, sensorVehicleId,
                            sensorVehicle, markv);
                    }
                    String plateNumber = sensorVehicle.getPlateNumber();
                    message.append("???????????? : ").append(plateNumber).append(" ??????????????????????????????").append(" <br/>");
                }

            }
        }
        if (StringUtils.isNotBlank(message.toString())) {
            if (paramList.size() == 1) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "????????????????????????????????????");
            }
        }
    }

    /**
     * ??????????????????
     * @param response
     */
    @Override
    public void generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // ??????
        headList.add("??????");
        headList.add("AD???*");
        headList.add("???????????????Kg???*");
        // ????????????
        requiredList.add("???????????????");
        requiredList.add("???????????????");
        // ???????????????????????????
        exportList.add("1");
        exportList.add("10");
        exportList.add("0");
        ExportOilBoxExcel export = new ExportOilBoxExcel(TEMPLATE_COMMENT, headList, requiredList, null);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        // ????????????????????????????????????
        export.write(out);
        out.close();
    }

    /**
     * ??????????????????AD??????
     * @param multipartFile
     * @param request
     * @param ipAddress
     * @return
     */
    @Override
    public Map importBatch(MultipartFile multipartFile, HttpServletRequest request, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        Integer limitNum = 3;
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        StringBuilder message = new StringBuilder();
        int failNum = 0;
        int totalNum = 0;
        // ???????????????
        ImportExcel importExcel = new ImportExcel(multipartFile, 2, 0);
        int cellNum = importExcel.getLastCellNum();
        // ??????????????????
        /**
         * ????????????
         */
        if (cellNum != limitNum) {
            resultMapInfo(0, resultMap, import_ERROR_MESSAGE, import_ERROR_INFO);
            return resultMap;
        }
        // excel ????????? list
        List<AdValueForm> list = importExcel.getDataList(AdValueForm.class, null);
        if (null != list && list.size() > 0) {
            totalNum = list.size();
        }
        // ??????????????????
        if (totalNum > 50 || totalNum < 2) {
            resultMapInfo(0, resultMap, "??????????????????2-50??????", "??????????????????2-50??????");
            return resultMap;
        }
        List<AdValueForm> importList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            AdValueForm adValueForm = list.get(i);
            // ??????????????????
            if (Converter.toBlank(adValueForm.getAdNumber()).equals("") || Converter.toBlank(adValueForm.getAdValue())
                .equals("") || Converter.toBlank(adValueForm.getAdActualValue()).equals("")) {
                resultMap.put("flag", 1);
                errorMsg.append("???").append(i + 1).append("???????????????????????????<br/>");
                failNum++;
                break;
            } else if (!adValueForm.getAdNumber().matches("^[0-9]+$")) {
                resultMap.put("flag", 1);
                errorMsg.append("???").append(i + 1).append("????????????").append(adValueForm.getAdNumber())
                    .append("???:AD???????????????<br/>");
                failNum++;
                break;
            } else if (!adValueForm.getAdValue().matches("^[0-9]+$")) {
                resultMap.put("flag", 1);
                errorMsg.append("???").append(i + 1).append("????????????").append(adValueForm.getAdValue())
                    .append("???:AD???????????????<br/>");
                failNum++;
                break;
            } else if (!adValueForm.getAdActualValue().matches("^[0-9]+$")) {
                resultMap.put("flag", 1);
                errorMsg.append("???").append(i + 1).append("????????????").append(adValueForm.getAdActualValue())
                    .append("???:AD???????????????<br/>");
                failNum++;
                break;
            }
            if (i == 0 && !(adValueForm.getAdActualValue().equals("0"))) {
                errorMsg.append("???").append(i + 1).append("????????????").append(adValueForm.getAdActualValue())
                    .append("???:AD?????????0<br/>");
                failNum++;
                break;
            }
            if (!RegexUtils.checkRightfulString1(adValueForm.getAdNumber())) {
                resultMap.put("flag", 1);
                errorMsg.append("???").append(i + 1).append("????????????????????????").append(adValueForm.getAdNumber())
                    .append("???:AD?????????????????????<br/>");
                failNum++;
                break;
            } else if (Converter.toBlank(adValueForm.getAdNumber()).length() > SENSOR_NUMBER_MAX_LENGTH) {
                resultMap.put("flag", 1);
                errorMsg.append("???").append(i + 1).append("????????????").append(adValueForm.getAdNumber()).append("???:AD???????????????")
                    .append(SENSOR_NUMBER_MAX_LENGTH).append("<br/>");
                failNum++;
                break;
            }
            importList.add(adValueForm);
            message.append("AD???????????? : ").append(adValueForm.getAdNumber()).append(" <br/>");
        }
        boolean flag = false;
        // ??????????????????
        if (importList.size() == list.size()) {
            // ???????????? ?????????AD?????????????????? ?????????ADact???????????????
            flag = getSortImport(list, resultMap, errorMsg);
            String listValue = JSONObject.toJSONString(importList);
            if (!org.springframework.util.StringUtils.isEmpty(listValue) && flag) {
                String resultInfo = "";
                resultMap.put("listValue", listValue);
                resultInfo += "??????" + (totalNum - failNum) + "???,??????" + failNum + "??????";
                resultMapInfo(1, resultMap, errorMsg.toString(), resultInfo);
            } else {
                resultMapInfo(0, resultMap, errorMsg.toString(), import_ERROR_MSG);
                return resultMap;
            }

        } else {
            resultMapInfo(0, resultMap, errorMsg.toString(), import_ERROR_MSG);
            return resultMap;
        }
        return resultMap;
    }

    private boolean getSortImport(List<AdValueForm> list, Map<String, Object> resultMap, StringBuilder errorMsg) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (new BigDecimal(list.get(i).getAdValue()).compareTo(new BigDecimal(list.get(i + 1).getAdValue())) > 0) {
                resultMap.put("flag", 1);
                errorMsg.append("AD????????????????????????");
                return false;
            }
            if (new BigDecimal(list.get(i).getAdActualValue())
                .compareTo(new BigDecimal(list.get(i + 1).getAdActualValue())) > 0) {
                resultMap.put("flag", 1);
                errorMsg.append("????????????????????????????????????");
                return false;
            }
        }
        return true;
    }

    /**
     * ??????id????????????
     * @param id
     * @return
     */
    @Override
    public List<AdValueForm> findAdList(String id, String sensorVehicleId) {
        List<AdValueForm> list = new ArrayList<>();
        ZwMCalibration byId;
        if (id == null) {
            byId = loadAdDao.findBySensorVehicleId(sensorVehicleId);
        } else {
            byId = loadAdDao.findByIdAndSensorId(id, sensorVehicleId);
        }
        if (!org.springframework.util.StringUtils.isEmpty(byId) && !org.springframework.util.StringUtils
            .isEmpty(byId.getCalibration())) {
            list = JSONObject.parseArray(byId.getCalibration(), AdValueForm.class);
        }
        return list;
    }

    /**
     * ???????????????
     * @param id
     * @param sensorVehicleId
     * @param calibrationValue
     * @return
     */
    @Override
    public String updateCalibration(String id, String sensorVehicleId, String calibrationValue) throws Exception {
        // ????????????
        String valueId = "";
        //        if(org.springframework.util.StringUtils.isEmpty(id)){
        //            // ???????????? ????????????
        //            String str = UUID.randomUUID().toString();
        //            loadAdDao.addByBatch(str,"1",SystemHelper.getCurrentUser().getUsername(),calibrationValue);
        //            valueId =str;
        //        }else{
        //            // ????????????
        //            int i = loadAdDao.updateCalibration(id, calibrationValue);
        //            valueId =""+i;
        //        }
        return valueId;
    }

    @Override
    public JsonResultBean updateWorkSettingBind(LoadVehicleSettingSensorForm form, String ipAddress, String paramType) {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        sendHelper.deleteByVehicleIdParameterName(form.getVehicleId(), form.getId(), paramType);
        boolean result = loadVehicleSettingDao.updateLoadSetting(form);
        if (result) {
            // ??????????????????????????????
            final Map<String, String> vehicleInfo = RedisHelper.getHashMap(
                    RedisKeyEnum.MONITOR_INFO.of(form.getVehicleId(), "name", "plateColor", "orgName"));
            if (vehicleInfo != null) {
                String brand = vehicleInfo.get("name");
                String plateColor = vehicleInfo.get("plateColor");
                String orgName = vehicleInfo.get("orgName");
                String msg = "???????????????" + brand + "( @" + orgName + ") ????????????????????????";
                logSearchService.addLog(ipAddress, msg, "3", "", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ????????????????????????
     * @param vehicleId
     * @return
     */
    @Override
    public String getLatestPositional(String vehicleId) throws Exception {
        // ???????????????????????????
        BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicleInfo.getDeviceNumber();
        // ?????????
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // ??????????????????
        if (msgSN != null) {
            // ????????????
            sendvehicleLocationQuery(msgSN, vehicleInfo);
        }
        return String.valueOf(msgSN);
    }

    private void sendvehicleLocationQuery(Integer transNo, BindDTO vehicleInfo) {
        String deviceId = vehicleInfo.getDeviceId();
        //??????????????????
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_GPS_INFO_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimCardNumber(), ConstantUtil.T808_QUERY_LOCATION_COMMAND, transNo, null,
                vehicleInfo);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_QUERY_LOCATION_COMMAND, deviceId);
    }

    /**
     * ????????????
     * @param paramId
     * @param vehicleId
     * @param paramType
     * @param sensorVehicleId
     * @param sensorVehicle
     * @param i
     * @return
     * @throws Exception
     */
    public String sendLoadCalibration(String paramId, String vehicleId, String paramType, String sensorVehicleId,
        LoadVehicleSettingInfo sensorVehicle, int i) throws Exception {
        // ???????????????????????????
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // ??????????????????s
        if (msgSN != null) {
            // ?????????
            int status = 4;
            // ????????????
            // ??????????????????????????????: ????????????????????????????????????paramId?????????
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // ????????????
            ZwMCalibration byId = loadAdDao.findBySensorVehicleId(sensorVehicle.getId());
            List<AdValueForm> adValueForms = JSONObject.parseArray(byId.getCalibration(), AdValueForm.class);
            List<SensorParam> result = new ArrayList<>();
            for (AdValueForm adValueForm : adValueForms) {
                SensorParam sensorParam = new SensorParam();
                sensorParam.setHeight(new Double(adValueForm.getAdValue()));
                sensorParam.setSurplus(new Double(adValueForm.getAdActualValue()));
                result.add(sensorParam);
            }
            loadSettingParamV2(vehicle, result, msgSN, i);
        } else {
            /**
             * ???????????????
             */
            int status = 5;
            msgSN = 0;
            // ??????????????????
            sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
        }
        return String.valueOf(msgSN);
    }

    private void loadSettingParamV2(BindDTO vehicle, List<SensorParam> list, Integer transNo, int mark) {
        T808_0x8900 parameter = new T808_0x8900();
        parameter.setType(PublicVariable.CALIBRATION_DATA);
        parameter.setSum(1);
        List<PeripheralMessageItem> peripheralMessageItems = new ArrayList<>();
        PeripheralMessageItem per = new PeripheralMessageItem();
        per.setSensorID(mark);
        if (list != null && list.size() > 0) {
            if (list.size() < 50) {
                SensorParam sensorParam = new SensorParam();
                sensorParam.setHeight(0xFFFFFFFF);
                sensorParam.setSurplus(0xFFFFFFFF);
                list.add(sensorParam);
            }
            per.setSensorSum(list.size());
            per.setDemarcates(list);
        } else {
            per.setSensorSum(0);
        }
        peripheralMessageItems.add(per);
        parameter.setSensorDatas(peripheralMessageItems);

        if (vehicle != null) {
            String deviceId = vehicle.getDeviceId();
            String simcardNumber = vehicle.getSimCardNumber();
            //??????????????????????????????????????????,?????????????????????????????????,??????????????????????????????websocket??????
            paramSendingCache
                .put(SystemHelper.getCurrentUsername(), transNo, simcardNumber, SendTarget.getInstance(LOAD));
            //??????????????????
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK,
                    1);
            SubscibeInfoCache.getInstance().putTable(info);
            info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo,
                ConstantUtil.T808_DATA_PERMEANCE_REPORT);
            SubscibeInfoCache.getInstance().putTable(info);
            T808Message message =
                MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_PENETRATE_DOWN, transNo, parameter, vehicle);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, deviceId);
        }
    }

    /**
     * ?????????????????????
     * @param paramId                ??????id
     * @param vehicleId              ??????id
     * @param paramType              ????????????
     * @param sensorVehicleId        ?????????????????????id
     * @param loadVehicleSettingInfo ?????????????????????;
     * @param mark                   0:??????????????????/????????????;
     */
    public String sendLoadSensor(String paramId, String vehicleId, String paramType, String sensorVehicleId,
        LoadVehicleSettingInfo loadVehicleSettingInfo, int mark) throws Exception {
        // ???????????????????????????
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();
        // ?????????
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // ??????????????????
        if (msgSN != null) {
            // ?????????
            int status = 4;
            // ????????????
            // ??????????????????????????????: ????????????????????????????????????paramId?????????
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // ??????????????????
            // ????????????
            LoadSettingParam loadSettingParam = new LoadSettingParam();
            dealLoadVehilToLoadParam(loadVehicleSettingInfo, loadSettingParam);
            sensorService.sendLoadSensorParam(vehicle, loadSettingParam, msgSN, mark);

            // updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
        } else {
            /**we
             * ???????????????
             */
            int status = 5;
            msgSN = 0;
            // ??????????????????
            sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
        }
        return String.valueOf(msgSN);

    }

    private void dealLoadVehilToLoadParam(LoadVehicleSettingInfo loadVehicleSettingInfo,
        LoadSettingParam loadSettingParam) {
        PersonLoadParam personLoadParam = loadVehicleSettingInfo.getPersonLoadParam();
        loadSettingParam.setCompensate(loadVehicleSettingInfo.getCompensate());
        loadSettingParam.setUploadTime(1);
        // ????????? ???????????????  ?????????????????? 0:????????? 1:????????? 2:?????????
        // ????????? ???????????????  ?????????????????? 1:????????? 2:????????? 4:?????????
        int scheme = 1;
        int loadMeter = Integer.parseInt(personLoadParam.getLoadMeterWay());
        if (loadMeter == 1) {
            scheme = 2;
        } else if (loadMeter == 2) {
            scheme = 4;
        }
        loadSettingParam.setScheme(scheme);
        loadSettingParam.setSmoothing(loadVehicleSettingInfo.getFilterFactor());
        loadSettingParam.setOutputCorrectionK(100);
        loadSettingParam.setOutputCorrectionB(100);
        byte[] by = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        loadSettingParam.setReservedItem1(by);
        int unit = Integer.parseInt(personLoadParam.getLoadMeterUnit());
        loadSettingParam.setUnit(unit);
        loadSettingParam.setReservedItem2(0);
        loadSettingParam.setApprovedLoadWeight(0xFFFF);
        BigDecimal unit1 = new BigDecimal("0.1");
        int unit2 = new BigDecimal(Math.pow(10, unit) + "").intValue();
        BigDecimal multiply = unit1.multiply(new BigDecimal(unit2 + "")).setScale(1, BigDecimal.ROUND_DOWN);
        BigDecimal acUnit = BigDecimal.ONE.divide(multiply);
        loadSettingParam.setOverLoadThreshold(
            personLoadParam.getOverLoadValue().multiply(acUnit).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        loadSettingParam
            .setOverLoadThresholdOffset(Integer.parseInt(personLoadParam.getOverLoadThreshold().toString()));
        loadSettingParam.setFullLoadThreshold(
            personLoadParam.getFullLoadValue().multiply(acUnit).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        loadSettingParam
            .setFullLoadThresholdOffset(Integer.parseInt(personLoadParam.getFullLoadThreshold().toString()));
        loadSettingParam.setNullLoadThreshold(
            personLoadParam.getNoLoadValue().multiply(acUnit).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        loadSettingParam.setNullLoadThresholdOffset(Integer.parseInt(personLoadParam.getNoLoadThreshold().toString()));
        loadSettingParam.setLightLoadThreshold(
            personLoadParam.getLightLoadValue().multiply(acUnit).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        loadSettingParam
            .setLightLoadThresholdOffset(Integer.parseInt(personLoadParam.getLightLoadThreshold().toString()));
        byte[] b2 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        loadSettingParam.setReservedItem3(b2);
    }

    private boolean deleteLoadIsFlag(boolean flag, String id, int mark, String ipAddress) throws Exception {
        // ?????????sensor_vehicle id
        LoadVehicleSettingInfo loadBingInfo = loadVehicleSettingDao.findSensorVehicleByBindId(id);
        if (Objects.isNull(loadBingInfo)) {
            return flag;
        }

        String vehicleId = loadBingInfo.getVehicleId();
        LoadVehicleSettingInfo loadVehicleSetting = findLoadBingInfo(vehicleId);
        if (Objects.isNull(loadVehicleSetting)) {
            return flag;
        }
        // ???????????????1,????????????
        boolean flag1 =
            StringUtils.isNotBlank(loadVehicleSetting.getId()) && StringUtils.isBlank(loadVehicleSetting.getTwoId());
        // ?????????????????????2
        boolean flag2 = StringUtils.isNotBlank(loadVehicleSetting.getId())
            && loadBingInfo.getSensorSequence() == WorkHourSettingInfo.SENSOR_SEQUENCE_TWO;
        // ????????????2,?????????????????????1,???????????????1??????????????????2???sensor_sequence ???0
        boolean flag3 =
            StringUtils.isNotBlank(loadVehicleSetting.getId()) && StringUtils.isNotBlank(loadVehicleSetting.getTwoId())
                && loadBingInfo.getSensorSequence() == WorkHourSettingInfo.SENSOR_SEQUENCE_ONE;
        if (flag1 || flag2) {
            flag = loadVehicleSettingDao.deleteLoadSetting(id);
        } else if (flag3) {
            flag = loadVehicleSettingDao.deleteLoadSetting(id);
            flag = loadVehicleSettingDao.updateLoadSettingByID(loadVehicleSetting.getTwoId());
        }
        loadAdDao.deleteAdLoad(id);
        redisVehicleService
            .delLoadBind(loadBingInfo.getVehicleId(), loadBingInfo.getId(), loadBingInfo.getSensorNumber());
        if (mark == 0 && flag) {
            final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
            Objects.requireNonNull(bindInfo);
            String plateNumber = bindInfo.getName();
            String orgId = bindInfo.getOrgId();
            String orgName = organizationService.getOrgNameByUuid(orgId);
            String message = "???????????? ???" + plateNumber + "( @" + orgName + ") ????????????????????????";
            logSearchService.addLog(ipAddress, message, "3", "", "");
        }
        return flag;
    }

    private String getDoubleLoadForm(LoadVehicleSettingSensorForm form, LoadVehicleSettingSensorForm twoForm) {
        StringBuilder sb = new StringBuilder();
        return sb.append(form.getSensorNumber()).append(RedisKeys.SEPARATOR).append(form.getId()).append(",")
            .append(twoForm.getSensorNumber()).append(RedisKeys.SEPARATOR).append(twoForm.getId()).toString();

    }

    private String getLoadBindForm(LoadVehicleSettingSensorForm form) {
        StringBuilder sb = new StringBuilder();
        return sb.append(form.getSensorNumber()).append(RedisKeys.SEPARATOR).append(form.getId()).toString();
    }

    private void clearSendStatus(String vehicleId, String loadId, String twoLoadId) throws Exception {
        String paramType = "F3-8103-loadSensor0";
        String paramTypeV2 = "F3-8103-loadSensor1";
        if (StringUtils.isNotBlank(loadId)) {
            sendHelper.deleteByVehicleIdParameterName(vehicleId, loadId, paramType);
        }
        if (StringUtils.isNotBlank(twoLoadId)) {
            sendHelper.deleteByVehicleIdParameterName(vehicleId, twoLoadId, paramTypeV2);
        }
    }

    /**
     * ?????????????????????????????????
     * @param vehicleId
     * @return
     */
    private String getAddLoadRedis(String vehicleId) {
        // ??????redis??????
        List<LoadVehicleSettingInfo> works = loadVehicleSettingDao.findLoadSettingByMonitorVid(vehicleId);
        String value = "";
        if (works.size() == 1) {
            value = getLoadBindInfo(works.get(0));
        } else if (works.size() == 2) {
            value = getLoadBindInfo(works.get(0)) + "," + getLoadBindInfo(works.get(1));
        }
        return value;
    }

    private String getLoadBindInfo(LoadVehicleSettingInfo info) {
        return info.getSensorNumber() + RedisKeys.SEPARATOR + info.getId();
    }

    private LoadVehicleSettingSensorForm buildLodSettingTwo(LoadVehicleSettingSensorForm form) {
        LoadVehicleSettingSensorForm form1 = new LoadVehicleSettingSensorForm();
        form1.setSensorId(form.getTwoSensorId());
        form1.setSensorNumber(form.getTwoSensorNumber());
        form1.setSensorSequence(form.getTwoSensorSequence());
        form1.setVehicleId(form.getVehicleId());
        form1.setPlateNumber(form.getPlateNumber());
        form1.setParamId(form.getParamId());
        form1.setMonitorType(form.getMonitorType());
        form1.setPersonLoadParam(form.getTwoPersonLoadParam());
        form1.setPersonLoadParamJSON(JSONObject.toJSONString(form.getTwoPersonLoadParam()));
        form1.setAdParamJson(form.getTwoAdParamJson());
        form1.setSensorOutId("71");
        return form1;
    }

    /**
     * ??????????????? ??????groupName ?????????????????????param
     * @param page
     */
    private void setGroupNameByGroupId(Page<LoadVehicleSettingInfo> page) {
        if (null != page && page.size() > 0) {
            Set<String> vids = page.stream().map(LoadVehicleSettingInfo::getVehicleId).collect(Collectors.toSet());
            Map<String, BindDTO> bindInfos = VehicleUtil.batchGetBindInfosByRedis(vids);
            for (LoadVehicleSettingInfo parameter : page) {
                BindDTO bindDTO = bindInfos.get(parameter.getVehicleId());
                if (bindDTO != null) {
                    parameter.setGroupName(bindDTO.getOrgName());
                }
                // ??????????????????
                if (StringUtils.isNotBlank(parameter.getVehicleId()) && StringUtils.isNotBlank(parameter.getId())) {
                    // ????????????
                    String paramType = "F3-8103-loadSensor" + parameter.getSensorSequence();
                    List<Directive> paramlist1 =
                        parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), paramType);
                    // ????????????
                    String paramType1 = "F3-8103-loadCalibration" + parameter.getSensorSequence();
                    List<Directive> paramlist2 =
                        parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), paramType1);
                    Directive param1 = null;
                    Directive param2 = null;
                    if (paramlist1 != null && paramlist1.size() > 0) {
                        param1 = paramlist1.get(0);
                    }
                    if (paramlist2 != null && paramlist2.size() > 0) {
                        param2 = paramlist2.get(0);
                    }
                    if (param1 != null && param2 != null) {
                        parameter.setParamId(param1.getId());
                        parameter.setCalibrationParamId(param2.getId());
                        if (param1.getStatus().equals(param2.getStatus())) {
                            parameter.setStatus(param1.getStatus());
                        } else if (param1.getStatus() == 4 || param2.getStatus() == 4) { // ???????????????????????????????????????????????????
                            parameter.setStatus(4);
                        } else if (param1.getStatus() == 7 || param2.getStatus() == 7) { // ???????????????????????????????????????????????????
                            parameter.setStatus(7);
                        } else {
                            parameter.setStatus(1);
                        }
                    } else if (param1 != null) {
                        parameter.setParamId(param1.getId());
                        parameter.setStatus(param1.getStatus());
                    }
                }
            }
        }
    }

    private Page<LoadVehicleSettingInfo> getLoadVehicleFromCache(LoadVehicleSettingQuery query) throws Exception {
        /**
         * ?????????redis query
         */
        RedisSensorQuery redisQuery =
            new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                Integer.valueOf(query.getProtocol()));
        /**
         * ????????????????????????id??????????????????id
         * RedisKeys.SensorType.SENSOR_LOAD_MONITOR ??????KEYS???????????????????????????????????????????????????id
         */
        List<String> cacheIdList =
            redisVehicleService.getVehicleByType(redisQuery, RedisKeys.SensorType.SENSOR_LOAD_MONITOR);
        /**
         * ??????????????????????????????
         */
        if (cacheIdList == null) {
            throw new RedisException(">=======redis ???????????????===========<");
        }
        int total = cacheIdList.size();
        /**
         * ?????????
         */
        int curPage = query.getPage().intValue();
        /**
         * ????????????
         */
        int pageSize = query.getLimit().intValue();
        /**
         * ??????????????????
         */
        int start = (curPage - 1) * pageSize;
        /**
         * ??????????????????
         */
        int end = pageSize > (total - start) ? total : (pageSize * curPage);
        List<String> queryList = cacheIdList.subList(start, end);
        List<LoadVehicleSettingInfo> resultList = new LinkedList<>();
        /**
         * ?????????????????? ?????? ?????????
         */
        if (CollectionUtils.isNotEmpty(queryList)) {
            List<String> vehicleIds = new LinkedList<>();
            List<String> engineIds = new LinkedList<>();
            for (String item : queryList) {
                String[] items = item.split(RedisKeys.SEPARATOR);
                vehicleIds.add(items[0]);
                if (items.length == 2) {
                    engineIds.add(items[1]);
                }
            }
            /**
             * ????????????id????????????????????????????????????
             */
            resultList = loadVehicleSettingDao.findVehicleByIds(vehicleIds, engineIds);
            if (CollectionUtils.isNotEmpty(resultList)) {
                Map m;
                for (LoadVehicleSettingInfo loadVehicleSettingInfo : resultList) {
                    if (!org.springframework.util.StringUtils.isEmpty(loadVehicleSettingInfo.getSensorId())) {
                        dealLoadVehicle(loadVehicleSettingInfo);
                    }
                }
            }
            //??????
            VehicleUtil.sort(resultList, vehicleIds);
        }
        return RedisQueryUtil.getListToPage(resultList, query, total);
    }

    /**
     * ??????????????????????????? str?????? JSON????????????
     * @param loadVehicleSettingInfo
     */
    private void dealLoadVehicle(LoadVehicleSettingInfo loadVehicleSettingInfo) {
        // ??????????????????
        // ????????????
        String[][] compensateArray = { { "1", "??????" }, { "2", "??????" } };
        for (String[] strings : compensateArray) {
            if (strings[0].equals(loadVehicleSettingInfo.getCompensate().toString())) {
                loadVehicleSettingInfo.setCompensateStr(strings[1]);
            }
        }
        if (StringUtils.isEmpty(loadVehicleSettingInfo.getCompensateStr())) {
            loadVehicleSettingInfo.setCompensateStr("??????");
        }
        // ????????????
        String[][] oddEvenCheckArray = { { "1", "?????????" }, { "2", "?????????" }, { "3", "?????????" } };
        for (String[] strings : oddEvenCheckArray) {
            if (strings[0].equals(loadVehicleSettingInfo.getOddEvenCheck())) {
                loadVehicleSettingInfo.setOddEvenCheckStr(strings[1]);
            }
        }
        if (StringUtils.isEmpty(loadVehicleSettingInfo.getOddEvenCheckStr())) {
            loadVehicleSettingInfo.setOddEvenCheckStr("?????????");
        }
        // ????????????
        String[][] filterFactorArray = { { "1", "??????" }, { "2", "??????" }, { "3", "??????" } };
        for (String[] strings : filterFactorArray) {
            if (strings[0].equals(loadVehicleSettingInfo.getFilterFactor().toString())) {
                loadVehicleSettingInfo.setFilterFactorStr(strings[1]);
            }
        }
        if (StringUtils.isEmpty(loadVehicleSettingInfo.getFilterFactorStr())) {
            loadVehicleSettingInfo.setFilterFactorStr("??????");
        }
        // ?????????
        String[][] baudRateArray =
            { { "1", "2400" }, { "2", "4800" }, { "3", "9600" }, { "4", "19200" }, { "5", "38400" }, { "6", "57600" },
                { "7", "115200" } };
        for (String[] strings : baudRateArray) {
            if (strings[0].equals(loadVehicleSettingInfo.getBaudRate())) {
                loadVehicleSettingInfo.setBaudRateStr(strings[1]);
            }
        }
        if (StringUtils.isEmpty(loadVehicleSettingInfo.getBaudRateStr())) {
            loadVehicleSettingInfo.setBaudRateStr("9600");
        }
        loadVehicleSettingInfo.setPersonLoadParam(
            JSONObject.parseObject(loadVehicleSettingInfo.getPersonLoadParamJSON(), PersonLoadParam.class));
    }

    private void resultMapInfo(int i, Map<String, Object> resultMap, String msg, String info) {
        resultMap.put("flag", i);
        resultMap.put("errorMsg", msg);
        resultMap.put("resultInfo", info);
    }

    @Override
    public List<LoadVehicleSettingInfo> findReferenceVehicleByProtocols(List<Integer> protocols) {
        List<LoadVehicleSettingInfo> list = new ArrayList<>();
        String userId = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (userId != null && !"".equals(userId) && orgList != null && orgList.size() > 0) {
            
            list = loadVehicleSettingDao.findLoadVehicleByProtocols(userId, orgList, protocols);
            for (LoadVehicleSettingInfo loadVehicleSettingInfo : list) {
                dealLoadVehicle(loadVehicleSettingInfo);
            }
            for (LoadVehicleSettingInfo loadVehicleSettingInfo : list) {
                dealLoadVehicle(loadVehicleSettingInfo);
            }
        }
        return list;
    }
}
