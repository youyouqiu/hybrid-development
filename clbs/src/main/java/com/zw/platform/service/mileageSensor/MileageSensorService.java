package com.zw.platform.service.mileageSensor;


import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.mileageSensor.MileageSensor;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * <p> Title:里程传感器基础信息Service <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:02
 */
public interface MileageSensorService {

    /**
     * 新增MileageSensor
     * @param mileageSensor
     */
    JsonResultBean addMileageSensor(MileageSensor mileageSensor, String ipAddress) throws Exception;

    /**
     * 修改 MileageSensor
     * @param mileageSensor
     */
    JsonResultBean updateMileageSensor(MileageSensor mileageSensor, String ipAddress) throws Exception;

    /**
     * 批量添加MileageSensor
     * @param mileageSensors
     */
    void addBatchMileageSensors(List<MileageSensor> mileageSensors) throws Exception;

    /**
     * 批量删除MileageSensor
     * @param mileageSensors
     */
    JsonResultBean deleteBatchMileageSensor(List<String> mileageSensors, String ipAddress) throws Exception;

    /**
     * 根据ID查询MileageSensor
     * @param id
     * @return
     */
    MileageSensor findById(String id) throws Exception;

    /**
     * 根据查询条件查询信息
     * @param query
     * @return
     */
    Page<MileageSensor> findByQuery(MileageSensorQuery query) throws Exception;

    /**
     * 根据所有可用
     * @return
     */
    List<MileageSensor> findAll() throws Exception;

    /**
     * 根据sensorType查询MileageSensor
     * @param sensorType
     * @return
     */
    MileageSensor findBySensorType(String sensorType) throws Exception;

    /**
     * 检查是否有配置
     * @param id
     * @return
     */
    String checkConfig(String id) throws Exception;

    /**
     * 导入
     * @param multipartFile
     * @return
     */
    Map addImportSensor(MultipartFile multipartFile, String ipAddress) throws Exception;

    /**
     * 导出
     * @param title
     *            excel名称
     * @param type
     *            导出类型（1:导出数据；2：导出模板）
     * @param response
     *            文件
     * @return
     */
    boolean export(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 生成模板
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

}
