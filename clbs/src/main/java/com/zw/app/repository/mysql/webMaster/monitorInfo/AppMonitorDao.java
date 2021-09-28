package com.zw.app.repository.mysql.webMaster.monitorInfo;

import com.zw.app.domain.webMaster.monitorInfo.AppMonitorConfigInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lijie
 * @create 2019-09-29 09:35
 * @desc
 */
public interface AppMonitorDao {

    //根据组织id获取报警配置
    List<AppMonitorConfigInfo> getMonitorConfig(@Param("groupId") String groupId,
        @Param("groupDefault") int groupDefault, @Param("type") String type);

    //根据组织id获取报警配置
    List<AppMonitorConfigInfo> getMonitorConfigByGroupId(@Param("groupId") String groupId,
        @Param("groupDefault") int groupDefault);

    //根据组织id获取报警配置
    List<AppMonitorConfigInfo> getMonitorConfigByVersion(@Param("groupId") String groupId,
        @Param("groupDefault") int groupDefault, @Param("appVersion") Integer appVersion, @Param("type") String type);

    //根据组织id删除组织所有数据
    Boolean deleteGroupMonitorConfig(@Param("groupId") String groupId, @Param("groupDefault") int groupDefault);

    //一次添加多条数据（一个组织的）
    Boolean addGroupMonitorConfig(@Param("list") List<AppMonitorConfigInfo> appMonitorConfigInfos);

    String getGroupName(@Param("groupId") String groupId, @Param("type") String type);//查找数据库中是否有该组织的数据，有就获取其组织名称

    //获取默认配置
    List<AppMonitorConfigInfo> getDefaultMonitorConfig(@Param("groupId") String groupId, @Param("type") String type);

}
