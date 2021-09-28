package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;

/**
 * @author xiaoyun
 */
@Data
public class CorpCheckReq {
    /**
     * 企业名称
     */
    private String corpName;
    /**
     * 企业id
     */
    private String orgId;
    /**
     * 开始时间(格式: yyyyMMdd，如: 20160301)
     */
    private String startTime;
    /**
     * 结束时间(格式: yyyyMMdd，如: 20160301 )
     */
    private String endTime;
    /**
     * 时间类型(1:年 2:季3:月4:周 5:日 )
     */
    private String timeType;
}
