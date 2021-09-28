package com.zw.platform.domain.vas.carbonmgt.query;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class MileageSelectQuery extends BaseFormBean implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * 查询方式
	 */
	private String inquiryMode;
	/**
	 * 车牌号
	 */
	private String brand;
	/**
	 *开始时间 
	 */
	private String startDate;
	/**
	 *结束时间
	 */
	private String endDate;
}
