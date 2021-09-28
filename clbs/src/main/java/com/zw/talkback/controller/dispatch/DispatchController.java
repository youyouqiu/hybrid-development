package com.zw.talkback.controller.dispatch;

import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
import com.zw.talkback.service.dispatch.DispatchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/talkback/monitoring/dispatch")
public class DispatchController {
    private Logger log = LogManager.getLogger(DispatchController.class);

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private LogSearchService logSearchService;

    @Resource
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 不可见人员列表
     * @return
     */
    @RequestMapping(value = "/invisibleList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean invisibleList() {
        try {
            return dispatchService.invisibleList();
        } catch (Exception e) {
            log.error("查询当前不在组数据异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 不可见人员详情信息
     * @param id 对讲对象ID（平台）
     * @return
     */
    @RequestMapping(value = "/invisibleDetailsInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean invisibleDetailsInfo(String id) {
        try {
            return dispatchService.findDetailsInfo(id);
        } catch (Exception e) {
            log.error("查询当前不在组用户详情", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 获取地图海量点
     * @return
     */
    @RequestMapping(value = "/getMassPoint", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMassPoint() {
        try {
            String result = dispatchService.getMassPoint();
            return new JsonResultBean((Object) ZipUtil.compress(result));
        } catch (Exception e) {
            log.error("查询海量点异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 获取地图海量点弹出详情
     * @return
     */
    @RequestMapping(value = "/getPointInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPointInfo(Long userID) {
        try {
            return new JsonResultBean(dispatchService.getPointInfo(userID, request));
        } catch (Exception e) {
            log.error("查询海量点弹出详情异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @RequestMapping(value = "/monitorBounced", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean monitorBounced(Long userId) {
        try {
            return dispatchService.getPersonnelInfo(userId);
        } catch (Exception e) {
            log.error("监控调度用户弹窗异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/assignmentBounced", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean assignmentBounced(Long userId) {
        try {
            return new JsonResultBean(dispatchService.getAssignmentInfo(userId));
        } catch (Exception e) {
            log.error("监控调度企业弹窗异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 记录日志
     * @param type    1(个呼),2(电话),3(禁言),4(解除禁言),5(开启定位),6(组呼),7(加入群组),8(退出群组),9(抢麦)
     * @param id      type(1-5)为对讲对象调度传对讲对象uuid，type(6-9)为对讲群组调度传群组uuid
     * @param request
     * @return
     */
    @RequestMapping(value = "/addLog", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addLog(Integer type, String id, HttpServletRequest request) {
        try {
            if (type != null) {
                String msg = dispatchService.getLogMsg(type, id);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                logSearchService.addLog(ipAddress, msg, "3", "监控调度");
            }
            return new JsonResultBean();
        } catch (Exception e) {
            log.error("监控调度记录日志异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 记录通知记录
     * @param receiveId
     * @param content
     * @return
     */
    @RequestMapping(value = "/addNotificationRecord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addNotificationRecord(String receiveId, String content, Integer type,
        HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return new JsonResultBean(dispatchService.addNotificationRecord(receiveId, content, ipAddress, type));
        } catch (Exception e) {
            log.error("记录调度员通知记录异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 查询通知记录
     */
    @RequestMapping(value = "/notificationRecordList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean notificationRecordList(String receiveId, Integer pageSize, Integer limitSize) {
        try {
            return new JsonResultBean(dispatchService.notificationRecordList(receiveId, pageSize, limitSize));
        } catch (Exception e) {
            log.error("记录调度员通知记录异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 查询用户设置的默认显示的所有围栏信息
     */
    @RequestMapping(value = "/getUserFenceInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getUserFenceInfo() {
        try {
            return new JsonResultBean(dispatchService.findUserSettingFenceInfo());
        } catch (Exception e) {
            log.error("查询用户设置的默认显示的所有围栏信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/handleAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean handleAlarm(HandleAlarms handleAlarms) {
        try {
            if (handleAlarms != null) {
                // 获得访问ip
                String ip = new GetIpAddr().getIpAddr(request);
                return dispatchService.commonHandleAlarms(handleAlarms, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("报警状态存储异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
