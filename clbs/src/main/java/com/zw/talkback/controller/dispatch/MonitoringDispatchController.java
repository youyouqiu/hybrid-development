package com.zw.talkback.controller.dispatch;

import com.zw.platform.commons.Auth;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.talkback.service.dispatch.MonitoringDispatchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 监控调度页面
 */
@Controller
@RequestMapping("/talkback/monitoring/dispatch")
public class MonitoringDispatchController {

    private static Logger log = LogManager.getLogger(MonitoringDispatchController.class);

    private static final String PAGE_LIST = "talkback/vas/monitoring/dispatch/list";

    @Autowired
    MonitoringDispatchService monitoringDispatchService;

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private HttpServletRequest request;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView init() {
        return new ModelAndView(PAGE_LIST);
    }

    /**
     * 调度服务登录
     * @return JsonResultBean JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/dispatchLogin", method = RequestMethod.POST)
    public JsonResultBean dispatchLogin() {
        try {
            return monitoringDispatchService.dispatchLoginIn();
        } catch (Exception e) {
            log.error("调度服务登录异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 对讲对象树初始化
     * @param interlocutorStatus 对讲对象状态 0:全部; 1:在线; 2:离线;
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/getInterlocutorTree", method = RequestMethod.POST)
    public JsonResultBean getInterlocutorTree(Integer interlocutorStatus) {
        try {
            return monitoringDispatchService.getInterlocutorTree(interlocutorStatus);
        } catch (Exception e) {
            log.error("获取对讲对象树异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 模糊搜索对讲对象 对讲对象名称 或者分组名称
     * @param queryParam 搜索条件
     * @param queryType  搜索类型 name:对讲对象名称; assignment:分组名称;
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/fuzzySearchInterlocutor", method = RequestMethod.POST)
    public JsonResultBean fuzzySearchInterlocutor(String queryParam, String queryType) {
        try {
            return monitoringDispatchService.fuzzySearchInterlocutor(queryParam, queryType);
        } catch (Exception e) {
            log.error("通过名称模糊搜索对讲对象异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 查询对讲组内用户
     * @param intercomGroupId    对讲群组id
     * @param interlocutorStatus 对讲对象状态 0:全部; 1:在线; 2:离线;
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/getInterlocutorAssignmentMember", method = RequestMethod.POST)
    public JsonResultBean getInterlocutorAssignmentMember(Long intercomGroupId, Integer interlocutorStatus) {
        try {
            return monitoringDispatchService.getInterlocutorAssignmentMember(intercomGroupId, interlocutorStatus);
        } catch (Exception e) {
            log.error("查询对讲组内用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 查询对讲用户信息
     * @param interlocutorId 对讲对象id
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/getInterlocutorInfoById", method = RequestMethod.POST)
    public JsonResultBean getInterlocutorInfoById(Long interlocutorId) {
        try {
            return monitoringDispatchService.getInterlocutorInfoById(interlocutorId);
        } catch (Exception e) {
            log.error("查询对讲用户信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 查找对讲对象,通过画的圆形区域
     * @param assignmentId   如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @param longitude      圆心经度
     * @param latitude       圆心纬度
     * @param radius         半径
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/findInterlocutorByCircleArea", method = RequestMethod.POST)
    public JsonResultBean findInterlocutorByCircleArea(String assignmentId, String assignmentType, Double longitude,
        Double latitude, Double radius) {
        try {
            return monitoringDispatchService
                .findInterlocutorByCircleArea(assignmentId, assignmentType, longitude, latitude, radius);
        } catch (Exception e) {
            log.error("查找对讲对象,通过画的圆形区域异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 查找对讲对象,通过画的矩形区域
     * @param assignmentId   如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @param leftLongitude  矩形区域左上角的经度
     * @param leftLatitude   矩形区域左上角的纬度
     * @param rightLongitude 矩形区域右下角的经度
     * @param rightLatitude  矩形区域右下角的纬度
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/findInterlocutorByRectangleArea", method = RequestMethod.POST)
    public JsonResultBean findInterlocutorByRectangleArea(String assignmentId, String assignmentType,
        Double leftLongitude, Double leftLatitude, Double rightLongitude, Double rightLatitude) {
        try {
            return monitoringDispatchService
                .findInterlocutorByRectangleArea(assignmentId, assignmentType, leftLongitude, leftLatitude,
                    rightLongitude, rightLatitude);
        } catch (Exception e) {
            log.error("查找对讲对象,通过画的矩形区域异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 查找对讲对象,通过固定对象
     * @param assignmentId   如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/findInterlocutorByFixedInterlocutor", method = RequestMethod.POST)
    public JsonResultBean findInterlocutorByFixedInterlocutor(String assignmentId, String assignmentType) {
        try {
            return monitoringDispatchService.findInterlocutorByFixedInterlocutor(assignmentId, assignmentType);
        } catch (Exception e) {
            log.error("查找对讲对象,通过固定对象异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 获得技能列表
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/getAllSkillList", method = RequestMethod.POST)
    public JsonResultBean getAllSkillList() {
        try {
            return monitoringDispatchService.getAllSkillList();
        } catch (Exception e) {
            log.error("获得技能列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 获得对讲机型列表
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/getAllIntercomModeList", method = RequestMethod.POST)
    public JsonResultBean getAllIntercomModeList() {
        try {
            return monitoringDispatchService.getAllIntercomModeList();
        } catch (Exception e) {
            log.error("获得对讲机型列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 获得驾照类别列表
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/getAllDriverLicenseCategoryList", method = RequestMethod.POST)
    public JsonResultBean getAllDriverLicenseCategoryList() {
        try {
            return monitoringDispatchService.getAllDriverLicenseCategoryList();
        } catch (Exception e) {
            log.error("获得驾照类别列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 获得资格证列表
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/getAllQualificationList", method = RequestMethod.POST)
    public JsonResultBean getAllQualificationList() {
        try {
            return monitoringDispatchService.getAllQualificationList();
        } catch (Exception e) {
            log.error("获得资格证列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 获得血型列表
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/getAllBloodTypeList", method = RequestMethod.POST)
    public JsonResultBean getAllBloodTypeList() {
        try {
            return monitoringDispatchService.getAllBloodTypeList();
        } catch (Exception e) {
            log.error("获得血型列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 查找对讲对象,通过固定条件
     * @param assignmentId             如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType           分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @param skillIds                 技能id
     * @param intercomModelIds         对讲机型id
     * @param driverLicenseCategoryIds 驾照类别id
     * @param qualificationIds         资格证id
     * @param gender                   性别 1:男; 2:女
     * @param bloodTypeIds             血型id
     * @param ageRange                 年龄范围 ps:20,50
     * @param longitude                经度
     * @param latitude                 纬度
     * @param radius                   半径
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/findInterlocutorByFixedCondition", method = RequestMethod.POST)
    public JsonResultBean findInterlocutorByFixedCondition(String assignmentId, String assignmentType, String skillIds,
        String intercomModelIds, String driverLicenseCategoryIds, String qualificationIds, String gender,
        String bloodTypeIds, String ageRange, Double longitude, Double latitude, Double radius) {
        try {
            return monitoringDispatchService
                .findInterlocutorByFixedCondition(assignmentId, assignmentType, skillIds, intercomModelIds,
                    driverLicenseCategoryIds, qualificationIds, gender, bloodTypeIds, ageRange, longitude, latitude,
                    radius);
        } catch (Exception e) {
            log.error("查找对讲对象,通过固定条件异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 判断对讲对象的任务组数量是否超出限制
     * @param interlocutorIds 对讲对象id
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/judgeInterlocutorTaskAssignmentNumIsOverLimit", method = RequestMethod.POST)
    public JsonResultBean judgeInterlocutorTaskAssignmentNumIsOverLimit(String interlocutorIds) {
        try {
            return monitoringDispatchService.judgeInterlocutorTaskAssignmentNumIsOverLimit(interlocutorIds);
        } catch (Exception e) {
            log.error("判断对讲对象的任务组数量是否超出限制异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 创建任务组
     * @param assignmentName  任务组名称
     * @param interlocutorIds 组内对讲对象id
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/addTaskAssignmentAndMember", method = RequestMethod.POST)
    public JsonResultBean addTaskAssignmentAndMember(String assignmentName, String interlocutorIds) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return monitoringDispatchService.addTaskAssignmentAndMember(assignmentName, interlocutorIds, ipAddress);
        } catch (Exception e) {
            log.error("创建任务组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 创建临时组
     * @param assignmentName  临时组名称
     * @param intercomGroupId 对讲组id
     * @param interlocutorIds 组内对讲对象id
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/addTemporaryAssignment", method = RequestMethod.POST)
    public JsonResultBean addTemporaryAssignment(String assignmentName, Long intercomGroupId, String interlocutorIds) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return monitoringDispatchService
                .addTemporaryAssignment(assignmentName, ipAddress, intercomGroupId, interlocutorIds);
        } catch (Exception e) {
            log.error("创建临时组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 解散对讲组
     * @param assignmentId   分组id
     * @param assignmentType 分组类型 1：固定组；2：任务组; 3:临时组
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/unbindAssignmentAndMonitor", method = RequestMethod.POST)
    public JsonResultBean unbindAssignmentAndMonitor(String assignmentId, String assignmentType) {
        try {
            return monitoringDispatchService.unbindAssignmentAndMonitor(assignmentId, assignmentType);
        } catch (Exception e) {
            log.error("解散任务组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 判断分组是否能加入对讲对象
     * @param assignmentId   分组id
     * @param assignmentType 分组类型 1：固定组；2：任务组; 3:临时组
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/judgeAssignmentIfJoinMonitor", method = RequestMethod.POST)
    public JsonResultBean judgeAssignmentIfJoinMonitor(String assignmentId, String assignmentType) {
        try {
            return monitoringDispatchService.judgeAssignmentIfJoinMonitor(assignmentId, assignmentType);
        } catch (Exception e) {
            log.error("判断分组是否能加入对讲对象异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 加入任务组
     * @param assignmentId    任务组id
     * @param interlocutorIds 组内对讲对象id
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/insertTaskAssignmentAndMember", method = RequestMethod.POST)
    public JsonResultBean insertTaskAssignmentAndMember(String assignmentId, String interlocutorIds) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return monitoringDispatchService.insertTaskAssignmentAndMember(assignmentId, interlocutorIds, ipAddress);
        } catch (Exception e) {
            log.error("加入任务组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 加入临时组 记录日志
     * @param intercomGroupId 对讲群组id
     * @param interlocutorIds 对讲对象id
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/insertTemporaryAssignmentRecordLog", method = RequestMethod.POST)
    public JsonResultBean insertTemporaryAssignmentRecordLog(Long intercomGroupId, String interlocutorIds) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return monitoringDispatchService
                .insertTemporaryAssignmentRecordLog(intercomGroupId, interlocutorIds, ipAddress);
        } catch (Exception e) {
            log.error("加入临时组记录日志异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 踢出任务组内对讲对象
     * @param assignmentId   分组id
     * @param interlocutorId 对讲对象id
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/removeTaskAssignmentInterlocutor", method = RequestMethod.POST)
    public JsonResultBean removeTaskAssignmentInterlocutor(String assignmentId, Long interlocutorId) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return monitoringDispatchService.removeTaskAssignmentInterlocutor(assignmentId, interlocutorId, ipAddress);
        } catch (Exception e) {
            log.error("踢出任务组内对讲对象异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 踢出临时组内对讲对象记录日志
     * @param intercomGroupId 对讲群组id
     * @param interlocutorId  对讲对象id
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/removeTemporaryAssignmentInterlocutorRecordLog", method = RequestMethod.POST)
    public JsonResultBean removeTemporaryAssignmentInterlocutorRecordLog(Long intercomGroupId, Long interlocutorId) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return monitoringDispatchService
                .removeTemporaryAssignmentInterlocutorRecordLog(intercomGroupId, interlocutorId, ipAddress);
        } catch (Exception e) {
            log.error("踢出临时组内对讲对象记录日志异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

}
