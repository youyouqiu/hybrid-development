package com.zw.ws.entity.defence;

import lombok.Data;

import java.io.Serializable;

@Data
public class PointLngLat implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 7070491821436430836L;
	private double lng;
	private double lat;
}
