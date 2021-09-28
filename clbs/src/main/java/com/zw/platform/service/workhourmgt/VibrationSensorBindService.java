package com.zw.platform.service.workhourmgt;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorBindForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorBindQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: 振动传感器绑定Service</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年9月19日上午9:29:52
 */
public interface VibrationSensorBindService {

    /**
     * 查询振动传感器绑定（分页）
     */
    Page<VibrationSensorBind> findWorkHourSensorBind(VibrationSensorBindQuery query);

    /**
         * @return List<FuelVehicle>
     * @throws
     * @Title: 查询参考车辆
     * @author wangying
     */
    List<VibrationSensorBind> findReferenceVehicle() throws Exception;

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 新增工时车辆设置
     * @author wangying
     */
    JsonResultBean addWorkHourSensorBind(VibrationSensorBindForm form, String ipAddress) throws Exception;

    /**
         * @param id
     * @return VibrationSensorBind
     * @throws
     * @Title: 根据id查询工时车辆设置
     * @author wangying
     */
    VibrationSensorBind findWorkHourVehicleById(String id) throws Exception;

    /**
         * @return VibrationSensorBind
     * @throws
     * @Title: 根据车辆id查询工时车辆设置
     * @author wangying
     */
    VibrationSensorBind findWorkHourVehicleByVid(String vehicleId) throws Exception;

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 修改车辆与传感器绑定
     * @author wangying
     */
    JsonResultBean updateWorkHourSensorBind(VibrationSensorBindForm form, String ipAddress) throws Exception;

    /**
         * @param id
     * @return boolean
     * @throws
     * @Title: 根据id删除工时车辆设置
     * @author wangying
     */
    JsonResultBean deleteWorkHourSensorBindById(String id, String ipAddress) throws Exception;

    /**
         * @param paramList
     * @return void
     * @throws
     * @Title: 工时下发
     * @author wangying
     */
    void sendWorkHour(ArrayList<JSONObject> paramList, String ipAddress) throws Exception;

}
