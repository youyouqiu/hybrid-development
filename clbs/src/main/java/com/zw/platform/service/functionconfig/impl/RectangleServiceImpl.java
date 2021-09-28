package com.zw.platform.service.functionconfig.impl;

import com.zw.platform.domain.functionconfig.Rectangle;
import com.zw.platform.repository.modules.RectangleDao;
import com.zw.platform.service.functionconfig.RectangleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Tdz on 2016/8/9.
 */
@Service
public class RectangleServiceImpl implements RectangleService {
    @Autowired
    private RectangleDao rectangleDao;
    @Override
    public Rectangle getRectangleByID(String id) {
        return rectangleDao.getRectangleByID(id);
    }
}
