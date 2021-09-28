package com.zw.platform.service.generalCargoReport;

import com.zw.platform.dto.reportManagement.WorkHandOverRecordQuery;
import com.zw.platform.util.common.JsonResultBean;

/**
 * @author CJY
 */
public interface WorkHandoverRecordService {
    /**
     * 值班交接班记录表
     * @param query query
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getWorkHandOverRecord(WorkHandOverRecordQuery query) throws Exception;

    /**
     * 值班交接班记录表-离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportWorkHandOverRecord(WorkHandOverRecordQuery query);
}
