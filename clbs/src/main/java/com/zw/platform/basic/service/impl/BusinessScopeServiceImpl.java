package com.zw.platform.basic.service.impl;

import com.zw.platform.basic.domain.BusinessScopeDO;
import com.zw.platform.basic.dto.BusinessScopeDTO;
import com.zw.platform.basic.repository.BusinessScopeDao;
import com.zw.platform.basic.service.BusinessScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author wanxing
 * @Title: 商业经营范围实现
 * @date 2020/11/516:47
 */
@Service
public class BusinessScopeServiceImpl implements BusinessScopeService {


    @Autowired
    private BusinessScopeDao businessScopeDao;

    @Override
    public boolean addBusinessScope(Collection<BusinessScopeDO> businessScopes) {
        return businessScopeDao.addBusinessScope(businessScopes);
    }

    @Override
    public boolean bindBusinessScope(String id, List<String> scopeIds, Integer type) {
        return businessScopeDao.bindBusinessScope(id, scopeIds, type);
    }

    @Override
    public boolean deleteById(String id) {
        return businessScopeDao.deleteById(id);
    }

    @Override
    public boolean deleteByIds(Collection<String> ids) {
        return businessScopeDao.deleteByIds(ids);
    }

    @Override
    public List<BusinessScopeDTO> getBusinessScope(String id) {
        return businessScopeDao.getBusinessScope(id);
    }

    @Override
    public List<BusinessScopeDTO> getBusinessScopeByIds(Collection<String> ids) {
        return businessScopeDao.getBusinessScopeByIds(ids);
    }

}
