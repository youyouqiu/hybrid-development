package com.zw.platform.basic.service.impl;

import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.domain.LifecycleDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewLifecycleDao;
import com.zw.platform.basic.service.LifecycleService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.common.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;

/**
 * 服务周期实现类
 * @author zhangjuan
 */
@Service
public class LifecycleServiceImpl implements LifecycleService {
    @Autowired
    private NewLifecycleDao lifecycleDao;

    @Override
    public String add(String billingDateStr, String expireDateStr) {

        Date billingDate = DateUtil.getStringToDate(billingDateStr, DateFormatKey.YYYY_MM_DD);
        Date expireDate = DateUtil.getStringToDate(expireDateStr, DateFormatKey.YYYY_MM_DD);
        LifecycleDO lifecycleDO = new LifecycleDO(billingDate, expireDate);
        lifecycleDao.insert(lifecycleDO);
        return lifecycleDO.getId();
    }

    @Override
    public String update(BindDTO curBind, BindDTO oldBind) {
        //当前监控对象是否有绑定服务周期
        String billingDateStr = curBind.getBillingDate();
        String expireDateStr = curBind.getExpireDate();
        String lifecycleId = oldBind.getServiceLifecycleId();
        boolean curHasLifecycle = StringUtils.isNotBlank(billingDateStr) && StringUtils.isNotBlank(expireDateStr);
        boolean oldHasLifecycle = StringUtils.isNotBlank(lifecycleId);

        //当前和之前都没有服务周期，不做任何改动
        if (!oldHasLifecycle && !curHasLifecycle) {
            return "";
        }
        //原来没有，当前存在服务周期，新增服务周期
        if (!oldHasLifecycle) {
            return add(billingDateStr, expireDateStr);
        }

        //原来存在，当前不存在服务周期
        if (!curHasLifecycle) {
            lifecycleDao.delete(lifecycleId);
            return "";
        }

        //原来和当前都存在，进行修改
        Date billingDate = DateUtil.getStringToDate(billingDateStr, DateFormatKey.YYYY_MM_DD);
        Date expireDate = DateUtil.getStringToDate(expireDateStr, DateFormatKey.YYYY_MM_DD);
        LifecycleDO lifecycleDO = new LifecycleDO(billingDate, expireDate);
        lifecycleDO.setId(lifecycleId);
        lifecycleDO.setUpdateDataTime(new Date());
        lifecycleDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        lifecycleDao.update(lifecycleDO);
        return lifecycleId;
    }

    @Override
    public void delete(Collection<String> lifecycleIds) {
        lifecycleDao.deleteBatch(lifecycleIds);
    }

    @Override
    public boolean addByBatch(Collection<LifecycleDO> lifecycleList) {
        return lifecycleDao.addByBatch(lifecycleList);
    }
}
