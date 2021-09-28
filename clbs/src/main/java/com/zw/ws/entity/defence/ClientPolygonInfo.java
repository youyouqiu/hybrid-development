package com.zw.ws.entity.defence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
public class ClientPolygonInfo extends RegionAttribute implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4823997662495026185L;

	//多边形ID
	public String polygonID;
	
	//区域名称
	private String name;
	
	//区域描述
	private String description;
	
	//区域类型
	private String regionType;	
	
	//经纬度点集
	private List<PointLngLat> points=new ArrayList<PointLngLat>();
	
	//创建人姓名
	private String userName;
}
