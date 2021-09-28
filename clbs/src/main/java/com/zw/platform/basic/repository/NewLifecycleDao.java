package com.zw.platform.basic.repository;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.LifecycleDO;
import com.zw.platform.domain.statistic.LifecycleExpireStatisticQuery;
import com.zw.platform.domain.statistic.info.LifecycleExpireStatisticInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * zw_m_service_lifecycle
 * @author zhangjuan
 */
public interface NewLifecycleDao {
    /**
     * 插入
     * @param lifecycleDO lifecycleDO
     * @return 是否插入成功
     */
    boolean insert(LifecycleDO lifecycleDO);

    /**
     * 删除
     * @param id id
     * @return 是否删除成功
     */
    boolean delete(@Param("id") String id);

    /**
     * 修改服务周期
     * @param lifecycleDO lifecycleDO
     * @return 是否更新成功
     */
    boolean update(LifecycleDO lifecycleDO);

    /**
     * 批量删除
     * @param ids ids
     * @return 是否删除成功
     */
    boolean deleteBatch(@Param("ids") Collection<String> ids);

    /**
     * 批量添加服务周期
     * @param lifecycleList 服务周期列表
     * @return 操作是否成功
     */
    boolean addByBatch(@Param("list") Collection<LifecycleDO> lifecycleList);

    /**
     * 查询到期提醒列表
     * @param currentDateStr   currentDateStr
     * @param expireRemindDate currentDateStr + 30
     * @return list
     */
    List<String> findLifecycleExpireRemindList(@Param("currentDateStr") String currentDateStr,
        @Param("expireRemindDate") String expireRemindDate);

    /**
     * 查询服务已经到期的数据
     * @param currentDateStr currentDateStr
     * @return list
     */
    List<String> findLifecycleAlreadyExpireRemindList(@Param("currentDateStr") String currentDateStr);

    /**
     * 查找监控对象服务周期
     * @param query query
     * @return list
     */
    Page<LifecycleExpireStatisticInfo> findLifecycleExpireBy(LifecycleExpireStatisticQuery query);
}
