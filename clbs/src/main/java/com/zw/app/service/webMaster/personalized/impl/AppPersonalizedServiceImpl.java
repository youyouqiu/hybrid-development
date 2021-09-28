package com.zw.app.service.webMaster.personalized.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.controller.monitor.MonitorManagementController;
import com.zw.app.domain.webMaster.personalized.AppPersonalized;
import com.zw.app.domain.webMaster.personalized.LoginConfigInfo;
import com.zw.app.domain.webMaster.personalized.PersonalConfigInfo;
import com.zw.app.domain.webMaster.personalized.PlatformAppConfig;
import com.zw.app.repository.mysql.webMaster.personalized.AppPersonalizedDao;
import com.zw.app.service.webMaster.personalized.AppPersonalizedService;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * @author lijie
 * @date 2018/8/22 11:49
 */
@Service
public class AppPersonalizedServiceImpl implements AppPersonalizedService {
    private static Logger log = LogManager.getLogger(MonitorManagementController.class);
    @Autowired
    AppPersonalizedDao appPersonalizedDao;
    @Autowired
    UserService userService;

    /**
     * 获取app后台配置信息
     * @author lijie
     * @date 2018/8/22 15:49
     */
    @Override
    public AppResultBean find() {
        AppPersonalized appPersonalizedNow = null;
        AppPersonalized appPersonalizedDef = null;
        String userName = SystemHelper.getCurrentUser().getUsername();
        String groupId = userService.getCurrentUserOrg().getUuid();
        //便利当前组织及其上级组织，从下往上去查找数据库中存在的配置信息
        appPersonalizedNow = appPersonalizedDao.find(groupId, 0);
        if (appPersonalizedNow == null) {
            appPersonalizedNow = new AppPersonalized();
            appPersonalizedNow.setId(UUID.randomUUID().toString());
            appPersonalizedNow.setGroupId(groupId);
            appPersonalizedNow.setCreateDataTime(new Date());
            appPersonalizedNow.setCreateDataUsername(userName);
            appPersonalizedNow.setGroupDefault(0);
            appPersonalizedDao.addGroupData(appPersonalizedNow);
        }
        appPersonalizedDef = appPersonalizedDao.find(groupId, 1);
        if (appPersonalizedDef == null) {
            appPersonalizedDef = new AppPersonalized();
            appPersonalizedDef.setId(UUID.randomUUID().toString());
            appPersonalizedDef.setGroupId(groupId);
            appPersonalizedDef.setCreateDataTime(new Date());
            appPersonalizedDef.setCreateDataUsername(userName);
            appPersonalizedDef.setGroupDefault(1);
            appPersonalizedDao.addGroupData(appPersonalizedDef);
        }

        PlatformAppConfig platformAppConfig = new PlatformAppConfig();
        platformAppConfig.setAggrNum(
            appPersonalizedNow.getAggregationNumber() == 0 ? "" : appPersonalizedNow.getAggregationNumber() + "");
        platformAppConfig.setMaxStatObjNum(
            appPersonalizedNow.getMaxObjectnumber() == 0 ? "" : appPersonalizedNow.getMaxObjectnumber() + "");
        platformAppConfig.setQueryAlarmPeriod(
            appPersonalizedNow.getAlarmTimeLimit() == 0 ? "" : appPersonalizedNow.getAlarmTimeLimit() + "");
        platformAppConfig.setQueryHistoryPeriod(
            appPersonalizedNow.getHistoryTimeLimit() == 0 ? "" : appPersonalizedNow.getHistoryTimeLimit() + "");

        LoginConfigInfo loginConfigInfo = new LoginConfigInfo();
        loginConfigInfo.setAbout(getString(appPersonalizedNow.getLoginPrompt()));
        loginConfigInfo.setForgetPwd(getString(appPersonalizedNow.getPasswordPrompt()));
        loginConfigInfo.setLogo(getString(appPersonalizedNow.getLoginLogo()));
        loginConfigInfo.setTitle(getString(appPersonalizedNow.getLoginTitle()));
        loginConfigInfo.setUrl(getString(appPersonalizedNow.getWebsiteName()));

        PersonalConfigInfo personalConfigInfo = new PersonalConfigInfo();
        personalConfigInfo.setAboutUs(getString(appPersonalizedNow.getAboutPlatform()));
        personalConfigInfo.setGroupAvatar(getString(appPersonalizedNow.getGroupAvatar()));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("login", JSON.toJSON(loginConfigInfo));
        jsonObject.put("app", JSON.toJSON(platformAppConfig));
        jsonObject.put("personal", JSON.toJSON(personalConfigInfo));
        jsonObject.put("adas", getString(appPersonalizedNow.getAdasFlag()));

        return new AppResultBean(jsonObject);
    }

    //将null转为“”回前台
    public String getString(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    /**
     * 修改app后台配置数据库数据
     * @author lijie
     * @date 2018/8/23 09:49
     */
    @Override
    public Boolean updateAppPersonalized(AppPersonalized appPersonalized) {
        String userName = SystemHelper.getCurrentUser().getUsername();
        String groupId = userService.getCurrentUserOrg().getUuid();
        appPersonalized.setCreateDataTime(null);
        appPersonalized.setUpdateDataTime(new Date());
        appPersonalized.setUpdateDataUsername(userName);
        appPersonalized.setGroupId(groupId);
        Boolean flag = appPersonalizedDao.updateAppPersonalized(appPersonalized);
        return flag;
    }

    /**
     * 恢复登录标题为默认值
     * @author lijie
     * @date 2018/8/23 11:36
     */
    @Override
    public Boolean resetLoginTitle() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        String defaultLoginTitle = defaultAppPersonalized.getLoginTitle();
        if (StringUtils.isNotBlank(defaultLoginTitle)) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setLoginTitle(defaultLoginTitle);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置当前登录页标题为组织默认值
     * @author lijie
     * @date 2018/8/23 15:56
     */
    @Override
    public Boolean defaultLoginTitle(String title) {
        if (StringUtils.isNotBlank(title)) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setLoginTitle(title);
            defaultAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(defaultAppPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 恢复平台网址为组织默认值
     * @author lijie
     * @date 2018/8/23 16:51
     */
    @Override
    public Boolean resetLoginUrl() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        String defaultLogoUrl = defaultAppPersonalized.getWebsiteName();
        if (StringUtils.isNotBlank(defaultLogoUrl)) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setWebsiteName(defaultLogoUrl);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置当前平台网址为组织默认
     * @author lijie
     * @date 2018/8/23 17:16
     */
    @Override
    public Boolean defaultLoginUrl(String url) {
        if (StringUtils.isNotBlank(url)) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setWebsiteName(url);
            defaultAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(defaultAppPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 恢复关于登录提示为组织默认
     * @author lijie
     * @date 2018/8/23 17:36
     */
    @Override
    public Boolean resetAboutLogin() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        String defaultAboutLogin = defaultAppPersonalized.getLoginPrompt();
        if (StringUtils.isNotBlank(defaultAboutLogin)) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setLoginPrompt(defaultAboutLogin);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置关于登录提示为组织默认
     * @author lijie
     * @date 2018/8/23 18:16
     */
    @Override
    public Boolean defaultAboutLogin(String aboutLogin) {
        if (StringUtils.isNotBlank(aboutLogin)) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setLoginPrompt(aboutLogin);
            defaultAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(defaultAppPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 恢复忘记密码提示为组织默认
     * @author lijie
     * @date 2018/8/24 09:36
     */
    @Override
    public Boolean resetPwdComment() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        String defaultPwdComment = defaultAppPersonalized.getPasswordPrompt();
        if (StringUtils.isNotBlank(defaultPwdComment)) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setPasswordPrompt(defaultPwdComment);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置当前忘记密码提示为组织默认
     * @author lijie
     * @date 2018/8/24 09:50
     */
    @Override
    public Boolean defaultPwdComment(String pwdComment) {
        if (StringUtils.isNotBlank(pwdComment)) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setPasswordPrompt(pwdComment);
            defaultAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(defaultAppPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 恢复关于我们提示为组织默认
     * @author lijie
     * @date 2018/8/24 09:36
     */
    @Override
    public Boolean resetAboutUs() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        String defaultAboutUs = defaultAppPersonalized.getAboutPlatform();
        if (StringUtils.isNotBlank(defaultAboutUs)) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setAboutPlatform(defaultAboutUs);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置当前关于我们为组织默认
     * @author lijie
     * @date 2018/8/24 10:20
     */
    @Override
    public Boolean defaultAboutUs(String aboutUs) {
        if (StringUtils.isNotBlank(aboutUs)) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setAboutPlatform(aboutUs);
            defaultAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(defaultAppPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 恢复开始聚合对象数量为组织默认
     * @author lijie
     * @date 2018/8/24 10:58
     */
    @Override
    public Boolean resetAggrNum() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        int defaultAggrNum = defaultAppPersonalized.getAggregationNumber();
        if (defaultAggrNum != 0) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setAggregationNumber(defaultAggrNum);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置开始聚合对象数量为组织默认
     * @author lijie
     * @date 2018/8/24 11:20
     */
    @Override
    public Boolean defaultAggrNum(int aggrNum) {
        if (aggrNum != 0) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setAggregationNumber(aggrNum);
            defaultAppPersonalized.setGroupDefault(1);
            return updateAppPersonalized(defaultAppPersonalized);
        } else {
            return false;
        }
    }

    /**
     * 恢复开始聚合对象数量为组织默认
     * @author lijie
     * @date 2018/8/24 10:58
     */
    @Override
    public Boolean resetHistoryPeriod() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        int defaultHistoryPeriod = defaultAppPersonalized.getHistoryTimeLimit();
        if (defaultHistoryPeriod != 0) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setHistoryTimeLimit(defaultHistoryPeriod);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置历史数据最大查询时间范围为组织默认
     * @author lijie
     * @date 2018/8/24 11:40
     */
    @Override
    public Boolean defaultHistoryPeriod(int historyPeriod) {
        if (historyPeriod != 0) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setHistoryTimeLimit(historyPeriod);
            defaultAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(defaultAppPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 恢复报警最大查询时间范围为组织默认
     * @author lijie
     * @date 2018/8/24 11:50
     */
    @Override
    public Boolean resetAlarmPeriod() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        int defaultAlarmPeriod = defaultAppPersonalized.getAlarmTimeLimit();
        if (defaultAlarmPeriod != 0) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setAlarmTimeLimit(defaultAlarmPeriod);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置报警最大查询时间范围为组织默认
     * @author lijie
     * @date 2018/8/24 11:55
     */
    @Override
    public Boolean defaultAlarmPeriod(int alarmPeriod) {
        if (alarmPeriod != 0) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setAlarmTimeLimit(alarmPeriod);
            defaultAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(defaultAppPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 恢复统计最多选择对象数量为组织默认
     * @author lijie
     * @date 2018/8/24 13:55
     */
    @Override
    public Boolean resetMaxStatObjNum() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        int defaultMaxStatObjNum = defaultAppPersonalized.getMaxObjectnumber();
        if (defaultMaxStatObjNum != 0) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setMaxObjectnumber(defaultMaxStatObjNum);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置统计最多选择对象数量为组织默认
     * @author lijie
     * @date 2018/8/24 14:05
     */
    @Override
    public Boolean defaultMaxStatObjNum(int maxStatObjNum) {
        if (maxStatObjNum != 0) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setMaxObjectnumber(maxStatObjNum);
            defaultAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(defaultAppPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 修改登录页logo
     * @param logo 上传的图片
     * @author lijie
     * @date 2018/8/27 09:15
     */
    @Override
    public String updateLoginLogo(MultipartFile logo, HttpServletRequest request) {
        String logoName = getImageName(logo, request);
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized appPersonalized = appPersonalizedDao.find(groupId, 0);
        String oldLogo = appPersonalized.getLoginLogo();
        String id = appPersonalized.getId();
        Integer logoCount = appPersonalizedDao.getSameLoginLogo(oldLogo, id);
        if (logoName != null) {
            AppPersonalized updateAppPersonalized = new AppPersonalized();
            updateAppPersonalized.setLoginLogo(logoName);
            updateAppPersonalized.setGroupDefault(0);
            updateAppPersonalized(updateAppPersonalized);
            if (logoCount == 0) {
                deleteImage(oldLogo, request);
            }
        }
        return logoName;
    }

    /**
     * 恢复登录页logo为组织默认
     * @author lijie
     * @date 2018/8/27 09:50
     */
    @Override
    public Boolean resetLoginLogo(HttpServletRequest request) {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized appPersonalized = appPersonalizedDao.find(groupId, 0);
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        String oldLogo = appPersonalized.getLoginLogo();
        String id = appPersonalized.getId();
        Integer logoCount = appPersonalizedDao.getSameLoginLogo(oldLogo, id);
        String defaultLoginLogo = defaultAppPersonalized.getLoginLogo();
        if (StringUtils.isNotBlank(defaultLoginLogo)) {
            AppPersonalized updateAppPersonalized = new AppPersonalized();
            updateAppPersonalized.setLoginLogo(defaultLoginLogo);
            updateAppPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(updateAppPersonalized);
            if (logoCount == 0) {
                deleteImage(oldLogo, request);
            }
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置当前登录页logo为组织默认
     * @author lijie
     * @date 2018/8/27 11:15
     */
    @Override
    public Boolean defaultLoginLogo(String logo, HttpServletRequest request) {
        if (StringUtils.isNotBlank(logo)) {
            String groupId = userService.getCurrentUserOrg().getUuid();
            AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
            String oldDefaultLogo = defaultAppPersonalized.getLoginLogo();
            String id = defaultAppPersonalized.getId();
            Integer logoCount = appPersonalizedDao.getSameLoginLogo(oldDefaultLogo, id);
            AppPersonalized updateAppPersonalized = new AppPersonalized();
            updateAppPersonalized.setLoginLogo(logo);
            updateAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(updateAppPersonalized);
            if (logoCount == 0) {
                deleteImage(oldDefaultLogo, request);
            }
            return success;
        } else {
            return false;
        }
    }

    /**
     * 修改用户组织头像
     * @param avatar 上传的图片
     * @author lijie
     * @date 2018/8/27 15:25
     */
    @Override
    public String updateGroupAvatar(MultipartFile avatar, HttpServletRequest request) {
        String avatarName = getImageName(avatar, request);
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized appPersonalized = appPersonalizedDao.find(groupId, 0);
        String oldAvatar = appPersonalized.getGroupAvatar();
        String id = appPersonalized.getId();
        Integer avatarCount = appPersonalizedDao.getSameGroupAvatar(oldAvatar, id);
        if (avatarName != null) {
            AppPersonalized updateAppPersonalized = new AppPersonalized();
            updateAppPersonalized.setGroupAvatar(avatarName);
            updateAppPersonalized.setGroupDefault(0);
            updateAppPersonalized(updateAppPersonalized);
            if (avatarCount == 0) {
                deleteImage(oldAvatar, request);
            }
        }
        return avatarName;
    }

    /**
     * 恢复登录页logo为组织默认
     * @author lijie
     * @date 2018/8/27 15:40
     */
    @Override
    public Boolean resetGroupAvatar(HttpServletRequest request) {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized appPersonalized = appPersonalizedDao.find(groupId, 0);
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        String oldAvatar = appPersonalized.getGroupAvatar();
        String id = appPersonalized.getId();
        Integer logoCount = appPersonalizedDao.getSameGroupAvatar(oldAvatar, id);
        String defaultGroupAvatar = defaultAppPersonalized.getGroupAvatar();
        if (StringUtils.isNotBlank(defaultGroupAvatar)) {
            AppPersonalized updateAppPersonalized = new AppPersonalized();
            updateAppPersonalized.setGroupAvatar(defaultGroupAvatar);
            updateAppPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(updateAppPersonalized);
            if (logoCount == 0) {
                deleteImage(oldAvatar, request);
            }
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置当前头像为组织默认
     * @author lijie
     * @date 2018/8/27 15:55
     */
    @Override
    public Boolean defaultGroupAvatar(String avatar, HttpServletRequest request) {
        if (StringUtils.isNotBlank(avatar)) {
            String groupId = userService.getCurrentUserOrg().getUuid();
            AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
            String oldDefaultAvatar = defaultAppPersonalized.getGroupAvatar();
            String id = defaultAppPersonalized.getId();
            Integer logoCount = appPersonalizedDao.getSameGroupAvatar(oldDefaultAvatar, id);
            AppPersonalized updateAppPersonalized = new AppPersonalized();
            updateAppPersonalized.setGroupAvatar(avatar);
            updateAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(updateAppPersonalized);
            if (logoCount == 0) {
                deleteImage(oldDefaultAvatar, request);
            }
            return success;
        } else {
            return false;
        }
    }

    /**
     * 恢复是否开启adas为组织默认
     * @author lijie
     * @date 2018/12/06 13:55
     */
    @Override
    public Boolean resetAdasFlag() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        AppPersonalized defaultAppPersonalized = appPersonalizedDao.find(groupId, 1);
        String adasFlag = defaultAppPersonalized.getAdasFlag();
        if (StringUtils.isNotBlank(adasFlag)) {
            AppPersonalized appPersonalized = new AppPersonalized();
            appPersonalized.setAdasFlag(adasFlag);
            appPersonalized.setGroupDefault(0);
            Boolean success = updateAppPersonalized(appPersonalized);
            return success;
        } else {
            return false;
        }
    }

    /**
     * 设置统计最多选择对象数量为组织默认
     * @author lijie
     * @date 2018/12/06 14:05
     */
    @Override
    public Boolean defaultAdasFlag(String adasFlag) {
        if (StringUtils.isNotBlank(adasFlag)) {
            AppPersonalized defaultAppPersonalized = new AppPersonalized();
            defaultAppPersonalized.setAdasFlag(adasFlag);
            defaultAppPersonalized.setGroupDefault(1);
            Boolean success = updateAppPersonalized(defaultAppPersonalized);
            return success;
        } else {
            return false;
        }
    }

    @Override
    public OrganizationLdap getGroupInfoByUser() throws Exception {
        return userService.getCurrentUserOrg();
    }

    /**
     * 将上传的logo图片储存返回图片名和路径
     * @author lijie
     * @date 2018/8/27 09:05
     */
    public String getImageName(MultipartFile image, HttpServletRequest request) {
        try {
            String newName = "";
            try {
                // 文件保存路径
                String filePath = request.getSession().getServletContext().getRealPath("/") + "resources/img/app/";
                File saveFile = new File(filePath);
                if (!saveFile.exists()) {
                    saveFile.mkdirs();
                }
                // 获取文件后缀名
                String suffix = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf("."));
                // 判断文件后缀名是否为png
                if (suffix.equals(".png") || suffix.equals(".jpg")) {
                    newName = (new Date().getTime()) + "" + new Random().nextInt(100) + suffix;
                    // 转存文件
                    image.transferTo(new File(filePath + newName));
                    return newName;
                } else {
                    // 删除
                    (new File(filePath + newName)).delete();
                    return null;
                }
            } catch (IllegalStateException e) {
                log.error("error", e);
            } catch (IOException e) {
                log.error("error", e);
            }
            return null;
        } catch (Exception e) {
            log.error("储存app信息配置上传图片异常", e);
            return null;
        }
    }

    /**
     * 将修改登录logo之前的logo存储的没有用到的图片删除
     * @author lijie
     * @date 2018/8/27 10:10
     */
    public void deleteImage(String imageName, HttpServletRequest request) {
        try {
            String filePath =
                request.getSession().getServletContext().getRealPath("/") + "resources/img/app/" + imageName;
            File logoFile = new File(filePath);
            logoFile.delete();
        } catch (Exception e) {
            log.error("app信息配置删除没用的图片异常", e);
        }
    }

}