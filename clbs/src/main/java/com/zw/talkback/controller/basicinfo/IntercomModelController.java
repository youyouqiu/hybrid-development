package com.zw.talkback.controller.basicinfo;

import com.zw.platform.commons.Auth;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.common.ControllerTemplate;
import com.zw.talkback.domain.intercom.form.IntercomModelForm;
import com.zw.talkback.domain.intercom.query.IntercomModelQuery;
import com.zw.talkback.service.baseinfo.IntercomModelService;
import com.zw.talkback.service.baseinfo.OriginalModelService;
import com.zw.talkback.util.imports.ProgressDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 对讲机型管理
 * @version 1.0
 **/
@Controller
@RequestMapping("/talkback/intercomplatform/intercommodel")
public class IntercomModelController {

    private static final String LIST_PAGE = "talkback/intercomplatform/intercommodel/list";
    private static final String EDIT_PAGE = "talkback/intercomplatform/intercommodel/edit";
    private static final String ADD_PAGE = "talkback/intercomplatform/intercommodel/add";
    private static final String SOM_PAGE = "talkback/intercomplatform/intercommodel/synchronizeOriginalModel";
    @Autowired
    private OriginalModelService originalModelService;

    @Autowired
    private IntercomModelService intercomModelService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/synchronizeOriginalModel" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean synchronizeOriginalModel(HttpServletRequest request) {
        return ControllerTemplate.getResultBean(() -> originalModelService.addOriginalModelInfos(request), "初始化对讲机型异常");

    }

    @RequestMapping(value = { "/addIntercomModel" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addIntercomModel(IntercomModelForm form, HttpServletRequest request) {

        return ControllerTemplate
            .getResultBean(() -> intercomModelService.addIntercomModel(form, new GetIpAddr().getIpAddr(request)),
                "新增对讲机型异常");

    }

    @RequestMapping(value = { "/updateIntercomModel" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateIntercomModel(IntercomModelForm form, HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> intercomModelService.updateIntercomModel(form, new GetIpAddr().getIpAddr(request)),
                "更新对讲机型异常");

    }

    @RequestMapping(value = { "/deleteIntercomModel" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteIntercomModel(String id, HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> intercomModelService.deleteIntercomModelById(id, new GetIpAddr().getIpAddr(request)),
                "删除对讲机型异常");

    }

    /**
     * 批量删除
     */
    @RequestMapping(value = { "/deleteIntercomModels" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteIntercomModels(String ids, HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> intercomModelService.deleteIntercomModelByIds(ids, new GetIpAddr().getIpAddr(request)),
                "批量对讲机型异常");

    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getIntercomModels(IntercomModelQuery query) {
        return ControllerTemplate
            .getResultBean(() -> intercomModelService.getIntercomModels(query), query, "查询对讲机型异常！");
    }

    @Deprecated
    @RequestMapping(value = { "/exportIntercomModels" }, method = RequestMethod.GET)
    public void exportIntercomModels(IntercomModelQuery query, HttpServletResponse response) {
        ControllerTemplate
            .export(() -> intercomModelService.exportIntercomModels(query, response), "对讲机型", response, "导出对讲机型异常");
    }

    @RequestMapping(value = { "/getAllOriginalModel" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAllOriginalModel(String index) {
        return ControllerTemplate.getResultBean(() -> originalModelService.getAllOriginalModel(index), "获取所有的原始机型");
    }

    @RequestMapping(value = { "/getOriginalModelByIndex" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOriginalModelByIndex(String index) {
        return ControllerTemplate.getResultBean(() -> originalModelService.getOriginalModelByIndex(index), "获取原始机型");
    }

    @RequestMapping(value = "/importProgress", method = RequestMethod.GET)
    @ResponseBody
    public ProgressDetail importProgress(HttpServletRequest request) {
        ProgressDetail progress = (ProgressDetail) request.getSession().getAttribute("ORIGINAL_MODEL");
        return Optional.ofNullable(progress).orElse(new ProgressDetail());
    }

    @RequestMapping(value = { "/edit_{id}" }, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("id") String id) {
        return ControllerTemplate.editPage(EDIT_PAGE, () -> intercomModelService.getIntercomModelById(id));
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addMobileSourceBaseInfo() {
        return ADD_PAGE;
    }

    @RequestMapping(value = { "/synchronizeOriginalModel" }, method = RequestMethod.GET)
    public String synchronizeOriginalModel() {
        return SOM_PAGE;
    }

}
