package com.zw.platform.service.workhourmgt;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSensorInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSensorQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 工时传感器
 * @author denghuabing
 * @date 2018.5.29
 * @version 1.0
 */
public interface WorkHourSensorService {

    /**
     * 型号重复校验
     * @param sensorNumber
     * @param id
     * @return
     */
    boolean repetition(String sensorNumber,String id);

    /**
     * 分页查询
     * @param query
     * @return
     */
    Page<WorkHourSensorInfo> findByPage(WorkHourSensorQuery query);

    /**
     * 新增工时传感器
     * @param form
     * @param ipAddress
     * @return
     * @throws Exception
     */
    JsonResultBean addWorkHourSensor(WorkHourSensorForm form,String ipAddress) throws Exception;

    /**
     * 根据id查询工时传感器信息
     * @param id
     * @return
     */
    WorkHourSensorForm findWorkHourSensorById(String id);

    /**
     * 修改工时传感器
     * @param form
     * @param ipAddress
     * @return
     */
    JsonResultBean updateWorkHourSensor(WorkHourSensorForm form,String ipAddress) throws Exception;

    /**
     * 删除工时传感器
     * @param id
     * @param ipAddress
     * @return
     */
    JsonResultBean deleteWorkHourSensor(String id,String ipAddress) throws Exception;

    /**
     * 批量删除
     * @param deltems
     * @param ipAddress
     * @return
     */
    JsonResultBean deleteMore(String deltems,String ipAddress) throws Exception;

    /**
     * 创建模板
     * @param response
     */
    void generateTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导出excel
     * @param title
     * @param type
     * @param response
     */
    void exportWorkHourSensor(String title,int type,HttpServletResponse response) throws Exception;

    /**
     * 批量导入
     * @param file
     * @param ipAddress
     * @return
     */
    Map<String,Object> importWorkHourSensor(MultipartFile file,String ipAddress) throws Exception;
}
