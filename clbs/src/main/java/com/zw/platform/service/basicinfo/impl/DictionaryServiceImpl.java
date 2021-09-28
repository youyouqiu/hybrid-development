package com.zw.platform.service.basicinfo.impl;

import com.zw.platform.basic.domain.DictionaryDO;
import com.zw.platform.basic.repository.NewDictionaryDao;
import com.zw.platform.service.basicinfo.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/9/28
 **/
@Service("oldDictionaryService")
public class DictionaryServiceImpl implements DictionaryService {

    private static final String BUSINESS_SCOPE = "BUSINESS_SCOPE";

    private static final String BUSINESS_LICENSE_TYPE = "BUSINESS_LICENSE_TYPE";

    @Autowired
    private NewDictionaryDao newDictionaryDao;

    @Override
    public List<DictionaryDO> getBusinessScope() {
        return newDictionaryDao.findByType(BUSINESS_SCOPE).stream()
                .sorted(Comparator.comparingInt(dic -> Integer.parseInt(dic.getCode()))).collect(Collectors.toList());
    }

    @Override
    public List<DictionaryDO> getBusinessLicenseType() {
        return newDictionaryDao.findByType(BUSINESS_LICENSE_TYPE);
    }
}
