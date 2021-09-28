package com.zw.platform.repository.modules;

import com.zw.adas.domain.platforminspection.PlatformInspectionDTO;
import com.zw.adas.domain.platforminspection.PlatformInspectionQuery;
import com.zw.platform.domain.platformInspection.PlatformInspectionDO;
import com.zw.platform.dto.platformInspection.PlatformInspectionParamDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 平台巡检报表
 */
public interface PlatformInspectionDao {

    /**
     * 分页接口
     * @param query
     * @return
     */
    List<PlatformInspectionDTO> getListByKeyword(@Param("query") PlatformInspectionQuery query);

    /**
     * 新增
     * @param platformInspectionDo
     * @return boolean
     */
    boolean insert(PlatformInspectionDO platformInspectionDo);

    /**
     * 刪除
     * @param id id
     * @return boolean
     */
    boolean delete(String id);

    /**
     * 更新
     * @param platformInspectionDO
     * @return boolean
     */
    boolean update(PlatformInspectionDO platformInspectionDO);

    /**
     * 查询 根据主键 id 查询
     * @param id id
     * @return AdasPlatformInspectionDo
     */
    PlatformInspectionDO getById(String id);


    List<String> get0706Inspection(@Param("time") Date time, @Param("vehicleId") String vehicleId);

    void setInspectionResult(@Param("inspectionResultId") String id, @Param("ids") List<String> ids);


    void updateInspectionStatus(@Param("status")Integer status, @Param("id") String id);

    Set<String> getOverTimeInspection(
        @Param("platformInspectionParams")List<PlatformInspectionParamDTO> platformInspectionParams);

    void batchUpdateInspectionStatus(@Param("status")Integer status,
        @Param("platformInspectionIds") Collection<String> platformInspectionIds);

}
