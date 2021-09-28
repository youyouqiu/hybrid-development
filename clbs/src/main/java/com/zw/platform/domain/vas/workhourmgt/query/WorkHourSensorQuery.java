package com.zw.platform.domain.vas.workhourmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorkHourSensorQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
}
