package com.zw.platform.service.oil;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.basicinfo.query.ObdTripDataQuery;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * <p> Title:位置信息Service <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月27日 14:35
 */
public interface PositionalService {

    /**
     * /** 根据位置记录计算 行驶时长,怠速时长,总里程,平均速度
     * @param positionals
     *            位置记录计算
     * @param type
     *            里程统计方法 GPS_SENSOR GPS统计 MILE_SENSOR 里程传感器统计
     * @return
     */
    public JSONObject getStatisticalData(List<Positional> positionals, String type) throws Exception;

    /**
     * 根据车辆编号查询位置信息
     * @param vehicleId
     *            车辆编号
     * @param startTime
     *            开始时间
     * @param endTime
     *            结束时间
     * @return
     */
    public List<Positional> getHistoryInfo(String vehicleId, String startTime, String endTime) throws Exception;

    /**
     * 根据车辆编号查询位置信息(调用paas-cloud api)
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param msg
     * @throws Exception
     */
    JSONObject getHistoryInfoByPaas(String vehicleId, String startTime,
                                    String endTime, JSONObject msg) throws Exception;

    /**
     * 将位置信息存入readis
     * @param positionalList
     *            位置信息列表
     */
    public void putToRedis(List<Positional> positionalList) throws Exception;

    /**
     * 逆地址解析 先查询habase 后访问高德api
     * @param longitude
     * @param latitude
     * @return
     * @throws Exception
     */
    String getAddress(String longitude, String latitude);

    /**
     * 查询obd行程统计
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JsonResultBean getObdTripDataList(String vehicleIds, String startTime, String endTime)
        throws Exception;

    /**
     * 查询obd行程统计分页数据
     * @param query
     * @return
     * @throws Exception
     */
    PageGridBean getTotalDataFormInfo(ObdTripDataQuery query) throws Exception;

    /**
     * 导出obd行程统计
     * @param response
     * @param simpleQueryParam
     * @throws Exception
     */
    void exportObdTripDataList(HttpServletResponse response, String simpleQueryParam) throws Exception;
}
