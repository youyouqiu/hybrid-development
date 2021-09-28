package com.zw.platform.domain.vas.alram.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhouzongbo on 2018/12/25 14:31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AlarmSearchQuery809 extends BaseQueryBean implements Serializable {

    private static final long serialVersionUID = -4873468734887061309L;

    /**
     * 报警开始时间
     */
    private String alarmStartTime;
    private Long alarmStartTimeL;

    /**
     * 报警开始时间
     */
    private String alarmEndTime;
    private Long alarmEndTimeL;

    /**
     * 报警类型
     */
    private String alarmType;
    private List<Integer> alarmTypeList;

    /**
     * 报警来源 0:平台; 1: 终端
     */
    private String alarmSource;

    /**
     * 车辆ID集合
     */
    private String vehicleIds;
    private List<byte[]> vehicleIdByteList;
}
