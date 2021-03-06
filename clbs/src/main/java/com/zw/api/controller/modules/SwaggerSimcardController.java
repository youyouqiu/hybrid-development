package com.zw.api.controller.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.form.SimGroupForm;
import com.zw.platform.domain.basicinfo.form.SimcardForm;
import com.zw.platform.domain.basicinfo.query.SimcardQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.service.basicinfo.SimcardService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RegexUtils;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p> Title: sim???controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: fanlu
 * @date 2017???2???10?????????9:37
 */
@RestController
@RequestMapping("/swagger/m/basicinfo/equipment/simcard")
@Api(tags = { "SIM?????????" }, description = "sim?????????api")
public class SwaggerSimcardController {
    private static Logger log = LogManager.getLogger(SwaggerSimcardController.class);

    @Autowired
    private SimcardService simcardService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sim.number.bound}")
    private String simNumberBound;

    private static final String LIST_PAGE = "modules/basicinfo/equipment/simcard/list";

    private static final String ADD_PAGE = "modules/basicinfo/equipment/simcard/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/simcard/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/equipment/simcard/import";

    // ????????????????????????
    private static final String COLUMN_STR = "id,simcardNumber,operator,isStart,openCardTime,capacity,networkType,"
        + "simFlow,useFlow,flag,createDataTime,createDataUsername,updateDataTime,"
        + "updateDataUsername,alertsFlow,endTime,isRegister,brand,groupName";

    /**
     * ????????????
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "??????sim?????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "??????", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "??????????????????", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "??????sim????????????????????????????????????", required = false, paramType = "query",
            dataType = "string"), })
    public PageGridBean getListPage(final SimcardQuery query) {
        try {
            if (query != null) {
                Page<Map<String, Object>> result = (Page<Map<String, Object>>) simcardService.findSimCardByUser(query);
                List<Map<String, Object>> dataMap = result.getResult();
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
            log.error("????????????sim?????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author fanlu
     * @Title: ??????sim?????????
     */
    @ApiOperation(value = "??????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simcardNumber", value = "sim?????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "????????????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final SimcardForm form,
        final BindingResult bindingResult) {
        try {
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                SimcardInfo vt = simcardService.findBySIMCard(form.getSimcardNumber());

                if (vt != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "sim?????????????????????");
                }
                // ??????sim??????
                if (StringUtils.isNotBlank(form.getSimcardNumber()) && !RegexUtils.checkMobile(form.getSimcardNumber())
                    && !RegexUtils.checkPhone(form.getSimcardNumber())) { // ??????sim??????????????????/??????
                    return new JsonResultBean(JsonResultBean.FAULT, "sim??????????????????/?????????");
                }

                if (!StringUtil.isNullOrBlank(form.getOperator())) {
                    if ((!"????????????".equals(form.getOperator())) && (!"????????????".equals(form.getOperator())) && (!"????????????".equals(
                        form.getOperator()))) {
                        return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????????????????????????????");
                    }
                }
                SimGroupForm groupForm = new SimGroupForm();
                groupForm.setSimcardId(form.getId());
                if (!Converter.toBlank(form.getGroupId()).equals("")) {
                    groupForm.setGroupId(form.getGroupId());
                    try {
                        if (userService.getOrgByUuid(form.getGroupId()) == null) {
                            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
                        }
                    } catch (Exception e) {
                        return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
                    }

                } else {
                    groupForm.setGroupId(Converter.toBlank(userService.getOrgUuidByUser()));
                }
                if (form.getOpenCardTime() != null && form.getEndTime() != null) {
                    if (form.getEndTime().getTime() < form.getOpenCardTime().getTime()) {
                        return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????");
                    }
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // boolean flag = simcardService.addSimcardWithGroup(form, groupForm, ipAddress);
                boolean flag = false;
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }

            }
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }

    }

    /**
     * ??????id??????sim???
     */
    @ApiOperation(value = "??????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                ConfigForm c = configService.getIsBand("", "", id, ""); // ??????SIM???????????????
                if (c == null) { // SIM????????????
                    SimcardForm form = new SimcardForm();
                    form.setId(id);
                    form.setFlag(0);
                    // ????????????????????????????????????IP??????
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // boolean flag = simcardService.updateSimcardWithGroup(form, ipAddress);
                    boolean flag = false;
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT, simNumberBound);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "??????ids????????????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "???????????????sim???ids(???????????????)", required = true, paramType = "query",
        dataType = "Stirng")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");

            ConfigForm c = null;
            for (int i = 0, n = item.length; i < n; i++) {
                c = configService.getIsBand("", "", item[i], "");
                if (c != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, simNumberBound);
                }
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // simcardService.deleteSimcardWithGroupByBatch(item, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????sim???
     */
    @ApiOperation(value = "??????id??????sim?????????", notes = "??????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            Map<String, Object> resultMap = simcardService.findSimcardGroupById(id);
            String groupId = (String) resultMap.get("groupName");
            OrganizationLdap organization = userService.getOrgByUuid(groupId);
            resultMap.put("groupName", organization.getName());
            SimcardForm form = new SimcardForm();
            ConvertUtils.register(form, Date.class);
            BeanUtils.populate(form, resultMap);
            form.setGroupId(groupId);
            return new JsonResultBean(form);
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author fanlu
     * @Title: ??????sim???
     */
    @ApiOperation(value = "??????sim?????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "sim???id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "simcardNumber", value = "sim?????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "????????????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final SimcardForm form,
        final BindingResult bindingResult) {
        try {
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                SimcardInfo vt = simcardService.isExist(form.getId(), form.getSimcardNumber());

                if (vt != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "sim?????????????????????");
                }
                // ??????sim??????
                if (StringUtils.isNotBlank(form.getSimcardNumber()) && !RegexUtils.checkMobile(form.getSimcardNumber())
                    && !RegexUtils.checkPhone(form.getSimcardNumber())) { // ??????sim??????????????????/??????
                    return new JsonResultBean(JsonResultBean.FAULT, "sim??????????????????/?????????");
                }
                try {
                    if (userService.getOrgByUuid(form.getGroupId()) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
                    }
                } catch (Exception e) {
                    return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
                }
                if (form.getOpenCardTime() != null && form.getEndTime() != null) {
                    if (form.getEndTime().getTime() < form.getOpenCardTime().getTime()) {
                        return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????");
                    }
                }
                if (!StringUtil.isNullOrBlank(form.getOperator())) {
                    if ((!"????????????".equals(form.getOperator())) && (!"????????????".equals(form.getOperator())) && (!"????????????".equals(
                        form.getOperator()))) {
                        return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????????????????????????????");
                    }
                }
                SimGroupForm groupForm = new SimGroupForm();
                groupForm.setSimcardId(form.getId());
                groupForm.setGroupId(form.getGroupId());
                // ????????????????????????????????????IP??????
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // boolean flag = simcardService.updateSimcardWithGroup(form, ipAddress);
                boolean flag = false;
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }

    }

    /**
     * ??????
     */
    @ApiOperation(value = "??????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            String filename = "SIM?????????";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            // simcardService.exportSimcard(null, 1, response);
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
        }
    }

    /**
     * ????????????
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "??????sin???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            String filename = "???????????????????????????";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            simcardService.generateTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????", e);
        }
    }

    @ApiOperation(value = "??????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSimcard(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // ????????????????????????????????????IP??????
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // return simcardService.importSimcard(file, ipAddress, request);
            return null;
        } catch (Exception e) {
            log.error("??????sin???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    @ApiOperation(value = "??????sim???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("simcardNumber") String simcardNumber) {
        try {
            SimcardInfo vt = simcardService.findBySIMCard(simcardNumber);

            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("??????sim?????????????????????", e);
            return false;
        }
    }
}
