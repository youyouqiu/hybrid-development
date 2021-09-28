package com.zw.platform.domain.functionconfig.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * <p>
 * Title: 标注Query.java
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
 * @date 2016年8月8日下午1:35:52
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MarkQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 标注
	 */
	private String id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 经度
	 */
	private Double longitude;

	/**
	 * 纬度
	 */
	private Double latitude;

	private Integer radius;

	/**
	 * 类型
	 */
	private String type;

	/**
	 * 描述
	 */
	private String description;

	private Integer flag;

	private Date createDataTime;

	private String createDataUsername;

	private Date updateDataTime;

	private String updateDataUsername;
}
