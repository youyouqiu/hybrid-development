package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 人员组织关联表
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PersonnelGroupForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String peopleId;
    private String groupId;

}