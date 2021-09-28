package com.zw.platform.controller.OBDManager;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.constant.ObdEnum;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.form.OBDMetaInfo;
import com.zw.platform.domain.basicinfo.query.OBDVehicleTypeQuery;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/12/29 11:47
 */
@Controller
@RequestMapping("/v/obdManager/obdVehicleDataReport")
public class OBDVehicleDataReportController {
    private static final Logger log = LogManager.getLogger(OBDVehicleDataReportController.class);

    private static final String LIST_PAGE = "vas/obdManager/obdVehicleDataReport/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private OBDVehicleTypeService obdVehicleTypeService;


    /**
     * 获得原车数据报表页面
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getListPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            List<String> bandObdSensorVehicles = obdVehicleTypeService.getBandObdSensorVehicle();
            mav.addObject("bandObdSensorVehicles", JSON.toJSONString(bandObdSensorVehicles));
            final List<Object> obdInfo = Arrays.stream(ObdEnum.values())
                    .filter(o -> o.getId() != 0xF0FF)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            mav.addObject("defaultShowColumn", JSON.toJSONString(obdInfo));
            return mav;
        } catch (Exception e) {
            log.error("获得原车数据报表页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    private OBDMetaInfo convertToDTO(ObdEnum obdEnum) {
        return new OBDMetaInfo(
                obdEnum.getId(), obdEnum.getColumnName(), obdEnum.getDisplayName(), obdEnum.getUnit(),
                obdEnum.getDesc(), obdEnum.getType(), obdEnum.isNumeric(), obdEnum.isShowByDefault()
        );
    }

    /**
     * 获得OBD原车数据报表
     */
    @ResponseBody
    @RequestMapping(value = "/getOBDVehicleDataReport", method = RequestMethod.POST)
    public JsonResultBean getObdVehicleDataReport(String monitorId, String startTimeStr, String endTimeStr) {
        try {
            return obdVehicleTypeService.getObdVehicleDataReport(monitorId, startTimeStr, endTimeStr);
        } catch (Exception e) {
            log.error("获得OBD原车数据报表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 获得OBD原车数据表格
     */
    @ResponseBody
    @RequestMapping(value = "/getOBDVehicleDataTable", method = RequestMethod.POST)
    public PageGridBean list(OBDVehicleTypeQuery query) {
        try {
            return obdVehicleTypeService.getOBDVehicleDataTable(query);
        } catch (Exception e) {
            log.error("获得OBD原车数据表格异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }
}
