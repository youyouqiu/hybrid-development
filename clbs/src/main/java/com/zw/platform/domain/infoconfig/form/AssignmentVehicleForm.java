package com.zw.platform.domain.infoconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 车辆与分组关联表form
 * <p>Title: AssignmentVehicleForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author Liubangquan
 * @date 2016年10月12日下午4:13:50
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false,
    exclude = { "monitorType", "assignmentName", "sourceAssignId", "sourceAssignName" })
@NoArgsConstructor
public class AssignmentVehicleForm extends BaseFormBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 分组id
	 */
	private String assignmentId = "";

	private String assignmentName = "";
	/**
	 * 源分组id
	 */
	private String sourceAssignId = "";

	private String sourceAssignName = "";
	/**
	 * 车辆id
	 */
	private String vehicleId = "";
	
	private String monitorType; // 监控对象类型

	public AssignmentVehicleForm(String assignmentId, String name, String vehicleId) {
		this.assignmentId = assignmentId;
		this.assignmentName = name;
		this.vehicleId = vehicleId;
	}
}
