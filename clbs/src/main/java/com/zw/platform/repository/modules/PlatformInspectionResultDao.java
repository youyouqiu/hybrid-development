package com.zw.platform.repository.modules;

import com.zw.platform.domain.platformInspection.PlatformInspectionResultDO;
import org.apache.ibatis.annotations.Param;

/**
 * 平台巡检报表
 */
public interface PlatformInspectionResultDao {

    /**
     * 新增
     * @param platformInspectionResultDO
     * @return boolean
     */
    boolean insert(PlatformInspectionResultDO platformInspectionResultDO);

    /**
     * 刪除
     * @param id id
     * @return boolean
     */
    boolean delete(String id);

    /**
     * 更新
     * @param platformInspectionResultDO
     * @return boolean
     */
    boolean update(PlatformInspectionResultDO platformInspectionResultDO);

    /**
     * 查询 根据主键 id 查询
     * @param id id
     * @return PlatformInspectionResultDo
     */
    PlatformInspectionResultDO getById(String id);


    void setImageUrl(@Param("id")String id, @Param("imageUrl")String imageUrl);

    void setVideoUrl(@Param("id")String id, @Param("videoUrl")String videoUrl);

}
