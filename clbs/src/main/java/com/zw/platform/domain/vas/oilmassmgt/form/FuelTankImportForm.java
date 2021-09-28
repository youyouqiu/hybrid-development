/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.platform.domain.vas.oilmassmgt.form;


import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;


/**
 * 油箱信息Form
 * <p>Title: FuelTankForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年10月25日下午2:09:17
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FuelTankImportForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = -4658040495054717642L;

    private String carBrand = "";

    private String carType = "";

    private String factoryBatch = "";

    private String displacement = "";

    private String powerRate = "";

    @ExcelField(title = "油箱型号")
    private String type = "";

    private String shape = "";

    @ExcelField(title = "油箱形状")
    private String shapeStr = "";

    @ExcelField(title = "长度(mm)")
    private String boxLength = "";

    @ExcelField(title = "宽度(mm)")
    private String width = "";

    @ExcelField(title = "高度(mm)")
    private String height = "";

    @ExcelField(title = "壁厚(mm)")
    private String thickness = "";

    /**
     * 下圆角半径
     */
    @ExcelField(title = "下圆角半径(mm)")
    private String buttomRadius;

    /**
     * 上圆角半径
     */
    @ExcelField(title = "上圆角半径(mm)")
    private String topRadius;

    @ExcelField(title = "油箱容量(L)")
    private String capacity = "";

    private String addOilTimeThreshold = "";

    private String addOilAmountThreshol = "";

    private String seepOilTimeThreshold = "";

    private String seepOilAmountThreshol = "";

    private String theoryVolume = "";

    private String realVolume = "";

    private String calibrationSets = "";

    private String sensorNumber = "";

    private String sensorLength;

    @ExcelField(title = "备注")
    private String remark;

    /**
     * 油杆上盲区
     */
    private String upperBlindZone = "";

    /**
     * 油杆下盲区
     */
    private String lowerBlindArea = "";

    private String measuringRange = "";

    private String filteringFactorStr = "";

    private String filteringFactor = "";

    private String baudRateStr = "";

    private String baudRate = "";

    private String oddEvenCheck = "";

    private String oddEvenCheckStr = "";

    private String compensationCanMake = "";

    private String compensationCanMakeStr = "";

    /**
     * 传感器id
     */
    private String sensorId = "";

    private String oilBoxId = ""; // 油箱id

    private List<String> oilLevelHeightList; // 一组油位高度模拟量

    private String oilLevelHeights = "";

    private List<String> oilValueList; // 一组油量值

    private String oilValues = "";

    /**
     * 油箱标定
     */
    private List<OilCalibrationForm> oilCalList;

    private String tanktyp = ""; // 油箱型号：油箱1、油箱2

    private String vehicleId = ""; // 与油箱绑定的车辆id

    private String brand = ""; // 与油箱绑定的车辆车牌号

    private String oilLevelHeight = ""; // 油位高度模拟量

    private String oilValue = ""; // 油量值

}
