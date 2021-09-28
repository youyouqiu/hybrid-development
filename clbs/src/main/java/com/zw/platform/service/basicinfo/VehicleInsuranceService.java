package com.zw.platform.service.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.VehicleInsuranceInfo;
import com.zw.platform.domain.basicinfo.form.VehicleInsuranceForm;
import com.zw.platform.domain.basicinfo.query.VehicleInsuranceQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 车辆保险service
 * @author zhouzongbo on 2018/5/10 9:27
 */
public interface VehicleInsuranceService {
    /**
     * 分页查询车辆保险
     * @param vehicleInsuranceQuery this
     * @return this
     */
    Page<VehicleInsuranceInfo> findVehicleInsuranceList(VehicleInsuranceQuery vehicleInsuranceQuery);

    /**
     * 新增车辆
     * @param vehicleInsuranceForm form
     * @param ip                   ip
     * @return flag
     * @throws Exception ex
     */
    boolean add(VehicleInsuranceForm vehicleInsuranceForm, String ip) throws Exception;

    /**
     * 根据车辆保险id获取数据
     * @param id 车辆保险id
     * @return VehicleInsuranceInfo
     */
    VehicleInsuranceInfo getVehicleInsuranceById(String id);

    /**
     * 修改车辆信息
     * @param vehicleInsuranceForm form
     * @param ip                   ip
     * @return flag
     * @throws Exception ex
     */
    boolean updateVehicleInsurance(VehicleInsuranceForm vehicleInsuranceForm, String ip) throws Exception;

    /**
     * 根据保险单号查询数据
     * @param insuranceId 保险单号
     * @return VehicleInsuranceInfo
     */
    VehicleInsuranceInfo getVehicleInsuranceByInsuranceId(String insuranceId);

    /**
     * 删除车辆保险
     * @param id id
     * @param ip ip
     * @return boolean
     * @throws Exception this
     */
    boolean delete(String id, String ip) throws Exception;

    /**
     * 批量删除车辆保险
     * @param ids ids
     * @param ip  ip
     * @return boolean
     * @throws Exception this
     */
    JsonResultBean deleteMore(String ids, String ip) throws Exception;

    /**
     * 导出数据
     * @param response         response
     * @param simpleQueryParam 车牌号/保险单号
     */
    void getExport(HttpServletResponse response, String simpleQueryParam, Integer insuranceTipType);

    /**
     * 车辆模板下载
     * @param response response
     * @throws Exception this
     */
    void buildTemplate(HttpServletResponse response) throws Exception;

    /**
     * 车辆保险导入
     * @param file      this
     * @param ipAddress ip
     * @return map
     * @throws Exception ex
     */
    Map<String, Object> importVehicleInsurance(MultipartFile file, String ipAddress) throws Exception;

    /**
     * 根据车辆ids删除车辆下的保险信息
     * @param vehicleIds 车辆ids
     * @return boolean
     */
    boolean deleteByVehicleIds(String vehicleIds, String ip) throws Exception;

    /**
     * 根据车辆id删除车辆下的保险信息
     * @param ipAddress ip
     * @param vehicleId 车辆id
     * @param brand     车牌号
     * @return boolean
     * @throws Exception ex
     */
    boolean deleteByVehicleId(String ipAddress, String vehicleId, String brand) throws Exception;

    /**
     * 查询出保险到期时间和提前提醒天数满足条件的保险单号数据
     * @return list
     */
    List<Map<String, String>> findExpireVehicleInsurance();

    /**
     * 查询当前用户下的车牌号信息
     * @return list
     */
    List<Map<String, Object>> findVehicleMapSelect();
}
