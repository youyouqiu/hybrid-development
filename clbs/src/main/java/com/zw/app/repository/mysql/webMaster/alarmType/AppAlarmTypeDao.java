package com.zw.app.repository.mysql.webMaster.alarmType;


import com.zw.app.domain.webMaster.alarmType.AppAlarmConfigInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lijie
 * @create 2018-08-28 09:35
 * @desc
 */
public interface AppAlarmTypeDao {

    //根据组织id获取报警配置
    List<AppAlarmConfigInfo> getAlarmType(@Param("groupId") String groupId, @Param("groupDefault") int groupDefault);

    //根据组织id获取报警配置
    List<AppAlarmConfigInfo> getAlarmTypeByVersion(@Param("groupId") String groupId,
                                                   @Param("groupDefault") int groupDefault,
                                                   @Param("appVersion")Integer appVersion);

    //根据组织id删除组织所有数据
    Boolean deleteGroupAlarmType(@Param("groupId") String groupId, @Param("groupDefault") int groupDefault);

    //一次添加多条数据（一个组织的）
    Boolean addGroupAlarmType(@Param("list") List<AppAlarmConfigInfo> appAlarmConfigInfos);

    String getGroupName(@Param("groupId") String groupId);//查找数据库中是否有该组织的数据，有就获取其组织名称

    /**
     * 根据组织id查询报警查询最大时间
     */
    int getAlarmMaxDateByGroupId(String groupId);
}
