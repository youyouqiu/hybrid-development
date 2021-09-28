package com.zw.platform.controller.statistic;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.statistic.LifecycleExpireStatisticQuery;
import com.zw.platform.domain.statistic.info.LifecycleExpireStatisticInfo;
import com.zw.platform.service.statistic.LifecycleExpireStatisticService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * 服务到期报表
 * @author zhouzongbo on 2018/12/10 10:15
 */
@Controller
@RequestMapping("/v/statistic/lifecycleStatistic")
public class LifecycleExpireStatisticController {
    private static final Logger log = LogManager.getLogger(LifecycleExpireStatisticController.class);

    private static final String LIST_PAGE = "vas/statistic/lifecycleStatistic/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private LifecycleExpireStatisticService lifecycleExpireStatisticService;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getListPage() {
        try {
            return new ModelAndView(LIST_PAGE);
        } catch (Exception e) {
            log.error("服务到期报表列表异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取服务到期报表数据
     */
    @ResponseBody
    @RequestMapping(value = "/findLifecycle", method = RequestMethod.POST)
    public JsonResultBean findLifecycle(LifecycleExpireStatisticQuery query) {
        try {
            if (Objects.nonNull(query)) {
                List<LifecycleExpireStatisticInfo> lifecycle = lifecycleExpireStatisticService.findLifecycle(query);
                return new JsonResultBean(lifecycle);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取服务到期报表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出服务到期报表数据
     */
    @RequestMapping(value = "/findExportLifecycle", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportLifecycle(LifecycleExpireStatisticQuery query) {
        try {
            if (Objects.nonNull(query)) {
                return lifecycleExpireStatisticService.findExportLifecycle(query);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("导出服务到期报表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/exportLifecycle", method = RequestMethod.GET)
    public void exportLifecycle(HttpServletResponse response) {
        try {
            String username = SystemHelper.getCurrentUsername();
            RedisKey exportLifecycleListRedisKey = HistoryRedisKeyEnum.EXPORT_LIFECYCLE_LIST.of(username);
            List<LifecycleExpireStatisticInfo> resultList =
                RedisHelper.getList(exportLifecycleListRedisKey, LifecycleExpireStatisticInfo.class);
            ExportExcelUtil.setResponseHead(response, "服务到期报表");
            ExportExcelUtil.export(new ExportExcelParam("", 1, resultList, LifecycleExpireStatisticInfo.class, null,
                response.getOutputStream()));
        } catch (Exception e) {
            log.error("导出服务到期报表数据异常", e);
        }

    }

}
