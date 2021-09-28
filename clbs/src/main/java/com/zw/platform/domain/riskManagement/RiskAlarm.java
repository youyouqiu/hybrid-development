package com.zw.platform.domain.riskManagement;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class RiskAlarm implements Serializable {
	private String id;
	private String eventNumber;//报警编号
	private String eventId;//事件id
	private String riskId;//风险id
	private long startTime;//开始时间
	private long endTime;//结束时间
	private Date eventTimeStr;
	private Date endTimeStr;
	private long continueTime;//持续时间
	private int riskEventLevel;//等级
	
	/**
     * 0不显示、1显示
     */
    private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;

}
