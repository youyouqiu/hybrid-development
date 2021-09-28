package com.zw.platform.controller.schedulingcenter;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.scheduledmanagement.SchedulingFrom;
import com.zw.platform.domain.scheduledmanagement.SchedulingQuery;
import com.zw.platform.domain.scheduledmanagement.SchedulingRelationMonitorInfo;
import com.zw.platform.service.schedulingcenter.SchedulingManagementService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 11:37
 */
@Controller
@RequestMapping("/m/schedulingCenter/schedulingManagement")
public class SchedulingManagementController {
    private static Logger logger = LogManager.getLogger(SchedulingManagementController.class);

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 排班管理页面
     */
    private static final String LIST_PAGE =
        "modules/schedulingCenter/schedulingManagement/schedulingManagementListPage";

    /**
     * 新增排班页面
     */
    private static final String ADD_PAGE = "modules/schedulingCenter/schedulingManagement/schedulingManagementAddPage";

    /**
     * 修改排班页面
     */
    private static final String UPDATE_PAGE =
        "modules/schedulingCenter/schedulingManagement/schedulingManagementUpdatePage";

    /**
     * 排班详情页面
     */
    private static final String DETAIL_PAGE =
        "modules/schedulingCenter/schedulingManagement/schedulingManagementDetailPage";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SchedulingManagementService schedulingManagementService;

    /**
     * 排班管理页面
     */
    @Auth
    @RequestMapping(value = "/listPage", method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 排班列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(SchedulingQuery query) {
        try {
            return schedulingManagementService.getSchedulingList(query);
        } catch (Exception e) {
            logger.error("分页查询排班管理异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 新增排班页面
     */
    @RequestMapping(value = "/getSchedulingAddPage", method = RequestMethod.GET)
    public String getSchedulingAddPage() {
        return ADD_PAGE;
    }

    /**
     * 新增排班
     */
    @RequestMapping(value = "/addScheduling", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addScheduling(@Validated(ValidGroupAdd.class) final SchedulingFrom schedulingFrom,
        final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return schedulingManagementService.addScheduling(schedulingFrom, ipAddress);
        } catch (Exception e) {
            logger.error("新增排班异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 删除排班
     * @param scheduledInfoId 排班id
     */
    @RequestMapping(value = "/deleteScheduling", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteScheduling(String scheduledInfoId) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return schedulingManagementService.deleteScheduling(scheduledInfoId, ipAddress);
        } catch (Exception e) {
            logger.error("删除排班异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得排班修改页面
     */
    @RequestMapping(value = "/getSchedulingUpdatePage/{scheduledInfoId}", method = RequestMethod.GET)
    public ModelAndView getSchedulingUpdatePage(@PathVariable("scheduledInfoId") String scheduledInfoId) {
        try {
            return new ModelAndView(UPDATE_PAGE)
                .addObject("schedulingInfo", schedulingManagementService.getSchedulingInfoById(scheduledInfoId))
                .addObject("monitorIdList", JSONArray.toJSONString(
                    schedulingManagementService.getSchedulingRelationMonitorInfoList(scheduledInfoId).stream()
                        .map(SchedulingRelationMonitorInfo::getMonitorId).collect(Collectors.toList())))
                .addObject("schedulingItemList",
                    schedulingManagementService.getSchedulingItemInfoList(scheduledInfoId));
        } catch (Exception e) {
            logger.error("获得排班修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改排班
     */
    @RequestMapping(value = "/updateScheduling", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateScheduling(@Validated(ValidGroupUpdate.class) final SchedulingFrom schedulingFrom,
        final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return schedulingManagementService.updateScheduling(schedulingFrom, ipAddress);
        } catch (Exception e) {
            logger.error("修改排班异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得排班详情页面
     */
    @RequestMapping(value = "/getSchedulingDetailPage/{scheduledInfoId}", method = RequestMethod.GET)
    public ModelAndView getSchedulingDetailPage(@PathVariable("scheduledInfoId") String scheduledInfoId) {
        try {
            return new ModelAndView(DETAIL_PAGE)
                .addObject("schedulingInfo", schedulingManagementService.getSchedulingInfoById(scheduledInfoId))
                .addObject("monitorNameList", JSONArray.toJSONString(
                    schedulingManagementService.getSchedulingRelationMonitorInfoList(scheduledInfoId).stream()
                        .map(SchedulingRelationMonitorInfo::getMonitorName).collect(Collectors.toList())))
                .addObject("schedulingItemList",
                    schedulingManagementService.getSchedulingItemInfoList(scheduledInfoId));
        } catch (Exception e) {
            logger.error("获得排班修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 强制结束排班
     */
    @RequestMapping(value = "/mandatoryTerminationScheduling", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean mandatoryTerminationScheduling(String scheduledInfoId) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return schedulingManagementService.updateSchedulingEndDateToNowDate(scheduledInfoId, ipAddress);
        } catch (Exception e) {
            logger.error("强制结束排班异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 判断排班名称是否可以使用
     * @param scheduledName   排班名称
     * @param scheduledInfoId 排班id
     * @return true:可以使用; false: 不可以使用
     */
    @RequestMapping(value = "/judgeScheduledNameIsCanBeUsed", method = RequestMethod.POST)
    @ResponseBody
    public boolean judgeScheduledNameIsCanBeUsed(String scheduledName, String scheduledInfoId) {
        try {
            return schedulingManagementService.judgeScheduledNameIsCanBeUsed(scheduledName, scheduledInfoId);
        } catch (Exception e) {
            logger.error("判断排班名称是否可以使用异常", e);
            return false;
        }
    }

    /**
     * 检查排班冲突
     */
    @RequestMapping(value = "/checkSchedulingConflicts", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkSchedulingConflicts(SchedulingFrom schedulingFrom) {
        try {
            return schedulingManagementService.checkSchedulingConflicts(schedulingFrom);
        } catch (Exception e) {
            logger.error("检查排班冲突异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }
}
