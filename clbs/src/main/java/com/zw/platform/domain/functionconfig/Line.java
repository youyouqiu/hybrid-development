package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 线路主表
 * <p>Title: Line.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年8月18日下午4:23:29
 * @version 1.0
 */
@Data
public class Line implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String id; // 线的坐标
	private String name; // 线路名称
	private String description; // 描述
	private String type; // 类型
	private String groupId;
	private String groupName;
	private Integer flag; // 逻辑删除标志
	private Date createDataTime;
	private String createDataUsername;
	private Date updateDataTime;
	private String updateDataUsername;
	private Integer width; // 宽度	
}
