package com.zw.adas.service.driverStatistics;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.adas.domain.driverStatistics.query.AdasDriverQuery;
import com.zw.adas.domain.driverStatistics.show.AdasDriverInfoShow;
import com.zw.adas.domain.driverStatistics.show.AdasDriverStatisticsShow;
import com.zw.adas.domain.driverStatistics.show.AdasProfessionalShow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/7/10 10:39
 @Description 司机统计service
 @version 1.0
 **/
public interface AdasDriverStatisticsService {
    List<AdasDriverStatisticsShow> getDriverInfo(AdasDriverQuery adasDriverQuery);

    List<AdasDriverStatisticsShow> getDriverInfoByCardNumber(List<AdasDriverStatisticsShow> datas, String cardNumber);

    /**
     * 导出
     */
    void export(AdasDriverQuery adasDriverQuery, HttpServletResponse response, HttpServletRequest request)
        throws IOException;

    /**
     * 查询实时监控页面ic卡司机动态信息
     */
    AdasDriverInfoShow getIcCardDriverInfo(String vehicleId, String cardNumber);

    JSONObject bindIcCardTree();

    JSONArray bindIcCardTreeByAssign(String assignmentId, boolean isChecked);

    Map<String, JSONArray> bindIcCardTreeByGroup(String groupId, boolean isChecked);

    JSONArray bindIcCardTreeSearch(String param);

    /**
     * 根据从业人员资格证号和名称获取从业人员信息
     * @param cardNumber 从业资格证号
     * @param name       从业人员名称
     */
    AdasProfessionalShow getAdasProfessionalDetail(String cardNumber, String name);

    /**
     * 导出驾驶员一次插拔卡的明细信息
     */
    void exportDetail(AdasDriverQuery adasDriverQuery, HttpServletResponse response) throws Exception;

    /**
     * 导出驾驶员所有拔卡的明细信息
     */
    void exportDetails(AdasDriverQuery adasDriverQuery, HttpServletResponse response) throws Exception;

    String migrate443();
}
