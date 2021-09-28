package com.zw.api.controller.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.core.Group;
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
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 角色 RoleController
 */
@Controller
@RequestMapping("/swagger/c/role")
@Api(tags = { "角色管理" }, description = "角色相关api接口")
public class SwaggerRoleController {
    private static final Logger log = LogManager.getLogger(SwaggerRoleController.class);

    private static final String ROLE_NOT_EXISTS = "该角色不存在！";
    private static final String ROLE_ID_NULL = "角色id不能为空！";

    @Value("${sys.error.msg}")
    private String systemError;

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 查询角色(分页)
     */
    @Auth
    @ApiOperation(value = "分页查询角色列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）",
            required = true, paramType = "query", dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true,
            paramType = "query", dataType = "long", defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20",
            required = false, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(RoleQuery query) {
        try (Page<Group> rolePage = new Page<>()) {
            if (query == null) {
                return new PageGridBean(PageGridBean.FAULT);
            }
            // 校验传入字段
            if (query.getPage() == null || query.getLimit() == null) { // page和limit不能为空
                return new PageGridBean(PageGridBean.FAULT);
            }
            if (StringUtils.isNotBlank(query.getSimpleQueryParam())
                && query.getSimpleQueryParam().length() > 20) { // 模糊搜索长度小于20
                return new PageGridBean(PageGridBean.FAULT);
            }
            List<Group> roles = userService.queryRoleList(query.getSimpleQueryParam());
            if (roles != null && !roles.isEmpty()) {
                List<Group> roleList = userService.getCurRole(roles);
                // 组装page
                int curPage = query.getPage().intValue(); // 当前页数
                int pageSize = query.getLimit().intValue(); // 每页条数
                int pages = (roleList.size() - 1) / pageSize + 1; // 总页数
                int start = (curPage - 1) * pageSize; // 开始位置
                int end = curPage * pageSize > roleList.size() ? (roleList.size() - 1)
                    : (curPage * pageSize - 1); // 结束位置（判断是否最后一页）
                if (curPage > pages) { // 若当前页大于总页数，当前页设置为1
                    curPage = 1;
                }
                for (int i = start; i <= end; i++) {
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
            log.error("分页查询角色信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @ApiOperation(value = "根据选角色id获取可选该角色资源权限树结构", notes = "返回json的串", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "roleId", value = "当前登录用户的角色id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/choicePermissionTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean choicePermissionTree(String roleId) {
        JsonResultBean errResult = checkRole(roleId);
        if (errResult != null) {
            return errResult;
        }
        JSONArray treeList = roleService.generateTree(roleId);
        return new JsonResultBean(treeList.toJSONString());
    }

    /**
     * 新增 角色
     */
    @ApiOperation(value = "新增角色", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "roleName", value = "角色名称", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "permissionTree",
            value = "所选角色权限json串,例：[{'id':'b46de828-6a8e-11e6-8b77-86f30ca893d3','edit':false},{...}]",
            required = true, paramType = "query", dataType = "string", defaultValue = "[]"),
        @ApiImplicitParam(name = "description", value = "角色描述", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) final RoleForm form,
        @RequestParam("permissionTree") final String permissionTree, final BindingResult bindingResult) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
            // 数据校验
            if (bindingResult.hasErrors()) {
                String errorMsg = SpringBindingResultWrapper.warpErrors(bindingResult);
                return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
            }
            // 校验数据
            if (StringUtil.isEmpty(form.getRoleName())) {
                msg.put("errMsg", "添加失败，必填字段未填！");
                return new JsonResultBean(msg);
            }
            List<Group> groupByName = userService.getGroupByName(form.getRoleName());
            if (groupByName != null) {
                msg.put("errMsg", "添加失败，该角色已存在！");
                return new JsonResultBean(msg);
            }
            // 生成cn
            String cn = "ROLE_" + UUID.randomUUID();
            // 生成id
            String roleId = "cn=" + cn + ",ou=Groups";
            List<RoleResourceForm> formList = new ArrayList<>();
            // 用户所选可写权限
            if (!parsePermission(permissionTree, msg, roleId, formList)) {
                return new JsonResultBean(msg);
            }
            // ldap 新增角色
            Group group = new Group();
            group.setId(LdapUtils.newLdapName(roleId));
            group.setName(cn);
            group.setDescription(form.getDescription());
            group.setRoleName(form.getRoleName());
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            userService.addRole(group, ipAddress);

            roleService.addRoleResourceByBatch(formList);
            msg.put("flag", 1);
            msg.put("errMsg", "保存成功！");
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("新增角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, systemError);
        }
    }

    /**
     * 解析权限树的JSON字符串
     * @return 如果解析失败，则返回false；反之则返回true
     */
    private boolean parsePermission(String permissions, JSONObject msg, String roleId, List<RoleResourceForm> forms) {
        if (StringUtils.isBlank(permissions)) {
            msg.put("errMsg", "资源权限为空");
            return false;
        }
        try {
            JSONArray resourceArray = JSON.parseArray(permissions);
            JSONObject resource;
            for (Object obj : resourceArray) {
                resource = (JSONObject) obj;
                RoleResourceForm roleResource = new RoleResourceForm();
                String id = resource.getString("id");
                // 是否可写
                boolean edit = resource.getBooleanValue("edit");
                if (resourceService.findResourceById(id) == null) { // 查询id所对应的资源是否存在
                    msg.put("errMsg", "该资源权限id不存在！");
                    return false;
                }
                roleResource.setRoleId(roleId);
                roleResource.setResourceId(id);
                if (edit) {
                    roleResource.setEditable(1);
                } else {
                    roleResource.setEditable(0);
                }
                forms.add(roleResource);
            }
        } catch (Exception e) {
            msg.put("errMsg", "资源权限格式错误！");
            return false;
        }
        return true;
    }

    /**
     * 修改角色页面
     */
    @ApiOperation(value = "根据角色id查询角色详细信息", notes = "用于编辑", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            JsonResultBean errResult = checkRole(id);
            if (errResult != null) {
                return errResult;
            }
            return new JsonResultBean(userService.getGroupById(id));
        } catch (Exception e) {
            log.error("修改角色信息界面弹出异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, systemError);
        }
    }

    private JsonResultBean checkRole(String id) {
        if (StringUtils.isNotBlank(id)) {
            try {
                if (userService.getGroupById(id) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, ROLE_NOT_EXISTS);
                }
            } catch (Exception e) {
                return new JsonResultBean(JsonResultBean.FAULT, ROLE_NOT_EXISTS);
            }
        } else {
            return new JsonResultBean(JsonResultBean.FAULT, ROLE_ID_NULL);
        }
        return null;
    }

    /**
     * 修改角色
     */
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "角色id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "roleName", value = "角色名称", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "permissionEditTree",
            value = "所选角色权限json串,例：[{'id':'b46de828-6a8e-11e6-8b77-86f30ca893d3','edit':false},{...}]",
            required = true, paramType = "query", dataType = "string", defaultValue = "[]"),
        @ApiImplicitParam(name = "description", value = "角色描述", required = false,
            paramType = "query", dataType = "string") })
    @ApiOperation(value = "修改角色", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) final RoleForm form,
        @RequestParam("permissionEditTree") final String permissionEditTree, final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    if (StringUtils.isBlank(form.getId())) {
                        return new JsonResultBean(JsonResultBean.FAULT, ROLE_ID_NULL);
                    }
                    if (userService.getGroupById(form.getId()) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, ROLE_NOT_EXISTS);
                    }
                    // 修改角色(ldap)
                    Group group = new Group();
                    BeanUtils.copyProperties(form, group);
                    group.setId(LdapUtils.newLdapName(form.getId()));
                    return userService.updateGroup(form.getId(), group, permissionEditTree, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, systemError);
        }
    }

    /**
     * 根据id删除角色
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "根据id删除角色", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        if (StringUtils.isNotBlank(id)) {
            try {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // ldap删除角色
                userService.deleteGroup(id, ipAddress);
                // 数据库删除角色与资源关联关系
                roleService.deleteByRole(id);
            } catch (Exception e) {
                return new JsonResultBean(JsonResultBean.FAULT, ROLE_NOT_EXISTS);
            }
        } else {
            return new JsonResultBean(JsonResultBean.FAULT, ROLE_NOT_EXISTS);
        }

        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 批量删除
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "根据ids批量删除角色", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "角色ids,用分号(;)隔开",
        required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        String items = request.getParameter("deltems");
        // 获取操作用户的IP
        String ipAddress = new GetIpAddr().getIpAddr(request);
        try {
            userService.deleteGroup(items, ipAddress);// 删除ldap中group
            // 删除关联关系
            roleService.deleteByRole(items);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的角色！");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 将form表单里面的String Date转换成Date型，字符串去掉空白
     */
    @InitBinder
    protected final void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
