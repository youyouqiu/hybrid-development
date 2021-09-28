package com.zw.platform.domain.systems.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ParameterQuery extends BaseQueryBean implements Serializable{
	private static final long serialVersionUID = 1L;
	private String monitoring;
	private String groupId;
	private String device;
	private String equipment;
	private String simcard;
	private String parameterType;
	private String name;
	private String downTime;
	private String status;
	private String remark;
	private List<String> vehicleIds;
}
