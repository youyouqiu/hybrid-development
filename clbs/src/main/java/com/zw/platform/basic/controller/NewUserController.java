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
 * @Title: ?????????controller
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
     * ??????????????????
     * @author FanLu
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getPageByKeyword(UserQuery query) {

        UserPageQuery userPageQuery = UserPageQuery.transform(query);
        if (StringUtils.isEmpty(query.getGroupName())) {
            //???????????????????????????????????????????????????????????????????????????
            String currentUserOrgDn = userService.getCurrentUserOrgDn();
            userPageQuery.setOrgDn(currentUserOrgDn);
            userPageQuery.setSearchSubFlag(true);
        }
        try {
            Page<UserBean> userPage = userService.getPageByKeyword(userPageQuery);
            return new PageGridBean(userPageQuery, userPage, true);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT, "??????????????????");
        }
    }

    /**
     * ????????????????????????
     * @author FanLu
     */
    @RequestMapping(value = "/roleList_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView roleList(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(USER_ROLE_PAGE);
            UserDTO user = userService.getUserByEntryDn(id);
            if (user == null) {
                log.error("??????????????????,??????dn:{}", id);
                throw new RuntimeException("??????????????????");
            }
            JSONArray roleList = userService.getRoleTreeByUserDn(id);
            mav.addObject("result", user);
            mav.addObject("roles", roleList);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????????????????
     * @author FanLu
     */
    @RequestMapping(value = "/updateRolesByUser.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateResourcesByRole(@RequestParam("userId") final String userDn,
        @RequestParam("roleIds") final String roleIds) {
        if (StringUtils.isEmpty(userDn) && StringUtils.isEmpty(roleIds)) {
            throw new RuntimeException("??????????????????");
        }
        try {
            // ?????????????????????IP
            if (StringUtils.isNotEmpty(roleIds)) {
                List<String> roleList = Arrays.asList(roleIds.split(","));
                // ?????????????????????????????????????????????
                boolean flag = userService.compareAllotRole(SystemHelper.getCurrentUser().getId().toString(), roleList);
                if (!flag) {
                    return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????????????????");
                }
            }
            return userService.updateUserRole(userDn, roleIds);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
    }

    /**
     * ????????????
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
            // ????????????
            mav.addObject("operationList", operationService.findAll());
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
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
            // ????????????
            UserDTO userDTO = UserDTO.transform(user);
            JsonResultBean resultBean = userService.add(userDTO);
            if (!resultBean.isSuccess()) {
                return resultBean;
            }
            JSONObject msg = new JSONObject();
            msg.put("flag", 1);
            msg.put("errMsg", "???????????????");
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
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
            // ????????????
            userDTO.setSendDownCommand("");
            UserBean user = userDTO.transform(userDTO, BeanCopier.create(UserDTO.class, UserBean.class, false));
            mav.addObject("result", user);
            mav.addObject("userId", user.getId().toString());
            // ????????????
            List<Operations> operationList = operationService.findAll();
            mav.addObject("operationList", operationList);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
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
            msg.put("errMsg", "???????????????");
            // ??????????????????????????????redis
            RedisKey userStateKey = HistoryRedisKeyEnum.USER_STATE.of(user.getUsername() + "_state");
            RedisHelper.setString(userStateKey, user.getState());
            if (StringUtils.isNotBlank(user.getState()) && "0".equals(user.getState())) {
                userService.expireUserSession(user.getUsername());
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @author lijie
     */
    @RequestMapping(value = "/batchEdit.gsp", method = RequestMethod.GET)
    public ModelAndView batchUpdateUser() {
        try {
            return new ModelAndView(BATCH_EDIT_PAGE);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/verification", method = RequestMethod.POST)
    @ResponseBody
    public boolean verificationAuthorizationDate(String authorizationDate) {
        try {
            // ?????????????????????id
            String userDn = SystemHelper.getCurrentUser().getId().toString();
            // ???????????????????????????
            UserDTO userDTO = userService.getUserByEntryDn(userDn);
            // ???????????????????????????????????????
            String loginUserAuthorizationDate = userDTO.getAuthorizationDate();
            // ????????????????????????????????????????????????????????????
            if (StringUtils.isNotEmpty(authorizationDate) && StringUtils.isNotEmpty(loginUserAuthorizationDate)) {
                // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????.?????????????????????????????????????????????????????????????????????????????????????????????
                Date d = DateUtils.parseDate(loginUserAuthorizationDate.trim(), DateUtil.DATE_Y_M_D_FORMAT);
                Date s = DateUtils.parseDate(authorizationDate.trim(), DateUtil.DATE_Y_M_D_FORMAT);
                return s.getTime() <= d.getTime();
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return false;
        }
    }

    /**
     * ???????????????????????????
     */
    @RequestMapping(value = "/verifyUserName", method = RequestMethod.POST)
    @ResponseBody
    public boolean compareUserName(String userName) {
        try {
            if (StringUtils.isEmpty(userName)) {
                return false;
            }
            // ???????????????????????????,?????????false????????????????????????true??????????????????????????????
            UserDTO userDTO = userService.getUserByUsername(userName);
            return !userName.equals(userDTO.getUsername());
        } catch (Exception e) {
            log.error("????????????????????????????????????????????????", e);
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
                throw new RuntimeException("???????????????");
            }
            model.addAttribute("user", user);
            return PROFILE_PAGE;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
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
            log.error("??????????????????????????????", e);
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
            // 0????????? 1??? ?????? 2???????????????
            msg.put("flag", 2);
            // ????????????
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @author FanLu
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        if (StringUtils.isEmpty(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
        }
        try {
            //????????????
            userService.deleteTalkBackDispatcherRole(id);
            //??????clbs???????????????
            final UserDTO deleted = userService.delete(id);
            if (deleted != null) {
                inspectionAndSupervisionService.deleteByUsername(Collections.singletonList(deleted.getUsername()));
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @author FanLu
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(String deltems) {
        if (StringUtils.isEmpty(deltems)) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
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
            log.error("????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/vehiclePer.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveVehiclePer(@RequestParam("id") final String userDn,
        @RequestParam("userVehicleList") final String groupIds) {
        if (StringUtils.isEmpty(groupIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????????????????");
        }
        if (StringUtils.isEmpty(userDn)) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
        }
        try {
            // ?????????????????????IP
            userGroupService.addGroups2User(userDn, groupIds);
            return new JsonResultBean(JsonResultBean.SUCCESS, "????????????");
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????id????????????????????????
     * @param type ?????????????????????
     */
    @RequestMapping(value = "/groupTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findGroupTree(String type) {
        try {
            JSONObject obj = new JSONObject();
            JSONArray result = new JSONArray();
            // ???????????????????????????id
            List<OrganizationLdap> orgs = userService.getCurrentUseOrgList();
            if (CollectionUtils.isEmpty(orgs)) {
                return new JsonResultBean("");
            }
            List<UserDTO> list = Lists.newLinkedList();
            // ?????????????????????????????????????????????id???list
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
                // ???????????????????????????????????????
                userObj.put("checked", true);
                result.add(userObj);
            }
            // ?????????????????????
            result.addAll(JsonUtil.getOrgTree(orgs, type));
            obj.put("tree", result);
            obj.put("size", value);
            // ????????????
            String resultValue = ZipUtil.compress(obj.toJSONString());
            return new JsonResultBean(resultValue);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????id????????????????????????
     * @param type ?????????????????????
     */
    @RequestMapping(value = "/chatUserTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean chatUserTree(String type, String groupId) {
        try {
            String data = userService.getChatGroupUserList(type, groupId);
            // ????????????
            String resultValue = ZipUtil.compress(data);
            return new JsonResultBean(resultValue);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
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
            log.error("????????????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????????????????");
        }
    }

}
