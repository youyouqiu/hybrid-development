package com.zw.platform.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class BrandModelsInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 机型id
	 */
	private String id;

	/**
	 * 品牌id
	 */
	private String brandId;

	/**
	 * 品牌名称
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
