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
@Api(tags = { "????????????" }, description = "????????????api")
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

    // ????????????????????????
    private static final String COLUMN_STR = "id,deviceNumber,deviceName,isStart,deviceType,channelNumber,iSvideo,"
        + "barCode,manuFacturer,flag,createDataTime,createDataUsername,updateDataTime,updateDataUsername,isRegister,"
        + "brand,groupName,installTime,installTimeStr";

    /**
     * ????????????
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "??????", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "??????????????????", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "???????????????????????????????????????????????????????????????", required = false,
            paramType = "query", dataType = "string"), })
    public PageGridBean getListPage(final DeviceQuery query) {
        Page<Map<String, Object>> result = new Page<>();
        try {
            if (query != null) {
                List<Map<String, Object>> dataMap = new ArrayList<>();
                boolean isNull = false; // ???????????????????????????????????????????????????
                if (StringUtils.isBlank(query.getSimpleQueryParam())) { // ??????????????????
                    String userId = SystemHelper.getCurrentUser().getId().toString();
                    // ??????redis????????????????????????key(user+table+type)
                    /*String key = RedisHelper.buildKey(userId, "zw_m_device_info", "list");
                    if (!RedisHelper.isContainsKey(key, PublicVariable.REDIS_MYSQL_DATABASE)) {
                        // ???redis??????????????????key??????????????????????????????????????????redis???
                        List<Map<String, Object>> sqlList = diviceService.findDeviceByUser(query);
                        RedisHelper.rpush(key, sqlList, PublicVariable.REDIS_MYSQL_DATABASE);
                        RedisHelper.setExpire(key, VehicleStatus.ONE_HOUR, PublicVariable.REDIS_MYSQL_DATABASE);
                    }*/
                    // ???redis????????????????????????
                    // result = RedisQueryUtil.queryPageList(key, PublicVariable.REDIS_MYSQL_DATABASE, query);
                    dataMap = result.getResult();
                    if (dataMap == null || dataMap.isEmpty()) {
                        isNull = true;
                    }
                }
                if (StringUtils.isNotBlank(query.getSimpleQueryParam()) || isNull) { // ?????????????????????????????????????????????,????????????????????????
                    result = diviceService.findDeviceByUser(query);
                    dataMap = result.getResult();
                }
                // ???????????????????????????????????????????????????""
                String[] column = COLUMN_STR.split(",");
                for (Map<String, Object> map : dataMap) {
                    for (String keyStr : column) {
                        if (!map.containsKey(keyStr)) {
                            map.put(keyStr, "");
                        }
                        // ???Ldap????????????????????????
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
            log.error("??????????????????????????????", e);
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
     * @Title: ????????????
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deviceType", value = "???????????????1:?????????,2:GV320,3:TH)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "deviceNumber", value = "?????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "????????????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addDevice(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final DeviceForm form,
        final BindingResult bindingResult) {
        try {
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                DeviceInfo vt = diviceService.findByDevice(form.getDeviceNumber());
                if (vt != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
                }

                DeviceGroupForm groupForm = new DeviceGroupForm();
                groupForm.setDeviceId(form.getId());
                if (!Converter.toBlank(form.getGroupId()).equals("")) {
                    groupForm.setGroupId(form.getGroupId());
                    try {
                        if (userService.getOrgByUuid(form.getGroupId()) == null) { // ??????????????????????????????????????????
                            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????");
                        }
                    } catch (Exception e) {
                        return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????");
                    }
                } else {
                    groupForm.setGroupId(Converter.toBlank(userService.getOrgIdByUser()));
                }
                // ????????????????????????????????????IP??????
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }

    }

    /**
     * ??????id?????? ??????
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) throws BusinessException {
        try {
            if (id != null) {
                // ??????????????????????????????
                ConfigForm config = configService.getIsBand("", id, "", "");
                if (config == null) { // ?????????
                    // ??????ID????????????
                    DeviceInfo div = diviceService.findDeviceById(id);
                    if (div != null) {
                        DeviceForm form = new DeviceForm();
                        form.setId(id);
                        form.setFlag(0);
                        // ????????????????????????????????????IP??????
                        String ipAddress = new GetIpAddr().getIpAddr(request);
                        int sign = 0; // ?????????????????????
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }

    }

    /**
     * ????????????
     */
    @ApiOperation(value = "??????ids??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "?????????????????????ids(???????????????)", required = true, paramType = "query",
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
                // ??????redis ??????--????????????
                // RedisHelper.del(deviceNumber + "_deviceType", PublicVariable.REDIS_EIGHT_DATABASE);
            }
            // ????????????????????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // diviceService.deleteDeviceGroupByBatch(item, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "??????id??????????????????", notes = "??????", authorizations = {
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
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "deviceType", value = "???????????????1:?????????,2:GV320,3:TH)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "deviceNumber", value = "?????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "????????????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final DeviceForm form,
        final BindingResult bindingResult) throws BusinessException {
        try {
            if (form != null) {
                // ????????????
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // ?????????????????????IP
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    int sign = 1; // ????????????????????????
                    /*boolean flag = diviceService.updateNewDeviceWithGroup(form, sign, ipAddress);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }*/
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }

    }

    /**
     * ??????
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {
        try {
            String filename = "??????????????????";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            // diviceService.exportDevice(null, 1, response);
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
    }

    /**
     * ????????????
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {
        try {
            String filename = "????????????????????????";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            diviceService.generateTemplate(response);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
    }

    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file)
        throws BusinessException {
        try {
            //????????????????????????????????????IP??????
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // return diviceService.importDevice(file, ipAddress, request);
            return null;
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    @ApiOperation(value = "?????????????????????????????????", notes = "")
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
            log.error("????????????????????????", e);
            return false;
        }
    }
}
