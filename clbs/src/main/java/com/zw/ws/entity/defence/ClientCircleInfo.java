package com.zw.ws.entity.defence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class ClientCircleInfo extends RegionAttribute implements Serializable {
	
	private static final long serialVersionUID = 2L;

	//圆形ID
	private String circleID;
	
	//圆形名称
	private String name;
	
	//圆形描述
	private String description;
	
	//区域类型
	private String regionType;
	
	//中心点经度
	private double centerX;
	
	//中心点纬度
	private double centerY;
	
	//圆半径
	private double rRadius;
	
	//创建人姓名
	private String userName;	
}
