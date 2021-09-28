package com.zw.platform.service.basicinfo;


import com.github.pagehelper.Page;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.domain.basicinfo.BrandInfo;
import com.zw.platform.domain.basicinfo.BrandModelsInfo;
import com.zw.platform.domain.basicinfo.form.BrandForm;
import com.zw.platform.domain.basicinfo.form.BrandModelsForm;
import com.zw.platform.domain.basicinfo.query.BrandModelsQuery;
import com.zw.platform.domain.basicinfo.query.BrandQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 *  <p> Title: 车辆品牌service </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @author: penghujie
 * @date 2018年4月17日下午4:00:00
 * @version 1.0
 */
public interface BrandService extends IpAddressService {

    /**
     * 添加品牌信息
     *
     * @param brandForm
     * @return true: 添加成功 false: 添加失败
     */
    boolean addBrand(BrandForm brandForm) throws Exception;

    /**
     * 添加品牌机型信息
     *
     * @param brandModelsForm
     * @return true: 添加成功 false: 添加失败
     */
    boolean addBrandModels(BrandModelsForm brandModelsForm) throws Exception;

    /**
     * 根据名称查询品牌
     */
    BrandInfo findBrandByName(String name) throws Exception;

    /**
     * 根据名称查询机型
     */
    List<BrandModelsInfo> findBrandModelsByName(String name) throws Exception;

    /**
     * 根据id查询品牌
     */
    BrandInfo getBrand(final String id) throws Exception;

    /**
     * 根据id查询机型
     */
    BrandModelsInfo getBrandModels(final String id) throws Exception;

    /**
     * 修改品牌
     * @param form
     * @return
     */
    JsonResultBean updateBrand(final BrandForm form) throws Exception;

    /**
     * 修改品牌
     * @param form
     * @return
     */
    JsonResultBean updateBrandModels(final BrandModelsForm form) throws Exception;

    /**
     * 查询品牌是否绑定机型
     * @param  id
     * @return 是否绑定机型
     */
    int getIsBandModel(String id) throws Exception;

    /**
     * 批量查询品牌是否绑定机型
     * @param ids
     * @return
     * @author Wang Ying
     */
    int getIsBandModelByBatch(List<String> ids) throws Exception;

    /**
     *根据id删除品牌
     * @param id
     * @return
     */
    JsonResultBean deleteBrandById(String id) throws Exception;

    /**
     * 批量删除品牌
     * @param ids
     * @return
     * @throws Exception
     */
    JsonResultBean deleteBrandByBatch(List<String> ids) throws Exception;

    /**
     *根据id删除机型
     * @param id
     * @return
     */
    JsonResultBean deleteBrandModelsById(String id) throws Exception;

    /**
     * 批量删除机型
     * @param ids
     * @return
     * @throws Exception
     */
    JsonResultBean deleteBrandModelsByBatch(List<String> ids) throws Exception;

    /**
     * 分页查询品牌
     */
    Page<BrandInfo> findBrandByPage(BrandQuery query) throws Exception;

    /**
     * 分页查询机型
     */
    Page<BrandModelsInfo> findBrandModelsByPage(BrandModelsQuery query) throws Exception;

    /**
     * 导出品牌
     */
    boolean exportBrand(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 导出机型
     */
    boolean exportBrandModels(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 生成品牌模版
     * @param response
     * @return
     */
    boolean generateTemplateBrand(HttpServletResponse response) throws Exception;

    /**
     * 生成机型模版
     * @param response
     * @return
     */
    boolean generateTemplateBrandModels(HttpServletResponse response) throws Exception;

    /**
     * 导入品牌
     */
    Map<String, Object> importBrand(MultipartFile multipartFile) throws Exception;

    /**
     * 导入机型
     */
    Map<String, Object> importBrandModels(MultipartFile multipartFile) throws Exception;


    List<BrandModelsInfo> findBrandModelsByBrandId(String id);
}
