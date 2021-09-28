package com.zw.platform.domain.forwardplatform;

import java.io.Serializable;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by LiaoYuecai on 2017/3/7.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ThirdPlatFormConfig extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String configId;
    private String thirdPlatformId;
}
