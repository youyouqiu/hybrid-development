package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.form.LifecycleInfoForm;
import com.zw.platform.domain.statistic.LifecycleExpireStatisticQuery;
import com.zw.platform.domain.statistic.info.LifecycleExpireStatisticInfo;
import com.zw.platform.util.common.BusinessException;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * Created by Tdz on 2016/8/1.
 */
@Deprecated
public interface LifecycleDao {
    /**
     * 新增
     */
    void add(final LifecycleInfoForm form);

    /**
     * 批量新增
     */
    boolean addList(@Param("list") final Collection<LifecycleInfoForm> list);

    /**
     * 修改服务周期
     * @param form
     * @return void
     * @throws BusinessException
     * @Title: update
     * @author Liubangquan
     */
    void update(final LifecycleInfoForm form) throws BusinessException;

    /**
     * 删除服务周期
     * @param id
     * @return void
     * @throws BusinessException
     * @Title: delete
     * @author Liubangquan
     */
    void deleteById(String id) throws BusinessException;

    /**
     * 查找监控对象服务周期
     * @param query query
     * @return list
     */
    Page<LifecycleExpireStatisticInfo> findLifecycleExpireBy(LifecycleExpireStatisticQuery query);

    /**
     * @param currentDateStr   currentDateStr
     * @param expireRemindDate currentDateStr + 30
     * @return list
     */
    List<String> findLifecycleExpireRemindList(@Param("currentDateStr") String currentDateStr,
        @Param("expireRemindDate") String expireRemindDate);

    /**
     * 查询服务已经到期的数据
     * @param currentDateStr
     * @return
     */
    List<String> findLifecycleAlreadyExpireRemindList(@Param("currentDateStr") String currentDateStr);
}
