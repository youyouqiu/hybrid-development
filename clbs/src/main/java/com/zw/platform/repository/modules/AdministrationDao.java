package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.Administration;
import com.zw.platform.domain.functionconfig.form.AdministrationForm;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;

import java.util.List;

public interface AdministrationDao {
    /**
     * 新增行政区域坐标点
     */
    boolean administrationContent(List<AdministrationForm> list);

    /**
     * 新增行政区域
     */
    boolean administration(final AdministrationForm form);

    /**
     * 新增电子围栏
     */
    boolean fenceInfo(final ManageFenceFrom fenceForm);

    /**
     * 通过行政区域ID查询多边形点
     */
    List<Administration> getAdministrationByID(String id);

    /**
     * 通过行政区域name查询多边形点
     */
    List<Administration> findAdministrationByName(String id);

    /**
     * 通过行政区域ID查询行政区域详细信息
     */
    Administration findAdministrationByIds(String id);

    /**
     * 删除行政区域点
     */
    boolean deleteAdministrationContent(String id);

    /**
     * 更新行政围栏信息表
     * @param form form
     * @return 是否更新成功
     */
    boolean updateAdministration(AdministrationForm form);

    /**
     * 删除行政区划围栏数据
     * @param fenceId fenceId
     * @return 是否删除成功
     */
    boolean deleteAdministration(String fenceId);

    List<Administration> getAdministrationById(String id);
}
