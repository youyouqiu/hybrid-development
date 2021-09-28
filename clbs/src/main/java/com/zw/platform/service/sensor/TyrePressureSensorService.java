package com.zw.platform.service.sensor;

import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.domain.basicinfo.form.TyrePressureSensorForm;
import com.zw.platform.domain.basicinfo.query.TyrePressureSensorQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TyrePressureSensorService extends IpAddressService {

    List<TyrePressureSensorForm> getList(TyrePressureSensorQuery query);

    JsonResultBean saveSensor(TyrePressureSensorForm form) throws Exception;

    TyrePressureSensorForm findSensorById(String id);

    JsonResultBean updateSensor(TyrePressureSensorForm form) throws Exception;

    JsonResultBean deleteSensor(String id) throws Exception;

    boolean checkSensorName(String name, String id);

    JsonResultBean deleteMore(String ids) throws Exception;

    void generateTemplate(HttpServletResponse response) throws IOException;

    void exportSensor(String title, int type, HttpServletResponse response) throws Exception;

    Map<String, Object> importSensor(MultipartFile file) throws Exception;

    List<TyrePressureSensorForm> findAllSensor();
}
