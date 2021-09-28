/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.platform.service.oilmassmgt;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.query.OilVehicleSettingQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 油量车辆设置Service <p>Title: FuelTankManageService.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月24日下午4:28:42
 */
public interface OilVehicleSettingService {

    /**
     * 查询车辆与油箱的绑定
     * @param query 查询条件
     * @return Page<OilVehicleSetting>
     * @throws @Title: findOilVehicleList
     * @author wangying
     */
    Page<OilVehicleSetting> findOilVehicleList(OilVehicleSettingQuery query);

    /**
     * 查询车辆与油箱的绑定单个车
     * @param vehicleId
     * @return
     * @throws Exception
     */
    OilVehicleSetting findOilVehicleByVid(String vehicleId) throws Exception;

    /**
         * @return List<FuelVehicle>
     * @throws @Title: 查询参考车辆
     * @author wangying
     */
    List<DoubleOilVehicleSetting> findReferenceVehicle() throws Exception;

    /**
     * TODO 查询油箱list
     * @return List<FuelTank>
     * @throws @Title: findFuelTankList
     * @author wangying
     */
    List<FuelTank> findFuelTankList() throws Exception;

    /**
     * @return List<RodSensor>
     * @Description:查询油杆传感器
     * @exception:
     * @author: wangying
     * @time:2016年12月2日 下午2:40:39
     */
    List<RodSensor> findRodSensorList() throws Exception;

    /**
     * TODO 新增绑定
     * @param bean
     * @return boolean
     * @throws @Title: addFuelTankBind
     * @author wangying
     */
    JsonResultBean addFuelTankBind(DoubleOilVehicleSetting bean, String ipAddress) throws Exception;

    /**
     * TODO 根据id删除
     * @param id
     * @return boolean
     * @throws @Title: deleteFuelTankBindById
     * @author wangying
     */
    JsonResultBean deleteFuelTankBindById(String id, String ipAddress) throws Exception;

    /**
     * TODO 根据车辆id查询车辆油箱设置
     * @param vid
     * @return DoubleOilVehicleSetting
     * @throws @Title: findOilVehicleSettingByVid
     * @author wangying
     */
    DoubleOilVehicleSetting findOilVehicleSettingByVid(String vid) throws Exception;

    /**
     * TODO 修改油箱车辆设置
     * @param bean
     * @return boolean
     * @throws @Title: updateOilVehicleSetting
     * @author wangying
     */
    JsonResultBean updateOilVehicleSetting(DoubleOilVehicleSetting bean, String ipAddress) throws Exception;

    /**
     * TODO 根据id查询车辆油箱绑定
     * @return OilVehicleSetting
     * @throws @Title: selectOilVehicleById
     * @author wangying
     */
    OilVehicleSetting selectOilVehicleById(String id) throws Exception;

    /**
     * TODO 根据车辆id查询车辆油箱设置list
     * @param vehicleId
     * @return List<OilVehicleSetting>
     * @throws @Title: findOilBoxVehicleByVid
     * @author wangying
     */
    List<OilVehicleSetting> findOilBoxVehicleByVid(String vehicleId) throws Exception;

    /**
     * TODO 根据车辆与油箱的绑定id查询车辆油箱设置list
     * @param id
     * @return List<OilVehicleSetting>
     * @throws @Title: findOilBoxVehicleByVid
     * @author wangying
     */
    OilVehicleSetting findOilBoxVehicleByBindId(String id) throws Exception;

    /**
     * TODO 下发油箱数据
     * @param paramList
     * @param ipAddress
     * @return String 返回msgSN，用于匹配相同的msgSN并推送到指定用户
     * @throws @Title: sendOil
     * @author wangying
     */
    String sendOil(ArrayList<JSONObject> paramList, String ipAddress) throws Exception;

    /**
     * TODO 根据车辆id删除车辆与油箱的关联
     * @param vehicleId
     * @param type
     * @return boolean
     * @throws @Title: deleteOilSettingByVid
     * @author wangying
     */
    boolean deleteOilSettingByVid(String vehicleId, Integer type);

    /**
     * TODO 查询参考车牌
     * @return
     * @throws Exception
     */
    List<OilVehicleSetting> findReferenceBrand() throws Exception;

    List<DoubleOilVehicleSetting> findReferenceVehicleByProtocols(List<Integer> protocols) throws Exception;
}
