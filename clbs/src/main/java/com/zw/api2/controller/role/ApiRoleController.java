package com.zw.api2.controller.role;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerRoleForm;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.form.RoleForm;
import com.zw.platform.domain.core.form.RoleResourceForm;
import com.zw.platform.domain.core.query.RoleQuery;
import com.zw.platform.service.core.ResourceService;
import com.zw.platform.service.core.RoleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import jodd.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.ldap.LdapName;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ?????? RoleController
 */
@Controller
@RequestMapping("api/c/role")
@Api(tags = { "????????????_dev" }, description = "??????????????????api??????")
public class ApiRoleController {
    private static Logger log = LogManager.getLogger(ApiRoleController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;
    @Value("${requisite.null}")
    private String requisiteNull;
    @Value("${role.exist}")
    private String roleExist;
    @Value("${role.user.delete}")
    private String roleUserDelete;
    @Value("${role.user.edit}")
    private String roleUserEdit;

    /**
     * ??????????????????
     */
    @Value("${experience.role.id}")
    private String experienceRoleId;

    /**
     * ????????????(??????)
     */
    @ApiOperation(value = "????????????(??????)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "???????????????????????????????????????????????????????????????????????????", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "??????????????????", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "????????????????????????????????????", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(RoleQuery query) {
        Page<Group> rolePage = new Page<>();
        try {
            List<Group> roles = userService.queryRoleList(query.getSimpleQueryParam());
            if (roles != null && !roles.isEmpty()) {
                List<Group> roleList = userService.getCurRole(roles);
                // ??????page
                int curPage = query.getPage().intValue(); // ????????????
                int pageSize = query.getLimit().intValue(); // ????????????
                int pages = (roleList.size() - 1) / pageSize + 1; // ?????????
                int start = (curPage - 1) * pageSize; // ????????????
                int end = curPage * pageSize > roleList.size() ? (roleList.size() - 1) : (curPage * pageSize - 1);
                for (int i = start; i <= end; i++) {
                    Group group = roleList.get(i);
                    if (experienceRoleId.equals(group.getName())) { //??????????????????????????????????????????????????????
                        group.setDelFlag(false);
                    }
                    rolePage.add(roleList.get(i));
                }
                rolePage.setPages(pages);
                rolePage.setPageSize(pageSize);
                rolePage.setTotal(roleList.size());
            } else {
                rolePage.setPages(0);
                rolePage.setPageSize(query.getLimit().intValue());
                rolePage.setTotal(0);
            }

            return new PageGridBean(query, rolePage, true);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        } finally {
            rolePage.close();
        }
    }

    /**
     * ??????tree
     * @author wangying
     */
    @ApiOperation(value = "??????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "roleId", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/permissionTree", method = RequestMethod.POST)
    @ResponseBody
    public List getPermissionTree(String roleId) {
        return roleService.generateTree(roleId);
    }

    @ApiOperation(value = "???????????????id??????????????????????????????????????????", notes = "??????json??????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "roleId", value = "???????????????????????????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/choicePermissionTree", method = RequestMethod.POST)
    @ResponseBody
    public String choicePermissionTree(String roleId) {
        JSONArray treeList = roleService.generateTree(roleId);
        return treeList.toJSONString();
    }

    /**
     * ?????? ??????
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "roleName", value = "????????????", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "permissionTree",
            value = "??????????????????json???,??????[{'id':'b46de828-6a8e-11e6-8b77-86f30ca893d3','edit':false},"
                + "{...}] ??????id?????????id???edit???????????????", required = true, paramType = "query", dataType = "string",
            defaultValue = "[]"),
        @ApiImplicitParam(name = "description", value = "????????????", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@RequestParam("permissionTree") final String permissionTree,
        @RequestParam("roleName") final String roleName, @RequestParam("description") final String description,
        final BindingResult bindingResult) {
        try {
            RoleForm form = new RoleForm();
            form.setDescription(description);
            form.setRoleName(roleName);
            JSONObject msg = new JSONObject();
            msg.put("flag", 2); // 0????????? 1??? ?????? 2???????????????
            // ????????????
            if (bindingResult.hasErrors()) {
                String errorMsg = SpringBindingResultWrapper.warpErrors(bindingResult);
                return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
            } else {
                // ????????????
                if (StringUtil.isEmpty(form.getRoleName())) {
                    msg.put("errMsg", requisiteNull);
                    return new JsonResultBean(msg);
                }
                // ???????????????????????????
                List<Group> groupByName = userService.getGroupByName(form.getRoleName());
                if (groupByName != null) {
                    msg.put("errMsg", roleExist);
                    return new JsonResultBean(msg);
                }
                // ??????cn
                String cn = "ROLE_" + UUID.randomUUID();
                // ??????id
                String roleId = "cn=" + cn + ",ou=Groups";
                List<RoleResourceForm> formList = new ArrayList<>();
                // ?????????????????????????????????????????????????????????????????? gfw 20180904
                // ????????????????????????
                if (permissionTree != null && !permissionTree.equals("")) {
                    JSONArray resourceArray = JSON.parseArray(permissionTree);
                    for (Object obj : resourceArray) {
                        RoleResourceForm roleResource = new RoleResourceForm();
                        String id = (String) ((JSONObject) obj).get("id");
                        // ????????????
                        boolean edit = (boolean) ((JSONObject) obj).get("edit");
                        roleResource.setRoleId(roleId);
                        roleResource.setResourceId(id);
                        if (edit) {
                            roleResource.setEditable(1);
                        } else {
                            roleResource.setEditable(0);
                        }
                        formList.add(roleResource);
                    }
                }
                if (!formList.isEmpty() && userService.compareUserForm(SystemHelper.getCurrentUser().getId().toString(),
                    formList)) {
                    return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
                }

                // ldap ????????????
                Group group = new Group();
                LdapName ldapName = LdapUtils.newLdapName(roleId);
                group.setId(ldapName);
                group.setName(cn);
                group.setDescription(form.getDescription());
                group.setRoleName(form.getRoleName());
                // ?????????????????????IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                userService.addRole(group, ipAddress);
                if (!formList.isEmpty()) {
                    roleService.addRoleResourceByBatch(formList);
                }

                msg.put("flag", 1);
                msg.put("errMsg", "???????????????");

                // ??????????????????????????????????????????admin??????  gfw 20180904
                String userId = SystemHelper.getCurrentUser().getId().toString();
                // ?????? ???????????? gfw 20180904
                userService.addAllotRole(userId, cn, ipAddress);
                // admin ???????????? gfw 20180904
                String admin = userService.getUserDetails("admin").getId().toString();
                userService.addAllotRole(admin, cn, ipAddress);
                return new JsonResultBean(msg);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@ApiParam(value = "??????id", required = true) @PathVariable final String id) {
        try {
            JSONObject data = new JSONObject();
            data.put("result", userService.getGroupById(id));
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????
     */
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "name", value = "??????ldap??????cn??????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "roleName", value = "????????????", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "permissionEditTree",
            value = "??????????????????json???,??????[{'id':'b46de828-6a8e-11e6-8b77-86f30ca893d3','edit':false},{...}]", required = true,
            paramType = "query", dataType = "string", defaultValue = "[]"),
        @ApiImplicitParam(name = "description", value = "????????????", required = false, paramType = "query",
            dataType = "string") })
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final SwaggerRoleForm swaggerForm,
        @RequestParam("permissionEditTree") final String permissionEditTree, final BindingResult bindingResult) {
        try {
            if (swaggerForm != null) {
                RoleForm form = new RoleForm();
                BeanUtils.copyProperties(swaggerForm, form);
                // ????????????
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // ?????????????????????IP??????
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // ????????????(ldap)
                    Group group = new Group();
                    BeanUtils.copyProperties(form, group);
                    group.setId(LdapUtils.newLdapName(form.getId()));
                    // ????????????: name = POWER_USER
                    String name = group.getName();
                    boolean adminRole = userService.isADMINRole();
                    // "POWER_USER"(?????????????????????)????????????admin????????????????????????
                    boolean powerUserFlag = "POWER_USER".equals(name) && !adminRole;
                    boolean superAdmin = "ROLE_ADMIN".equals(name);
                    if (powerUserFlag || superAdmin) {
                        return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????,??????????????????????????????");
                    } else {
                        return userService.updateGroup(form.getId(), group, permissionEditTree, ipAddress);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????tree
     * @return List
     * @author wangying
     */
    @ApiOperation(value = "?????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/resourceTree", method = RequestMethod.POST)
    @ResponseBody
    public String resourceTree() {
        List<Resource> resources = resourceService.findAll(); // ????????????
        // ???????????????????????????
        LdapName name = LdapUtils.newLdapName(
            SystemHelper.getCurrentUser().getId() + "," + userService.getBaseLdapPath().toString());
        List<Group> roles = (List<Group>) userService.findByMember(name);
        List<String> roleIds = new ArrayList<>();
        if (roles != null && !roles.isEmpty()) {
            for (Group role : roles) {
                roleIds.add(role.getId().toString());
            }
        }
        // ???????????????????????????????????????
        List<String> curResources = resourceService.findResourceByRoleIds(roleIds);
        JSONArray result = new JSONArray();
        for (Resource resource : resources) {
            // ?????????????????????????????????
            JSONObject obj = new JSONObject();
            obj.put("id", resource.getId());
            obj.put("pId", resource.getParentId());
            obj.put("name", resource.getResourceName());
            obj.put("type", resource.getType());
            if (!curResources.contains(resource.getId())) {
                obj.put("chkDisabled", true);
            }
            if (resource.getType() == 1) {
                obj.put("nocheck", true);
            }
            result.add(obj);
        }
        return result.toJSONString();
    }

    /**
     * ??????id????????????
     * @author wangying
     */
    @ApiOperation(value = "??????id????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@ApiParam(value = "??????id") @PathVariable("id") final String id) {
        try {
            if (id != null) {
                // ?????????????????????IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // ldap????????????
                userService.deleteGroup(id, ipAddress);
            }

            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "??????ids??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "??????ids,?????????(;)??????", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            // ?????????????????????IP
            String ipAddr = new GetIpAddr().getIpAddr(request);// ????????????ip
            // ??????ldap???group
            userService.deleteGroup(items, ipAddr);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ???form???????????????String Date?????????Date???????????????????????????
     */
    @InitBinder
    protected final void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
