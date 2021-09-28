package com.zw.platform.dto.reportManagement;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/14 10:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkHandOverRecordQuery extends BaseQueryBean {
    private static final long serialVersionUID = -6234631968651176533L;
    private String orgIds;
    private String startTime;
    private String endTime;
}
