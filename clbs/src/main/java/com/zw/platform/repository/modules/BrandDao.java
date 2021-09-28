package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.BrandInfo;
import com.zw.platform.domain.basicinfo.BrandModelsInfo;
import com.zw.platform.domain.basicinfo.form.BrandForm;
import com.zw.platform.domain.basicinfo.form.BrandModelsForm;
import com.zw.platform.domain.basicinfo.query.BrandModelsQuery;
import com.zw.platform.domain.basicinfo.query.BrandQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BrandDao {
    /**
     * 添加品牌信息
     *
     * @param brandForm
     * @return true: 添加成功 false: 添加失败
     */
    boolean addBrand(BrandForm brandForm);

    /**
     * 添加品牌信息
     *
     * @param brandModelsForm
     * @return true: 添加成功 false: 添加失败
     */
    boolean addBrandModels(BrandModelsForm brandModelsForm);

    /**
     * 根据名称查询品牌
     */
    BrandInfo findBrandByName(@Param("name") String name);

    /**
     * 根据名称查询品牌
     */
    List<BrandModelsInfo> findBrandModelsByName(@Param("name") String name);

    /**
     * 根据id查询品牌
     */
    BrandInfo getBrand(final String id);

    /**
     * 根据id查询机型
     */
    BrandModelsInfo getBrandModels(final String id);

    /**
     * 根据品牌id查询机型
     * @param id 品牌id
     * @return BrandModelsInfo
     * @author zhouzongbo
     */
    List<BrandModelsInfo> findBrandModelsByBrandId(final String id);

    /**
     * 修改品牌
     * @param form
     * @return
     */
    boolean updateBrand(final BrandForm form);

    /**
     * 查询品牌是否绑定机型
     * @param id
     * @return
     */
    int getIsBandModel(String id);

    /**
     * 批量查询品牌是否绑定机型
     * @param ids
     * @return
     */
    int getIsBandModelByBatch(List<String> ids);

    /**
     * 删除品牌
     */
    boolean deleteBrandById(String id);

    /**
     * 批量删除品牌
     * @param ids
     * @return
     */
    boolean deleteBrandByBatch(List<String> ids);
    /**
     * 删除机型
     */
    boolean deleteBrandModelsById(String id);

    /**
     * 批量删除机型
     * @param ids
     * @return
     */
    boolean deleteBrandModelsByBatch(List<String> ids);

    /**
     * 修改机型
     * @param form
     * @return
     */
    boolean updateBrandModels(final BrandModelsForm form);

    /**
     * 查询品牌
     * @param query
     * @param
     * @return
     */
    Page<BrandInfo> findBrand(BrandQuery query);

    /**
     * 查询机型
     * @param query
     * @param
     * @return
     */
    Page<BrandModelsInfo> findBrandModels(BrandModelsQuery query);

    /**
     * 查询品牌导出
     * @return
     */
    List<BrandForm> findBrandExport();

    /**
     * 查询机型导出
     * @return
     */
    List<BrandModelsForm> findBrandModelsExport();

    /**
     * 批量新增品牌
     * @param importList
     * @return
     */
    boolean addBrandMore(List<BrandForm> importList);

    /**
     * 批量新增机型
     * @param importList
     * @return
     */
    boolean addBrandModelsMore(List<BrandModelsForm> importList);

    /**
     * 判断品牌机型是否存在
     * @param brandName  brandName
     * @param modelName modelName
     * @return int
     */
    BrandModelsForm countBrandAndBrandModel(@Param(value = "brandName") String brandName,@Param(value = "modelName") String modelName);
}
