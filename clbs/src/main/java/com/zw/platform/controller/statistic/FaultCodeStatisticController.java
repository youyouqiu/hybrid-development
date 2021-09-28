package com.zw.platform.controller.statistic;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.statistic.FaultCodeQuery;
import com.zw.platform.service.obdManager.FaultCodeService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author zhouzongbo on 2018/12/28 16:09
 */
@Controller
@RequestMapping("/statistic/faultCodeStatistic")
public class FaultCodeStatisticController {

    private static final Logger logger = LogManager.getLogger(FaultCodeStatisticController.class);

    private static final String LIST_PAGE = "vas/statistic/faultCodeStatistic/list";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private FaultCodeService faultCodeService;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getFaultCodeListPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询故障码列表
     */
    @RequestMapping(value = "/getFaultCodeList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getFaultCodeList(FaultCodeQuery query) {
        try {
            if (Objects.nonNull(query)) {
                return faultCodeService.getFaultCodeList(query);
            }
            return new PageGridBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("分页查询故障码列表失败", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 查询导出数据
     */
    @RequestMapping(value = "/findExportFaultCode", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportFaultCode(FaultCodeQuery query) {
        try {
            return faultCodeService.findExportFaultCode(query);
        } catch (Exception e) {
            logger.error("查询导出数据故障码列表失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 导出
     */
    @RequestMapping(value = "/exportFaultCode", method = RequestMethod.GET)
    public void exportFaultCode(HttpServletResponse response) throws Exception {
        faultCodeService.getExportFaultCode(response);
    }
}
