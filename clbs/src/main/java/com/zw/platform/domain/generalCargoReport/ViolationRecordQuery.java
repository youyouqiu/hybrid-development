package com.zw.platform.domain.generalCargoReport;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 @Author zhengjc
 @Date 2019/9/4 14:28
 @Description 违章处置记录报表
 @version 1.0
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class ViolationRecordQuery extends BaseQueryBean {
    private static final long serialVersionUID = 6730765194905056879L;
    /**
     * 起始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 企业id
     */
    private String orgId;
}
