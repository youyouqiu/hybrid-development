package com.zw.platform.repository.modules;

import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.Parameter;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.systems.query.ParameterQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface ParameterDao {
    /**
     * @param id
     * @return Directive
     * @throws @author wangying
     * @Title: 根据id查询
     */
    List<Directive> findById(@Param("ids") List<String> ids);

    /**
     * 查询
     */
    List<Parameter> find(ParameterQuery query);

    /**
     * @param form
     * @return void
     * @throws @author wangying
     * @Title: 新增参数下发
     */
    void addDirective(DirectiveForm form);

    /**
     * @param form
     * @return void
     * @throws @author wangying
     * @Title: 批量新增参数下发
     */
    void addDirectiveByBatch(List<DirectiveForm> list);

    /**
     * @return void
     * @throws @author wangying
     * @Title: 根据id修改status
     */
    void updateStatusByMsgSN(@Param("msgSN") int msgSN, @Param("vehicleId") String vehicleId,
                             @Param("status") Integer status);

    /**
     * @return void
     * @throws @author wangying
     * @Title: 根据id修改status
     */
    void updateStatusByMsgSNAndReplyCode(@Param("msgSN") int msgSN, @Param("vehicleId") String vehicleId,
                                         @Param("status") Integer status);

    /**
     * @param ids
     * @param msgSN
     * @return void
     * @throws @author wangying
     * @Title: 根据id修改流水号
     */
    void updateMsgSNById(@Param("ids") List<String> ids, @Param("msgSN") int msgSN, @Param("status") Integer status,
                         @Param("replyCode") Integer replyCode);

    /**
     * @param ids
     * @param msgSN
     * @return void
     * @throws @author wangying
     * @Title: 根据id修改流水号和绑定id
     */
    void updateMsgSNAndNameById(@Param("ids") List<String> ids, @Param("msgSN") int msgSN,
                                @Param("status") Integer status, @Param("parameterName") String parameterName,
                                @Param("replyCode") Integer replyCode);

    /**
     * TODO 根据车辆id和流水号查询围栏绑定id
     *
     * @param vehicleId
     * @param msgSn
     * @return String
     * @throws @author wangying
     * @Title: selectFenceConfigId
     */
    String selectFenceConfigId(@Param("vehicleId") String vehicleId, @Param("msgSN") int msgSn);

    /**
     * TODO 根据类型查询下发参数
     *
     * @param vehicleId
     * @param msgSn
     * @param type
     * @return Parameter
     * @throws @author wangying
     * @Title: findParameterByType
     */
    List<Directive> findParameterByType(@Param("vehicleId") String vehicleId,
                                        @Param("parameterName") String parameterName, @Param("type") String type);

    /**
     * 根据类型查询监控对象下发状态
     *
     * @param moIds 监控对象id
     * @param type  类型
     * @return List<Directive>
     */
    List<Directive> findDirectiveByMoIdAndType(@Param("moIds") Collection<String> moIds, @Param("type") String type);

    /**
     * TODO 根据类型查询下发参数(模糊搜)
     *
     * @param vehicleId
     * @param type
     * @return Parameter
     * @throws @author wangying
     * @Title: findParameterByType
     */
    List<Directive> findParameterFuzzyByType(@Param("vehicleId") String vehicleId,
                                             @Param("parameterName") String parameterName, @Param("type") String type);

    /**
     * 根据绑定表删除下发数据（解除绑定时）
     *
     * @param configId
     */
    void deleteByConfigId(@Param("configId") String configId);

    /**
     * 根据车辆不同下发类型及绑定的id删除
     *
     * @param parameterName
     * @param type
     */
    void deleteByVechicleidParameterName(@Param("vechicleid") String vechicleid,
                                         @Param("parameterName") String parameterName, @Param("type") String type);

    void deleteProtocolParameterByVechicleId(@Param("vechicleid") String vechicleid);

    void deleteByVechicleidType(@Param("vechicleid") String vechicleid, @Param("type") String type);

    /**
     * @param vehicleId
     * @param parameterName
     * @param parameter_type
     * @return int
     * @Description:根据绑定id和类型，修改下发状态（当修改绑定时，下发状态也要一同修改）
     * @exception:
     * @author: wangying
     * @time:2017年1月9日 上午10:56:48
     */
    int updateStatusByParameterName(@Param("status") int status,
                                    @Param("vehicleId") String vehicleId,
                                    @Param("parameterName") String parameterName,
                                    @Param("parameterType") String parameterType);

    /**
     * 根据监控对象id、参数类型和绑定表id 修改下发状态(仅用于报警参数设置)
     *
     * @param status
     * @param conditions monitorObjectId：监控对象id; parameterName:绑定表id; parameterType:参数类型;
     * @return
     */
    int updateStatusByBatch(@Param("status") int status, @Param("conditions") List<Map<String, String>> conditions);

    /**
     * 带条件查询
     *
     * @param map
     * @return
     */
    List<Directive> selectDirectiveByConditions(Map<String, Object> map);

    /**
     * 根据车辆id和绑定表id修改下发状态为null(修改了设置就要清空下发状态)
     */
    boolean updateSendStatus(@Param("vehicleId") String vehicleId, @Param("sensorConfigId") String sensorConfigId);

    /**
     * 更新下发状态
     *
     * @param id     下发记录id
     * @param status 状态
     * @return boolean
     */
    boolean updateStatusById(@Param("id") String id, @Param("status") Integer status);

    /**
     * 清除轮询
     *
     * @param ids
     * @param msgSN
     * @param downTime
     * @param status
     * @param parameterName
     * @param replyCode
     * @param remark
     */
    void updateMsgSNAndNameAndRemarkById(@Param("ids") List<String> ids,
                                         @Param("msgSN") int msgSN,
                                         @Param("downTime") Date downTime,
                                         @Param("status") int status,
                                         @Param("parameterName") String parameterName,
                                         @Param("replyCode") int replyCode,
                                         @Param("remark") String remark);

    List<Directive> findParameterStatus(@Param("vehicleId") String vehicleId,
                                        @Param("parameterName") String parameterName,
                                        @Param("type") String type,
                                        @Param("swiftNumber") String swiftNumber);

    /**
     * 查找下发指令
     *
     * @param monitorId     监控对象id
     * @param parameterType 参数类型
     * @return DirectiveForm
     */
    DirectiveForm findDirective(@Param("monitorId") String monitorId,
                                @Param("parameterType") String parameterType);

    /**
     * 修改下发指令
     *
     * @param directiveForm directiveForm
     */
    void updateDirectiveById(DirectiveForm directiveForm);

    /**
     * 获得下发文本信息状态列表
     *
     * @param monitorIdList
     * @param parameterType
     * @return
     */
    List<DirectiveForm> getSendStatusList(@Param("monitorIdList") List<String> monitorIdList,
                                          @Param("parameterType") String parameterType);

    DirectiveForm getLastDirective(@Param("msgSN") Integer msgSN,
                                   @Param("vehicleId") String vehicleId,
                                   @Param("parameterType") String parameterType);

    void deleteByMonitorIds(@Param("monitorIds") Set<String> monitorIds);

    List<Directive> findParameterByVehicleIds(@Param("vehicleIds") List<String> vehicleIds,
                                              @Param("parameterNames") List<String> parameterNames,
                                              @Param("type") String type);

    List<String> listIdByParameterName(List<String> parameterNames);

    void deleteByIds(List<String> ids);

    Integer getSendAdasStatus(@Param("vehicleId") String vehicleId,
                              @Param("parameterType") String parameterType,
                              @Param("parameterName") String parameterName);
}
