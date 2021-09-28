package com.zw.platform.util.excel.annotation;

import com.zw.platform.util.common.BusinessException;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @param <T> 代表转后，存储数据库的do对象
 * @param <E> 代表导入的数据类型
 */
public interface ExcelImportHelper<T, E> {
    /**
     * 获取最终通过校验的数据，避免有些属性在校验通过之后，需要重新赋值初始化问题
     * @return
     */
    List<T> getFinalData();

    /**
     * 执行校验逻辑
     */
    void validate(Map<String, String> orgMap) throws BusinessException;

    /**
     * 获取excel的数据信息
     * @return
     */
    List<E> getExcelData();

    default void validateDataSize(List<E> excelData) throws BusinessException {
        if (CollectionUtils.isEmpty(excelData)) {
            throw new BusinessException("导入数据不能为空");
        }
    }

    /**
     * 初始化,进行excel数据转换
     */
    void init(Class<E> cls) throws InstantiationException, IllegalAccessException;

    /**
     * 获取校验结果
     * @return
     */
    boolean getValidateResult();
}
