package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.vas.f3.Peripheral;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Title:外设Dao
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月08日 17:51
 */
public interface PeripheralDao {

    Page<Peripheral> findByPage(final AssignmentQuery query);

    Peripheral get(@Param("id")final String id);

    /**
     * 根据外设ID查询外设信息
     * @param identId
     * @return
     */
    List<Peripheral> getByIdentId(@Param("identId")String identId);

    /**
     * 根据外设identName查询外设信息
     * @param identName
     * @return
     */
    List<Peripheral> getByIdentName(@Param("identName")String identName);

    /**
     * 通过ID获取绑定的车辆数
     * @param id
     * @return
     */
    Integer getConfigCountById(@Param("id")final String id);


    /**
     * 查询所有可用
     * @return
     */
    List<Peripheral> findAllow();


    /**
     * 根据ID删除外设信息
     * @param id
     */
    boolean delete(@Param("id")final String id);

    /**
     * 批量删除
     * @param ids
     * @return
     */
    boolean unbindFenceByBatch(@Param("ids") List<String> ids);

    /**
     * 新增Peripheral
     * @param peripheral
     */
    boolean add(final Peripheral peripheral);

    /**
     * 新增Peripheral
     * @param peripheral
     */
    boolean addBatch(final List<Peripheral> peripheral);


    /**
     * 修改Peripheral
     * @param peripheral
     */
    boolean update(final Peripheral peripheral);
}
