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
public class GroupForConfigForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String configid;//配置id
    private String groupid ; // 分组ID
    private String groupName; // 分组名称

}
