package com.zw.platform.controller.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.form.RoleForm;
import com.zw.platform.domain.core.form.RoleResourceForm;
import com.zw.platform.domain.core.query.RoleQuery;
import com.zw.platform.service.core.ResourceService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import jodd.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
import org.springframework.web.servlet.ModelAndView;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 角色 RoleController
 */
@Controller
@RequestMapping("/c/role")
public class RoleController {
    private static Logger log = LogManager.getLogger(RoleController.class);

    private static final String LIST_PAGE = "core/uum/role/list";

    private static final String ADD_PAGE = "core/uum/role/add";

    private static final String EDIT_PAGE = "core/uum/role/edit";

    private static final String EDIT_USER_PAGE = "core/uum/role/editUser";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    // // 车辆的资源id
    // private static final String VEHICLE_CODE = "a1cf4672-6f4c-11e6-8b77-86f30ca893d3";
    // // 人的资源id
    // private static final String PERSON_CODE = "963285e0-6aab-11e6-8b77-86f30ca893d3";
    // // 物的资源id
    // private static final String THING_CODE = "963289c8-6aab-11e6-8b77-86f30ca893d3";

    // private static final String INFO_CODE = "b46dec1a-6a8e-11e6-8b77-86f30ca893d3";

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LogSearchService ls;

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
     * 角色管理页面
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage(ModelMap map) {
        // map.put("hasRole", SystemHelper.checkPermissionEditable());
        return LIST_PAGE;
    }

    /**
     * 查询角色(分页)
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(RoleQuery query) {
        try {
            Page<Group> rolePage = new Page<>();
            List<Group> roles = roleService.getListByKeyword(query.getSimpleQueryParam(), true);
            if (roles != null && roles.size() > 0) {
                // 组装page
                // 当前页数
                int curPage = query.getPage().intValue();
                // 每页条数
                int pageSize = query.getLimit().intValue();
                // 总页数
                int pages = (roles.size() - 1) / pageSize + 1;
                // 开始位置
                int start = (curPage - 1) * pageSize;
                // 结束位置（判断是否最后一页）
                int end = curPage * pageSize > roles.size() ? (roles.size() - 1) : (curPage * pageSize - 1);
                // 若当前页大于总页数，当前页设置为1
                if (curPage > pages) {
                    curPage = 1;
                }
                for (int i = start; i <= end; i++) {
                    Group group = roles.get(i);
                    //若匹配成功则是即刻体验角色，不能删除
                    if (experienceRoleId.equals(group.getName())) {
                        group.setDelFlag(false);
                    }
                    rolePage.add(roles.get(i));
                }
                rolePage.setPages(pages);
                rolePage.setPageSize(pageSize);
                rolePage.setTotal(roles.size());
            } else {
                rolePage.setPages(0);
                rolePage.setPageSize(query.getLimit().intValue());
                rolePage.setTotal(0);
            }

            return new PageGridBean(query, rolePage, true);
        } catch (Exception e) {
            log.error("分页查询角色异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/editUser_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getUserPage(@PathVariable String id) {
        ModelAndView mav = new ModelAndView(EDIT_USER_PAGE);
        Group role = roleService.getGroupById(id);
        Set<Name> roleMembers = role.getMembers();
        String tree = roleService.getRoleUserTree(roleMembers).toJSONString();
        mav.addObject("name", role.getRoleName());
        mav.addObject("roleId", id);
        mav.addObject("userTree", tree);
        return mav;
    }

    @RequestMapping(value = "/updateUserByRole.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updateUserByRole(String userIds, String roleId) {
        try {
            if ("cn=ROLE_ADMIN,ou=Groups".equals(roleId)) {
                return new JsonResultBean(JsonResultBean.FAULT, "超级管理员不能随意分配");
            }
            return userService.updateUserByRole(userIds, roleId);
        } catch (Exception e) {
            log.error("角色分配用户异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, "授权异常");
    }

    /**
         * @return PageGridBean
     * @Title: 权限tree
     * @author wangying
     */
    @RequestMapping(value = "/permissionTree", method = RequestMethod.POST)
    @ResponseBody
    public List getPermissionTree(String roleId) {
        return roleService.generateTree(roleId);
    }

    @RequestMapping(value = "/choicePermissionTree", method = RequestMethod.POST)
    @ResponseBody
    public String choicePermissionTree(String roleId) {
        JSONArray treeList = roleService.generateTree(roleId);
        return treeList.toJSONString();
    }

    /**
     * 新增角色
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增 角色
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final RoleForm form,
        @RequestParam("permissionTree") final String permissionTree, final BindingResult bindingResult) {
        try {
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
                List<Group> groupByName = roleService.getListByKeyword(form.getRoleName(), false);
                if (groupByName != null) {
                    msg.put("errMsg", roleExist);
                    return new JsonResultBean(msg);
                }
                // 生成cn
                String cn = "ROLE_" + UUID.randomUUID();
                // 生成id
                String roleId = "cn=" + cn + ",ou=Groups";
                List<RoleResourceForm> formList = new ArrayList<RoleResourceForm>();
                // 检测用户新增角色所选权限是否在用户角色范围内 gfw 20180904
                // 用户所选可写权限
                if (StrUtil.isNotBlank(permissionTree)) {
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
                if (formList.size() > 0 && roleService.compareUserForm(formList)) {
                    return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
                }

                // ldap 新增角色
                Group group = new Group();
                LdapName ldapName = LdapUtils.newLdapName(roleId);
                group.setId(ldapName);
                group.setName(cn);
                group.setDescription(form.getDescription());
                group.setRoleName(form.getRoleName());
                roleService.addRole(group);
                if (formList.size() > 0) {
                    roleService.addRoleResourceByBatch(formList);
                }

                msg.put("flag", 1);
                msg.put("errMsg", "保存成功！");

                // 新增角色自动分配到创建用户和admin用户  gfw 20180904
                String userId = userService.getCurrentUserInfo().getId().toString();
                // 用户 添加角色 gfw 20180904
                roleService.addAllotRole(userId, cn);
                // admin 添加角色 gfw 20180904
                String admin = userService.getUserByUsername("admin").getId().toString();
                roleService.addAllotRole(admin, cn);
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
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", roleService.getGroupById(id));
            return mav;
        } catch (Exception e) {
            log.error("修改角色页面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改角色
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final RoleForm form,
        @RequestParam("permissionEditTree") final String permissionEditTree, final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 修改角色(ldap)
                    Group group = new Group();
                    BeanUtils.copyProperties(form, group);
                    group.setId(LdapUtils.newLdapName(form.getId()));
                    return roleService.updateRole(form.getId(), group, permissionEditTree);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         * @param
     * @return List
     * @Title: 组装tree
     * @author wangying
     */
    @RequestMapping(value = "/resourceTree", method = RequestMethod.POST)
    @ResponseBody
    public String resourceTree() {
        // 所有权限
        List<Resource> resources = resourceService.findAll();
        // 当前用户所拥有角色
        LdapName name = LdapUtils
                .newLdapName(userService.getCurrentUserInfo().getId() + ","
                        + userService.getBaseLdapPath().toString());
        List<Group> roles = (List<Group>) roleService.getByMemberName(name);
        List<String> roleIds = roles.stream().map(o -> o.getId().toString()).collect(Collectors.toList());
        // 查询当前用户拥有的菜单权限
        List<String> curResources = resourceService.findResourceByRoleIds(roleIds);
        JSONArray result = new JSONArray();
        for (Resource resource : resources) {
            if ("APP登录".equals(resource.getResourceName())) {
                //APP登录 不显示
                continue;
            }
            // 除去用户管理和角色管理
            if (curResources.contains(resource.getId())) {
                JSONObject obj = new JSONObject();
                obj.put("id", resource.getId());
                obj.put("pId", resource.getParentId());
                obj.put("name", resource.getResourceName());
                obj.put("type", resource.getType());
                if (resource.getType() == 1) {
                    obj.put("nocheck", true);
                }
                result.add(obj);
            }
        }
        return result.toJSONString();
    }

    /**
         * @param id
     * @return JsonResultBean
     * @Title: 根据id删除角色
     * @author wangying
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                String mesaage = roleService.deleteGroup(id);
                if (mesaage.length() > 0) {
                    return new JsonResultBean(JsonResultBean.FAULT, mesaage);
                }
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("删除角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         * @return JsonResultBean
     * @Title: 批量删除
     * @author wangying
     */

    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            // 删除ldap中group
            String mesaage = roleService.deleteGroup(items);
            if (mesaage.length() > 0) {
                return new JsonResultBean(JsonResultBean.FAULT, mesaage);
            }
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
    protected final void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder)
        throws Exception {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
