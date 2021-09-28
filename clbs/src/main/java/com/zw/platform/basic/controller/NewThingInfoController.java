package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.service.ThingService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.form.ThingInfoForm;
import com.zw.platform.domain.basicinfo.query.ThingInfoQuery;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 物品监控Controller Modification by Wjy on 2016/7/26.
 */
@Controller
@RequestMapping("/m/basicinfo/monitoring/ThingInfo")
public class NewThingInfoController {
    private static Logger log = LogManager.getLogger(NewThingInfoController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/ThingInfo/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/ThingInfo/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/ThingInfo/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/ThingInfo/import";

    @Autowired
    private ThingService thingService;

    @Autowired
    private HttpServletRequest request;

    @Value("${vehicle.brand.bound}")
    private String vehicleBrandBound;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 新增物品
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增物品
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final ThingInfoForm form,
        final BindingResult bindingResult) {
        return ControllerTemplate.getResultBean(() -> thingService.add(ThingDTO.getAddInstance(form)), "新增物品数据异常！");
    }

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final ThingInfoQuery query) {
        return ControllerTemplate.getPageGridBean(() -> thingService.getListByKeyWord(query), query, "查询物品列表异常");
    }

    /**
     * 通过ID得到ThingInfo
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        return ControllerTemplate.editPage(getEditModelAndView(id));
    }

    private ModelAndView getEditModelAndView(@PathVariable String id) {
        ModelAndView mav = new ModelAndView(EDIT_PAGE);
        ThingDTO thingDTO = thingService.getById(id);
        ThingInfo thingInfo = thingDTO.convertThingInfo();
        mav.addObject("result", thingInfo);
        mav.addObject("groupName", thingDTO.getOrgName());
        mav.addObject("bandState", Vehicle.BindType.HAS_BIND.equals(thingDTO.getBindType()));
        return mav;
    }

    /**
     * 修改ThingInfo
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final ThingInfoForm form,
        final BindingResult bindingResult) {
        return ControllerTemplate
            .getResultBean(() -> thingService.update(ThingDTO.getUpdateInstance(form)), "修改物品数据异常！");

    }

    /**
     * 导出excel表
     */
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "物品列表");
            thingService.export(response);
        } catch (Exception e) {
            log.error("导出物品信息异常", e);
        }
    }

    /**
     * 批量删除
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        return ControllerTemplate.getResult(() -> thingService.deleteThingInfoByBatch(request.getParameter("deltems")));

    }

    /**
     * 根据id删除 ThingInfo
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        return ControllerTemplate.getResultBean(() -> getDeleteResultBean(id), "删除物品信息异常");

    }

    private JsonResultBean getDeleteResultBean(String id) throws BusinessException {
        JSONObject msg = new JSONObject();
        msg.put("thingId", id);
        if (!thingService.delete(id)) {
            msg.put("infoMsg", vehicleBrandBound);
            return new JsonResultBean(msg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 导入
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        return ControllerTemplate.getResultBean(() -> thingService.importThingInfo(file), "导入物品信息异常");
    }

    @RequestMapping(value = "/exportError", method = RequestMethod.GET)
    public void exportDeviceError(HttpServletResponse response) {
        ControllerTemplate
            .execute(() -> ImportErrorUtil.generateErrorExcel(ImportModule.THING, "物品导入错误信息", null, response),
                "导出终端错误信息异常");
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        ControllerTemplate.export(() -> thingService.thingTemplate(response), "物品列表模板", response, "下载通用车辆列表模板异常");
    }

    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("thingNumber") String thingNumber, @RequestParam("id") String id) {
        return ControllerTemplate
            .execute(() -> thingService.checkThingNumberSole(thingNumber, id), "校验物品信息存在异常", false);

    }

    @RequestMapping(value = "/checkThingNumberSole", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkThingNumberSole(String thingNumber) {
        return ControllerTemplate
            .execute(() -> thingService.checkThingNumberSole(thingNumber, null), "校验物品编号唯一性异常", false);

    }
}
