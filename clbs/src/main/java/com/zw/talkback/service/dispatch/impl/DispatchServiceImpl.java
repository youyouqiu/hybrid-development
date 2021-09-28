package com.zw.talkback.service.dispatch.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.alarm.AlarmInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.functionconfig.Circle;
import com.zw.platform.domain.functionconfig.FenceInfo;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LineSpot;
import com.zw.platform.domain.functionconfig.Mark;
import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.domain.functionconfig.query.LineSegmentInfo;
import com.zw.platform.domain.multimedia.AlarmHandleForm;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.push.factory.AlarmFactory;
import com.zw.platform.repository.modules.FenceManagementDao;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.functionconfig.CircleService;
import com.zw.platform.service.functionconfig.LineService;
import com.zw.platform.service.functionconfig.MarkService;
import com.zw.platform.service.functionconfig.PolygonService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.IntercomObjectInfo;
import com.zw.talkback.domain.basicinfo.TempAssignmentInterlocutor;
import com.zw.talkback.domain.basicinfo.form.JobInfoData;
import com.zw.talkback.domain.dispatch.DispatchErrorMessageEnum;
import com.zw.talkback.domain.dispatch.DispatchGroupInfo;
import com.zw.talkback.domain.dispatch.IntercomInfoBean;
import com.zw.talkback.domain.dispatch.IntercomObjectBean;
import com.zw.talkback.domain.dispatch.PointInfo;
import com.zw.talkback.repository.mysql.ClusterDao;
import com.zw.talkback.repository.mysql.IntercomObjectDao;
import com.zw.talkback.repository.mysql.JobManagementDao;
import com.zw.talkback.service.dispatch.DispatchService;
import com.zw.talkback.service.dispatch.MonitoringDispatchService;
import com.zw.talkback.util.CheckIdentityUtil;
import com.zw.talkback.util.TalkCallUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class DispatchServiceImpl implements DispatchService {

    private final Logger logger = LogManager.getLogger(DispatchServiceImpl.class);

    private static final Integer IS_TEMPORARY_ASSIGNMENT = 3;

    //个呼
    private static final int INDIVIDUAL_CALL = 1;

    //电话
    private static final int PHONE_CALL = 2;

    //禁言
    private static final int BANNED_TALK = 3;

    //解除禁言
    private static final int REMOVE_BANNED = 4;

    //开启定位
    private static final int POSITIONING = 5;

    // 组呼
    private static final int GROUP_CALL = 6;

    //加入群组
    private static final int JOIN_GROUP = 7;

    //退出群组
    private static final int QUIT_GROUP = 8;

    //抢麦
    private static final int ROB_MAK = 9;

    //对讲对象页面调度
    private static final int IS_MONITOR_BOUNCED = 1;

    //对讲群组页面调度
    private static final int IS_ASSIGNMENT_BOUNCED = 2;

    private static final String MONITORING_MODULE = "MONITORING";

    /**
     * 轨迹回放
     */
    private static final String TRACK = "/v/monitoring/trackPlayback";

    @Autowired
    private UserService userService;

    @Autowired
    private JobManagementDao jobManagementDao;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private ClusterDao clusterDao;
    @Autowired
    private IntercomObjectDao intercomObjectDao;

    @Autowired
    private TalkCallUtil talkCallUtil;

    @Autowired
    private PositionalService positionalService;

    @Autowired
    MonitoringDispatchService monitoringDispatchService;

    @Autowired
    private AlarmSettingDao alarmSettingDao;

    @Autowired
    MarkService markService;

    @Autowired
    LineService lineService;

    @Autowired
    CircleService circleService;

    @Autowired
    PolygonService polygonService;

    @Autowired
    private FenceManagementDao fenceManagementDao;

    @Autowired
    AlarmFactory alarmFactory;

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Override
    public JsonResultBean invisibleList() {
        List<IntercomObjectBean> result = new ArrayList<>();
        //用户所属组织的平台分组id集合（不包含临时组）
        Set<String> assignmentPlatformIds = new HashSet<>();
        //用户所属组织的对讲平台群组集合（包含临时组）
        Set<Long> assignmentIntercomIds = new HashSet<>();
        //用户所属组织的临时组id集合
        Set<String> temporaryAssignment = new HashSet<>();
        //组装平台分组id集合和对讲群组id集合
        assemblyIdData(assignmentPlatformIds, assignmentIntercomIds, temporaryAssignment);
        if (assignmentIntercomIds.size() == 0) {
            return new JsonResultBean(result);
        }
        //通过第三方获取当前可见的用户id
        Set<Long> userIdSet = findUserIdSet(assignmentIntercomIds, temporaryAssignment);
        if (userIdSet == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "调度服务未登录");
        }
        //获取监控对象idSet
        final Set<String> monitorIds = this.loadMonitorIdsFromRedis();

        //过滤出是对讲对象的监控对象
        List<IntercomObjectBean> intercomObjectBeanList = findIntercomObjectList(monitorIds);
        // 过滤出不可见的对讲对象
        result = intercomObjectBeanList.stream().filter(bean -> !userIdSet.contains(bean.getUserId()))
            .collect(Collectors.toList());
        return new JsonResultBean(result);
    }

    private List<IntercomObjectBean> findIntercomObjectList(Set<String> monitorIds) {
        List<IntercomObjectBean> intercomObjectBeanList = new ArrayList<>();
        doActionOnIntercomMonitorsFromRedis(new ArrayList<>(monitorIds), intercomObject -> {
            IntercomObjectBean objectBean = new IntercomObjectBean();
            Long userId = intercomObject.getUserId();
            objectBean.setId(intercomObject.getMonitorId());
            objectBean.setGroupId(intercomObject.getGroupId());
            objectBean.setGroupName(intercomObject.getGroupName());
            objectBean.setName(intercomObject.getMonitorName());
            objectBean.setUserId(userId);
            intercomObjectBeanList.add(objectBean);
        });
        return intercomObjectBeanList;
    }

    /**
     * 通过第三方获取当前可见的用户id
     * @param assignmentIntercomIds 用户所属组织的对讲平台群组集合（包含临时组）
     * @param temporaryAssignment   用户所属组织的临时组id集合
     * @return 可见用户ID
     */
    private Set<Long> findUserIdSet(Set<Long> assignmentIntercomIds, Set<String> temporaryAssignment) {
        Set<Long> userIdSet = new HashSet<>();
        JSONObject dispatchUserLoginInfo =
            WebSubscribeManager.getInstance().getDispatchUserLoginInfo(SystemHelper.getCurrentUsername());
        if (dispatchUserLoginInfo == null) {
            return null;
        }
        JSONObject data = dispatchUserLoginInfo.getJSONObject("data");
        for (Long assignmentId : assignmentIntercomIds) {
            JSONObject queryInGroupMemberListResultJsonObj =
                talkCallUtil.queryInGroupMemberList(data.getLong("custId"), assignmentId, null, null, 1, 500);
            if (queryInGroupMemberListResultJsonObj == null) {
                logger.error("查询对讲组内用户异常");
                continue;
            }
            Integer queryInGroupMemberListStatusCode = queryInGroupMemberListResultJsonObj.getInteger("result");
            if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), queryInGroupMemberListStatusCode)) {
                logger.error(DispatchErrorMessageEnum.getMessage(queryInGroupMemberListStatusCode));
                continue;
            }
            JSONArray records = queryInGroupMemberListResultJsonObj.getJSONObject("data").getJSONArray("records");
            Set<Long> interlocutorIdSet =
                records.stream().map(info -> ((JSONObject) info).getLong("userId")).collect(Collectors.toSet());
            userIdSet.addAll(interlocutorIdSet);
        }
        //过滤出是在其他临时组的userId
        if (userIdSet.size() > 0) {
            Set<Long> otherUserId = clusterDao.findOtherTemporaryAssignmentUserIds(userIdSet, temporaryAssignment);
            userIdSet.removeAll(otherUserId);
        }
        return userIdSet;
    }

    private void assemblyIdData(Set<String> assignmentPlatformIds, Set<Long> assignmentIntercomIds,
        Set<String> temporaryAssignment) {
        UserLdap user = SystemHelper.getCurrentUser();
        String userId = user.getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(",");
        String orgId = userId.substring(beginIndex + 1);
        //获得当前用户的所属组织uuid
        OrganizationLdap currentOrganization = userService.getOrgByEntryDN(orgId);
        if (Objects.equals(user.getUsername(), "admin")) {
            // 用户当前组织和所属下级组织信息
            List<OrganizationLdap> userOwnAuthorityOrganizeInfo = userService.getUserOwnAuthorityOrganizeInfo(orgId);
            currentOrganization = monitoringDispatchService
                .updateOrganizationStructure(currentOrganization, userOwnAuthorityOrganizeInfo);
        }
        List<Map<String, Object>> assignmentMapList = clusterDao.findAssignmentMap(uuid, currentOrganization.getUuid());
        for (Map<String, Object> map : assignmentMapList) {
            //临时组不算是平台权限
            if (!IS_TEMPORARY_ASSIGNMENT.equals(Integer.parseInt(map.get("type").toString()))) {
                assignmentPlatformIds.add(String.valueOf(map.get("assignmentPlatformId")));
            } else {
                temporaryAssignment.add(String.valueOf(map.get("assignmentPlatformId")));
            }
            if (map.get("assignmentIntercomId") == null) {
                continue;
            }
            assignmentIntercomIds.add(Long.parseLong(String.valueOf(map.get("assignmentIntercomId"))));
        }
    }

    @Override
    public JsonResultBean findDetailsInfo(String id) {
        IntercomInfoBean result = new IntercomInfoBean();
        String intercomObjectStr = RedisHelper.getString(HistoryRedisKeyEnum.INTERCOM_LIST.of(id));
        IntercomObjectInfo intercomObject = JSONObject.parseObject(intercomObjectStr, IntercomObjectInfo.class);
        Long userId = intercomObject.getUserId();
        JSONObject dispatchUserLoginInfo =
            WebSubscribeManager.getInstance().getDispatchUserLoginInfo(SystemHelper.getCurrentUsername());
        if (dispatchUserLoginInfo == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "调度服务未登录");
        }
        JSONObject data = dispatchUserLoginInfo.getJSONObject("data");
        JSONArray userList = new JSONArray();
        JSONObject userJsonObj = new JSONObject();
        userJsonObj.put("userId", userId);
        userList.add(userJsonObj);
        JSONObject queryUserListResultJsonObj = talkCallUtil.queryUserList(data.getLong("custId"), userList, 1, 1);
        if (queryUserListResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询用户异常，请联系管理员");
        }
        Integer queryUserListListStatusCode = queryUserListResultJsonObj.getInteger("result");
        if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), queryUserListListStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT,
                DispatchErrorMessageEnum.getMessage(queryUserListListStatusCode));
        }
        JSONArray jsonArray = queryUserListResultJsonObj.getJSONObject("data").getJSONArray("records");
        if (jsonArray.size() == 0) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询用户无数据，请联系管理员");
        }
        //指定用户只会反会一条数据
        JSONObject records = queryUserListResultJsonObj.getJSONObject("data").getJSONArray("records").getJSONObject(0);
        Long defaultGroupId = records.getLong("defaultGroupId");
        // 判断是否在临时组中
        List<TempAssignmentInterlocutor> inTemporaryAssignmentInterlocutorInfoList =
            clusterDao.findInTemporaryAssignmentInterlocutorInfo(Collections.singletonList(userId));
        if (CollectionUtils.isNotEmpty(inTemporaryAssignmentInterlocutorInfoList)) {
            defaultGroupId = inTemporaryAssignmentInterlocutorInfoList.get(0).getIntercomGroupId();
        }
        Map<String, String> assignmentInfoMap = clusterDao.findAssignmentInfoMap(defaultGroupId);
        if (assignmentInfoMap != null) {
            OrganizationLdap org = userService.getOrgByUuid(assignmentInfoMap.get("groupId"));
            result.setAssignmentId(assignmentInfoMap.get("id"));
            result.setAssignmentName(assignmentInfoMap.get("name"));
            result.setAssignmentGroupId(assignmentInfoMap.get("groupId"));
            result.setAssignmentGroupName(org.getName());
        }
        result.setStatus(records.getLong("audioOnlineStatus"));
        result.setId(intercomObject.getMonitorId());
        result.setGroupId(intercomObject.getGroupId());
        result.setGroupName(intercomObject.getGroupName());
        result.setUserId(userId);
        result.setName(intercomObject.getMonitorName());
        return new JsonResultBean(result);
    }

    @Override
    public String getMassPoint() {
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        Map<String, Long> talkMap = new HashMap<>();
        //当前用户权限范围内是对讲对象，并且在已经在第三方平台生成成功的
        Set<String> talkIdSet = findTalkIdSet(talkMap);
        List<String> sortIconList = new ArrayList<>();
        //获得职位id和职位图标顺序关系
        Map<String, Integer> sortJobIcoMap = getSortJobIconMap(sortIconList);
        final List<String> responses = RedisHelper.hmget(HistoryRedisKeyEnum.MASSIVE_LOCATION.of(), talkIdSet);
        for (String response : responses) {
            JSONObject jsonObject = JSONObject.parseObject(response);
            JSONObject pointInfo = new JSONObject();
            pointInfo.put("jobIcon", sortJobIcoMap.get(jsonObject.getString("jobId")));
            pointInfo.put("monitorId", jsonObject.getString("monitorId"));
            pointInfo.put("longitude", jsonObject.getString("longitude"));
            pointInfo.put("latitude", jsonObject.getString("latitude"));
            pointInfo.put("userId", talkMap.get(jsonObject.getString("monitorId")));
            array.add(pointInfo);
        }
        result.put("sortIcon", sortIconList);
        result.put("data", array);
        return JSON.toJSONString(result);
    }

    private Map<String, Integer> getSortJobIconMap(List<String> sortIconList) {
        Map<String, Integer> sortJobIcoMap = new LinkedHashMap<>();
        List<Map<String, String>> jobMapList = jobManagementDao.findJobMapList();
        int num = 0;
        for (Map<String, String> map : jobMapList) {
            sortIconList.add(map.get("jobIconName"));
            sortJobIcoMap.put(map.get("id"), num++);
        }
        return sortJobIcoMap;
    }

    private Set<String> findTalkIdSet(Map<String, Long> talkMap) {
        final Set<String> monitorIdSet = this.loadMonitorIdsFromRedis();

        final Consumer<IntercomObjectInfo> action =
            intercomObject -> talkMap.put(intercomObject.getMonitorId(), intercomObject.getUserId());
        this.doActionOnIntercomMonitorsFromRedis(new ArrayList<>(monitorIdSet), action);
        return Sets.intersection(monitorIdSet, talkMap.keySet());
    }

    private Set<String> loadMonitorIdsFromRedis() {
        String username = SystemHelper.getCurrentUser().getUsername();
        Set<String> monitorIdSet = new HashSet<>();
        final Set<String> groupIds = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(username));
        if (CollectionUtils.isNotEmpty(groupIds)) {
            // 一次获取10个分组下的监控对象id不会严重阻塞Redis
            Lists.partition(new ArrayList<>(groupIds), 10).forEach(groups -> {
                final Set<RedisKey> keys =
                        groups.stream().map(RedisKeyEnum.GROUP_MONITOR::of).collect(Collectors.toSet());
                monitorIdSet.addAll(RedisHelper.batchGetSet(keys));
            });
        }
        return monitorIdSet;
    }

    /**
     * 根据分组id获得用户所属组织下的分组内的监控对象
     *
     * @param monitorIdList 监控对象列表
     * @param action        后续动作
     */
    private void doActionOnIntercomMonitorsFromRedis(List<String> monitorIdList, Consumer<IntercomObjectInfo> action) {
        Lists.partition(monitorIdList, 1000).forEach(monitorIds -> {
            final List<RedisKey> keys =
                    monitorIds.stream().map(HistoryRedisKeyEnum.INTERCOM_LIST::of).collect(Collectors.toList());
            final List<String> values = RedisHelper.batchGetString(keys);
            values.stream()
                    .map(o -> JSONObject.parseObject(o, IntercomObjectInfo.class))
                    .filter(o -> o != null && o.getUserId() != null)
                    .forEach(action);
        });
    }

    @Override
    public PointInfo getPointInfo(Long userId, HttpServletRequest request) {
        Map<String, Object> map = intercomObjectDao.getMonitorId(userId);
        PointInfo pointInfo = new PointInfo();
        String monitorId = map.get("monitorId").toString();
        String cacheLocationInfo = RedisHelper.hget(HistoryRedisKeyEnum.MASSIVE_LOCATION.of(), monitorId);
        if (!StringUtils.isNotBlank(cacheLocationInfo)) {
            return pointInfo;
        }
        JSONObject jsonObject = JSONObject.parseObject(cacheLocationInfo);
        String address =
            positionalService.getAddress(jsonObject.getString("longitude"), jsonObject.getString("latitude"));
        VehicleInfo vehicleInfo = alarmSettingDao.findPeopleOrVehicleOrThingById(monitorId);
        if (vehicleInfo != null) {
            pointInfo.setMonitorName(vehicleInfo.getBrand());
            pointInfo.setMonitorId(monitorId);
            pointInfo.setLongitude(jsonObject.getDouble("longitude"));
            pointInfo.setLatitude(jsonObject.getDouble("latitude"));
            pointInfo.setAddress(address);

            JobInfoData job = jobManagementDao.findJobByMonitorId(monitorId);
            if (job != null) {
                pointInfo.setJobId(job.getId());
                pointInfo.setJobIcon(job.getJobIconName());
            }
            pointInfo.setGpsTime(getGpsTimeDate(jsonObject.getString("gpsTime ")));
        }

        //是否拥有轨迹回放的权限
        String permissionUrls = (String) request.getSession().getAttribute("permissionUrls");
        List<String> permissionUrlList;
        if (StringUtils.isBlank(permissionUrls)) {
            pointInfo.setHasTrackPlaybackPermissions(false);
        } else {
            permissionUrlList = Arrays.asList(permissionUrls.split(","));
            pointInfo.setHasTrackPlaybackPermissions(permissionUrlList.contains(TRACK));
        }
        return pointInfo;
    }

    private String getGpsTimeDate(String time) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.format(DateUtils.parseDate("20" + time, "yyyyMMddHHmmss"));
        } catch (Exception e) {
            logger.error("时间解析异常", e);
        }
        return null;
    }

    @Override
    public JsonResultBean getPersonnelInfo(Long userId) {
        Map<String, Object> map = intercomObjectDao.getMonitorId(userId);
        if (null == map) {
            return new JsonResultBean(JsonResultBean.FAULT, "对讲对象不存在");
        }

        String monitorId = (String) map.get("monitorId");
        String monitorType = (String) map.get("monitorType");
        RedisKey detailKey = RedisKeyEnum.MONITOR_INFO.of(monitorId);
        JSONObject monitorObject = JSONObject.parseObject(RedisHelper.getString(detailKey));
        OrganizationLdap org = userService.getOrganizationByUuid(monitorObject.getString("groupId"));
        if (org != null) {
            monitorObject.put("groupName", org.getName());
        } else {
            monitorObject.put("groupName", "-");
        }

        String gender = monitorObject.getString("gender");
        if (StringUtils.isNotBlank(gender)) {
            monitorObject.put("gender", Objects.equals(gender, "1") ? "男" : Objects.equals(gender, "2") ? "女" : null);
        }
        String identity = monitorObject.getString("identity");
        if (StringUtils.isNotBlank(identity)) {
            //验证身份证有效性
            if (CheckIdentityUtil.checkIdentity(identity)) {
                monitorObject.put("age", CheckIdentityUtil.idNOToAge(identity));
            }
        }

        monitorObject.put("userId", userId);
        monitorObject.put("number", map.get("number"));
        monitorObject.put("monitorType", monitorType);
        return new JsonResultBean(monitorObject);
    }

    @Override
    public DispatchGroupInfo getAssignmentInfo(Long userId) {
        DispatchGroupInfo info = new DispatchGroupInfo();
        Cluster assignment = clusterDao.findAssignmentByIntercomGroupId(userId);
        info.setGroupId(assignment.getGroupId());
        info.setName(assignment.getName());
        info.setId(assignment.getId());
        OrganizationLdap organizationLdap = userService.getOrgByUuid(assignment.getGroupId());
        info.setGroupName(organizationLdap.getName());
        info.setOrganizationCode(organizationLdap.getOrganizationCode());
        info.setAddress(organizationLdap.getAddress());
        info.setContactName(organizationLdap.getContactName());
        info.setPhone(organizationLdap.getPhone());
        info.setDescription(organizationLdap.getDescription());
        info.setGroupCallNumber(assignment.getGroupCallNumber());
        info.setUserId(userId);
        return info;
    }

    @Override
    public String getLogMsg(Integer type, String id) {
        StringBuilder result = new StringBuilder();
        switch (type) {
            case INDIVIDUAL_CALL:
                result.append("个呼:").append(getMonitorName(id));
                break;
            case PHONE_CALL:
                result.append("电话:").append(getMonitorName(id));
                break;
            case BANNED_TALK:
                result.append("\"").append(getMonitorName(id)).append("\"").append("被禁言");
                break;
            case REMOVE_BANNED:
                result.append("取消").append("\"").append(getMonitorName(id)).append("\"").append("禁言");
                break;
            case POSITIONING:
                result.append("开启").append("\"").append(getMonitorName(id)).append("\"").append("定位");
                break;
            case GROUP_CALL:
                result.append("组呼:").append(clusterDao.findAssignmentName(id));
                break;
            case JOIN_GROUP:
                result.append("加入").append("\"").append(clusterDao.findAssignmentName(id)).append("\"");
                break;
            case QUIT_GROUP:
                result.append("退出").append("\"").append(clusterDao.findAssignmentName(id)).append("\"");
                break;
            case ROB_MAK:

                result.append("\"").append(clusterDao.findAssignmentName(id)).append("\"").append("抢麦成功");
                break;
            default:
                result.append("调度未知操作,操作类型:").append(type);
                break;
        }
        return result.toString();
    }

    private String getMonitorName(String id) {
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(id);
        if (bindDTO == null) {
            return "";
        }
        return bindDTO.getName();
    }

    @Override
    public boolean addNotificationRecord(String receiveId, String content, String ipAddress, Integer type) {
        UserBean userBean = userService.getUserDetails(SystemHelper.getCurrentUsername());
        boolean flag = intercomObjectDao
            .addNotificationRecord(UUID.randomUUID().toString(), userBean.getUuid(), receiveId, content, new Date());
        if (flag) {
            StringBuilder msg = new StringBuilder();
            if (type == IS_MONITOR_BOUNCED) {
                msg.append("向").append("\"").append(getMonitorName(receiveId)).append("\"").append("发送通知");
            }
            if (type == IS_ASSIGNMENT_BOUNCED) {
                String name = clusterDao.findAssignmentName(receiveId);
                msg.append("向").append("\"").append(name).append("\"").append("发送通知");
            }
            logSearchService.addLog(ipAddress, msg.toString(), "3", "监控调度");
        }
        return flag;
    }

    @Override
    public List<Map<String, Object>> notificationRecordList(String receiveId, Integer pageSize, Integer limitSize) {
        UserBean userBean = userService.getUserDetails(SystemHelper.getCurrentUsername());
        return PageHelper.startPage(pageSize, limitSize)
                .doSelectPage(() -> intercomObjectDao.notificationRecordList(userBean.getUuid(), receiveId));
    }

    @Override
    public List<JSONObject> findUserSettingFenceInfo() {
        List<JSONObject> result = new ArrayList<>();
        String userId = userService.getCurrentUserUuid();
        List<FenceInfo> fenceInfos = fenceManagementDao.findSettingFenceInfo(userId);
        if (CollectionUtils.isEmpty(fenceInfos)) {
            return result;
        }
        getFenceDetail(result, fenceInfos);
        return result;
    }

    private void getFenceDetail(List<JSONObject> result, List<FenceInfo> fenceInfos) {
        for (FenceInfo fenceInfo : fenceInfos) {
            JSONObject msg = new JSONObject();
            String type = fenceInfo.getType();
            String id = fenceInfo.getShape();
            if ("zw_m_marker".equals(type)) { // 查询标注详情
                Mark mark = markService.findMarkById(id);
                if (mark != null) {
                    msg.put("fenceType", type);
                    msg.put("fenceData", mark);
                }
            } else if ("zw_m_line".equals(type)) { // 查询线的详情
                List<LineContent> lineList = lineService.findLineContentsById(id);
                List<LineSpot> lineSpotList = lineService.findLineSpotByLid(id);
                List<LineSegmentInfo> lineSegmentInfos = lineService.findSegmentContentByLid(id);
                if (lineList != null && lineList.size() != 0) {
                    msg.put("fenceType", type);
                    msg.put("fenceData", lineList);
                    msg.put("lineSpot", lineSpotList);
                    msg.put("lineSegment", lineSegmentInfos);
                    // return new JsonResultBean(msg);
                }
            } else if ("zw_m_circle".equals(type)) { // 查询圆形的详情
                Circle circle = circleService.getCircleByID(id);
                if (circle != null) {
                    msg.put("fenceType", type);
                    msg.put("fenceData", circle);
                }
            } else if ("zw_m_polygon".equals(type)) { // 查询多边形的详情
                List<Polygon> polygonList = polygonService.getPolygonByID(id);
                if (polygonList != null && polygonList.size() != 0) {
                    msg.put("fenceType", type);
                    msg.put("fenceData", polygonList);
                }
            }
            result.add(msg);
        }
    }

    @Override
    public JsonResultBean commonHandleAlarms(HandleAlarms handleAlarms, String ip) throws Exception {
        String[] alarmTypeArr = handleAlarms.getAlarm().split(",");
        if (alarmTypeArr.length > 1) {
            return new JsonResultBean(JsonResultBean.FAULT, "只能处理单条报警！");
        }
        JSONObject result = new JSONObject();
        String monitorId = handleAlarms.getVehicleId();
        String alarmType = handleAlarms.getAlarm();
        String remark = handleAlarms.getRemark();
        String alarmStartTimeStr = handleAlarms.getStartTime();
        long alarmStartTimeL =
            StringUtils.isNotBlank(alarmStartTimeStr) ? DateUtil.getStringToLong(alarmStartTimeStr, null) : 0L;
        List<AlarmInfo> alarmList =
            alarmSearchService.getTheSameTimeAlarmInfo(monitorId, alarmType, alarmStartTimeStr, 1);
        if (CollectionUtils.isNotEmpty(alarmList) && Objects.equals(alarmList.get(0).getStatus(), 1)) {
            result.put("flag", 1);
            result.put("alarm", alarmList.get(0));
        } else {
            result.put("flag", 0);
            alarmSearchService.handleAlarmSingle(handleAlarms, alarmStartTimeL, alarmType, true);
            AlarmHandleForm alarmHandleForm = new AlarmHandleForm();
            alarmHandleForm.setHandleTime(System.currentTimeMillis() / 1000);
            alarmHandleForm.setPersonName(SystemHelper.getCurrentUsername());
            alarmHandleForm.setRemark(remark);
            result.put("alarm", alarmHandleForm);
        }
        alarmFactory.dealAlarm(handleAlarms);
        String monitorName = handleAlarms.getPlateNumber();
        String message = "监控对象 : " + monitorName + " 报警处理： " + remark;
        logSearchService.addLog(ip, message, "3", MONITORING_MODULE, monitorName, "");
        return new JsonResultBean(result);
    }
}
