package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.mileageSensor.TyreSize;
import com.zw.platform.domain.vas.mileageSensor.TyreSizeQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Title:轮胎规格Dao
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:26
 */
public interface TyreSizeDao {

    /**
     * 新增TyreSize
     * @param tyreSize
     */
    boolean addTyreSize(TyreSize tyreSize);
    /**
     * 批量新增TyreSize
     * @param tyreSizes
     */
    boolean addBatchTyreSize(List<TyreSize> tyreSizes);

    /**
     * 修改 TyreSize
     * @param tyreSizes
     */
    boolean updateTyreSize(TyreSize tyreSizes);


    /**
     * 批量删除TyreSize
     * @param tyreSizesids
     */
    void deleteBatchTyreSize(@Param("tyreSizesids")List<String> tyreSizesids);

    /**
     * 根据ID查询TyreSize
     * @param id
     * @return
     */
    TyreSize findById(@Param("id")String  id);

    /**
     * 根据类型及名称查询轮胎规格
     * @param tireType 类型
     * @param sizeName 规格
     * @return
     */
    TyreSize findByTypeAndName(@Param("tireType")String  tireType,@Param("sizeName")String sizeName);


    /**
     * 根据查询条件查询信息
     * @param query
     * @return
     */
    Page<TyreSize> findByQuery(TyreSizeQuery query);
    /**
     * 根据所有可用
     * @return
     */
    List<TyreSize> findAll();

    /**
     * 检查轮胎数据是否被绑定并返回有绑定的轮胎规格
     * @param id
     * @return
     */
    String checkConfig(@Param("id")String id);
}
