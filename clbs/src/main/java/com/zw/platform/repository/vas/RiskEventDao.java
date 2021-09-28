package com.zw.platform.repository.vas;

import java.util.List;
import java.util.Map;

import com.zw.platform.domain.riskManagement.RiskEvent;
import com.zw.platform.domain.riskManagement.form.RiskEventForm;
import com.zw.platform.domain.riskManagement.form.RiskEventImportForm;
import com.zw.platform.domain.riskManagement.query.RiskEventQuery;


/**
 * 风险事件 Created by zjc on 2017/8/16.
 */
public interface RiskEventDao {
    RiskEvent get(final String id);

    /**
     * 查询
     */
    List<RiskEvent> find(final RiskEventQuery query);

    /**
     * 根据风险事件名称进行查询
     *
     * @param query
     * @return
     */
    RiskEvent findByRiskEvent(final String query);

    /**
     * 查询
     */
    List<RiskEvent> findAllow();

    public RiskEvent findById(String id);

    /**
     * 修改
     */
    int update(final RiskEventForm form);

    /**
     * 新增
     */
    void add(final RiskEventForm form);

    /**
     * 根据id删除一个
     */
    int delete(final String id);

    RiskEvent findByRiskEvent(String riskType, String riskEvent);

    RiskEvent isExist(String riskType, String riskEvent);

    int isRepeate(RiskEventForm form);

    boolean addByBatch(List<RiskEventImportForm> importList);

    List<Map<String, String>> getDocRiskEventMap();

    List<Map<String, String>> getRiskEventMap();

    List<Map<String, String>> getRiskTypeMap();

    String getWarnTypeBy808pos(String alarmType);

    /**
     * 通过functionId  获取风险事件和风险类型
     * @param functionId 风险事件Id
     * @return riskEvent
     */
    RiskEvent getRiskEventByFunctionId(Integer functionId);

    List<Map<String, String>> findAllEventTypeMap();

    /**
     *获取commonName 和对应的functionId
     * @return Map
     */
    List<RiskEvent> getNameAndFunctionIds();

    List<RiskEvent> findAllEventAndEventCommonFiled();
}
