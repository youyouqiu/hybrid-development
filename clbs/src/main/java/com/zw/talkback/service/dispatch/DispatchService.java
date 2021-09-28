package com.zw.talkback.service.dispatch;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.talkback.domain.dispatch.DispatchGroupInfo;
import com.zw.talkback.domain.dispatch.PointInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface DispatchService {
    /**
     * 当前不可见人员list集合
     * @return
     */
    JsonResultBean invisibleList();

    /**
     * 查询不可见对讲对象人员信息
     * @param id 对讲对象id（平台）
     * @return
     */
    JsonResultBean findDetailsInfo(String id);

    /**
     * 海量点查询
     * @return
     */
    String getMassPoint();

    /**
     * 具体海量点弹窗
     * @param userId
     * @param request
     */
    PointInfo getPointInfo(Long userId, HttpServletRequest request);

    /**
     * 获取人员信息
     * @param userId 人员ID
     * @return 监控对象详情
     */
    JsonResultBean getPersonnelInfo(Long userId);

    DispatchGroupInfo getAssignmentInfo(Long userId);

    String getLogMsg(Integer type, String id);

    boolean addNotificationRecord(String receiveId, String content, String ipAddress, Integer type);

    List<Map<String, Object>> notificationRecordList(String receiveId, Integer pageSize, Integer limitSize);

    List<JSONObject> findUserSettingFenceInfo();

    /**
     * 告警处理
     * @param handleAlarms handleAlarms
     * @param ip           ip
     * @return JSONObject
     * @throws Exception Exception
     */
    JsonResultBean commonHandleAlarms(HandleAlarms handleAlarms, String ip) throws Exception;

}
