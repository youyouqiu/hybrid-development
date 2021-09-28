package com.zw.talkback.domain.basicinfo.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zw.platform.util.common.BaseQueryBean;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 
 * <p>
 * Title: 分组管理Query
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * 
 * @author: wangying
 * @date 2016年10月9日下午6:15:22
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AssignmentQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 分组表
	 */
	@ApiParam(value = "分组id")
	private String id;

	/**
	 * 分组名称
	 */
	@ApiParam(value = "分组名称")
	private String name;

	/**
	 * 监控对象类型
	 */
	@ApiParam(value = "监控对象类型")
	private String type;

	@ApiParam(value = "备注")
	private String description;
	
	/**
	 *  联系人
	 */
	@ApiParam(value = "联系人")
	private String contacts;
	
	/**
	 *  电话号码
	 */
	@ApiParam(value = "电话号码")
	private String telephone;

	@JsonIgnoreProperties
	private Short flag;

	private Date createDataTime;

	private String createDataUsername;

	private Date updateDataTime;

	private String updateDataUsername;
	
	private String groupId;
	
	private List<String> groupList;
}
