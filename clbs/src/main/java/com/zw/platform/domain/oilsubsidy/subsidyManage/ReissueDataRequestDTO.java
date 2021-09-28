package com.zw.platform.domain.oilsubsidy.subsidyManage;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/26 15:07
 */
@Data
@NoArgsConstructor
public class ReissueDataRequestDTO {
    /**
     * 成功
     */
    public static final int STATE_SUCCESS = 0;
    /**
     * 请求失败少于三次
     */
    public static final int STATE_FAIL_LESS_THREE = 1;
    /**
     * 请求失败三次
     */
    public static final int STATE_FAIL_THREE = 2;
    /**
     * 失败，已有无需补发
     */
    public static final int STATE_FAIL = 3;
    /**
     * 其他原因
     */
    public static final int STATE_OTHER_REASON = 4;
    /**
     * 请求失败,重复补发
     */
    public static final int STATE_FAIL_REPEAT_REISSUE = 5;
    /**
     * 车辆id
     */
    private String monitorId;
    /**
     * 状态 0:请求成功 1:请求失败(成功，企业平台即刻补发) 2:请求失败(请求三次失败) 3:请求失败(失败，已有无需补发) 4:其他原因
     */
    private Integer state;

    public ReissueDataRequestDTO(String monitorId, Integer state) {
        this.monitorId = monitorId;
        this.state = state;
    }
}
