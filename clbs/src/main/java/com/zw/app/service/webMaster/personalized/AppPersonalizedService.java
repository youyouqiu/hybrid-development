package com.zw.app.service.webMaster.personalized;

import com.zw.app.domain.webMaster.personalized.AppPersonalized;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.domain.core.OrganizationLdap;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;

/**
 * @author lijie
 * @date 2018/8/22 11:49
 */
public interface AppPersonalizedService {

    AppResultBean find();//获取app后台配置信息

    Boolean updateAppPersonalized(AppPersonalized appPersonalized);//修改app后台配置

    Boolean resetLoginTitle();//恢复登录页标题为默认配置

    Boolean defaultLoginTitle(String title);//设置当前登录页标题为组织默认

    Boolean resetLoginUrl();//恢复平台网址为默认

    Boolean defaultLoginUrl(String url);//设置当前平台网址为组织默认

    Boolean resetAboutLogin();//恢复关于登录提示为组织默认

    Boolean defaultAboutLogin(String AboutLogin);//设置当前关于登录提示为组织默认

    Boolean resetPwdComment();//恢复忘记密码提示为组织默认

    Boolean defaultPwdComment(String PwdComment);//设置当前忘记密码提示为组织默认

    Boolean resetAboutUs();//恢复关于我们显示内容为组织默认

    Boolean defaultAboutUs(String aboutUs);//设置当前关于我们显示内容为组织默认

    Boolean resetAggrNum();//恢复开始聚合对象数量为组织默认

    Boolean defaultAggrNum(int AggrNum);//设置开始聚合对象数量为组织默认

    Boolean resetHistoryPeriod();//恢复历史数据最大查询时间范围为组织默认

    Boolean defaultHistoryPeriod(int historyPeriod);//设置历史数据最大查询时间范围为组织默认

    Boolean resetAlarmPeriod();//恢复报警最大查询时间范围为组织默认

    Boolean defaultAlarmPeriod(int alarmPeriod);//设置报警最大查询时间范围为组织默认

    Boolean resetMaxStatObjNum();//恢复统计最多选择对象数量为组织默认

    Boolean defaultMaxStatObjNum(int maxStatObjNum);//设置统计最多选择对象数量为组织默认

    String updateLoginLogo(MultipartFile logo, HttpServletRequest request);//修改登录页logo

    Boolean resetLoginLogo(HttpServletRequest request);//恢复登录页logo为组织默认

    Boolean defaultLoginLogo(String logo,HttpServletRequest request);//设置当前登录页logo为组织默认

    String updateGroupAvatar(MultipartFile avatar,HttpServletRequest request);//修改用户组织头像

    Boolean resetGroupAvatar(HttpServletRequest request);//设置当前用户组织头像为组织默认

    Boolean defaultGroupAvatar(String avatar,HttpServletRequest request);//恢复当前用户头像为组织默认

    Boolean resetAdasFlag();//恢复是否开启adas为组织默认

    Boolean defaultAdasFlag(String adasFlag);//设置是否开启adas为组织默认

    OrganizationLdap getGroupInfoByUser() throws Exception;

}
