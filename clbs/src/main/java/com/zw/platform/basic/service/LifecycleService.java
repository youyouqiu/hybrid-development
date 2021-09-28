package com.zw.platform.basic.service;

import com.zw.platform.basic.domain.LifecycleDO;
import com.zw.platform.basic.dto.BindDTO;

import java.util.Collection;

/**
 * 计费周期服务
 */
public interface LifecycleService {

    /**
     * 添加计费周期服务
     *
     * @param billingDate 计费日期
     * @param expireDate  到期日期
     * @return 服务周期ID
     */
    String add(String billingDate, String expireDate);

    /**
     * 修改服务周期
     *
     * @param curBindDTO 当前绑定信息
     * @param oldBindDTO 历史的绑定信息
     * @return 服务周期ID
     */
    String update(BindDTO curBindDTO, BindDTO oldBindDTO);


    /**
     * 批量删除服务周期
     *
     * @param lifecycleIds 服务周期ID
     */
    void delete(Collection<String> lifecycleIds);


    /**
     * 批量添加服务周期
     *
     * @param lifecycleList 服务周期列表
     * @return 操作是否成功
     */
    boolean addByBatch(Collection<LifecycleDO> lifecycleList);


}
