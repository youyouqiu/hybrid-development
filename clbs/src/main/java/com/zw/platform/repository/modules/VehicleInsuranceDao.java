package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.VehicleInsuranceInfo;
import com.zw.platform.domain.basicinfo.form.VehicleInsuranceForm;
import com.zw.platform.domain.basicinfo.query.VehicleInsuranceQuery;

import java.util.List;
import java.util.Map;

/**
 * 车辆保险dao
 * @author zhouzongbo on 2018/5/10 10:26
 */
public interface VehicleInsuranceDao {
    /**
     * 删除车辆保险
     * @param id id
     * @return boolean
     */
    boolean delete(String id);

    /**
     * 根据车辆保险id查询数据
     * @param id id
     * @return VehicleInsuranceInfo
     */
    VehicleInsuranceInfo getVehicleInsuranceById(String id);

    /**
     * 修改
     * @param vehicleInsuranceForm form
     * @return boolean
     */
    boolean updateVehicleInsurance(VehicleInsuranceForm vehicleInsuranceForm);

    /**
     * 新增
     * @param vehicleInsuranceForm form
     * @return boolean
     */
    boolean addVehicleInsurance(VehicleInsuranceForm vehicleInsuranceForm);

    /**
     * 批量新增
     * @param vehicleInsuranceForm form
     * @return boolean
     */
    boolean addBatchVehicleInsurance(List<VehicleInsuranceForm> vehicleInsuranceForm);

    /**
     * 根据保险单号查询数据
     * @param insuranceId 保险单号
     * @return VehicleInsuranceInfo
     */
    VehicleInsuranceInfo getVehicleInsuranceByInsuranceId(String insuranceId);

    /**
     * 根据车辆id删除保险信息
     * @param vehicleId 车辆id
     * @return boolean
     */
    boolean deleteByVehicleId(String vehicleId);

    /**
     * 根据车辆ids删除保险信息
     * @param vehicleIds 车辆ids
     * @return boolean
     */
    boolean deleteByVehicleIds(String[] vehicleIds);

    /**
     * 查询出保险到期时间和提前提醒天数满足条件的保险单号数据
     * @return list
     */
    List<String> findExpireVehicleInsurance();

    List<Map<String, String>> findExpireVehicleInsuranceVehIds();

    /**
     * 查询车辆下的保险新消息
     * @param vehicleId 车辆id
     * @return insurance1，insurance3，insurance3
     */
    String findBindingInsuranceByVehicleId(String vehicleId);

    /**
     * 分页查询车辆保险
     * @param vehicleInsuranceQuery query
     * @return page
     */
    Page<VehicleInsuranceInfo> findVehicleInsuranceList(VehicleInsuranceQuery vehicleInsuranceQuery);
}
