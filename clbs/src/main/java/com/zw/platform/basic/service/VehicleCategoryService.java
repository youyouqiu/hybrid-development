package com.zw.platform.basic.service;

import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.util.common.BusinessException;

import java.util.Collection;
import java.util.List;

/**
 * 车辆类别服务接口
 * @author zhangjuan
 */
public interface VehicleCategoryService {
    /**
     * 添加车辆类别
     * @param categoryDTO 车辆类别实体
     * @return 是否添加成功
     * @throws BusinessException 异常
     */
    boolean add(VehicleCategoryDTO categoryDTO) throws BusinessException;

    /**
     * 判断车辆类别是否已经存在
     * @param id   类别ID 可为空
     * @param name 类别名称
     * @return true 已经存在 false 不存在
     */
    boolean isExistCategory(String id, String name);

    /**
     * 更新车辆类别
     * @param categoryDTO categoryDTO
     * @return 是否更新成功
     * @throws BusinessException 异常
     */
    boolean update(VehicleCategoryDTO categoryDTO) throws BusinessException;

    /**
     * 删除车辆类别
     * @param id id
     * @return 是否删除成功
     * @throws BusinessException 异常
     */
    boolean delete(String id) throws BusinessException;

    /**
     * 批量删除
     * @param ids ids
     * @return 删除的车辆类别名称
     * @throws BusinessException 异常
     */
    String deleteBatch(Collection<String> ids);

    /**
     * 根据ID获取详情
     * @param id id
     * @return 详情
     */
    VehicleCategoryDTO getById(String id);

    /**
     * 根据关键字查询车辆类别
     * @param keyword keyword
     * @return 车辆类别列表
     */
    List<VehicleCategoryDTO> getAllByKeyword(String keyword);

    /**
     * 类别中是否有绑定车辆类型
     * @param ids ids
     * @return true 有绑定 false 无绑定
     */
    boolean isBindType(Collection<String> ids);

    /**
     * 根据类别名称获取
     * @param categoryName 类别名称
     * @return 类别
     */
    VehicleCategoryDTO getByName(String categoryName);

}
