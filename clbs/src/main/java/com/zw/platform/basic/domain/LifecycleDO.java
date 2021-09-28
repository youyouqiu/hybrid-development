package com.zw.platform.basic.domain;

import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * zw_m_service_lifecycle
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class LifecycleDO extends BaseDO {

    /**
     * 计费日期
     */
    private Date billingDate;

    /**
     * 到期日期
     */
    private Date expireDate;

    public LifecycleDO(Date billingDate, Date expireDate) {
        super();
        this.expireDate = expireDate;
        this.billingDate = billingDate;
    }

    public LifecycleDO(String billingDate, String expireDate) {
        super();
        this.expireDate = DateUtil.getStringToDate(expireDate, DateFormatKey.YYYY_MM_DD);
        this.billingDate = DateUtil.getStringToDate(billingDate, DateFormatKey.YYYY_MM_DD);
    }
}
