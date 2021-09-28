package com.zw.platform.service.obdManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.form.OBDDataInfo;
import com.zw.platform.domain.basicinfo.form.OBDVehicleDataInfo;
import com.zw.platform.domain.basicinfo.form.OBDVehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.OBDVehicleTypeQuery;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.protocol.msg.t808.body.LocationInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OBDVehicleTypeService {

    Page<OBDVehicleTypeForm> getList(BaseQueryBean queryBean);

    JsonResultBean addVehicleType(OBDVehicleTypeForm form);

    JsonResultBean updateVehicleType(OBDVehicleTypeForm form);

    JsonResultBean delete(String id);

    Map<String, Object> importVehicleType(MultipartFile file) throws Exception;

    void generateTemplate(HttpServletResponse response) throws IOException;

    void export(String title, int type, HttpServletResponse response, String query) throws IOException;

    boolean repetition(String name, Integer type, String id);

    OBDVehicleTypeForm findById(String id);

    boolean checkCode(String code, String id);

    /**
     * 获得OBD原车数据
     * @param monitorId
     * @param startTimeStr
     * @param endTimeStr
     * @return
     * @throws Exception
     */
    JsonResultBean getObdVehicleDataReport(String monitorId, String startTimeStr, String endTimeStr) throws Exception;

    /**
     * 组装obd数据
     */
    void installObdInfo(OBDVehicleDataInfo obdInfo);

    /**
     * 获得用户权限下绑定obd传感器的车辆
     * @return
     * @throws Exception
     */
    List<String> getBandObdSensorVehicle();

    /**
     * 获得OBD原车数据表格
     * @param query
     * @return
     * @throws Exception
     */
    PageGridBean getOBDVehicleDataTable(OBDVehicleTypeQuery query);

    List<OBDVehicleTypeForm> findAll();

    JsonResultBean findByCode(String code);

    /**
     * 预处理obd数据流
     */
    Map<Integer, String> prepareObdDataStream(JSONObject obdJsonObject, boolean isNeedAlarmInfo);

    /**
     * 组装obd流数据
     * @param info                 监控对象定位信
     * @return obd数据实体
     */
    OBDVehicleDataInfo convertStreamToObdInfo(LocationInfo info);

    /**
     * 组装obd流数据
     * @param info                 监控对象定位信
     * @return obd数据列表
     */
    List<OBDDataInfo> convertStreamToObdList(LocationInfo info);
}
