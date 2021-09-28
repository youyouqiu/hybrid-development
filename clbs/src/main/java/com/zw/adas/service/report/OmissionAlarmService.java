package com.zw.adas.service.report;

import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.domain.reportManagement.query.OmissionAlarmQuery;

/**
 * @author wanxing
 * @Title: 漏报报警service
 * @date 2021/1/1916:37
 */
public interface OmissionAlarmService {

    /**
     * 分页获取数据列表接口
     * @param query
     * @return
     */
    PassCloudResultBean getPageByKeyword(OmissionAlarmQuery query);

    /**
     * 企业详情
     * @param query
     * @return
     */
    PassCloudResultBean orgDetail(OmissionAlarmQuery query);

    /**
     * 企业每天数据
     * @param query
     * @return
     */
    PassCloudResultBean orgDayCount(OmissionAlarmQuery query);
}
