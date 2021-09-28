package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.FenceConfig;
import com.zw.platform.domain.functionconfig.FenceInfo;
import com.zw.platform.domain.functionconfig.form.FenceConfigForm;
import com.zw.platform.domain.functionconfig.query.FenceConfigQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p> Title: 围栏绑定Dao </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年8月8日上午10:20:55
 */
public interface FenceConfigDao {

    /**
         * @param query
     * @return List<FenceConfig>
     * @throws @author wangying
     * @Title: 查询围栏绑定信息
     */
    List<Map<String, Object>> findFenceConfig(FenceConfigQuery query);

    /**
     * 查询围栏绑定信息
     * @param monitorId
     * @param groupIds
     * @return
     */
    List<Map<String, Object>> findFenceConfigInfo(@Param("monitorId") String monitorId,
        @Param("groupIds") List<String> groupIds);

    /**
         * @param fenceConfigForm
     * @return boolean
     * @throws @author wangying
     * @Title: 新增围栏绑定
     */
    boolean addFenceConfig(FenceConfigForm fenceConfigForm);

    /**
         * @param fenceConfigForm
     * @return boolean
     * @throws @author wangying
     * @Title: 批量新增
     */
    boolean addFenceConfigByBatch(List<FenceConfigForm> fenceConfigForm);

    /**
     * 查询轨迹列表
     * @param query
     * @return List<FenceConfig>
     * @Title: findOrbitList
     * @author Liubangquan
     */
    List<FenceConfig> findOrbitList(@Param("query") FenceConfigQuery query,
        @Param("simpleQueryParam") String simpleQueryParam);

    /**
     * 根据围栏车辆关联表id查询轨迹
     * @param id
     * @return FenceConfig
     * @Title: getFenceConfigById
     * @author Liubangquan
     */
    FenceConfig getFenceConfigById(String id);

    /**
         * @param id
     * @return boolean
     * @throws @author wangying
     * @Title: 解除围栏绑定
     */
    boolean unbindFence(String id);

    /**
     * 根据车辆id解除围栏与车辆的绑定
     * @param vid
     * @return boolean
     * @throws @author Liubangquan
     * @Title: unbindFenceByVid
     */
    boolean unbindFenceByVid(String vid);

    /**
     * 根据信息配置id删除围栏绑定关系
     * @param configId
     * @return boolean
     * @throws @author Liubangquan
     * @Title: unbindFenceByConfigId
     */
    boolean unbindFenceByConfigId(String configId);

    /**
         * @param ids
     * @return boolean
     * @throws @author wangying
     * @Title: 批量解除绑定
     */
    boolean unbindFenceByBatch(@Param("ids") List<String> ids);

    /**
         * @param id
     * @return FenceConfig
     * @throws @author wangying
     * @Title: 根据车辆查询围栏和车的绑定关系
     */
    FenceInfo findFenceConfigById(String id);

    /**
         * @param ids
     * @return void
     * @throws @author wangying
     * @Title: 修改下发状态
     */
    void updateStatus(@Param("ids") List<String> ids, @Param("status") int status);

    /**
         * @return FenceConfig
     * @throws @author wangying
     * @Title: 根据id查询绑定关系
     */
    FenceConfig queryFenceConfigById(String id);

    /**
         * @param ids
     * @return List<FenceConfig>
     * @throws @author wangying
     * @Title: 根据id批量查询
     */
    List<Map<String, Object>> findFenceConfigByIds(@Param("ids") List<String> ids);

    /**
         * @param vehicleId,fenceId
     * @return FenceConfig
     * @throws @author wangying
     * @Title: 根据车辆id和围栏id查询围栏绑定
     */
    Map<String, Object> findByVIdAndFId(@Param("vehicleId") String vehicleId, @Param("fenceId") String fenceId);

    /**
     * 回复逻辑删除
     * @param id id
     * @return
     */
    boolean updateFenceConfigById(@Param("id") String id);

    boolean deleteKeyPoint(@Param("id") String id);

    /**
     * 修改围栏绑定
     * @param form
     * @return
     */
    boolean updateFenceConfig(FenceConfigForm form);

    /**
         * @param vehicleId,fenceConfig
     * @param shapeId               围栏ID
     * @return FenceConfig
     * @throws @author wangying
     * @Title: 根据车辆id和围栏绑定关系id查询围栏绑定
     */
    Map<String, Object> findFenceInfoByVehicle(@Param("vehicleId") String vehicleId,
        @Param("fenceConfigId") String fenceConfigId, @Param("shapeId") String shapeId);

    /**
     * 查询围栏数据
     * @param vehicleId      vehicleId
     * @param fenceConfigIds fenceConfigIds
     * @return list
     */
    List<Map<String, Object>> findFenceInfoByVehicleIdAndFenceConfigId(@Param("vehicleId") String vehicleId,
        @Param("fenceConfigIds") String[] fenceConfigIds);

    /**
     * 围栏下发hashCode值和车辆id查询围栏信息
     */
    Map<String, Object> findFenceInfoByVehicleIdAndHashCode(@Param("sendDownId") int sendDownId,
        @Param("vehicleId") String vehicleId);

    /**
     * 根据围栏绑定表id增加下发hashCode值
     */
    boolean addHashCodeByConfigId(@Param("configId") String configId, @Param("hashCodeNumber") int hashCodeNumber);

    String getFenceType(@Param("fid") String fid);

    /**
     * 根据围栏Id查询车辆Id
     * @param fenceId
     * @return
     */
    List<String> getVehicleIdsByFenceId(@Param("fenceId") String fenceId, @Param("vehicleIds") List<String> vehicleIds);

    boolean unbindBatchFenceByVid(@Param("monitorIds") List<String> monitorIds);

    /**
     * 按照企业id以及围栏名称过滤途经点的围栏id
     * @param groupIds
     * @param fenceName
     * @return
     */
    Set<String> getPointFenceIdByGroupIds(@Param("groupIds") Collection<String> groupIds,
        @Param("fenceName") String fenceName);

    /**
     * 照监控对象id以及围栏名称过滤途经点的围栏id
     * @param monitorIds
     * @param fenceName
     * @return
     */
    Set<String> getPointFenceIdByMonitorIds(@Param("monitorIds") Collection<String> monitorIds,
        @Param("fenceName") String fenceName);

    /**
     * 查询围栏信息
     * @param fenceConfigIds fenceConfigIds
     * @return List<FenceInfo>
     */
    List<FenceInfo> getFenceInfoByFenceConfigIds(@Param("fenceConfigIds") Collection<String> fenceConfigIds);
}
