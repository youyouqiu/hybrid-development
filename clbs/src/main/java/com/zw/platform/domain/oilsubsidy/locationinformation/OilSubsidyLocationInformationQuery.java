package com.zw.platform.domain.oilsubsidy.locationinformation;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

/**
 * 定位信息统计查询
 *
 * @author XK 2020-10-10
 */
@Data
public class OilSubsidyLocationInformationQuery extends BaseQueryBean {
    /**
     * 转发平台对应的企业id
     */
    private String forwardOrgId;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
