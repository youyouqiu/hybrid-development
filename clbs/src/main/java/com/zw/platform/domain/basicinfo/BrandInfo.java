package com.zw.platform.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class BrandInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 车辆品牌id
	 */
	private String id;

	/**
	 * 车辆品牌名称
	 */
	private String brandName;

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
