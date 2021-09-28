package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.riskManagement.query.AdasRiskEventQuery;
import com.zw.adas.domain.riskManagement.AdasRiskEvent;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventImportForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 风险事件 Created by zjc on 2017/8/16.
 */
public interface AdasRiskEventDao {
    AdasRiskEvent get(final String id);

    /**
     * 查询
     */
    List<AdasRiskEvent> find(final AdasRiskEventQuery query);

    /**
     * 根据风险事件名称进行查询
     *
     * @param query
     * @return
     */
    AdasRiskEvent findByRiskEvent(final String query);

    /**
     * 查询
     */
    List<AdasRiskEvent> findAll(@Param("flag") String flag);

    public AdasRiskEvent findById(String id);

    /**
     * 修改
     */
    int update(final AdasRiskEventForm form);

    /**
     * 新增
     */
    void add(final AdasRiskEventForm form);

    /**
     * 根据id删除一个
     */
    int delete(final String id);

    AdasRiskEvent findByRiskEvent(String riskType, String riskEvent);

    AdasRiskEvent isExist(String riskType, String riskEvent);

    int isRepeate(AdasRiskEventForm form);

    boolean addByBatch(List<AdasRiskEventImportForm> importList);

    List<Map<String, String>> getDocRiskEventMap();

    List<Map<String, String>> getRiskEventMap();

    List<Map<String, String>> getRiskTypeMap();

    List<Map<String, Object>> findAllEvent();

    /**
     * 根据functionId 获取riskEvent
     *
     * @param functionId 报警编号
     * @return 报警名称
     */
    String getRiskEvent(String functionId);
}
