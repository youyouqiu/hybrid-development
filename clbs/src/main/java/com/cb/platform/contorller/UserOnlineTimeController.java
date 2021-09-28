package com.cb.platform.contorller;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.MonthGroupOnlineTime;
import com.cb.platform.domain.MonthOnlineTime;
import com.cb.platform.domain.UserLogin;
import com.cb.platform.service.UserOnlineTimeService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户在线统计controller
 */
@Controller
@RequestMapping("/cb/cbReportManagement/userOnlineTime")
public class UserOnlineTimeController {

    private static Logger logger = LogManager.getLogger(UserOnlineTimeController.class);

    private static final String page = "modules/cbReportManagement/userOnlineStatistics";

    @Autowired
    private UserOnlineTimeService userOnlineTimeService;

    /**
     * 获取用户在线统计页面
     * @return string
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getPage() {
        return page;
    }

    /**
     * 根据道路运输企业、时间查询道路运输企业月在线时长数据
     * @param groupId  企业id
     * @param nowMonth 时间
     * @return
     */
    @RequestMapping(value = "/getGroupsOnlineTime", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonthGroupOnlineTime(String groupId, String nowMonth, String nextMonth) {
        try {
            JSONObject msg = new JSONObject();
            if (groupId != null && !groupId.isEmpty() && nowMonth != null && !nowMonth.isEmpty() && nextMonth != null
                && !nextMonth.isEmpty()) {
                List<MonthGroupOnlineTime> result =
                    userOnlineTimeService.findMonthGroupOnlineTime(groupId, nowMonth, nextMonth);
                if (result != null) {
                    msg.put("monthData", result);
                }
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("查询道路运输企业月在线时长数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 根据道路运输企业、时间查询导出道路运输企业月在线时长数据
     * @param groupId   组织uuid
     * @param nowMonth  当月第一天字符串日期
     * @param nextMonth 下月第一条字符串日期
     * @return
     */
    @RequestMapping(value = "/exportGroupsOnlineTime", method = RequestMethod.POST)
    @ResponseBody
    public void exportMonthGroupOnlineTime(String groupId, String nowMonth, String nextMonth,
        HttpServletResponse response) {
        try {
            if (groupId != null && !groupId.isEmpty() && nowMonth != null && !nowMonth.isEmpty() && nextMonth != null
                && !nextMonth.isEmpty()) {
                userOnlineTimeService.exportMonthGroup(null, 1, response, groupId, nowMonth, nextMonth);
            }
        } catch (Exception e) {
            logger.error("导出道路运输企业月在线时长数据异常", e);
        }
    }

    /**
     * 根据道路运输企业、时间查询道路运输企业用户月在线时长数据
     * @param userId    组织id
     * @param nowMonth  当月第一天的字符串日期
     * @param nextMonth 当月的下月的第一天的字符串日期
     * @return
     */
    @RequestMapping(value = "/getGroupUsersOnlineTime", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getGroupUserOnline(String userId, String nowMonth, String nextMonth) {
        try {
            JSONObject msg = new JSONObject();
            if (userId != null && !userId.isEmpty() && nowMonth != null && !nowMonth.isEmpty() && nextMonth != null
                && !nextMonth.isEmpty()) {
                List<MonthOnlineTime> resultData =
                    userOnlineTimeService.findMonthGroupUserOnlineTime(userId, nowMonth, nextMonth);
                if (resultData != null) {
                    msg.put("userMonthData", resultData);
                }
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("查询道路运输企业用户月在线时长数据异常");
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 根据道路运输企业、时间导出道路运输企业用户月在线时长数据
     * @param groupId   组织uuid
     * @param nowMonth  当月第一天字符串日期
     * @param nextMonth 下月第一天字符串日期
     * @return
     */
    @RequestMapping(value = "/exportgroupUsersOnlineTime", method = RequestMethod.POST)
    @ResponseBody
    public void exportGroupUserOnline(String groupId, String nowMonth, String nextMonth, HttpServletResponse response) {
        try {
            if (groupId != null && !groupId.isEmpty() && nowMonth != null && !nowMonth.isEmpty() && nextMonth != null
                && !nextMonth.isEmpty()) {
                userOnlineTimeService.exportMonthUser(null, 1, response, groupId, nowMonth, nextMonth);
            }
        } catch (Exception e) {
            logger.error("导出道路运输企业用户月在线时长数据异常");
        }
    }

    /**
     * 根据用户id查询用户登录明细
     * @param userIds   用户uuid
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @RequestMapping(value = "/usersOnlienTime", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getUserLoginDetail(String userIds, String startTime, String endTime) {
        try {
            JSONObject msg = new JSONObject();
            if (userIds != null && !userIds.isEmpty() && startTime != null && !startTime.isEmpty() && endTime != null
                && !endTime.isEmpty()) {
                List<UserLogin> result = userOnlineTimeService.findUserOnlineOfflineDate(userIds, startTime, endTime);
                if (result != null) {
                    msg.put("userData", result);
                }
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("查询用户登录明细数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出用户登录明细数据
     * @param userIds   用户uuid
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @RequestMapping(value = "/exportUsersOnlienTime", method = RequestMethod.POST)
    @ResponseBody
    public void exportUserLoginData(String userIds, String startTime, String endTime, HttpServletResponse response) {
        try {
            if (userIds != null && !userIds.isEmpty() && startTime != null && !startTime.isEmpty() && endTime != null
                && !endTime.isEmpty()) {
                List<UserLogin> resultValue =
                    userOnlineTimeService.findUserOnlineOfflineDate(userIds, startTime, endTime);
                userOnlineTimeService.exportUserLoginDetail(null, 1, response, resultValue);
            }
        } catch (Exception e) {
            logger.error("导出用户登录明细数据异常", e);
        }
    }
}
