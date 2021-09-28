package com.zw.adas.controller.platforminspection;

import com.zw.adas.domain.platforminspection.PlatformInspectionQuery;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.platformInspection.PlatformInspectionService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.talkback.common.ControllerTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 平台巡检controller
 * @date 2020/11/139:59
 */
@Controller
@Slf4j
@RequestMapping("/adas/platformInspection")
public class PlatformInspectionController {

    private static final String LIST_PAGE = "modules/adas/platformInspection/list";

    @Autowired
    private PlatformInspectionService platformInspectionService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        return new ModelAndView(LIST_PAGE);
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(@Valid PlatformInspectionQuery query, BindingResult result) {

        String errorMsg = generateParameters(query, result);
        if (StringUtils.isNotEmpty(errorMsg)) {
            return new PageGridBean(JsonResultBean.FAULT, errorMsg);
        }
        if (query.getVehicleIds() == null || query.getVehicleIds().size() == 0) {
            return new PageGridBean(JsonResultBean.SUCCESS);
        }
        return ControllerTemplate
            .getResultBean(() -> platformInspectionService.getListByKeyword(query), query, "平台巡检查询报错");
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = { "/export" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export(@Valid PlatformInspectionQuery query, BindingResult result,
        HttpServletResponse response) {

        String errorMsg = generateParameters(query, result);
        if (StringUtils.isNotEmpty(errorMsg)) {
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
        }
        if (query.getVehicleIds().isEmpty()) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        ExportExcelUtil.setResponseHead(response, "平台巡检");
        try {
            platformInspectionService.export(query, response);
        } catch (Exception e) {
            errorMsg = "平台巡检导出报错";
            log.error(errorMsg, e.getMessage());
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private String generateParameters(@Valid PlatformInspectionQuery query, BindingResult result) {

        if (result.hasErrors()) {
            return SpringBindingResultWrapper.warpErrors(result);
        }
        //校验时间
        if (query.getStartTime().getTime() > query.getEndTime().getTime()) {
            return "开始时间不能大于结束时间";
        }
        String keyword = query.getKeyword();
        Set<String> vehicleIds = query.getVehicleIds();
        if (StringUtils.isNotEmpty(keyword)) {
            //模糊查询
            vehicleIds.retainAll(MonitorUtils.fuzzySearchBindMonitorIds(keyword));
        }
        return null;
    }

    /**
     * 巡检结果查询
     */
    @RequestMapping(value = { "/getInspectionResult" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getInspectionResult(String inspectionResultId, Integer inspectionType) {
        if (StringUtils.isEmpty(inspectionResultId) || inspectionType == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }

        return ControllerTemplate
            .getResultBean(() -> platformInspectionService.getInspectionResult(inspectionResultId, inspectionType),
                "平台巡检结果查询报错");
    }

}
