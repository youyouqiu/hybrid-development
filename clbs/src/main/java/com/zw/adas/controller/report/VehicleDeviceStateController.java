package com.zw.adas.controller.report;

import com.zw.adas.domain.report.query.SingleVehicleStateQuery;
import com.zw.adas.domain.report.query.VehicleDeviceStateQuery;
import com.zw.adas.service.report.VehicleDeviceStateService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.talkback.common.ControllerTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author wanxing
 * @Title: 车辆终端运行状态
 * @date 2020/11/1310:32
 */
@Controller
@Slf4j
@RequestMapping("/adas/vehicleDeviceState")
public class VehicleDeviceStateController {
    private static final String LIST_PAGE = "modules/adas/vehicleDeviceState/list";

    @Autowired
    private OfflineExportService exportService;

    @Autowired
    private VehicleDeviceStateService vehicleDeviceStateService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        return new ModelAndView(LIST_PAGE);
    }

    /**
     * 离线导出
     *
     * @param query  查询条件
     * @param result 参数校验结果
     * @return 导出接口
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export(@Valid VehicleDeviceStateQuery query, BindingResult result) {
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        Map<String, String> condition = vehicleDeviceStateService.getQueryCondition(query, false);
        OfflineExportInfo exportInfo = query.getOffLineExportMsg(new TreeMap<>(condition));
        return ControllerTemplate.addExportOffline(exportService, exportInfo, "车辆与终端运行状态导出异常");
    }

    /**
     * 车辆与终端运行状态分页查询
     *
     * @param query  查询条件
     * @param result 校验结果
     * @return 分页结果
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(@Valid VehicleDeviceStateQuery query, BindingResult result) {
        if (result.hasErrors()) {
            return new PageGridBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        return ControllerTemplate.getPageResult(() -> vehicleDeviceStateService.getList(query));
    }


    /**
     * 查询单条车辆与终端运行状态
     *
     * @param query  查询条件
     * @param result 校验结果
     * @return 运行状态详情
     */
    @RequestMapping(value = "/single", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSingleState(@Valid SingleVehicleStateQuery query, BindingResult result) {
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        return ControllerTemplate.getResult(() -> vehicleDeviceStateService.getSingleState(query));
    }
}
