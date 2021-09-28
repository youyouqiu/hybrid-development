package com.zw.platform.domain.infoconfig;

import lombok.Data;

import java.io.Serializable;

/**
 * 关联信息实体
 * @author hujun
 *
 */
@Data
public class RelateConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	private String professionalNames;//从业人员名字
	private String assignmentName;//分组信息
	private String carLicense;//车牌号
	private String simCardNumber;//SIM卡号
	private String deviceNumber;//终端编号
	private String vehicleId;//车辆id
	private String groupId;//所属企业id
}
