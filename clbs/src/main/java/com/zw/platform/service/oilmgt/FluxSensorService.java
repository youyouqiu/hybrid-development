package com.zw.platform.service.oilmgt;

import com.zw.platform.domain.vas.oilmgt.FluxSensor;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: 流量传感器Service</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年9月19日上午9:29:52
 */
public interface FluxSensorService {

    /**
     * 查询流量传感器信息（分页）
     * @param query
     * @param doPage
     * @return
     */
    List<FluxSensor> findFluxSensorByPage(FluxSensorQuery query, boolean doPage);

    /**
         * @param id
     * @return FluxSensor
     * @throws
     * @Title: 根据id查询
     * @author wangying
     */
    FluxSensor findById(String id) throws Exception;

    /**
         * @param number
     * @return FluxSensor
     * @throws
     * @Title: 根据number查询
     * @author wangying
     */
    FluxSensor findByNumber(String number) throws Exception;

    FluxSensor findByNumber(String id, String number) throws Exception;

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 新增流量传感器
     * @author wangying
     */
    JsonResultBean addFluxSensor(FluxSensorForm form, String ipAddress) throws Exception;

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 修改流量传感器
     * @author wangying
     */
    JsonResultBean updateFluxSensor(FluxSensorForm form, String ipAddress) throws Exception;

    /**
         * @param id
     * @return boolean
     * @throws
     * @Title: 删除
     * @author wangying
     */
    JsonResultBean deleteFluxSensor(String id, String ipAddress) throws Exception;

    /**
         * @param sensorId
     * @return boolean
     * @throws
     * @Title: 根据流量传感器id删除车和流量传感器的关联关系
     * @author wangying
     */
    boolean deleteFluxSensorBind(String sensorId) throws Exception;

    /**
         * @param vehicleId
     * @param type
     * @return boolean
     * @throws
     * @Title: 根据车辆id删除车和流量传感器的关联关系
     * @author wangying
     */
    boolean deleteFluxSensorBindByVid(String vehicleId, Integer type);

    /**
     * 导入
     * @param multipartFile
     * @return
     */
    Map importSensor(MultipartFile multipartFile, String ipAddress) throws Exception;

    /**
     * 导出
     * @param title excel名称
     * @param type  导出类型（1:导出数据；2：导出模板）
     * @return
     */
    boolean export(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 生成模板
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    List<FluxSensor> findOilWearByVid(String id);
}
