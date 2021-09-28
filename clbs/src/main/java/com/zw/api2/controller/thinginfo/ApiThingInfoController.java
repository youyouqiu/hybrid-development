package com.zw.api2.controller.thinginfo;

import com.alibaba.fastjson.JSONObject;
import com.zw.api2.swaggerEntity.SwaggerThingInfoForm;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.form.ThingInfoForm;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 物品监控Controller Modification by Wjy on 2016/7/26.
 */
@Controller
@RequestMapping("/api/basicinfo/monitoring/ThingInfo")
@Api(tags = { "物品模块_dev" }, description = "物品模块相关api")
public class ApiThingInfoController {
    private static Logger log = LogManager.getLogger(ApiThingInfoController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/ThingInfo/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/ThingInfo/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/ThingInfo/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/ThingInfo/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService userService;

    @Value("${sys.error.msg}")
    private String syError;

    @Value("${vehicle.brand.bound}")
    private String vehicleBrandBound;

    @Autowired
    private ConfigService configService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    @ApiIgnore
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 新增物品
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @ApiIgnore
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增物品
     */
    @Auth
    @ApiOperation(value = "物品模块新增接口", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final SwaggerThingInfoForm formData,
        final BindingResult bindingResult) {
        //后端表单校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }

        //表单赋值
        ThingInfoForm form = new ThingInfoForm();
        BeanUtils.copyProperties(formData, form);

        //操作逻辑
        try {
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
     * 通过ID得到ThingInfo
     */

    @ApiOperation(value = "物品修改页详情接口", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable("id") @ApiParam("物品id") final String id) {
        try {
            Map<String, Object> mav = new HashMap<>();
            // ThingInfo thingInfo = thingInfoService.get(id);
            // thingInfo.setProductDate(
            // StringUtils.isNotEmpty(thingInfo.getProductDate()) ? thingInfo.getProductDate().split(" ")[0] : null);
            // mav.put("result", thingInfo);
            // OrganizationLdap organization = userService.getOrgByUuid(thingInfo.getGroupId());
            mav.put("groupName", "organization.getName()");
            ConfigForm cfg = configService.getIsBand(id, "", "", "");
            mav.put("bandState", cfg == null ? false : true);
            return new JsonResultBean(mav);
        } catch (Exception e) {
            log.error("获取物品信息界面信息内容", e);
            return new JsonResultBean(syError);
        }
    }

    /**
     * 修改ThingInfo
     */
    @ApiOperation(value = "物品模块修改接口", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody

    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final SwaggerThingInfoForm formData,
        final BindingResult bindingResult) {
        //后端表单校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }

        //表单赋值
        ThingInfoForm form = new ThingInfoForm();
        BeanUtils.copyProperties(formData, form);

        //修改逻辑
        try {
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            // return thingInfoService.update(form, ip);
            return null;
        } catch (Exception e) {
            log.error("修改物品信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @Auth
    @ApiOperation(value = "物品模块批量删除接口", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "delIds", value = "物品id,多个以逗号进行拼接",
         required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(@RequestParam(value = "delIds") String delIds) {
        try {
            String[] item = delIds.split(",");
            // 已被绑定的物品编号号，用于提示信息时使用
            StringBuilder boundThingNumbersBuilder = new StringBuilder();
            // 已被绑定的物品id， 用于删除时使用
            StringBuilder boundThingIdsBuilder = new StringBuilder();
            // 没有被绑定的车辆的车辆id
            StringBuilder notBoundThingIdsBuilder = new StringBuilder();
            ConfigForm c;
            for (String anItem : item) {
                c = configService.getIsBand(anItem, "", "", "");
                if (c != null) {
                    boundThingNumbersBuilder.append(c.getBrands() + " ,");
                    boundThingIdsBuilder.append(anItem + ",");
                } else {
                    notBoundThingIdsBuilder.append(anItem + ",");
                }
            }
            // 没有被绑定的车辆，直接删除
            if (notBoundThingIdsBuilder.length() > 0) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                String notBoundThingIds = Converter.removeStringLastChar(notBoundThingIdsBuilder.toString());
                // thingInfoService.deleteBatch(notBoundThingIds.split(","), ip);
            }
            // 已经被绑定的车辆，提示用户是否确认删除
            JSONObject msg = new JSONObject();
            if (boundThingNumbersBuilder.length() > 0) {
                String boundThingNumbers = Converter.removeStringLastChar(boundThingNumbersBuilder.toString());
                String boundThingIds = Converter.removeStringLastChar(boundThingIdsBuilder.toString());
                msg.put("boundThingNumbers", boundThingNumbers);
                msg.put("boundThingIds", boundThingIds);
                msg.put("infoMsg", vehicleBrandBound);
            } else {
                msg.put("boundThingNumbers", "");
                msg.put("boundBrandIds", "");
                msg.put("infoMsg", "");
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("批量删除物品信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除 ThingInfo
     */
    @ApiOperation(value = "删除物品信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") @ApiParam("物品id") final String id) {
        try {
            if (id != null) {
                JSONObject msg = new JSONObject();
                msg.put("thingId", id);
                ConfigForm c = configService.getIsBand(id, "", "", "");
                if (c != null) {
                    msg.put("infoMsg", vehicleBrandBound);
                    return new JsonResultBean(msg);
                } else {
                    String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                    // thingInfoService.delete(id, ip);
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除物品信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 导入
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    @ApiIgnore
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    @ApiIgnore
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 获取客户端的IP地址
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
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ApiIgnore
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "物品列表模板");
            // thingInfoService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载物品列表模板异常", e);
        }
    }

    @ApiOperation(value = "检查物品编号是否已经存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "thingNumber", value = "物品编号",
         required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "id", value = "物品的uuid", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("thingNumber") String thingNumber, @RequestParam("id") String id) {
        try {
            // ThingInfo vt = thingInfoService.findByThingInfo(id, thingNumber);
            ThingInfo vt = null;
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("校验物品信息存在异常", e);
            return false;
        }
    }

    @ApiOperation(value = "校验物品编号唯一性", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "thingNumber", value = "物品编号",
         required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/checkThingNumberSole", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkThingNumberSole(String thingNumber) {
        try {
            // return thingInfoService.checkThingNumberSole(thingNumber);
            return false;
        } catch (Exception e) {
            log.error("校验物品编号唯一性异常", e);
            return false;
        }
    }
}
