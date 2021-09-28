package com.zw.platform.domain.core;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationForm extends BaseFormBean {

    private static final long serialVersionUID = 1L;

    private String operationType;//运营资质类别

    private String explains;//说明

}
