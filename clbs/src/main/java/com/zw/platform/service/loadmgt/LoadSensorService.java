package com.zw.platform.service.loadmgt;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.loadmgt.ZwMSensorInfo;
import com.zw.platform.domain.vas.loadmgt.form.LoadSensorForm;
import com.zw.platform.domain.basicinfo.query.LoadSensorQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/***
 @Author gfw
 @Date 2018/9/6 14:24
 @Description 载重传感器接口 提供数据库增删改查
 @version 1.0
 **/
public interface LoadSensorService {
    /**
     * 分页查询
     * @param query
     * @return
     * @throws Exception
     */
    Page<ZwMSensorInfo> findByPage(final LoadSensorQuery query) throws Exception;

    /**
     * 新增单个传感器
     * @param form
     * @param ipAddress
     * @return
     * @throws Exception
     */
    JsonResultBean add(final LoadSensorForm form, String ipAddress) throws Exception;

    /**
     * 导出载重传感器列表
     * @param title
     * @param type
     * @param response
     * @return Exception
     */
    void exportList(String title, int type, HttpServletResponse response,LoadSensorQuery query) throws Exception;

    /**
     * 根据ids删除一个 或多个传感器
     * @param ids
     * @param ipAddress
     * @return
     * @throws Exception
     */
    JsonResultBean deleteMore(final String ids, String ipAddress) throws Exception;
    /**
     * 生成导入模板
     * @param response
     * @throws Exception
     */
    void generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 根据导入文件新增传感器
     * @param multipartFile
     * @param request
     * @param ipAddress
     * @return
     * @throws Exception
     */
    Map importBatch(MultipartFile multipartFile, HttpServletRequest request, String ipAddress)throws Exception;

    /**
     * 根据id和type查询传感器列表
     * @param id 传感器id
     * @return
     */
    ZwMSensorInfo getById(String id);

    /**
     * 修改传感器表
     * @param form 传感器信息
     * @param ipAddress ip地址
     * @return
     */
    JsonResultBean update(LoadSensorForm form, String ipAddress) throws Exception;

    boolean repetition(String sensorNumber, String id);
//    /**
//     * 根据id查询传感器
//     * @param id
//     * @return
//     */
//    ZwMSensorInfo findSensorById(String id);
//    /**
//     * 根据id判断浏览器是否被绑定
//     * @param id
//     * @return
//     */
//    int getIsBand(String id);
}
