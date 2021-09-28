package com.zw.platform.service.functionconfig;

import com.zw.platform.domain.functionconfig.Administration;

import java.util.List;

public interface AdministrationService {
    List<List<List<String>>> getAdministrationByID(final String id);

    /**
     * 根据多边形id查询多边形主表信息
     * @author yangyi
     */
    Administration findAdministrationById(final String id);

    List<Administration> findAdministrationByName(final String name);
}
