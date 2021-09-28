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
 * <p> Title: sim卡controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: fanlu
 * @date 2017年2月10日上午9:37
 */
@RestController
@RequestMapping("/swagger/m/basicinfo/equipment/simcard")
@Api(tags = { "SIM卡管理" }, description = "sim卡相关api")
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

    // 前端需要显示的列
    private static final String COLUMN_STR = "id,simcardNumber,operator,isStart,openCardTime,capacity,networkType,"
        + "simFlow,useFlow,flag,createDataTime,createDataUsername,updateDataTime,"
        + "updateDataUsername,alertsFlow,endTime,isRegister,brand,groupName";

    /**
     * 分页查询
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "获取sim卡列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照sim卡号、车牌号进行模糊搜索", required = false, paramType = "query",
            dataType = "string"), })
    public PageGridBean getListPage(final SimcardQuery query) {
        try {
            if (query != null) {
                Page<Map<String, Object>> result = (Page<Map<String, Object>>) simcardService.findSimCardByUser(query);
                List<Map<String, Object>> dataMap = result.getResult();
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
            log.error("分页查询sim卡信息异常异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author fanlu
     * @Title: 新增sim卡信息
     */
    @ApiOperation(value = "添加sim卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simcardNumber", value = "sim卡编号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属企业id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final SimcardForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                SimcardInfo vt = simcardService.findBySIMCard(form.getSimcardNumber());

                if (vt != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "sim卡编号已存在！");
                }
                // 校验sim卡号
                if (StringUtils.isNotBlank(form.getSimcardNumber()) && !RegexUtils.checkMobile(form.getSimcardNumber())
                    && !RegexUtils.checkPhone(form.getSimcardNumber())) { // 校验sim卡必须为手机/电话
                    return new JsonResultBean(JsonResultBean.FAULT, "sim卡必须为手机/电话！");
                }

                if (!StringUtil.isNullOrBlank(form.getOperator())) {
                    if ((!"中国移动".equals(form.getOperator())) && (!"中国联通".equals(form.getOperator())) && (!"中国电信".equals(
                        form.getOperator()))) {
                        return new JsonResultBean(JsonResultBean.FAULT, "运营商输入错误，只能输入中国移动、中国联通、中国电信！");
                    }
                }
                SimGroupForm groupForm = new SimGroupForm();
                groupForm.setSimcardId(form.getId());
                if (!Converter.toBlank(form.getGroupId()).equals("")) {
                    groupForm.setGroupId(form.getGroupId());
                    try {
                        if (userService.getOrgByUuid(form.getGroupId()) == null) {
                            return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                        }
                    } catch (Exception e) {
                        return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                    }

                } else {
                    groupForm.setGroupId(Converter.toBlank(userService.getOrgUuidByUser()));
                }
                if (form.getOpenCardTime() != null && form.getEndTime() != null) {
                    if (form.getEndTime().getTime() < form.getOpenCardTime().getTime()) {
                        return new JsonResultBean(JsonResultBean.FAULT, "到期时间要大于激活日期！");
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
            log.error("新增sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 根据id删除sim卡
     */
    @ApiOperation(value = "删除sim卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                ConfigForm c = configService.getIsBand("", "", id, ""); // 判断SIM卡是否绑定
                if (c == null) { // SIM卡未绑定
                    SimcardForm form = new SimcardForm();
                    form.setId(id);
                    form.setFlag(0);
                    // 获取访问服务器的客户端的IP地址
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
            log.error("删除sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除sim卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的sim卡ids(用逗号隔开)", required = true, paramType = "query",
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
            log.error("批量删除sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改sim卡
     */
    @ApiOperation(value = "根据id获取sim卡信息", notes = "修改", authorizations = {
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
            log.error("修改sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author fanlu
     * @Title: 修改sim卡
     */
    @ApiOperation(value = "修改sim卡信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "sim卡id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "simcardNumber", value = "sim卡编号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属企业id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final SimcardForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                SimcardInfo vt = simcardService.isExist(form.getId(), form.getSimcardNumber());

                if (vt != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "sim卡编号已存在！");
                }
                // 校验sim卡号
                if (StringUtils.isNotBlank(form.getSimcardNumber()) && !RegexUtils.checkMobile(form.getSimcardNumber())
                    && !RegexUtils.checkPhone(form.getSimcardNumber())) { // 校验sim卡必须为手机/电话
                    return new JsonResultBean(JsonResultBean.FAULT, "sim卡必须为手机/电话！");
                }
                try {
                    if (userService.getOrgByUuid(form.getGroupId()) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                    }
                } catch (Exception e) {
                    return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                }
                if (form.getOpenCardTime() != null && form.getEndTime() != null) {
                    if (form.getEndTime().getTime() < form.getOpenCardTime().getTime()) {
                        return new JsonResultBean(JsonResultBean.FAULT, "到期时间要大于激活日期！");
                    }
                }
                if (!StringUtil.isNullOrBlank(form.getOperator())) {
                    if ((!"中国移动".equals(form.getOperator())) && (!"中国联通".equals(form.getOperator())) && (!"中国电信".equals(
                        form.getOperator()))) {
                        return new JsonResultBean(JsonResultBean.FAULT, "运营商输入错误，只能输入中国移动、中国联通、中国电信！");
                    }
                }
                SimGroupForm groupForm = new SimGroupForm();
                groupForm.setSimcardId(form.getId());
                groupForm.setGroupId(form.getGroupId());
                // 获取访问服务器的客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // boolean flag = simcardService.updateSimcardWithGroup(form, ipAddress);
                boolean flag = false;
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 导出
     */
    @ApiOperation(value = "导出sim卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            String filename = "SIM卡列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            // simcardService.exportSimcard(null, 1, response);
        } catch (Exception e) {
            log.error("导出sim卡信息异常", e);
        }
    }

    /**
     * 下载模板
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "下载sin卡导入模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            String filename = "终端手机号列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            simcardService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载模板异常", e);
        }
    }

    @ApiOperation(value = "导入sim卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSimcard(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // 获取访问服务器的客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // return simcardService.importSimcard(file, ipAddress, request);
            return null;
        } catch (Exception e) {
            log.error("导入sin卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "检查sim卡编号是否已经存在", authorizations = {
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
            log.error("检查sim卡编号存在异常", e);
            return false;
        }
    }
}
