package com.zw.platform.service.sensor;


import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.vas.f3.Peripheral;
import com.zw.platform.util.common.JsonResultBean;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月08日 17:47
 */
public interface PeripheralService {

    /**
     * 查询分页数据
     * @param query
     * @return
     */
    Page<Peripheral> findByPage(AssignmentQuery query) throws Exception;

    /**
     * 查询所有可用
     * @return
     */
    List<Peripheral> findAllow();

    /**
     * 根据主键ID查询外设信息
     * @param id
     * @return
     */
    Peripheral findById(String id);

    /**
     * 根据外设ID查询外设信息
     * @param identId
     * @return
     */
    List<Peripheral> findByIdentId(String identId);

    /**
     * 根据外设identName查询外设信息
     * @param identName
     * @return
     */
    List<Peripheral> getByIdentName(String identName);

    /**
     * 通过ID获取绑定的车辆数
     * @param id
     * @return
     */
    Integer getConfigCountById(String id);

    /**
     * 根据ID删除外设信息
     */
    JsonResultBean deleteById(String id);

    /**
     * 新增Peripheral
     * @param peripheral
     */
    JsonResultBean addPeripheral(Peripheral peripheral);

    /**
     * 修改Peripheral
     * @param peripheral
     */
    JsonResultBean updatePeripheral(Peripheral peripheral);

    /**
     * 批量删除
     * @param ids
     */
    // void deleteByBatch(List<String> ids) throws Exception;
    /**
     * 批量删除2
     * @param item
     * @Param ip
     * return JsonResultBean
     */
    JsonResultBean deleteByBatch(String[] item);

    /**
     * 导入
     * @param multipartFile
     * @return
     * @throws Exception
     */
    Map<String, Object> addImportPeripheral(MultipartFile multipartFile) throws Exception;

    /**
     * 导出
     *
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
