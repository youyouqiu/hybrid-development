package com.zw.ws.entity.defence;
import lombok.Data;

@Data
public class RegionAttribute {

	private String zoneID;
	
	//类型，2：矩形；3：多边形；4：圆形
	private Integer type;
	
	//区域属性
	private Integer attribute;
	
	private long startTime;
	
	private long endTime;
	
	private double maxSpeed;
	
	//路段超速持续时间
	private int overspeedLastTime;
}
