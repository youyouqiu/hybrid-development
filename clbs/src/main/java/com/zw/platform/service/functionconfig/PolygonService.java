package com.zw.platform.service.functionconfig;

import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.util.common.BusinessException;

import java.util.List;

/**
 * Created by Tdz on 2016/8/9.
 */
public interface PolygonService {
    List<Polygon> getPolygonByID(final String id);
    
    /**
    * 根据多边形id查询多边形主表信息
    * @Title: findPolygonById
    * @param id
    * @throws BusinessException
    * @return Polygon
    * @author Liubangquan
     */
    Polygon findPolygonById(final String id);
}
