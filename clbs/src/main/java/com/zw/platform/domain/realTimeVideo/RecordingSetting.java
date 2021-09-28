package com.zw.platform.domain.realTimeVideo;


import java.io.Serializable;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class RecordingSetting extends BaseFormBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4501480323603743108L;

    private String vehicleId;

    private Integer thresholdValue; // 报警录像占用主存储器存储阈值百分比

    private Integer keepTime; // 报警录像的最长持续时间

    private Integer startTime; // 报警发生前进行标记的录像时间

}