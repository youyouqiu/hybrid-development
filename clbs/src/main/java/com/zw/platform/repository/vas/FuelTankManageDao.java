package com.zw.platform.repository.vas;


import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankImportForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.query.FuelTankQuery;
import com.zw.platform.util.common.BusinessException;


/**
 * 油箱管理Dao层 <p>Title: FuelTankManageDao.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年10月24日下午4:36:53
 * @version 1.0
 */
public interface FuelTankManageDao {

    /**
     * 分布查询油箱列表
     * @Title: findFuelTankByPage
     * @param query
     * @return
     * @throws BusinessException
     * @return List<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    public List<FuelTankForm> findFuelTankByPage(FuelTankQuery query) throws BusinessException;

    /**
     * 根据油箱id查询油量车辆设置
     * @Title: findOilVehicleSettingByOilBoxId
     * @param oilBoxId
     * @return
     * @throws BusinessException
     * @return List<OilVehicleSettingForm>
     * @throws @author
     *             Liubangquan
     */
    public Integer findOilVehicleSettingByOilBoxId(String oilBoxId) throws BusinessException;

    /**
     * 获取油杆传感器列表
     * @Title: findRodSensorList
     * @param boxHeight
     *            油箱高度
     * @return
     * @throws BusinessException
     * @return List<RodSensor>
     * @throws @author
     *             Liubangquan
     */
    public List<RodSensor> findRodSensorList(@Param("boxHeight") String boxHeight) throws BusinessException;

    /**
     * 根据传感器id查询传感器详细信息
     * @Title: getSensorDetail
     * @param sensorId
     * @return
     * @throws BusinessException
     * @return List<RodSensor>
     * @throws @author
     *             Liubangquan
     */
    public List<RodSensorForm> getSensorDetail(@Param("sensorId") String sensorId) throws BusinessException;

    /**
     * 新增油箱
     * @Title: addFuelTank
     * @param form
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public boolean addFuelTank(FuelTankForm form) throws BusinessException;

    /**
     * 删除油箱
     * @Title: deleteFuelTankById
     * @param id
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public boolean deleteFuelTankById(String id) throws BusinessException;

    /**
     * 查询油箱详细信息
     * @Title: getFuelTankDetail
     * @param id
     * @return
     * @throws BusinessException
     * @return List<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    public List<FuelTankForm> getFuelTankDetail(String id) throws BusinessException;

    /**
     * 修改油箱
     * @Title: editFuelTank
     * @param form
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public boolean updateFuelTank(FuelTankForm form) throws BusinessException;

    /**
     * 新增油量标定表
     * @Title: addOilCalibration
     * @param form
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public boolean addOilCalibration(OilCalibrationForm form);

    /**
     * 删除油量标定表
     * @Title: deleteOilCalibration
     * @param id
     *            油量标定表id
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public boolean deleteOilCalibration(String vehicleId);

    /**
     * 根据油量车辆绑定表id读取油箱标定数据表
     * @Title: getOilCalibrationList
     * @param oilBoxVehicleId
     * @return
     * @throws BusinessException
     * @return List<OilCalibrationForm>
     * @throws @author
     *             Liubangquan
     */
    public List<OilCalibrationForm> getOilCalibrationList(String oilBoxVehicleId);

    /**
     * 根据车辆id获取与车辆绑定的油箱的标定数据
     * @Title: getOilCalibrationByVid
     * @param vehicleId
     *            车辆id
     * @return
     * @throws BusinessException
     * @return List<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    public List<FuelTankForm> getOilCalibrationByVid(String vehicleId) throws BusinessException;

    /**
     * @Description:根据车辆与油箱绑定id获取与车辆绑定的油箱的标定数据
     * @param vehicleId
     * @return
     * @throws BusinessException
     *             List<FuelTankForm>
     * @exception:
     * @author: wangying
     * @time:2016年12月30日 下午2:26:00
     */
    public List<FuelTankForm> getOilCalibrationByBindId(String id) throws BusinessException;

    /**
     * 根据油箱型号查询油箱
     * @Title: getOilBoxByType
     * @param type
     * @return
     * @throws BusinessException
     * @return List<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    public List<FuelTankForm> getOilBoxByType(String type) throws BusinessException;

    /**
     * 批量新增油箱
     * @param list
     * @return
     */
    // boolean addTankByBatch(List<FuelTankForm> list);
    boolean addTankByBatch(List<FuelTankImportForm> list);

    /**
     * 根据车辆id查询与其绑定的油箱信息
     * @Title: getFuelTankDetailByVehicleId
     * @param vehicleId
     * @return
     * @return List<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    public List<FuelTankForm> getFuelTankDetailByVehicleId(String vehicleId);

    /**
     * 校验油箱是否被绑定
     * @Title: checkBoxBound
     * @param oilBoxId
     * @return
     * @return int
     * @throws
     * @author Liubangquan
     */
    int checkBoxBound(String oilBoxId);

    FuelTankForm isExist(@Param("id") String id, @Param("type") String type);

    /**
     * 根据id查询油箱
     * @param id
     * @return
     */
    FuelTank findFuelTankById(@Param("id") String id);

	/**
	 * 根据查询条件
	 * @param id
	 * @return
	 */
	List<OilVehicleSetting> findVehicleBindingOilBox(String id);

}
