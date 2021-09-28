package com.zw.app.service.personalCenter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.webMaster.alarmType.AlarmType;
import com.zw.app.domain.webMaster.alarmType.AppAlarmConfigInfo;
import com.zw.app.domain.webMaster.personalized.AppConfigInfo;
import com.zw.app.domain.webMaster.personalized.LoginConfigInfo;
import com.zw.app.domain.webMaster.personalized.PersonalConfigInfo;
import com.zw.app.domain.webMaster.statistics.StatisticsConfig;
import com.zw.app.domain.webMaster.statistics.StatisticsConfigInfo;
import com.zw.app.repository.mysql.webMaster.Statistics.StatisticsDao;
import com.zw.app.repository.mysql.webMaster.alarmType.AppAlarmTypeDao;
import com.zw.app.repository.mysql.webMaster.monitorInfo.AppMonitorDao;
import com.zw.app.repository.mysql.webMaster.personalized.AppPersonalizedDao;
import com.zw.app.service.personalCenter.PersonalCenterService;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SecurityPasswordHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.core.UserRepo;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.GetIpAddr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * app个人中心
 * @author lijie
 * @date 2018/9/6 15:41
 */
@Service
public class PersonalCenterServiceImpl implements PersonalCenterService {

    @Autowired
    UserService userService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    AppPersonalizedDao appPersonalizedDao;
    @Autowired
    AppAlarmTypeDao appAlarmTypeDao;
    @Autowired
    StatisticsDao statisticsDao;
    @Autowired
    AppMonitorDao appMonitorDao;
    @Autowired
    LogSearchServiceImpl logSearchServiceImpl;
    @Value("${old.password.error}")
    private String oldPasswordError;
    @Value("${edit.password.success}")
    private String editPasswordSuccess;

    @Value("${mediaServer.ip.local}")
    private String realTimeVideoIp;

    @Value("${mediaServer.port.websocket.app.video}")
    private String realTimeVideoPort;

    @Value("${mediaServer.port.tcp.video}")
    private String videoTcpPort;

    @Value("${mediaServer.port.websocket.app.resource}")
    private String videoPlaybackPort;

    @Value("${mediaServer.port.tcp.resource}")
    private String resourceTcpPort;

    @Value("${fdfs.webServerUrl}")
    private String webServerUrl;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    /**
     * app用户密码修改
     * @author lijie
     * @since 2018/9/6 15:41
     */
    @Override
    public AppResultBean updateUserPassword(String oldPassword, String newPassword, String equipmentType)
        throws Exception {
        final UserLdap user = (UserLdap) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserBean currentUser = userRepo.findOne(user.getId());
        // 校验数据
        String b = currentUser.getPassword();
        String[] bt = b.split(",");
        byte[] bb = new byte[bt.length];
        for (int i = 0; i < bt.length; i++) {
            int u = Integer.parseInt(bt[i]);
            bb[i] = (byte) u;
        }
        String s = new String(bb);
        if (!SecurityPasswordHelper.isPasswordValid(s, oldPassword)) {
            return new AppResultBean(AppResultBean.PARAM_ERROR, oldPasswordError);
        }
        userService.updatePassword(newPassword, equipmentType);
        return new AppResultBean(AppResultBean.SUCCESS, editPasswordSuccess);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * 获取app自定义信息
     * @author lijie
     * @since 2018/9/11 14:41
     */
    @Override
    public AppResultBean getAppCustomInfo(Integer version) throws Exception {
        List<AppAlarmConfigInfo> appAlarmConfigInfos = null;
        List<StatisticsConfigInfo> statisticsConfigInfos = null;
        String userName = SystemHelper.getCurrentUser().getUsername();
        List<OrganizationLdap> superiorOrg = organizationService.getSuperiorOrg();
        List<String> superiorGroupIds =
            superiorOrg.stream().map(o -> o.getUuid()).collect(Collectors.toList());
        superiorGroupIds.add("default");
        String groupName = userService.getCurrentUserOrg().getName();
        if (superiorGroupIds.size() > 0) {
            //从当前组织往上找组织的报警配置
            for (String superiorGroupId : superiorGroupIds) {
                appAlarmConfigInfos = appAlarmTypeDao.getAlarmTypeByVersion(superiorGroupId, 0, version);
                if (!appAlarmConfigInfos.isEmpty()) {
                    break;
                }
            }
            //从当前组织往上找app综合统计配置
            for (String superiorGroupId : superiorGroupIds) {
                statisticsConfigInfos = statisticsDao.getStatisticsByVersion(superiorGroupId, 0, version);
                if (!statisticsConfigInfos.isEmpty()) {
                    break;
                }
            }

        } else {
            return null;
        }

        AppConfigInfo appConfigInfo = new AppConfigInfo();
        appConfigInfo.setAggrNum(getAggregationNumber(superiorGroupIds));
        appConfigInfo.setMaxStatObjNum(getMaxObjectnumber(superiorGroupIds));
        appConfigInfo.setQueryAlarmPeriod(getAlarmTimeLimit(superiorGroupIds));
        appConfigInfo.setQueryHistoryPeriod(getHistoryTimeLimit(superiorGroupIds));

        LoginConfigInfo loginConfigInfo = new LoginConfigInfo();
        loginConfigInfo.setAbout(getLoginPrompt(superiorGroupIds));
        loginConfigInfo.setForgetPwd(getPasswordPrompt(superiorGroupIds));
        loginConfigInfo.setLogo("/clbs/resources/img/app/" + getLoginLogo(superiorGroupIds));
        loginConfigInfo.setTitle(getloginTitle(superiorGroupIds));
        loginConfigInfo.setUrl(getWebsiteName(superiorGroupIds));

        PersonalConfigInfo personalConfigInfo = new PersonalConfigInfo();
        personalConfigInfo.setAboutUs(getAboutPlatform(superiorGroupIds));
        personalConfigInfo.setGroupAvatar("/clbs/resources/img/app/" + getGroupAvatar(superiorGroupIds));

        List<AlarmType> alarmTypes = new ArrayList<>();
        if (!appAlarmConfigInfos.isEmpty()) {
            for (AppAlarmConfigInfo appAlarmConfigInfo : appAlarmConfigInfos) {
                AlarmType alarmType = new AlarmType();
                alarmType.setCategory(appAlarmConfigInfo.getCategory());
                alarmType.setType(appAlarmConfigInfo.getType());
                alarmType.setName(appAlarmConfigInfo.getName());
                alarmTypes.add(alarmType);
            }
        }

        List<StatisticsConfig> statisticsConfigs = new ArrayList<>();
        if (!statisticsConfigInfos.isEmpty()) {
            for (StatisticsConfigInfo statisticsConfigInfo : statisticsConfigInfos) {
                StatisticsConfig statisticsConfig = new StatisticsConfig();
                statisticsConfig.setNumber(statisticsConfigInfo.getNumber());
                statisticsConfig.setName(statisticsConfigInfo.getName());
                statisticsConfigs.add(statisticsConfig);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", userName);
        jsonObject.put("groupName", groupName);
        jsonObject.put("groupId", superiorGroupIds.get(0));
        jsonObject.put("login", JSON.toJSON(loginConfigInfo));
        jsonObject.put("app", JSON.toJSON(appConfigInfo));
        jsonObject.put("personal", JSON.toJSON(personalConfigInfo));
        jsonObject.put("alarmTypes", JSON.toJSON(alarmTypes));
        jsonObject.put("statistics", JSON.toJSON(statisticsConfigs));
        jsonObject.put("adasFlag", getAdasFlag(superiorGroupIds));
        jsonObject.put("imageWebUrl", webServerUrl);
        jsonObject.put("realTimeVideoIp", realTimeVideoIp);
        jsonObject.put("realTimeVideoPort", realTimeVideoPort);
        jsonObject.put("videoPlaybackPort", videoPlaybackPort);
        jsonObject.put("videoTcpPort", videoTcpPort);
        jsonObject.put("resourceTcpPort", resourceTcpPort);
        return new AppResultBean(jsonObject);
    }

    /**
     * 获取app用户信息
     * @author lijie
     * @date 2018/9/17 09:51
     */
    @Override
    public AppResultBean getAppUser() {
        UserDTO userDTO = userService.getCurrentUserInfo();
        OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
        if (userDTO != null) {
            String state = getString(userDTO.getState());
            if (state == null || state.equals("1")) {
                state = "启用";
            } else {
                state = "停用";
            }
            String gender = getString(userDTO.getGender());
            if (gender == null || gender.equals("1")) {
                gender = "男";
            } else {
                gender = "女";
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userName", getString(userDTO.getUsername()));
            jsonObject.put("groupName", getString(currentUserOrg.getName()));
            jsonObject.put("state", state);
            jsonObject.put("authorizationDate", getString(userDTO.getAuthorizationDate()));
            jsonObject.put("realName", getString(userDTO.getFullName()));
            jsonObject.put("gender", gender);
            jsonObject.put("mobile", getString(userDTO.getMobile()));
            jsonObject.put("mail", getString(userDTO.getMail()));
            return new AppResultBean(jsonObject);
        } else {
            return null;
        }
    }

    @Override
    public Boolean saveAppRegisterLog(HttpServletRequest request, Integer registerType) throws Exception {
        if (request != null && registerType != null) {
            // 获得访问ip
            String ip = new GetIpAddr().getIpAddr(request);
            String msg;
            if (registerType == 1) {
                msg = "用户：" + SystemHelper.getCurrentUsername() + " 登录";
            } else {
                msg = "用户：" + SystemHelper.getCurrentUsername() + " 退出登录";
            }
            return logSearchServiceImpl.addLog(ip, msg, "4", "", "-", "");
        }
        return false;
    }

    public String getString(String s) {
        if (s == null || s.equals("null")) {
            return null;
        } else {
            return s;
        }
    }

    public String getLoginLogo(List<String> superiorGroupIds) {
        String result = null;
        for (String superiorGroupId : superiorGroupIds) {
            result = appPersonalizedDao.getloginLogo(superiorGroupId);
            if (result != null && !result.equals("")) {
                break;
            }
        }
        return result;
    }

    public String getGroupAvatar(List<String> superiorGroupIds) {
        String result = null;
        for (String superiorGroupId : superiorGroupIds) {
            result = appPersonalizedDao.getGroupAvatar(superiorGroupId);
            if (result != null && !result.equals("")) {
                break;
            }
        }
        return result;
    }

    public String getWebsiteName(List<String> superiorGroupIds) {
        String result = null;
        for (String superiorGroupId : superiorGroupIds) {
            result = appPersonalizedDao.getWebsiteName(superiorGroupId);
            if (result != null && !result.equals("")) {
                break;
            }
        }
        return result;
    }

    public String getloginTitle(List<String> superiorGroupIds) {
        String result = null;
        for (String superiorGroupId : superiorGroupIds) {
            result = appPersonalizedDao.getloginTitle(superiorGroupId);
            if (result != null && !result.equals("")) {
                break;
            }
        }
        return result;
    }

    public String getAboutPlatform(List<String> superiorGroupIds) {
        String result = null;
        for (String superiorGroupId : superiorGroupIds) {
            result = appPersonalizedDao.getAboutPlatform(superiorGroupId);
            if (result != null && !result.equals("")) {
                break;
            }
        }
        return result;
    }

    public String getPasswordPrompt(List<String> superiorGroupIds) {
        String result = null;
        for (String superiorGroupId : superiorGroupIds) {
            result = appPersonalizedDao.getPasswordPrompt(superiorGroupId);
            if (result != null && !result.equals("")) {
                break;
            }
        }
        return result;
    }

    public String getLoginPrompt(List<String> superiorGroupIds) {
        String result = null;
        for (String superiorGroupId : superiorGroupIds) {
            result = appPersonalizedDao.getLoginPrompt(superiorGroupId);
            if (result != null && !result.equals("")) {
                break;
            }
        }
        return result;
    }

    public int getMaxObjectnumber(List<String> superiorGroupIds) {
        int result = 0;
        for (String superiorGroupId : superiorGroupIds) {
            String re = appPersonalizedDao.getMaxObjectnumber(superiorGroupId);
            if (re != null && !re.equals("0")) {
                result = Integer.parseInt(re);
                break;
            }
        }
        return result;
    }

    public int getAlarmTimeLimit(List<String> superiorGroupIds) {
        int result = 0;
        for (String superiorGroupId : superiorGroupIds) {
            String re = appPersonalizedDao.getAlarmTimeLimit(superiorGroupId);
            if (re != null && !re.equals("0")) {
                result = Integer.parseInt(re);
                break;
            }
        }
        return result;
    }

    public int getHistoryTimeLimit(List<String> superiorGroupIds) {
        int result = 0;
        for (String superiorGroupId : superiorGroupIds) {
            String re = appPersonalizedDao.getHistoryTimeLimit(superiorGroupId);
            if (re != null && !re.equals("0")) {
                result = Integer.parseInt(re);
                break;
            }
        }
        return result;
    }

    public int getAggregationNumber(List<String> superiorGroupIds) {
        int result = 0;
        for (String superiorGroupId : superiorGroupIds) {
            String re = appPersonalizedDao.getAggregationNumber(superiorGroupId);
            if (re != null && !re.equals("0")) {
                result = Integer.parseInt(re);
                break;
            }
        }
        return result;
    }

    public int getAdasFlag(List<String> superiorGroupIds) {
        String re = null;
        for (String superiorGroupId : superiorGroupIds) {
            re = appPersonalizedDao.getAdasFlag(superiorGroupId);
            if (re != null && !re.equals("")) {
                return Integer.parseInt(re);
            }
        }
        return 0;
    }
}
