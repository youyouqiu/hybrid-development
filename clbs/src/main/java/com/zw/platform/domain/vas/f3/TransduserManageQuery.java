package com.zw.platform.domain.vas.f3;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class TransduserManageQuery extends BaseQueryBean implements Serializable{
	private static final long serialVersionUID = 1L;
	int type;//传感器类型
}
