package com.zw.ws.entity.adas;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class RiskCampaign {
	private String id;
	private String vehicleId;//车辆id
	private String riskNumber;//风险编号
	private int riskLevel;//风险等级
	private String riskType;//风险类型
	private int status;//风险状态
	private double speed;//速度
	private String dealId;//处理人
	private String driverId;//司机
	private String address;//位置
	private String job;//岗位
	private Date fileTime;//归档时间
	private Date dealTime;//处理时间
	private int riskResult;//风控结果
	/**
     * 0不显示、1显示
     */
    private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
	
	private long startTime;
	private long endTime;
	private Date warningTime; // 开始时间
	private Date endTimeStr;
	private long tempTime;
	private List<RiskAlarm> riskEventList;//风险-报警
	
}
