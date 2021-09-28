package com.zw.app.repository.mysql.webMaster.personalized;

import com.zw.app.domain.webMaster.personalized.AppPersonalized;
import org.apache.ibatis.annotations.Param;

/**
 * @author lijie
 * @create 2018-08-22 10:35
 * @desc
 */
public interface AppPersonalizedDao {

  AppPersonalized find(@Param("groupId") String groupId,@Param("groupdefault") Integer groupdefault);//询app个性化设置参数

  boolean addGroupData(AppPersonalized appPersonalized);//添加组织的数据

  boolean updateAppPersonalized(AppPersonalized appPersonalized);//修改app后台配置信息

  Integer getSameLoginLogo(@Param("login_logo") String logo,@Param("id") String id);//获取除去此id的另外的所有配置同样logo的信息数

  Integer getSameGroupAvatar(@Param("group_avatar") String avatar,@Param("id") String id);//获取除去此id的另外的所有配置同样头像的信息数

  String getloginLogo(String gropId);

  String getGroupAvatar(String gropId);

  String getWebsiteName(String gropId);

  String getloginTitle(String gropId);

  String getAboutPlatform(String gropId);

  String getPasswordPrompt(String gropId);

  String getLoginPrompt(String gropId);

  String getMaxObjectnumber(String gropId);

  String getAlarmTimeLimit(String gropId);

  String getHistoryTimeLimit(String gropId);

  String getAggregationNumber(String gropId);

  String getAdasFlag(String gropId);

}
