package com.zw.platform.service.basicinfo;

import com.zw.platform.domain.basicinfo.form.LifecycleInfoForm;

import java.util.Collection;
import java.util.List;

/**
 * Created by Tdz on 2016/8/1.
 */
public interface LifecycleService {
    /**
     * 新增
     */
    void add(final LifecycleInfoForm form);

    void addList(Collection<LifecycleInfoForm> list);

    List<String> findLifecycleExpireRemindList() throws Exception;

    /**
     * 查询服务已经到期车辆
     * @return
     */
    List<String> findLifecycleAlreadyExpireRemindList();
}
