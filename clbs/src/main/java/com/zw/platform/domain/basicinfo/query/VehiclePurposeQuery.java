package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 车辆用途query
 * @author tangshunyu
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehiclePurposeQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String purposeCategory; //车辆用途
	private String description; //说明
	private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
}
