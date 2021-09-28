package com.zw.api.controller.modules;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
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
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
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
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/swagger/m/basicinfo/equipment/device")
@Api(tags = { "设备管理" }, description = "设备相关api")
public class SwaggerDeviceController {
    private static Logger log = LogManager.getLogger(SwaggerDeviceController.class);

    @Autowired
    private DeviceService diviceService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Value("${device.number.bound}")
    private String deviceNumberBound;

    private static final String ADD_PAGE = "modules/basicinfo/equipment/device/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/device/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/equipment/device/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    // 前端需要显示的列
    private static final String COLUMN_STR = "id,deviceNumber,deviceName,isStart,deviceType,channelNumber,iSvideo,"
        + "barCode,manuFacturer,flag,createDataTime,createDataUsername,updateDataTime,updateDataUsername,isRegister,"
        + "brand,groupName,installTime,installTimeStr";

    /**
     * 分页查询
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取设备列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照设备编号、设备名称、车牌号进行模糊搜索", required = false,
            paramType = "query", dataType = "string"), })
    public PageGridBean getListPage(final DeviceQuery query) {
        Page<Map<String, Object>> result = new Page<>();
        try {
            if (query != null) {
                List<Map<String, Object>> dataMap = new ArrayList<>();
                boolean isNull = false; // 若缓存中数据为空，则到数据库中查询
                if (StringUtils.isBlank(query.getSimpleQueryParam())) { // 没有查询条件
                    String userId = SystemHelper.getCurrentUser().getId().toString();
                    // 判断redis里面是否包含当前key(user+table+type)
                    /*String key = RedisHelper.buildKey(userId, "zw_m_device_info", "list");
                    if (!RedisHelper.isContainsKey(key, PublicVariable.REDIS_MYSQL_DATABASE)) {
                        // 若redis中不包含当前key值，则从数据库中查询再保存到redis中
                        List<Map<String, Object>> sqlList = diviceService.findDeviceByUser(query);
                        RedisHelper.rpush(key, sqlList, PublicVariable.REDIS_MYSQL_DATABASE);
                        RedisHelper.setExpire(key, VehicleStatus.ONE_HOUR, PublicVariable.REDIS_MYSQL_DATABASE);
                    }*/
                    // 从redis中查询数据并分页
                    // result = RedisQueryUtil.queryPageList(key, PublicVariable.REDIS_MYSQL_DATABASE, query);
                    dataMap = result.getResult();
                    if (dataMap == null || dataMap.isEmpty()) {
                        isNull = true;
                    }
                }
                if (StringUtils.isNotBlank(query.getSimpleQueryParam()) || isNull) { // 若是条件搜索并且缓存中数据为空,则在数据库中查询
                    result = diviceService.findDeviceByUser(query);
                    dataMap = result.getResult();
                }
                // 遍历所有列名，若没有值，默认设置为""
                String[] column = COLUMN_STR.split(",");
                for (Map<String, Object> map : dataMap) {
                    for (String keyStr : column) {
                        if (!map.containsKey(keyStr)) {
                            map.put(keyStr, "");
                        }
                        // 从Ldap中查询出组织名称
                        if ("groupName".equals(keyStr) && !Converter.toBlank(map.get(keyStr)).equals("")) {
                            OrganizationLdap organization = userService.getOrgByUuid((String) map.get(keyStr));
                            if (organization != null) {
                                map.put(keyStr, organization.getName());
                            } else {
                                map.put(keyStr, "");
                            }
                        }
                    }
                }
                return new PageGridBean(query, result, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("分页查询终端信息异常", e);
            return new PageGridBean(false);
        } finally {
            if (Objects.nonNull(result)) {
                result.close();
            }
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author wangying
     * @Title: 添加终端
     */
    @ApiOperation(value = "添加设备", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deviceType", value = "终端类型（1:交通部,2:GV320,3:TH)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "deviceNumber", value = "终端号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属企业id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addDevice(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final DeviceForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                DeviceInfo vt = diviceService.findByDevice(form.getDeviceNumber());
                if (vt != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "终端号已存在！");
                }

                DeviceGroupForm groupForm = new DeviceGroupForm();
                groupForm.setDeviceId(form.getId());
                if (!Converter.toBlank(form.getGroupId()).equals("")) {
                    groupForm.setGroupId(form.getGroupId());
                    try {
                        if (userService.getOrgByUuid(form.getGroupId()) == null) { // 校验所属企业是否在数据库存在
                            return new JsonResultBean(JsonResultBean.FAULT, "所属企业不存在！");
                        }
                    } catch (Exception e) {
                        return new JsonResultBean(JsonResultBean.FAULT, "所属企业不存在！");
                    }
                } else {
                    groupForm.setGroupId(Converter.toBlank(userService.getOrgIdByUser()));
                }
                // 获取访问服务器的客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // boolean flag = diviceService.addDeviceWithGroup(form, groupForm, ipAddress);
                // if (flag) {
                //     return new JsonResultBean(JsonResultBean.SUCCESS);
                // } else {
                //     return new JsonResultBean(JsonResultBean.FAULT);
                // }
                return new JsonResultBean(JsonResultBean.FAULT);

            }
        } catch (Exception e) {
            log.error("新增终端异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 根据id删除 终端
     */
    @ApiOperation(value = "删除设备", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) throws BusinessException {
        try {
            if (id != null) {
                // 判断终端是否已经绑定
                ConfigForm config = configService.getIsBand("", id, "", "");
                if (config == null) { // 未绑定
                    // 根据ID查询终端
                    DeviceInfo div = diviceService.findDeviceById(id);
                    if (div != null) {
                        DeviceForm form = new DeviceForm();
                        form.setId(id);
                        form.setFlag(0);
                        // 获取访问服务器的客户端的IP地址
                        String ipAddress = new GetIpAddr().getIpAddr(request);
                        int sign = 0; // 区分修改和删除
                        // boolean flag = diviceService.updateNewDeviceWithGroup(form, sign, ipAddress);
                        // if (flag) {
                        //     return new JsonResultBean(JsonResultBean.SUCCESS);
                        // }
                    }
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT, deviceNumberBound);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除终端异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除设备", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的设备ids(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() throws BusinessException {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            ConfigForm c = null;
            for (int i = 0, n = item.length; i < n; i++) {
                c = configService.getIsBand("", item[i], "", "");
                if (c != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, deviceNumberBound);
                }
            }
            for (int i = 0; i < item.length; i++) {
                String deviceNumber = diviceService.findDeviceById(item[i]).getDeviceNumber();
                // 清除redis 终端--设备类型
                // RedisHelper.del(deviceNumber + "_deviceType", PublicVariable.REDIS_EIGHT_DATABASE);
            }
            // 获取访问服务器的客户端的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // diviceService.deleteDeviceGroupByBatch(item, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("批量删除终端异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改车辆
     */
    @ApiOperation(value = "根据id获取设备信息", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            Map<String, Object> resultMap = diviceService.findDeviceGroupById(id);
            String groupId = (String) resultMap.get("groupName");
            OrganizationLdap organization = userService.getOrgByUuid(groupId);
            resultMap.put("groupName", organization.getName());
            DeviceForm form = new DeviceForm();
            ConvertUtils.register(form, Date.class);
            BeanUtils.populate(form, resultMap);
            form.setGroupId(groupId);
            form.setInstallTimeStr(Converter.toString(form.getInstallTime(), "yyyy-MM-dd"));
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("修改终端弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @ApiOperation(value = "修改设备信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "设备id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "deviceType", value = "终端类型（1:交通部,2:GV320,3:TH)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "deviceNumber", value = "终端号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属企业id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final DeviceForm form,
        final BindingResult bindingResult) throws BusinessException {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 获取操作用户的IP
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    int sign = 1; // 区分删除还是修改
                    /*boolean flag = diviceService.updateNewDeviceWithGroup(form, sign, ipAddress);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }*/
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改终端异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 导出
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "导出设备", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {
        try {
            String filename = "终端信息列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            // diviceService.exportDevice(null, 1, response);
        } catch (Exception e) {
            log.error("导出终端信息异常", e);
        }
    }

    /**
     * 下载模板
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "下载设备导入模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {
        try {
            String filename = "终端信息列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            diviceService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载终端信息列表模板异常", e);
        }
    }

    @ApiOperation(value = "导入设备", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file)
        throws BusinessException {
        try {
            //获取访问服务器的客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // return diviceService.importDevice(file, ipAddress, request);
            return null;
        } catch (Exception e) {
            log.error("导入终端信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "检查终端号是否已经存在", notes = "")
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("deviceNumber") String deviceNumber) {
        try {
            DeviceInfo vt = diviceService.findByDevice(deviceNumber);
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("校验终端存在异常", e);
            return false;
        }
    }
}
