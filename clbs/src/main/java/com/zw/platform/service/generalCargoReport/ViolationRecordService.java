package com.zw.platform.service.generalCargoReport;

import com.zw.platform.domain.generalCargoReport.ViolationRecordQuery;
import com.zw.platform.util.common.JsonResultBean;

/***
 @Author zhengjc
 @Date 2019/9/4 14:27
 @Description 违章记录报表
 @version 1.0
 **/
public interface ViolationRecordService {
    /**
     * 违章记录表
     * @param query query
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getViolationRecords(ViolationRecordQuery query) throws Exception;

    /**
     * 违章记录表-离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportViolationRecords(ViolationRecordQuery query);
}
