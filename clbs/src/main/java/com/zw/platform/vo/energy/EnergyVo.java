package com.zw.platform.vo.energy;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author 作者 E-mail:yangya
 * @version 创建时间：2017年11月14日 上午9:33:56 类说明:
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class EnergyVo implements Serializable {

	private String id;

	private String vehicleId;// 车辆id

	private String travelMile;// 总行驶里程

	private String travelTotal;// 总行驶油耗量

	private String travelBase;// 行驶油耗基准(可输入）

	private String travelBaseList;// 行驶能耗基准(基准计算的)

	private Integer idleTime;// 总怠速时长(s)

	private String idleTotal;// 总怠速油耗量

	private String idleBase;// 怠速油耗基准(可输入）

	private String idleBaseList;// 怠速油耗基准(基准计算的)

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date installTime;// 节油产品安装日期

	private Integer idleThreshold;// 怠速阈值

}
