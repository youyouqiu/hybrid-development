package com.zw.platform.repository.modules;

import com.zw.platform.domain.reportManagement.DrivingRecordInfo;
import com.zw.platform.domain.reportManagement.query.DrivingRecordInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 行驶记录仪采集报表
 */
public interface DrivingRecordReportDao {
    void addDrivingRecordInfo(DrivingRecordInfo drivingRecordInfo);

    void updateDrivingRecordInfo(DrivingRecordInfo drivingRecordInfo);

    /**
     * 根据监控对象id查询行驶记录仪数据
     */
    List<DrivingRecordInfo> getDrivingRecordDataByMonitionId(@Param("monitorIds") List<String> monitorIds,
        @Param("queryStartTime") String queryStartTime, @Param("queryEndTime") String queryEndTime);

    /**
     * 根据监控对象id、采集命令、流水号、时间查询最近的一条行驶记录仪数据采集记录
     */
    List<DrivingRecordInfo> getDrivingRecordByMsgSNAck(DrivingRecordInfoQuery query);
}
