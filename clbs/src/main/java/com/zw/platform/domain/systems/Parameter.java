package com.zw.platform.domain.systems;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Parameter implements Serializable{
	 private static final long serialVersionUID = 1L;
	    private String id;
		private String monitoring;
		private String groupId;
		private String groupName;
		private String device;
		private String equipment;
		private String simcard;
		private String parameterType = "1";
		private String name;
		private String downTime;
		private Integer status;
		private String remark;
		private String directiveName;
		private Date createDataTime;
}
