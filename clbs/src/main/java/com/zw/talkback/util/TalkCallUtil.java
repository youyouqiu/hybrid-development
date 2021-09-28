package com.zw.talkback.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.PersonnelForm;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.form.ClusterForm;
import com.zw.talkback.domain.intercom.form.IntercomObjectForm;
import com.zw.talkback.domain.intercom.info.IntercomIotUserInfo;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.domain.lyxj.FirstCustomer;
import com.zw.talkback.domain.lyxj.tsm3.PageInfo;
import com.zw.talkback.domain.lyxj.tsm3.Tsm3Result;
import com.zw.talkback.repository.mysql.OriginalModelDao;
import com.zw.talkback.util.common.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 对讲平台专用的httphe客户端工具类，所有关于对讲平台的http请求都放到这里面来
 * @author admin
 */
@Component
public class TalkCallUtil {

    private static final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(2,
        new BasicThreadFactory.Builder().namingPattern("login-duijiang-pool").daemon(true).build());

    private static final Logger logger = LoggerFactory.getLogger(TalkCallUtil.class);

    /**
     * 对讲平台地址
     */
    @Value("${task.tsm3Url:http://118.190.3.121/TSM3/}")
    private String tsm3Url;

    /**
     * 对讲平台地址
     */
    @Value("${task.iotpmUrl:http://118.190.3.121/IOTPM/}")
    private String iotpmUrl;

    /**
     * 对讲平台代理商账号
     */
    @Value("${task.proxy.name:bjzw}")
    private String proxyName;
    /**
     * 对讲平台代理密码
     */
    @Value("${task.proxy.password:123456}")
    private String proxyPassword;

    /**
     * 一级客户账号
     */
    @Value("${task.first.customer.name:ZWKJ}")
    private String firstCustomerName;
    /**
     * 一级客户密码
     */
    @Value("${task.first.customer.password:ZWLBS16888}")
    private String firstCustomerPassword;

    @Value("${module.talk.enable:false}")
    private boolean enabled;

    private static final String VERSION_AND_PID = "?version=1";

    private static FirstCustomer firstCustomer = new FirstCustomer();

    /**
     * 定时登陆对讲平台
     */
    private String loginPidUrl;
    private String addFirstCustomerUrl;
    private String loginUrl;
    private String addAssignmentUrl;
    private String updateAssignmentUrl;
    private String delAssignmentUrl;
    private String updatePeopleInfoUrl;
    private String setRecordStatusUrl;

    private String getDeviceTypeUrl;

    /**
     * 添加群组
     */
    private String addGroupUrl;
    /**
     * 添加群组成员
     */
    private String addGroupMemberListUrl;
    /**
     * 删除群组
     */
    private String deleteGroupUrl;

    /**
     * 查询群组成员
     */
    private String queryGroupMemberListUrl;

    /**
     * 调度服务登录
     */
    private String dispatchLoginInUrl;

    /**
     * 调度服务登录
     */
    private String dispatchLoginOutUrl;

    /**
     * 查询在对讲组内用户
     */
    private String queryInGroupMemberListUrl;

    /**
     * 查询用户
     */
    private String queryUserListUrl;

    /**
     * 清理临时组
     */
    private String clearTempGroupListUrl;

    /**
     * 删除群组成员
     */
    private String deleteGroupMemberListUrl;

    /**
     * 添加云调度员
     */
    private String addIotUserUrl;

    /**
     * 查询云调度员
     */
    private String queryIotUserListUrl;

    /**
     * 删除云调度员
     */
    private String deleteIotUserUrl;

    @Autowired
    private OriginalModelDao originalModelDao;

    @PostConstruct
    public void initBaseData() {
        addFirstCustomerUrl = tsm3Url + "firstClassCustomer_add.action?version=1";
        loginUrl = tsm3Url + "standardApi_login.action?version=1";
        getDeviceTypeUrl = tsm3Url + "deviceTypeList_query.action?version=1";
        addGroupUrl = tsm3Url + "group_add.action?version=1";
        addGroupMemberListUrl = tsm3Url + "groupMemberList_add.action?version=1";
        deleteGroupUrl = tsm3Url + "group_delete.action?version=1";
        queryGroupMemberListUrl = tsm3Url + "groupMemberList_query.action?version=1";
        dispatchLoginInUrl = iotpmUrl + "standardApi_login.action?version=1";
        dispatchLoginOutUrl = iotpmUrl + "standardApi_logout.action?version=1";
        queryInGroupMemberListUrl = iotpmUrl + "groupMemberList_query.action?version=1";
        queryUserListUrl = iotpmUrl + "userList_query.action?version=1";
        clearTempGroupListUrl = iotpmUrl + "tempGroupList_clear.action?version=1";
        deleteGroupMemberListUrl = tsm3Url + "groupMemberList_delete.action?version=1";
        addAssignmentUrl = tsm3Url + "group_add.action?version=1";
        updateAssignmentUrl = tsm3Url + "group_update.action?version=1";
        delAssignmentUrl = tsm3Url + "group_delete.action?version=1";
        updatePeopleInfoUrl = tsm3Url + "user_update.action?version=1";
        setRecordStatusUrl = tsm3Url + "groupRecordStatus_set.action?version=1";
        addIotUserUrl = tsm3Url + "iotUser_add.action" + VERSION_AND_PID;
        queryIotUserListUrl = tsm3Url + "iotUserList_query.action" + VERSION_AND_PID;
        deleteIotUserUrl = tsm3Url + "iotUser_delete.action" + VERSION_AND_PID;
        loginPidUrl = tsm3Url + "standardApi_login.action" + VERSION_AND_PID;
        scheduledLogin();
    }

    /**
     * 定时登陆对讲平台, 对方设置了30分钟超时
     */
    private void scheduledLogin() {
        if (!enabled) {
            return;
        }
        executorService.scheduleAtFixedRate(() -> {
            JSONObject jsonObject =
                HttpClientUtil.doHttPost(loginPidUrl, getLoginInfo(2, firstCustomerName, firstCustomerPassword));
            logger.info("定时刷新对讲平台session: {}", (jsonObject == null ? "" : jsonObject.toJSONString()));
        }, 0, 10, TimeUnit.MINUTES);
    }

    /**
     * 获取没有变动的一级客户pid
     * @return
     */
    public String getFirstCustomerPid() {
        return firstCustomer.getPid();
    }

    /**
     * 重新获取pid，并更新一级客户相关信息
     * @return
     */
    public String getNewFirstCustomerPid() {
        String pid = getNewFirstCustomer().getPid();
        return pid;
    }

    public FirstCustomer getNewFirstCustomerInfo() {
        FirstCustomer newFirstCustomer = getNewFirstCustomer();
        return newFirstCustomer;
    }

    /**
     * 重新获取pid，并更新一级客户相关信息
     * @return
     */
    public FirstCustomer getFirstCustomerInfo() {
        return firstCustomer;
    }

    /**
     * 新增一级用户调用该接口，只会在初始化调用一次
     * @param firstCustomer
     * @return
     */
    public boolean addFirstCustomer(Map<String, String> firstCustomer) {
        String response = HttpClientUtil.sendPost(addFirstCustomerUrl + getNewProxyPid(), firstCustomer);
        boolean result = JSONObject.parseObject(response).getIntValue("result") == 0;
        if (result) {
            getNewFirstCustomerPid();
            //添加1类调度员账号admin:000000
        }

        return result;
    }

    private String getPid(JSONObject result) {
        JSONObject data = result.getJSONObject("data");
        return getDataPid(data);
    }

    private String getDataPid(JSONObject data) {
        return data.getString("pid");
    }

    private String getNewProxyPid() {
        JSONObject result = HttpClientUtil.doHttPost(loginUrl, getLoginInfo(1, proxyName, proxyPassword));
        return getPid(result);
    }

    private FirstCustomer getNewFirstCustomer() {
        JSONObject result =
            HttpClientUtil.doHttPost(loginUrl, getLoginInfo(2, firstCustomerName, firstCustomerPassword));
        JSONObject data = result.getJSONObject("data");
        firstCustomer = JSONObject.parseObject(data.toJSONString(), FirstCustomer.class);
        return firstCustomer;
    }

    /**
     * @param type      1代表是代理商 2代表一级客户
     * @param loginName 登录名称
     * @param password  登录密码
     * @return
     */
    private String getLoginInfo(int type, String loginName, String password) {
        JSONObject param = new JSONObject();
        param.put("type", type);
        param.put("loginName", loginName);
        param.put("password", password);
        return param.toJSONString();
    }

    public Tsm3Result getDeviceTypePageData(String modelName, Integer pageSize, Integer pageIndex) {

        JSONObject params = getQueryDeviceTypeParam(modelName, pageSize, pageIndex);
        String response = HttpClientUtil.sendPost(getDeviceTypeUrl + getNewFirstCustomerPid(), params.toString());
        Tsm3Result<OriginalModelInfo> result = JSONObject.parseObject(response, Tsm3Result.class);
        //如果是无效pid
        while (result.getResult() == 1000) {
            response = HttpClientUtil.sendPost(getDeviceTypeUrl, params.toString());
            result = JSONObject.parseObject(response, Tsm3Result.class);
        }
        return result;
    }

    private JSONObject getQueryDeviceTypeParam(String modelName, Integer pageSize, Integer pageIndex) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pageSize", pageSize);
        jsonObject.put("pageIndex", pageIndex);
        JSONObject params = new JSONObject();
        params.put("modelName", modelName);
        params.put("pageInfo", jsonObject);
        return params;
    }

    /*---------------对讲对象管理 START------------*/

    /**
     * 更新对讲对象
     * @param intercomParams intercomParams
     */
    public JSONObject updateIntercomObject(Map<String, String> intercomParams) {
        String url = getTsm3Url("user_update.action");
        String resultBodyStr = HttpClientUtil.sendPost(url, intercomParams);
        JSONObject resultBody = JSON.parseObject(resultBodyStr);
        // JSONObject resultBody = HttpClientUtil.doHttPost(url, JSON.toJSONString(intercomParams));
        return getResultBodyAndUpdate(resultBody);
    }

    /**
     * 新增对讲对象
     * @param intercomParams intercomParams
     * @return JSONObject
     */
    public JSONObject addIntercomObject(Map<String, String> intercomParams) {

        // JSONObject resultMap = new JSONObject();
        String url = getTsm3Url("quickUser_add.action");
        String resultBodyStr = HttpClientUtil.sendPost(url, intercomParams);
        // resultMap = getResultBodyAndUpdate(JSON.parseObject(resultBodyStr));

        // if (resultMap.getIntValue("result") == ErrorMessageEnum.SUCCESS_CODE) {
        //     long userId = resultMap.getJSONObject("data").getLongValue("userId");
        //     // .新增对讲对象成功后,当前监控对象所在的分组; 对讲对象加入群组
        //     String custId = intercomParams.get("custId");
        //     resultMap = addUserGroupList(addUserGroupRequestParam, userId, custId);
        //     resultMap.getJSONObject("data").put("userId", userId);
        //     return resultMap;
        // }
        return getResultBodyAndUpdate(JSON.parseObject(resultBodyStr));
    }

    /**
     * 用户添加到群组
     * @param addUserGroupRequestParam addUserGroupRequestParam
     * @param userId                   userId
     * @param customerCode             customerCode
     * @return
     */
    public JSONObject addUserGroupList(JSONObject addUserGroupRequestParam, Long userId, String customerCode) {
        String url = getTsm3Url("userGroupList_add.action");
        addUserGroupRequestParam.put("userId", userId);
        addUserGroupRequestParam.put("custId", customerCode);
        return getJsonObject(url, addUserGroupRequestParam);
    }

    public String getTsm3Url(String uri) {
        return tsm3Url + uri + "?version=1";
    }

    /**
     * 获得客户id
     * @return 客户id
     */
    public Long getCustId() {
        return firstCustomer.getCustId();
    }

    /**
     * 添加群组
     * @param type            群组类型 0:普通组 1:全呼组
     * @param assignmentName  分组名称
     * @param groupCallNumber 群组号码
     * @param comments        憋住
     * @return JSONObject
     */
    public JSONObject addGroup(String type, String assignmentName, String groupCallNumber, String comments) {
        Map<String, String> assignmentInfoMap = new HashMap<>(16);
        assignmentInfoMap.put("group.type", type);
        assignmentInfoMap.put("group.name", assignmentName);
        assignmentInfoMap.put("group.number", groupCallNumber);
        assignmentInfoMap.put("group.comments", comments);
        assignmentInfoMap.put("custId", firstCustomer.getCustId().toString());
        assignmentInfoMap.put("parentId", firstCustomer.getParentId().toString());
        return HttpClientUtil.sendPostRequest(addGroupUrl, assignmentInfoMap);
    }

    /**
     * 添加群组内成员
     * @param addGroupMemberParam 添加群组成员参数
     * @return JSONObject
     */
    public JSONObject addGroupMemberList(JSONObject addGroupMemberParam) {
        return HttpClientUtil.doHttPost(addGroupMemberListUrl, addGroupMemberParam.toJSONString());
    }

    /**
     * 删除群组
     * @param groupId 删除的群组id
     * @return JSONObject
     */
    public JSONObject deleteGroup(Long groupId) {
        JSONObject deleteGroupParam = new JSONObject();
        deleteGroupParam.put("custId", getCustId());
        deleteGroupParam.put("groupId", groupId);
        return HttpClientUtil.doHttPost(deleteGroupUrl, deleteGroupParam.toJSONString());
    }

    /**
     * 查询群组成员
     * @param groupId   当前选择查询群组ID
     * @param userName  用户名称 支持模糊查询
     * @param pageIndex 当前页码
     * @param pageSize  每页记录条数
     * @return JSONObject
     */
    public JSONObject queryGroupMemberList(Long groupId, String userName, Integer pageIndex, Integer pageSize) {
        JSONObject queryGroupMemberParam = new JSONObject();
        queryGroupMemberParam.put("custId", getCustId());
        queryGroupMemberParam.put("groupId", groupId);
        queryGroupMemberParam.put("userName", userName);
        JSONObject pageInfo = installPageInfoJsonObj(pageIndex, pageSize);
        queryGroupMemberParam.put("pageInfo", pageInfo);
        return HttpClientUtil.doHttPost(queryGroupMemberListUrl, queryGroupMemberParam.toJSONString());
    }

    /**
     * 调度服务登录
     * @param loginName 登录账号
     * @param password  登陆密码
     * @return JSONObject
     */
    public JSONObject dispatchLoginIn(String loginName, String password) {
        JSONObject dispatchLoginParam = new JSONObject();
        dispatchLoginParam.put("loginName", loginName);
        dispatchLoginParam.put("password", "000000");
        return HttpClientUtil.doHttPostAndSaveCookie(dispatchLoginInUrl, dispatchLoginParam.toJSONString(), loginName);
    }

    /**
     * 调度服务登出
     * @param userName 用户名称
     * @return JSONObject
     */
    public JSONObject dispatchLoginOut(String userName) {
        return JSONObject.parseObject(HttpClientUtil.sendGetAndUseSavedCookie(dispatchLoginOutUrl, null, userName));
    }

    /**
     * 查询在对讲组内用户
     * @param custId    群组归属客户ID 可为一级客户/二级客户
     * @param groupId   群组ID 查询“未指定默认群组设备”该值为0
     * @param userName  用户名称 支持模糊查询
     * @param userId    用户ID 用于精确查询
     * @param pageIndex 当前页码
     * @param pageSize  每页记录条数
     * @return JSONObject
     */
    public JSONObject queryInGroupMemberList(Long custId, Long groupId, String userName, Integer userId,
        Integer pageIndex, Integer pageSize) {
        JSONObject queryInGroupMemberParam = new JSONObject();
        queryInGroupMemberParam.put("custId", custId);
        queryInGroupMemberParam.put("groupId", groupId);
        queryInGroupMemberParam.put("userName", userName);
        queryInGroupMemberParam.put("userId", userId);
        JSONObject pageInfo = installPageInfoJsonObj(pageIndex, pageSize);
        queryInGroupMemberParam.put("pageInfo", pageInfo);
        return HttpClientUtil
            .doHttPostAndUseSavedCookie(queryInGroupMemberListUrl, queryInGroupMemberParam.toJSONString(),
                SystemHelper.getCurrentUsername());
    }

    /**
     * 查询用户
     * @param custId    指定客户ID 若不指定填写-1，查询结果为本次登陆的客户账号下用户
     * @param userList  用户信息列表
     * @param pageIndex 当前页码
     * @param pageSize  每页记录条数
     * @return JSONObject
     */
    public JSONObject queryUserList(Long custId, JSONArray userList, Integer pageIndex, Integer pageSize) {
        JSONObject queryUserParam = new JSONObject();
        queryUserParam.put("custId", custId);
        queryUserParam.put("userIdList", userList);
        queryUserParam.put("userName", "");
        queryUserParam.put("userNumber", "");
        JSONObject pageInfo = installPageInfoJsonObj(pageIndex, pageSize);
        queryUserParam.put("pageInfo", pageInfo);
        return HttpClientUtil.doHttPostAndUseSavedCookie(queryUserListUrl, queryUserParam.toJSONString(),
            SystemHelper.getCurrentUsername());
    }

    /**
     * 清理临时组
     * @param custId   归属客户ID
     * @param id       当前账号ID 注：I类账号ID/II类账号ID/III类账号ID
     * @param userName 用户名称
     * @return JSONObject
     */
    public JSONObject clearTempGroupList(Long custId, Long id, String userName) {
        JSONObject clearTempGroupParam = new JSONObject();
        clearTempGroupParam.put("custId", custId);
        clearTempGroupParam.put("id", id);
        return HttpClientUtil
            .doHttPostAndUseSavedCookie(clearTempGroupListUrl, clearTempGroupParam.toJSONString(), userName);
    }

    /**
     * 删除群组成员
     * @param groupId              当前选择群组ID
     * @param needDeleteUserIdList 需要删除的成员id列表
     * @return JSONObject
     */
    public JSONObject deleteGroupMemberList(Long groupId, List<Long> needDeleteUserIdList) {
        JSONObject deleteGroupMemberParam = new JSONObject();
        deleteGroupMemberParam.put("custId", getCustId());
        deleteGroupMemberParam.put("groupId", groupId);
        JSONArray userList = new JSONArray();
        for (Long userId : needDeleteUserIdList) {
            JSONObject userJsonObj = new JSONObject();
            userJsonObj.put("userId", userId);
            userList.add(userJsonObj);
        }
        deleteGroupMemberParam.put("userList", userList);
        return HttpClientUtil.doHttPost(deleteGroupMemberListUrl, deleteGroupMemberParam.toJSONString());
    }

    /**
     * 组装分页json对象
     * @param pageIndex 当前页码
     * @param pageSize  每页记录条数
     * @return JSONObject
     */
    private JSONObject installPageInfoJsonObj(Integer pageIndex, Integer pageSize) {
        JSONObject pageInfo = new JSONObject();
        pageInfo.put("pageSize", pageSize);
        pageInfo.put("pageIndex", pageIndex);
        return pageInfo;
    }

    /**
     * 新增群组
     * @param form
     * @return
     */
    public JSONObject addAssignment(ClusterForm form) {
        String url = addAssignmentUrl;
        Map<String, String> entity = new HashMap<>();
        entity.put("custId", firstCustomer.getCustId().toString());
        entity.put("parentId", "-1");
        entity.put("group.type", "0");
        entity.put("group.name", form.getName());
        entity.put("group.number", form.getGroupCallNumber());
        entity.put("group.comments", form.getDescription());
        String sendPost = HttpClientUtil.sendPost(url, entity);
        if (StringUtils.isEmpty(sendPost)) {
            return null;
        }
        return JSON.parseObject(sendPost);
    }

    /**
     * 修改群组
     * @param form
     * @return
     */
    public JSONObject updateAssignment(Cluster assign, ClusterForm form) {
        //空指针校验
        if (assign == null) {
            return null;
        }
        String url = updateAssignmentUrl;
        Map<String, String> entity = new HashMap<>();
        entity.put("custId", firstCustomer.getCustId().toString());
        entity.put("parentId", "-1");
        entity.put("group.type", "0");
        entity.put("group.id", assign.getIntercomGroupId().toString());
        entity.put("group.name", form.getName());
        entity.put("group.number", assign.getGroupCallNumber());
        entity.put("group.comments", form.getDescription());
        String sendPost = HttpClientUtil.sendPost(url, entity);
        if (StringUtils.isEmpty(sendPost)) {
            return null;
        }
        return JSON.parseObject(sendPost);
    }

    /**
     * 删除群组
     * @param cluster
     * @return
     */
    public JSONObject delAssignment(Cluster cluster) {
        String url = delAssignmentUrl;
        JSONObject param = new JSONObject();
        param.put("custId", firstCustomer.getCustId().toString());
        param.put("groupId", cluster.getIntercomGroupId().toString());
        return getJsonObject(url, param);
    }

    /**
     * 群组添加成员
     * @param cluster
     * @return
     */
    public JSONObject addAssignmentUser(Cluster cluster, List<JSONObject> addIntercomId,
        List<JSONObject> knobUserList) {
        String url = addGroupMemberListUrl;
        JSONObject param = new JSONObject();
        param.put("custId", firstCustomer.getCustId());
        param.put("groupId", cluster.getIntercomGroupId().toString());
        param.put("userList", addIntercomId);
        param.put("knobUserList", knobUserList);
        return getJsonObject(url, param);
    }

    /**
     * 群组删除成员
     * @param cluster
     * @return
     */
    public JSONObject delAssignmentUser(Cluster cluster, List<JSONObject> deleteIntercomId) {
        String url = deleteGroupMemberListUrl;
        JSONObject param = new JSONObject();
        param.put("custId", firstCustomer.getCustId());
        param.put("groupId", cluster.getIntercomGroupId());
        param.put("userList", deleteIntercomId);
        return getJsonObject(url, param);
    }

    public JSONObject updatePeopleInfo(IntercomObjectForm intercomObjectForm, PersonnelForm form) {
        String url = updatePeopleInfoUrl;
        Map<String, String> entity = new HashMap<>();
        entity.put("custId", firstCustomer.getCustId().toString());
        entity.put("parentId", "-1");
        entity.put("ms.id", intercomObjectForm.getUserId().toString());
        entity.put("ms.name", form.getPeopleNumber());
        entity.put("ms.priority", String.valueOf(intercomObjectForm.getPriority()));
        entity.put("ms.number", intercomObjectForm.getNumber());
        // -----根据机型能力填写 START 1:支持 0:不支持----
        OriginalModelInfo modelInfo =
            originalModelDao.getOriginalModelByModelId(intercomObjectForm.getOriginalModelId());
        // 是否支持视频会议
        entity.put("ms.videoConferenceEnable", String.valueOf(modelInfo.getVideoConferenceEnable()));
        // 是否支持音频会议
        entity.put("ms.audioConferenceEnable", String.valueOf(modelInfo.getAudioConferenceEnable()));
        // 是否支持视频会话
        entity.put("ms.videoCallEnable", String.valueOf(modelInfo.getVideoCallEnable()));
        // 是否支持文本消息
        entity.put("ms.sendTextEnable", String.valueOf(modelInfo.getSendTextEnable()));
        // 是否支持图片消息
        entity.put("ms.sendImageEnable", String.valueOf(modelInfo.getSendImageEnable()));
        // 是否支持离线语音消息
        entity.put("ms.sendAudioEnable", String.valueOf(modelInfo.getSendAudioEnable()));
        // 是否支持实时视频功能
        entity.put("ms.videoFuncEnable", String.valueOf(modelInfo.getVideoFuncEnable()));
        // APP登陆开关, 此处没有App,因此为空
        entity.put("ms.appEnable", "0");
        // -----根据机型能力填写 END----
        entity.put("ms.appPassword", "");
        // 是否开启录音
        entity.put("ms.recordEnable", String.valueOf(intercomObjectForm.getRecordEnable()));
        // 用户电话
        entity.put("ms.phoneNumber", intercomObjectForm.getSimcardNumber());
        // 备注
        entity.put("ms.comments", modelInfo.getComments());
        JSONObject object = JSON.parseObject(HttpClientUtil.sendPost(url, entity));
        if (object == null) {
            return null;
        }
        return object;
    }

    /**
     * 设置群组录音
     * @param cluster
     * @return
     */
    public JSONObject setRecordStatus(Cluster cluster) {
        String url = setRecordStatusUrl;
        JSONObject param = new JSONObject();
        param.put("groupId", cluster.getIntercomGroupId());
        param.put("recordEnable", cluster.getSoundRecording());
        return getJsonObject(url, param);

    }

    public JSONObject deleteIntercomObject(JSONObject deleteObject) {
        deleteObject.put("custId", firstCustomer.getCustId());
        String url = getTsm3Url("quickUser_delete.action");
        return getJsonObject(url, deleteObject);
    }

    public JSONObject findFriendList(Long userId) {
        String uri = "friendList_query.action?";
        String url = getTsm3Url(uri);
        JSONObject requestParam = new JSONObject();
        requestParam.put("custId", -1);
        requestParam.put("userId", userId);
        requestParam.put("friendName", "");

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(200);
        pageInfo.setPageIndex(1);
        requestParam.put("pageInfo", pageInfo);
        return HttpClientUtil.doHttPost(url, requestParam.toJSONString());
    }

    public JSONObject addFriends(List<Map<String, Object>> friends, Long userId) {
        String uri = "friendList_add.action";
        String url = getTsm3Url(uri);

        JSONObject requestParam = addFriendRequestParam(friends, userId);
        return getJsonObject(url, requestParam);
    }

    private JSONObject addFriendRequestParam(List<Map<String, Object>> friends, Long userId) {
        Long custId = getFirstCustomerInfo().getCustId();

        JSONObject requestParam = new JSONObject();
        requestParam.put("custId", custId);
        requestParam.put("userId", userId);
        requestParam.put("friendList", friends);
        return requestParam;
    }

    public JSONObject deleteFriends(List<Long> friends, Long userId) {
        String uri = "friendList_delete.action";
        String url = getTsm3Url(uri);
        JSONObject requestParam = getFriendRequestParam(friends, userId);
        return getJsonObject(url, requestParam);
    }

    private JSONObject getFriendRequestParam(List<Long> friends, Long userId) {
        Long custId = getFirstCustomerInfo().getCustId();

        JSONObject requestParam = new JSONObject();
        requestParam.put("custId", custId);
        requestParam.put("userId", userId);
        JSONArray friendList = new JSONArray();
        friends.forEach(id -> {
            JSONObject friendId = new JSONObject();
            friendId.put("userId", id);
            friendList.add(friendId);
        });
        requestParam.put("friendList", friendList);
        return requestParam;
    }

    private JSONObject getJsonObject(String url, JSONObject requestParam) {
        JSONObject resultBody = HttpClientUtil.doHttPost(url, requestParam.toJSONString());
        return getResultBodyAndUpdate(resultBody);
    }

    private JSONObject getResultBodyAndUpdate(JSONObject resultBody) {
        if (resultBody == null) {
            resultBody = new JSONObject();
            resultBody.put("message", "接口调用异常");
            resultBody.put("result", 1);
            return resultBody;
        }
        return resultBody;
    }

    /**
     * 更新录音状态
     * @param recordEnable recordEnable
     * @param userId       userId
     * @return JSONObject
     */
    public JSONObject updateRecordStatus(Integer recordEnable, Long userId) {
        String uri = "userRecordStatus_set.action";
        String url = getTsm3Url(uri);
        JSONObject requestParam = new JSONObject();
        requestParam.put("userId", userId);
        requestParam.put("recordEnable", recordEnable);
        return getJsonObject(url, requestParam);
    }



    /*--------------调度员 START------------*/

    public JSONObject addIotUser(String userName, String attribute, String personCallNumbers) {
        FirstCustomer firstCustomer = getNewFirstCustomerInfo();
        Map<String, String> iotUserInfo = getIotUserInfo(userName, attribute, personCallNumbers, firstCustomer);
        return JSONObject.parseObject(HttpClientUtil.sendPost(addIotUserUrl, iotUserInfo));
    }

    public JSONObject deleteIotUser(String userId) {
        FirstCustomer firstCustomer = getNewFirstCustomerInfo();
        JSONObject param = new JSONObject();
        param.put("custId", firstCustomer.getCustId());
        param.put("userId", userId);
        return HttpClientUtil.doHttPost(deleteIotUserUrl, param.toJSONString());
    }

    public Tsm3Result<IntercomIotUserInfo> queryIotUser(String userName) {
        FirstCustomer firstCustomer = getFirstCustomerInfo();
        JSONObject param = getQueryIotUserParam(userName, firstCustomer);
        String response = HttpClientUtil.sendPost(queryIotUserListUrl, param.toJSONString());
        Tsm3Result<IntercomIotUserInfo> result = JSONObject.parseObject(response, Tsm3Result.class);
        //如果是无效pid
        while (result.getResult() == 1000) {
            response = HttpClientUtil.sendPost(queryIotUserListUrl, param.toString());
            result = JSONObject.parseObject(response, Tsm3Result.class);
        }
        return result;
    }

    private JSONObject getQueryIotUserParam(String userName, FirstCustomer firstCustomer) {
        JSONObject param = new JSONObject();
        JSONObject pageInfo = new JSONObject();
        pageInfo.put("pageSize", 10);
        pageInfo.put("pageIndex", 1);
        param.put("custId", firstCustomer.getCustId());
        param.put("pageInfo", pageInfo);
        param.put("userName", userName);
        return param;
    }

    private Map<String, String> getIotUserInfo(String userName, String attribute, String personCallNumbers,
        FirstCustomer firstCustomer) {
        Map<String, String> iotUserInfo = new HashMap<>();
        iotUserInfo.put("custId", firstCustomer.getCustId().toString());
        iotUserInfo.put("parentId", firstCustomer.getParentId().toString());
        iotUserInfo.put("ms.userName", userName);
        iotUserInfo.put("ms.userNumber", personCallNumbers);
        iotUserInfo.put("ms.loginName", userName);
        iotUserInfo.put("ms.password", "000000");
        iotUserInfo.put("ms.attribute", attribute);
        iotUserInfo.put("ms.comments", "备注");
        return iotUserInfo;
    }


    /*--------------调度员 END------------*/

    /**
     * 快速删除用户
     * @param userId       userId
     * @param customerCode 客户ID
     * @return JSONObject
     */
    public JSONObject quickDeleteUser(Long userId, String customerCode) {
        String uri = "quickUser_delete.action";
        String url = getTsm3Url(uri);
        JSONObject requestParam = new JSONObject();
        requestParam.put("custId", customerCode);
        requestParam.put("userId", userId);
        return getJsonObject(url, requestParam);
    }

    public JSONObject deleteUserGroupList(List<Long> userGroupList, Long userId) {
        String uri = "userGroupList_delete.action";
        String url = getTsm3Url(uri);
        JSONObject requestParam = new JSONObject();
        requestParam.put("custId", getCustId());
        requestParam.put("userId", userId);
        List<Map<String, Object>> groupList = new ArrayList<>();

        for (Long groupId : userGroupList) {
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", groupId);
            groupList.add(map);
        }

        requestParam.put("groupList", groupList);
        return getJsonObject(url, requestParam);
    }

    @PreDestroy
    public void close() {
        executorService.shutdown();
    }
}
