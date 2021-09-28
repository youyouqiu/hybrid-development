package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.AdministrativeDivisionsInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/20 16:10
 */
@Deprecated
public interface AdministrativeDivisionsDao {
    /**
     * 通过省、直辖市名称获得区划代码
     * @param provinceName
     * @param type
     * @return
     */
    String getDivisionsCodeByProvinceName(@Param("provinceName") String provinceName, @Param("type") Integer type);

    /**
     * 通过市、区名称获得区划代码
     * @param cityName
     * @param type
     * @return
     */
    List<Map> getDivisionsCodeByCityName(@Param("cityName") String cityName, @Param("type") Integer type);

    /**
     * 通过县名称获得区划代码
     * @param countyName
     * @param type
     * @return
     */
    List<Map> getDivisionsCodeByCountyName(@Param("countyName") String countyName, @Param("type") Integer type);

    /**
     * 根据行政区划代码查询行政区划信息
     * @param divisionsCode
     * @return
     */
    AdministrativeDivisionsInfo getInfoByDivisionsCode(String divisionsCode);

    List<AdministrativeDivisionsInfo> getAll();
}
