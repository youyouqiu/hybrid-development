package com.zw.platform.repository.modules;

import com.zw.platform.domain.generalCargoReport.CargoMonthReportInfo;
import com.zw.platform.domain.generalCargoReport.CargoRecordForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;


public interface CargoDao {

    Set<String> getCargoRecordVids(@Param("groupId") String groupId, @Param("startTime") String startTime,
        @Param("endTime") String endTime, @Param("type") Integer type);

    /**
     * 根据企业id和时间查询一段时间内的普货车辆变动记录
     *
     * @param groupId   企业id
     * @param startTime 查询开始时间
     * @param endTime   查询结束时间
     * @return 普货车辆变动记录
     */
    List<CargoRecordForm> getCargoRecordByGroupId(@Param("groupId") List<String> groupId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime);


    /**
     * 根据企业id和时间查询山东报表月报表
     *
     */
    List<CargoMonthReportInfo> getMonthData(@Param("groupIds") Set<String> groupIds, @Param("time") Integer time);
}
