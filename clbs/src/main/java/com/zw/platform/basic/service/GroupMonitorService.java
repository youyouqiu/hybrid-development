package com.zw.platform.basic.service;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.dto.CountDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.util.common.BusinessException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 分组-监控对象service
 * @date 2020/10/279:24
 */
public interface GroupMonitorService extends CacheService {

    /**
     * 通过组织ID获取组织下的分组的监控对象ID
     * @param orgIds 组织ID
     * @return Set集合
     */
    Set<String> getMonitorIdsByOrgId(Collection<String> orgIds);

    /**
     * 通过分组ID获取监控对象ID
     * @param groupIds 分组ID
     * @return Set集合
     */
    Set<String> getMonitorIdsByGroupId(Collection<String> groupIds);

    /**
     * 分配监控对象
     * @param addList
     * @param deleteList
     * @param groupId
     * @return
     * @throws BusinessException
     */
    boolean updateMonitorGroup(List<GroupMonitorDTO> addList, List<GroupMonitorDTO> deleteList, String groupId)
        throws BusinessException;

    /**
     * 通过分组Id获取当前用户监控对象的树
     * @param groupId
     * @return
     */
    Map<String, String> getGroupMonitorTreeByGroupId(String groupId);

    /**
     * 通过groupId统计分组监控对象的数量
     * @param groupIds
     * @return
     */
    List<CountDTO> getCountListByGroupId(Collection<String> groupIds);

    /**
     * 根据监控对象删除分组关系
     * @param monitorIds    监控对象ID集合
     * @param isUpdateRedis 是否维护redis缓存 true db和数据库需要同步维护 false:只维护数据库
     * @return true：删除成功 false 删除失败
     */
    boolean deleteByMonitorIds(Collection<String> monitorIds, boolean isUpdateRedis);

    /**
     * 添加监控对象的分组关系
     * @param groupMonitorList 监控对象分组关系列表
     * @param isUpdateRedis    是否维护redis缓存 true db和数据库需要同步维护 false:只维护数据库
     * @return true：操作成功 false 操作失败
     */
    boolean add(Collection<GroupMonitorDO> groupMonitorList, boolean isUpdateRedis);

    /**
     * 根据分组id获取分组下的监控对象
     * @param groupIds 分组ID集合
     * @return 分组与监控对象的集合
     */
    List<GroupMonitorDTO> getByGroupIds(Collection<String> groupIds);

    /**
     * 获取所有
     * @return
     */
    List<GroupMonitorDO> getAll();

    /**
     * 通过分组查询监控对象的信息
     * @param groupId
     * @return
     */
    List<GroupMonitorDTO> getMonitorByGroupId(String groupId);

    /**
     * 获取分组监控对象树
     * @param multiple   single or multiple
     * @param groupId    分组Id
     * @param queryParam
     * @param queryType
     * @return
     */
    JSONArray getGroupMonitorTree(String multiple, String groupId, String queryParam, String queryType);

    /**
     * 检查分组下的监控对象数量
     * @param groupId 分组Id
     * @return true 监控对象数量未达上限 false 监控对象数量已达上限
     */
    boolean checkGroupMonitorNum(String groupId);

    /**
     * 通过车辆Id进行获取分组-监控中间表
     * @param monitorIds
     * @return
     */
    List<GroupMonitorDTO> getByMonitorIds(Collection<String> monitorIds);

    /**
     * 根据监控对象获取其用户权限下的分组
     * @param monitorId 监控对象ID
     * @return 分组ID集合
     */
    List<String> getUserOwnGroupByMonitorId(String monitorId);

    /**
     * 获取分组下的监控对象集合Map
     * @param groupIds 分组ids
     * @return groupId-monitorIdSet
     */
    Map<String, Set<String>> getGroupMonitorIdSet(Collection<String> groupIds);
}
