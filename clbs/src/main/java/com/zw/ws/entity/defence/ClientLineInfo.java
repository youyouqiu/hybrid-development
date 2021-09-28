package com.zw.ws.entity.defence;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClientLineInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	//线路ID
	private String lineID;
	
	//线路名称
	private String name;
	
	//描述
	private String description;
	
	//线路宽度
	private String lineWidth;
		
	private int lineType;
	
	private String userName;
		
	private List<PointLngLat> points=new ArrayList<PointLngLat>();
}
