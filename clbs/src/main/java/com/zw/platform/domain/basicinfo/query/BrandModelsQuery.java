package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Title: 从业人员Query
 * Copyright: Copyright (c) 2016
 * Company: ZhongWei
 * team: ZhongWeiTeam
 * @author: penghujie
 * @date 2018年4月16日下午4:15:13
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BrandModelsQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 机型id
	 */
	private String id;

	/**
	 * 车辆品牌id
	 */
	private String brandId;

	/**
	 * 车辆品牌名称
	 */
	private String brandName;

	/**
	 * 机型名称
	 */
	private String modelName;

	/**
	 * 备注
	 */
	private String describtion;

	private Integer flag;

	private Date createDataTime;

	private String createDataUsername;

	private Date updateDataTime;

	private String updateDataUsername;

}
