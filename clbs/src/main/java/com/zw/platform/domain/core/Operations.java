package com.zw.platform.domain.core;

import lombok.Data;

import java.io.Serializable;

@Data
public class Operations implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String operationType;//运营资质类别
	
	private String explains;//说明 
		
}
