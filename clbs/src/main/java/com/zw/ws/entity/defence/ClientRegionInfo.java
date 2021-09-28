package com.zw.ws.entity.defence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
public class ClientRegionInfo extends RegionAttribute implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	//矩形ID
	private String recID;
	
	//左上角纬度
	private double topleftLat;
	
	//右上角经度
	private double topleftLong;
	
	//右下角纬度
	private double bottomrightLat;
	
	//右下角经度
	private double bottomrightLong;
	
	
	
	//区域名称
	private String name;
	
	//区域描述
	private String description;
	
	//区域类型
	//private String regionType;	
	
	//经纬度点集
	private List<PointLngLat> points=new ArrayList<PointLngLat>();
	
	//创建人姓名
	private String userName;
		
}
