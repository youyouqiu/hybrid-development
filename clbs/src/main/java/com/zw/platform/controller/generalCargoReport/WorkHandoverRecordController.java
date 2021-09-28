package com.zw.platform.controller.generalCargoReport;

import com.zw.platform.commons.Auth;
import com.zw.platform.dto.reportManagement.WorkHandOverRecordQuery;
import com.zw.platform.service.generalCargoReport.WorkHandoverRecordService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 值班交接班记录表
 * @author CJY
 */
@Controller
@RequestMapping("/m/reportManagement/generalCargo")
public class WorkHandoverRecordController {
    private static final Logger log = LogManager.getLogger(WorkHandoverRecordController.class);

    private static final String LIST_PAGE = "modules/sdReportManagement/shiftRecordReport";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private WorkHandoverRecordService watchHandoverRecordService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 值班交接班记录表
     */
    @RequestMapping(value = "/getWorkHandOverRecord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getWorkHandOverRecord(WorkHandOverRecordQuery query) {
        try {
            return watchHandoverRecordService.getWorkHandOverRecord(query);
        } catch (Exception e) {
            log.error("值班交接班记录表查询列表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 值班交接班记录表-离线导出
     */
    @RequestMapping(value = "/exportWorkHandOverRecord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportWorkHandOverRecord(WorkHandOverRecordQuery query) {
        return watchHandoverRecordService.exportWorkHandOverRecord(query);
    }
}
