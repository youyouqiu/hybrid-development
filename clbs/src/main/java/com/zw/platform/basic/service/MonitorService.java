package com.zw.platform.basic.service;

import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.domain.basicinfo.MonitorAccStatus;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 监控对象service类
 * @date 2020/11/615:26
 */
public interface MonitorService {
    /**
     * 通过组织Id获取监控对象Id
     * @param orgId
     * @return
     */
    List<String> getMonitorIdByOrgId(String orgId);

    /**
     * 根据监控对象id查询监控对象id-name的map映射
     * @param monitorIds
     * @return
     */
    Map<String, BaseKvDo<String, String>> getMonitorIdNameMap(Collection<String> monitorIds, String search);

    /**
     * 获取监控对象的在线状态详情
     * @param monitorIds 监控对象ID
     * @return 监控对象ID--监控对象状态详情
     */
    Map<String, ClientVehicleInfo> getMonitorStatus(Collection<String> monitorIds);

    /**
     * 获取所有在线的监控对象ID
     * @return 在线的监控对象集合
     */
    Set<String> getAllOnLineMonitor();

    /**
     * 获取所有在线监控对象的状态
     * @return 在线监控对象-状态Map
     */
    Map<String, ClientVehicleInfo> getAllMonitorStatus();

    /**
     * 获取监控在线状态和在线车辆的ACC状态
     * @param monitorIds 监控对象ID
     * @return 监控对象id-acc和在线的状态
     */
    Map<String, MonitorAccStatus> getAccAndStatus(Collection<String> monitorIds);

    /**
     * 获取监控在线状态和在线车辆的ACC状态
     * @param monitorIds    监控对象ID
     * @param needAccStatus true 封装ACC状态 false 不封装ACC状态
     * @return 监控对象id-acc和在线的状态
     */
    Map<String, MonitorAccStatus> getAccAndStatus(Collection<String> monitorIds, boolean needAccStatus);

    /**
     * 获取上过上线的监控对象---有位置缓存的监控对象
     * @param monitorIds 监控对象id集合
     * @return 上过线的监控对象
     */
    Set<String> getOnceOnLineIds(Collection<String> monitorIds);

    /**
     * 根据分组ID或orgDN获取监控对象
     * @param pid    分组ID或orgDn
     * @param type   group：分组 org：按组织
     * @param status 0不在线， 1在线， null 所有
     * @return 监控对象ID集合
     */
    Set<String> getMonitorByGroupOrOrgDn(String pid, String type, Integer status);

    /**
     * 根据监控对象类型过滤监控对象
     * @param monitorType vehicle:车 people:人  thing:物 monitor或空全部
     * @param monitorIds  已有的监控对象id
     */
    void filterByMonitorType(String monitorType, Set<String> monitorIds);

    /**
     * 根据模糊搜索关键字过滤监控对象
     * @param keyword    关键字
     * @param queryType  name：按监控对象名称 simCardNumber：按终端手机号 deviceNumber：按终端号  professional：按从业人员名称
     * @param monitorIds 已有的监控对象id
     */
    void filterByKeyword(String keyword, String queryType, Set<String> monitorIds);

    /**
     * 过滤协议类型
     * @param deviceTypes 协议类型列表
     * @param ownIds      过滤前的监控对象Id集合
     */
    void filterByDeviceType(Collection<String> deviceTypes, Set<String> ownIds);

    /**
     * 根据车辆类型过滤监控对象
     * @param veTypeName 车型名称
     * @param ownIds     监控对象Id集合
     */
    void filterByVehicleTypeName(String veTypeName, Set<String> ownIds);

    /**
     * 按状态过滤监控对象
     * @param accAndStatusMap 在线的监控对象及监控对象的状态Map 可以为空,若为null，则在方法内部获取该方法
     * @param monitorIds      已有的监控对象ID集合
     * @param onlineStatus    0未上线, 1在线，2在线停车，3在线行驶，4报警，5超速报警,6未定位,7未上线,8离线,9心跳
     */
    void filterByOnlineStatus(Map<String, MonitorAccStatus> accAndStatusMap, Set<String> monitorIds,
        Integer onlineStatus);

    /**
     * 根据监控对象获取终端ID
     * @param monitorIds
     * @return
     */
    Set<String> getDeviceIdByMonitor(Collection<String> monitorIds);

    /**
     * 获得在线的监控对象id
     * @param monitorIds  监控对象id
     * @return Set<String>
     */
    Set<String> getOnLineMonIds(Collection<String> monitorIds);

    /**
     * 获得离线的监控对象id
     * @param monitorIds  监控对象id
     * @return Set<String>
     */
    Set<String> getOffLineMonIds(Collection<String> monitorIds);

    /**
     * 根据车辆Id查询参数下发Id
     * @param monitorId 监控对象id
     * @return 参数下发id
     */
    List<String> findSendParmId(String monitorId);

}
