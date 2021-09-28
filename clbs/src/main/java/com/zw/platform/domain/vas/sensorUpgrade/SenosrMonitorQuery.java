package com.zw.platform.domain.vas.sensorUpgrade;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SenosrMonitorQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = -631087051340116417L;
    /**
     * 传感器id
     */
    private String sensorId;

    /**
     * 模糊搜索参数
     */
    private String fuzzyParam;

    /**
     * 模糊搜索标识
     */
    private String paramSign;
}
