package com.zw.talkback.service.baseinfo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.core.LogInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.event.ConfigUnbindVehicleEvent;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.MagicNumbers;
import com.zw.platform.util.OrganizationUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.FriendInfo;
import com.zw.talkback.domain.basicinfo.IntercomObjectInfo;
import com.zw.talkback.domain.basicinfo.form.FriendForm;
import com.zw.talkback.domain.basicinfo.form.InConfigInfoForm;
import com.zw.talkback.domain.intercom.ErrorMessageEnum;
import com.zw.talkback.domain.intercom.form.IntercomObjectForm;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.domain.lyxj.FirstCustomer;
import com.zw.talkback.repository.mysql.ClusterDao;
import com.zw.talkback.repository.mysql.InConfigDao;
import com.zw.talkback.repository.mysql.IntercomObjectDao;
import com.zw.talkback.repository.mysql.IntercomPersonnelDao;
import com.zw.talkback.service.baseinfo.IntercomCallNumberService;
import com.zw.talkback.service.baseinfo.IntercomObjectService;
import com.zw.talkback.util.IntercomRedisKeys;
import com.zw.talkback.util.TalkCallUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 对讲对象逻辑层实现类
 */
@Service
public class IntercomObjectServiceImpl implements IntercomObjectService {

    private Logger logger = LogManager.getLogger(IntercomObjectServiceImpl.class);
    @Autowired
    private IntercomObjectDao intercomObjectDao;

    @Autowired
    private InConfigDao inConfigDao;

    @Autowired
    private ClusterDao clusterDao;

    @Autowired
    private TalkCallUtil talkCallUtil;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private IntercomCallNumberService intercomCallNumberService;

    @Autowired
    private UserService userService;

    @Autowired
    private IntercomPersonnelDao intercomPersonnelDao;

    @Override
    public boolean addIntercomInfo(IntercomObjectInfo intercomObjectInfo) {
        return intercomObjectDao.addIntercomObject(intercomObjectInfo);
    }

    @Override
    public IntercomObjectInfo initIntercomObjectInfo(InConfigInfoForm form) throws Exception {

        //获取用户名
        String userName = form.getCreateDataUsername();
        if (StringUtils.isBlank(userName)) {
            userName = SystemHelper.getCurrentUsername();
        }

        //获取个呼号
        String number = intercomCallNumberService.updateAndReturnPersonCallNumber();

        IntercomObjectInfo intercom = new IntercomObjectInfo();
        intercom.setId(UUID.randomUUID().toString());
        intercom.setIntercomDeviceId(form.getIntercomDeviceId());
        intercom.setGroupId(form.getGroupid());
        intercom.setSimcardId(form.getSimID());
        intercom.setDevicePassword(form.getDevicePassword());
        intercom.setPriority(form.getPriority());
        FirstCustomer firstCustomerInfo = talkCallUtil.getFirstCustomerInfo();
        intercom.setCustomerCode(Objects.isNull(firstCustomerInfo) ? 1L : firstCustomerInfo.getCustId());
        intercom.setNumber(number);
        intercom.setOriginalModelId(form.getOriginalModelId());
        intercom.setTextEnable(form.getTextEnable());
        intercom.setImageEnable(form.getImageEnable());
        intercom.setAudioEnable(form.getAudioEnable());
        intercom.setStatus(IntercomObjectInfo.NOT_GENERATE_STATUS);
        intercom.setCreateDataUsername(userName);
        intercom.setCreateDataTime(form.getCreateDataTime());
        intercom.setConfigId(form.getId());
        intercom.setMonitorId(form.getBrandID());
        intercom.setMonitorName(form.getBrands());
        intercom.setMonitorType(form.getMonitorType());
        intercom.setSimcardNumber(form.getSims());
        intercom.setModelId(form.getModelId());
        intercom.setAssignmentId(form.getCitySelID().replaceAll(";", ","));
        intercom.setAssignmentName(form.getAssignmentName());
        intercom.setCurrentGroupNum(form.getCitySelID().split(";").length);
        intercom.setRecordEnable(0);
        OrganizationLdap ol = userService.getOrgByUuid(form.getGroupid());
        intercom.setGroupName(ol != null ? ol.getName() : form.getGroupName());
        OriginalModelInfo originalModelInfo = form.getOriginalModelInfo();
        if (originalModelInfo != null) {
            intercom.setIntercomModelName(originalModelInfo.getIntercomName());
            intercom.setMaxGroupNum(originalModelInfo.getMaxGroupNum());
            intercom.setMaxFriendNum(originalModelInfo.getMaxFriendNum());
        }
        return intercom;
    }

    @Override
    public void addIntercomObjectCache(IntercomObjectInfo intercomObjectInfo) {
        addIntercomRedisSort(intercomObjectInfo.getMonitorId());
        addIntercomFuzzyQuery(intercomObjectInfo);
        addIntercomDetails(intercomObjectInfo);
    }

    /**
     * 维护对讲对象的排序
     * @param monitorId monitorId
     */
    private void addIntercomRedisSort(String monitorId) {
        String monitorIdSortListJson = RedisHelper.getString(HistoryRedisKeyEnum.INTERCOM_LIST_SORT.of());
        List<String> monitorIdSortList;
        if (StringUtils.isEmpty(monitorIdSortListJson)) {
            monitorIdSortList = new ArrayList<>();
        } else {
            monitorIdSortList = JSON.parseArray(monitorIdSortListJson, String.class);
        }

        monitorIdSortList.remove(monitorId);
        // 最新添加的车辆增加的集合的第一个位置
        monitorIdSortList.add(0, monitorId);

        RedisHelper.setString(HistoryRedisKeyEnum.INTERCOM_LIST_SORT.of(), JSON.toJSONString(monitorIdSortList));
    }

    /**
     * 维护对讲对象模糊查询
     * @param intercomObjectInfo 对讲对象
     */
    private void addIntercomFuzzyQuery(IntercomObjectInfo intercomObjectInfo) {
        String field = IntercomRedisKeys
            .fuzzyField(intercomObjectInfo.getMonitorName(), intercomObjectInfo.getIntercomDeviceId(),
                intercomObjectInfo.getSimcardNumber());
        String value = IntercomRedisKeys.fuzzyValue(intercomObjectInfo.getMonitorId(), intercomObjectInfo.getId(),
            intercomObjectInfo.getSimcardId());
        RedisHelper.addToHash(HistoryRedisKeyEnum.INTERCOM_LIST_FUZZY.of(), field, value);
    }

    /**
     * 维护对讲对象的详细缓存
     * @param intercomObjectInfo intercomObjectInfo
     */
    private void addIntercomDetails(IntercomObjectInfo intercomObjectInfo) {
        RedisKey intercomKey = HistoryRedisKeyEnum.INTERCOM_LIST.of(intercomObjectInfo.getMonitorId());
        RedisHelper.setString(intercomKey, JSON.toJSONString(intercomObjectInfo));
    }

    @Override
    public JsonResultBean addIntercomInfoToIntercomPlatform(String configId, String ipAddress) throws Exception {
        //根据configId查询对讲对象详情
        IntercomObjectInfo intercomObjectInfo = intercomObjectDao.getIntercomInfoByConfigId(configId);

        JsonResultBean result = addOrUpdateIntercomObject(intercomObjectInfo);
        // 增加日志
        if (result.isSuccess() && StringUtils.isNotBlank(ipAddress)) {
            String message = String.format("生成对讲对象: %s", intercomObjectInfo.getMonitorName());
            logSearchService.addLog(ipAddress, message, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, "",
                intercomObjectInfo.getMonitorName(), null);
        } else {
            logger.error("新增对讲对象接口调用异常【{}】", result.getMsg());
        }
        return result;
    }

    private JsonResultBean addOrUpdateIntercomObject(IntercomObjectInfo intercomObjectInfo) throws Exception {
        Map<String, String> intercomParams = createAddUserRequestParams(intercomObjectInfo);
        Long userId = intercomObjectInfo.getUserId();
        boolean userIdIsNull = Objects.isNull(userId);
        JSONObject resultMap;
        int resultFlag;
        boolean addOrUpdateIntercomSuccessFlag = false;
        if (userIdIsNull) {
            // 1. 如果存在绑定关系, 但是未生成对讲对象(userId为空), 调用新增接口
            intercomParams.put("device.deviceId", intercomObjectInfo.getIntercomDeviceId());
            intercomParams.put("device.password", intercomObjectInfo.getDevicePassword());

            resultMap = talkCallUtil.addIntercomObject(intercomParams);
            resultFlag = resultMap.getIntValue("result");
            if (resultFlag == ErrorMessageEnum.SUCCESS_CODE) {
                intercomObjectInfo.setStatus(IntercomObjectInfo.GENERATE_SUCCESS_STATUS);
                Long newUserId = resultMap.getJSONObject("data").getLong("userId");
                if (Objects.nonNull(newUserId)) {
                    // 4、获取用户添加群组请求
                    addLargePointCache(intercomObjectInfo, newUserId);
                    JSONObject userGroupJson = getUserGroupList(intercomObjectInfo);
                    resultMap = talkCallUtil.addUserGroupList(userGroupJson, newUserId, intercomParams.get("custId"));
                    resultFlag = resultMap.getIntValue("result");
                    addOrUpdateIntercomSuccessFlag = (resultFlag == ErrorMessageEnum.SUCCESS_CODE);
                }
            }
        } else {
            // 5. 如果存在绑定关系, 已生成对讲对象(userId不为空), 调用修改接口;
            // 此处不修改群组信息, 在分组管理和信息配置修改群组时会修改群组;
            intercomParams.put("ms.id", String.valueOf(userId));
            resultMap = talkCallUtil.updateIntercomObject(intercomParams);
            resultFlag = resultMap.getIntValue("result");
            addOrUpdateIntercomSuccessFlag = (resultFlag == ErrorMessageEnum.SUCCESS_CODE);
            if (addOrUpdateIntercomSuccessFlag) {
                intercomObjectInfo.setStatus(IntercomObjectInfo.GENERATE_SUCCESS_STATUS);
                intercomObjectDao.updateIntercomInfo(intercomObjectInfo);
                updateIntercomICache(intercomObjectInfo, userId);
            }
        }

        // 6.处理返回结果
        if (addOrUpdateIntercomSuccessFlag) {
            return new JsonResultBean(JsonResultBean.SUCCESS, "生成对讲对象成功");
        }

        intercomObjectInfo.setStatus(IntercomObjectForm.GENERATE_FAILED_STATUS);
        intercomObjectDao.updateIntercomInfo(intercomObjectInfo);
        String errorMsg;
        if (1003 == resultFlag) {
            errorMsg = "监控对象名称已存在，生成失败";
        } else if (1015 == resultFlag) {
            errorMsg = "个呼号码已被使用";
        } else {
            errorMsg = ErrorMessageEnum.getMessage(resultFlag);
            errorMsg = StringUtils.isBlank(errorMsg) ? resultMap.getString("message") : errorMsg;
        }
        return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
    }

    /**
     * 获取用户添加群组请求消息体
     * @param intercomObjectInfo intercomObjectInfo
     * @return JSONObject
     */
    private JSONObject getUserGroupList(IntercomObjectInfo intercomObjectInfo) throws Exception {
        JSONObject requestParam = new JSONObject();
        Integer knobNum = intercomObjectInfo.getKnobNum();
        int withKnob = Objects.nonNull(knobNum) && knobNum > 0 ? 1 : 0;
        String monitorId = intercomObjectInfo.getMonitorId();
        List<Cluster> assignments = clusterDao.getTackbackAssignsByMonitorId(monitorId);
        List<Map<String, Object>> groupList = new ArrayList<>();
        assignments.forEach(assignment -> {
            Map<String, Object> param = new HashMap<>(8);
            param.put("groupId", assignment.getIntercomGroupId());
            if (withKnob == MagicNumbers.INT_ONE) {
                param.put("knobNo", assignment.getKnobNo());
            }
            groupList.add(param);
        });
        requestParam.put("withKnob", withKnob);
        requestParam.put("groupList", groupList);
        return requestParam;
    }

    private void addLargePointCache(IntercomObjectInfo intercomObjectInfo, Long newUserId) {
        intercomObjectInfo.setUserId(newUserId);
        // 此处无论新增分组失败还是成功, 都先把userId更新到对讲终端表中
        boolean userFlag = intercomObjectDao.updateIntercomInfo(intercomObjectInfo);

        //更新缓存
        if (userFlag) {
            updateIntercomICache(intercomObjectInfo, newUserId);
        }
    }

    private void updateIntercomICache(IntercomObjectInfo intercomObjectInfo, Long newUserId) {
        String monitorId = intercomObjectInfo.getMonitorId();
        RedisKey redisKey = HistoryRedisKeyEnum.INTERCOM_LIST.of(monitorId);
        String jsonStr = RedisHelper.getString(redisKey);
        IntercomObjectInfo intercomObject = JSONObject.parseObject(jsonStr, IntercomObjectInfo.class);
        if (intercomObject != null) {
            intercomObject.setUserId(newUserId);
            intercomObject.setStatus(1);
            RedisHelper.setString(redisKey, JSON.toJSONString(intercomObject));
            RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(monitorId), "userId", newUserId.toString());
        } else {
            RedisHelper.setString(redisKey, JSON.toJSONString(intercomObjectInfo));
        }
    }

    /**
     * 必要参数:
     * 1. priority 2. modelId 3. intercomDeviceId 3.devicePassword 4. monitorName 5. number
     * 6.originalModelId(原始机型ID) 7.recordEnable 8.simcardNumber
     * @param objectInfo objectInfo
     * @return params
     */
    public static Map<String, String> createAddUserRequestParams(IntercomObjectInfo objectInfo) {
        // 获取
        Map<String, String> intercomParams = new HashMap<>(32);

        intercomParams.put("custId", String.valueOf(objectInfo.getCustomerCode()));
        intercomParams.put("parentId", "-1");
        intercomParams.put("ms.name", objectInfo.getMonitorName());
        intercomParams.put("ms.priority", String.valueOf(objectInfo.getPriority()));
        intercomParams.put("ms.number", objectInfo.getNumber());
        // -----根据机型能力填写 START 1:支持 0:不支持----
        // 是否支持视频会议
        intercomParams.put("ms.videoConferenceEnable", String.valueOf(objectInfo.getVideoConferenceEnable()));
        // 是否支持音频会议
        intercomParams.put("ms.audioConferenceEnable", String.valueOf(objectInfo.getAudioConferenceEnable()));
        // 是否支持视频会话
        intercomParams.put("ms.videoCallEnable", String.valueOf(objectInfo.getVideoCallEnable()));
        // 是否支持文本消息
        intercomParams.put("ms.sendTextEnable", String.valueOf(objectInfo.getSendTextEnable()));
        // 是否支持图片消息
        intercomParams.put("ms.sendImageEnable", String.valueOf(objectInfo.getSendImageEnable()));
        // 是否支持离线语音消息
        intercomParams.put("ms.sendAudioEnable", String.valueOf(objectInfo.getSendAudioEnable()));
        // 是否支临时组功能
        intercomParams.put("ms.tempGroupEnable", String.valueOf(objectInfo.getTempGroupEnable()));
        // 是否支持实时视频功能
        intercomParams.put("ms.videoFuncEnable", String.valueOf(objectInfo.getVideoFuncEnable()));
        // 备注
        intercomParams.put("ms.comments", objectInfo.getComments());
        // -----根据机型能力填写 END----
        // APP登陆开关, 此处没有App,因此为空
        intercomParams.put("ms.appEnable", "0");
        intercomParams.put("ms.appPassword", "000000");
        // 是否开启录音
        intercomParams.put("ms.recordEnable", String.valueOf(objectInfo.getRecordEnable()));
        // 用户电话
        intercomParams.put("ms.phoneNumber", objectInfo.getSimcardNumber());
        return intercomParams;
    }

    @Override
    public JsonResultBean addIntercomInfosToIntercomPlatform(String configIds, String ipAddress) throws Exception {
        // 根据对讲对象ID, 查询数据
        List<String> configIdList = Arrays.asList(configIds.split(","));
        List<IntercomObjectInfo> intercomObjectInfos = intercomObjectDao.findIntercomByConfigIds(configIdList);
        StringBuilder errorMessage = new StringBuilder();
        StringBuilder message = new StringBuilder();
        if (CollectionUtils.isNotEmpty(intercomObjectInfos)) {
            for (IntercomObjectInfo objectInfo : intercomObjectInfos) {
                JsonResultBean resultBean = addOrUpdateIntercomObject(objectInfo);
                String intercomDeviceId = objectInfo.getIntercomDeviceId();
                // 增加日志
                if (!resultBean.isSuccess()) {
                    objectInfo.setStatus(IntercomObjectForm.GENERATE_FAILED_STATUS);
                    errorMessage.append("对讲终端：").append(intercomDeviceId).append(", 异常信息: ").append(resultBean.getMsg())
                        .append("<br/>");
                }
                message.append("生成对讲对象:").append(objectInfo.getMonitorName()).append("<br/>");
            }
        }
        String errorMessageStr = errorMessage.toString();
        if (errorMessageStr.length() > 0) {
            errorMessageStr += "生成对讲对象失败!";
            return new JsonResultBean(JsonResultBean.SUCCESS, errorMessageStr);
        }
        logSearchService
            .addMoreLog(ipAddress, message.toString(), LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, "-", "对讲信息录入：批量生成对讲对象");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public IntercomObjectInfo getIntercomBindInfoByConfigId(String configId) throws Exception {
        IntercomObjectInfo intercomObject = intercomObjectDao.getIntercomInfoByConfigId(configId);
        if (Objects.nonNull(intercomObject)) {
            String intercomDeviceId = intercomObject.getIntercomDeviceId();
            // 截取对讲终端号后7位, 用于前端展示
            intercomDeviceId = intercomDeviceId.substring(MagicNumbers.INT_FIVE);
            intercomObject.setIntercomDeviceId(intercomDeviceId);

            //获取对讲对象的群组信息
            List<Cluster> clusters = clusterDao.getTackbackAssignsByMonitorId(intercomObject.getMonitorId());
            if (!clusters.isEmpty()) {
                Set<String> assignmentIds = new HashSet<>();
                Set<String> assignmentNames = new HashSet<>();
                clusters.forEach(cluster -> {
                    assignmentIds.add(cluster.getAssignmentId());
                    assignmentNames.add(cluster.getName());
                });
                intercomObject.setAssignmentName(StringUtils.join(assignmentNames, ","));
                intercomObject.setAssignmentId(StringUtils.join(assignmentIds, ","));
            }
        }

        //获取分组信息
        return intercomObject;
    }

    @Override
    public List<FriendInfo> findFriendList(Long userId) {
        JSONObject friendObj = talkCallUtil.findFriendList(userId);
        if (friendObj == null) {
            return Lists.newArrayList();
        }
        Integer result = friendObj.getInteger("result");
        if (result == 0) {
            JSONObject resultData = friendObj.getJSONObject("data");
            String resultDataString = resultData.getString("records");
            List<FriendInfo> resultList = JSON.parseArray(resultDataString, FriendInfo.class);
            if (CollectionUtils.isNotEmpty(resultList)) {
                // 查询当前对讲对象设置的好友
                Map<Long, FriendForm> friendMap = getFriendMap(userId);
                boolean friendEmpty = MapUtils.isEmpty(friendMap);
                for (FriendInfo info : resultList) {
                    Long friendUserId = info.getUserId();
                    info.setFriendId(friendUserId);
                    info.setName(info.getUserName());
                    FriendForm friend = friendMap.get(friendUserId);
                    if (friendEmpty || friend.getType() == FriendInfo.TYPE_INTERCOM_OBJECT) {
                        if ("0".equals(friend.getMonitorType())) {
                            info.setIconSkin("vehicleSkin");
                        } else if ("2".equals(friend.getMonitorType())) {
                            info.setIconSkin("thingSkin");
                        } else {
                            info.setIconSkin("peopleSkin");
                        }
                        info.setType(FriendInfo.TYPE_INTERCOM_OBJECT);
                    } else {
                        info.setIconSkin("userSkin");
                        info.setType(FriendInfo.TYPE_DISPATCHER);
                    }
                }
                return resultList;
            }
        }
        return new ArrayList<>();
    }

    private Map<Long, FriendForm> getFriendMap(Long userId) {
        List<FriendForm> friedList = intercomObjectDao.findFriedList(userId);
        Map<Long, FriendForm> friendMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(friedList)) {
            for (FriendForm friendForm : friedList) {
                friendMap.put(friendForm.getFriendId(), friendForm);
            }
        }
        return friendMap;
    }

    @Override
    public JSONArray findIntercomObjectTree(String type, String queryParam) throws Exception {
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(",");
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }
        if (userOrgListId.isEmpty()) {
            return result;
        }

        // 查询当前用户权限分组
        List<Assignment> assignmentList = intercomObjectDao.findUserAssignment(uuid, userOrgListId);
        List<String> assignIdList = assignmentService.putAssignmentTree(assignmentList, result, type, true);

        orgs = new ArrayList<>(OrganizationUtil.filterOrgList(orgs, assignmentList));
        // 组装组织树结构
        result.addAll(com.zw.talkback.util.JsonUtil.getGroupTree(orgs, type, false));

        if (assignIdList == null || assignIdList.isEmpty()) {
            return result;
        }

        List<IntercomObjectInfo> intercomObjectInfoList = new ArrayList<>();
        if (StringUtils.isNotEmpty(queryParam)) {
            intercomObjectInfoList = intercomObjectDao.findIntercomObjectByAssignmentIds(assignIdList, queryParam);
        } else if (CollectionUtils.isNotEmpty(assignmentList)) {
            // 第一个分组ID, // 查询第一个分组中的对讲对象
            List<String> singleAssignList = new ArrayList<>();
            singleAssignList.add(String.valueOf(result.get(0)));
            intercomObjectInfoList = intercomObjectDao.findIntercomObjectByAssignmentIds(singleAssignList, queryParam);
        }

        if (CollectionUtils.isNotEmpty(intercomObjectInfoList)) {
            for (IntercomObjectInfo intercomObjectInfo : intercomObjectInfoList) {
                JSONObject intercomObj = new JSONObject();
                // 组装将空对象数据
                putMonitorTree(intercomObjectInfo, intercomObj);
                result.add(intercomObj);
            }
        }

        return result;
    }

    private void putMonitorTree(IntercomObjectInfo intercom, JSONObject intercomObj) {
        getBaseTreeData(intercom, intercomObj);
        intercomObj.put("deviceNumber", intercom.getIntercomDeviceId());
        intercomObj.put("simcardNumber", intercom.getSimcardNumber());
        intercomObj.put("assignName", intercom.getAssignmentName());
        intercomObj.put("open", true);
    }

    private void getBaseTreeData(IntercomObjectInfo intercom, JSONObject intercomObj) {
        intercomObj.put("id", intercom.getId());
        intercomObj.put("pId", intercom.getAssignmentId());
        intercomObj.put("name", intercom.getMonitorName());
        if ("0".equals(intercom.getMonitorType())) {
            intercomObj.put("iconSkin", "vehicleSkin");
            intercomObj.put("type", "vehicle");
        } else if ("1".equals(intercom.getMonitorType())) {
            intercomObj.put("iconSkin", "peopleSkin");
            intercomObj.put("type", "people");
        } else if ("2".equals(intercom.getMonitorType())) {
            intercomObj.put("iconSkin", "thingSkin");
            intercomObj.put("type", "thing");
        }
        intercomObj.put("monitorType", intercom.getMonitorType());
        intercomObj.put("userId", intercom.getUserId());
    }

    @Override
    public JSONArray getIntercomObjectByAssignmentId(String assignmentId) {
        JSONArray result = new JSONArray();
        // 第一个分组ID, // 查询第一个分组中的对讲对象
        List<String> singleAssignList = new ArrayList<>();
        singleAssignList.add(assignmentId);
        List<IntercomObjectInfo> intercomObjectInfoList =
            intercomObjectDao.findIntercomObjectByAssignmentIds(singleAssignList, "");
        if (CollectionUtils.isNotEmpty(intercomObjectInfoList)) {
            for (IntercomObjectInfo intercomObjectInfo : intercomObjectInfoList) {
                JSONObject intercomObj = new JSONObject();
                // 组装将空对象数据
                getBaseTreeData(intercomObjectInfo, intercomObj);
                result.add(intercomObj);
            }
        }
        return result;
    }

    /**
     * 获取当前组织及下级组织下的树
     * @return
     */
    @Override
    public String getGroupAndUserTree(String queryParam) {
        // 获取当前用户所在组织及下级组织
        String orgId = userService.getOrgIdByUser();
        List<OrganizationLdap> organizationLdaps = userService.getOrgChild(orgId); // 所有组织 当前组织

        // 组织下的调度员
        List<UserBean> userBeans = new ArrayList<>();
        List<UserBean> userList;
        for (OrganizationLdap organizationLdap : organizationLdaps) {
            if (StringUtils.isNotEmpty(queryParam)) {
                userList = userService.findFuzzyUserByOrgId(organizationLdap.getId().toString(), queryParam);
            } else {
                userList = userService.listUserByOrgId(organizationLdap.getId().toString());
            }
            if (CollectionUtils.isNotEmpty(userList)) {
                for (UserBean user : userList) {
                    String dispatcherId = user.getDispatcherId();
                    if (StringUtils.isEmpty(dispatcherId)) {
                        continue;
                    }
                    user.setGroupId(organizationLdap.getId().toString());
                    userBeans.add(user);
                }
            }
        }

        userBeans.sort(Comparator.comparing(UserBean::getCreateTimestamp).reversed());

        // 组装监控人员树, 有监控人员的企业才展示出来
        Set<String> hasUseGroups = new HashSet<>(16);
        JSONArray result = new JSONArray();
        if (!userBeans.isEmpty()) {
            for (UserBean user : userBeans) {
                String urId = user.getId().toString();
                String userUuid = user.getUuid();
                // 获取组织id(根据用户id得到用户所在部门)
                String userPid = userService.getOrgIdByUserId(urId);

                JSONObject userObj = new JSONObject();
                userObj.put("id", urId);
                userObj.put("pId", userPid);
                userObj.put("name", user.getUsername());

                userObj.put("type", "user");
                userObj.put("uuid", userUuid);
                userObj.put("iconSkin", "userSkin");
                userObj.put("userId", user.getDispatcherId());
                result.add(userObj);
                hasUseGroups.add(userPid);
            }
        }
        // 组装组织树
        result.addAll(getGroupTree(organizationLdaps, "multiple", false, hasUseGroups));
        return result.toJSONString();
    }

    /**
     * 组装组织树结构
     * @author wangying
     */
    public static JSONArray getGroupTree(List<OrganizationLdap> orgs, String type, Boolean isNoCheck,
        Set<String> hasUseGroups) {
        JSONArray array = new JSONArray();
        // 组装组织树
        if (orgs != null && !orgs.isEmpty()) {
            for (OrganizationLdap group : orgs) {
                String cid = group.getCid();
                if (!hasUseGroups.contains(cid)) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                // 组装group数结构
                obj.put("id", cid);
                obj.put("pId", group.getPid());
                obj.put("name", group.getName());
                obj.put("iconSkin", "groupSkin");
                obj.put("type", "group");
                obj.put("open", false);
                obj.put("uuid", group.getUuid());
                if ("single".equals(type)) { // 根节点是否可选
                    obj.put("nocheck", true);
                }
                if ("ou=organization".equals(cid)) {
                    obj.put("pId", "0");
                }
                // 节点是否可选
                obj.put("nocheck", isNoCheck);
                array.add(obj);
            }
        }
        return array;
    }

    @Override
    public JsonResultBean addFriends(String userForm, String monitorName, Long userId, String ipAddress) {
        String username = SystemHelper.getCurrentUsername();
        List<FriendForm> friends = JSON.parseArray(userForm, FriendForm.class);

        // 1.删除旧的好友
        List<FriendForm> oldFriedList = intercomObjectDao.findFriedList(userId);

        if (CollectionUtils.isNotEmpty(oldFriedList)) {
            List<Long> oldFriedIdList = oldFriedList.stream().map(FriendForm::getFriendId).collect(Collectors.toList());
            JSONObject resultBody = talkCallUtil.deleteFriends(oldFriedIdList, userId);
            if (resultBody.getInteger("result") != 0) {
                return new JsonResultBean(JsonResultBean.FAULT, resultBody.getString("message"));
            }

            // 删除当前对讲对象的好友
            intercomObjectDao.deleteIntercomFriendByUserId(userId);
        }

        // 2.调用三方平台新增
        List<Map<String, Object>> friendList = new ArrayList<>();

        friends.forEach(x -> {
            Map<String, Object> friend = new HashMap<>(6);
            friend.put("userId", x.getFriendId());
            friendList.add(friend);
        });
        JSONObject resultBody = talkCallUtil.addFriends(friendList, userId);
        if (resultBody.getInteger("result") != 0) {
            return new JsonResultBean(JsonResultBean.FAULT, resultBody.getString("message"));
        }

        // 3.然后新增
        boolean flag = intercomObjectDao.addFriendForm(friends, username);
        if (flag) {
            if (!CollectionUtils.isEmpty(friends)) {
                String message = String.format("给监控对象: %s 设置好友", monitorName);
                logSearchService
                    .addLog(ipAddress, message, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, "", monitorName, null);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean updateRecordStatus(Integer recordEnable, String configId, String ipAddress) {
        IntercomObjectInfo intercomObject = intercomObjectDao.getIntercomInfoByConfigId(configId);
        if (intercomObject == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "该对讲对象不存在");
        }
        if (intercomObject.getUserId() == null || intercomObject.getUserId() <= 0) {
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象未生成");
        }
        // 调用第三方接口
        JSONObject resultBody = talkCallUtil.updateRecordStatus(recordEnable, intercomObject.getUserId());
        if (resultBody.getInteger("result") != 0) {
            return new JsonResultBean(JsonResultBean.FAULT, resultBody.getString("message"));
        }
        intercomObject.setRecordEnable(recordEnable);
        boolean isSuccess = intercomObjectDao.updateRecordStatus(intercomObject);

        //更新缓存
        RedisKey redisKey = HistoryRedisKeyEnum.INTERCOM_LIST.of(intercomObject.getMonitorId());
        IntercomObjectInfo oldIntercomObject =
                JSONObject.parseObject(RedisHelper.getString(redisKey), IntercomObjectInfo.class);
        if (oldIntercomObject != null) {
            oldIntercomObject.setRecordEnable(recordEnable);
            RedisHelper.setString(redisKey, JSON.toJSONString(oldIntercomObject));
        }

        if (isSuccess) {
            String message = String.format("%s【%s】录音", IntercomObjectForm.getRecordEnableFormat(recordEnable),
                intercomObject.getMonitorName());
            logSearchService.addLog(ipAddress, message, LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public JsonResultBean deleteIntercomObject(String configIds, String ipAddress, List<String> monitorIdList) {

        List<String> monitorIds = new ArrayList<>();
        if (StringUtils.isNotBlank(configIds)) {
            List<String> configIdList = Arrays.asList(configIds.split(","));
            List<IntercomObjectInfo> intercomObjectInfos = intercomObjectDao.getConfigByConfigIds(configIdList);
            monitorIds =
                intercomObjectInfos.stream().map(IntercomObjectInfo::getMonitorId).collect(Collectors.toList());
        } else if (monitorIdList != null && !monitorIdList.isEmpty()) {
            monitorIds = monitorIdList;
        }
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "对讲对象不存在");
        }
        List<IntercomObjectInfo> intercomObjectList = getIntercoms(monitorIds);
        if (CollectionUtils.isEmpty(intercomObjectList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "对讲对象不存在");
        }

        List<String> successMonitorNames = deleteIntercomObjects(intercomObjectList, true);

        if (CollectionUtils.isNotEmpty(successMonitorNames) && StringUtils.isNotBlank(ipAddress)) {
            StringBuilder builder = new StringBuilder();
            for (IntercomObjectInfo intercomObject : intercomObjectList) {
                if (successMonitorNames.contains(intercomObject.getMonitorName())) {
                    builder.append("解除对讲绑定关系：监控对象(").append(intercomObject.getMonitorName()).append(")/群组(")
                        .append(intercomObject.getAssignmentName()).append(")/对讲设备标识(")
                        .append(intercomObject.getIntercomDeviceId()).append(")/SIM卡(")
                        .append(intercomObject.getSimcardNumber()).append(")<br/>");
                }
            }
            String monitoringOperation;
            String monitorName = "-";
            if (successMonitorNames.size() > 1) {
                monitoringOperation = "对讲信息录入：批量解除对讲绑定关系";
            } else {
                monitorName = successMonitorNames.get(0);
                monitoringOperation = "对讲信息录入：解除对讲绑定关系";
            }
            logSearchService
                .addMoreLog(ipAddress, builder.toString(), LogInfo.LOG_SOURCE_PLATFORM_OPERATOR, monitorName,
                    monitoringOperation);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public List<String> deleteIntercomObjects(List<IntercomObjectInfo> intercomObjectInfos, boolean isUpdateConfig) {
        List<String> successMonitorNames = new ArrayList<>();
        List<String> intercomFuzzyKeys = new ArrayList<>();
        List<String> intercomInfoIds = new ArrayList<>();
        List<String> monitorIds = new ArrayList<>();
        List<String> peopleIds = new ArrayList<>();
        List<String> configIdList = new ArrayList<>();

        deleteUserToPlatform(intercomObjectInfos, successMonitorNames, intercomFuzzyKeys, monitorIds, intercomInfoIds,
            peopleIds, configIdList);

        //删除对讲对象相关缓存
        deleteRedisCache(intercomFuzzyKeys, monitorIds, peopleIds);
        if (!configIdList.isEmpty() && isUpdateConfig) {
            //删除绑定配置表里的对讲对象ID
            inConfigDao.updateConfigListBatch(configIdList);
        }
        if (!monitorIds.isEmpty() && isUpdateConfig) {
            //删除分组中的旋钮数
            clusterDao.deleteKnobsByMonitorId(monitorIds);
        }
        deleteMysql(intercomInfoIds, peopleIds);
        return successMonitorNames;
    }

    private void deleteMysql(List<String> intercomInfoIds, List<String> peopleIds) {
        if (!intercomInfoIds.isEmpty()) {
            //删除对讲对象
            intercomObjectDao.deleteIntercomInfoByIds(intercomInfoIds);
        }
        if (!peopleIds.isEmpty()) {
            intercomPersonnelDao.updatePeopleincumbency(peopleIds);
        }

    }

    private void deleteRedisCache(List<String> intercomFuzzyKeys, List<String> monitorIds, List<String> peopleIds) {
        // 1.批量删除模糊查询
        if (CollectionUtils.isNotEmpty(intercomFuzzyKeys)) {
            RedisHelper.hdel(HistoryRedisKeyEnum.INTERCOM_LIST_FUZZY.of(), intercomFuzzyKeys);
        }

        if (monitorIds.isEmpty()) {
            return;
        }
        // 2.更新排序
        final RedisKey sortKey = HistoryRedisKeyEnum.INTERCOM_LIST_SORT.of();
        String monitorSortListStr = RedisHelper.getString(sortKey);
        List<String> monitorSortList = JSON.parseArray(monitorSortListStr, String.class);
        if (CollectionUtils.isNotEmpty(monitorSortList)) {
            final Set<String> monitorSortSet = new HashSet<>(monitorSortList);
            monitorSortSet.removeAll(monitorIds);
            RedisHelper.setString(sortKey, JSON.toJSONString(monitorSortSet));
        }

        // 3.批量删除监控对象详情
        final List<RedisKey> infoKeys =
                monitorIds.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toList());
        RedisHelper.hdel(infoKeys, Collections.singleton("userId"));
        RedisHelper.batchAddToHash(infoKeys, ImmutableMap.of("isIncumbency", "1"));
    }

    private String deleteUserToPlatform(List<IntercomObjectInfo> intercomObjectInfos, List<String> successMonitorNames,
        List<String> intercomFuzzyKeys, List<String> monitorIds, List<String> intercomInfoIds, List<String> peopleIds,
        List<String> configIdList) {
        StringBuilder errorBuilder = new StringBuilder();
        Set<String> numbers = new HashSet<>();
        for (IntercomObjectInfo intercomObject : intercomObjectInfos) {
            Long userId = intercomObject.getUserId();
            String monitorName = intercomObject.getMonitorName();
            if (Objects.nonNull(userId)) {
                JSONObject deleteObject = new JSONObject();
                deleteObject.put("userId", userId);
                JSONObject result = talkCallUtil.deleteIntercomObject(deleteObject);
                int deleteResult = result.getIntValue("result");
                if (deleteResult > ErrorMessageEnum.SUCCESS_CODE) {
                    // 删除失败
                    errorBuilder.append("对讲对象: ").append(monitorName).append(" 删除异常, 异常信息: ")
                        .append(result.getString("message")).append("<br/>");
                    continue;
                }
            }
            configIdList.add(intercomObject.getConfigId());
            numbers.add(intercomObject.getNumber());

            //删除成功的对讲对象
            successMonitorNames.add(monitorName);
            //对应对象的模糊查询key，用于后续批量删除
            intercomFuzzyKeys.add(IntercomRedisKeys
                .fuzzyField(monitorName, intercomObject.getIntercomDeviceId(), intercomObject.getSimcardNumber()));
            //删除监控对象ID
            monitorIds.add(intercomObject.getMonitorId());
            intercomInfoIds.add(intercomObject.getId());

            if ("1".equals(intercomObject.getMonitorType())) {
                peopleIds.add(intercomObject.getMonitorId());
            }
        }
        //回收个呼号码
        intercomCallNumberService.updateAndRecyclePersonCallNumberBatch(numbers);
        return errorBuilder.toString();
    }

    /**
     * 监听定位绑定关系解绑
     * @param event 绑定关系解绑事件
     */
    @EventListener
    public void listenConfigUnbindEvent(ConfigUnbindVehicleEvent event) {
        if (StringUtils.isBlank(event.getVehicleId())) {
            return;
        }
        List<String> monitorIds = Arrays.asList(event.getVehicleId().split(","));
        try {
            List<IntercomObjectInfo> intercomObjectInfos = getIntercoms(monitorIds);
            //若存在对讲对象进行解绑
            if (intercomObjectInfos.size() > 0) {
                deleteIntercomObjects(intercomObjectInfos, false);
            }
        } catch (Exception e) {
            logger.error("删除对讲对象失败", e);
        }
    }

    private List<IntercomObjectInfo> getIntercoms(List<String> monitorIds) {
        List<IntercomObjectInfo> intercomObjectInfos = new ArrayList<>();
        Lists.partition(monitorIds, 1000).forEach(monitorIdPart -> {
            final List<RedisKey> keys =
                    monitorIdPart.stream().map(HistoryRedisKeyEnum.INTERCOM_LIST::of).collect(Collectors.toList());
            final List<String> values = RedisHelper.batchGetString(keys);
            values.stream()
                    .map(o -> JSONObject.parseObject(o, IntercomObjectInfo.class))
                    .filter(o -> o != null && o.getUserId() != null)
                    .forEach(intercomObjectInfos::add);
        });
        return intercomObjectInfos;
    }
}
