package com.zw.platform.service.driverDiscernManage.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Sets;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.ConfigService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.driverDiscernManage.DeviceDriverInfo;
import com.zw.platform.domain.basicinfo.driverDiscernManage.DriverDiscernManageInfo;
import com.zw.platform.domain.basicinfo.driverDiscernManage.DriverDiscernManageIssueParam;
import com.zw.platform.domain.basicinfo.driverDiscernManage.VehicleAssignmentInfo;
import com.zw.platform.domain.basicinfo.query.DriverDiscernManageQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.dto.driverMiscern.DeviceDriverDto;
import com.zw.platform.dto.driverMiscern.DeviceDriverStatusDto;
import com.zw.platform.dto.driverMiscern.DriverDiscernManageDto;
import com.zw.platform.dto.driverMiscern.VehicleDeviceDriverDo;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.driverDiscernManage.DriverDiscernManageDao;
import com.zw.platform.service.driverDiscernManage.DriverDiscernManageService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.DelayedEventTrigger;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.device.FaceInfo;
import com.zw.ws.entity.t808.device.T808_0x8E11;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.DRIVER_DISCERN_ISSUE_ACK_URL;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.DRIVER_DISCERN_QUERY_ACK_URL;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.FACE_ID_PATTERN;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.ISSUE_RESULT_ACK;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.ISSUE_RESULT_DEVICE_OFFLINE;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.ISSUE_RESULT_NOT_ACK;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.ISSUE_STATUS_FAIL;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.ISSUE_STATUS_ISSUING;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.ISSUE_STATUS_SUCCESS;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.PIC_SOURCE_TYPE;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.TYPE_ISSUE;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.TYPE_QUERY;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.URL_TYPE_HTTP;
import static com.zw.platform.dto.driverMiscern.DeviceDriverConstant.UUID_SERIALIZED_LENGTH;

/**
 * @Description: 驾驶员识别管理ServiceImpl
 * @Author Tianzhangxu
 * @Date 2020/9/27 10:44
 */
@Service
public class DriverDiscernManageServiceImpl implements DriverDiscernManageService {

    private static final Logger logger = LogManager.getLogger(DriverDiscernManageServiceImpl.class);

    /**
     * 树节点类型：企业
     */
    private static final int TREE_TYPE_OGR = 0;

    /**
     * 监控对象类型：车
     */
    private static final int MONITOR_TYPE_VEHICLE = 0;

    private static final String DEFAULT = "--";

    @Autowired
    private RedisVehicleService redisVehicleService;

    @Autowired
    private DriverDiscernManageDao manageDao;

    @Autowired
    private NewProfessionalsDao professionalsDao;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private WebSocketMessageDispatchCenter messageDispatchCenter;

    @Autowired
    private DelayedEventTrigger trigger;

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${adas.professionalFtpPath}")
    private String professionalFtpPath;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Override
    public Page<DriverDiscernManageDto> list(DriverDiscernManageQuery query) throws Exception {
        // 车是具有绑定关系的
        Map<String, Object> map = new HashMap<>(200);
        map.put("query", query.getSimpleQueryParam());
        if (null != query.getTreeType()) {
            if (query.getTreeType() == TREE_TYPE_OGR) {
                map.put("groupId", query.getTreeId());
            } else {
                map.put("assignmentId", query.getTreeId());
            }
        }
        // 协议类型
        Integer protocol = query.getDeviceType();
        List<String> vehicleList = redisVehicleService.getUserVehicles(map, null, protocol);
        if (vehicleList == null) {
            throw new RedisException(">=======redis 缓存出错了===========<");
        }
        // 监控对象类型为车（0）
        Set<String> moIds = newVehicleDao.findAllMidsBytype(MONITOR_TYPE_VEHICLE);
        // 去重 (用户所有的对应协议的监控对象和信息配置中的车辆监控对象去重)
        vehicleList = new ArrayList<>(Sets.intersection(new LinkedHashSet<>(vehicleList), moIds));
        if (CollectionUtils.isEmpty(vehicleList)) {
            return new Page<>();
        }
        query.setVehicleIds(vehicleList);
        // 用户当前组织和下级组织的组织id和组织名称map
        Map<String, String> orgIdAndName = userService.getCurrentUseOrgList().stream()
                .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        //查询车辆分组名称关系
        Map<String, String> vidAndAssignName = groupDao.findVehicleAndAssignNameInfos(vehicleList).stream()
                .collect(Collectors.toMap(VehicleAssignmentInfo::getVehicleId,
                        VehicleAssignmentInfo::getAssignmentName));
        //查询数据库
        Page<DriverDiscernManageInfo> list = PageHelperUtil
                .doSelect(query, () -> manageDao.findDriverDiscernManageInfo(query));
        Page<DriverDiscernManageDto> result = new Page<>();
        result.setTotal(list.getTotal());
        list.forEach(info -> {
            DriverDiscernManageDto manageDto = new DriverDiscernManageDto(
                    info.getMonitorId(),
                    info.getMonitorName(),
                    VehicleUtil.getPlateColorStr(info.getPlateColor().toString()),
                    orgIdAndName.get(info.getOrgId()),
                    vidAndAssignName.get(info.getMonitorId()),
                    info.getDriverNum(),
                    null != info.getLatestQueryTime()
                            ? DateUtil.getDateToString(info.getLatestQueryTime(), null) : null,
                    null != info.getQuerySuccessTime()
                            ? DateUtil.getDateToString(info.getQuerySuccessTime(), null) : null,
                    null != info.getLatestIssueTime()
                            ? DateUtil.getDateToString(info.getLatestIssueTime(), null) : null,
                    info.getIssueStatus(),
                    info.getIssueResult(),
                    info.getQueryResult(),
                    info.getIssueUsername()
            );
            result.add(manageDto);
        });
        return result;
    }

    @Override
    public List<DeviceDriverDto> listDriverDetail(String vehicleId) {
        List<DeviceDriverInfo> list = manageDao.findDriverDetail(vehicleId);
        //获取所有驾驶员所属组织ID,查询组织信息
        Set<String> orgIds = list.stream().map(DeviceDriverInfo::getGroupId).collect(Collectors.toSet());
        Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(orgIds);
        //组装返回参数
        return list.stream().map(o -> {
            DeviceDriverDto driverDto = new DeviceDriverDto();
            driverDto.setFaceId(o.getFaceId());
            driverDto.setProfessionalsId(o.getProfessionalsId());
            driverDto.setName(o.getName());
            driverDto.setCardNumber(o.getCardNumber());
            driverDto.setOrgName(StringUtils.isBlank(o.getGroupId()) ? DEFAULT : orgMap.get(o.getGroupId()).getName());
            String fileName = o.getPhotograph();
            if (StringUtils.isNotBlank(fileName)) {
                if (sslEnabled) {
                    driverDto.setPhotograph("/mediaserver" + professionalFtpPath + fileName);
                } else {
                    driverDto.setPhotograph(mediaServer + professionalFtpPath + fileName);
                }
            }
            return driverDto;
        }).collect(Collectors.toList());
    }

    @Override
    public void sendQueryBatch(List<String> vehicleIds, String sessionId) {
        //逐条进行订阅下发操作
        for (String id : vehicleIds) {
            BindDTO bindDTO = configService.getByMonitorId(id);
            String deviceId = bindDTO.getDeviceId();
            //生成下发流水号
            int msgSn = DeviceHelper.serialNumber(id);

            //设备离线 推送前端：下发失败，终端离线
            if (msgSn < 0) {
                DeviceDriverStatusDto failDto = new DeviceDriverStatusDto(id, ISSUE_STATUS_FAIL,
                        ISSUE_RESULT_DEVICE_OFFLINE);
                messageDispatchCenter.pushMsgToUser(sessionId, DRIVER_DISCERN_QUERY_ACK_URL,
                        JSON.toJSONString(failDto));
                // 新增或修改识别管理信息 最近查询时间
                DriverDiscernManageInfo info = new DriverDiscernManageInfo();
                info.setLatestQueryTime(new Date());
                info.setQueryResult(ISSUE_RESULT_DEVICE_OFFLINE);
                addOrUpdateDriverDiscernInfo(id, info);
                continue;
            }

            //推送前端：下发中;并更新数据库信息（最近查询时间）
            DeviceDriverStatusDto issuingDto = new DeviceDriverStatusDto(id, ISSUE_STATUS_ISSUING, null);
            messageDispatchCenter.pushMsgToUser(sessionId, DRIVER_DISCERN_QUERY_ACK_URL,
                JSON.toJSONString(issuingDto));
            DriverDiscernManageInfo manageInfo = new DriverDiscernManageInfo();
            manageInfo.setLatestQueryTime(new Date());
            addOrUpdateDriverDiscernInfo(id, manageInfo);

            //维护订阅关系缓存 key: vehicleId  value:sessionId
            //判断是否已有该车辆订阅关系，如果有，则更新value；没有，则直接添加订阅关系
            String oldSessionId = WsSessionManager.INSTANCE.getDriverDiscernSession(id);
            if (StringUtils.isNotBlank(oldSessionId)) {
                //如果已有订阅关系，则更新value，跳过下发逻辑，与之前对其车辆进行的查询下发共用
                String newSessionId = oldSessionId + "," + sessionId;
                WsSessionManager.INSTANCE.addDriverDiscern(newSessionId, id);
            } else {
                //如果没有订阅关系，则添加订阅关系，进行指令下发逻辑
                WsSessionManager.INSTANCE.addDriverDiscern(sessionId, id);
                int sendCommand = bindDTO.getDeviceType().equals(ProtocolEnum.T808_2013_HN.getDeviceType())
                    ? ConstantUtil.QUERY_DEVICE_DRIVER_REQ_HUNAN : ConstantUtil.QUERY_DEVICE_DRIVER_REQ;
                //下发查询指令
                T808Message t808Message = MsgUtil.get808Message(bindDTO.getSimCardNumber(), sendCommand,
                            msgSn, null, bindDTO.getDeviceType());
                WebSubscribeManager.getInstance().sendMsgToAll(t808Message, sendCommand, deviceId);
            }

            //同时添加应答延时事件
            trigger.addEvent(90, TimeUnit.SECONDS, () -> delay(TYPE_QUERY, id, id), id);

        }
    }

    @Override
    public void sendQueryAckHandle(Message message) {
        MsgDesc desc = message.getDesc();

        //从应答消息中获取相关车辆对应人脸ID，更新数据库 终端驾驶员与车辆关联信息
        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
        String deviceType = t808Message.getMsgHead().getDeviceType();
        JSONObject body = (JSONObject) t808Message.getMsgBody();
        List<String> rawFaceIds;
        if (Objects.equals(desc.getMsgID(), ConstantUtil.QUERY_DEVICE_DRIVER_REQ_HUNAN_ACK)) {
            rawFaceIds =  body.getJSONArray("faceList").toJavaList(JSONObject.class)
                .stream().map(o -> o.getString("faceId")).collect(Collectors.toList());
        } else {
            rawFaceIds = body.getJSONArray("list").toJavaList(String.class);
        }

        //组装订阅关系 key（也是延时事件KEY）,查询指令订阅关系key即为车辆ID
        String vid = desc.getMonitorId();
        dealDriverQueryAck(deviceType, rawFaceIds, vid);
    }

    private void dealDriverQueryAck(String deviceType, List<String> rawFaceIds, String vid) {
        //先取消对应延时应答事件
        trigger.cancelEvent(vid);

        //判断对应的订阅关系是否存在，如存在进行相应逻辑处理；不存在直接不处理
        String sessionId = WsSessionManager.INSTANCE.getDriverDiscernSession(vid);
        if (StringUtils.isNotBlank(sessionId)) {
            final Date querySuccessTime = new Date();
            //推送前端：下发成功，终端已应答 ;并更新数据库驾驶员管理相关信息（查询成功时间）
            DeviceDriverStatusDto successDto = new DeviceDriverStatusDto(vid,
                                                                        ISSUE_STATUS_SUCCESS,
                                                                        ISSUE_RESULT_ACK,
                                                                        querySuccessTime);
            //由于可能多个用户对同一车辆进行查询指令，将sessionId组装好，进行循环推送前端
            List<String> sessionIds = Arrays.asList(sessionId.split(","));
            sessionIds.forEach(o -> messageDispatchCenter.pushMsgToUser(o, DRIVER_DISCERN_QUERY_ACK_URL,
                    JSON.toJSONString(successDto)));

            DriverDiscernManageInfo manageInfo = new DriverDiscernManageInfo();
            manageInfo.setQuerySuccessTime(querySuccessTime);
            manageInfo.setQueryResult(ISSUE_RESULT_ACK);
            addOrUpdateDriverDiscernInfo(vid, manageInfo);

            // 终端人脸id因长度限制不含"-"，这里加回来
            List<VehicleDeviceDriverDo> vehicleDeviceDriverDos = new ArrayList<>();
            rawFaceIds.stream().forEach(o -> {
                VehicleDeviceDriverDo vehicleDeviceDriverDo = new VehicleDeviceDriverDo();
                vehicleDeviceDriverDo.setVehicleId(vid);
                vehicleDeviceDriverDo.setFaceId(o);
                if (Objects.equals(deviceType, ProtocolEnum.T808_2019_SD.getDeviceType()) || Objects
                    .equals(deviceType, ProtocolEnum.T808_2013_HN.getDeviceType())) {
                    try {
                        String[] driverInfos = o.split("_");
                        vehicleDeviceDriverDo.setProfessionalsId(professionalsDao
                            .getProByCardNumberAndCreateTime(driverInfos[0],
                                DateUtil.getStringToDate(driverInfos[1], DateUtil.DATE_YYMMDDHHMMSS)));
                    } catch (Exception e) {
                        logger.error("0E12/0E22根据人脸id获取平台从业人员id异常", e);
                    }
                } else {
                    if (o.length() == UUID_SERIALIZED_LENGTH && FACE_ID_PATTERN.matcher(o).matches()) {
                        vehicleDeviceDriverDo.setProfessionalsId(deserializeFaceId(o));
                    }
                }
                vehicleDeviceDriverDos.add(vehicleDeviceDriverDo);
            });
            manageDao.deleteDriverAndPro(vid);

            if (CollectionUtils.isNotEmpty(vehicleDeviceDriverDos)) {
                manageDao.insertDriverAndPro(vehicleDeviceDriverDos);
            }
            //删除对应订阅关系
            WsSessionManager.INSTANCE.removeDriverDiscernSession(vid);
        }
    }

    @Override
    public void sendIssueBatch(DriverDiscernManageIssueParam param, String sessionId) {
        List<String> proIds = param.getProIds();
        Integer type = param.getType();
        List<String> vehicleIds = param.getVehicleIds();
        String deviceType = param.getDeviceType();
        String userName = SystemHelper.getCurrentUsername();
        List<FaceInfo> faceInfos = new ArrayList<>();
        //查询对应从业人员相关信息,并组装0X8E11下发实体
        if (CollectionUtils.isNotEmpty(proIds)) {
            Map<String, ProfessionalDTO> proInfoMap = professionalsDao.findProfessionalsByIds(proIds).stream()
                            .collect(Collectors.toMap(ProfessionalDTO::getId, Function.identity()));
            faceInfos = proIds.stream().map(o -> {
                String faceId = serializeFaceId(o);
                FaceInfo info = new FaceInfo();
                ProfessionalDTO proInfo = proInfoMap.get(o);
                info.setUrlType(URL_TYPE_HTTP);
                info.setPictureSource(PIC_SOURCE_TYPE);
                if (Objects.nonNull(proInfo)) {
                    info.setCertificate(proInfo.getCardNumber());
                    info.setName(proInfo.getName());
                    //鲁标和湘标的
                    if (deviceType.equals(ProtocolEnum.T808_2019_SD.getDeviceType())
                        || deviceType.equals(ProtocolEnum.T808_2013_HN.getDeviceType())) {
                        String time = DateUtil.getDateToString(proInfo.getCreateDataTime(), DateUtil.DATE_YYMMDDHHMMSS);
                        faceId = proInfo.getCardNumber() + "_" + time + "_" + 1;
                    }
                    // 组装照片的完整路径
                    String fileName = proInfo.getPhotograph();
                    if (StringUtils.isNotBlank(fileName)) {
                        info.setPictureUrl(mediaServer + professionalFtpPath + fileName);
                    }
                }
                info.setFaceId(faceId);
                return info;
            }).collect(Collectors.toList());
        }
        T808_0x8E11 t8080x8E11 = new T808_0x8E11();
        t8080x8E11.setType(type);
        t8080x8E11.setFaceList(faceInfos);

        //逐条进行订阅下发操作
        for (String id : vehicleIds) {
            BindDTO bindDto = configService.getByMonitorId(id);
            String deviceId = bindDto.getDeviceId();
            //生成下发流水号
            int msgSn = DeviceHelper.serialNumber(id);

            //设备离线 推送前端：下发失败，终端离线
            if (msgSn < 0) {
                DeviceDriverStatusDto failDto = new DeviceDriverStatusDto(id, ISSUE_STATUS_FAIL,
                        ISSUE_RESULT_DEVICE_OFFLINE);
                messageDispatchCenter.pushMsgToUser(sessionId, DRIVER_DISCERN_ISSUE_ACK_URL,
                        JSON.toJSONString(failDto));
                // 新增或修改识别管理信息 最近下发时间
                DriverDiscernManageInfo info = new DriverDiscernManageInfo();
                info.setLatestIssueTime(new Date());
                info.setIssueUsername(userName);
                info.setIssueStatus(ISSUE_STATUS_FAIL);
                info.setIssueResult(ISSUE_RESULT_DEVICE_OFFLINE);
                addOrUpdateDriverDiscernInfo(id, info);
                continue;
            }

            //推送前端：下发中; 并更新数据库数据（最近下发时间，下发人）
            DeviceDriverStatusDto issuingDto = new DeviceDriverStatusDto(id, ISSUE_STATUS_ISSUING, null);
            messageDispatchCenter.pushMsgToUser(sessionId, DRIVER_DISCERN_ISSUE_ACK_URL,
                JSON.toJSONString(issuingDto));
            DriverDiscernManageInfo manageInfo = new DriverDiscernManageInfo();
            manageInfo.setLatestIssueTime(new Date());
            manageInfo.setIssueUsername(userName);
            addOrUpdateDriverDiscernInfo(id, manageInfo);

            //维护订阅关系缓存 key: vehicleId_流水号  value:sessionId
            String idAndMsg = id + "_" + msgSn;
            WsSessionManager.INSTANCE.addDriverDiscern(sessionId, idAndMsg);

            //下发指令下发 订阅0001通用应答
            SubscibeInfo info = new SubscibeInfo(userName, deviceId, msgSn, ConstantUtil.T808_DEVICE_GE_ACK);
            SubscibeInfoCache.getInstance().putTable(info);

            int sendCommand = deviceType.equals(ProtocolEnum.T808_2013_HN.getDeviceType())
                ? ConstantUtil.ISSUE_DEVICE_DRIVER_SYNCHRONIZE : ConstantUtil.ISSUE_DEVICE_DRIVER_DISCERN;

            T808Message t808Message = MsgUtil.get808Message(bindDto.getSimCardNumber(), sendCommand,
                            msgSn, t8080x8E11, bindDto.getDeviceType());
            WebSubscribeManager.getInstance().sendMsgToAll(t808Message, sendCommand, deviceId);

            //同时添加应答延时事件
            trigger.addEvent(90, TimeUnit.SECONDS, () -> delay(TYPE_ISSUE, idAndMsg, id), idAndMsg);

        }
    }



    /**
     * clbs人脸id -> 终端人脸id
     * <p>协议要求人脸id不超过32字节，而gbk字符集下的UUID是36位，这里删除4个"-"以满足长度要求
     */
    private static String serializeFaceId(String s) {
        return s.replaceAll("-", "");
    }

    /**
     * 终端人脸id -> clbs人脸id
     */
    private static String deserializeFaceId(String s) {
        return s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16)
                + "-" + s.substring(16, 20) + "-" + s.substring(20);
    }

    /**
     * 下发指令应答处理，只要收到应答，即认为下发成功，无需判断应答的具体内容
     * @param message message
     * @param msgSn 下发流水号
     */
    @Override
    public void sendIssueAckHandle(Message message, Integer msgSn) {
        MsgDesc desc = message.getDesc();
        String vid = desc.getMonitorId();
        //组装订阅关系 key（也是延时事件KEY）
        String idAndMsg = vid + "_" + msgSn;
        //先取消对应延时应答事件
        trigger.cancelEvent(idAndMsg);

        //判断对应的订阅关系是否存在，如存在进行相应逻辑处理；不存在直接不处理
        String sessionId = WsSessionManager.INSTANCE.getDriverDiscernSession(idAndMsg);
        if (StringUtils.isNotBlank(sessionId)) {
            //推送前端：下发成功，终端已应答 ;并更新数据库驾驶员管理相关信息
            DeviceDriverStatusDto successDto = new DeviceDriverStatusDto(vid,
                                                                        ISSUE_STATUS_SUCCESS,
                                                                        ISSUE_RESULT_ACK);
            messageDispatchCenter.pushMsgToUser(sessionId, DRIVER_DISCERN_ISSUE_ACK_URL,
                    JSON.toJSONString(successDto));
            DriverDiscernManageInfo manageInfo = new DriverDiscernManageInfo();
            manageInfo.setIssueResult(ISSUE_RESULT_ACK);
            manageInfo.setIssueStatus(ISSUE_STATUS_SUCCESS);
            addOrUpdateDriverDiscernInfo(vid, manageInfo);

            //删除对应订阅关系
            WsSessionManager.INSTANCE.removeDriverDiscernSession(idAndMsg);
        }
    }

    /**
     * 新增或修改驾驶员识别管理信息
     * @param vehicleId 车辆ID
     * @param info info
     */
    private void addOrUpdateDriverDiscernInfo(String vehicleId, DriverDiscernManageInfo info) {
        info.setMonitorId(vehicleId);
        DriverDiscernManageInfo oldInfo = manageDao.findByVid(vehicleId);
        if (Objects.isNull(oldInfo)) {
            info.setId(UUID.randomUUID().toString());
            manageDao.insert(info);
        } else {
            manageDao.update(info);
        }
    }


    /**
     * 应答延时事件逻辑方法
     * @param issueType 指令类型 0:查询指令 1:下发指令
     * @param idAndMsg 订阅缓存KEY
     * @param vehicleId 车辆ID
     */
    private void delay(Integer issueType, String idAndMsg, String vehicleId) {
        //判断驾驶员识别订阅关系是否存在，如果还存在说明90S未应答，则推送前端：下发失败，终端未应答
        String sessionId = WsSessionManager.INSTANCE.getDriverDiscernSession(idAndMsg);
        String sendUrl = issueType == TYPE_QUERY ? DRIVER_DISCERN_QUERY_ACK_URL : DRIVER_DISCERN_ISSUE_ACK_URL;
        if (StringUtils.isNotBlank(sessionId)) {
            DeviceDriverStatusDto failDto = new DeviceDriverStatusDto(vehicleId, ISSUE_STATUS_FAIL,
                    ISSUE_RESULT_NOT_ACK);
            messageDispatchCenter.pushMsgToUser(sessionId, sendUrl,
                    JSON.toJSONString(failDto));
            //删除订阅关系
            WsSessionManager.INSTANCE.removeDriverDiscernSession(idAndMsg);

            //根据对应指令，进行数据组装
            DriverDiscernManageInfo info = new DriverDiscernManageInfo();
            if (TYPE_ISSUE == issueType) {
                info.setIssueResult(ISSUE_RESULT_NOT_ACK);
                info.setIssueStatus(ISSUE_STATUS_FAIL);
            } else {
                info.setQueryResult(ISSUE_RESULT_NOT_ACK);
            }

            //更新数据库信息
            addOrUpdateDriverDiscernInfo(vehicleId, info);
        }
    }
}
