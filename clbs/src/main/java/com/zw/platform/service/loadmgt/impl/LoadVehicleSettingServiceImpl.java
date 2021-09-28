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
 @Description 载重车辆设置 实现
 @version 1.0
 **/
@Service
public class LoadVehicleSettingServiceImpl implements LoadVehicleSettingService {
    /**
     * 车
     */
    private static final Integer Monitor_Type_One = 1;
    /**
     * 物
     */
    private static final Integer Monitor_Type_Two = 2;
    /**
     * 人
     */
    private static final Integer Monitor_Type_Three = 3;
    /**
     * 载重传感器 1
     */
    public static final int LOAD_SENSOR_SEQUENCE_ID = 0xf370;
    /**
     * 载重传感器 2
     */
    public static final int LOAD_SENSOR_SEQUENCE_TWO_ID = 0xf371;

    private static final String import_ERROR_MESSAGE = "请将导入文件按照模板格式整理后再导入！";

    private static final String import_ERROR_INFO = "请将导入文件按照模板格式整理后再导入！";

    /**
     * 传感器型号最大长度
     */
    private static final Integer SENSOR_NUMBER_MAX_LENGTH = 10;
    private static final String import_ERROR_MSG = "成功导入0条数据!";
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
    private static final String TEMPLATE_COMMENT = "注：第一组实际载重值必须为'0'";

    @Override
    public Page<LoadVehicleSettingInfo> findLoadVehicleList(LoadVehicleSettingQuery query) {
        Page<LoadVehicleSettingInfo> page = new Page<>();
        try {
            /**
             * 从缓存中读取数据
             */
            page = getLoadVehicleFromCache(query);
        } catch (Exception e) {
            if (e instanceof RedisException) {
                String userId = userService.getCurrentUserUuid();
                // 获取当前用户所属组织及下级组织
                List<String> userOrgListId = userService.getCurrentUserOrgIds();
                if (StringUtils.isNotBlank(userId) && CollectionUtils.isNotEmpty(userOrgListId)) {
                    page = PageHelperUtil
                        .doSelect(query, () -> loadVehicleSettingDao.findLoadVehicleList(query, userId, userOrgListId));
                }
            }
            log.error("应用管理--->载重车辆管理分页查询失败", e);
        }

        // 处理result，将groupId对应的groupName给result相应的值赋上
        // 同时获取参数的下发状态
        setGroupNameByGroupId(page);
        return page;
    }

    /**
     * 查询车辆与载重传感器的绑定 单个
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

        // 如果缓存失败，从数据库中获取
        String userUuid = userService.getCurrentUserUuid();
        // 获取当前用户所属组织及下级组织
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
            // 参数下发
            String paramType = "F3-8103-loadSensor" + loadVehicleSettingInfo.getSensorSequence();
            List<Directive> paramlist1 = parameterDao
                .findParameterByType(loadVehicleSettingInfo.getVehicleId(), loadVehicleSettingInfo.getId(), paramType);
            // 标定下发
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
                } else if (param1.getStatus() == 4 || param2.getStatus() == 4) { // 有一个没有收到回应，则状态为已下发
                    loadVehicleSettingInfo.setStatus(4);
                } else if (param1.getStatus() == 7 || param2.getStatus() == 7) { // 有一个没有收到回应，则状态为已下发
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
     * 根据传感器类型查询传感器
     * @param sensorType
     * @return
     */
    @Override
    public List<ZwMSensorInfo> findSensorInfo(String sensorType) {
        return loadVehicleSettingDao.findSensor(sensorType);
    }

    /**
     * 根据绑定的车辆id获取绑定信息
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
                    // 单个传感器
                    loadVehicleSettingInfo = list.get(0);
                    dealLoadVehicle(loadVehicleSettingInfo);
                    //设置标定数据
                    ZwMCalibration calibration = loadAdDao.findBySensorVehicleId(loadVehicleSettingInfo.getId());
                    if (calibration != null) {
                        loadVehicleSettingInfo.setAdParamJson(calibration.getCalibration());
                    }
                } else if (list.size() == 2) {
                    //两个传感器
                    loadVehicleSettingInfo = list.get(0);
                    dealLoadVehicle(loadVehicleSettingInfo);
                    //设置标定数据
                    ZwMCalibration calibration = loadAdDao.findBySensorVehicleId(loadVehicleSettingInfo.getId());
                    if (calibration != null) {
                        loadVehicleSettingInfo.setAdParamJson(calibration.getCalibration());
                    }
                    LoadVehicleSettingInfo twoLoadVehicleSettingInfo = list.get(1);
                    dealLoadVehicle(twoLoadVehicleSettingInfo);
                    //设置标定数据
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
     * 根据车辆id获取车辆载重传感器信息
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
     * 查询参考对象
     * @return
     */
    @Override
    public List<LoadVehicleSettingInfo> findReferenceVehicle() {
        List<LoadVehicleSettingInfo> list = new ArrayList<>();
        String userId = userService.getCurrentUserUuid();
        // 获取当前用户所属组织及下级组织
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
     * 绑定传感器到车辆
     * @param form
     * @param ipAddress
     * @return
     */
    @Override
    public JsonResultBean addLoadVehicleSetting(LoadVehicleSettingSensorForm form, String ipAddress) throws Exception {
        String message = "";
        // 从缓存中获取到监控信息 1车 2物 3人
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        Objects.requireNonNull(bindInfo);
        String plateNumber = bindInfo.getName();
        String orgId = bindInfo.getOrgId();
        // 处理载重传感器
        // 载重处理第二个传感器处理
        LoadVehicleSettingSensorForm twoForm = null;
        // 如果存在第二个载重传感器
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
            message = "监控对象: " + plateNumber + "( @" + organizationLdap.getName() + ") 载重车辆设置";
            logSearchService.addLog(ipAddress, message, "3", "", "", "");
        }
        String value = getAddLoadRedis(form.getVehicleId());

        // 存redis
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
        // 从缓存中获取到车辆信息
        String vehicleId = form.getVehicleId();
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        Objects.requireNonNull(bindInfo);
        String plateNumber = bindInfo.getName();
        String orgId = bindInfo.getOrgId();
        LoadVehicleSettingInfo loadVehicleSettingInfo = findLoadBingInfo(vehicleId);
        // 因为此处可能存在两个发动机，因此根据车辆id查询绑定的发动机;  1发动机如果为空，表示数据不存在;
        if (Objects.isNull(loadVehicleSettingInfo) || StringUtils.isBlank(loadVehicleSettingInfo.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // 发动机2
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
        // 维护redis数据
        String redisValue = "";
        // 日志信息
        String message = "";
        if (StringUtils.isBlank(loadVehicleSettingInfo.getTwoId()) && StringUtils.isBlank(twoForm.getSensorId())) {
            // 如果之前有一个载重传感器,并且现在修改载重传感器，那么修改
            boolean flag = loadVehicleSettingDao.updateLoadSetting(form);
            if (!org.springframework.util.StringUtils.isEmpty(loadAdDao.findBySensorVehicleId(form.getId()))) {
                loadAdDao.updateCalibration(form.getId(), form.getSensorId(), form.getAdParamJson());
            } else {
                loadAdDao.addByBatch(UUID.randomUUID().toString(), "1", form.getId(), form.getSensorId(),
                    form.getVehicleId(), SystemHelper.getCurrentUsername(), form.getAdParamJson());
            }
            if (flag) {
                message = "监控对象 ：" + plateNumber + "( @" + groupName + ") 载重车辆设置修改";
            }
            /**
             * 删除载重设置的下发状态
             */
            clearSendStatus(vehicleId, form.getId(), null);

            redisValue = getLoadBindForm(form);
        } else if (StringUtils.isBlank(loadVehicleSettingInfo.getTwoId()) && StringUtils
            .isNotBlank(twoForm.getSensorId())) {
            // 如果之前有一个发动机，并且现在传递两个发动机，那么发动机1修改，2新增
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
                message = "监控对象 ：" + plateNumber + "( @" + groupName + ") 载重车辆设置修改1发动机,新增2发动机";
            }
            clearSendStatus(vehicleId, form.getId(), null);
            redisValue = getAddLoadRedis(vehicleId);
        } else if (StringUtils.isNotBlank(loadVehicleSettingInfo.getTwoId()) && StringUtils
            .isNotBlank(twoForm.getSensorId())) {
            // 如果之前有两个发动机，并且现在传递两个发动机，那么同时修改
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
                message = "监控对象 ：" + plateNumber + "( @" + groupName + ") 工时车辆设置修改两个发动机";
            }
            clearSendStatus(vehicleId, form.getId(), twoForm.getId());
            redisValue = getDoubleLoadForm(form, twoForm);
        } else if (StringUtils.isNotBlank(loadVehicleSettingInfo.getTwoId()) && StringUtils
            .isBlank(twoForm.getSensorId())) {
            // 如果之前有两个发动机， 并且现在传递一个，那么删除发动机2，修改发动机1
            boolean flag = loadVehicleSettingDao.updateLoadSetting(form);
            boolean flag1 = loadVehicleSettingDao.deleteLoadSetting(twoForm.getId());
            if (flag && flag1) {
                message = "监控对象 ：" + plateNumber + "( @" + groupName + ") 载重车辆设置修改1发动机，删除2发动机";
            }
            clearSendStatus(vehicleId, form.getId(), twoForm.getId());
            redisValue = getLoadBindForm(form);
        }

        //维护redis数据
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
     * 根据id进行解绑
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
     * 批量解绑传感器和车辆
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
            logSearchService.addLog(ipAddress, "批量解绑工时车辆设置", "3", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 根据传感器id获得传感器信息
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
     * 载重参数下发
     * @param paramList
     * @param ipAddress
     */
    @Override
    public void sendLoadSetting(List<JSONObject> paramList, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        String vehicleId = "";
        for (JSONObject obj : paramList) {
            // 载重
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
                calibrationParamId = obj.get("calibrationParamId").toString(); // 油箱设置
            }
            if (StringUtils.isNotBlank(sensorVehicleId) && StringUtils.isNotBlank(vehicleId)) {
                LoadVehicleSettingInfo sensorVehicle = loadVehicleSettingDao.findSensorVehicleByBindId(sensorVehicleId);

                if (sensorVehicle != null) {
                    dealLoadVehicle(sensorVehicle);
                    // 载重下发 F3-8103-loadSensor0: 载重; F3-8103-loadSensor1: 载重2
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
                    // 载重标定下发
                    ZwMCalibration bySensorVehicleId = loadAdDao.findBySensorVehicleId(sensorVehicle.getId());
                    if (!org.springframework.util.StringUtils.isEmpty(bySensorVehicleId)
                        && !org.springframework.util.StringUtils.isEmpty(bySensorVehicleId.getCalibration()) && !"[]"
                        .equals(bySensorVehicleId.getCalibration())) {
                        sendLoadCalibration(calibrationParamId, vehicleId, calibrationParamType, sensorVehicleId,
                            sensorVehicle, markv);
                    }
                    String plateNumber = sensorVehicle.getPlateNumber();
                    message.append("监控对象 : ").append(plateNumber).append(" 下发载重车辆设置参数").append(" <br/>");
                }

            }
        }
        if (StringUtils.isNotBlank(message.toString())) {
            if (paramList.size() == 1) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量下发载重车辆设置参数");
            }
        }
    }

    /**
     * 生成导入模板
     * @param response
     */
    @Override
    public void generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("序号");
        headList.add("AD值*");
        headList.add("实际载重（Kg）*");
        // 必填字段
        requiredList.add("传感器品牌");
        requiredList.add("传感器型号");
        // 默认设置第一条数据
        exportList.add("1");
        exportList.add("10");
        exportList.add("0");
        ExportOilBoxExcel export = new ExportOilBoxExcel(TEMPLATE_COMMENT, headList, requiredList, null);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();
    }

    /**
     * 根据模板导入AD数据
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
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 2, 0);
        int cellNum = importExcel.getLastCellNum();
        // 导入条数限制
        /**
         * 元素不够
         */
        if (cellNum != limitNum) {
            resultMapInfo(0, resultMap, import_ERROR_MESSAGE, import_ERROR_INFO);
            return resultMap;
        }
        // excel 转换成 list
        List<AdValueForm> list = importExcel.getDataList(AdValueForm.class, null);
        if (null != list && list.size() > 0) {
            totalNum = list.size();
        }
        // 导入数量有误
        if (totalNum > 50 || totalNum < 2) {
            resultMapInfo(0, resultMap, "导入数量需在2-50之间", "导入数量需在2-50之间");
            return resultMap;
        }
        List<AdValueForm> importList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            AdValueForm adValueForm = list.get(i);
            // 校验必填字段
            if (Converter.toBlank(adValueForm.getAdNumber()).equals("") || Converter.toBlank(adValueForm.getAdValue())
                .equals("") || Converter.toBlank(adValueForm.getAdActualValue()).equals("")) {
                resultMap.put("flag", 1);
                errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                failNum++;
                break;
            } else if (!adValueForm.getAdNumber().matches("^[0-9]+$")) {
                resultMap.put("flag", 1);
                errorMsg.append("第").append(i + 1).append("条数据【").append(adValueForm.getAdNumber())
                    .append("】:AD只能是数字<br/>");
                failNum++;
                break;
            } else if (!adValueForm.getAdValue().matches("^[0-9]+$")) {
                resultMap.put("flag", 1);
                errorMsg.append("第").append(i + 1).append("条数据【").append(adValueForm.getAdValue())
                    .append("】:AD只能是数字<br/>");
                failNum++;
                break;
            } else if (!adValueForm.getAdActualValue().matches("^[0-9]+$")) {
                resultMap.put("flag", 1);
                errorMsg.append("第").append(i + 1).append("条数据【").append(adValueForm.getAdActualValue())
                    .append("】:AD只能是数字<br/>");
                failNum++;
                break;
            }
            if (i == 0 && !(adValueForm.getAdActualValue().equals("0"))) {
                errorMsg.append("第").append(i + 1).append("条数据【").append(adValueForm.getAdActualValue())
                    .append("】:AD只能是0<br/>");
                failNum++;
                break;
            }
            if (!RegexUtils.checkRightfulString1(adValueForm.getAdNumber())) {
                resultMap.put("flag", 1);
                errorMsg.append("第").append(i + 1).append("条数据实际载重【").append(adValueForm.getAdNumber())
                    .append("】:AD值包含特殊字符<br/>");
                failNum++;
                break;
            } else if (Converter.toBlank(adValueForm.getAdNumber()).length() > SENSOR_NUMBER_MAX_LENGTH) {
                resultMap.put("flag", 1);
                errorMsg.append("第").append(i + 1).append("条数据【").append(adValueForm.getAdNumber()).append("】:AD值长度超过")
                    .append(SENSOR_NUMBER_MAX_LENGTH).append("<br/>");
                failNum++;
                break;
            }
            importList.add(adValueForm);
            message.append("AD标定数据 : ").append(adValueForm.getAdNumber()).append(" <br/>");
        }
        boolean flag = false;
        // 组装导入结果
        if (importList.size() == list.size()) {
            // 排序判断 先判断AD值是否是升序 在判断ADact是否是升序
            flag = getSortImport(list, resultMap, errorMsg);
            String listValue = JSONObject.toJSONString(importList);
            if (!org.springframework.util.StringUtils.isEmpty(listValue) && flag) {
                String resultInfo = "";
                resultMap.put("listValue", listValue);
                resultInfo += "成功" + (totalNum - failNum) + "条,失败" + failNum + "条。";
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
                errorMsg.append("AD数值没有升序排列");
                return false;
            }
            if (new BigDecimal(list.get(i).getAdActualValue())
                .compareTo(new BigDecimal(list.get(i + 1).getAdActualValue())) > 0) {
                resultMap.put("flag", 1);
                errorMsg.append("实际载重数值没有升序排列");
                return false;
            }
        }
        return true;
    }

    /**
     * 根据id查询模板
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
     * 更新标定表
     * @param id
     * @param sensorVehicleId
     * @param calibrationValue
     * @return
     */
    @Override
    public String updateCalibration(String id, String sensorVehicleId, String calibrationValue) throws Exception {
        // 插入数据
        String valueId = "";
        //        if(org.springframework.util.StringUtils.isEmpty(id)){
        //            // 导入逻辑 批量新增
        //            String str = UUID.randomUUID().toString();
        //            loadAdDao.addByBatch(str,"1",SystemHelper.getCurrentUser().getUsername(),calibrationValue);
        //            valueId =str;
        //        }else{
        //            // 更新数据
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
            // 维护车和传感器的缓存
            final Map<String, String> vehicleInfo = RedisHelper.getHashMap(
                    RedisKeyEnum.MONITOR_INFO.of(form.getVehicleId(), "name", "plateColor", "orgName"));
            if (vehicleInfo != null) {
                String brand = vehicleInfo.get("name");
                String plateColor = vehicleInfo.get("plateColor");
                String orgName = vehicleInfo.get("orgName");
                String msg = "监控对象：" + brand + "( @" + orgName + ") 修改载重设置参数";
                logSearchService.addLog(ipAddress, msg, "3", "", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 获取车辆位置信息
     * @param vehicleId
     * @return
     */
    @Override
    public String getLatestPositional(String vehicleId) throws Exception {
        // 获取车辆及设备信息
        BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicleInfo.getDeviceNumber();
        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // 设备已经注册
        if (msgSN != null) {
            // 下发参数
            sendvehicleLocationQuery(msgSN, vehicleInfo);
        }
        return String.valueOf(msgSN);
    }

    private void sendvehicleLocationQuery(Integer transNo, BindDTO vehicleInfo) {
        String deviceId = vehicleInfo.getDeviceId();
        //订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_GPS_INFO_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimCardNumber(), ConstantUtil.T808_QUERY_LOCATION_COMMAND, transNo, null,
                vehicleInfo);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_QUERY_LOCATION_COMMAND, deviceId);
    }

    /**
     * 标定下发
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
        // 获取车辆及设备信息
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // 设备已经注册s
        if (msgSN != null) {
            // 已下发
            int status = 4;
            // 下发参数
            // 先更新下发状态再下发: 避免车辆第一次下发不存在paramId的情况
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // 载重标定
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
             * 设备未注册
             */
            int status = 5;
            msgSN = 0;
            // 载重绑定下发
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
            //下发后需要更新在载重管理模块,调用通用应答模块的逻辑,推送用户订阅的载重的websocket接口
            paramSendingCache
                .put(SystemHelper.getCurrentUsername(), transNo, simcardNumber, SendTarget.getInstance(LOAD));
            //订阅推送消息
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
     * 下发载重传感器
     * @param paramId                参数id
     * @param vehicleId              车辆id
     * @param paramType              参数类型
     * @param sensorVehicleId        车辆绑定传感器id
     * @param loadVehicleSettingInfo 传感器配置信息;
     * @param mark                   0:常规参数下发/参数下发;
     */
    public String sendLoadSensor(String paramId, String vehicleId, String paramType, String sensorVehicleId,
        LoadVehicleSettingInfo loadVehicleSettingInfo, int mark) throws Exception {
        // 获取车辆及设备信息
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();
        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // 设备已经注册
        if (msgSN != null) {
            // 已下发
            int status = 4;
            // 下发参数
            // 先更新下发状态再下发: 避免车辆第一次下发不存在paramId的情况
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // 载重绑定下发
            // 处理数据
            LoadSettingParam loadSettingParam = new LoadSettingParam();
            dealLoadVehilToLoadParam(loadVehicleSettingInfo, loadSettingParam);
            sensorService.sendLoadSensorParam(vehicle, loadSettingParam, msgSN, mark);

            // updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
        } else {
            /**we
             * 设备未注册
             */
            int status = 5;
            msgSN = 0;
            // 载重绑定下发
            sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
        }
        return String.valueOf(msgSN);

    }

    private void dealLoadVehilToLoadParam(LoadVehicleSettingInfo loadVehicleSettingInfo,
        LoadSettingParam loadSettingParam) {
        PersonLoadParam personLoadParam = loadVehicleSettingInfo.getPersonLoadParam();
        loadSettingParam.setCompensate(loadVehicleSettingInfo.getCompensate());
        loadSettingParam.setUploadTime(1);
        // 数据库 对应方式为  载重测量方式 0:单计重 1:双计重 2:四计重
        // 数据库 对应方式为  载重测量方式 1:单计重 2:双计重 4:四计重
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
        // 关联表sensor_vehicle id
        LoadVehicleSettingInfo loadBingInfo = loadVehicleSettingDao.findSensorVehicleByBindId(id);
        if (Objects.isNull(loadBingInfo)) {
            return flag;
        }

        String vehicleId = loadBingInfo.getVehicleId();
        LoadVehicleSettingInfo loadVehicleSetting = findLoadBingInfo(vehicleId);
        if (Objects.isNull(loadVehicleSetting)) {
            return flag;
        }
        // 只有发动机1,直接删除
        boolean flag1 =
            StringUtils.isNotBlank(loadVehicleSetting.getId()) && StringUtils.isBlank(loadVehicleSetting.getTwoId());
        // 直接删除发动机2
        boolean flag2 = StringUtils.isNotBlank(loadVehicleSetting.getId())
            && loadBingInfo.getSensorSequence() == WorkHourSettingInfo.SENSOR_SEQUENCE_TWO;
        // 有发动机2,并且删除发动机1,删除发动机1后修改发动机2的sensor_sequence 为0
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
            String message = "监控对象 ：" + plateNumber + "( @" + orgName + ") 解绑载重车辆设置";
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
     * 新增时从数据库获取信息
     * @param vehicleId
     * @return
     */
    private String getAddLoadRedis(String vehicleId) {
        // 维护redis缓存
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
     * 封装返回值 赋值groupName 和下发参数状态param
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
                // 下发参数状态
                if (StringUtils.isNotBlank(parameter.getVehicleId()) && StringUtils.isNotBlank(parameter.getId())) {
                    // 参数下发
                    String paramType = "F3-8103-loadSensor" + parameter.getSensorSequence();
                    List<Directive> paramlist1 =
                        parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), paramType);
                    // 标定下发
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
                        } else if (param1.getStatus() == 4 || param2.getStatus() == 4) { // 有一个没有收到回应，则状态为已下发
                            parameter.setStatus(4);
                        } else if (param1.getStatus() == 7 || param2.getStatus() == 7) { // 有一个没有收到回应，则状态为已下发
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
         * 初始化redis query
         */
        RedisSensorQuery redisQuery =
            new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                Integer.valueOf(query.getProtocol()));
        /**
         * 从缓存中读取车辆id和载重传感器id
         * RedisKeys.SensorType.SENSOR_LOAD_MONITOR 这个KEYS中包含的的是载重传感器和绑定车辆的id
         */
        List<String> cacheIdList =
            redisVehicleService.getVehicleByType(redisQuery, RedisKeys.SensorType.SENSOR_LOAD_MONITOR);
        /**
         * 判定缓存列表是否为空
         */
        if (cacheIdList == null) {
            throw new RedisException(">=======redis 缓存出错了===========<");
        }
        int total = cacheIdList.size();
        /**
         * 当前页
         */
        int curPage = query.getPage().intValue();
        /**
         * 每页条数
         */
        int pageSize = query.getLimit().intValue();
        /**
         * 遍历开始条数
         */
        int start = (curPage - 1) * pageSize;
        /**
         * 遍历结束条数
         */
        int end = pageSize > (total - start) ? total : (pageSize * curPage);
        List<String> queryList = cacheIdList.subList(start, end);
        List<LoadVehicleSettingInfo> resultList = new LinkedList<>();
        /**
         * 从缓存重截取 开始 结束页
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
             * 根据车辆id列表读取数据中的车辆信息
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
            //排序
            VehicleUtil.sort(resultList, vehicleIds);
        }
        return RedisQueryUtil.getListToPage(resultList, query, total);
    }

    /**
     * 处理传感器参数信息 str转义 JSON数据解析
     * @param loadVehicleSettingInfo
     */
    private void dealLoadVehicle(LoadVehicleSettingInfo loadVehicleSettingInfo) {
        // 定义二维数组
        // 补偿使能
        String[][] compensateArray = { { "1", "使能" }, { "2", "禁用" } };
        for (String[] strings : compensateArray) {
            if (strings[0].equals(loadVehicleSettingInfo.getCompensate().toString())) {
                loadVehicleSettingInfo.setCompensateStr(strings[1]);
            }
        }
        if (StringUtils.isEmpty(loadVehicleSettingInfo.getCompensateStr())) {
            loadVehicleSettingInfo.setCompensateStr("使能");
        }
        // 奇偶校验
        String[][] oddEvenCheckArray = { { "1", "奇校验" }, { "2", "偶校验" }, { "3", "无校验" } };
        for (String[] strings : oddEvenCheckArray) {
            if (strings[0].equals(loadVehicleSettingInfo.getOddEvenCheck())) {
                loadVehicleSettingInfo.setOddEvenCheckStr(strings[1]);
            }
        }
        if (StringUtils.isEmpty(loadVehicleSettingInfo.getOddEvenCheckStr())) {
            loadVehicleSettingInfo.setOddEvenCheckStr("无校验");
        }
        // 滤波系数
        String[][] filterFactorArray = { { "1", "实时" }, { "2", "平滑" }, { "3", "平稳" } };
        for (String[] strings : filterFactorArray) {
            if (strings[0].equals(loadVehicleSettingInfo.getFilterFactor().toString())) {
                loadVehicleSettingInfo.setFilterFactorStr(strings[1]);
            }
        }
        if (StringUtils.isEmpty(loadVehicleSettingInfo.getFilterFactorStr())) {
            loadVehicleSettingInfo.setFilterFactorStr("平稳");
        }
        // 波特率
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
        // 获取当前用户所属组织及下级组织
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
