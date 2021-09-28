package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.VehiclePurposeDO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.domain.basicinfo.VehiclePurpose;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * zw_m_vehicle_purpose -- 车辆运营类别表的操作
 * @author zhangjuan
 * @date 2020/10/14
 */
public interface VehiclePurposeDao {
    /**
     * 根据车辆运营类别名称获取车辆运营类别
     * @param name 车辆运营类别名称
     * @return 车辆运营类别详情
     */
    VehiclePurposeDO getByName(@Param("name") String name);

    /**
     * 添加运营类别
     * @param vehiclePurposeDO vehiclePurposeDO
     * @return 是否添加成功
     */
    boolean insert(VehiclePurposeDO vehiclePurposeDO);

    /**
     * 更新车辆运营类比
     * @param vehiclePurposeDO vehiclePurposeDO
     * @return 是否更新成功
     */
    boolean update(VehiclePurposeDO vehiclePurposeDO);

    /**
     * 根据ID获取运营类别
     * @param id id
     * @return 运营类别信息
     */
    VehiclePurposeDO getById(@Param("id") String id);

    /**
     * 批量获取车辆运营类别
     * @param ids 车辆运营类别Id
     * @return 车辆运营类别列表
     */
    List<VehiclePurposeDO> getByIds(@Param("ids") Collection<String> ids);

    /**
     * 批量删除运营类别
     * @param ids ids
     * @return 删除数量
     */
    int delBatch(@Param("ids") Collection<String> ids);

    /**
     * 删除运营类别
     * @param id id
     * @return boolean
     */
    boolean delete(String id);

    /**
     * 根据关键字查询运营类别
     * @param keyword 关键字
     * @return 车辆运营类别列表
     */
    List<VehiclePurposeDTO> getByKeyword(@Param("keyword") String keyword);

    /**
     * 批量插入车辆运营列表
     * @param vehiclePurposeList 车辆运营类别列表
     * @return 插入数据条数
     */
    int addBatch(@Param("vehiclePurposeList") List<VehiclePurposeDO> vehiclePurposeList);

    /**
     * 查询所有车辆用途
     * @return list
     */
    List<VehiclePurpose> findVehicleCategory();
}
