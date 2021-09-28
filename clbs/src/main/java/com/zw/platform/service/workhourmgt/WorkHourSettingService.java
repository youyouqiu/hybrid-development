package com.zw.platform.service.workhourmgt;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSettingForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSettingQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

/**
 * 工时设置service
 * @author zhouzongbo on 2018/5/28 16:24
 */
public interface WorkHourSettingService {
    /**
     * 工时设置list
     * @param query query
     * @return return
     * @throws Exception ex
     */
    Page<WorkHourSettingInfo> findWorkHourSettingList(WorkHourSettingQuery query) throws Exception;

    /**
     * 工时设置list
     * @param vehicleId
     * @return return
     * @throws Exception ex
     */
    WorkHourSettingInfo findWorkHourSettingByVid(String vehicleId) throws Exception;

    /**
     * 参数参考对象
     * @return list
     */
    List<WorkHourSettingInfo> findReferenceVehicle();

    List<WorkHourSettingInfo> findReferenceVehicleByProtocols(List<Integer> protocols);

    /**
     * 设置
     * @param form      from
     * @param ipAddress ip
     * @return JsonResultBean
     * @throws Exception ex
     */
    JsonResultBean addWorkHourSetting(WorkHourSettingForm form, String ipAddress) throws Exception;

    WorkHourSettingInfo findVehicleWorkHourSettingByVid(String vehicleId);

    /**
     * 修改
     * @param form      form
     * @param ipAddress ipAddress
     * @return JsonResultBean
     */
    JsonResultBean updateWorkHourSetting(WorkHourSettingForm form, String ipAddress) throws Exception;

    /**
     * 解绑
     * @param id id
     * @return JsonResultBean
     */
    JsonResultBean deleteWorkHourSettingBind(String id, String ipAddress) throws Exception;

    /**
     * 批量解绑
     * @param ids ids
     * @return JsonResultBean
     */
    JsonResultBean deleteMoreWorkHourSettingBind(String ids, String ipAddress) throws Exception;

    /**
     * 参数下发
     * @param paramList 参数列表
     * @param ip        ip
     * @throws Exception ex
     */
    void sendWorkHourSetting(List<JSONObject> paramList, String ip) throws Exception;

    /**
     * 根据sensorVehicle表id
     * @param id id
     * @return WorkHourSettingInfo
     */
    WorkHourSettingInfo getSensorVehicleByBindId(String id);

    JsonResultBean updateWorkSettingBind(WorkHourSettingForm form, String ipAddress) throws Exception;
}
