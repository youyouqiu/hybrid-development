package com.zw.platform.basic.service;


import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.domain.basicinfo.query.VehiclePurposeQuery;
import com.zw.platform.util.common.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 运营类别管理
 *
 * @author zhangjuan 2020-10-14
 */
public interface VehiclePurposeService {

    /**
     * 添加运营类别
     *
     * @param vehiclePurposeDTO 运营类别实体
     * @return 是否添加成功
     * @throws BusinessException BusinessException
     */
    boolean add(VehiclePurposeDTO vehiclePurposeDTO) throws BusinessException;

    /**
     * 更新运营类别更新
     *
     * @param vehiclePurposeDTO 运营类别实体
     * @return 是否更新成功
     * @throws BusinessException BusinessException
     */
    boolean update(VehiclePurposeDTO vehiclePurposeDTO) throws BusinessException;

    /**
     * 删除车辆运营类别
     *
     * @param id 车辆运营ID
     * @return 是否删除成功
     * @throws BusinessException BusinessException
     */
    boolean delete(String id) throws BusinessException;

    /**
     * 批量删除车辆运营类别
     *
     * @param ids 车辆运营ID集合
     * @return 删除成功条数
     */
    int delBatch(Collection<String> ids);

    /**
     * 获取运营类别详情
     *
     * @param id 车辆运营ID
     * @return 运营类别详情
     */
    VehiclePurposeDTO getById(String id);

    /**
     * 根据名称获取运营类别
     * @param name 名称
     * @return 运营类别详情
     */
    VehiclePurposeDTO getByName(String name);

    /**
     * 运营类别是否存在
     *
     * @param id              id 添加时可为空，更新时必填
     * @param purposeCategory 运营类别名称
     * @return true 存在 false 不存在
     */
    boolean isExistPurpose(String id, String purposeCategory);


    /**
     * 分页获取车辆运营类别
     *
     * @param query 查询条件
     * @return 车辆运营类别列表
     */
    Page<VehiclePurposeDTO> getListByKeyWord(VehiclePurposeQuery query);

    /**
     * 生成模板
     *
     * @param response response
     * @return 是否生成成功
     * @throws Exception Exception
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;


    /**
     * 运营类别导入
     *
     * @param file file
     * @return 导入结果
     * @throws Exception Exception
     */
    Map<String, Object> importExcel(MultipartFile file) throws Exception;

    /**
     * 导出运营类别数据
     *
     * @param response http请求响应数据
     * @return 是否导入成功
     * @throws Exception Exception
     */
    boolean export(HttpServletResponse response) throws Exception;

    /**
     * 获取所有的车辆运营类别
     * @param keyword 关键字 为空查询全部
     * @return 运营类别列表
     */
    List<VehiclePurposeDTO> getAllByKeyWord(String keyword);
}
