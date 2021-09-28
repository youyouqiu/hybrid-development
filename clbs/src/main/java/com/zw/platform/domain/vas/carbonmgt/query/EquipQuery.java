package com.zw.platform.domain.vas.carbonmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
/**
 * 
 * <p>
 * Title: 设备录入query
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
 * @author: fanlu
 * @date 2016年9月19日上午9:11
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class EquipQuery extends BaseQueryBean implements Serializable{
	private static final long serialVersionUID = -2074161134779217606L;
	
	 private String brand;
	 private String fuelType;
	 private String vehicleType;
	 private String groupId;

}
