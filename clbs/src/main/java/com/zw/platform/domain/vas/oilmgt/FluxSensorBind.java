package com.zw.platform.domain.vas.oilmgt;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * <p>Title: 流量传感器与车的绑定表实体</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年9月19日上午9:13:36
 * @version 1.0
 */
@Data
public class FluxSensorBind implements Serializable {
	private static final long serialVersionUID = 1L;

    /**
     * 油耗传感器与车辆关联
     */
    private String id;

    /**
     * 油耗传感器ID
     */
    private String oilWearId;

    /**
     * 车辆ID
     */
    private String vehicleId;
    

    /**
     * 自动上传时间
     */
    private Integer autoUploadTime;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionK;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionB;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

}
