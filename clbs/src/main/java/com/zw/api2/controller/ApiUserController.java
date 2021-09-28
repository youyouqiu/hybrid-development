package com.zw.api2.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api.config.ResponseUntil;
import com.zw.api2.swaggerEntity.SwaggerSimpleUserQuery;
import com.zw.api2.swaggerEntity.SwaggerUserAddBean;
import com.zw.api2.swaggerEntity.SwaggerUserUpdateBean;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SecurityPasswordHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.core.query.UserQuery;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ZipUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import jodd.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/api/c/user")
@Api(tags = { "用户管理dev" }, description = "用户管理相关api接口")
public class ApiUserController {
    private static Logger log = LogManager.getLogger(ApiUserController.class);

    @Value("${oauthToken.url}")
    private String oauthTokenUrl;

    @Value("${experience.id}")
    private String experienceId;

    @Value("${experience.uuid}")
    private String experienceUuid;

    @Value("${experience.role.id}")
    private String experienceRoleId;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${outh2.fail}")
    private String outh2Fail;

    @Value("${user.name.exist}")
    private String userNameExist;

    @Value("${old.password.error}")
    private String oldPasswordError;

    @Value("${edit.password.success}")
    private String editPasswordSuccess;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 查询用户信息
     * @author FanLu
     */
    @ApiOperation(value = "查询用户信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(@ModelAttribute("swaggerSimpleUserQuery") SwaggerSimpleUserQuery swaggerSimpleUserQuery) {
        UserQuery query = new UserQuery();
        Page<UserBean> userPage = null;
        try {
            BeanUtils.copyProperties(swaggerSimpleUserQuery, query);
            String orgId = userService.getOrgIdByUser();
            boolean searchSubTree = true;
            // 包含查询条件，从ldap中查询
            if (StringUtils.isNotBlank(query.getGroupName())) {
                orgId = query.getGroupName();
                searchSubTree = false;
            }
            List<UserBean> users = userService.getUserList(query.getSimpleQueryParam(), orgId, searchSubTree);
            userPage = new Page<>(query.getPage().intValue(), query.getLimit().intValue());
            if (users == null || users.isEmpty()) {
                userPage.setTotal(0);
            } else {
                int size = users.size();
                userPage.setTotal(size);
                int endIndex = Math.min(size, userPage.getEndRow());
                if (size < userPage.getPageSize()) {
                    userPage.addAll(users);
                } else {
                    userPage.addAll(users.subList(userPage.getStartRow(), endIndex));
                }
            }
            return new PageGridBean(query, userPage, true);
        } catch (Exception e) {
            log.error("查询用户信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        } finally {
            if (userPage != null) {
                userPage.close();
            }
        }
    }

    /**
     * 模糊查询用户信息
     * @author FanLu
     */
    @ApiOperation(value = " 模糊查询用户信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "searchParam", value = "模糊搜索的条件", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "orgId", value = "企业id", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/searchByKey" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getUserByKey(String searchParam, String orgId) {
        try {
            List<UserBean> users = userService.getUserList(searchParam, orgId, true);
            for (UserBean user : users) {
                // 获取用户所属部门 uid=admin,ou=IT,ou=zw,ou=organization
                LdapName name = (LdapName) user.getId();
                String group = name.get(2);
                user.setGroupName(group.substring(group.indexOf('=') + 1));
                // 获取用户所属角色集合
                List<Group> roles;
                StringBuilder roleName = new StringBuilder();
                name = LdapUtils.newLdapName(name.toString() + "," + userService.getBaseLdapPath().toString());
                roles = (List<Group>) userService.findByMember(name);
                for (Group role : roles) {
                    roleName.append(role.getName()).append(",");
                }
                user.setRoleName(
                    roleName.length() == 0 ? roleName.toString() : roleName.substring(0, roleName.length() - 1));
            }
            return new PageGridBean(users);
        } catch (Exception e) {
            log.error("查询用户信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 分配角色列表页面
     * @author FanLu
     */
    @ApiOperation(value = "根据所选用户id查询其角色权限（当前用的的角色checked=true）", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/roleList_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean roleList(@PathVariable("id") @ApiParam("用户id") final String id) {
        try {
            JSONObject data = new JSONObject();
            UserBean user = userService.findUser(id);
            List<Group> roles =
                (List<Group>) userService.findByMember(SystemHelper.getCurrentUser().getId().toString());
            // 用户是否是admin权限
            boolean isAdmin = userService.isADMINRole();
            if (isAdmin) {
                roles = userService.getAllGroup();
            }
            // copy list
            List<Group> filterRole = new ArrayList<>();
            CollectionUtils.addAll(filterRole, new Object[roles.size()]);
            Collections.copy(filterRole, roles);
            for (Group group : roles) {
                if ("ROLE_ADMIN".equals(group.getName())) {
                    filterRole.remove(group);
                    break;
                }
            }
            // 根据用户id获取角色
            Name name = LdapUtils.newLdapName(id + "," + userService.getBaseLdapPath().toString());
            List<Group> userGroup = (List<Group>) userService.findByMember(name);
            JSONArray roleList = new JSONArray();
            for (Group group : filterRole) {
                JSONObject curRole = new JSONObject();
                curRole.put("id", group.getId());
                curRole.put("name", group.getRoleName());
                curRole.put("cn", group.getName());
                curRole.put("checked", false);
                curRole.put("readyonly", false);
                if (!isAdmin && "POWER_USER".equals(group.getName())) {
                    curRole.put("readyonly", true);
                }
                for (Group userGro : userGroup) {
                    if (group.getId().toString().equals(userGro.getId().toString())) { // 已选
                        curRole.put("checked", true);
                        break;
                    }
                }
                roleList.add(curRole);
            }

            data.put("result", user);
            data.put("roles", roleList);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("分配角色界面弹出异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 更新某个用户下的角色
     * @author FanLu
     */
    @ApiOperation(value = "更新所选用户下的角色", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "userId", value = "所选用户id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "roleIds", value = "所选角色集合字符串，用逗号隔开(不填则角色置空)",
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/updateRolesByUser.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateResourcesByRole(@RequestParam("userId") final String userId,
        @RequestParam("roleIds") final String roleIds) {
        try {
            if (userId != null && roleIds != null) {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                if (StringUtils.isNotBlank(roleIds)) {
                    String[] roleArray = roleIds.split(",");
                    List<String> roleList = Arrays.asList(roleArray);
                    // 分配用户角色属于操作用户的角色 gfw 20180904
                    if (!userService.compareAllotRole(SystemHelper.getCurrentUser().getId().toString(), roleList)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "分配用户角色不能超过操作用户的角色");
                    }
                }
                userService.updateRolesByUser(userId, roleIds, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS, "分配角色成功");
            }
            return new JsonResultBean(JsonResultBean.FAULT, "分配角色失败");
        } catch (Exception e) {
            log.error("更新用户下的角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新建用户
     * @author FanLu
     */
    @ApiOperation(value = "新建用户", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/newuser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean createUser(@ModelAttribute("swaggerUserAddBean") SwaggerUserAddBean swaggerUserAddBean,
        final BindingResult bindingResult) {
        UserBean user = new UserBean();
        try {
            BeanUtils.copyProperties(swaggerUserAddBean, user);
            JSONObject msg = new JSONObject();
            msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
            // 数据校验
            if (bindingResult.hasErrors()) {
                String errorMsg = SpringBindingResultWrapper.warpErrors(bindingResult);
                return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
            } else {
                // 校验数据
                if (StringUtil.isEmpty(user.getUsername()) || StringUtil.isEmpty(user.getGroupId()) || StringUtil
                    .isEmpty(user.getPassword())) {
                    msg.put("errMsg", "添加失败，必填字段未填！");
                    return new JsonResultBean(msg);
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                /*JsonResultBean resultBean = userService.addCreateUser(user, ipAddress);
                if (!resultBean.isSuccess()) {
                    return resultBean;
                }*/
                msg.put("flag", 1);
                msg.put("errMsg", "保存成功！");
                return new JsonResultBean(msg);
            }
        } catch (Exception e) {
            log.error("新建用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改用户
     * @author FanLu
     */
    @ApiOperation(value = "根据id查询用户的详细信息", notes = "用于编辑", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean initUpdateUser(@PathVariable("id") @ApiParam("id") String id) {
        try {
            JSONObject data = new JSONObject();
            UserBean user = userService.findUser(id);
            if ("null".equals(user.getAuthorizationDate())) {
                user.setAuthorizationDate(null);
            }
            if ("admin".equals(user.getUsername()) && "null".equals(user.getSendDownCommand())) {
                user.setSendDownCommand(null);
            }
            if ("null".equals(user.getMobile())) { // 电话
                user.setMobile("");
            }
            if ("null".equals(user.getFullName())) { // 真实姓名
                user.setFullName("");
            }
            if ("null".equals(user.getMail())) { // 邮箱
                user.setMail("");
            }
            String userId = user.getId().toString();
            // 根据用户id得到用户所属组织
            int beginIndex = userId.indexOf(','); // 获取组织id(根据用户id得到用户所在部门)
            String orgId = userId.substring(beginIndex + 1);
            OrganizationLdap org = userService.findOrganization(orgId);
            user.setGroupId(orgId);
            user.setGroupName(org.getName());
            // 无需展示
            user.setSendDownCommand("");
            data.put("result", user);
            data.put("userId", userId);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("修改用户界面弹出异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 修改用户
     * @author FanLu
     */
    @ApiOperation(value = "修改用户", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateUser(@ModelAttribute("userUpdateBean") SwaggerUserUpdateBean userUpdateBean) {
        String userId = userUpdateBean.getUserId();
        UserBean user = new UserBean();
        try {
            BeanUtils.copyProperties(userUpdateBean, user);
            JSONObject msg = new JSONObject();
            msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
            // 校验数据
            if (StringUtil.isEmpty(user.getUsername()) || StringUtil.isEmpty(user.getGroupId())) {
                msg.put("errMsg", "修改失败，必填字段未填！");
                return new JsonResultBean(msg);
            }
            // 对比修改前后用户名
            UserBean beforeUser = userService.findUser(userId);
            String beforeName = beforeUser.getUsername();
            //如果修改前后用户名不一致则不允许修改
            if (!beforeName.equals(user.getUsername())) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            msg.put("flag", 1);
            msg.put("errMsg", "修改成功！");
            // 将用户的启停状态存入redis
            // RedisHelper.setString(user.getUsername() +
            // "_state", user.getState(), PublicVariable.REDIS_EIGHT_DATABASE);
            if (StringUtils.isNotBlank(user.getState()) && "0".equals(user.getState())) {
                userService.expireUserSession(user.getUsername());
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("修改用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 授权截止日期验证
     */
    @ApiOperation(value = "授权截止日期验证", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "authorizationDate", value = "授权时间（yyyy-MM-dd）",
            required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/verification", method = RequestMethod.POST)
    @ResponseBody
    public boolean verificationAuthorizationDate(String authorizationDate) {
        try {
            /*
             * 这个验证方法验证的是普通用户增加或修改时的数据验证
             */
            // 当前登录用户的id
            String loginId = SystemHelper.getCurrentUser().getId().toString();
            // 当前登录用户的信息
            UserBean userBean = userService.findUser(loginId);
            // 当前登录用户的授权截止日期
            String loginUserAuthorizationDate = userBean.getAuthorizationDate();
            // 先判断验证操作用户的授权截止日期是否为空
            if (authorizationDate != null && !"".equals(authorizationDate) && loginUserAuthorizationDate != null && !""
                .equals(loginUserAuthorizationDate) && !"null".equals(loginUserAuthorizationDate)) {
                // 如果在要验证的授权截止日期不为空的前提下当前操作用户的授权截止时间不等于空.那么（创建的用户的授权截止不能大于当前操作用户的授权截止时间）
                Date d = DateUtils.parseDate(loginUserAuthorizationDate.trim(), DATE_FORMAT);
                Date s = DateUtils.parseDate(authorizationDate.trim(), DATE_FORMAT);
                return s.getTime() <= d.getTime();
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("授权截止日期验证异常", e);
            return false;
        }
    }

    /**
     * 验证用户名是否重复
     */
    @ApiOperation(value = "验证用户名是否重复", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "userName", value = "用户名", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/verifyUserName", method = RequestMethod.POST)
    @ResponseBody
    public boolean compareUserName(String userName) {
        try {
            boolean flag = false;
            if (userName != null && !"".equals(userName)) {
                // 校验用户名是否重复
                List<UserBean> userByUid = userService.getUserByUid(userName);
                flag = userByUid == null;
            }
            return flag;
        } catch (Exception e) {
            log.error("添加用户时验证用户名是否重复异常", e);
            return false;
        }
    }

    @ApiOperation(value = "获取当前登录用户的详细信息(包含所在部门)", notes = "用于显示用户详细信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean showViewLdapUserProfilePage(ModelMap model) {
        try {
            final UserLdap user = (UserLdap) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String org = userService.getOrgIdByUser();
            OrganizationLdap orgnization = userService.findOrganization(org);
            user.setOu(orgnization.getName());
            String currentUser = SystemHelper.getCurrentUId();
            UserBean newUserBean = userService.findUser(currentUser);
            if (Objects.nonNull(newUserBean)) {
                String mobile = newUserBean.getMobile();
                if ("null".equals(mobile)) {
                    user.setMobile("");
                } else {
                    user.setMobile(mobile);
                }
                String mail = newUserBean.getMail();
                if ("null".equals(mail)) {
                    user.setMail("");
                } else {
                    user.setMail(mail);
                }
            }
            model.addAttribute("user", user);
            return new JsonResultBean(model);
        } catch (Exception e) {
            log.error("授权截止日期验证异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 修改用户密码接口
     * @param oldpass 老密码
     * @param newpass 新密码
     */
    @ApiOperation(value = "修改用户密码", notes = "用于修改密码", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "oldpass", value = "旧密码，6——25个字符", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "newpass", value = "新密码，6——25个字符", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/changePwd", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean changePwd(@RequestParam("oldpass") final String oldpass,
        @RequestParam("newpass") final String newpass) {
        try {
            final UserLdap user = (UserLdap) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserBean currentUser = userService.findUser(user.getId().toString());
            JSONObject msg = new JSONObject();
            msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
            // 校验数据
            String b = currentUser.getPassword();
            String[] bt = b.split(",");
            byte[] bb = new byte[bt.length];
            for (int i = 0; i < bt.length; i++) {
                int u = Integer.parseInt(bt[i]);
                bb[i] = (byte) u;
            }
            String s = new String(bb);
            if (!SecurityPasswordHelper.isPasswordValid(s, oldpass)) {
                msg.put("errMsg", oldPasswordError);
                return new JsonResultBean(msg);
            }
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            userService.updateModifyPwd(newpass, ipAddress, null);
            msg.put("flag", 1);
            msg.put("errMsg", editPasswordSuccess);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("修改密码异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 删除单个用户
     * @author FanLu
     */
    @ApiOperation(value = "根据id删除用户", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") @ApiParam("用户id") final String id) {
        try {
            if (id != null) {
                UserBean userBean = userService.findUser(id);
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                userService.delete(id, ipAddress);
                // 删除用户和车组的关联
                // vehicleService.deleteUserVehicleByUserId(userBean.getUuid(), userBean.getUsername());
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除用户
     * @author FanLu
     */
    @ApiOperation(value = "根据ids批量删除用户(用分号(;)隔开)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "需要删除的用户id集合(用分号(;)隔开)", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null) {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                userService.delete(items, ipAddress);
                String[] item = items.split(",");
                // 删除用户和车的关联
                List<String> userIdList = Arrays.asList(item);
                vehicleService.deleteUserVehicleByUsers(userIdList);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 分配车
     * @author wangying
     */
    @ApiOperation(value = "根据所选用户id查询其拥有的分组权限", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/vehiclePer_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getVehiclePer(@PathVariable("id") @ApiParam("用户id") String id) {
        try {
            JSONObject data = new JSONObject();
            UserBean user = userService.findUser(id);
            // String tree = assignmentService.getEditAssignmentTree(id, user).toJSONString();
            data.put("user", user);
            data.put("vehicleTree", "tree");
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("授权界面弹出异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    @ApiOperation(value = "修改所选用户的分组权限", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "需要修改的用户id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "userVehicleList", value = "修改的分组权限Json串,不填则分组权限置空(格式[id1,id2,...]：  "
            + "例：['288d106e-be41-46dc-98da-a84d71da6675',"
            + "'ba71fe20-9338-4b67-aba3-3b3752e1f7bd'])", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/vehiclePer.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveVehiclePer(@RequestParam("id") final String userId,
        @RequestParam("userVehicleList") final String userVehicleList) {
        try {
            // 获取操作用户的IP
            // String ipAddress = new GetIpAddr().getIpAddr(request);
            // boolean flag = vehicleService.updateUserVehicel(userId, userVehicleList, ipAddress);
            // if (flag) {
            //     return new JsonResultBean(JsonResultBean.SUCCESS, "授权成功");
            // } else {
            //     return new JsonResultBean(JsonResultBean.FAULT, outh2Fail);
            // }
            return null;
        } catch (Exception e) {
            log.error("授权查看分组权限异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据组织id查询组织下的用户
     * @param type 根节点是否可选
     */
    @ApiOperation(value = "根据终端id查询终端详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "根节点是否可选", required = true,
            defaultValue = "group", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/groupTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findGroupTree(String type) {
        try {
            JSONObject obj = new JSONObject();
            JSONArray result = new JSONArray();
            // 根据用户名获取用户id
            String userId = SystemHelper.getCurrentUser().getId().toString();
            // 获取当前用户所在组织及下级组织
            int beginIndex = userId.indexOf(','); // 获取组织id(根据用户id得到用户所在部门)
            String orgId = userId.substring(beginIndex + 1);
            List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
            int value = 0;
            // 遍历得到当前用户组织及下级组织id的list
            if (orgs != null && !orgs.isEmpty()) {
                for (OrganizationLdap org : orgs) {
                    List<UserBean> user = userService.getUserList(null, org.getId().toString(), false);
                    if (user != null && !user.isEmpty()) {
                        value += user.size();
                        for (UserBean userBean : user) {
                            JSONObject userObj = new JSONObject();
                            String uid = String.valueOf(userBean.getUuid());
                            userObj.put("id", uid);
                            userObj.put("name", userBean.getUsername());
                            userObj.put("type", "user");
                            userObj.put("iconSkin", "userSkin");
                            userObj.put("pId",
                                userBean.getId().toString().substring(userBean.getId().toString().indexOf(',') + 1));
                            userObj.put("checked", true); // 用户在线率查询，默认都勾选
                            result.add(userObj);
                        }
                    }
                }
            }
            // 组装组织树结构
            result.addAll(JsonUtil.getOrgTree(orgs, type));
            obj.put("tree", result);
            obj.put("size", value);
            String data = obj.toJSONString();
            // 压缩数据
            String resultValue = ZipUtil.compress(data);
            return new JsonResultBean(resultValue);
        } catch (Exception e) {
            log.error("查询组织下的用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据组织id查询组织下的用户
     * @param type 根节点是否可选
     */
    @ApiOperation(value = "根据组织id查询组织下的用户", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "根节点是否可选", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "企业id", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/chatUserTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean chatUserTree(String type, String groupId) {
        try {
            String data = userService.getChatGroupUserList(type, groupId);
            // 压缩数据
            String resultValue = ZipUtil.compress(data);
            return new JsonResultBean(resultValue);
        } catch (Exception e) {
            log.error("查询组织下的用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @ApiOperation(value = "体验账号", notes = "用于体验", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getExperenceUser", method = RequestMethod.GET)
    public void getExperenceUser(String accessToken, HttpServletResponse response, HttpSession session, String tel) {
        try {
            //获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            ResponseUntil.setResponseHeader(response); // 解决跨域问题
            // 注册账号，分配权限
            String password = tel.substring(tel.length() - 6);
            List<UserBean> s = userService.getUserByUid(tel);
            if (s == null) { //若用户不存在则新增用户
                // experienceRegister(tel, password, ipAddress);
            } else { //若用户存在直接跳转至登录界面
                SecurityContextHolder.clearContext();
                session.removeAttribute("SPRING_SECURITY_CONTEXT");
                response.sendRedirect("/clbs");
                return;
            }
            Map<String, String> params = new HashMap<>();
            params.put("client_id", "mobile_1");
            params.put("client_secret", "secret_1");
            params.put("grant_type", "password");
            params.put("username", tel);
            params.put("password", password);
            String result = HttpClientUtil.sendPost(oauthTokenUrl, params);
            // 若没有获取到token则直接跳转到登录界面
            if (StringUtils.isBlank(result)) {
                SecurityContextHolder.clearContext();
                session.removeAttribute("SPRING_SECURITY_CONTEXT");
                response.sendRedirect("/clbs/");
                return;
            }
            JSONObject jsStr = JSONObject.parseObject(result);
            String token = (String) jsStr.get("value");
            SecurityContext context = SecurityContextHolder.getContext();
            if (context == null) {
                context = SecurityContextHolder.createEmptyContext();
            }
            OAuth2Authentication authToken = tokenStore.readAuthentication(token);
            Authentication authentication = authToken.getUserAuthentication();
            context.setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            response.sendRedirect("/clbs/");
        } catch (Exception e) {
            log.error("新增体验用户异常", e);
            SecurityContextHolder.clearContext();
            session.removeAttribute("SPRING_SECURITY_CONTEXT");
        }
    }

}
