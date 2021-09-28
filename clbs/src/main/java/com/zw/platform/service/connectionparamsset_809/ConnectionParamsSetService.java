package com.zw.platform.service.connectionparamsset_809;

import com.zw.platform.domain.connectionparamsset_809.AlarmHandleParam;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.connectionparamsset_809.PlantParamQuery;
import com.zw.platform.domain.connectionparamsset_809.T809AlarmSetting;
import com.zw.platform.domain.connectionparamsset_809.T809PlantFormCheck;
import com.zw.platform.domain.reportManagement.SuperPlatformMsg;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.body.module.ExtendPlatformMsgInfo;
import com.zw.protocol.msg.t809.body.module.PlatformAlarmInfo;
import com.zw.protocol.msg.t809.body.module.PlatformMsgAckInfo;

import java.util.List;
import java.util.Map;

/**
 * 809连接参数service
 * @author hujun
 * @since 创建时间：2018年2月26日 下午4:49:34
 */
public interface ConnectionParamsSetService {
    /**
     * 保存809连接参数
     * @author hujun
     * @since 创建时间：2018年2月26日 下午4:57:23
     */
    boolean save809ConnectionParamsSet(PlantParam param, String ipAddress);

    /**
     * 修改809连接参数（查询修改平台信息）
     */
    PlantParam get809ConnectionParamsForEdit(PlantParamQuery plantParamQuery) throws Exception;

    /**
     * 修改809连接参数
     * @author hujun
     * @since 创建时间：2018年2月27日 上午9:06:38
     */
    boolean update809ConnectionParamsSet(PlantParam param, String ipAddress);

    /**
     * 删除809连接参数
     * @author hujun
     * @since 创建时间：2018年2月27日 上午11:26:26
     */
    JsonResultBean delete809ConnectionParamsSet(String id, String ipAddress);

    /**
     * 查询809连接参数
     * @author hujun
     * @since 创建时间：2018年2月27日 上午11:26:37
     */
    List<PlantParam> get809ConnectionParamsSet(PlantParamQuery plantParamQuery);

    /**
     * 检查当前平台是否可以更改或者删除
     * @author hujun
     * @since 创建时间：2018年3月9日 下午5:43:37
     */
    boolean checkedPlatFormCanOperate(String platFormId);

    /**
     * 校验平台名称是否唯一
     * @author hujun
     * @since 创建时间：2018年3月12日 下午3:12:52
     */
    boolean check809PlatFormSole(String platFormName, String pid);

    /**
     * 校验协议类型下是否已经存在平台
     * @author hujun
     * @since 创建时间：2018年3月16日 上午11:08:18
     */
    boolean check809ProtocolType(String protocolType, String pid);

    /**
     * 校验主链路ip和从链路ip有无重复
     * @author hujun
     * @since 创建时间：2018年4月10日 下午3:38:22
     */
    boolean check809Ip(T809PlantFormCheck param);

    /**
     * 校验平台数据是否唯一（相同主链路ip下groupId及centerId不能重复）
     * @param param 校验参数实体
     */
    boolean check809DateSole(T809PlantFormCheck param);

    /**
     * 处理平台查岗、平台间报文并下发
     */
    void sendPlatformMsgAck(PlatformMsgAckInfo platformMsgAckInfo, String ipAddress);

    /**
     * 查岗辅助方法
     */
    void assistOnInspection(SuperPlatformMsg msg, Message message);

    List<String> getGroupId(String centerId, String ip);

    /**
     * 报警督办
     */
    Integer sendFormAlarmAck(PlatformAlarmInfo info, String ipAddress) throws Exception;

    Integer sendPlatformGangAck(PlatformMsgAckInfo platformMsgAckInfo, String ipAddress) throws Exception;

    Integer sendExtendHandleAck(ExtendPlatformMsgInfo info, String ipAddress) throws Exception;

    List<PlantParam> getMonitorPlatform(String monitorId);

    /**
     * 根据809转发平台ID、接入码查询809绑定信息
     */
    List<PlantParam> getPlatformFlag(String centerId, String ip);

    /**
     * 根据转发平台id、下级平台接入码、上级平台IP查询转发平台企业信息
     */
    PlantParam getPlatformInfoById(String id, String serviceIp, String centerId);

    JsonResultBean add809AlarmMapping(T809AlarmSetting t809AlarmSetting, String ip) throws Exception;

    /**
     * 根据809设置id和协议与类型查询809报警映射
     */
    Map<String, String> get809AlarmMapping(T809AlarmSetting t809AlarmSetting);

    /**
     * 获取808报警类型
     */
    List<AlarmType> getAlarmType(Integer protocolType);

    /**
     * 报警上报809平台
     */
    void initiativeSendAlarmHandle(AlarmHandleParam param) throws Exception;

    /**
     * 通过809平台id获取连接的状态
     */
    void pushConnectionStatusByPlatformId(String platformId);

    /**
     * 关闭809连接时候，取消809断线重连通知
     */
    void cancelRemindByGroupId(String groupId);

    /**
     * 接入码唯一性验证
     */
    boolean check809CenterIdUnique(Integer centerId, String id);

    /**
     * 平台唯一性验证（接入码，主IP，从IP三者组合）
     */
    JsonResultBean check809Unique(Integer centerId, String ip, String ipBranch, String id);

    /**
     * 验证平台名称唯一性
     * @param platformName 平台名称
     * @param id           平台id
     */
    boolean checkPlatformNameUnique(String platformName, String id);
}
