package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.FuelTypeDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 燃料类型 zw_m_fuel_type
 * @author zhangjuan
 */
public interface FuelTypeDao {
    /**
     * 获取全部的燃料类型
     * @return 燃料类型列表
     */
    List<FuelTypeDO> getAll();

    /**
     * 根据ID获取燃料类型
     * @param id id
     * @return 燃料类型
     */
    FuelTypeDO getById(@Param("id") String id);

    /**
     * 根据关键字获取燃料类型
     * @param keyword keyword 为空返回全部
     * @return 燃料类型列表
     */
    List<FuelTypeDO> getByKeyword(@Param("keyword") String keyword);
}
