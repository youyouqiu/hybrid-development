package com.zw.platform.domain.infoconfig.query;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 极速录入数据查询
 * @author hujun
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SpeedQueryList implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * 唯一标识（0：设备号，1：SIM卡号）
	 */
	private Integer identifyNumber;
	/**
	 * 标识包含类型
	 */
	private List<String> identifyType;
	
}
