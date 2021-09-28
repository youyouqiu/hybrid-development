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
 * 油量标定表Form
 * <p>Title: OilCalibrationForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年11月1日上午11:39:04
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OilCalibrationForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = -4658040495054717642L;
	
	private String oilBoxVehicleId = ""; // 油量车辆设置表id
	private String oilBoxType = ""; // 油箱号：1-油箱1；2-油箱2
	private String oilLevelHeight = ""; // 油位高度模拟量
	private String oilValue = ""; // 油量值
	
}
