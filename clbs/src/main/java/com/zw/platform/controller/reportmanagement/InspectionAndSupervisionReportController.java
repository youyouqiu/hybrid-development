package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.dto.platformInspection.Zw809MessageDTO;
import com.zw.platform.service.reportManagement.InspectionAndSupervisionService;
import com.zw.platform.service.reportManagement.impl.SuperPlatformMsgServiceImpl;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
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
 * 809查岗督办报表
 * @author denghuabing
 */
@Controller
@RequestMapping("/m/reportManagement/inspectionAndSupervision")
public class InspectionAndSupervisionReportController {

    Logger logger = LogManager.getLogger(InspectionAndSupervisionReportController.class);

    private static final String LIST_PAGE = "modules/reportManagement/inspectionAndSupervision";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private SuperPlatformMsgServiceImpl superPlatformMsgService;

    @Autowired
    private InspectionAndSupervisionService inspectionAndSupervisionService;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/getList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(String groupIds, String type, String startTime, String endTime, Integer status) {
        try {
            if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)
                && status != null) {
                // 获取数据前先更新表
                superPlatformMsgService.updatePastData();
                final List<Zw809MessageDTO> list =
                        inspectionAndSupervisionService.getList(groupIds, type, startTime, endTime, status);
                return new JsonResultBean(list);
            }
            return new JsonResultBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("809查岗督办报表异常", e);
            return new JsonResultBean(PageGridBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "809查岗督办报表列表");
            inspectionAndSupervisionService.exportList(null, response);
        } catch (Exception e) {
            logger.error("导出809查岗督办报表异常", e);
        }
    }
}
