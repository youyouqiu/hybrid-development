package com.zw.platform.domain.infoconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by Tdz on 2016/8/19.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfessionalForConfigFrom extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String configid;//配置id
    private String professionalsid; // 从业人员id
    private String professionalsName; // 从业人员名称

    public static ProfessionalForConfigFrom initConfigImpot(ConfigImportForm config, String name, String id) {
        ProfessionalForConfigFrom form = new ProfessionalForConfigFrom();
        form.setProfessionalsid(id);
        form.setProfessionalsName(name);
        form.setConfigid(config.getId());
        form.setCreateDataUsername(config.getCreateDataUsername());
        return form;
    }
}
