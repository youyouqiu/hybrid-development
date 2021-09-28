package com.zw.platform.domain.vas.f3;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * Title:轮询配置表
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月09日 14:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SensorConfig extends BaseFormBean {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String  plate;//车牌号
    private String   vehicleId;//车辆ID
    private String   status;//下发状态
    private String    pollingId;//传感器轮询ID
    private String pollingNames;//传感器轮询名称集合
    private String    pollingTime;//传感器轮询时间
    /**
     * 下发状态
     */
    private Integer sendStatus;
    /**
     * 下发时间
     */
    private String sendTime;

    /**
     * 下发id
     */
    private String sendParamId;

    private List<SensorPolling> pollings;//轮询配置信息

    private Integer monitorType;//对象类型

    private String remark;
}
