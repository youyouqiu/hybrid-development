package com.zw.adas.service.defineSetting;

import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.AdasRiskEvent;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskEventQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by zjc on 2017/8/16.
 */
public interface AdasRiskEventService {
    AdasRiskEvent get(final String id) throws Exception;

    /**
     * 分页查询
     */
    Page<AdasRiskEvent> findByPage(final AdasRiskEventQuery query) throws Exception;

    List<AdasRiskEvent> find(final AdasRiskEventQuery query);

    AdasRiskEvent findByRiskEvent(String riskEvent) throws Exception;

    int update(final AdasRiskEventForm form) throws Exception;

    public AdasRiskEvent findById(String id) throws Exception;

    int delete(final String id) throws Exception;

    /**
     * 导出
     */
    boolean exportInfo(String title, int type, HttpServletResponse response) throws Exception;

    AdasRiskEvent findByRiskEvent(String riskType, String riskEvent);

    boolean isRepeate(AdasRiskEventForm form);

    /**
     * 新增
     */
    void add(final AdasRiskEventForm form) throws Exception;

    boolean generateTemplate(HttpServletResponse response) throws Exception;


}
