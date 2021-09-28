package com.zw.platform.service.personalized.impl;

import com.zw.platform.repository.modules.PersonalizedDao;
import com.zw.platform.service.personalized.IcoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Deprecated
@Service
public class IcoServiceImpl implements IcoService {
    private static Logger log = LogManager.getLogger(IcoServiceImpl.class);

    @Autowired
    private PersonalizedDao personalizedDao;


    @Override
    public List<String> getVidsBySubTypeId(String subTypeId) {
        return personalizedDao.getVidsBySubTypeId(subTypeId);
    }

    @Override
    public List<String> getVidsByCategoryId(String categoryId) {
        return personalizedDao.getVidsByCategoryId(categoryId);
    }

}
