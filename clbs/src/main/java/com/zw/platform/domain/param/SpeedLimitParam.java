package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 超速设置
 *
 * @author  Tdz
 * @create 2017-04-24 9:07
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class SpeedLimitParam extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer masSpeed;//最高速度
    private Integer speedTime;//超速持续时间
}
