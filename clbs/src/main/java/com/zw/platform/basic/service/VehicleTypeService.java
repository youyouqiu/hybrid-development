package com.zw.platform.basic.service;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.dto.query.VehicleTypePageQuery;
import com.zw.platform.util.common.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 车辆类型服务接口
 * @author zhangjuan
 */
public interface VehicleTypeService {

    /**
     * 新增
     * @param typeDTO 车辆类型实体
     * @return 是否操作成功
     * @throws BusinessException 异常
     */
    boolean add(VehicleTypeDTO typeDTO) throws BusinessException;

    /**
     * 同类别下是否存在相同的车辆类型
     * @param id       车辆类型ID
     * @param category 车辆类别
     * @param type     车辆类型
     * @return true 存在 false 不存在
     */
    boolean isExistType(String id, String category, String type);

    /**
     * 更新车辆类型
     * @param typeDTO 车辆类型实体
     * @return 是否操作成功
     * @throws BusinessException 异常
     */
    boolean update(VehicleTypeDTO typeDTO) throws BusinessException;

    /**
     * 删除
     * @param id 车辆类型ID
     * @return 是否操作成功
     * @throws BusinessException 异常
     */
    boolean delete(String id) throws BusinessException;

    /**
     * 批量删除
     * @param ids 车辆类型ID集合
     * @return 操作结果信息
     */
    String deleteBatch(Collection<String> ids);

    /**
     * 是否绑定车辆子类型
     * @param id 车辆类型ID
     * @return true 绑定  false 未绑定
     */
    boolean isBindSubType(String id);

    /**
     * 绑定车辆的车辆类型
     * @param ids 车辆类型ID集合
     * @return 绑定车辆的车辆类型ID集合
     */
    Set<String> getVehicleBindTypes(Collection<String> ids);

    /**
     * 获取车型详情
     * @param id id
     * @return 车型详情
     */
    VehicleTypeDTO getById(String id);

    /**
     * 分页查询
     * @param query 分页查询条件
     * @return 车辆类型
     */
    Page<VehicleTypeDTO> getByPage(VehicleTypePageQuery query);

    /**
     * 获取车型列表
     * @param keyword keyword
     * @return 车型列表
     */
    List<VehicleTypeDTO> getListByKeyword(String keyword);


    /**
     * 导出车型数据
     * @param response http请求响应数据
     * @return 是否导入成功
     * @throws Exception Exception
     */
    boolean export(HttpServletResponse response) throws Exception;

    /**
     * 生成模板
     * @param response response
     * @return 是否生成成功
     * @throws Exception Exception
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 车型导入
     * @param file file
     * @return 导入结果
     * @throws Exception Exception
     */
    Map<String, Object> importExcel(MultipartFile file) throws Exception;

    /**
     * 根据车辆类型名称和类别获取车辆类型
     * @param category 类别名称
     * @param type     类型名称
     * @return 车辆类型
     */
    VehicleTypeDTO getByName(String category, String type);

    /**
     * 根据车辆类别获取车辆类型列表
     * @param categoryId 车辆类别ID
     * @return 车辆类型列表
     */
    List<VehicleTypeDTO> getByCategoryId(String categoryId);

    /**
     * 根据标准类型获取车辆类型
     * @param standard 车辆类别标准
     * @return 车辆类型列表
     */
    List<VehicleTypeDTO> getByStandard(Integer standard);

}
