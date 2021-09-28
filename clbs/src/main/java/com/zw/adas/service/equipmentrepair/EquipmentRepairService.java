package com.zw.adas.service.equipmentrepair;

import com.zw.adas.domain.equipmentrepair.*;
import com.zw.adas.domain.equipmentrepair.query.DeviceRepairQuery;
import com.zw.platform.util.common.PageGridBean;

import java.util.Collection;
import java.util.List;

/**
 * 设备报修service层
 */
public interface EquipmentRepairService {

    /**
     * 分页查询设备报修记录
     * @param query 查询条件
     * @return 分页内容
     * @throws Exception 异常
     */
    PageGridBean getList(DeviceRepairQuery query) throws Exception;

    /**
     * 根据主键获取设备报修记录
     * @param primaryKeys 主键(企业id_报修时间_故障类型_监控对象id)
     * @return 设备报修记录
     * @throws Exception 异常
     */
    List<DeviceRepairDTO> getByPrimaryKeys(Collection<String> primaryKeys) throws Exception;

    /**
     * 获取单条设备报修记录
     * @param primaryKey 主键(企业id_报修时间_故障类型_监控对象id)
     * @return 报修记录
     * @throws Exception 异常
     */
    DeviceRepairDTO getByPrimaryKey(String primaryKey) throws Exception;

    /**
     * 设备维修确认
     * @param confirmDTO 确认信息体
     * @return 是否确认成功
     * @throws Exception 异常
     */
    boolean confirm(ConfirmDeviceRepairDTO confirmDTO) throws Exception;

    /**
     * 设备维修完成
     * @param finishDTO 完成维修信息
     * @return 是否操作成功
     * @throws Exception 异常
     */
    boolean finish(FinishDeviceRepairDTO finishDTO) throws Exception;

    /**
     * 批量确认设备维修信息
     * @param batchConfirmDTO 确认维修信息
     * @return 是否确认成功
     * @throws Exception 异常
     */
    boolean batchConfirm(BatchConfirmRepairDTO batchConfirmDTO) throws Exception;

    /**
     * 批量完成设备维修
     * @param batchFinishDTO 完成设备维修信息
     * @return 是否操作成功
     * @throws Exception 异常
     */
    boolean batchFinish(BatchFinishRepairDTO batchFinishDTO) throws Exception;

}
