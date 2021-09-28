package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleTypeQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String vehicleCategory;//车辆类别id
	private String category;//车辆类别
	private String ico;//图标
	private String vehicleType;//车辆类型
	private String description;//类型描述
	private Short flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
    private String standard;
}
