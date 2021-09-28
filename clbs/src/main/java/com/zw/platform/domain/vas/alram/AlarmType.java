package com.zw.platform.domain.vas.alram;


import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by FanLu on 2016/12/06.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmType extends BaseFormBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String parent;

    private String type;

    private String description;

    private String sendFlag;

    private String pos;

    private int protocolType;

    private Boolean checked = true;
}
