package com.zw.platform.basic.repository;

import com.zw.platform.basic.dto.AdministrativeDivisionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdministrativeDivisionDao {
    /**
     * 根据行政区划代码获取行政区划信息
     * @param divisionCode 行政区划代码
     * @return 行政区划信息
     */
    AdministrativeDivisionDTO getByDivisionCode(@Param("divisionCode") String divisionCode);

    /**
     * 通过行政区划名称获得区划代码
     * @param province 省名称
     * @param city     市区名称
     * @param county   区县名称
     * @return 行政区划信息
     */
    AdministrativeDivisionDTO getByName(@Param("province") String province, @Param("city") String city,
        @Param("county") String county);

    /**
     * 获取所有的省市区
     * @return 所有的行政区划
     */
    List<AdministrativeDivisionDTO> getAll();

}
