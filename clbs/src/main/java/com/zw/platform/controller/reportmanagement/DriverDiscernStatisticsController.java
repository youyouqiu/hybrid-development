package com.zw.platform.controller.reportmanagement;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.query.DriverDiscernStatisticsQuery;
import com.zw.platform.dto.driverMiscern.DriverDiscernStatisticsDetailDto;
import com.zw.platform.dto.driverMiscern.DriverDiscernStatisticsDto;
import com.zw.platform.service.reportManagement.DriverDiscernStatisticsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 终端驾驶员识别统计
 * @author penghj
 * @version 1.0
 * @date 2020/9/24 14:10
 */
@Controller
@RequestMapping("/m/driver/discern/statistics")
public class DriverDiscernStatisticsController {
    private static final Logger logger = LogManager.getLogger(DriverDiscernStatisticsController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/driverDiscernStatistics/list";

    @Autowired
    private DriverDiscernStatisticsService driverDiscernStatisticsService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页
     */
    @RequestMapping(value = "/pageQuery", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean pageQuery(final DriverDiscernStatisticsQuery query) {
        try {
            if (StringUtils.isBlank(query.getIdentificationStartDate())
                || StringUtils.isBlank(query.getIdentificationEndDate())) {
                return new PageGridBean(PageGridBean.FAULT);
            }
            Page<DriverDiscernStatisticsDto> page = driverDiscernStatisticsService.pageQuery(query);
            return new PageGridBean(page, true);
        } catch (Exception e) {
            logger.error("分页查询终端驾驶员识别统计异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 黑标获取附件详情
     */
    @RequestMapping(value = "/mediaInfo", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean mediaInfo(String id) {
        try {
            return new JsonResultBean(driverDiscernStatisticsService.getMediaInfo(id));
        } catch (Exception e) {
            logger.error("终端驾驶员识别统计查看附件信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }


    /**
     * 详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detail(String id, String time) {
        try {
            if (StringUtils.isBlank(id) || StringUtils.isBlank(time)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            List<DriverDiscernStatisticsDetailDto> detail = driverDiscernStatisticsService.detail(id, time);
            return new JsonResultBean(detail);
        } catch (Exception e) {
            logger.error("终端驾驶员识别统计详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public void export(HttpServletResponse response, DriverDiscernStatisticsQuery query) {
        try {
            driverDiscernStatisticsService.export(response, query);
        } catch (Exception e) {
            logger.error("终端驾驶员识别统计导出异常", e);
        }
    }
}
