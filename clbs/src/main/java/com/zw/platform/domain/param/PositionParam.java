package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by LiaoYuecai on 2017/4/11.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PositionParam extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
    private String vid;
    private Integer positionUpTactics;
    private Integer positionUpScheme;
    private Integer driverLoggingOutUpTimeSpace;
    private Integer dormancyUpTimeSpace;
    private Integer emergencyAlarmUpTimeSpace;
    private Integer defaultTimeUpSpace;
    private Integer defaultDistanceUpSpace;
    private Integer driverLoggingOutUpDistanceSpace;
    private Integer dormancyUpDistanceSpace;
    private Integer emergencyAlarmUpDistanceSpace;
}
