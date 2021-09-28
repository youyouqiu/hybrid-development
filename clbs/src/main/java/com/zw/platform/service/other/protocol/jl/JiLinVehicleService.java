package com.zw.platform.service.other.protocol.jl;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.other.protocol.jl.query.JiLinVehicleSetListQuery;
import com.zw.platform.domain.other.protocol.jl.resp.JiLinVehicleSetResp;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 10:39
 */
public interface JiLinVehicleService {

    /**
     * 根据协议类型获取809链接参数设置
     * @param protocolType 协议类型
     * @return List<PlantParam>
     */
    List<PlantParam> get809ConnectionParamSetsByProtocolType(Integer protocolType);

    /**
     * 根据809设置id获取 809平台数据交互管理监树
     * @param id         809设置id
     * @param queryType  模糊查询查询类型 vehicle:车辆; organization:企业; assignment:分组
     * @param queryParam 模糊查询参数
     * @return JSONObject
     * @throws Exception Exception
     */
    JSONArray getTree(String id, String queryType, String queryParam) throws Exception;

    /**
     * 车辆设置列表
     * @param query query
     * @return Page<JiLinVehicleSetResp>
     */
    Page<JiLinVehicleSetResp> getVehicleSetList(JiLinVehicleSetListQuery query);
}
