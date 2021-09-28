package com.zw.api.controller.modules;

import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.form.ThingInfoForm;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/swagger/m/basicinfo/monitoring/ThingInfo")
@Api(tags = { "物品管理" }, description = "物品相关api")
public class SwaggerThingInfoController {
    private static Logger log = LogManager.getLogger(SwaggerThingInfoController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/ThingInfo/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/ThingInfo/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/ThingInfo/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/ThingInfo/import";

    // @Autowired
    // private ThingInfoService thingInfoService;

    @Autowired
    private HttpServletRequest request;

    @ApiOperation(value = "添加物品信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "物品名称", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "thingNumber", value = "物品编号", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final ThingInfoForm form,
        final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            // ThingInfo vt = thingInfoService.findByThingInfo(form.getThingNumber());
            // if (vt != null) {
            //     return new JsonResultBean(JsonResultBean.FAULT, "物品编号已经存在！请重新输入！");
            // }
            // thingInfoService.add(form, new GetIpAddr().getIpAddr(request));
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("新增物品信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除 ThingInfo
     */
    @ApiOperation(value = "删除物品信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            // thingInfoService.delete(id, new GetIpAddr().getIpAddr(request));
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("删除物品信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 通过ID得到ThingInfo
     */
    @ApiOperation(value = "根据id获取物品信息", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            // return new JsonResultBean(thingInfoService.get(id));
            return null;
        } catch (Exception e) {
            log.error("获取物品信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改ThingInfo
     */
    @ApiOperation(value = "修改物品信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "物品id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "name", value = "物品名称", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "thingNumber", value = "物品编号", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final ThingInfoForm form,
        final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            // ThingInfo vt = thingInfoService.findByThingInfo(form.getId(), form.getThingNumber());
            // if (vt != null) {
            //     return new JsonResultBean(JsonResultBean.FAULT, "物品编号已经存在！请重新输入！");
            // }
            // thingInfoService.update(form, new GetIpAddr().getIpAddr(request));
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("修改物品信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除物品信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的物品ids(用逗号隔开)", required = true, paramType = "query",
        dataType = "Stirng")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            for (int i = 0; i < item.length; i++) {
                // thingInfoService.delete(item[i], new GetIpAddr().getIpAddr(request));
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("批量删除物品信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "导入物品信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
            // return thingInfoService.importThingInfo(file, ipAddress);
            return null;
        } catch (Exception e) {
            log.error("导入物品信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     */
    @ApiOperation(value = "下载物品导入模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, HttpServletRequest request) {
        try {
            String filename = "物品列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            // thingInfoService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载物品列表模板异常", e);
        }
    }

    @ApiOperation(value = "检查物品编号是否已经存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("thingNumber") String thingNumber) {
        try {
            ThingInfo vt = null;
            // vt = thingInfoService.findByThingInfo(thingNumber);
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("下检查物品编号存在异常", e);
            return false;
        }
    }
}
