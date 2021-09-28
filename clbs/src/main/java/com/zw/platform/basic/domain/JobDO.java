package com.zw.platform.basic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职位类别表数据 zw_m_job_info
 *
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobDO extends BaseDO {
    /**
     * 职位类别名称
     */
    private String jobName;

    /**
     * 图标
     */
    private String jobIconName;

    /**
     * 备注
     */
    private String remark;
}
