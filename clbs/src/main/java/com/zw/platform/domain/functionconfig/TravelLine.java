package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 行驶路线实体
 * @author tangshunyu 
 *
 */
@Data
public class TravelLine implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String name;	//行驶路线名称
	private Double startLongitude;	//开始位置经度
	private Double startLatitude;	//开始位置纬度
	private Double endLongitude;	//结束位置经度
	private Double endLatitude;		//结束位置
	private String lineType;		//路线类型
	private Integer lineOffset;		//路线偏移量
	private String description;		//描述
	private Integer flag;			//逻辑删除标志
	private Date createDataTime;
	private String createDataUsername;
	private Date updateDataTime;
	private String updateDataUsername;
	

}
