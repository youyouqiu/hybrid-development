package com.zw.platform.domain.infoconfig;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 信息配置实体类
 * <p>Title: Config.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年7月26日上午11:01:32
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Config1 {
	/** 信息配置 */
	private String id;
	/** 车牌号 */
	private String brands;
	/** 终端编号 */
	private String devices;
	/** SIM卡号 */
	private String sims;
	/** 计费日期 */
	private Date billingDate;
	/** 到期日期 */
	private Date dueDate;
	/** 从业人员 */
	private String professionals;
	private String deviceName;
	private String deviceType;
	private String manuFacturer;
	private String operator;
	private String simFlow;
	private String useFlow;
	/** 车辆id */
	private String brandID;
	/** 分组id */
	private String citySelID;
	/** 终端id */
	private String deviceID;
	/** Sim卡id */
	private String simID;



}
