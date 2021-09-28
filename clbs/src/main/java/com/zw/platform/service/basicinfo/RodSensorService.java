package com.zw.platform.service.basicinfo;


import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.basicinfo.query.RodSensorQuery;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


/**
 * Created by Tdz on 2016/7/25.
 */
public interface RodSensorService {
    RodSensor get(final String id) throws Exception;

    /**
     * 分页查询
     */
    Page<RodSensor> findByPage(final RodSensorQuery query) throws Exception;

    /**
     * 新增
     */
    JsonResultBean add(final RodSensorForm form, String ipAddress) throws Exception;

    /**
     * 根据id删除一个 Personnel
     */
    JsonResultBean delete(final String id, String ipAddress) throws Exception;

    JsonResultBean update(final RodSensorForm form, String ipAddress) throws Exception;

    /**
     * 导出
     */
    boolean exportInfo(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 导入
     * @param multipartFile
     * @return
     */
    Map importSensor(MultipartFile multipartFile, HttpServletRequest request, String ipAddress) throws Exception;

    /**
     * 生成导入模板
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    RodSensor findByRodSensor(String sensorNumber) throws Exception;

    int getIsBand(String id) throws Exception;

    /**
     * 根据id查询传感器信息
     * @Title: findById
     * @param id
     * @return
     * @return RodSensor
     * @throws @author
     *             Liubangquan
     */
    public RodSensor findById(String id) throws Exception;

    RodSensor findByRodSensor(String id, String sensorNumber) throws Exception;
}
