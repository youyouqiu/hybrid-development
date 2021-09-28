package com.zw.api.service.impl;

import com.zw.api.repository.mysql.SwaggerGroupDao;
import com.zw.api.service.SwaggerGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SwaggerGroupServiceImpl implements SwaggerGroupService {
    @Autowired
    private SwaggerGroupDao swaggerGroupDao;

    @Override
    public List<String> getGroupIdsByUserId(String userId) {
        return swaggerGroupDao.getGroupIdsByUserId(userId);
    }
}
