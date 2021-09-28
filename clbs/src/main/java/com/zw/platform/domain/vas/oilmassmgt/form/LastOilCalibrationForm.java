/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with ZhongWei.
 */
package com.zw.platform.domain.vas.oilmassmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 车辆最后一次标定时间Form
 * <p>Title: LastOilCalibrationForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2017年1月18日下午3:49:08
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LastOilCalibrationForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = -4658040495054717642L;
	
	private String vehicleId = ""; // 车辆id
	private String oilBoxType = ""; // 油箱号：1-油箱1；2-油箱2
	private String lastCalibrationTime = ""; // 最后一次标定时间
	
}
