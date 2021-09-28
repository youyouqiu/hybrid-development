package com.zw.platform.domain.basicinfo;

import lombok.Data;

import java.util.Date;

@Data
public class ProfessionalsType {
	 private static final long serialVersionUID = 1L;
	    private String id;
	    private String professionalstype;
	    private String description;
		private Integer flag;
	    private Date createDataTime;
	    private String createDataUsername;
	    private Date updateDataTime;
	    private String updateDataUsername;
}
