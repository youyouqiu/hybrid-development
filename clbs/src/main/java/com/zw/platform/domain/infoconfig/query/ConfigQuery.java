package com.zw.platform.domain.infoconfig.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 信息配置查询 
 * <p>Title: ConfigQuery.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年7月26日上午11:01:57
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConfigQuery extends BaseQueryBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String id = ""; // 信息配置
	private String vehicleid = ""; // 车辆ID
	private String groupid = ""; // 分组ID
	private String deviceid=""; // 终端ID
	private String sim_cardid="";//SIMID
	private String peripheralsid="";//外设ID
	private String servicelifecycle_id="";//服务周期ID
	private Integer alarmstatus; // 报警状态
	private Date alarmtime; // 报警时间
	private Integer onlinestatus; // 在线状态
	private Date offlinetime; // 离线时间
	private Date onlinetime; // 在线时间
	private Double longitude; //最后车的经度
	private Double latitude; //最后车的纬度
	private Integer speed; // 速度
	private String orientation="";//方向
	private String location="";//位置
	private Integer altitude; // 海拔高度
	private Integer islocation; // 是否定位
	private Date gpstime; // GPS时间
	private Date returntime; // 最后返回时间
	private Date accstatus; // 熄火状态
	private Integer flag; // 1-显示；0-不显示（假删除）
	private Date createDateTime; // 创建时间
	private String createDateUsername = ""; // 创建人
	private Date updateDateTime; // 更新时间
	private String updateDateUsername = ""; // 更新人
	private String peopleid = ""; // 人物ID
	private String thingid = ""; // 物品ID
	private String professionalsid = ""; // 从业人员ID
	
	// 列表query
	private String configId; // 信息配置id
	private String carLicense; // 车牌号
	private String peopleName; // 人名
	private String thingName; // 物名
	private String groupId; // 组id
	private String groupName; // 组名
	private String groupType;
	private String deviceNumber; // 终端编号
	private String simcardNumber; // SIM卡号
	
}
