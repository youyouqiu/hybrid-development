package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.loadmgt.ZwMCalibration;
import org.apache.ibatis.annotations.Param;

/***
 @Author gfw
 @Date 2018/9/17 8:39
 @Description 载重标定Dao层
 @version 1.0
 **/
public interface LoadAdDao {
    /**
     * AD标定表导入数组
     * @param id 标定表id
     * @param flag flag
     * @param userName 用户名
     * @param calibation JSON数组
     * @param id
     * @param flag
     * @param sensorVehicleId
     * @param sensorId
     * @param vehicleId
     * @param userName
     * @param calibation
     * @return
     */
    Integer addByBatch(@Param("id")String id, @Param("flag")String flag,@Param("sensorVehicleId") String sensorVehicleId,
                       @Param("sensorId")String sensorId,@Param("vehicleId")String vehicleId,
                       @Param("userName")String userName, @Param("calibation")String calibation);

    /**
     * 根据传感器车辆绑定id查询AD列表
     * @param id
     * @param sensorVehicleId
     * @return
     */
    ZwMCalibration findByIdAndSensorId(String id,String sensorVehicleId);

    /**
     * 修改ad标定表
     * @param id
     * @param sensorId
     * @param calibrationValue
     * @return
     */
    int updateCalibration(@Param("id")String id,@Param("sensorId") String sensorId,@Param("calibrationValue") String calibrationValue);

    /**
     * 根据传感器车辆绑定id查询AD列表
     * @param sensorVehicleId
     * @return
     */
    ZwMCalibration findBySensorVehicleId(@Param("sensorVehicleId") String sensorVehicleId);

    /**
     * 根据关联表id删除AD值
     * @param id
     */
    void deleteAdLoad(String id);
}
