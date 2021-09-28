package com.zw.platform.domain.vas.carbonmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class FuelTypeQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String fuelType; //燃料类型
	//0:柴油 1：汽油 2：天然气
	private String fuelCategory; // 燃料类别
	private String describes; //燃料类型描述
	private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;

}
