package com.zw.platform.service.functionconfig.impl;

import com.zw.platform.domain.functionconfig.Circle;
import com.zw.platform.repository.modules.CircleDao;
import com.zw.platform.service.functionconfig.CircleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Tdz on 2016/8/9.
 */
@Service
public class CircleServiceImpl implements CircleService {
    @Autowired
    private CircleDao circleDao;

    @Override
    public Circle getCircleByID(String id) {
        return circleDao.getCircleById(id);
    }
}
