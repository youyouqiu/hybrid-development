package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.GroupMonitorBindDO;
import com.zw.platform.basic.domain.GroupMonitorCountDo;
import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.dto.CountDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 分组监控对象Dao
 * @date 2020/11/314:22
 */
public interface GroupMonitorDao {

    /**
     * 通过分组查询监控对象的信息
     * @param groupId
     * @return
     */
    List<GroupMonitorDTO> getMonitorByGroupId(String groupId);

    /**
     * 删除分组-监控对象关系，通过分组Id,监控对象Id
     * @param groupId
     * @param monitorId
     * @return
     */
    boolean deleteByGroupIdAndMonitorId(@Param("groupId") String groupId, @Param("monitorId") String monitorId);

    /**
     * 通过监控对象Id查询
     * @param monitorIdSet
     * @return
     */
    List<GroupMonitorDTO> getListByMonitorIds(@Param("monitorIdSet") Collection<String> monitorIdSet);

    /**
     * 通过groupId统计分组监控对象的数量
     * @param groupIds
     * @return
     */
    List<CountDTO> getCountListByGroupId(@Param("ids") Collection<String> groupIds);

    /**
     * 批量添加
     * @param list
     * @return
     */
    boolean batchAdd(@Param("list") Collection<GroupMonitorDO> list);

    /**
     * 根据分组集合获取分组下的监控对象绑定关系
     * @param groupIds 分组集合
     * @return 分组与监控对象绑定关系列表
     */
    List<GroupMonitorDTO> getByGroupIds(@Param("groupIds") Collection<String> groupIds);

    /**
     * 按监控对象删除分组与监控对象的绑定关系
     * @param monitorIds 监控对象ID
     * @return 是否操作成功
     */
    boolean deleteByMonitorIds(@Param("monitorIds") Collection<String> monitorIds);

    /**
     * 清除分组组旋钮位置编号
     * @param monitorIds 监控对象id集合
     * @return 是否操作成功
     */
    boolean clearKnobNo(@Param("monitorIds") Collection<String> monitorIds);

    /**
     * 更新监控对象分组组旋钮位置编号
     * @param groupMonitorList 监控对象分组编号
     * @return 是否操作成功
     */
    boolean updateKnobNo(@Param("groupMonitorList") Collection<GroupMonitorDO> groupMonitorList);

    /**
     * 查询所有
     * @return
     */
    List<GroupMonitorDO> getAll();

    /**
     * 判断当前分组是否包含监控对象
     * @param groupIds
     * @return
     */
    Set<String> judgeGroupsHaveMonitor(@Param("groupIds") Collection<String> groupIds);

    /**
     * 通过分组Ids和监控对象的名称模糊查询
     * @param groupIds
     * @param queryParam
     * @return
     */
    List<GroupMonitorDTO> getMonitorByGroupIds(@Param("groupIds") List<String> groupIds,
        @Param("keyword") String queryParam);

    /**
     * 分组下监控对象的数量
     * @param groupId 分组ID
     * @return 绑定监控对象数量
     */
    int getCountByGroupId(@Param("groupId") String groupId);

    /**
     * 查询分组信息及分组下监控对象数量
     * @param ids         分组id
     * @param monitorType 监控对象类型 0:车 1:人 2:物;  空查询所有类型
     * @return List<GroupMonitorCountDo>
     */
    List<GroupMonitorCountDo> getGroupMonitorCountList(@Param("ids") Collection<String> ids,
        @Param("monitorType") String monitorType);

    /**
     * 查询分组下监控对象
     * @param groupIds    分组id
     * @param monitorType 监控对象类型 0:车 1:人 2:物;  空查询所有类型
     * @return List<GroupMonitorDO>
     */
    List<GroupMonitorBindDO> getGroupMonitorBindInfoListByIds(@Param("groupIds") Collection<String> groupIds,
        @Param("monitorType") String monitorType);

    /**
     * 根据监控对象获取
     * @param monitorId 监控对象ID
     * @param userId    用户ID
     * @return 分组Id
     */
    List<String> getGroupIdIdByMonitorId(@Param("monitorId") String monitorId, @Param("userId") String userId);
}
