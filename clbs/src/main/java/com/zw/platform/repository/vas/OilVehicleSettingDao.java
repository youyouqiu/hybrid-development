/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.platform.repository.vas;


import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.OilVehicleSettingForm;
import com.zw.platform.domain.vas.oilmassmgt.query.OilVehicleSettingQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 油量车辆设置Dao层 <p>Title: OilVehicleSettingDao.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年10月24日下午4:36:53
 * @version 1.0
 */
public interface OilVehicleSettingDao {

    /**
     * 查询油量和车的绑定
     */
    Page<OilVehicleSetting> findOilVehicleList(@Param("param") OilVehicleSettingQuery query,
                                               @Param("userId") String userId,
                                               @Param("groupList") List<String> groupList);

    /**
     * TODO 查询参考车辆
     * @Title: findOilBoxVehicle
     * @param userId
     * @param groupList
     * @return
     * @return List<OilVehicleSetting>
     * @throws @author
     *             wangying
     */
    List<OilVehicleSetting> findOilBoxVehicle(@Param("userId") String userId,
                                              @Param("groupList") List<String> groupList);
    
    List<OilVehicleSetting> findVehicleSetting(@Param("userId") String userId);

    Long findOilVehicleList_COUNT(@Param("param") OilVehicleSettingQuery query, @Param("userId") String userId,
                                  @Param("groupList") List<String> groupList);

    /**
     * TODO 查询油箱list
     * @Title: findFuelTankList
     * @return
     * @return List<FuelTank>
     * @throws @author
     *             wangying
     */
    List<FuelTank> findFuelTankList();

    /**
     * TODO 新增车辆设置
     * @Title: addOilSetting
     * @param form
     * @return
     * @return boolean
     * @throws @author
     *             wangying
     */
    boolean addOilSetting(OilVehicleSettingForm form);

    /**
     * TODO 根据id删除
     * @Title: deleteFuelTankBindById
     * @param id
     * @return
     * @return boolean
     * @throws @author
     *             wangying
     */
    boolean deleteFuelTankBindById(@Param("id") String id);

    /**
     * TODO 根据车辆id查询邮箱车辆设置
     * @Title: findOilBoxVehicleByVid
     * @param vId
     * @return
     * @return OilVehicleSetting
     * @throws @author
     *             wangying
     */
    List<OilVehicleSetting> findOilBoxVehicleByVid(@Param("vId") String vid);

    /**
     * TODO 根据车辆与油箱的绑定id查询邮箱车辆设置
     * @Title: findOilBoxVehicleByVid
     * @param id
     * @return
     * @return OilVehicleSetting
     * @throws @author
     *             wangying
     */
    OilVehicleSetting findOilBoxVehicleByBindId(@Param("id") String id);

    /**
     * TODO 修改油箱车辆设置
     * @Title: updateOilSetting
     * @param form
     * @return
     * @return boolean
     * @throws @author
     *             wangying
     */
    boolean updateOilSetting(OilVehicleSettingForm form);

    /**
     * TODO 常规参数修改油箱车辆设置
     * @Title: updateOilSetting
     * @param form
     * @return
     * @return boolean
     * @throws @author
     *             wangying
     */
    boolean updateParamOilSetting(OilVehicleSettingForm form);

    /**
     * TODO 根据id查询油箱车辆设置
     * @Title: selectOilVehicleById
     * @param id
     * @return
     * @return OilVehicleSetting
     * @throws @author
     *             wangying
     */
    OilVehicleSetting selectOilVehicleById(String id);

    /**
     * TODO 修改油箱2 to 油箱1
     * @Title: updateOil2ToOil1
     * @param id
     * @return
     * @return boolean
     * @throws @author
     *             wangying
     */
    boolean updateOil2ToOil1(String id);

    /**
     * TODO 根据油箱id删除油箱与车辆的关联
     * @Title: deleteByOilTankId
     * @param oilBoxId
     * @return
     * @return boolean
     * @throws @author
     *             wangying
     */
    boolean deleteByOilTankId(@Param("oilBoxId") String oilBoxId);

    /**
     * TODO 根据车辆id删除油箱与车辆的关联
     * @Title: deleteOilSettingByVid
     * @param vehicleId
     * @return
     * @return boolean
     * @throws @author
     *             wangying
     */
    boolean deleteOilSettingByVid(@Param("vehicleId") String vehicleId);

    /**
     * 优化缓存使用方法
     * @author wanxing
     * @param vehicleList
     *            车辆id列表
     * @return
     */
    List<OilVehicleSetting> listOilVehicleByIds(@Param("list") List<String> vehicleList,
                                                @Param("tanks") List<String> tankList);

    List<OilVehicleSetting> findBindingOilBoxList();

    boolean deleteBatchOilSettingByVid(@Param("monitorIds") List<String> monitorIds);
}
