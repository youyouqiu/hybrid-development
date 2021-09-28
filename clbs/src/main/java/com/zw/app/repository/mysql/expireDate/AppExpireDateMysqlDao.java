package com.zw.app.repository.mysql.expireDate;

import com.zw.app.domain.expireDate.AppExpireDateEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/***
 @Author zhengjc
 @Date 2019/11/25 9:24
 @Description 平台各种到期提醒
 @version 1.0
 **/
public interface AppExpireDateMysqlDao {
    /**
     * 0查找保险到期数据
     * @return
     */
    List<AppExpireDateEntity> findInsuranceExpireList(@Param("vehicleIds") Set<String> vehicleIds);

    /**
     * 1查找服务到期数据
     */
    List<AppExpireDateEntity> findLifecycleExpireList(@Param("vehicleIds") Set<String> vehicleIds);

    /**
     * 2查找保养到期数据
     */
    List<AppExpireDateEntity> findMaintenanceExpireList(@Param("vehicleIds") Set<String> vehicleIds);

    /**
     * 3查找行驶证到期数据
     */
    List<AppExpireDateEntity> findDrivingLicenseExpireList(@Param("vehicleIds") Set<String> vehicleIds);

    /**
     * 4查找道路运输证到期数据
     */
    List<AppExpireDateEntity> findRoadTransportExpireList(@Param("vehicleIds") Set<String> vehicleIds);

}
