package com.zw.talkback.repository.mysql;

import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 原始机型
 */
public interface OriginalModelDao {

    /**
     * 查询原始机型信息
     * @return list
     */
    List<OriginalModelInfo> findOriginalModelAndIntercomModel();

    /**
     * 新增原始机型信息
     */
    void addOriginalModel(@Param("originalModelInfos") List<OriginalModelInfo> originalModelInfos);

    /**
     * 根据原始机型名称或则原始机型ID查询原始机型信息
     * @param originalModelId originalModelId
     * @return
     */
    OriginalModelInfo getOriginalModelByModelId(@Param("originalModelId") Long originalModelId);

    /**
     * 根据index查询原始机型
     */
    List<OriginalModelInfo> getOriginalModelByIndex(@Param("index") String index);

    /**
     * 获取所有的原始机型
     * @return
     */
    List<Map<Long, String>> getAllOriginalModel(@Param("index") String index);

    List<OriginalModelInfo> findAllOriginalModelInfo();

    List<OriginalModelInfo> getOriginalModelList();
}
