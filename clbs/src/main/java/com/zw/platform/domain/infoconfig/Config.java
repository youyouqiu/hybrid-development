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
public class Config {

	/** 信息配置 */
	private String id;
	/** 车辆id */
	private String vehicleId;
	/** 分组id */
	private String groupId;
	/** 终端ID */
	private String deviceId;
	/** SIM卡ID */
	private String simCardId;
	/** 外设ID */
	private String peripheralsId;
	/** 服务周期ID */
	private String serviceLifecycleId;
	/** 报警状态 */
	private Integer alarmStatus;
	/** 报警时间 */
	private Date alarmTime;
	/** 在线状态 */
	private Integer onlineStatus;
	/** 离线时间 */
	private Date offlineTime;
	/** 在线时间 */
	private Date onlineTime;
	/** 最后车的经度 */
	private Double longitude;
	/** 最后车的纬度 */
	private Double latitude;
	/** 速度 */
	private Integer speed;
	/** 方向 */
	private String orientation;
	/** 位置 */
	private String location;
	/** 海拔高度 */
	private Integer altitude;
	/** 是否定位（0是未定位、1是定位） */
	private Integer isLocation;
	/** GPS时间 */
	private Date gpsTime;
	/** 最后返回时间 */
	private Date returnTime;
	/** 0点火、1是熄火 */
	private Integer accStatus;
	/** 显示标志：1-显示；0-不显示(假删除) */
	private Integer flag;
	/** 创建时间 */
	private Date createDataTime;
	/** 创建人 */
	private String createDateUsername;
	/** 更新时间 */
	private Date updateDataTime;
	/** 更新人 */
	private String updateDataUsername;
	/** 人ID */
	private String peopleId;
	/** 物ID */
	private String thingId;
	/** 从业人员 */
	private String professionalsId;
	
}
