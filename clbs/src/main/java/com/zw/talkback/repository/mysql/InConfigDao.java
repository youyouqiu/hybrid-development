package com.zw.talkback.repository.mysql;

import com.zw.talkback.domain.basicinfo.IntercomObjectInfo;
import com.zw.talkback.domain.basicinfo.form.InConfigInfoForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 信息录入
 */
public interface InConfigDao {
    /**
     * 判断监控对象，卡号设备是否被绑定过
     * @param monitorName 监控对象名称
     * @param simNum      SIM卡号
     * @param device      设备号
     * @param monitorType 监控对象类型
     * @return 绑定对象
     */
    InConfigInfoForm getIsBand(@Param("monitorName") String monitorName, @Param("simNum") String simNum,
        @Param("device") String device, @Param("monitorType") String monitorType);

    /**
     * 判断监控对象，卡号设备绑定的对象
     * @param monitorName 监控对象名称
     * @param simNum      SIM卡号
     * @param device      设备号
     * @return 绑定对象
     */
    List<InConfigInfoForm> getIsBands(@Param("monitorName") String monitorName, @Param("simNum") String simNum,
        @Param("device") String device, @Param("monitorType") String monitorType);

    /**
     * 设备绑定过的对讲对象数
     * @param device 设备号
     * @return 设备被绑定的对讲对象数量
     */
    int getIsBandIntercomObject(@Param("device") String device);

    boolean updateConfigIntercomID(@Param("configId") String configId, @Param("intercomInfoId") String intercomInfoId);

    /**
     * 获取所有绑定对讲的监控对象类型
     * @param monitorType 监控类型
     * @return 监控ID
     */
    Set<String> getBindMonitorId(String monitorType);

    /**
     * 获取所有绑定对讲对象的SIM卡ID
     * @return 获取所有绑定对讲对象的SIM卡ID
     */
    Set<String> getBindSimId();

    /**
     * 修改监控对象的对讲对象ID为空
     * @param configIdList configIdList
     * @return 是否成功
     */
    boolean updateConfigListBatch(@Param("configIdList") List<String> configIdList);

    List<String> getMonitorIdByFuzzyQuery(@Param("simpleQueryParam") String simpleQueryParam);

    /**
     * 获取对讲设备绑定的configId
     * @param device 设备号
     * @return 设备被绑定的对讲对象数量
     */
    String getIsBandConfigIdForDevice(@Param("device") String device);

    List<String> getIdbySimDeviceOrmonitor(@Param("monitorName") String monitorName, @Param("simNum") String simNum,
        @Param("device") String device, @Param("monitorType") String monitorType);

    /**
     * 更新设备功能类型
     * @param deviceId       设备ID
     * @param functionalType 功能类型
     * @return 是否更新成功
     */
    boolean updateDeviceFuncType(@Param("deviceId") String deviceId, @Param("functionalType") String functionalType);

    /**
     * 批量更新绑定关系表中的对讲对象ID
     * @param intercomObjects intercomObjects
     * @return 是否更新成功
     */
    boolean updateBatchConfigIntercomID(@Param("intercomObjects") List<IntercomObjectInfo> intercomObjects);
}
