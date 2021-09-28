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
 * 角色 RoleController
 */
@Controller
@RequestMapping("api/c/role")
@Api(tags = { "角色管理_dev" }, description = "角色管理相关api接口")
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
     * 即刻体验角色
     */
    @Value("${experience.role.id}")
    private String experienceRoleId;

    /**
     * 查询角色(分页)
     */
    @ApiOperation(value = "查询角色(分页)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照角色名称进行模糊搜索", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(RoleQuery query) {
        Page<Group> rolePage = new Page<>();
        try {
            List<Group> roles = userService.queryRoleList(query.getSimpleQueryParam());
            if (roles != null && !roles.isEmpty()) {
                List<Group> roleList = userService.getCurRole(roles);
                // 组装page
                int curPage = query.getPage().intValue(); // 当前页数
                int pageSize = query.getLimit().intValue(); // 每页条数
                int pages = (roleList.size() - 1) / pageSize + 1; // 总页数
                int start = (curPage - 1) * pageSize; // 开始位置
                int end = curPage * pageSize > roleList.size() ? (roleList.size() - 1) : (curPage * pageSize - 1);
                for (int i = start; i <= end; i++) {
                    Group group = roleList.get(i);
                    if (experienceRoleId.equals(group.getName())) { //若匹配成功则是即刻体验角色，不能删除
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
            log.error("分页查询角色异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        } finally {
            rolePage.close();
        }
    }

    /**
     * 权限tree
     * @author wangying
     */
    @ApiOperation(value = "根据选角色查看其有的操作权限", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "roleId", value = "角色id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/permissionTree", method = RequestMethod.POST)
    @ResponseBody
    public List getPermissionTree(String roleId) {
        return roleService.generateTree(roleId);
    }

    @ApiOperation(value = "根据选角色id获取可选该角色资源权限树结构", notes = "返回json的串", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "roleId", value = "当前登录用户的角色id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/choicePermissionTree", method = RequestMethod.POST)
    @ResponseBody
    public String choicePermissionTree(String roleId) {
        JSONArray treeList = roleService.generateTree(roleId);
        return treeList.toJSONString();
    }

    /**
     * 新增 角色
     */
    @ApiOperation(value = "新增角色", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "roleName", value = "角色名称", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "permissionTree",
            value = "所选角色权限json串,例：[{'id':'b46de828-6a8e-11e6-8b77-86f30ca893d3','edit':false},"
                + "{...}] 其中id为菜单id，edit为可写权限", required = true, paramType = "query", dataType = "string",
            defaultValue = "[]"),
        @ApiImplicitParam(name = "description", value = "角色描述", paramType = "query", dataType = "string") })
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
            msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
            // 数据校验
            if (bindingResult.hasErrors()) {
                String errorMsg = SpringBindingResultWrapper.warpErrors(bindingResult);
                return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
            } else {
                // 校验数据
                if (StringUtil.isEmpty(form.getRoleName())) {
                    msg.put("errMsg", requisiteNull);
                    return new JsonResultBean(msg);
                }
                // 校验角色名是否重复
                List<Group> groupByName = userService.getGroupByName(form.getRoleName());
                if (groupByName != null) {
                    msg.put("errMsg", roleExist);
                    return new JsonResultBean(msg);
                }
                // 生成cn
                String cn = "ROLE_" + UUID.randomUUID();
                // 生成id
                String roleId = "cn=" + cn + ",ou=Groups";
                List<RoleResourceForm> formList = new ArrayList<>();
                // 检测用户新增角色所选权限是否在用户角色范围内 gfw 20180904
                // 用户所选可写权限
                if (permissionTree != null && !permissionTree.equals("")) {
                    JSONArray resourceArray = JSON.parseArray(permissionTree);
                    for (Object obj : resourceArray) {
                        RoleResourceForm roleResource = new RoleResourceForm();
                        String id = (String) ((JSONObject) obj).get("id");
                        // 是否可写
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

                // ldap 新增角色
                Group group = new Group();
                LdapName ldapName = LdapUtils.newLdapName(roleId);
                group.setId(ldapName);
                group.setName(cn);
                group.setDescription(form.getDescription());
                group.setRoleName(form.getRoleName());
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                userService.addRole(group, ipAddress);
                if (!formList.isEmpty()) {
                    roleService.addRoleResourceByBatch(formList);
                }

                msg.put("flag", 1);
                msg.put("errMsg", "保存成功！");

                // 新增角色自动分配到创建用户和admin用户  gfw 20180904
                String userId = SystemHelper.getCurrentUser().getId().toString();
                // 用户 添加角色 gfw 20180904
                userService.addAllotRole(userId, cn, ipAddress);
                // admin 添加角色 gfw 20180904
                String admin = userService.getUserDetails("admin").getId().toString();
                userService.addAllotRole(admin, cn, ipAddress);
                return new JsonResultBean(msg);
            }
        } catch (Exception e) {
            log.error("新增角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改角色页面
     */
    @ApiOperation(value = "修改角色页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@ApiParam(value = "角色id", required = true) @PathVariable final String id) {
        try {
            JSONObject data = new JSONObject();
            data.put("result", userService.getGroupById(id));
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("修改角色页面弹出异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改角色
     */
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "角色id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "name", value = "角色ldap里的cn名字", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "roleName", value = "角色名称", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "permissionEditTree",
            value = "所选角色权限json串,例：[{'id':'b46de828-6a8e-11e6-8b77-86f30ca893d3','edit':false},{...}]", required = true,
            paramType = "query", dataType = "string", defaultValue = "[]"),
        @ApiImplicitParam(name = "description", value = "角色描述", required = false, paramType = "query",
            dataType = "string") })
    @ApiOperation(value = "修改角色", authorizations = {
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
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 获取当前用户的IP地址
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // 修改角色(ldap)
                    Group group = new Group();
                    BeanUtils.copyProperties(form, group);
                    group.setId(LdapUtils.newLdapName(form.getId()));
                    // 增加判断: name = POWER_USER
                    String name = group.getName();
                    boolean adminRole = userService.isADMINRole();
                    // "POWER_USER"(普通管理员角色)只能具有admin角色权限才能修改
                    boolean powerUserFlag = "POWER_USER".equals(name) && !adminRole;
                    boolean superAdmin = "ROLE_ADMIN".equals(name);
                    if (powerUserFlag || superAdmin) {
                        return new JsonResultBean(JsonResultBean.FAULT, "您没有修改该角色的权限,如需修改请联系管理员");
                    } else {
                        return userService.updateGroup(form.getId(), group, permissionEditTree, ipAddress);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 组装tree
     * @return List
     * @author wangying
     */
    @ApiOperation(value = "获取当前用户所有角色的有权限树", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/resourceTree", method = RequestMethod.POST)
    @ResponseBody
    public String resourceTree() {
        List<Resource> resources = resourceService.findAll(); // 所有权限
        // 当前用户所拥有角色
        LdapName name = LdapUtils.newLdapName(
            SystemHelper.getCurrentUser().getId() + "," + userService.getBaseLdapPath().toString());
        List<Group> roles = (List<Group>) userService.findByMember(name);
        List<String> roleIds = new ArrayList<>();
        if (roles != null && !roles.isEmpty()) {
            for (Group role : roles) {
                roleIds.add(role.getId().toString());
            }
        }
        // 查询当前用户拥有的菜单权限
        List<String> curResources = resourceService.findResourceByRoleIds(roleIds);
        JSONArray result = new JSONArray();
        for (Resource resource : resources) {
            // 除去用户管理和角色管理
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
     * 根据id删除角色
     * @author wangying
     */
    @ApiOperation(value = "根据id删除角色", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@ApiParam(value = "角色id") @PathVariable("id") final String id) {
        try {
            if (id != null) {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // ldap删除角色
                userService.deleteGroup(id, ipAddress);
            }

            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("删除角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "根据ids批量删除角色", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "角色ids,用分号(;)隔开", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            // 获取操作用户的IP
            String ipAddr = new GetIpAddr().getIpAddr(request);// 获得访问ip
            // 删除ldap中group
            userService.deleteGroup(items, ipAddr);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("批量删除角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 将form表单里面的String Date转换成Date型，字符串去掉空白
     */
    @InitBinder
    protected final void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
