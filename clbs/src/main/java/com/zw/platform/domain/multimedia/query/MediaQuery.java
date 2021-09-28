package com.zw.platform.domain.multimedia.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * <p>
 * Title: 多媒体Query
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * 
 * @author: wangying
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MediaQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 多媒体
	 */
	private String id;

	/**
	 * 多媒体名称
	 */
	private String mediaName;

	/**
	 * 多媒体类型
	 */
	private Integer type; 
	
	private Integer formatCode;//多媒体格式编码
	
    private Integer eventCode;//事件项编码
    
    private Integer wayID;//通道ID
    
    private String vehicleId;//车辆ID
    
    private String brand; // 车牌号
    
    private String assignment; // 分组

	private Short flag;

	private Date createDataTime;

	private String createDataUsername;

	private Date updateDataTime;

	private String updateDataUsername;
	
	private String startTime;
	
	private String endTime;
}
