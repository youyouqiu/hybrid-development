package com.zw.platform.service.basicinfo.impl;

import com.zw.platform.basic.domain.LifecycleDO;
import com.zw.platform.basic.repository.NewLifecycleDao;
import com.zw.platform.domain.basicinfo.form.LifecycleInfoForm;
import com.zw.platform.service.basicinfo.LifecycleService;
import com.zw.platform.service.statistic.LifecycleExpireStatisticService;
import com.zw.platform.util.LocalDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Tdz on 2016/8/1.
 */
@Service("oldLifecycleService")
public class LifecycleServiceImpl implements LifecycleService {
    @Autowired
    private NewLifecycleDao newLifecycleDao;

    @Autowired
    private LifecycleExpireStatisticService lifecycleExpireStatisticService;

    @Override
    public void add(LifecycleInfoForm form) {
        newLifecycleDao.insert(new LifecycleDO(form.getBillingDate(), form.getExpireDate()));
    }

    @Override
    public void addList(Collection<LifecycleInfoForm> list) {
        if (list.isEmpty()) {
            return;
        }
        List<LifecycleDO> lifecycleDOList =
            list.stream().map(obj -> new LifecycleDO(obj.getBillingDate(), obj.getExpireDate()))
                .collect(Collectors.toList());
        newLifecycleDao.addByBatch(lifecycleDOList);
    }

    @Override
    public List<String> findLifecycleExpireRemindList() throws Exception {
        // 平台设置的超时时间
        int expireRemindDays = lifecycleExpireStatisticService.getExpireRemindDays();
        // 当前时间
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, expireRemindDays);
        String expireRemindDate = LocalDateUtils.dateFormate(calendar.getTime());
        String currentDateStr = LocalDateUtils.dateFormate(currentDate);
        return newLifecycleDao.findLifecycleExpireRemindList(currentDateStr, expireRemindDate);
    }

    @Override
    public List<String> findLifecycleAlreadyExpireRemindList() {
        Date currentDate = new Date();
        String currentDateStr = LocalDateUtils.dateFormate(currentDate);
        return newLifecycleDao.findLifecycleAlreadyExpireRemindList(currentDateStr);
    }
}
