package com.zw.platform.domain.vas.monitoring.query;


import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode(callSuper = false)
public class RealTimeCommandQuery extends BaseQueryBean{

	private static final long serialVersionUID = 1L;
	
	private String vehicleIdList;
}
