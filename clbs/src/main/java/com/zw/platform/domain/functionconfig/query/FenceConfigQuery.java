package com.zw.platform.domain.functionconfig.query;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.zw.platform.util.common.BaseQueryBean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * 终端Query
 * 
 * @author wangying
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FenceConfigQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 电子围栏信息
	 */
	private String id;

	/**
	 * 类型
	 */
	private String type;

	/**
	 * 形状（每个形状的ID）
	 */
	private String shape;

	/**
	 * 预览
	 */
	private String preview;

	private Integer flag;

	private Date createDataTime;

	private String createDataUsername;

	private Date updateDataTime;

	private String updateDataUsername;
	
	private List<String> vehicleIds;
	
	private List<String> queryFenceId;
	private String queryFenceIdStr;
	private List<String> groupIds;
}
