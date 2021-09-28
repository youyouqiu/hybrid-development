package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 多边形点
 * <p>Title: PolygonContent.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年9月2日下午4:19:41
 * @version 1.0
 */
@Data
public class PolygonContent implements Serializable {
	private static final long serialVersionUID = 1L;
	private String polygonId; // 多边形主表id
	private Integer sortOrder; // 顺序
	private Double longitude; // 经度
	private Double latitude; // 纬度
	private Integer flag; // 逻辑删除标志
	private Date createDataTime;
	private String createDataUsername;
	private Date updateDataTime;
	private String updateDataUsername;

}
