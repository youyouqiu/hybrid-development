package com.zw.platform.service.functionconfig;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.query.PointQuery;
import com.github.pagehelper.Page;
import com.zw.platform.domain.functionconfig.FenceConfig;
import com.zw.platform.domain.functionconfig.form.FenceConfigForm;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.query.FenceConfigQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;
import java.util.Map;

/**
 * 围栏绑定
 * @version 1.0
 * @author: wangying
 * @date 2016年8月8日上午10:44:10
 */
public interface FenceConfigService {

    /**
     * 分页查询围栏绑定
     * @author wangying
     */
    Page<Map<String, Object>> findFenceConfigByPage(FenceConfigQuery query) throws Exception;

    /**
     * 新增围栏绑定
     * @author wangying
     */
    boolean addFenceConfig(FenceConfigForm fenceConfigForm) throws Exception;

    /**
     * 批量新增
     * @author wangying
     */
    boolean addFenceConfigByBatch(List<FenceConfigForm> list) throws Exception;

    /**
     * 查询轨迹列表
     * @author Liubangquan
     */
    Page<FenceConfig> findOrbitList(FenceConfigQuery query, String simpleQueryParam) throws Exception;

    /**
     * 根据围栏车辆关联表id查询轨迹
     * @author Liubangquan
     */
    FenceConfig getFenceConfigById(String id) throws Exception;

    /**
     * 轨迹添加
     */
    void editFenceConfig(LineForm form) throws Exception;

    /**
     * 解除围栏绑定
     * @author wangying
     */
    JsonResultBean unbindFence(String id, String ipAddress) throws Exception;

    /**
     * 根据车辆id解除围栏绑定
     * @param vid  车辆id
     * @param type
     */
    boolean unbindFenceByVid(String vid, Integer type) throws Exception;

    /**
     * 根据信息配置id解除围栏绑定
     */
    boolean unbindFenceByConfigId(String configId) throws Exception;

    /**
     * 批量解除绑定
     * @author wangying
     */
    JsonResultBean unbindFenceByBatch(List<String> ids, String ipAddress) throws Exception;

    /**
     * 下发解绑
     * @author wangying
     */
    void sendUnbindFence(List<Map<String, Object>> configs) throws Exception;

    List<Map<String, Object>> findFenceConfigByIds(List<String> ids) throws Exception;

    /**
     * 根据车辆id和围栏id查询围栏绑定
     * @author wangying
     */
    Map<String, Object> findByVIdAndFId(String vehicleId, String fenceId) throws Exception;

    /**
     * 根据id查询绑定关系
     */
    FenceConfig queryFenceConfigById(String id) throws Exception;

    boolean deleteKeyPoint(String id) throws Exception;

    /**
     * 下发
     */
    void sendFenceByType(List<Map<String, Object>> listMap) throws Exception;

    /**
     * 组装围栏下发数据
     */
    JsonResultBean sendFenceData(List<JSONObject> paramList, String ipAddress) throws Exception;

    /**
     * 修改围栏绑定
     */
    boolean updateFenceConfig(FenceConfigForm form, String ipAddress) throws Exception;

    Map<String, Object> getStatistical(Integer webType);

    /**
     * 根据监控对象绑定表id和车辆id查询围栏信息
     */
    Map<String, Object> findFenceInfo(String vehicleId, int sendDownId);

    /**
     * 获取围栏树结构
     * @param type
     */
    String getFenceTree(String type);

    /**
     * 根据车辆id查询其绑定的围栏,并组装成树结构
     * @param vid 车辆id
     */
    String findFenceConfigByVid(String vid);

    /**
     * 围栏绑定
     */
    JsonResultBean addBindFence(String data, String ipAddress) throws Exception;

    /**
     * 获得行驶和停止的监控对象数量
     */
    Map<String, Object> getRunAndStopMonitorNum(Boolean isNeedMonitorId, String userName) throws Exception;

    /**
     * 按照企业id以及围栏名称过滤途经点的围栏id
     * @param pointQuery
     * @return
     */
    String getPointFenceIdByGroupIds(PointQuery pointQuery);

    /**
     * 照监控对象id以及围栏名称过滤途经点的围栏id
     * @param pointQuery
     * @return
     */
    String getPointFenceIdByMonitorIds(PointQuery pointQuery);
}
