package com.zw.app.service.activeSecurity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.alarm.PercentageOfRank;
import com.zw.app.domain.alarm.RiskRankResult;

import java.util.List;
import java.util.Map;


public interface AppRiskRankService {
    /**
     * @param page     第几页
     * @param pageSize 每页显示数量
     * @param type     要筛选的监控对象类型（0车，1人，2物，3所有）
     * @return
     * @throws Exception
     */
    JSONObject getAssList(Integer page, Integer pageSize, String type) throws Exception;


    /**
     * @param page     第几页
     * @param pageSize 每页显示数量
     * @param type     要筛选的监控对象类型（0车，1人，2物，3所有）
     * @return
     * @throws Exception
     */
    JSONObject getAssListSeven(Integer page, Integer pageSize, String type) throws Exception;

    /**
     * @param page     第几页
     * @param pageSize 每页数量
     * @return
     */
    JSONObject getAssVeList(Integer page, Integer pageSize) throws Exception;


    /**
     * @param page     第几页
     * @param pageSize 每页数量
     * @return
     */
    JSONObject getAssVeListSeven(Integer page, Integer pageSize) throws Exception;

    List<RiskRankResult> getRiskRank(String vehicleIds, String startTime, String endTime, Integer riskType)
        throws Exception;

    PercentageOfRank getPercentageOfRank(String vehicleIds, String startTime, String endTime)
        throws Exception;

    List<RiskRankResult> getAllMonitors(String[] vids, Map<String, String> moniorsMap, Map<String, Integer> dayMap)
        throws Exception;

    List<RiskRankResult> assemblyData(JSONArray jsonArray, Map<String, String> moniorsMap, Map<String, Integer> dayMap,
        String conditions);

    /**
     * 根据App配置数量获得监控对象id
     *
     * @param type        0车，1人，2物
     * @param defaultSize 配置的监控对象数量（null 值会取出所有）
     * @return
     */
    List<String> getDefaultMonitorIdsSeven(String type, Integer defaultSize, Boolean isVehicle);

    //String[] getVids(String vehicleIds, Integer defaultSize);

    JSONObject adasMonitorFlag();

    boolean getVehicleIsBindRiskDefined(String vehicleId);

    /**
     * 行驶统计
     * 停止统计
     * 上线统计
     * 超速统计
     * 公用（模糊查询）
     *
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param type       要筛选的监控对象类型（0车，1人，2物，3所有）
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     * @throws Exception
     */
    JSONObject getFuzzyAssList(Integer page, Integer pageSize, String type,
        String search, Integer searchType) throws Exception;

    /**
     * 报警排名和报警处置 模糊查询 监控对象
     *
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param type       要筛选的监控对象类型（0车，1人，2物，3所有）
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     */
    JSONObject getFuzzyVehicleList(Integer page, Integer pageSize, String type,
        String search, Integer searchType);

    /**
     * 工时模糊查询 模糊查询 监控对象
     *
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param type       要筛选的监控对象类型（0车，1人，2物，3所有）
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     */
    JSONObject getFuzzyHourPollsList(Integer page, Integer pageSize, String type,
        String search, Integer searchType);


    /**
     * 油量模糊查询 模糊查询 监控对象
     *
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param type       要筛选的监控对象类型（0车，1人，2物，3所有）
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     */
    JSONObject getFuzzyPollingOilList(Integer page, Integer pageSize, String type,
        String search, Integer searchType);

    /**
     * 油耗模糊查询 模糊查询 监控对象
     *
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param type       要筛选的监控对象类型（0车，1人，2物，3所有）
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     */
    JSONObject getFuzzyOilSensorList(Integer page, Integer pageSize, String type, String search,
        Integer searchType);
}
