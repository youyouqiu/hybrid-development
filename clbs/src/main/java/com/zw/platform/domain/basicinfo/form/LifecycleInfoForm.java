package com.zw.platform.domain.basicinfo.form;


import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.domain.infoconfig.form.ConfigTransportImportForm;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.Converter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by Tdz on 2016/8/1.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LifecycleInfoForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 计费日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date billingDate;

    /**
     * 到期日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    private int count;//避免导入存入条数异常
    
    public static LifecycleInfoForm initConfigImport(ConfigImportForm config) {
        LifecycleInfoForm form = new LifecycleInfoForm();
        form.setBillingDate(Converter.toDate(config.getBillingDateStr(), "yyyy-MM-dd"));
        form.setExpireDate(Converter.toDate(config.getDurDateStr(), "yyyy-MM-dd"));
        form.setCreateDataUsername(config.getCreateDataUsername());
        return form;
    }

    public static LifecycleInfoForm initConfigImport(ConfigTransportImportForm config) {
        LifecycleInfoForm form = new LifecycleInfoForm();
        String date = LocalDateUtils.dateFormate(new Date());
        config.setBillingDateStr(date + " 00:00:00");
        form.setBillingDate(Converter.toDate(date, "yyyy-MM-dd"));
        form.setExpireDate(Converter.toDate(config.getExpireTimeStr(), "yyyy-MM-dd"));
        form.setCreateDataUsername(config.getCreateDataUsername());
        return form;
    }
}
