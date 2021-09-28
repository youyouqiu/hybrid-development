package com.zw.adas.controller.equipmentrepair;

import com.zw.adas.domain.equipmentrepair.BatchConfirmRepairDTO;
import com.zw.adas.domain.equipmentrepair.BatchFinishRepairDTO;
import com.zw.adas.domain.equipmentrepair.ConfirmDeviceRepairDTO;
import com.zw.adas.domain.equipmentrepair.FinishDeviceRepairDTO;
import com.zw.adas.domain.equipmentrepair.query.DeviceRepairQuery;
import com.zw.adas.service.equipmentrepair.EquipmentRepairService;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.talkback.common.ControllerTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

/**
 * @author wanxing
 * @Title: 设备报修controller
 * @date 2020/11/1310:29
 */
@Controller
@Slf4j
@RequestMapping("/adas/equipmentRepair")
public class EquipmentRepairController {
    private static final String LIST_PAGE = "modules/adas/equipmentRepair/list";

    @Autowired
    private EquipmentRepairService equipmentRepairService;

    @Autowired
    private OfflineExportService exportService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        return new ModelAndView(LIST_PAGE);
    }

    /**
     * 分页查询设备报修记录
     * @param query  查询条件
     * @param result 参数校验结果
     * @return 分页记录
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(@Valid DeviceRepairQuery query, BindingResult result) {
        if (result.hasErrors()) {
            return new PageGridBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        return ControllerTemplate.getPageResult(() -> equipmentRepairService.getList(query));
    }

    /**
     * 设备报修理离线导出
     * @param query  查询条件
     * @param result 参数校验结果
     * @return 导出结果
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export(@Valid DeviceRepairQuery query, BindingResult result) {
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        return ControllerTemplate.addExportOffline(exportService, query.getOffLineExport(), "设备报修导出异常");
    }

    /**
     * 根据主键值获取详细
     * @param primaryKey 主键值
     * @return 设备上报维修详情
     */
    @RequestMapping(value = { "/detail_{primaryKey}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getByPrimaryKey(@PathVariable("primaryKey") String primaryKey) {
        return ControllerTemplate.getResult(() -> equipmentRepairService.getByPrimaryKey(primaryKey));
    }

    /**
     * 确认设备报修
     * @param confirmDTO 确认信息
     * @param result     参数校验结果
     * @return 确认结果
     */
    @RequestMapping(value = { "/confirm" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean confirm(@Valid ConfirmDeviceRepairDTO confirmDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        return ControllerTemplate.getBooleanResult(() -> equipmentRepairService.confirm(confirmDTO));
    }

    /**
     * 批量确认设备报修上报
     * @param confirmDTO 确认信息
     * @param result     参数校验结果
     * @return 确认结果
     */
    @RequestMapping(value = { "/confirm/batch" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean batchConfirm(@Valid BatchConfirmRepairDTO confirmDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        if (StringUtils.isBlank(confirmDTO.getPrimaryKeys())) {
            return new JsonResultBean(JsonResultBean.FAULT, "请至少选择一条数据进行确认");
        }
        return ControllerTemplate.getBooleanResult(() -> equipmentRepairService.batchConfirm(confirmDTO));
    }

    /**
     * 设备维修上报完成
     * @param finishDTO 完成信息
     * @param result    参数校验结果
     * @return 确认结果
     */
    @RequestMapping(value = { "/finish" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean finish(@Valid FinishDeviceRepairDTO finishDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        return ControllerTemplate.getBooleanResult(() -> equipmentRepairService.finish(finishDTO));
    }

    /**
     * 批量完成设备维修
     * @param batchFinishDTO 批量完成信息
     * @param result         参数校验结果
     * @return 确认结果
     */
    @RequestMapping(value = { "/finish/batch" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean batchFinish(@Valid BatchFinishRepairDTO batchFinishDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        if (StringUtils.isBlank(batchFinishDTO.getPrimaryKeys())) {
            return new JsonResultBean(JsonResultBean.FAULT, "请至少选择一条数据进行完成");
        }
        return ControllerTemplate.getBooleanResult(() -> equipmentRepairService.batchFinish(batchFinishDTO));
    }
}
