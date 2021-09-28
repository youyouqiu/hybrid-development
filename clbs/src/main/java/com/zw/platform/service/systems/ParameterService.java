package com.zw.platform.service.systems;

import com.github.pagehelper.Page;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.Parameter;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.systems.query.ParameterQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ParameterService {
    /**
     * 根据车辆编号和消息编号修改下发状态，不调整返回状态 保持 未返回
     */
    boolean updateStatusByMsgSNVid(Integer msgSN, String vehiceleId, Integer status);

    /**
     * 根据车辆编号和消息编号修改下发状态，调整返回状态 为 已返回
     */
    boolean updateStatusByMsgSN(Integer msgSN, String vehiceleId, Integer status);

    Page<Parameter> findByPage(ParameterQuery query) throws Exception;

    List<Directive> findById(List<String> ids);

    boolean updateFenceConfig(String vehiceleId, int msgSN);

    /**
     * 根据绑定表id删除下发数据
     */
    void deleteByConfigId(String configId);

    /**
     * 根据不同下发类型及绑定的id删除
     */
    void deleteByVechicleidParameterName(String vechicleid, String parameterName, String type);

    void updateMsgSNAndNameById(List<String> ids, int msgSN, Integer status, String parameterName, Integer replyCode);

    void addDirective(DirectiveForm form);

    List<Directive> findParameterByType(String vehicleId, String parameterName, String parameterType);

    String updateParameterStatus(DirectiveForm form);

    void addDirective(String vehicleId, int status, int msgSN, String parameterName, int replyCode,
        String parameterType, String directiveName);

    Directive selectDirectiveByConditions(Map<String, Object> map);

    void updateMsgSNAndNameAndRemarkById(List<String> paramIds, int msgSN, int status, String parameterName, int i,
        String remark);

    /**
     * 批量删除下发指令
     * @param monitorIds monitorIds
     */
    void deleteByMonitorIds(Set<String> monitorIds);

    List<Directive> findParameterByVehicleIds(List<String> vehicleIds, List<String> parameterNames, String type);
}
