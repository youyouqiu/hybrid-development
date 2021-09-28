package com.zw.platform.service.reportManagement;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.reportManagement.query.DriverDiscernStatisticsQuery;
import com.zw.platform.dto.driverMiscern.DriverDiscernStatisticsDetailDto;
import com.zw.platform.dto.driverMiscern.DriverDiscernStatisticsDto;
import com.zw.protocol.msg.Message;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Administrator
 */
public interface DriverDiscernStatisticsService {

    Page<DriverDiscernStatisticsDto> pageQuery(DriverDiscernStatisticsQuery query) throws Exception;

    JSONObject getMediaInfo(String id);

    List<DriverDiscernStatisticsDetailDto> detail(final String id, String time);

    void export(HttpServletResponse response, DriverDiscernStatisticsQuery query) throws IOException;

    /**
     * 0x0E10 的上报处理
     */
    void saveReportHandle(Message message) throws IOException;

    /**
     * 定时删除照片任务
     */
    void deletePhoto();
}
