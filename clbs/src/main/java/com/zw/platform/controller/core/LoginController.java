package com.zw.platform.controller.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.zw.config.MapKeyConfig;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.RedisServiceUtils;
import com.zw.platform.commons.CustomAuthenticationKeyGenerator;
import com.zw.platform.commons.CustomTokenServices;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.LoginInResourceInfo;
import com.zw.platform.domain.basicinfo.Personalized;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.realTimeVideo.FtpBean;
import com.zw.platform.domain.realTimeVideo.Parameter;
import com.zw.platform.service.intercomplatform.PersonalizedService;
import com.zw.platform.service.realTimeVideo.ResourceListService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.DecryptionUtil;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.CaptchaUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nonnull;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ?????????????????????
 * @author Administrator
 */
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private PersonalizedService personalizedService;

    /**
     * ??????????????????
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * ?????????????????????????????????????????????????????????
     */
    private static boolean loginFlag = false;

    @Autowired
    private CustomTokenServices defaultTokenServices;

    @Autowired
    private ResourceListService resourceListService;

    @Autowired
    private OrganizationService organizationService;

    @Value("${login.fail.error}")
    private String loginFail;

    @Value("${validate.code.error}")
    private String validateCodeError;

    @Value("${login.timeout.error}")
    private String loginTimeoutError;

    @Value("${login.expired.error}")
    private String loginExpiredError;

    @Value("${login.changeState.error}")
    private String loginChangeStateError;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${login.user.stop}")
    private String loginUserStop;

    @Value("${account.stop.tima}")
    private String accountStopTime;

    @Value("${account.end.tima}")
    private String accountEndTime;

    @Value("${mediaServer.host}")
    private String videoUrl;

    @Value("${mediaServer.port.websocket.video}")
    private String videoPort;

    @Value("${mediaServer.port.websocket.audio}")
    private String audioPort;

    @Value("${mediaServer.port.websocket.resource}")
    private String resourcePort;

    @Value("${mediaServer.port.ftp}")
    private String ftpResourcePort;

    @Value("${module.decryption}")
    private boolean enableDecryption;

    @Value("${adas.risk.people.name}")
    private String riskPeopleName;

    /**
     * ?????????????????????????????????
     */
    @Value("${module.adas.enable}")
    private boolean adasSwitch;

    /**
     * ????????????????????????????????????
     */
    @Value("${max.number.assignment.monitor:100}")
    private Integer maxNumberAssignmentMonitor;

    @Autowired
    private MapKeyConfig mapKeysConfig;

    /**
     * ????????????
     */
    private static final String INFO_CONFIG = "/m/infoconfig/infoinput/list";

    /**
     * ????????????
     */
    private static final String MINOTOR = "/v/monitoring/realTimeMonitoring";

    /**
     * ????????????
     */
    private static final String TRACK = "/v/monitoring/trackPlayback";

    /**
     * ????????????
     */
    private static final String OIL_CALIBRATION = "/v/oilmassmgt/oilcalibration/list";

    /**
     * ????????????
     */
    private static final String OIL_MGT = "/v/oilmgt/list";

    /**
     * ????????????
     */
    private static final String OIL_QUANTITY_STATISTICS = "/v/oilmassmgt/oilquantitystatistics/list";

    /**
     * ????????????
     */
    private static final String MILEAGE_MGT = "/v/meleMonitor/mileStatistics/list";

    /**
     * ????????????
     */
    private static final String BIG_DATA = "/m/reportManagement/bigDataReport/list";

    /**
     * ????????????
     */
    private static final String ALARM = "/a/search/list";

    /**
     * ????????????
     */
    private static final String MINORTOR_VIDEO = "/realTimeVideo/video/list";

    /**
     * ????????????
     */
    private static final String RESOURCE_LIST = "/realTimeVideo/resource/list";

    private static final Logger log = LogManager.getLogger(LoginController.class);

    /**
     * ?????????????????????
     * @param request request
     * @param session session
     * @param model   model
     * @param type    type
     * @return result
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(@RequestParam(value = "type", required = false) final String type, final ModelMap model,
        final HttpSession session, HttpServletRequest request) {
        try {
            model.put("isFirst", true);
            model.put("random", Math.random());
            model.put("enableDecryption", enableDecryption);

            String loginKey = DecryptionUtil.generateKey(session.getId());
            model.put("key", loginKey);
            if ("error".equals(type)) {
                model.put("errorMsg", loginFail);
                model.put("isFirst", false);
            } else if ("logout".equals(type)) {
                model.put("errorMsg", "");
            } else if ("timeout".equals(type)) {
                model.put("errorMsg", loginTimeoutError);
            } else if ("expired".equals(type)) {
                model.put("errorMsg", loginExpiredError);
            } else if ("errorCode".equals(type)) {
                model.put("errorMsg", validateCodeError);
                model.put("isFirst", false);
            } else if ("stop".equals(type)) {
                model.put("errorMsg", loginUserStop);
            } else if ("changeState".equals(type)) {
                model.put("errorMsg", loginChangeStateError);
                if (StringUtils.isNotBlank(SystemHelper.getCurrentUsername())) {
                    log.info("????????????????????????????????????");
                    userService.expireUserSession(SystemHelper.getCurrentUsername());
                }
            }
            Cookie[] cookies = request.getCookies();
            String useName = "";
            if (cookies != null && cookies.length > 0) {
                for (Cookie cooky : cookies) {
                    if ("userName".equals(cooky.getName())) {
                        useName = URLDecoder.decode(cooky.getValue(), "UTF-8");
                        break;
                    }
                }
            }
            //???????????????????????????
            String platformSite = request.getRequestURL().toString().split(request.getRequestURI())[0].substring(7);
            Personalized personalized = null;
            if (!"".equals(useName)) {
                UserDTO user = userService.getUserByUsername(useName);
                if (user == null || user.getId() == null) {
                    // ??????????????????????????????????????????
                    personalized = personalizedService.find("defult");
                } else {
                    String id = user.getId().toString();
                    // ????????????id(????????????id????????????????????????)
                    int beginIndex = id.indexOf(",");
                    String orgDn = id.substring(beginIndex + 1);
                    String[] strs = orgDn.split(",");
                    for (int i = 0; i < strs.length; i++) {
                        // ???????????????uuid
                        String uuid = organizationService.getOrgByEntryDn(orgDn).getUuid();
                        personalized = personalizedService.find(uuid);
                        if (null == personalized) {
                            // ????????????id(????????????id????????????????????????)
                            int index = orgDn.indexOf(",");
                            orgDn = orgDn.substring(index + 1);
                        } else {
                            break;
                        }
                    }
                    if (null == personalized) {
                        personalized = personalizedService.find("defult");
                    }
                }
            } else {
                //?????????????????????????????????????????????????????????????????????????????????
                personalized = personalizedService.findByPlatformSite(platformSite);
                if (null == personalized) {
                    // ??????????????????????????????????????????
                    personalized = personalizedService.find("defult");
                }
            }
            if (personalized != null) {
                session.setAttribute("copyright", personalized.getCopyright());
                session.setAttribute("recordNumber", personalized.getRecordNumber());
                session.setAttribute("loginLogo", personalized.getLoginLogo());
                session.setAttribute("webIco", personalized.getWebIco());
                session.setAttribute("isAdmin", "");
                session.setAttribute("loginBackground", personalized.getLoginBackground());
                session.setAttribute("loginPersonalized", personalized.getLoginPersonalization());
            }
            String profile = request.getServletContext().getInitParameter("spring.profiles.active");
            session.setAttribute("profile", profile);
            session.setAttribute("loginKey", loginKey);
            return "login";
        } catch (Exception e) {
            model.put("errorMsg", sysErrorMsg);
            log.error("????????????", e);
            return "login";
        }
    }

    /**
     * ????????????session???????????????
     */
    @RequestMapping(value = "/session", method = RequestMethod.POST)
    @ResponseBody
    public String getSessionAttribute(String keys, HttpServletRequest request) {
        String[] split = keys.split(",");
        HttpSession session = request.getSession();
        JSONObject object = new JSONObject();
        for (String key : split) {
            Object attribute = session.getAttribute(key);
            object.put(key, attribute);
        }
        return object.toJSONString();
    }

    /**
     * ???????????????
     * @param response response
     * @param session  session
     */
    @RequestMapping(value = "/getCaptcha.gsp")
    public void getImage(final HttpSession session, final HttpServletResponse response) {
        // ????????????????????????Session???
        CaptchaUtil.getCaptcha(session, response);
    }

    /**
     * ??????????????????????????????(code)
     * @param session  session
     * @return result
     */
    @RequestMapping(value = "/getCaptchaString", method = RequestMethod.GET)
    @ResponseBody
    public String getString(final HttpSession session) {
        // ????????????????????????Session???
        String uuid = UUID.randomUUID().toString();
        session.setAttribute("simpleCaptcha", uuid);
        return uuid;
    }

    /**
     * ???????????????????????????????????????
     * @param request  request
     * @param response response
     * @return return
     */
    @RequestMapping(value = "/")
    public String index(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            // ??????????????????????????????????????????????????????(session???????????????????????????,??????????????????,???????????????????????????,??????PC???????????????)
            if (request.getSession().getAttribute("userName") == null) {
                loginFlag = true;
            }
            UserLdap userLdap = SystemHelper.getCurrentUser();
            if (userLdap == null) {
                return "login";
            }
            UserDTO user = userService.getUserByEntryDn(userLdap.getId().toString());
            OAuth2AccessToken accessToken = getAccessToken();
            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????session???
            LdapName name = LdapUtils.newLdapName(user.getId() + "," + userService.getBaseLdapPath().toString());
            List<Group> roles = (List<Group>) roleService.getByMemberName(name);
            List<String> currentRoleNames = new ArrayList<>();
            List<Resource> resources;
            List<String> menuUrls;
            List<String> homeUrls = new ArrayList<>();
            List<String> permissionUrls = new ArrayList<>();
            for (Group role : roles) {
                currentRoleNames.add(role.getId().toString());
            }
            menuUrls = roleService.getMenuEditableByRoles(currentRoleNames);
            resources = roleService.getMenuByRoles(currentRoleNames);

            if (CollectionUtils.isNotEmpty(resources)) {
                for (Resource re : resources) {
                    if (StringUtils.isNotEmpty(re.getPermValue())) {
                        homeUrls.add(re.getPermValue());
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(resources)) {
                resources = loadMenuInfos(resources, permissionUrls);
                sortMenusSelf(resources);
                sortChildMenus(resources);
            }
            String[] allHomeUrls =
                new String[] { INFO_CONFIG, MINOTOR, TRACK, OIL_CALIBRATION, OIL_MGT, OIL_QUANTITY_STATISTICS,
                    MILEAGE_MGT, ALARM, BIG_DATA, MINORTOR_VIDEO, RESOURCE_LIST };
            for (int i = 0; i < allHomeUrls.length; i++) {
                if (homeUrls.contains(allHomeUrls[i])) {
                    request.getSession().setAttribute("home" + (i + 1), true);
                    continue;
                }
                request.getSession().setAttribute("home" + (i + 1), false);
            }
            request.getSession().setAttribute("menus", resources);
            request.getSession().setAttribute("adasSwitch", adasSwitch);
            request.getSession().setAttribute("permissions", menuUrls);
            request.getSession().setAttribute("userName", SystemHelper.getCurrentUsername());
            request.getSession().setAttribute("access_token", accessToken.getValue());
            request.getSession().setAttribute("refresh_token", accessToken.getRefreshToken().getValue());
            request.getSession().setAttribute("isAdmin", SystemHelper.isAdmin());
            request.getSession().setAttribute("permissionUrls", String.join(",", permissionUrls));
            request.getSession().setAttribute("maxNumberAssignmentMonitor", maxNumberAssignmentMonitor);

            OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
            request.getSession().setAttribute("groupId", currentUserOrg.getUuid());
            request.getSession().setAttribute("groupName", currentUserOrg.getName());
            request.getSession().setAttribute("userAuthorizationDate", user.getAuthorizationDate());
            request.getSession().setAttribute("userRole", getUserRole().toString());
            //??????????????????????????????session
            TypeCacheManger typeCacheManger = TypeCacheManger.getInstance();
            List<Map<String, String>> thingCategory = typeCacheManger.getDictionaryMapList("THING_CATEGORY");
            List<Map<String, String>> thingType = typeCacheManger.getDictionaryMapList("THING_TYPE");
            request.getSession().setAttribute("thingCategoryList", thingCategory);
            request.getSession().setAttribute("thingTypeList", thingType);
            //???????????????????????????code-key,value-value???????????????session
            if (thingCategory != null && thingCategory.size() > 0) {
                Map<String, String> thingCategoryDetail = thingCategory.stream()
                    .collect(Collectors.toMap(category -> category.get("code"), category -> category.get("value")));
                request.getSession().setAttribute("thingCategoryDetail", thingCategoryDetail);
            }
            if (thingType != null && thingType.size() > 0) {
                Map<String, String> thingTypeDetail =
                    thingType.stream().collect(Collectors.toMap(type -> type.get("code"), type -> type.get("value")));
                request.getSession().setAttribute("thingTypeDetail", thingTypeDetail);
            }
            if (loginFlag) {
                // ????????????ip
                String msg = "?????????" + SystemHelper.getCurrentUsername() + " ??????";
                logSearchService.addLog(ip, msg, "3", "", "-", "");
                String userName = SystemHelper.getCurrentUsername();
                if (!("admin".equals(userName))) {
                    List<Group> allGroup = roleService.getAllGroup();
                    for (Group group : allGroup) {
                        if (!group.getRoleName().equals(riskPeopleName)) {
                            continue;
                        }
                        if (group.getMembers().toString().contains(SystemHelper.getCurrentUser().getId().toString())) {
                            RedisServiceUtils.storeCustomerLoginState(userName);
                            break;
                        }
                    }
                }
            }
            // ??????????????????????????????redis
            RedisHelper
                .setString(HistoryRedisKeyEnum.USER_STATE.of(SystemHelper.getCurrentUsername()), user.getState());
            // ????????????ip
            request.getSession().setAttribute("videoUrl", videoUrl);
            request.getSession().setAttribute("videoPort", videoPort);
            request.getSession().setAttribute("audioPort", audioPort);
            request.getSession().setAttribute("resourcePort", resourcePort);
            request.getSession().setAttribute("ftpResourcePort", ftpResourcePort);
            // ?????????????????????
            Personalized personalize = personalizedService.findByPermission(currentUserOrg.getUuid(), currentRoleNames);
            // ??????????????????????????????
            if (personalize == null) {
                personalize = personalizedService.findByPermission("defult", currentRoleNames);
            }
            if (personalize != null) {
                String permValue = personalize.getFrontPageUrl();
                if (permValue != null && !"".equals(permValue)) {
                    String url = permValue.substring(1);
                    response.sendRedirect("/clbs/" + url);
                    return null;
                }
            }
            //web?????????, ??????app????????????????????????id
            WebSubscribeManager.getInstance().removeUserClientId(user.getUsername());
            request.getSession().setAttribute("mapKeys", JSON.toJSONString(mapKeysConfig.getMapKeys()));
            return "home";
        } catch (Exception e) {
            log.error("????????????", e);
            return "login";
        }
    }

    /**
     * ????????????token
     */
    @Nonnull
    private OAuth2AccessToken getAccessToken() {
        OAuth2Authentication auth = prepareOAuth2Authentication();
        OAuth2AccessToken accessToken = defaultTokenServices.getAccessToken(auth);
        if (accessToken == null) {
            accessToken = defaultTokenServices.createAccessToken(auth);
        }
        return accessToken;
    }

    private OAuth2Authentication prepareOAuth2Authentication() {
        Authentication authentication = SystemHelper.getAuthentication();
        Set<String> scope = new HashSet<>();
        scope.add("read");
        scope.add("write");
        scope.add("trust");
        final ImmutableMap<String, String> requestParameters =
                ImmutableMap.of(CustomAuthenticationKeyGenerator.SESSION_ID, SystemHelper.getSession().getId());
        OAuth2Request auth2Request = new OAuth2Request(requestParameters, "web_1", authentication.getAuthorities(),
                authentication.isAuthenticated(), scope, null, null, null, null);
        return new OAuth2Authentication(auth2Request, authentication);
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = "/getCurrentResource", method = RequestMethod.POST)
    @ResponseBody
    public LoginInResourceInfo getCurrentResource() {
        try {
            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????session???
            LdapName name = LdapUtils
                .newLdapName(SystemHelper.getCurrentUser().getId() + "," + userService.getBaseLdapPath().toString());
            List<Group> roles = (List<Group>) roleService.getByMemberName(name);
            List<String> currentRoleNames = new ArrayList<>();
            List<String> permissionUrls = new ArrayList<>();
            for (Group role : roles) {
                currentRoleNames.add(role.getId().toString());
            }
            List<Resource> resources = roleService.getMenuByRoles(currentRoleNames);
            if (CollectionUtils.isNotEmpty(resources)) {
                resources = loadMenuInfos(resources, permissionUrls);
                sortMenusSelf(resources);
                sortChildMenus(resources);
            }
            addProjectName(resources);
            return new LoginInResourceInfo(resources);
        } catch (Exception e) {
            log.error("????????????", e);
            return null;
        }
    }

    /**
     * ??????accessToken,ftp?????????videoUrl?????????????????????
     */
    @RequestMapping(value = "/getParameter", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getParameter(final HttpServletRequest request) {
        try {
            OAuth2AccessToken accessToken = getAccessToken();

            FtpBean ftp = resourceListService.getFtpName();
            String name = "FTP?????????";
            if (ftp != null) {
                name = ftp.getFtpName();
            }

            String groupId = (String) request.getSession().getAttribute("groupId");
            String permissionUrls = (String) request.getSession().getAttribute("permissionUrls");

            Parameter param = new Parameter();
            param.setGroupId(groupId);
            param.setFtpName(name);
            param.setVideoUrl(videoUrl);
            param.setVideoPort(videoPort);
            param.setAudioPort(audioPort);
            param.setAccessToken(accessToken.getValue());
            param.setResourcePort(resourcePort);
            param.setFtpResourcePort(ftpResourcePort);
            param.setPermissionUrls(permissionUrls);

            return new JsonResultBean(param);
        } catch (Exception e) {
            log.error("??????ftp?????????videoUrl????????????????????????" + e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????token
     */
    @RequestMapping(value = "/getAccessToken", method = RequestMethod.POST)
    @ResponseBody
    public String getAccessToken1() {
        try {
            return getAccessToken().getValue();
        } catch (Exception e) {
            log.error("????????????token", e);
            return null;
        }
    }

    /**
     * ????????????????????????
     */
    private void addProjectName(List<Resource> childMenus) {
        for (Resource menu : childMenus) {
            if (!StringUtils.isEmpty(menu.getPermValue()) && !"javascript:void(0);".equals(menu.getPermValue())) {
                menu.setPermValue("/clbs" + menu.getPermValue());
            }
            if (menu.getChildMenus().size() > 0) {
                addProjectName(menu.getChildMenus());
            }
        }
    }

    /**
     * ???????????????????????????????????????
     */
    @RequestMapping(value = "/home")
    public String getHome() {
        return "home";
    }

    /**
     * ??????
     * @return result
     */
    @RequestMapping("/admin")
    @ResponseBody
    public List<String> admin() {
        return Arrays.asList("zhangsan", "lisi", "wangwu");
    }

    /**
     * ??????????????????????????????
     * @return result
     * @author FanLu
     */
    @RequestMapping(value = "/denied", method = RequestMethod.GET)
    public String accessDenied() {

        return "denied";
    }

    /**
     * ????????????
     * @param allMenus allmenus
     * @return resource
     * @author FanLu
     */
    public List<Resource> loadMenuInfos(List<Resource> allMenus, List<String> permissionUrls) {

        List<Resource> menus = new ArrayList<>();

        for (Resource menu : allMenus) {
            if (StringUtils.isNotBlank(menu.getPermValue())) {
                permissionUrls.add(menu.getPermValue());
            }
            // ???????????????1?????????
            if ("0".equals(menu.getParentId())) {
                menus.add(menu);
                this.loadChildMenus(menu, allMenus);
            }

        }

        return menus;

    }

    /**
     * ???????????????
     * @author FanLu
     */
    private void loadChildMenus(Resource currentMenu, List<Resource> allMenus) {

        for (Resource menu : allMenus) {
            // ?????????????????????????????????
            if (menu.getParentId().equals(currentMenu.getId())) {
                currentMenu.addChild(menu);
                // ??????
                this.loadChildMenus(menu, allMenus);
            }

        }

    }

    /**
     * ??????????????????
     * @param resources resources
     * @author FanLu
     */
    public static void sortChildMenus(List<Resource> resources) {
        for (Resource resource : resources) {
            if (!resource.getChildMenus().isEmpty()) {
                List<Resource> child = resource.getChildMenus();
                sortMenusSelf(child);
                sortChildMenus(child);
            }
        }
    }

    /**
     * ??????????????????
     * @param resources resources
     * @author FanLu
     */
    public static void sortMenusSelf(List<Resource> resources) {
        resources.sort(new Comparator<Resource>() {
            /**
             * ????????????????????????,????????????????????????;
             */
            @Override
            public int compare(Resource o1, Resource o2) {
                Integer sortOrder1 = o1.getSortOrder();
                Integer sortOrder2 = o2.getSortOrder();
                if (!Objects.equals(sortOrder2, sortOrder1)) {
                    return sortOrder1 - sortOrder2;
                }
                String codeNum1 = o1.getCodeNum();
                String codeNum2 = o2.getCodeNum();
                if (StringUtils.isNotBlank(codeNum1) && StringUtils.isNotBlank(codeNum2)) {
                    return Integer.parseInt(codeNum1) - Integer.parseInt(codeNum2);
                }
                return sortOrder1 - sortOrder2;
            }
        });
    }

    /**
     * ????????????
     * @return result
     */
    @RequestMapping(value = "/out", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean logout() {
        try {
            OAuth2AccessToken accessToken = getAccessToken();
            String value = accessToken.getValue();
            if (StringUtils.isNotBlank(value)) {
                defaultTokenServices.revokeToken(value);
            }
            // ??????????????????
            userService.updateUserOffline(SystemHelper.getCurrentUsername());
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ?????????????????????????????????
     * @return result
     */
    @RequestMapping(value = "/inspectAuthorizationDate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean inspectUserAuthorizationDate() {
        try {
            JSONObject msg = new JSONObject();
            boolean flag = false;
            // ???session????????????????????????,??????????????????????????????????????????(??????:?????????????????????,????????????);
            if (loginFlag) {
                // ??????????????????????????????
                String loginUserName = SystemHelper.getCurrentUsername();
                // ?????????????????????id
                String loginId = SystemHelper.getCurrentUser().getId().toString();
                // ???????????????????????????
                UserDTO userBean = userService.getUserByEntryDn(loginId);
                // ???????????????????????????
                String addUserAuthorizationDate = userBean.getAuthorizationDate();
                if (!"admin".equals(loginUserName) && !"".equals(addUserAuthorizationDate)
                    && addUserAuthorizationDate != null && !"null".equals(addUserAuthorizationDate)) {
                    addUserAuthorizationDate = addUserAuthorizationDate.trim();
                    // ?????????????????????
                    Date date = new Date();
                    Date authorizationDate = DateUtils.parseDate(addUserAuthorizationDate, DATE_FORMAT);
                    long authorizationTime = authorizationDate.getTime();
                    long expireTime = date.getTime();
                    // ????????????????????????-???????????????
                    long day = (authorizationTime - expireTime) / (1000 * 60 * 60 * 24);
                    // ??????????????????????????????????????????<=10???,?????????????????????????????????
                    if (day <= 9 && day >= 0) {
                        flag = true;
                        if (day == 0) {
                            msg.put("errMsg", accountStopTime);
                        } else {
                            msg.put("errMsg", accountEndTime + day + "???");
                        }
                    }
                }
            }
            loginFlag = false;
            if (flag) {
                return new JsonResultBean(msg);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("???????????????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ???????????????????????????
     */
    private List<String> getUserRole() {
        // ??????????????????????????????id
        List<String> roleName = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // ?????????????????????????????????
        Name name = LdapUtils.newLdapName(userId + "," + userService.getBaseLdapPath().toString());
        List<Group> currentRoles = (List<Group>) roleService.getByMemberName(name);
        for (Group group : currentRoles) {
            if (group.getRoleName() != null) {
                roleName.add(group.getName());
            }
        }
        return roleName;
    }

    @RequestMapping(value = "/check", method = RequestMethod.GET)
    public String checkPage(final ModelMap model) {
        try {
            String filePath = resourceListService.checkFtp();
            model.put("filePath", filePath);
            return "check";
        } catch (Exception e) {
            model.put("errorMsg", sysErrorMsg);
            log.error("????????????", e);
            return "check";
        }
    }

    @RequestMapping(value = "/mapKeys", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> mapKeys() {
        return mapKeysConfig.getMapKeys();
    }

}
