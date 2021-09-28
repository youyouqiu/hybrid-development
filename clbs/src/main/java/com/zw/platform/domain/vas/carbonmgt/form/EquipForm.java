package com.zw.platform.domain.vas.carbonmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
/**
 * 
 * <p>
 * Title: 设备录入form
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
 * @author: fanlu
 * @date 2016年9月19日上午9:11
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class EquipForm extends BaseFormBean implements Serializable{

	private static final long serialVersionUID = 1L;
	/**
	 * 里程基准油耗
	 */
	@Size(max = 20, message = "【基准能耗】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	private String mileageBenchmark;
	/**
	 * 时间基准油耗
	 */
	@Size(max = 20, message = "【基准能耗】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	private String timeBenchmark;
	/**
	 * 怠速基准油耗
	 */
	@Size(max = 20, message = "【基准能耗】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	private String idleBenchmark;
	/**
	 * 工时基准油耗
	 */
	@Size(max = 20, message = "【基准能耗】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	private String workHoursBenchmark;
	
	/**
	 * 组织
	 */
	private String groupId;
	/**
	 * 组织名称
	 */
	private String GroupName;
	/**
	 * 车辆编号
	 */
	private String vehicleId;
	/**
	 * 车牌号
	 */
	private String brand;
	/**
	 * 车辆类型
	 */
	private String vehicleType;
	/**
	 * 燃油类型
	 */
	private String fuelType;
	/**
	 * 0不显示、1显示
	 */
	private Integer flag;
	
	private Date createDataTime;

	private String createDataUsername;

	private Date updateDataTime;

	private String updateDataUsername;
	/**
	 * 车辆类型
	 */
	private String vehicleCategory;
}
