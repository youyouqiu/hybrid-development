package com.zw.platform.basic.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.query.MonitorTreeReq;

import java.util.Collection;
import java.util.Set;

/**
 * 监控对象树服务接口
 * @author zhangjuan
 */
public interface MonitorTreeService {
    /**
     * 获取组织+分组树，一般用于数据量较大的时候
     * @param type           根节点是否可选 single：nocheck=true multiple 不组装 nocheck
     * @param needMonitorNum 是否需要返回分组下的监控对象数量
     * @param needOnlineNum  是否需要返回分组下在离线监控对象的数量
     * @param needFilterOrg  是否需要过滤掉没有分组的组织 true 需要过滤  false 无需过滤
     * @param isBigData      分组专用 是否有子节点 true：isParent = true
     * @return 组织+分组树
     */
    JSONArray getGroupTree(String type, boolean needMonitorNum, boolean needOnlineNum, boolean needFilterOrg,
        boolean isBigData);

    /**
     * 获取监控对象树 --信息返回较全面，数据量太大（一般监控对象超过5000）不要使用该树形结构
     * @param type          用于组织和分组的根节点是否可选 single：nocheck=true multiple 不组装 nocheck
     * @param webType       1：实时监控 2：实时视频 10 两客一危
     * @param isCarousel    true 获取轮播树的监控对象附加通道号相关信息 false 其他不获取
     * @param needAccStatus true 需要返回ACC状态
     * @return 组织+分组+监控对象树
     */
    JSONArray getMonitorTree(String type, Integer webType, boolean isCarousel, boolean needAccStatus);

    /**
     * 按监控对象状态获取监控对象树
     * @param webType            1：实时监控 2：实时视频
     * @param monitorNameKeyword 监控对象名称模糊搜索关键字
     * @param onlineStatus       1在线，2在线停车，3在线行驶，4报警，5超速报警,6未定位,7未上线,8离线,9心跳
     * @param deviceType         为空查询全部，按deviceType过滤某一种协议
     * @param isCarousel         true 获取轮播树的监控对象附加通道号相关信息 false 其他不获取
     * @param needAccStatus      true 需要返回ACC状态
     * @return 组织+分组+监控对象树
     */
    JSONArray getMonitorStateTree(Integer webType, String monitorNameKeyword, Integer onlineStatus, String deviceType,
        boolean isCarousel, boolean needAccStatus);

    /**
     * 监控对象树查询--复杂条件的查询 包含监控对象的不同类型模糊搜索，在线状态过来、监控对象类型过滤、终端协议类型过滤、车型过滤
     * @param monitorTreeQuery 监控对象树查询条件
     * @return 组织+分组+监控对象树
     */
    JSONArray getMonitorTreeFuzzy(MonitorTreeReq monitorTreeQuery);

    /**
     * 监控对象树查询--复杂条件的查询 包含监控对象的不同类型模糊搜索，在线状态过来、监控对象类型过滤、终端协议类型过滤、车型过滤
     * @param monitorTreeQuery 监控对象树查询条件
     * @param isReturnAll      是否限制数量 当不涉及模糊搜索、车型等条件查询时生效：false:限制 监控对象在5000以内返回 true：全部返回
     * @return JSONObject size 监控对象数量，tree 树节点
     */
    JSONObject getMonitorTree(MonitorTreeReq monitorTreeQuery, boolean isReturnAll);

    /**
     * 监控对象车辆权限树结构
     * @param type                  用于组织和分组的根节点是否可选 single：nocheck=true multiple 不组装 nocheck
     * @param includeQuitPeopleFlag 是否包含离职 false:不包含; true:包含;
     * @return JSONObject size 监控对象数量，tree 树节点
     */
    JSONObject getMonitorTreeByType(String type, boolean includeQuitPeopleFlag);

    /**
     * 监控对象树监控对象数量查询--每个分组下的监控对象计数一次，不进行去重
     * @param monitorTreeQuery 监控对象树查询条件
     * @return 组织+分组+监控对象树
     */
    int getMonitorTreeFuzzyCount(MonitorTreeReq monitorTreeQuery);

    /**
     * 根据分组获取监控对象树节点
     * @param groupIds         分组ID集合
     * @param monitorTreeQuery 监控对象树查询条件
     * @return 根据分组下符合条件的树节点
     */
    JSONArray getByGroupId(Collection<String> groupIds, MonitorTreeReq monitorTreeQuery);

    /**
     * 根据组织获取组织下的监控监控对象树节点
     * @param orgDn            组织ID
     * @param monitorTreeQuery 监控对象查询条件过滤
     * @return 组织下的监控对象ID
     */
    JSONArray getByOrgDn(String orgDn, MonitorTreeReq monitorTreeQuery);

    /**
     * 报表模糊搜索树
     * @param type       搜索类型为企业group，分组assignment，车vechile
     * @param queryParam 模糊参数
     * @param queryType  multiple 代表人车物
     * @param treeType   single代表只能单选树最下面节点
     * @return JSONArray
     */
    JSONArray reportFuzzySearch(String type, String queryParam, String queryType, String treeType);

    /**
     * @param pid     分组Id获取组织ID
     * @param type    group：分组 org：按组织
     * @param webType 1：实时监控 2：实时视频 10 两客一危
     * @return 监控对象Id集合
     */
    Set<String> getMonitorIdSet(String pid, String type, Integer webType);

}
