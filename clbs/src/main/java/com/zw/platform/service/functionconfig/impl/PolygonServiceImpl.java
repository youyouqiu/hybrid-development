package com.zw.platform.service.functionconfig.impl;

import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.repository.modules.PolygonDao;
import com.zw.platform.service.functionconfig.PolygonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Tdz on 2016/8/9.
 */
@Service
public class PolygonServiceImpl implements PolygonService {
    @Autowired
    private PolygonDao polygonDao;

    @Override
    public List<Polygon> getPolygonByID(String id) {
        return polygonDao.getPolygonById(id);
    }

    @Override
    public Polygon findPolygonById(String id) {
        return polygonDao.findPolygonById(id);
    }
}
