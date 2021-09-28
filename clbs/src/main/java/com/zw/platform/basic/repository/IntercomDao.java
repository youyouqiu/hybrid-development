package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.IntercomDO;
import com.zw.platform.basic.dto.IntercomDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 对讲信息配置DAO类 zw_m_intercom_info
 * @author zhangjuan
 */
public interface IntercomDao {
    /**
     * 根据按时间升序获顺序列表
     * @return 监控对象集合
     */
    List<String> getSortList();

    /**
     * 对讲信息插入
     * @param intercomDO 对象信息
     * @return 是否操作成功
     */
    boolean insert(IntercomDO intercomDO);

    /**
     * 根据对讲终端号获取（5位机型+七位终端号）
     * @param intercomDeviceNum 对讲终端号
     * @return 对讲信息
     */
    IntercomDO getByIntercomDeviceNum(@Param("intercomDeviceNum") String intercomDeviceNum);

    /**
     * 根据ID获取对讲详情
     * @param configId 信息配置绑定Id
     * @return 对讲对象详情
     */
    IntercomDTO getDetailByConfigId(@Param("configId") String configId);

    /**
     * 根据ID获取对讲对象详情
     * @param configIds 信息配置绑定Id集合
     * @return 对讲对象详情集合
     */
    List<IntercomDTO> getDetailByConfigIds(@Param("configIds") Collection<String> configIds);

    /**
     * 根据监控对象id获取对讲详情
     * @param monitorIds 监控对象ID
     * @return 对讲对象详情集合
     */
    List<IntercomDTO> getDetailByMonitorIds(@Param("monitorIds") Collection<String> monitorIds);

    /**
     * 根据ID删除对讲对象
     * @param ids ids
     * @return 是否操作成功
     */
    boolean deleteByIds(@Param("ids") Collection<String> ids);

    /**
     * 清空信息配置的对讲id
     * @param configIds 信息配置Id集合
     * @return 是否操作成功
     */
    boolean clearConfigIntercomId(@Param("configIds") Collection<String> configIds);

    /**
     * 获取对讲设备标识列表
     * @param interDeviceNumList 对讲设备标识 为空：返回全部 不为空返回已经存在的设备标识
     * @return 对讲设备标识列表
     */
    Set<String> getIntercomDeviceNum(@Param("interDeviceNumList") Collection<String> interDeviceNumList);

    /**
     * 批量添加对讲对象
     * @param intercomList 对象信息列表
     * @return 是否操作成功
     */
    boolean addByBatch(@Param("intercomList") Collection<IntercomDO> intercomList);

    /**
     * 更新对讲对象信息
     * @param intercomDO 对讲信息
     * @return 是否更新成功
     */
    boolean update(IntercomDO intercomDO);

}
