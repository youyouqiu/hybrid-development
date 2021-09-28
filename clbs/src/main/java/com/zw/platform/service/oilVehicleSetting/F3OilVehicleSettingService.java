package com.zw.platform.service.oilVehicleSetting;

import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.loadmgt.LoadVehicleSettingInfo;
import com.zw.platform.domain.vas.loadmgt.form.LoadVehicleSettingSensorForm;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;


/**
 * <p>
 * Title:
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月09日 16:00
 */
public interface F3OilVehicleSettingService {

    /**
     * 处理远程升级指令
     *
     * @param wirelessParam
     * @param vehicleId
     * @param commandType
     * @param ipAddress
     * @param mark          0: 油量; 1: 工时，3:主动安全远程升级
     * @return
     * @throws Exception
     */
    JsonResultBean updateWirelessUpdate(WirelessUpdateParam wirelessParam,
        String vehicleId, Integer commandType, String ipAddress, int mark) throws Exception;

    /**
     * 修改常规参数设置
     *
     * @param setting
     * @return
     */
    JsonResultBean updateRoutineSetting(OilVehicleSetting setting, String dealType, String ipAddress) throws Exception;

    /**
     * 修改标定参数设置
     *
     * @param list
     * @return
     */
    JsonResultBean updateDemarcateSetting(List<OilCalibrationForm> list,
        OilVehicleSetting setting, String ipAddress) throws Exception;

    /**
     * 获取F3传感器参数
     *
     * @param brand
     * @param sensorId
     * @param commandType
     * @return
     */
    public JsonResultBean sendF3SensorParam(String brand, String sensorId, String commandType) throws Exception;

    /**
     * 获取F3传感器私有参数
     *
     * @param vehicleId  监控对象ID
     * @param sensorID   外设ID
     * @param commandStr 参数ID
     * @param ipAddress  IP地址
     * @param sign       标识符,区分是哪个模块进行的操作,方便记录日志
     * @return
     * @throws Exception
     */
    public JsonResultBean sendF3SensorPrivateParam(String vehicleId, String sensorID,
        String commandStr, String ipAddress, String sign) throws Exception;

    /**
     * 修改油耗常规参数设置
     *
     * @param setting
     * @return
     */
    public JsonResultBean updateFuelSetting(FuelVehicle setting, String dealType, String ipAddress) throws Exception;

    /**
     * 修改工时常规参数设置
     *
     * @param workHourSettingInfo workHourSettingInfo
     * @param dealType
     * @param ipAddress           ip
     * @param mark                0:常规参数下发/参数下发; 1:基值修正下发
     * @return JsonResultBean
     * @throws Exception ex
     */
    JsonResultBean updateWorkHourSetting(WorkHourSettingInfo workHourSettingInfo, String dealType,
        String ipAddress, int mark) throws Exception;

    /**
     * 修改载重常规参数设置
     *
     * @param setting   workHourSettingInfo
     * @param dealType
     * @param ipAddress ip
     * @param i         0:常规参数下发/参数下发; 1:基值修正下发
     * @return JsonResultBean
     * @throws Exception ex
     */
    JsonResultBean updateLoadSetting(LoadVehicleSettingInfo setting, String dealType, String ipAddress, int i)
        throws Exception;

    /**
     * 修改载重标定设置
     *
     * @param list
     * @param setting
     * @param ip
     * @return
     */
    JsonResultBean updateLoadAdSetting(LoadVehicleSettingSensorForm list, LoadVehicleSettingInfo setting, String ip)
        throws Exception;

    /**
     * 处理风险定义远程升级指令
     *
     * @param wirelessParam
     * @param vehicleId
     * @return
     */
    JsonResultBean updateWirelessUp(WirelessUpdateParam wirelessParam, String vehicleId, Integer commandType,
        String ipAddress) throws Exception;
}
