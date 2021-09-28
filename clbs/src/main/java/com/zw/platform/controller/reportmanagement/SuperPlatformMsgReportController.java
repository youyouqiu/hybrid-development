package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.dto.platformInspection.Zw809MessageDTO;
import com.zw.platform.service.reportManagement.impl.SuperPlatformMsgServiceImpl;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/**
 * 上级平台消息处理报表Controller
 */

@Controller
@RequestMapping("/m/reportManagement/gangSupervisionReport")
public class SuperPlatformMsgReportController {
    private static Logger log = LogManager.getLogger(SuperPlatformMsgReportController.class);

    private static final String LIST_PAGE = "modules/reportManagement/gangSupervisionReport";

    @Autowired
    private SuperPlatformMsgServiceImpl superPlatformMsgService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询当前用户权限下未处理的上级平台消息数量
     */
    @RequestMapping(value = {"/getPlatformMsgNumber"}, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getUntreatedMsg() {
        try {
            // 获取数据前先更新表
            superPlatformMsgService.updatePastData();
            return new JsonResultBean(superPlatformMsgService.getTheDayPlatformMsg());
        } catch (Exception e) {
            log.error("获取用户权限下当天未处理的上级平台消息数量异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 查询用户权限下当天所有的上级平台消息
     */
    @RequestMapping(value = {"/getTheDayAllMsg"}, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getGroupAllMsg(String type, String startTime, String endTime, int status) {
        try {
            // 获取数据前先更新表
            superPlatformMsgService.updatePastData();
            List<Zw809MessageDTO> result =
                    superPlatformMsgService.getTheDayAllMsgByUser(type, startTime, endTime, status);
            if (result != null) {
                return new JsonResultBean(result);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取用户权限下当日所有的上级平台消息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "data-migration/zw_809_message", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean migrate809Message() {
        try {
            return new JsonResultBean(superPlatformMsgService.migrate809Message());
        } catch (Exception e) {
            log.error("迁移数据出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
