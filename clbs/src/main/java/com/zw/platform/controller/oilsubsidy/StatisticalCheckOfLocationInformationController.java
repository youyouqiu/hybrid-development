package com.zw.platform.controller.oilsubsidy;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oilsubsidy.locationinformation.OilSubsidyLocationInformationDTO;
import com.zw.platform.domain.oilsubsidy.locationinformation.OilSubsidyLocationInformationQuery;
import com.zw.platform.service.oilsubsidy.StatisticalCheckOfLocationInformationService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.TemplateExportExcel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * 定位信息统计核对
 *
 * @author XK
 */
@Controller
@RequestMapping("/m/statistics/check/locationInformation")
public class StatisticalCheckOfLocationInformationController {

    private static final String LIST_PAGE = "/modules/oilSubsidyManage/statisticalCheckOfLocationInformation/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";
    private static Logger logger = LogManager.getLogger(StatisticalCheckOfLocationInformationController.class);

    @Autowired
    private StatisticalCheckOfLocationInformationService statisticalCheckOfLocationInformationService;
    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private TemplateExportExcel templateExportExcel;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @ResponseBody
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    public PageGridBean list(OilSubsidyLocationInformationQuery query) {
        try {
            Page<OilSubsidyLocationInformationDTO> locations =
                    statisticalCheckOfLocationInformationService.getListByOrgIdAndTime(query);
            return new PageGridBean(locations, PageGridBean.SUCCESS);
        } catch (Exception e) {
            logger.error("获取定位信息统计异常", e);
            return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 导出(生成excel文件) 导出定位信息统计报表
     * @param res res
     */
    @RequestMapping(value = "/export/locations", method = RequestMethod.GET)
    public void export(HttpServletResponse res) {
        try {
            statisticalCheckOfLocationInformationService.export(res);
        } catch (Exception e) {
            logger.error("导出定位信息统计报表异常", e);
        }
    }
}