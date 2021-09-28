package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by Tdz on 2016/8/4.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConfigGroupForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String configId;
    private String groupId;
}
