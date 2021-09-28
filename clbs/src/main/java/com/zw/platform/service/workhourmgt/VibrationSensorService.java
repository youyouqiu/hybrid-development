package com.zw.platform.service.workhourmgt;


import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


public interface VibrationSensorService {
    /**
     * 查询振动传感器信息（分页）
     */
    List<VibrationSensorForm> findVibrationSensorByPage(VibrationSensorQuery query, boolean doPage);

    /**
     * 新增振动传感器
     */
    JsonResultBean addVibrationSensor(VibrationSensorForm form, String ipAddress) throws Exception;

    /**
     * 删除振动传感器
     */
    JsonResultBean deleteVibrationSensor(String id, String ipAddress) throws Exception;

    /**
     * 修改振动传感器
     * @param form
     *            实体
     * @param ipAddress
     *            客户端的IP地址
     * @return
     * @throws Exception
     */
    boolean updateVibrationSensor(VibrationSensorForm form, String ipAddress) throws Exception;

    VibrationSensorForm findVibrationSensorById(String id) throws Exception;

    int findByNumber(String sensorNumber) throws Exception;

    /**
     * 导出振动传感器列表
     * @Title: export
     * @param title
     * @param type
     *            导出类型（1:导出数据；2：导出模板）
     * @param response
     * @return
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    boolean export(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 生成导入模板
     * @Title: generateTemplate
     * @param response
     * @return
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 导入振动传感器
     * @Title: importTank
     * @param multipartFile
     * @return
     * @return Map
     * @throws @author
     *             Liubangquan
     */
    public Map importData(MultipartFile multipartFile, String ipAddress) throws Exception;

    int findByNumber(String id, String sensorNumber) throws Exception;

    List<VibrationSensorForm> findById(@Param("id") String id) throws Exception;
}
