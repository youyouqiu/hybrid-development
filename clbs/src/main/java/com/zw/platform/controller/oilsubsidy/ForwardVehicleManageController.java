package com.zw.platform.controller.oilsubsidy;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlForm;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlQuery;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleQuery;
import com.zw.platform.service.oilsubsidy.ForwardVehicleManageService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.common.ControllerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 转发车辆管理
 * @author XK
 */
@Controller
@RequestMapping("/m/forward/vehicle/manage")
public class ForwardVehicleManageController {
    private static final String LIST_PAGE = "/modules/oilSubsidyManage/forwardVehicleManage/list";

    private static final String ADD_PAGE = "/modules/oilSubsidyManage/forwardVehicleManage/add";

    private static final String EDIT_PAGE = "/modules/oilSubsidyManage/forwardVehicleManage/edit";

    private static final String BIND_LINE_PAGE = "/modules/oilSubsidyManage/forwardVehicleManage/bindLine";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private ForwardVehicleManageService forwardVehicleManageService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(@Validated OilDownloadUrlForm oilDownloadUrl, BindingResult result,
        HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> forwardVehicleManageService.add(oilDownloadUrl, request), "新增油补转发地址信息异常", result);
    }

    @RequestMapping(value = { "/update" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean update(@Validated OilDownloadUrlForm oilDownloadUrl, BindingResult result,
        HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> forwardVehicleManageService.update(oilDownloadUrl, request), "修改油补转发地址信息异常", result);
    }

    @RequestMapping(value = { "/download_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean download(@PathVariable("id") String id, HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> forwardVehicleManageService.updateDownloadVehicles(id, request), "下载油补转发地址信息异常");
    }

    @RequestMapping(value = { "/delete_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") String id, HttpServletRequest request) {
        return ControllerTemplate.getResultBean(() -> forwardVehicleManageService.delete(id, request), "删除油补转发地址信息异常");
    }

    @RequestMapping(value = { "/findOilSubsidyPlat" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOilSubsidyPlat(String orgId) {
        return ControllerTemplate
            .getResultBean(() -> forwardVehicleManageService.findOilSubsidyPlat(orgId), "查询油补平台下拉框信息异常");
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(OilDownloadUrlQuery query) {
        return ControllerTemplate
            .getResultBean(() -> forwardVehicleManageService.queryInfos(query), query, "查询油补转发地址列表信息异常");
    }

    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView editForwardVehicleBaseInfo(@PathVariable("id") String id) {
        return ControllerTemplate.editPage(EDIT_PAGE, () -> forwardVehicleManageService.findById(id));
    }

    @RequestMapping(value = { "/canEdit_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean canEdit(@PathVariable("id") String id) {
        return ControllerTemplate.getResultBean(() -> forwardVehicleManageService.canEdit(id), "获取能否打开编辑页面信息异常");
    }

    @RequestMapping(value = { "/vehicle/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean vehicleList(OilForwardVehicleQuery query) {
        return new PageGridBean(query, forwardVehicleManageService.queryVehicleInfos(query), true);
    }

    /**
     * 关联路线页面
     * @param id
     * @return
     */
    @RequestMapping(value = { "/bindLine_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView bindLinePage(@PathVariable("id") String id) {
        return ControllerTemplate.editPage(BIND_LINE_PAGE, () -> forwardVehicleManageService.findVehicleById(id));
    }

    /**
     * 关联路线页面
     * @param id
     * @return
     */
    @RequestMapping(value = { "/bindLine" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean bindLine(String id, String lineId, HttpServletRequest request) {
        // 获取客户端的IP地址
        String ipAddress = new GetIpAddr().getIpAddr(request);
        return new JsonResultBean(forwardVehicleManageService.saveBindLine(id, lineId, ipAddress));
    }

    /**
     * 关联车辆
     * @param ids
     * @return
     */
    @RequestMapping(value = { "/bindVehicle" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean setCheckVehicle(String ids, HttpServletRequest request) {
        // 获取客户端的IP地址
        String ipAddress = new GetIpAddr().getIpAddr(request);
        return new JsonResultBean(forwardVehicleManageService.saveCheckVehicle(ids, ipAddress));
    }

    /**
     * 删除关联关系
     * @param ids
     * @return
     */
    @RequestMapping(value = { "/deleteVehicle" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteVehicle(String ids, HttpServletRequest request) {
        // 获取客户端的IP地址
        String ipAddress = new GetIpAddr().getIpAddr(request);
        return new JsonResultBean(forwardVehicleManageService.deleteVehicle(ids, ipAddress));
    }

}
