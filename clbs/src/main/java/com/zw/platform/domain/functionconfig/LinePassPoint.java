package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 途经点实体
 * @author tangshunyu
 *
 */
@Data
public class LinePassPoint implements Serializable {

	private static final long serialVersionUID = 1L;
	private String lineId;	//行驶线路Id
	private Integer sortOrder;	//顺序
	private Double longitude;	//经度
	private Double latitude;	//纬度
	private Integer flag;			//逻辑删除标志
	private Date createDataTime;
	private String createDataUsername;
	private Date updateDataTime;
	private String updateDataUsername;
}
