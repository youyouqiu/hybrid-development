package com.zw.platform.basic.domain;

import com.zw.platform.domain.basicinfo.form.ProfessionalsTypeForm;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ProfessionalsTypeDO {
    private static final long serialVersionUID = 1L;
    private String id;
    private String professionalstype;
    private String description;
    private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;

    public ProfessionalsTypeDO(ProfessionalsTypeForm professionalsTypeForm) {
        this.id = professionalsTypeForm.getId();
        this.professionalstype = professionalsTypeForm.getProfessionalstype();
        this.description = professionalsTypeForm.getDescription();
        this.flag = professionalsTypeForm.getFlag();
        this.createDataTime = professionalsTypeForm.getCreateDataTime();
        this.createDataUsername = professionalsTypeForm.getCreateDataUsername();
        this.updateDataTime = professionalsTypeForm.getUpdateDataTime();
        this.updateDataUsername = professionalsTypeForm.getUpdateDataUsername();
    }
}
