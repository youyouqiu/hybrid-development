package com.zw.platform.domain.vas.switching.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/9/6 16:49
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SwitchSignalQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1948858522464768386L;

    /**
     * 监控对象id
     */
    private String vehicleId;
    private byte[] vehicleIdBytes;
    /**
     * 查询开始时间
     */
    private String startTime;
    private Long startTimeLong;
    /**
     * 查询结束时间
     */
    private String endTime;
    private Long endTimeLong;
}
