package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.reportManagement.form.DriverDiscernReportDo;
import com.zw.platform.domain.reportManagement.query.DriverDiscernStatisticsQuery;
import com.zw.platform.dto.driverMiscern.DriverDiscernStatisticsDetailDto;
import com.zw.platform.dto.driverMiscern.DriverDiscernStatisticsDto;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 行驶记录仪采集报表
 */
public interface DriverDiscernStatisticsDao {

    Page<DriverDiscernStatisticsDto> pageQuery(@Param("ids") List<String> ids,
        @Param("query") DriverDiscernStatisticsQuery query);

    List<DriverDiscernStatisticsDto> find(@Param("ids") List<String> ids,
        @Param("query") DriverDiscernStatisticsQuery query);

    List<DriverDiscernStatisticsDetailDto> detail(@Param("id") String id, @Param("time") String time);

    void save(@Param("reportDo") DriverDiscernReportDo reportDo);

    List<DriverDiscernReportDo> findDeleteData(@Param("dateTime") Date dateTime);

    void delete(@Param("ids") Set<String> ids);

    DriverDiscernReportDo getById(@Param("id") String id);

    void setImageUrl(@Param("id")String id, @Param("imageUrl")String imageUrl);

    void setVideoUrl(@Param("id")String id, @Param("videoUrl")String videoUrl);

}
