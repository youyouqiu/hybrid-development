package com.zw.api.controller.modules;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;
import com.zw.platform.service.basicinfo.VehicleTypeService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
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
import java.util.Map;


@RestController
@RequestMapping("/swagger/m/basicinfo/monitoring/vehicle/type")
@Api(tags = {"车辆类型管理"}, description = "车辆类型相关api")
public class SwaggerVehicleTypeController {
    private static Logger log = LogManager.getLogger(SwaggerVehicleTypeController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/vehicle/type/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/vehicle/type/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/vehicle/type/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/vehicle/type/import";

    @Autowired
    private VehicleTypeService vehicleTypeService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 新增 车辆类型
     */
    @ApiOperation(value = "添加车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleCategory",
				value = "车辆类别(0:小型货车;1:载客货车;2:危险品运输车辆;3:货运运输车辆;4:工程车辆;5:特种车辆;6:其他车辆)",
				required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleType", value = "车辆类型", required = true, paramType = "query",
				dataType = "string")})
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ValidGroupAdd.class}) @ModelAttribute("form")
        final VehicleTypeForm form, final BindingResult bindingResult) {
        try {

            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            VehicleTypeDO vt = vehicleTypeService.findByVehicleType(form.getVehicleType());
            if (vt != null) {
                return new JsonResultBean(JsonResultBean.FAULT, "车辆类型已经存在！请重新输入！");
            }
            //获取访问服务器的客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            vehicleTypeService.add(form, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("新增车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 分页查询车辆类型
     */
    @ApiOperation(value = "获取车辆类型信息列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query",
				dataType = "Long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query",
				dataType = "Long", defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照车辆类别、车辆类型进行模糊搜索", paramType = "query",
            dataType = "string")})
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final VehicleTypeQuery query) {
        try {
            Page<VehicleTypeDO> result = vehicleTypeService.findByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询车辆信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 根据id删除 VehicleType
     */
    @ApiOperation(value = "删除车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            vehicleTypeService.delete(id, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("删除车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 通过ID得到VehicleType
     */
    @ApiOperation(value = "根据id获取车辆类型信息", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            return new JsonResultBean(vehicleTypeService.get(id));
        } catch (Exception e) {
            log.error("获取车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改VehicleType
     */
    @ApiOperation(value = "修改车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆类型id", required = true, paramType = "query",
				dataType = "string"),
        @ApiImplicitParam(name = "vehicleCategory",
				value = "车辆类别(0:小型货车;1:载客货车;2:危险品运输车辆;3:货运运输车辆;4:工程车辆;5:特种车辆;6:其他车辆)",
				required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleType", value = "车辆类型", required = true, paramType = "query",
				dataType = "string")})
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ValidGroupUpdate.class}) @ModelAttribute("form")
        final VehicleTypeForm form, final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            VehicleTypeDO vt = vehicleTypeService.findByVehicleType(form.getId(), form.getVehicleType());
            if (vt != null) {
                return new JsonResultBean(JsonResultBean.FAULT, "车辆类型已经存在！请重新输入！");
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            vehicleTypeService.update(form, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("修改车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 导出excel表
     */
    @ApiOperation(value = "导出车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        try {
            String filename = "车辆类型列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            vehicleTypeService.exportVehicleType(null, 1, response);
        } catch (Exception e) {
            log.error("导出车辆类型信息异常", e);
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "deltems", value = "批量删除的车辆类型ids(用逗号隔开)", required = true,
			paramType = "query", dataType = "Stirng")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            // 获取访问服务器的客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            String items = request.getParameter("deltems");
            vehicleTypeService.delete(items, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("批量删除车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "导入车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = vehicleTypeService.importVehicleType(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     */
    @ApiOperation(value = "下载车辆类型导入模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            String filename = "车辆类型模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            vehicleTypeService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载车辆类型导入模板异常", e);
        }
    }

    @ApiOperation(value = "检查车辆类型是否已经存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("vehicleType") String vehicleType) {
        try {
            VehicleTypeDO vt = null;
            vt = vehicleTypeService.findByVehicleType(vehicleType);
            return vt == null;
        } catch (Exception e) {
            log.error("检查车辆类型存在异常", e);
            return false;
        }
    }

}
