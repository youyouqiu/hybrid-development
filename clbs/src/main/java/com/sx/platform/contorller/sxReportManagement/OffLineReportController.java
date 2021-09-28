package com.sx.platform.contorller.sxReportManagement;

import com.alibaba.fastjson.JSONObject;
import com.sx.platform.domain.sxReport.OffLineReport;
import com.sx.platform.service.sxReportManagement.OffLineReportService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
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
 * @author Administrator
 */
@Controller
@RequestMapping("/sx/sxReportManagement/offLineReport")
public class OffLineReportController {
    private static final Logger logger = LogManager.getLogger(OffLineReportController.class);

    @Autowired
    private OffLineReportService offLineReportService;

    @Autowired
    private UserService userService;

    private static final String LIST_PAGE = "modules/sxReportManagement/offLineReport";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(String vehicleList, Integer day) {
        try {
            RedisKey redisKey = HistoryRedisKeyEnum.USER_OFF_LINE_REPORT_INFO_LIST.of(userService.getCurrentUserUuid());
            if (RedisHelper.isContainsKey(redisKey)) {
                RedisHelper.delete(redisKey);
            }
            JSONObject obj = new JSONObject();
            // 查询离线报表数据
            List<OffLineReport> list = offLineReportService.getList(vehicleList, day);

            RedisHelper.addToList(redisKey, list);
            RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
            obj.put("list", list);
            return new JsonResultBean(obj);
        } catch (Exception e) {
            logger.error("查询离线报表失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出(生成excel文件)
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res, String simpleQueryParam) {
        try {
            offLineReportService.export(res, simpleQueryParam);
        } catch (Exception e) {
            logger.error("离线查询报表报表页面导出数据异常(get)", e);
        }
    }

}
