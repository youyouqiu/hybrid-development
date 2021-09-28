package com.zw.platform.service.loadmgt;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.loadmgt.LoadVehicleSettingInfo;
import com.zw.platform.domain.vas.loadmgt.ZwMSensorInfo;
import com.zw.platform.domain.vas.loadmgt.form.AdValueForm;
import com.zw.platform.domain.vas.loadmgt.form.LoadVehicleSettingSensorForm;
import com.zw.platform.domain.vas.loadmgt.query.LoadVehicleSettingQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/***
 @Author gfw
 @Date 2018/9/10 9:19
 @Description 载重车辆设置 接口
 @version 1.0
 **/
public interface LoadVehicleSettingService {
    /**
     * 查询车辆与载重传感器的绑定
     * @param query
     * @return
     * @throws Exception
     */
    Page<LoadVehicleSettingInfo> findLoadVehicleList(LoadVehicleSettingQuery query) throws Exception;

    /**
     * 查询车辆与载重传感器的绑定 单个
     * @param vehicleId
     * @return
     * @throws Exception
     */
    LoadVehicleSettingInfo findLoadVehicleByVid(String vehicleId) throws Exception;


    /**
     * 根据传感器类型查询传感器
     * @param sensorType
     * @return
     * @throws Exception
     */
    List<ZwMSensorInfo> findSensorInfo(String sensorType) throws Exception;

    /**
     * 根据绑定的车辆id 获取绑定信息
     * @param vehicleId
     * @return
     * @throws Exception
     */
    LoadVehicleSettingInfo findLoadBingInfo(String vehicleId) throws Exception;

    /**
     * 查询参考对象
     * @return
     * @throws Exception
     */
    List<LoadVehicleSettingInfo> findReferenceVehicle() throws Exception;

    /**
     * 将车辆id和服务器进行绑定
     * @param form
     * @param ipAddress
     * @return
     * @throws Exception
     */
    JsonResultBean addLoadVehicleSetting(LoadVehicleSettingSensorForm form, String ipAddress) throws Exception;

    /**
     * 修改传感器
     * @param form
     * @param ipAddress
     * @return
     * @throws Exception
     */
    JsonResultBean updateLoadSetting(LoadVehicleSettingSensorForm form, String ipAddress) throws Exception;

    /**
     * 传感器和车辆进行解绑
     * @param id
     * @param ipAddress
     * @return
     * @throws Exception
     */
    JsonResultBean deleteLoadSettingBind(String id, String ipAddress) throws Exception;

    /**
     * 传感器和车辆进行批量解绑
     * @param ids
     * @param ipAddress
     * @return
     * @throws Exception
     */
    JsonResultBean deleteMoreLoadSettingBind(String ids, String ipAddress) throws Exception;

    /**
     * 根据绑定id获取传感器
     * @param id
     * @return
     * @throws Exception
     */
    LoadVehicleSettingInfo getSensorVehicleByBindId(String id) throws Exception;

    /**
     * 载重下发
     * @param paramList
     * @param ip
     * @throws Exception
     */
    void sendLoadSetting(List<JSONObject> paramList, String ip) throws Exception;

    /**
     * 生成AD导入模板
     * @param response
     */
    void generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 导入AD模板数据
     * @param file
     * @param request
     * @param ipAddress
     * @return
     * @throws Exception
     */
    Map importBatch(MultipartFile file, HttpServletRequest request, String ipAddress) throws Exception;

    /**
     * 根据id查询AD模板列表
     * @param id
     * @return
     */
    List<AdValueForm> findAdList(String id, String sensorVehicleId);

    /***
     * 更新标定表
     * @param id
     * @param sensorVehicleId
     * @param calibrationValue
     * @return
     * @throws Exception
     */
    String updateCalibration(String id, String sensorVehicleId, String calibrationValue) throws Exception;

    /**
     * 更新绑定关系
     * @param form
     * @param ipAddress
     * @param paramType
     * @return
     * @throws Exception
     */
    JsonResultBean updateWorkSettingBind(LoadVehicleSettingSensorForm form, String ipAddress, String paramType)
        throws Exception;

    /**
     * 查询车辆位置信息
     * @param vehicleId
     * @return
     * @throws Exception
     */
    String getLatestPositional(String vehicleId) throws Exception;

    List<LoadVehicleSettingInfo> findReferenceVehicleByProtocols(List<Integer> protocols);
}
