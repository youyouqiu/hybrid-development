package com.zw.api2.controller.equipment;

import com.zw.api2.swaggerEntity.SwaggerDeviceForm;
import com.zw.api2.swaggerEntity.SwaggerDeviceQuery;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.form.DeviceForm;
import com.zw.platform.domain.basicinfo.form.DeviceGroupForm;
import com.zw.platform.domain.basicinfo.query.DeviceQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.service.basicinfo.DeviceService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

@RequestMapping("/api/m/basicinfo/equipment/device")
@Api(tags = { "设备管理_dev" }, description = "设备相关api")
@Controller
public class ApiDeviceController {
    private static final Logger log = LogManager.getLogger(ApiDeviceController.class);

    @Value("${sys.error.msg}")
    private String syError;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Value("${device.number.bound}")
    private String deviceNumberBound;

    private static final String LIST_PAGE = "modules/basicinfo/equipment/device/list";

    private static final String ADD_PAGE = "modules/basicinfo/equipment/device/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/device/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/equipment/device/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 获取表格页面
     * @return list page
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    @ApiIgnore
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     * @param form
     * @return PageGridBean
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ApiOperation(value = "获取终端列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public PageGridBean getListPage(final SwaggerDeviceQuery form) {
        try {
            DeviceQuery query = new DeviceQuery();
            BeanUtils.copyProperties(query, form);
            // Page<Map<String, Object>> result = deviceService.listDevices(query);
            // return new PageGridBean(query, result, true);
            return null;
        } catch (Exception e) {
            log.error("分页查询终端信息异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 获取添加页面
     * @param map null
     * @return add page
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    @ApiIgnore
    public String getAddPage(ModelMap map) {
        return ADD_PAGE;
    }

    /**
     * 添加终端
     * @param source        source
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author wangying
     * @Title: 添加终端
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ApiOperation(value = "新增终端", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean addDevice(@Validated({ ValidGroupAdd.class }) final SwaggerDeviceForm source,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {

                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                DeviceForm form = new DeviceForm();
                BeanUtils.copyProperties(form, source);
                DeviceGroupForm groupForm = new DeviceGroupForm();
                groupForm.setDeviceId(form.getId());
                if (!Converter.toBlank(form.getGroupId()).equals("")) {
                    groupForm.setGroupId(form.getGroupId());
                } else {
                    groupForm.setGroupId(Converter.toBlank(userService.getOrgUuidByUser()));
                }
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                /* boolean flag = deviceService.addDeviceWithGroup(form, groupForm, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }*/
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增终端异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }

    }

    /**
     * 根据id删除 终端
     * @param id id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ApiOperation(value = "删除终端", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean delete(@ApiParam(value = "终端ID", required = true) @PathVariable("id") final String id) {
        try {
            if (id != null) {
                // 判断终端是否已经绑定
                ConfigForm config = configService.getIsBand("", id, "", "");
                if (config == null) { // 未绑定
                    // 根据ID查询终端
                    DeviceInfo div = deviceService.findDeviceById(id);
                    if (div != null) {
                        DeviceForm form = new DeviceForm();
                        form.setId(id);
                        form.setFlag(0);
                        form.setGroupId(div.getGroupId());
                        // 获取访问服务器的客户端的IP地址
                        String ipAddress = new GetIpAddr().getIpAddr(request);
                        int sign = 0; // 区分修改和删除
                        /*boolean flag = deviceService.updateNewDeviceWithGroup(form, sign, ipAddress);
                        if (flag) {
                            return new JsonResultBean(JsonResultBean.SUCCESS);
                        }*/
                    }
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT, deviceNumberBound);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除终端异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }

    }

    /**
     * 批量删除
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ApiOperation(value = "批量删除终端", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean deleteMore(@ApiParam(value = "终端ID,多个使用逗号隔开", required = true) String delIds) {
        try {
            if (delIds != null) {
                String[] item = delIds.split(",");
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // return deviceService.deleteDeviceGroupByBatch(item, ipAddress);
                return null;
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除终端异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 修改终端
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ApiOperation(value = "修改终端,得到修改页面的接口", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    public JsonResultBean editPage(@ApiParam(value = "终端ID", required = true) @PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // 设备所在的组织
            Map<String, Object> resultMap = deviceService.findDeviceGroupById(id);
            String groupId = (String) resultMap.get("groupName");
            OrganizationLdap organization = userService.getOrgByUuid(groupId);
            resultMap.put("groupName", organization.getName());
            DeviceForm form = new DeviceForm();
            ConvertUtils.register(form, Date.class);
            BeanUtils.populate(form, resultMap);
            form.setGroupId(groupId);
            form.setInstallTimeStr(Converter.toString(form.getInstallTime(), "yyyy-MM-dd"));
            form.setProcurementTimeStr(Converter.toString(form.getProcurementTime(), "yyyy-MM-dd"));
            mav.addObject("result", form);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("修改终端弹出页面异常", e);
            return new JsonResultBean(e.getMessage());
        }
    }

    /**
     * 修改终端
     * @param source        source
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ApiOperation(value = "修改终端", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) final SwaggerDeviceForm source,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                DeviceForm form = new DeviceForm();
                BeanUtils.copyProperties(form, source);
                int sign = 1; // 区分删除还是修改
                // boolean flag = deviceService.updateNewDeviceWithGroup(form, sign, ipAddress);
                // if (flag) {
                //     return new JsonResultBean(JsonResultBean.SUCCESS);
                // }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改终端异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }

    }

    /**
     * 导出
     * @param response response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ApiIgnore
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "终端信息列表");
            // deviceService.exportDevice(null, 1, response);
        } catch (Exception e) {
            log.error("导出终端信息异常", e);
        }
    }

    /**
     * 下载模板
     * @param response response
     * @param request  request
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ApiIgnore
    public void download(HttpServletResponse response, HttpServletRequest request) {
        try {
            ExportExcelUtil.setResponseHead(response, "终端信息列表模板");
            deviceService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载终端信息列表模板异常", e);
        }
    }

    /**
     * 弹出导入页面
     * @return String
     * @author wangying
     * @Title: 导入
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    @ApiIgnore
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * importDevice
     * @param file file
     * @return JsonResultBean
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    @ApiIgnore
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // return deviceService.importDevice(file, ipAddress, request);
            return null;
        } catch (Exception e) {
            log.error("导入终端信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * repetition
     * @param deviceNumber deviceNumber
     * @return boolean
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "新增终端时,重复校验", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    public boolean repetition(
        @ApiParam(value = "终端号", required = true) @RequestParam("deviceNumber") String deviceNumber) {
        try {
            DeviceInfo vt = deviceService.findByDevice(deviceNumber);
            return vt == null;
        } catch (Exception e) {
            log.error("校验终端存在异常", e);
            return false;
        }
    }
}
