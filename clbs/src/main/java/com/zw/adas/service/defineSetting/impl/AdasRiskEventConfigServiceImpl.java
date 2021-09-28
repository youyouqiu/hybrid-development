package com.zw.adas.service.defineSetting.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventVehicleConfigForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventVehicleForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskEventConfigQuery;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskEventConfigDao;
import com.zw.adas.service.defineSetting.AdasRiskEventConfigService;
import com.zw.adas.service.defineSetting.AdasSendTxtService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.common.MonitorHelper;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.param.F3SensorParamQuery;
import com.zw.platform.domain.riskManagement.RiskType;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.controller.UserCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.FtpClientUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.adas.DriverVehicleWarning;
import com.zw.ws.entity.adas.VehicleWarning;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.simcard.T808Msg8106;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fanlu on 2017/8/22.
 */
@Service
public class AdasRiskEventConfigServiceImpl implements AdasRiskEventConfigService {

    private static final Logger log = LogManager.getLogger(AdasRiskEventConfigServiceImpl.class);

    @Autowired
    private AdasRiskEventConfigDao adasRiskEventConfigDao;

    @Autowired
    private AdasSendTxtService adasSendTxtService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterDao parameterDao;

    private RedisVehicleService redisVehicleService;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private NewVehicleDao vehicleDao;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private MonitorHelper monitorHelper;

    @Value("${ftp.path}")
    private String ftpPath;

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host.clbs}")
    private String ftpHostClbs;

    @Value("${ftp.port.clbs}")
    private int ftpPortClbs;

    @Autowired
    private GroupMonitorService groupMonitorService;

    private static final String[] eventCodes =
        { "6401", "6402", "6403", "6404", "6405", "6407", "6408", "6409", "6410", "6502", "6503", "6505", "6506",
            "6507", "6508", "6509", "6510" };

    private static final String[] dsmEventCodes = { "6502", "6503", "6505", "6506", "6507", "6508", "6509", "6510" };

    @Autowired
    public void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    @Override
    public List<Map<String, Object>> findRiskVehicleList(AdasRiskEventConfigQuery query) {

        Page<Map<String, Object>> page = new Page<>();

        try {
            // 车是具有绑定关系的
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", query.getGroupId());
            map.put("assignmentId", query.getAssignmentId());
            map.put("query", query.getSimpleQueryParam());
            Integer protocol = 1; // SQL里指定了device_type = 1
            List<String> vehicleList = redisVehicleService.getUserVehicles(map, null, protocol);
            if (vehicleList == null) {
                throw new RedisException(">=======redis 缓存出错了===========<");
            }
            Set<String> mids = vehicleDao.findAllMidsBytype(0);
            vehicleList = new ArrayList<>(Sets.intersection(new LinkedHashSet<>(vehicleList), mids));
            int listSize = vehicleList.size();
            int curPage = query.getPage().intValue();// 当前页
            int pageSize = query.getLimit().intValue(); // 每页条数
            int lst = (curPage - 1) * pageSize;// 遍历开始条数
            int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);// 遍历条数
            List<String> list = new ArrayList<>();
            for (int i = 0; i < vehicleList.size(); i++) {
                if (i >= lst && i < ps) {
                    list.add(vehicleList.get(i));
                }
            }
            List<Map<String, Object>> riskEventConfigList = new ArrayList<>();
            if (list.size() > 0) {
                riskEventConfigList = adasRiskEventConfigDao.findRiskVehicleListRedis(list);
            }
            // 按照车的绑定时间进行排序
            VehicleUtil.sort(riskEventConfigList, list);
            page = RedisQueryUtil.getListToPage(riskEventConfigList, query, listSize);

        } catch (Exception e) {
            if (e instanceof RedisException) {
                // 如果缓存出错或者不存在,就从数据库中获取
                String userId = SystemHelper.getCurrentUser().getId().toString();
                // 获取当前用户所属组织及下级组织
                List<String> orgList = userService.getCurrentUserOrgIds();
                if (userId != null && !userId.equals("") && orgList != null && orgList.size() > 0) {
                    return adasRiskEventConfigDao.findRiskVehicleList(query, userService.getCurrentUserUuid(), orgList);
                }
            } else {
                log.error("=====>风险定义设置出错<====", e);
            }
        }
        // 设置分组名称
        setAssignName(page);

        return page;
    }

    /**
     * 设置分组名称
     */
    public void setAssignName(Page<Map<String, Object>> page) {

        Set<String> vehicleIds = Sets.newHashSet();
        for (Map<String, Object> map : page) {
            vehicleIds.add((String) map.get("vehicleId"));
        }
        List<GroupMonitorDTO> list = groupMonitorService.getByMonitorIds(vehicleIds);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, List<String>> vehicleIdGroupNameMap = new HashMap<>(CommonUtil.ofMapCapacity(list.size()));
        for (GroupMonitorDTO monitorDTO : list) {
            vehicleIdGroupNameMap.computeIfAbsent(monitorDTO.getMonitorId(), o -> new ArrayList<>())
                .add(monitorDTO.getGroupName());
        }
        for (Map<String, Object> map : page) {
            // 获取车辆的分组名称
            List<String> groupNames = vehicleIdGroupNameMap.get((String) map.get("vehicleId"));
            if (groupNames == null) {
                continue;
            }
            map.put("groupName", StringUtils.join(groupNames, ","));
        }

    }

    @Override
    public List<AdasRiskEventVehicleConfigForm> findRiskSettingByVid(String vehicleId) {
        if (StringUtils.isNotBlank(vehicleId)) {
            return adasRiskEventConfigDao.findRiskSettingByVid(vehicleId);
        }
        return null;
    }

    @Override
    public List<AdasRiskEventVehicleConfigForm> findReferVehicle() {
        List<AdasRiskEventVehicleConfigForm> list = new ArrayList<>();
        String uuid = userService.getCurrentUserUuid();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (StringUtils.isNotBlank(uuid) && !orgList.isEmpty()) {
            list = adasRiskEventConfigDao.findReferVehicle(uuid, orgList);
        }
        return list;
    }

    @Override
    public void updateRiskSettingByBatch(List<String> vehicleIds, List<AdasRiskEventVehicleConfigForm> list,
        String ipAddress) throws Exception {
        if (vehicleIds != null && !vehicleIds.isEmpty()) {
            List<AdasRiskEventVehicleConfigForm> addConfigList = new ArrayList<>();
            List<AdasRiskEventVehicleForm> addRiskVehicleList = new ArrayList<>();
            // 先删除所有车的绑定
            deleteRiskSettingByVehicleIds(vehicleIds, ipAddress, "0");
            StringBuilder message = new StringBuilder(); // 日志记录内容
            // 管道存储redis
            FTPClient ftp = null;
            try {
                ftp = FtpClientUtil.getFTPClient(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs, ftpPath);
                Map<RedisKey, Map<String, String>> redisKeyMapMap = new HashMap<>();
                for (String vehicleId : vehicleIds) {
                    if (list != null && !list.isEmpty()) {
                        AdasRiskEventVehicleForm form = new AdasRiskEventVehicleForm();
                        List<String> parameterList = Lists.newLinkedList();
                        for (AdasRiskEventVehicleConfigForm config : list) {
                            if (vidAndRiskIdIsBlank(config)) {
                                continue;
                            }
                            addConfigList.add(AdasRiskEventVehicleConfigForm.getInstance(config, vehicleId));
                            int riskType = RiskType.getRiskType(config.getRiskId());
                            if (riskType != 0) {
                                Map<String, String> data = form.initAndGetAssembleData(config);
                                redisKeyMapMap.put(
                                    HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE_RISK_ID.of(vehicleId, config.getRiskId()),
                                    data);
                                String adasSetting = getAdasSetting(vehicleId, config.getRiskId(), data);
                                //zmqSender.sendMsg(adasSetting, zmqAdasReceiverName);
                                parameterList.add(adasSetting);
                            }
                        }
                        //通过订阅模式发送出去
                        sendMessage(parameterList);
                        // 风险设置
                        AdasRiskEventVehicleConfigForm setForm = list.get(0);
                        form.initRiskSetting(setForm, vehicleId);
                        addRiskVehicleList.add(form);
                        createFtpDirectory(vehicleId, ftp);
                    } else {
                        log.info("车辆ID为：" + vehicleId + "未能成功设置风险定义参数,传递的参数为：" + list);
                    }
                    initRiskSettingMessage(message, vehicleId);

                }
                RedisHelper.batchAddToHash(redisKeyMapMap);
                if (!addConfigList.isEmpty() && !addRiskVehicleList.isEmpty()) {
                    // 批量设置风险事件
                    adasRiskEventConfigDao.addRiskVehicleConfig(addConfigList);
                    // 批量设置风险
                    adasRiskEventConfigDao.addRiskVehicleByBatch(addRiskVehicleList);
                    if (vehicleIds.size() == 1) { // 单个设置
                        String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleIds.get(0));
                        logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
                    } /*else { // 批量设置
                        logSearchServiceImpl.addLog(ipAddress,"", "3", "batch", "批量设置风险定义");
                    }*/
                }
            } finally {
                FtpClientUtil.closeFtpConnect(ftp);
            }
        }
    }

    private void createFtpDirectory(String vehicleId, FTPClient ftp) {

        try {
            // 组装文件路径(ADAS/车id前两位/完整车id/年月/)
            String mediaPath = ftpPath + "/" + vehicleId.substring(0, 2) + "/" + vehicleId + "/" + getCurrentDay()
                + "/";
            // 创建目录
            boolean flag = FtpClientUtil.createDir(mediaPath, ftp);
            if (!flag) {
                log.info("----------------配置adas参数设置时创建多媒体证据Ftp路径失败----------------");
            }
        } catch (Exception e) {
            log.error("配置adas参数设置时创建多媒体证据Ftp路径异常", e);
        }
    }

    private String getCurrentDay() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private void initRiskSettingMessage(StringBuilder message, String vehicleId) {
        // 从缓存中取车辆信息
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, "orgName", "name");
        if (bindDTO != null) {
            String brand = bindDTO.getName();
            String orgName = bindDTO.getOrgName();
            if (!brand.isEmpty() && !orgName.isEmpty()) {
                message.append("监控对象 : ").append(brand).append(" ( @").append(orgName).append(" ) 风险设置 <br/>");
            }
        }
    }

    private boolean vidAndRiskIdIsBlank(AdasRiskEventVehicleConfigForm config) {
        return !StringUtils.isNotBlank(config.getVehicleId()) || !StringUtils.isNotBlank(config.getRiskId());
    }

    @Override
    public void deleteRiskSettingByVehicleIds(List<String> vehicleIds, String ipAddress, String sign) {
        if (vehicleIds != null && !vehicleIds.isEmpty()) {
            adasRiskEventConfigDao.deleteRiskVehicleByBatch(vehicleIds);
            adasRiskEventConfigDao.deleteRiskVehicleConfigByBatch(vehicleIds);

            //ZmqSender zmqSender = new ZmqSender(zmqSenderName, zmqSendHost);
            StringBuilder message = new StringBuilder();
            for (String vid : vehicleIds) {
                // 删除redis缓存
                String pattern = vid + "*";
                RedisHelper.deleteScanKeys(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE_RISK_ID_FUZZY.of(pattern));
                // 删除下发表的下发数据
                parameterDao.deleteByVechicleidParameterName(vid, "ADAS_" + vid, "F3-8103-64");
                parameterDao.deleteByVechicleidParameterName(vid, "ADAS_" + vid, "F3-8103-65");
                addDeleteRiskSettingLog(sign, message);
                sendDeleteRiskMsgToStorm(vid);
            }
            //zmqSender.close();
            addRemoveRiskSettingLog(vehicleIds, ipAddress, sign, message);
        }

    }

    private void sendDeleteRiskMsgToStorm(String vid) {
        List<String> parameterList = Lists.newLinkedList();
        for (String eventCode : eventCodes) {
            //zmqSender.sendMsg("#del#RISK_" + vid + "_" + eventCode, zmqAdasReceiverName);
            parameterList.add("#del#RISK_" + vid + "_" + eventCode);
        }
        sendMessage(parameterList);
    }

    private void addRemoveRiskSettingLog(List<String> vehicleIds, String ipAddress, String sign,
        StringBuilder message) {
        if ("1".equals(sign)) { // 1表示需要记录日志
            if (vehicleIds.size() == 1) {
                String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleIds.get(0));
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量解除风险设置");
            }
        }
    }

    @Override
    public String sendParamSet(List<String> vehicleIds, String ipAddress) throws Exception {
        List<AdasRiskEventVehicleConfigForm> settingList;
        StringBuilder message = new StringBuilder();
        for (String vid : vehicleIds) {
            settingList = adasRiskEventConfigDao.findRiskSettingByVid(vid);
            DriverVehicleWarning dvw = new DriverVehicleWarning();
            VehicleWarning vw = new VehicleWarning();
            for (AdasRiskEventVehicleConfigForm rv : settingList) {
                String sid = rv.getRiskId().substring(0, 2);
                String eventId = rv.getRiskId().substring(2);
                if ("64".equals(sid)) {
                    // 主动安全辅助系统驾驶员不在驾驶位置预警参数设置项 VehicleWarning
                    setVehicleWarning(vw, rv, eventId);
                }
                if ("65".equals(sid)) {
                    // 异常驾驶员行为报警参数设置项 DriverVehicleWarning
                    setDriverVehicleWarning(dvw, rv, eventId);
                }
            }

            // ADAS下发
            List<ParamItem> params = new ArrayList<>();
            ParamItem paramItem = new ParamItem();
            paramItem.setParamValue(vw);
            paramItem.setParamId(0xF364);
            paramItem.setParamLength(56);
            params.add(paramItem);
            adasSendTxtService
                .sendF3SetParam(vid, "ADAS_" + vid, params, "F3-8103-64", true, SystemHelper.getCurrentUsername());

            params = new ArrayList<>();
            paramItem = new ParamItem();
            paramItem.setParamValue(dvw);
            paramItem.setParamId(0xF365);
            paramItem.setParamLength(56);
            params.add(paramItem);
            adasSendTxtService
                .sendF3SetParam(vid, "ADAS_" + vid, params, "F3-8103-65", true, SystemHelper.getCurrentUsername());

            // 从缓存中获取车辆的基本信息
            BindDTO bindDTO = MonitorUtils.getBindDTO(vid, "orgName", "name");
            if (bindDTO != null) {
                String brand = bindDTO.getName();
                String orgName = bindDTO.getOrgName();
                if (!brand.isEmpty() && !orgName.isEmpty()) {
                    message.append("监控对象 : ").append(brand).append(" ( @").append(orgName).append(" ) 风险设置下发 <br/>");
                }
            }
        }
        if (vehicleIds.size() == 1) {
            String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleIds.get(0));
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "2", "", vehicle[0], vehicle[1]);
        } else {
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "2", "batch", "ADAS风险设置批量下发");
        }
        return String.valueOf(0);
    }

    /**
     * 设置 65下发参数
     */
    private void setDriverVehicleWarning(DriverVehicleWarning dvw, AdasRiskEventVehicleConfigForm rv, String eventId) {
        if (rv.getLowSpeed() != null) {
            dvw.setSpeedThreshold(rv.getLowSpeed());
        }
        // 设置拍照分辨率
        if (rv.getCameraResolution() != null) {
            dvw.setCameraResolution(Integer.parseInt(rv.getCameraResolution().substring(2), 16));
        }
        // 设置分辨率
        if (rv.getVideoResolution() != null) {
            dvw.setVideoResolution(Integer.parseInt(rv.getVideoResolution().substring(2), 16));
        }
        switch (eventId) {
            case "01":// 疲劳驾驶报警
                if (rv.getHighSpeed() != null) {
                    dvw.setFatigueSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    dvw.setFatigueVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    dvw.setFatigueCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    dvw.setFatigueCameraTime(rv.getPhotographTime());
                }
                break;
            case "02":// 接打电话报警
                if (rv.getHighSpeed() != null) {
                    dvw.setPickUpSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    dvw.setPickUpVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    dvw.setPickUpCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    dvw.setPickUpCameraTime(rv.getPhotographTime());
                }
                break;
            case "03":// 抽烟报警
                if (rv.getHighSpeed() != null) {
                    dvw.setSmokingSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    dvw.setSmokingVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    dvw.setSmokingCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    dvw.setSmokingCameraTime(rv.getPhotographTime());
                }
                break;
            case "04":// 注意力分散报警
                if (rv.getHighSpeed() != null) {
                    dvw.setAttentionSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    dvw.setAttentionVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    dvw.setAttentionCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    dvw.setAttentionCameraTime(rv.getPhotographTime());
                }
                break;
            case "05":// 异常报警
                if (rv.getHighSpeed() != null) {
                    dvw.setDriveDeedSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    dvw.setDriveDeedVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    dvw.setDriveDeedCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    dvw.setDriveDeedCameraTime(rv.getPhotographTime());
                }
                break;
            case "06":// 闭眼
                if (rv.getHighSpeed() != null) {
                    dvw.setCloseEyesSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    dvw.setCloseEyesVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    dvw.setCloseEyesCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    dvw.setCloseEyesCameraTime(rv.getPhotographTime());
                }
                break;
            case "07":// 打哈欠
                if (rv.getHighSpeed() != null) {
                    dvw.setYawnSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    dvw.setYawnVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    dvw.setYawnCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    dvw.setYawnCameraTime(rv.getPhotographTime());
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置 64 下发参数
     */
    private void setVehicleWarning(VehicleWarning vw, AdasRiskEventVehicleConfigForm rv, String eventId) {
        if (rv.getLowSpeed() != null) {
            vw.setSpeedThreshold(rv.getLowSpeed());
        }
        if (rv.getCameraResolution() != null) {
            vw.setCameraResolution(Integer.parseInt(rv.getCameraResolution().substring(2), 16));
        }
        if (rv.getVideoResolution() != null) {
            vw.setVideoResolution(Integer.parseInt(rv.getVideoResolution().substring(2), 16));
        }
        switch (eventId) {
            case "01":// 前向碰撞预警
                if (rv.getHighSpeed() != null) {
                    vw.setVehicleCollisionSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    vw.setVehicleCollisionVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    vw.setVehicleCollisionCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    vw.setVehicleCollisionCameraTime(rv.getPhotographTime());
                }
                break;
            case "02":// 车道偏离预警
                if (rv.getHighSpeed() != null) {
                    vw.setDeviateSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    vw.setDeviateVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    vw.setDeviateCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    vw.setDeviateCameraTime(rv.getPhotographTime());
                }
                break;
            case "03":// 车距过近预警
                if (rv.getHighSpeed() != null) {
                    vw.setDistanceSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    vw.setDistanceVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    vw.setDistanceCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    vw.setDistanceCameraTime(rv.getPhotographTime());
                }
                break;
            case "04":// 行人碰撞报警
                if (rv.getHighSpeed() != null) {
                    vw.setPedestrianCollisionSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    vw.setPedestrianCollisionVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    vw.setPedestrianCollisionCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    vw.setPedestrianCollisionCameraTime(rv.getPhotographTime());
                }
                break;
            case "05":// 频繁变道报警
                if (rv.getHighSpeed() != null) {
                    vw.setLaneChangeSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    vw.setLaneChangeVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    vw.setLaneChangeCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    vw.setLaneChangeCameraTime(rv.getPhotographTime());
                }
                break;
            case "07":// 障碍物
                if (rv.getHighSpeed() != null) {
                    vw.setObstacleSpeed(rv.getHighSpeed());
                }
                if (rv.getVideoRecordingTime() != null) {
                    vw.setObstacleVideoTime(rv.getVideoRecordingTime());
                }
                if (rv.getPhotographNumber() != null) {
                    vw.setObstacleCameraNum(rv.getPhotographNumber());
                }
                if (rv.getPhotographTime() != null) {
                    vw.setObstacleCameraTime(rv.getPhotographTime());
                }
                break;
            case "10":// 限速标志识别事件
                if (rv.getPhotographNumber() != null) {
                    vw.setSpeedLimitCameraNum(rv.getPhotographNumber());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public JsonResultBean sendPInfo(String vid, String sensorID, String commandType, String ipAddress) {

        BindDTO bindDTO = monitorHelper.getBindDTO(vid, MonitorTypeEnum.VEHICLE);
        String deviceNumber = bindDTO.getDeviceNumber();
        Integer msgSN = DeviceHelper.getRegisterDevice(vid, deviceNumber);
        String deviceId = bindDTO.getDeviceId();
        String simCardNumber = bindDTO.getSimCardNumber();
        String paramType = "0x8106-" + "0xF9" + sensorID.replaceAll("0x", "");
        String paramId = this.getLastSendParamID(vid, vid + paramType, paramType);

        if (msgSN != null) {
            // 订阅回应的user
            String username = SystemHelper.getCurrentUsername();
            UserCache.put(String.valueOf(msgSN), username);

            // 下发参数
            sendHelper.updateParameterStatus(paramId, msgSN, 4, vid, paramType, vid + paramType);

            List<F3SensorParamQuery> list = new ArrayList<>();
            F3SensorParamQuery query = new F3SensorParamQuery();
            query.setSign(Integer.parseInt(commandType.replaceAll("0x", ""), 16));
            query.setId(Integer.parseInt(sensorID.replaceAll("0x", ""), 16));
            list.add(query);

            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_PARAM_ACK);
            SubscibeInfoCache.getInstance().putTable(info);

            sendParam(simCardNumber, list, msgSN, deviceId, bindDTO);
        } else { // 设备未注册
            msgSN = 0;
            sendHelper.updateParameterStatus(paramId, msgSN, 5, vid, paramType, vid + paramType);
        }

        if ("0".equals(String.valueOf(msgSN))) { // 如果终端流水号为0,则终端离线
            return new JsonResultBean(JsonResultBean.FAULT, "终端离线");
        }

        String username = SystemHelper.getCurrentUsername();
        JSONObject json = new JSONObject();
        json.put("msgId", String.valueOf(msgSN));
        json.put("userName", username);
        return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
    }

    @Override
    public List<AdasRiskEventVehicleConfigForm> findDsmRiskSettingByVid(String vehicleId) {
        if (StringUtils.isNotBlank(vehicleId)) {
            return adasRiskEventConfigDao.findDsmRiskSettingByVid(vehicleId);
        }
        return null;

    }

    @Override
    public List<AdasRiskEventVehicleConfigForm> findAdasRiskSettingByVid(String vehicleId) {
        if (StringUtils.isNotBlank(vehicleId)) {
            return adasRiskEventConfigDao.findAdasRiskSettingByVid(vehicleId);
        }
        return null;
    }

    @Override
    public void updateADRiskSetting(String vehicleId, List<AdasRiskEventVehicleConfigForm> dbSettings, String ipAddress,
        boolean flag) {
        List<AdasRiskEventVehicleConfigForm> addConfigList = new ArrayList<>();
        List<AdasRiskEventVehicleForm> addRiskVehicleList = new ArrayList<>();
        // 先删除所有车的绑定
        deleteDsmByVehicleId(vehicleId, ipAddress, "0", flag);

        StringBuilder message = new StringBuilder(); // 日志记录内容
        // 管道存储redis
        //ZmqSender zmqSender = new ZmqSender(zmqSenderName, zmqSendHost);
        if (dbSettings != null && !dbSettings.isEmpty()) {
            AdasRiskEventVehicleForm form = new AdasRiskEventVehicleForm();
            List<String> parameterList = Lists.newLinkedList();
            Map<RedisKey, Map<String, String>> redisKeyMapMap = new HashMap<>();
            for (AdasRiskEventVehicleConfigForm config : dbSettings) {
                addConfigList.add(AdasRiskEventVehicleConfigForm.getInstance(config, vehicleId));
                int riskType = RiskType.getRiskType(config.getRiskId());
                if (riskType != 0) {
                    Map<String, String> data = form.initAndGetAssembleData(config);
                    String riskId = config.getRiskId();
                    parameterList.add(getAdasSetting(vehicleId, riskId, data));
                    // 存入redis
                    redisKeyMapMap.put(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE_RISK_ID.of(vehicleId, riskId), data);
                    //zmqSender.sendMsg(adasSetting, zmqAdasReceiverName);
                }
                sendMessage(parameterList);
            }
            AdasRiskEventVehicleConfigForm setForm = dbSettings.get(0);
            form.initRiskSetting(setForm, vehicleId);
            //修正下发需要
            form.initUpdateIssueSetting(setForm);
            addRiskVehicleList.add(form);
            RedisHelper.batchAddToHash(redisKeyMapMap);
        } else {
            log.info("车辆ID为：" + vehicleId + "未能成功设置风险定义参数,传递的参数为：" + dbSettings);
        }
        initLogMessage(flag, message);

        if (!addConfigList.isEmpty() && !addRiskVehicleList.isEmpty()) {
            // 批量设置风险事件
            adasRiskEventConfigDao.addRiskVehicleConfig(addConfigList);
            // 批量设置风险
            adasRiskEventConfigDao.addRiskVehicleByBatch(addRiskVehicleList);
            addLog(vehicleId, ipAddress, message);
        }

    }

    private void addLog(String vehicleId, String ipAddress, StringBuilder message) {
        try {
            String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleId);
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "修正下发", vehicle[0], vehicle[1]);
        } catch (Exception e) {
            log.error("记录日志失败！");
        }
    }

    private void initLogMessage(boolean flag, StringBuilder message) {
        // 从缓存中取车辆信息
        BindDTO bindDTO = MonitorUtils.getBindDTO("orgName", "name");
        if (bindDTO != null) {
            String orgName = bindDTO.getOrgName();
            String brand = bindDTO.getName();
            if (!brand.isEmpty() && !orgName.isEmpty()) {
                if (flag) {
                    message.append("监控对象 : ( ").append(brand).append(" )  修正下发ADAS参数设置 <br/>");
                } else {
                    message.append("监控对象 : (").append(brand).append(" )  修正下发DSM参数设置 <br/>");
                }

            }
        }
    }

    private String getAdasSetting(String vehicleId, String riskId, Map<String, String> data) {
        return "#set#" + "RISK_" + vehicleId + "_" + riskId + "#" + JSON.toJSONString(data);
    }

    private void deleteDsmByVehicleId(String vehicleId, String ipAddress, String s, boolean flag) {
        adasRiskEventConfigDao.deleteDsmRiskVehicleBind(vehicleId);
        if (flag) {
            adasRiskEventConfigDao.deleteAdasRiskVehicleConfig(vehicleId);
        } else {
            adasRiskEventConfigDao.deleteDsmRiskVehicleConfig(vehicleId);
        }
        //ZmqSender zmqSender = new ZmqSender(zmqSenderName, zmqSendHost);
        StringBuilder message = new StringBuilder();

        // 删除redis缓存
        String pattern = vehicleId + "_65*";
        RedisHelper.deleteScanKeys(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE_RISK_ID_FUZZY.of(pattern));
        // 删除下发表的下发数据
        parameterDao.deleteByVechicleidParameterName(vehicleId, "ADAS_" + vehicleId, "F3-8103-65");
        addDeleteRiskSettingLog(s, message);
        List<String> parameterList = Lists.newLinkedList();
        for (String eventCode : dsmEventCodes) {
            parameterList.add("#del#RISK_" + vehicleId + "_" + eventCode);
            //zmqSender.sendMsg("#del#RISK_" + vehicleId + "_" + eventCode, zmqAdasReceiverName);
        }
        sendMessage(parameterList);
        //zmqSender.close();
        if ("1".equals(s)) { // 1表示需要记录日志
            try {
                String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleId);
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } catch (Exception e) {
                log.error("dsm修正下发日志错误", e);
            }
        }

    }

    private void addDeleteRiskSettingLog(String s, StringBuilder message) {
        if ("1".equals(s)) { // 1表示需要记录日志
            // 从缓存中获取到车辆信息
            BindDTO bindDTO = MonitorUtils.getBindDTO("orgName", "name");
            if (bindDTO != null) {
                String brand = bindDTO.getName();
                String orgName = bindDTO.getOrgName();
                if (!brand.isEmpty() && !orgName.isEmpty()) {
                    message.append("监控对象 : ").append(brand).append(" ( @").append(orgName).append(" ) 解除风险设置 <br/>");
                }
            }
        }
    }

    public void sendParam(String mobile, List<F3SensorParamQuery> queries, Integer msgSN, String deviceId,
        BindDTO bindDTO) {
        List<Integer> paramIDs = new ArrayList<>(queries.size());
        for (F3SensorParamQuery q : queries) {
            paramIDs.add(Integer.parseInt(Integer.toHexString(q.getSign()) + Integer.toHexString(q.getId()), 16));
        }
        T808Msg8106 t8080x8106 = new T808Msg8106();
        t8080x8106.setParamSum(queries.size());
        t8080x8106.setParamIds(paramIDs);

        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T808_QUERY_PARAMS, msgSN, t8080x8106, bindDTO.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_QUERY_PARAMS, deviceId);

    }

    private String getLastSendParamID(String vehicleId, String paramid, String type) {
        List<Directive> paramlist = parameterDao.findParameterByType(vehicleId, paramid, type); // 6:报警
        Directive param;
        if (paramlist != null && !paramlist.isEmpty()) {
            param = paramlist.get(0);
            return param.getId();
        }
        return "";
    }

    private void sendMessage(List<String> parameterList) {
        if (parameterList.size() > 0) {
            ZMQFencePub.pubAdasRiskParam(JSON.toJSONString(parameterList));
        }
    }
}
