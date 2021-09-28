package com.zw.platform.repository.modules;

import com.zw.platform.domain.reportManagement.form.VehBasicDO;
import com.zw.platform.domain.reportManagement.form.VehStateDO;
import com.zw.platform.domain.reportManagement.query.VehStateQuery;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zjc
 * @Description:车辆状态报表dao层
 * @Date: create in 2020/11/13 10:46
 */
public interface VehStateDao {

    /**
     * 查询单个企业车辆状态信息
     * @param query
     * @return
     */
    List<VehStateDO> getSingleOrgVehStateDO(@Param("query") VehStateQuery query);

    /**
     * 查询多个个企业车辆状态信息
     * @param query
     * @return
     */
    List<VehStateDO> getManyOrgVehStateDO(@Param("query") VehStateQuery query);

    /**
     * 查询单个企业下的车辆id(去重)
     * @param query
     * @return
     */
    Set<String> getOrgVehicleIds(@Param("query") VehStateQuery query);

    /**
     * 通过企业获取绑定的车辆信息
     * @param orgIds
     * @return
     */
    @MapKey("vehicleId")
    Map<String, VehBasicDO> getBindVehicleMap(@Param("orgIds") List<String> orgIds);

    /**
     * 获取某一天的企业车辆状态记录
     * @param orgId
     * @param startDaySecond
     * @return
     */

    List<VehStateDO> getDayOrgVehStateDo(@Param("orgId") String orgId, @Param("startDaySecond") long startDaySecond);

    /**
     * 获取要删除的数据（4个月以前）
     * @param timeSecond
     * @return
     */
    List<VehStateDO> getDayBeforeData(long timeSecond);

    /**
     * 删除
     * @param vehStateDOList
     */
    void deleteExpireData(@Param("vehStateDOList") List<VehStateDO> vehStateDOList);
}
