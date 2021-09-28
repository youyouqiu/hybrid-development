package com.zw.app.service.personalCenter;


import com.zw.app.util.common.AppResultBean;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lijie
 * @date 2018/9/6 15:39
 */
public interface PersonalCenterService {

    //app用户修改密码
    AppResultBean updateUserPassword(String oldPassword, String newPassword, String equipmentType) throws Exception;

    AppResultBean getAppCustomInfo(Integer version) throws Exception;//获取app自定义信息

    AppResultBean getAppUser();//获取app用户信息

    Boolean saveAppRegisterLog(HttpServletRequest request, Integer registerType) throws Exception;//保存App用户登录/登出日志

}
