package com.zw.platform.basic.service;

import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.zw.lkyw.domain.LocationForLkyw;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.ws.entity.OutputControlSettingDO;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 处理F3接收信息处理
 * @author zhangjuan
 */
public interface F3MessageService {

    /**
     * 获取终端上报的车架号
     * @param locationInfo 位置信息
     */
    void setDeviceFrameNumber(LocationInfo locationInfo);

    /**
     * locationInfo 中的单位转换
     * @param locationInfo x0200位置信息
     */
    void unitConversion(LocationInfo locationInfo);

    /**
     * 从缓存中获取监控对象的位置信息
     * @param monitorIds    监控对ID
     * @param subscribeUser 订阅用户
     * @param sessionId
     * @return 监控对象位置相关信息
     */
    List<VehicleSpotCheckInfo> getCacheLocation(Set<String> monitorIds, String subscribeUser, String sessionId);

    /**
     * 封装LocationInfo里面监控对象详情
     * @param monitorId    监控对象信息
     * @param locationInfo 定位信息
     * @param monitorIcon  监控对象图标，若为null的情况下，方法体内部查找适用于单个监控对象信息设置
     * @param vehicle      车辆监控对象图标,若为null的情况方法内部获取，不为空适用于批量，为空适用渝单对象
     */
    void getMonitorDetail(String monitorId, LocationInfo locationInfo, String monitorIcon, VehicleDTO vehicle);

    /**
     * 统一重新构建推送给前端的位置信息
     * @param messages    locationInfo 定位信息
     * @param isFromRedis 消息来源是否来源与redis
     * @return 监控对象id-Message Map
     */
    Map<String, Message> buildWebLocationMsg(List<Message> messages, boolean isFromRedis);

    /**
     * 单条处理--统一重新构建推送给前端的位置信息
     * @param locationInfo 定位信息
     * @param monitorId    监控对象id
     * @param isFromRedis  消息来源是否来源与redis
     */
    void buildWebLocationMsg(@NonNull LocationInfo locationInfo, String monitorId, boolean isFromRedis);

    /**
     * 两课一危--从缓存中获取监控对象的位置信息
     * @param monitorIds   监控对ID
     * @param isGetAddress true:解析逆地址信息
     * @return 监控对象位置相关信息
     */
    List<LocationForLkyw> getLkywCacheLocation(Collection<String> monitorIds, boolean isGetAddress);

    /**
     * 从缓存中获取监控对象的OBO信息并推送
     * @param monitorIds    监控对ID
     * @param subscribeUser 订阅用户
     */
    void pushCacheOboInfo(Set<String> monitorIds, String subscribeUser);

    /**
     * 计算当日油耗及里程--直接从flink计算好的缓存中获取
     * @param info      LocationInfo
     * @param monitorId 监控对象ID
     */
    void getCurDayOilAndMile(LocationInfo info, String monitorId);

    /**
     * 向油补平台发送补发数据请求
     * @param monitorIds 监控对象id集合
     * @param sessionId  websocket sessionId
     * @param startTime  开始时间
     * @param endTime    结束时间
     */
    void sendReissueDataRequest(Collection<String> monitorIds, String sessionId, Date startTime, Date endTime);

    /**
     * 发送输出控制指令 8500
     * @param sessionId websocket sessionId
     * @param settingDO 输出控制参数
     */
    void saveAndSendOutputControl(String sessionId, OutputControlSettingDO settingDO);
}
