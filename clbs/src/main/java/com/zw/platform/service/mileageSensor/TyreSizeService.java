package com.zw.platform.service.mileageSensor;


import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.mileageSensor.TyreSize;
import com.zw.platform.domain.vas.mileageSensor.TyreSizeQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


public interface TyreSizeService {

    /**
     * 新增TyreSize
     */
    JsonResultBean addTyreSize(TyreSize tyreSizes, String ipAddress);

    /**
     * 修改 TyreSize
     */
    JsonResultBean updateTyreSize(TyreSize tyreSizes, String ipAddress);

    /**
     * 批量删除TyreSize
     */
    JsonResultBean deleteBatchTyreSize(List<String> tyreSizes, String ipAddress);

    /**
     * 根据ID查询MTyreSize
     */
    TyreSize findById(String id);

    /**
     * 根据类型及名称查询轮胎规格
     * @param tireType
     *            类型
     * @param sizeName
     *            规格
     */
    TyreSize findByTypeAndName(String tireType, String sizeName);

    /**
     * 检查轮胎数据是否被绑定
     */
    String checkConfig(String id);

    /**
     * 根据查询条件查询信息
     */
    Page<TyreSize> findByQuery(TyreSizeQuery query);

    /**
     * 根据所有可用
     */
    List<TyreSize> findAll();

    /**
     * 导入
     */
    Map addImportTyreSize(MultipartFile multipartFile, String ipAddress) throws Exception;

    /**
     * 导出
     * @param title
     *            excel名称
     * @param type
     *            导出类型（1:导出数据；2：导出模板）
     * @param response
     *            文件
     */
    boolean export(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 生成模板
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;
}
