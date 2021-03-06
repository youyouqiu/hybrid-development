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
 * ?????? RoleController
 */
@Controller
@RequestMapping("/swagger/c/role")
@Api(tags = { "????????????" }, description = "????????????api??????")
public class SwaggerRoleController {
    private static final Logger log = LogManager.getLogger(SwaggerRoleController.class);

    private static final String ROLE_NOT_EXISTS = "?????????????????????";
    private static final String ROLE_ID_NULL = "??????id???????????????";

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
     * ????????????(??????)
     */
    @Auth
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "???????????????????????????????????????????????????????????????????????????",
            required = true, paramType = "query", dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "??????????????????", required = true,
            paramType = "query", dataType = "long", defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "???????????????,????????????20",
            required = false, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(RoleQuery query) {
        try (Page<Group> rolePage = new Page<>()) {
            if (query == null) {
                return new PageGridBean(PageGridBean.FAULT);
            }
            // ??????????????????
            if (query.getPage() == null || query.getLimit() == null) { // page???limit????????????
                return new PageGridBean(PageGridBean.FAULT);
            }
            if (StringUtils.isNotBlank(query.getSimpleQueryParam())
                && query.getSimpleQueryParam().length() > 20) { // ????????????????????????20
                return new PageGridBean(PageGridBean.FAULT);
            }
            List<Group> roles = userService.queryRoleList(query.getSimpleQueryParam());
            if (roles != null && !roles.isEmpty()) {
                List<Group> roleList = userService.getCurRole(roles);
                // ??????page
                int curPage = query.getPage().intValue(); // ????????????
                int pageSize = query.getLimit().intValue(); // ????????????
                int pages = (roleList.size() - 1) / pageSize + 1; // ?????????
                int start = (curPage - 1) * pageSize; // ????????????
                int end = curPage * pageSize > roleList.size() ? (roleList.size() - 1)
                    : (curPage * pageSize - 1); // ??????????????????????????????????????????
                if (curPage > pages) { // ????????????????????????????????????????????????1
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
            log.error("??????????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @ApiOperation(value = "???????????????id??????????????????????????????????????????", notes = "??????json??????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "roleId", value = "???????????????????????????id", required = true, paramType = "query", dataType = "string")
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
     * ?????? ??????
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "roleName", value = "????????????", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "permissionTree",
            value = "??????????????????json???,??????[{'id':'b46de828-6a8e-11e6-8b77-86f30ca893d3','edit':false},{...}]",
            required = true, paramType = "query", dataType = "string", defaultValue = "[]"),
        @ApiImplicitParam(name = "description", value = "????????????", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) final RoleForm form,
        @RequestParam("permissionTree") final String permissionTree, final BindingResult bindingResult) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("flag", 2); // 0????????? 1??? ?????? 2???????????????
            // ????????????
            if (bindingResult.hasErrors()) {
                String errorMsg = SpringBindingResultWrapper.warpErrors(bindingResult);
                return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
            }
            // ????????????
            if (StringUtil.isEmpty(form.getRoleName())) {
                msg.put("errMsg", "????????????????????????????????????");
                return new JsonResultBean(msg);
            }
            List<Group> groupByName = userService.getGroupByName(form.getRoleName());
            if (groupByName != null) {
                msg.put("errMsg", "????????????????????????????????????");
                return new JsonResultBean(msg);
            }
            // ??????cn
            String cn = "ROLE_" + UUID.randomUUID();
            // ??????id
            String roleId = "cn=" + cn + ",ou=Groups";
            List<RoleResourceForm> formList = new ArrayList<>();
            // ????????????????????????
            if (!parsePermission(permissionTree, msg, roleId, formList)) {
                return new JsonResultBean(msg);
            }
            // ldap ????????????
            Group group = new Group();
            group.setId(LdapUtils.newLdapName(roleId));
            group.setName(cn);
            group.setDescription(form.getDescription());
            group.setRoleName(form.getRoleName());
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            userService.addRole(group, ipAddress);

            roleService.addRoleResourceByBatch(formList);
            msg.put("flag", 1);
            msg.put("errMsg", "???????????????");
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, systemError);
        }
    }

    /**
     * ??????????????????JSON?????????
     * @return ??????????????????????????????false??????????????????true
     */
    private boolean parsePermission(String permissions, JSONObject msg, String roleId, List<RoleResourceForm> forms) {
        if (StringUtils.isBlank(permissions)) {
            msg.put("errMsg", "??????????????????");
            return false;
        }
        try {
            JSONArray resourceArray = JSON.parseArray(permissions);
            JSONObject resource;
            for (Object obj : resourceArray) {
                resource = (JSONObject) obj;
                RoleResourceForm roleResource = new RoleResourceForm();
                String id = resource.getString("id");
                // ????????????
                boolean edit = resource.getBooleanValue("edit");
                if (resourceService.findResourceById(id) == null) { // ??????id??????????????????????????????
                    msg.put("errMsg", "???????????????id????????????");
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
            msg.put("errMsg", "???????????????????????????");
            return false;
        }
        return true;
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "????????????id????????????????????????", notes = "????????????", authorizations = {
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
            log.error("????????????????????????????????????", e);
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
     * ????????????
     */
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "roleName", value = "????????????", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "permissionEditTree",
            value = "??????????????????json???,??????[{'id':'b46de828-6a8e-11e6-8b77-86f30ca893d3','edit':false},{...}]",
            required = true, paramType = "query", dataType = "string", defaultValue = "[]"),
        @ApiImplicitParam(name = "description", value = "????????????", required = false,
            paramType = "query", dataType = "string") })
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) final RoleForm form,
        @RequestParam("permissionEditTree") final String permissionEditTree, final BindingResult bindingResult) {
        try {
            if (form != null) {
                // ????????????
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
                    // ????????????(ldap)
                    Group group = new Group();
                    BeanUtils.copyProperties(form, group);
                    group.setId(LdapUtils.newLdapName(form.getId()));
                    return userService.updateGroup(form.getId(), group, permissionEditTree, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, systemError);
        }
    }

    /**
     * ??????id????????????
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "??????id????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        if (StringUtils.isNotBlank(id)) {
            try {
                // ?????????????????????IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // ldap????????????
                userService.deleteGroup(id, ipAddress);
                // ??????????????????????????????????????????
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
     * ????????????
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "??????ids??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "??????ids,?????????(;)??????",
        required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        String items = request.getParameter("deltems");
        // ?????????????????????IP
        String ipAddress = new GetIpAddr().getIpAddr(request);
        try {
            userService.deleteGroup(items, ipAddress);// ??????ldap???group
            // ??????????????????
            roleService.deleteByRole(items);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ???form???????????????String Date?????????Date???????????????????????????
     */
    @InitBinder
    protected final void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
