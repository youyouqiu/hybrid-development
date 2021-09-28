package com.zw.platform.domain.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 终端实体
 * 
 * @author wangying
 *
 */
@Data
public class RoleResource implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 角色资源中间表
	 */
	private String id;

	private String resourceId;

	private String roleId;

	/**
	 * 0表示可读、1表示可写
	 */
	private Integer editable;

	private Date createDataTime;

	private String createDataUsername;

	private Date updateDataTime;

	private String updateDataUsername;

	private Integer flag;

}
