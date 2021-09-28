package com.zw.platform.service.oilmgt;

import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorBindForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorBindQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

/**
 * <p>Title: 流量传感器绑定Service</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * 2016年9月19日上午9:29:52
 *
 * @version 1.0
 * @author wangying
 */
public interface FluxSensorBindService {

    /**
     * 查询流量传感器绑定（分页）
     */
    List<FuelVehicle> findFluxSensorBind(FluxSensorBindQuery query) throws Exception;

    /**
     * 查询流量传感器绑定
     */
    FuelVehicle findFluxSensorByVid(String vehicleId);

    /**
     * 查询参考车辆
     * @author wangying
     */
    List<FuelVehicle> findReferenceVehicle() throws Exception;

    /**
     * 根据id查询车辆查询油耗车辆设置
     * @author wangying
     */
    FuelVehicle findFuelVehicleById(String id) throws Exception;

    /**
     * 根据车辆id查询车辆查询油耗车辆设置
     * @author wangying
     */
    FuelVehicle findFuelVehicleByVid(String vehicleId);

    /**
     * 新增车辆与传感器绑定
     * @author wangying
     */
    JsonResultBean addFluxSensorBind(FluxSensorBindForm form, String ipAddress);

    /**
     * 修改车辆与传感器绑定
     * @author wangying
     */
    JsonResultBean updateFluxSensorBind(FluxSensorBindForm form, String ipAddress) throws Exception;

    /**
     * 删除车辆与传感器绑定
     * @author wangying
     */
    JsonResultBean deleteFluxSensorBind(String id, String ipAddress) throws Exception;

    /**
     * 下发油耗参数
     * @author wangying
     */
    JsonResultBean sendFuel(String sendParam, String ipAddress) throws Exception;

    /**
     * 远程升级
     * @author lifudong
     */
    JsonResultBean updateWirelessUpdate(WirelessUpdateParam wirelessParam, String vehicleId, Integer commandType,
        String ipAddress) throws Exception;

    List<FuelVehicle> findReferenceVehicleByProtocols(List<Integer> protocols);
}
