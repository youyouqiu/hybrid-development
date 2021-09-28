/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with ZhongWei.
 */

package com.zw.platform.service.oilmassmgt;

import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.LastOilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;

import java.util.List;
import java.util.Map;

/**
 * 油量标定Service
 * <p>Title: OilCalibrationService.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author Liubangquan
 * @version 1.0
 * @date 2016年12月14日下午3:13:01
 */
public interface OilCalibrationService {

    /**
     * 查询车辆列表：和油箱、传感器绑定的车辆
     * @author Liubangquan
     */
    List<OilVehicleSetting> getVehicleList();

    /**
     * 根据车辆id查询与车辆绑定油箱的标定数据
     * @author Liubangquan
     */
    Map<String, List<OilCalibrationForm>> getOilCalibrationByVid(String vehicleId);

    /**
     * 修正油箱标定数据
     */
    boolean updateOilCalibration(String vehicleId, String oilBoxVehicleIds, String oilBoxVehicleIds2,
        String oilLevelHeights, String oilLevelHeights2, String oilValues, String oilValues2);

    /**
     * 查询指定车辆最近一次上传的记录
     * @author Liubangquan
     */
    String getLatestPositional(String vehicleId);

    /**
     * 根据车辆id获取其标定状态
     * @author Liubangquan
     */
    String getCalibrationStatusByVid(String vehicleId);

    /**
     * 重置标定状态
     * @param vehicleId         车辆id
     * @param calibrationStatus 标定状态
     * @author Liubangquan
     */
    boolean updateCalibrationStatusByVid(String vehicleId, String calibrationStatus);

    /**
     * 判断车辆是否绑定油箱和传感器
     * @author Liubangquan
     */
    boolean findIsBondOilBox(String vehicleId);

    /**
     * 保存车辆最后一次标定的时间
     * @author Liubangquan
     */
    void saveLastCalibration(LastOilCalibrationForm form);

    /**
     * 根据车辆id删除车辆最后一次标定的时间
     * @author Liubangquan
     */
    void deleteLastCalibration(String vehicleId);

    /**
     * 根据车辆id获取车辆最后一次标定的时间
     * @author Liubangquan
     */
    List<LastOilCalibrationForm> getLastCalibration(String vehicleId);

    /**
     * 根据车辆id获取其更新时间，油量标定标定状态异常数据还原时使用
     * @author Liubangquan
     */
    String getCalibrationUpdateTimeByVid(String vehicleId);

}
