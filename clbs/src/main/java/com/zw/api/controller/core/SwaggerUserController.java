package com.zw.api.controller.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api.config.ResponseUntil;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.query.UserPageQuery;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SecurityPasswordHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.query.UserQuery;
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
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/swagger/c/user")
@Api(tags = { "用户管理" }, description = "用户管理相关api接口")
public class SwaggerUserController {
    private static final Logger log = LogManager.getLogger(SwaggerUserController.class);

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

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
    private OrganizationService organizationService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserGroupService userGroupService;

    /**
     * 查询用户信息
     * @author FanLu
     */
    @Auth
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true,
            paramType = "query", dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query",
            dataType = "long", defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupName", value = "所选组织", paramType = "query",
            dataType = "string") })
    @ApiOperation(value = "分页查询用户列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final UserQuery query) {
        UserPageQuery userPageQuery = UserPageQuery.transform(query);
        if (StringUtils.isEmpty(query.getGroupName())) {
            //没有选择组织时，需要显示当前用户的企业下的所有用户
            String currentUserOrgDn = userService.getCurrentUserOrgDn();
            userPageQuery.setOrgDn(currentUserOrgDn);
            userPageQuery.setSearchSubFlag(true);
        }
        try {
            Page<UserBean> userPage = userService.getPageByKeyword(userPageQuery);
            return new PageGridBean(userPageQuery, userPage, true);
        } catch (Exception e) {
            log.error("查询用户信息异常", e);
            return new PageGridBean(PageGridBean.FAULT, "请联系管理员");
        }
    }

    /**
     * 分配角色列表页面
     * @author FanLu
     */
    @ApiOperation(value = "查询用户角色权限", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "username", value = "用户名,长度4——25", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/roleList", method = RequestMethod.GET)
    public JsonResultBean roleList(final String username) {
        try {
            UserDTO user = userService.getUserByUsername(username);
            if (user.getUsername() == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "该用户不存在");
            }
            JSONArray roleList = userService.getRoleTreeByUserDn(user.getId().toString());
            JSONObject objJson = new JSONObject();

            objJson.put("result", user);
            objJson.put("roles", roleList);
            return new JsonResultBean(objJson);
        } catch (Exception e) {
            log.error("查询用户角色权限", e);
            return new JsonResultBean(JsonResultBean.FAULT, "查询用户角色权限错误，请联系管理员");
        }
    }

    /**
     * 新建用户
     * @author FanLu
     */
    @ApiOperation(value = "新建用户", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "username", value = "用户名,长度4——25", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "password", value = "密码，长度6——25", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "orgId", value = "所属企业id", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "fullName", value = "真实姓名,长度2——20", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "mail", value = "邮箱，长度小于60", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "mobile", value = "电话号码", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "gender", value = "性别(1:男；2:女)", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/newuser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean createUser(@Validated({ ValidGroupAdd.class }) UserDTO user,
        final BindingResult bindingResult) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
            // 数据校验
            if (bindingResult.hasErrors()) {
                String errorMsg = SpringBindingResultWrapper.warpErrors(bindingResult);
                return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
            } else {
                // 校验数据
                if (StringUtil.isEmpty(user.getUsername()) || StringUtil.isEmpty(user.getOrgId()) || StringUtil
                    .isEmpty(user.getPassword())) {
                    msg.put("errMsg", "添加失败，必填字段未填！");
                    return new JsonResultBean(msg);
                }
                // 校验所属企业是否存在
                if (organizationService.getOrganizationByUuid(user.getOrgId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                }
                // 校验电话号码
                if (StringUtils.isNotBlank(user.getMobile()) && !RegexUtils.checkMobile(user.getMobile()) && !RegexUtils
                    .checkPhone(user.getMobile())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "电话号码必须是电话或者手机！");
                }
                // 校验用户名是否重复
                UserDTO existUser = userService.getUserByUsername(user.getUsername());
                if (existUser.getId() != null) {
                    msg.put("errMsg", "添加失败，用户名已存在！");
                    return new JsonResultBean(msg);
                }
                //获取操作用户的IP
                userService.add(user);
                msg.put("flag", 1);
                msg.put("errMsg", "保存成功！");
                return new JsonResultBean(msg);
            }
        } catch (Exception e) {
            log.error("新建用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改用户
     * @author FanLu
     */
    @ApiOperation(value = "修改用户", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "username", value = "用户名,长度4——25", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "password", value = "密码(不修改则不填)，长度6——25", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "orgId", value = "所属企业id", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "fullName", value = "真实姓名,长度2——20", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "mail", value = "邮箱，长度小于60", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "mobile", value = "电话号码", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "gender", value = "性别(1:男；2:女)", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateUser(@Validated({ ValidGroupUpdate.class }) UserDTO user,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                JSONObject msg = new JSONObject();
                msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
                // 校验数据
                if (StringUtil.isEmpty(user.getUsername()) || StringUtil.isEmpty(user.getOrgId())) {
                    msg.put("errMsg", "修改失败，必填字段未填！");
                    return new JsonResultBean(msg);
                }
                // 校验用户是否存在
                UserDTO existUser = userService.getUserByUsername(user.getUsername());
                if (existUser.getUsername() == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "用户不存在！");
                }
                // 校验所属企业是否存在
                if (organizationService.getOrganizationByUuid(user.getOrgId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                }
                // 校验电话号码
                if (StringUtils.isNotBlank(user.getMobile()) && !RegexUtils.checkMobile(user.getMobile()) && !RegexUtils
                    .checkPhone(user.getMobile())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "电话号码必须是电话或者手机！");
                }
                userService.update(user);
                msg.put("flag", 1);
                msg.put("errMsg", "修改成功！");
                return new JsonResultBean(msg);
            }
        } catch (Exception e) {
            log.error("修改用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "修改用户密码", authorizations = {
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
            if (StringUtils.isBlank(oldpass) && StringUtils.isBlank(newpass)) {
                return new JsonResultBean(JsonResultBean.FAULT, "旧密码或者新密码不能为空");
            }
            if (oldpass.length() > 25 || oldpass.length() < 6) {
                return new JsonResultBean(JsonResultBean.FAULT, "旧密码的长度在6——25个字符");
            }
            if (newpass.length() > 25 || newpass.length() < 6) {
                return new JsonResultBean(JsonResultBean.FAULT, "新密码的长度在6——25个字符");
            }
            UserDTO currentUser = userService.getCurrentUserInfo();
            JSONObject msg = new JSONObject();
            msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
            // 校验数据
            String currentPassword = currentUser.getPassword();
            if (!SecurityPasswordHelper.isPasswordValid(currentPassword, oldpass)) {
                msg.put("errMsg", "旧密码输入错误，请重新输入！");
                return new JsonResultBean(msg);
            }
            currentUser.setPassword(newpass);
            userService.update(currentUser);
            msg.put("flag", 1);
            msg.put("errMsg", "密码修改成功,请重新登录！");
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("修改用户密码异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "修改用户密码错误，请联系管理员");
        }
    }

    @ApiOperation(value = "创建体验账号", notes = "用于体验", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getExperenceUser", method = RequestMethod.GET)
    public void getExperenceUser(HttpServletResponse response, HttpSession session, String tel) {
        try {
            ResponseUntil.setResponseHeader(response); // 解决跨域问题
            // 注册账号，分配权限
            String password = tel.substring(tel.length() - 6);
            UserDTO existUser = userService.getUserByUsername(tel);
            if (existUser.getUsername() != null) { //若用户存在直接跳转至登录界面
                SecurityContextHolder.clearContext();
                session.removeAttribute("SPRING_SECURITY_CONTEXT");
                response.sendRedirect("/clbs");
                return;
            }
            experienceRegister(tel, password); //若用户不存在则新增用户
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

    public void experienceRegister(String username, String password) throws Exception {
        // 注册账号
        Calendar curr = Calendar.getInstance();
        curr.set(Calendar.YEAR, curr.get(Calendar.YEAR) + 1);
        Date date = curr.getTime();
        String d = DateFormatUtils.format(date, DATE_FORMAT);
        // 根据企业的UUID, 获取groupId
        OrganizationLdap org = organizationService.getOrganizationByUuid(experienceUuid);
        UserDTO user = new UserDTO();
        // 如果根据UUID查询不出企业数据, 则设置默认的experienceId
        if (Objects.nonNull(org) && StringUtils.isNotEmpty(org.getEntryDN())) {
            user.setOrgDn(org.getEntryDN().replace(",dc=zwlbs,dc=com", ""));
        } else {
            user.setOrgDn(experienceId);
        }
        user.setOrgName("平台体验");
        user.setGender("1");
        user.setAuthorizationDate(d);
        user.setState("1");
        user.setUsername(username);
        user.setPassword(password);
        userService.add(user);
        // 分配角色
        user = userService.getUserByUsername(username);
        userService.updateUserRole(user.getId().toString(), experienceRoleId);
        // 授权
        List<GroupDTO> groups = groupService.getGroupsByOrgId(experienceUuid);
        List<String> groupIds = new ArrayList<>();
        for (GroupDTO group : groups) {
            groupIds.add("\"" + group.getId() + "\"");
        }
        userGroupService.addGroups2User(user.getId().toString(), groupIds.toString());
    }

}
