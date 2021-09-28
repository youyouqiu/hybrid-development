package com.zw.platform.domain.vas.carbonmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 时间能耗统计Query
 * <p>Title: TimeEnergyStatisticsQuery.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年9月20日上午10:17:41
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TimeEnergyStatisticsQuery extends BaseQueryBean implements Serializable{
	
	private static final long serialVersionUID = -2074161134779217606L;
	
	/**
	 * 查询方式
	 */
	private String queryWay = "";
	/**
	 * 组织id
	 */
	private String groupId = "";
	/**
	 * 组织名称
	 */
	private String groupName = "";
	/**
	 * 车牌号
	 */
	private String brand = "";
	/**
	 * 开始时间
	 */
	private String startDate = "";
	/**
	 * 结束时间
	 */
	private String endDate = "";
	/**
	 * 年份
	 */
	private String year = "";
	/**
	 * 月份
	 */
	private String month = "";
	/**
	 * 季度
	 */
	private String quarter = "";
	
}
