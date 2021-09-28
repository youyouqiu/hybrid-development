package com.zw.api.controller.modules;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.basicinfo.form.ProfessionalsGroupForm;
import com.zw.platform.domain.basicinfo.query.ProfessionalsQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.basicinfo.ProfessionalsService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.GetIpAddr;
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
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <p> Title: 从业人员controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年7月26日下午4:44:08
 */
@Controller
@RequestMapping("/swagger/m/professionals")
@Api(tags = { "从业人员管理" }, description = "从业人员相关api接口")
public class SwaggerProfessionalsController {
    private static Logger log = LogManager.getLogger(SwaggerProfessionalsController.class);

    @Autowired
    private ProfessionalsService professionalsService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private HttpServletRequest request;

    // 前端需要显示的列
    private static final String COLUMN_STR =
        "id,name,groupName,positionType,identity,jobNumber," + "cardNumber,gender,birthday,photograph,phone,email";

    @Auth
    /**
     * 分页查询
     */
    @ApiOperation(value = "分页查询从业人员列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupName", value = "组织id,查询某个组织下的从业人员", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final ProfessionalsQuery query) {
        try {
            if (query != null) {
                // 校验传入字段
                if (query.getPage() == null || query.getLimit() == null) { // page和limit不能为空
                    return new PageGridBean(PageGridBean.FAULT);
                }
                if (StringUtils.isNotBlank(query.getSimpleQueryParam())
                    && query.getSimpleQueryParam().length() > 20) { // 模糊搜索长度小于20
                    return new PageGridBean(PageGridBean.FAULT);
                }
                Page<Map<String, Object>> result = professionalsService.findProfessionalsWithGroup(query);
                List<Map<String, Object>> dataMap = result.getResult();
                // 遍历所有列名，若没有值，默认设置为""
                String[] column = COLUMN_STR.split(",");
                for (Map<String, Object> map : dataMap) {
                    for (String keyStr : column) {
                        if (!map.containsKey(keyStr)) {
                            map.put(keyStr, "");
                        }
                        // 从Ldap中查询出组织名称
                        if ("groupName".equals(keyStr) && !"".equals(map.get(keyStr))) {
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
            log.error("分页查询从业人员异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @Title: 添加从业人员
     * @author , @RequestParam(value = "image_file",
     * required = false) MultipartFile file, wangying
     */
    @ApiOperation(value = "新增从业人员", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "从业人员名称(不能重复，长度2-20)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属企业id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "positionType", value = "岗位类型(1--经理；2--技术人员；3--销售人员)", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "identity", value = "身份证号", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "jobNumber", value = "工号，长度不超过30", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "cardNumber", value = "卡号，长度不超过30", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "gender", value = "性别(1-男；2-女)", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "birthday", value = "出生年月，格式yyyy-MM-dd", required = false, paramType = "query",
            dataType = "date"),
        @ApiImplicitParam(name = "phone", value = "电话", required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "email", value = "邮箱", required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "photograph", value = "照片文件名", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addProfessionals(@Validated({ ValidGroupAdd.class }) final ProfessionalsForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                if (bindingResult.getFieldError("birthday") != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "出生年月输入错误，请输入正确的日期(格式yyyy-MM-dd)！");
                }
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // // 保存图片
                // String imgName = uploadImg(request,file);
                // if (StringUtils.isNotBlank(imgName)) {
                // form.setPhotograph(imgName);
                // }
                // 验证组织是否存在
                try {
                    if (StringUtils.isBlank(form.getGroupId())) {
                        return new JsonResultBean(JsonResultBean.FAULT, "所属企业不能为空！");
                    }
                    userService.getOrgByUuid(form.getGroupId());
                } catch (Exception e) {
                    return new JsonResultBean(JsonResultBean.FAULT, "所属企业不存在！");
                }
                // 验证身份证号
                if (StringUtils.isNotBlank(form.getIdentity()) && RegexUtils.checkIdentity(form.getIdentity())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "不是正确的身份证号");
                }
                // 验证电话
                if (StringUtils.isNotBlank(form.getPhone()) && !RegexUtils.checkMobile(form.getPhone())
                    && !RegexUtils.checkPhone(form.getPhone())) { // 校验sim卡必须为手机/电话
                    return new JsonResultBean(JsonResultBean.FAULT, "电话必须为手机/电话！");
                }
                // 验证从业人员名称不重复
                if (professionalsService.findProfessionalsByName(form.getName()) != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "从业人员已存在！");
                }
                ProfessionalsGroupForm proGroupForm = new ProfessionalsGroupForm();
                // 组装关联表
                proGroupForm.setProfessionalsId(form.getId());
                proGroupForm.setGroupId(form.getGroupId());
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // boolean flag = professionalsService.addProfessionalsWithGroup(form, proGroupForm, ipAddress);
                boolean flag = false;
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }

            }
        } catch (Exception e) {
            log.error("新增从业人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 根据id删除 从业人员
     */
    @ApiOperation(value = "根据id删除从业人员", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (professionalsService.findProfessionalsById(id) == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "该从业人员不存在！");
            }
            int isBandOrg = 0;
            isBandOrg = professionalsService.getIsBandGroup(id);
            // 判断是否存在绑定关系
            try {
                if (isBandOrg > 0) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该从业人员已绑定车辆，不能删除！");
                }
            } catch (Exception e) {
                log.error("error", e);
            }
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // return professionalsService.deleteProfessionalsById(id, ipAddress);
            return null;
        } catch (Exception e) {
            log.error("删除从业人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除从业人员", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的从业人员ids(用逗号隔开)", required = true, paramType = "query",
        dataType = "Stirng")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                int isBandOrg = 0;
                isBandOrg = professionalsService.getIsBandGroupByBatch(ids);
                if (isBandOrg > 0) {
                    return new JsonResultBean(JsonResultBean.FAULT, "从业人员已绑定车辆，不能删除！");
                }
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // return professionalsService.deleteProfessionalsByBatch(ids, ipAddress);
                return null;
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除从业人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改车辆
     */
    @ApiOperation(value = "根据id查询从业人员详细信息", notes = "用于编辑", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            Map<String, Object> resultMap = professionalsService.findProGroupById(id);
            String groupId = (String) resultMap.get("groupName");
            OrganizationLdap organization = userService.getOrgByUuid(groupId);
            resultMap.put("groupName", organization.getName());
            // 重组返回结果
            ProfessionalsForm form = new ProfessionalsForm();
            try {
                ConvertUtils.register(form, Date.class);
                BeanUtils.populate(form, resultMap);
                form.setGroupId(groupId);
            } catch (IllegalAccessException e) {
                log.error("error", e);
            } catch (InvocationTargetException e) {
                log.error("error", e);
            }
            return new JsonResultBean(form);
        } catch (Exception e) {
            log.error("查询从业人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "保存编辑的从业人员信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "从业人员id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "name", value = "从业人员名称(不能重复，长度2-20)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属企业id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "positionType", value = "岗位类型(1--经理；2--技术人员；3--销售人员)", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "identity", value = "身份证号", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "jobNumber", value = "工号，长度不超过30", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "cardNumber", value = "卡号，长度不超过30", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "gender", value = "性别(1-男；2-女)", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "birthday", value = "出生年月，格式yyyy-MM-dd", required = false, paramType = "query",
            dataType = "date"),
        @ApiImplicitParam(name = "phone", value = "电话", required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "email", value = "邮箱", required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "photograph", value = "照片文件名", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) final ProfessionalsForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                if (bindingResult.getFieldError("birthday") != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "出生年月输入错误，请输入正确的日期(格式yyyy-MM-dd)！");
                }
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // // 保存图片
                // MultipartFile file = form.getFile();
                // if (file != null){
                // String imgName = uploadImg(request,file);
                // if (StringUtils.isNotBlank(imgName)) {
                // form.setPhotograph(imgName);
                // }
                // }
                // 验证从业人员是否存在
                if (professionalsService.findProfessionalsById(form.getId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "从业人员不存在！");
                }
                // 验证组织是否存在
                try {
                    if (StringUtils.isBlank(form.getGroupId())) {
                        return new JsonResultBean(JsonResultBean.FAULT, "所属企业不能为空！");
                    }
                    userService.getOrgByUuid(form.getGroupId());
                } catch (Exception e) {
                    return new JsonResultBean(JsonResultBean.FAULT, "所属企业不存在！");
                }
                // 验证身份证号
                if (StringUtils.isNotBlank(form.getIdentity()) && !RegexUtils.checkIdentity(form.getIdentity())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "不是正确的身份证号");
                }
                // 验证电话
                if (StringUtils.isNotBlank(form.getPhone()) && !RegexUtils.checkMobile(form.getPhone())
                    && !RegexUtils.checkPhone(form.getPhone())) { // 校验sim卡必须为手机/电话
                    return new JsonResultBean(JsonResultBean.FAULT, "电话必须为手机/电话！");
                }
                // 验证从业人员名称不重复
                if (professionalsService.findProfessionalsForNameRep(form.getId(), form.getName()) != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "从业人员已存在！");
                }
                ProfessionalsGroupForm proGroupForm = new ProfessionalsGroupForm();
                proGroupForm.setProfessionalsId(form.getId());
                proGroupForm.setGroupId(form.getGroupId());
                boolean flag = professionalsService.updateProGroupByProId(form, proGroupForm);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
        } catch (Exception e) {
            log.error("修改从业人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 导出
     */
    @ApiOperation(value = "导出从业人员", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            String filename = "从业人员列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            professionalsService.exportProfessionals(null, 1, response);
            // return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("导出从业人员列表异常", e);
        }
    }

    /**
     * 下载模板
     */
    @ApiOperation(value = "下载从业人员导入的模板", notes = "用于导入", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            String filename = "从业人员列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            professionalsService.generateTemplate(response);
            // return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("下载从业人员列表异常", e);
        }
    }

    /**
     * @return String
     * @Title: 导入
     * @author wangying
     */

    @ApiOperation(value = "导入从业人员", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);

            // return professionalsService.importProfessionals(file, ipAddress);
            return null;
        } catch (Exception e) {
            log.error("导入从业人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 组织结构树数据 @Title: list @return @return List<Group>
     */
    @ApiOperation(value = "获取组织树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "isOrg", value = "是否显示组织树结构的最上级组织(0：不显示(用于新增编辑页面); 1:显示(用于展示页面))", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTree(String isOrg) {
        try {
            // 校验参数值
            if (!"0".equals(isOrg) && !"1".equals(isOrg)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数值错误！");
            }
            if ("admin".equals(SystemHelper.getCurrentUsername())) {
                // 获取当前用户所在组织及下级组织
                String orgId = userService.getOrgIdByUser();
                List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
                JSONArray result = new JSONArray();
                for (OrganizationLdap group : orgs) {
                    if ((isOrg == null || "0".equals(isOrg)) && "ou=organization".equals(group.getCid())) {
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("id", group.getCid());
                    obj.put("pId", group.getPid());
                    obj.put("name", group.getName());
                    obj.put("type", "group");
                    result.add(obj);
                }
                return new JsonResultBean(result.toJSONString());
            } else {
                String userId = SystemHelper.getCurrentUser().getId().toString();
                /*String key = RedisHelper.buildKey(userId, "zw_c_org", "tree");
                if (!RedisHelper.isContainsKey(key, PublicVariable.REDIS_TEN_DATABASE)) {
                    // 获取当前用户所在组织及下级组织
                    String orgId = userService.getOrgIdByUser();
                    // List<OrganizationLdap> orgs = userService.getAllOrganization();
                    List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
                    JSONArray result = new JSONArray();
                    for (OrganizationLdap group : orgs) {
                        if ((isOrg == null || "0".equals(isOrg)) && "ou=organization".equals(group.getCid())) {
                            continue;
                        }
                        JSONObject obj = new JSONObject();
                        obj.put("id", group.getCid());
                        obj.put("pId", group.getPid());
                        obj.put("name", group.getName());
                        obj.put("type", "group");
                        obj.put("uuid", group.getUuid());
                        result.add(obj);
                    }
                    String treeStr = result.toJSONString();
                    RedisHelper.set(key, VehicleStatus.ONE_HOUR, treeStr, PublicVariable.REDIS_TEN_DATABASE);
                }
                String resultStr = RedisHelper.get(key, PublicVariable.REDIS_TEN_DATABASE);*/
                return new JsonResultBean("JSON.parseArray(resultStr)");
            }
        } catch (Exception e) {
            log.error("获取组织结构树异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "上传照片", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/upload_img" }, method = RequestMethod.POST)
    @ResponseBody
    public JSONObject uploadImg(MultipartFile file) {
        String newName = "";
        JSONObject resultMap = new JSONObject();
        try {
            // 图片
            if (!file.isEmpty()) {
                // 文件保存路径
                String filePath = request.getSession().getServletContext().getRealPath("/") + "upload/";
                File saveFile = new File(filePath);
                if (!saveFile.exists() && saveFile.mkdirs()) {
                    return resultMap;
                }
                // 获取文件后缀名
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                newName = (System.currentTimeMillis()) + "" + new Random().nextInt(100) + suffix;
                // 转存文件
                file.transferTo(new File(filePath + newName));
            }
            resultMap.put("imgName", newName);
            return resultMap;
        } catch (Exception e) {
            log.error("上传照片异常", e);
            return resultMap;
        }
    }

    @ApiOperation(value = "根据从业人员名字查询改从业人员是否已存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "name", value = "从业人员名称", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean repetition(@RequestParam("name") String name) {
        try {
            ProfessionalDO vt = professionalsService.findProfessionalsByName(name);
            if (vt == null) {
                return new JsonResultBean(true);
            } else {
                return new JsonResultBean(false);
            }
        } catch (Exception e) {
            log.error("查询改从业人员存在异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }
}
