package com.zw.platform.service.transdu;


import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.f3.TransduserManage;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * 温度传感器 /湿度传感器 /正反转传感器 service
 *
 * @author Administrator
 */
public interface TransduserService {
    /**
     * 根据传感器类别查询传感器管理
     */
    Page<TransduserManage> getTransduserManage(int transduserType, String param);

    /**
     * 根据传感器型号和类型查询传感器信息
     *
     */
    TransduserManage getSensorByNumber(String sensorNumber, Integer sensorType) throws Exception;

    /**
     * 增加传感器管理
     */
    JsonResultBean addTransduserManage(TransduserManage transduserManage, String ipAddress) throws Exception;

    /**
     * 修改传感器管理
     */
    JsonResultBean updateTransduserManage(TransduserManage transduserManage, String ipAddress) throws Exception;

    /**
     * 删除传感器管理
     */
    JsonResultBean deleteTransduserManage(String id, String ipAddress) throws Exception;

    /**
     * 根据传感器id查询绑定车辆条数
     */
    Integer findBoundNumberById(String id) throws Exception;

    /**
     * 根据id查询传感器管理
     */
    TransduserManage findTransduserManageById(String id) throws Exception;

    /**
     * 批量删除传感器管理
     */
    JsonResultBean updateBatchTransduserManages(List<String> ids, String ipAddress) throws Exception;

    /**
     * 导入
     *
     */
    Map importSensor(MultipartFile multipartFile, Integer sensorType, String ipAddress) throws Exception;

    /**
     * 生成模板
     *
     */
    boolean generateTemplate(HttpServletResponse response, Integer sensorType) throws Exception;

    /**
     * 导出传感器管理到excle文件
     *
     */
    boolean export(String title, int type, HttpServletResponse response, Integer sensorType) throws Exception;

}
