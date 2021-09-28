package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.query.UserPageQuery;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SecurityPasswordHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Operations;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.core.query.UserQuery;
import com.zw.platform.dto.common.DropdownDTO;
import com.zw.platform.service.core.OperationService;
import com.zw.platform.service.reportManagement.InspectionAndSupervisionService;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: 用户新controller
 * @date 2021/1/1114:22
 */
@Controller
@RequestMapping("/c/user")
@Slf4j
public class NewUserController {

    private static final String PROFILE_PAGE = "core/uum/user/profile";

    private static final String CHANGE_PWD_PAGE = "core/uum/user/changePwd";

    private static final String LIST_PAGE = "core/uum/user/list";

    private static final String EDIT_PAGE = "core/uum/user/edit";

    private static final String BATCH_EDIT_PAGE = "core/uum/user/batchEdit";

    private static final String ADD_PAGE = "core/uum/user/add";

    private static final String USER_ROLE_PAGE = "core/uum/user/userRoles";

    private static final String VEHICLE_PAGE = "core/uum/user/vehiclePer";

    private static final String LOGIN_PAGE = "login";

    private static final String ERROR_PAGE = "html/errors/error_exception";

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

    @Autowired
    private UserService userService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private InspectionAndSupervisionService inspectionAndSupervisionService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String list(ModelMap map) {
        map.put("hasRole", SystemHelper.checkPermissionEditable());
        map.put("userId", SystemHelper.getCurrentUser().getId().toString());
        return LIST_PAGE;
    }

    /**
     * 查询用户信息
     * @author FanLu
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getPageByKeyword(UserQuery query) {

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
    @RequestMapping(value = "/roleList_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView roleList(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(USER_ROLE_PAGE);
            UserDTO user = userService.getUserByEntryDn(id);
            if (user == null) {
                log.error("该用户不存在,用户dn:{}", id);
                throw new RuntimeException("该用户不存在");
            }
            JSONArray roleList = userService.getRoleTreeByUserDn(id);
            mav.addObject("result", user);
            mav.addObject("roles", roleList);
            return mav;
        } catch (Exception e) {
            log.error("分配角色界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 更新某个用户下的角色
     * @author FanLu
     */
    @RequestMapping(value = "/updateRolesByUser.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateResourcesByRole(@RequestParam("userId") final String userDn,
        @RequestParam("roleIds") final String roleIds) {
        if (StringUtils.isEmpty(userDn) && StringUtils.isEmpty(roleIds)) {
            throw new RuntimeException("参数传递错误");
        }
        try {
            // 获取操作用户的IP
            if (StringUtils.isNotEmpty(roleIds)) {
                List<String> roleList = Arrays.asList(roleIds.split(","));
                // 分配用户角色属于操作用户的角色
                boolean flag = userService.compareAllotRole(SystemHelper.getCurrentUser().getId().toString(), roleList);
                if (!flag) {
                    return new JsonResultBean(JsonResultBean.FAULT, "分配用户角色不能超过操作用户的角色");
                }
            }
            return userService.updateUserRole(userDn, roleIds);
        } catch (Exception e) {
            log.error("更新用户下的角色信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "分配角色失败");
        }
    }

    /**
     * 新建用户
     * @author FanLu
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/newuser", method = RequestMethod.GET)
    public ModelAndView initNewUser(@RequestParam("uuid") String orgId) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            String userOrgId = userService.getCurrentUserOrg().getUuid();
            if (orgId.equals(userOrgId)) {
                orgId = "";
            }
            if (!"".equals(orgId)) {
                OrganizationLdap organization = organizationService.getOrganizationByUuid(orgId);
                if (organization == null) {
                    return new ModelAndView(ERROR_PAGE);
                }
                mav.addObject("orgId", organization.getId().toString());
                mav.addObject("groupName", organization.getName());
            }
            // 行业类别
            mav.addObject("operationList", operationService.findAll());
            return mav;
        } catch (Exception e) {
            log.error("新建用户界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 新建用户
     * @author FanLu
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/newuser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean createUser(@Valid UserBean user, final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMsg = SpringBindingResultWrapper.warpErrors(bindingResult);
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
        }
        try {
            // 校验数据
            UserDTO userDTO = UserDTO.transform(user);
            JsonResultBean resultBean = userService.add(userDTO);
            if (!resultBean.isSuccess()) {
                return resultBean;
            }
            JSONObject msg = new JSONObject();
            msg.put("flag", 1);
            msg.put("errMsg", "保存成功！");
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("新建用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改用户
     * @author FanLu
     */
    @RequestMapping(value = "/edit_{id}", method = RequestMethod.GET)
    public ModelAndView initUpdateUser(@PathVariable String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            UserDTO userDTO = userService.getUserByEntryDn(id);
            if (userDTO == null) {
                return new ModelAndView(ERROR_PAGE);
            }
            // 无需展示
            userDTO.setSendDownCommand("");
            UserBean user = userDTO.transform(userDTO, BeanCopier.create(UserDTO.class, UserBean.class, false));
            mav.addObject("result", user);
            mav.addObject("userId", user.getId().toString());
            // 行业类别
            List<Operations> operationList = operationService.findAll();
            mav.addObject("operationList", operationList);
            return mav;
        } catch (Exception e) {
            log.error("修改用户界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改用户
     * @author FanLu
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updateUser(UserBean user, final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMsg = SpringBindingResultWrapper.warpErrors(bindingResult);
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
        }
        try {
            UserDTO userDTO = UserDTO.transform(user);
            JsonResultBean jsonResultBean = userService.update(userDTO);
            if (!JsonResultBean.SUCCESS) {
                return jsonResultBean;
            }
            JSONObject msg = new JSONObject();
            msg.put("flag", 1);
            msg.put("errMsg", "修改成功！");
            // 将用户的启停状态存入redis
            RedisKey userStateKey = HistoryRedisKeyEnum.USER_STATE.of(user.getUsername() + "_state");
            RedisHelper.setString(userStateKey, user.getState());
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
     * 批量修改用户
     * @author lijie
     */
    @RequestMapping(value = "/batchEdit.gsp", method = RequestMethod.GET)
    public ModelAndView batchUpdateUser() {
        try {
            return new ModelAndView(BATCH_EDIT_PAGE);
        } catch (Exception e) {
            log.error("批量修改用户界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 批量修改用户
     * @author lijie
     */
    @RequestMapping(value = "/batchEdit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean batchUpdateUser(final String userId, String userName, String password, String state,
        String authorizationDate) {
        try {
            userService.updateBatch(userId, userName, password, state, authorizationDate);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("批量修改用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 授权截止日期验证
     */
    @RequestMapping(value = "/verification", method = RequestMethod.POST)
    @ResponseBody
    public boolean verificationAuthorizationDate(String authorizationDate) {
        try {
            // 当前登录用户的id
            String userDn = SystemHelper.getCurrentUser().getId().toString();
            // 当前登录用户的信息
            UserDTO userDTO = userService.getUserByEntryDn(userDn);
            // 当前登录用户的授权截止日期
            String loginUserAuthorizationDate = userDTO.getAuthorizationDate();
            // 先判断验证操作用户的授权截止日期是否为空
            if (StringUtils.isNotEmpty(authorizationDate) && StringUtils.isNotEmpty(loginUserAuthorizationDate)) {
                // 如果在要验证的授权截止日期不为空的前提下当前操作用户的授权截止时间不等于空.那么（创建的用户的授权截止不能大于当前操作用户的授权截止时间）
                Date d = DateUtils.parseDate(loginUserAuthorizationDate.trim(), DateUtil.DATE_Y_M_D_FORMAT);
                Date s = DateUtils.parseDate(authorizationDate.trim(), DateUtil.DATE_Y_M_D_FORMAT);
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
    @RequestMapping(value = "/verifyUserName", method = RequestMethod.POST)
    @ResponseBody
    public boolean compareUserName(String userName) {
        try {
            if (StringUtils.isEmpty(userName)) {
                return false;
            }
            // 校验用户名是否重复,返回为false为平台已经存在，true为平台不存在可以录入
            UserDTO userDTO = userService.getUserByUsername(userName);
            return !userName.equals(userDTO.getUsername());
        } catch (Exception e) {
            log.error("添加用户时验证用户名是否重复异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String showViewLdapUserProfilePage(ModelMap model) {
        try {
            final UserLdap user = (UserLdap) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            OrganizationLdap organization = userService.getCurrentUserOrg();
            user.setOu(organization.getName());
            UserDTO currentUserInfo = userService.getCurrentUserInfo();
            if (currentUserInfo == null) {
                throw new RuntimeException("用户不存在");
            }
            model.addAttribute("user", user);
            return PROFILE_PAGE;
        } catch (Exception e) {
            log.error("授权截止日期验证异常", e);
            return ERROR_PAGE;
        }
    }

    @RequestMapping(value = "/changePwd", method = RequestMethod.GET)
    public String getChangePwd(ModelMap model) {

        try {
            UserDTO currentUserInfo = userService.getCurrentUserInfo();
            if (currentUserInfo != null) {
                model.addAttribute("user", currentUserInfo);
                return CHANGE_PWD_PAGE;
            }
            return LOGIN_PAGE;
        } catch (Exception e) {
            log.error("修改密码界面弹出异常", e);
            return ERROR_PAGE;
        }
    }

    @RequestMapping(value = "/changePwd", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean changePwd(@RequestParam("oldpass") final String oldPwd,
        @RequestParam("newpass") final String newPwd) {
        try {
            UserDTO currentUserInfo = userService.getByDn(SystemHelper.getCurrentUser().getId().toString());
            JSONObject msg = new JSONObject();
            // 0：失败 1： 通过 2：校验失败
            msg.put("flag", 2);
            // 校验数据
            String b = currentUserInfo.getPassword();
            String[] bt = b.split(",");
            byte[] bb = new byte[bt.length];
            for (int i = 0; i < bt.length; i++) {
                int u = Integer.parseInt(bt[i]);
                bb[i] = (byte) u;
            }
            String s = new String(bb);
            if (!SecurityPasswordHelper.isPasswordValid(s, oldPwd)) {
                msg.put("errMsg", oldPasswordError);
                return new JsonResultBean(msg);
            }
            userService.updatePassword(newPwd, null);
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
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        if (StringUtils.isEmpty(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误");
        }
        try {
            //删除对讲
            userService.deleteTalkBackDispatcherRole(id);
            //删除clbs主干的逻辑
            final UserDTO deleted = userService.delete(id);
            if (deleted != null) {
                inspectionAndSupervisionService.deleteByUsername(Collections.singletonList(deleted.getUsername()));
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("删除用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除用户
     * @author FanLu
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(String deltems) {
        if (StringUtils.isEmpty(deltems)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误");
        }
        String[] userDns = deltems.split(";");
        try {
            userService.deleteTalkBackDispatcherRoles(userDns);
            final List<UserDTO> deleted = userService.deleteBatch(Arrays.asList(userDns));
            if (CollectionUtils.isNotEmpty(deleted)) {
                final List<String> usernames = deleted.stream().map(UserDTO::getUsername).collect(Collectors.toList());
                inspectionAndSupervisionService.deleteByUsername(usernames);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("批量删除用户异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 分配分组
     * @author wangying
     */
    @RequestMapping(value = "/vehiclePer_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView getVehiclePer(@PathVariable("id") String userDn) {
        try {
            ModelAndView mav = new ModelAndView(VEHICLE_PAGE);
            UserDTO userDTO = userService.getUserByEntryDn(userDn);
            String tree = userGroupService.distributeUserGroupTree(userDn).toJSONString();
            UserBean userBean = userDTO.transform(userDTO, BeanCopier.create(UserDTO.class, UserBean.class, false));
            mav.addObject("user", userBean);
            mav.addObject("vehicleTree", tree);
            return mav;
        } catch (Exception e) {
            log.error("授权界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/vehiclePer.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveVehiclePer(@RequestParam("id") final String userDn,
        @RequestParam("userVehicleList") final String groupIds) {
        if (StringUtils.isEmpty(groupIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "请至少勾选一个分组授权查看！");
        }
        if (StringUtils.isEmpty(userDn)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误！");
        }
        try {
            // 获取操作用户的IP
            userGroupService.addGroups2User(userDn, groupIds);
            return new JsonResultBean(JsonResultBean.SUCCESS, "授权成功");
        } catch (Exception e) {
            log.error("授权查看分组权限异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据组织id查询组织下的用户
     * @param type 根节点是否可选
     */
    @RequestMapping(value = "/groupTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findGroupTree(String type) {
        try {
            JSONObject obj = new JSONObject();
            JSONArray result = new JSONArray();
            // 根据用户名获取用户id
            List<OrganizationLdap> orgs = userService.getCurrentUseOrgList();
            if (CollectionUtils.isEmpty(orgs)) {
                return new JsonResultBean("");
            }
            List<UserDTO> list = Lists.newLinkedList();
            // 遍历得到当前用户组织及下级组织id的list
            for (OrganizationLdap org : orgs) {
                List<UserDTO> user = organizationService.fuzzyUsersByOrgDn(null, org.getId().toString(), false);
                if (CollectionUtils.isEmpty(user)) {
                    continue;
                }
                list.addAll(user);
            }
            int value = 0;
            JSONObject userObj;
            String userDn;
            for (UserDTO userDTO : list) {
                value += list.size();
                userObj = new JSONObject();
                userDn = userDTO.getId().toString();
                userObj.put("id", userDTO.getUuid());
                userObj.put("name", userDTO.getUsername());
                userObj.put("type", "user");
                userObj.put("iconSkin", "userSkin");
                userObj.put("pId", userDn.substring(userDn.indexOf(',') + 1));
                // 用户在线率查询，默认都勾选
                userObj.put("checked", true);
                result.add(userObj);
            }
            // 组装组织树结构
            result.addAll(JsonUtil.getOrgTree(orgs, type));
            obj.put("tree", result);
            obj.put("size", value);
            // 压缩数据
            String resultValue = ZipUtil.compress(obj.toJSONString());
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

    @RequestMapping(value = "/dropdown", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean dropdown(@RequestParam(required = false) String orgDn) {
        try {
            if (StringUtils.isBlank(orgDn)) {
                orgDn = userService.getCurrentUserOrgDn();
            }
            List<UserDTO> userBeans = organizationService.fuzzyUsersByOrgDn(null, orgDn, false);
            List<DropdownDTO> dropDown = null == userBeans
                    ? new ArrayList<>()
                    : userBeans.stream()
                            .filter(u -> "1".equals(u.getState()))
                            .map(u -> new DropdownDTO(u.getUuid(), u.getUsername()))
                            .collect(Collectors.toList());
            return new JsonResultBean(dropDown);
        } catch (Exception e) {
            log.error("查询当前用户所属组织的用户下拉选", e);
            return new JsonResultBean(JsonResultBean.FAULT, "查询当前用户所属组织的用户下拉选");
        }
    }

}
