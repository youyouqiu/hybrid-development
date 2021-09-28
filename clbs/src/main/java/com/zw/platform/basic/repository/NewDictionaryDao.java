package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.DictionaryDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * zw_c_dictionary
 *
 * @author zhangjuan
 * @date 2020/10/20
 */
public interface NewDictionaryDao {

    /**
     * 获取全部的数据字典列表
     *
     * @return 数据字典列表
     */
    List<DictionaryDO> getList();

    /**
     * 根据类型查询
     * @param type type
     * @return DictionaryDO
     */
    List<DictionaryDO> findByType(@Param("type") String type);


    /**
     * 根据code和type获取字段表数据
     * @param code code
     * @param type type
     * @return DictionaryInfo
     */
    String getValueByCodeAndType(@Param("code") String code, @Param("type") String type);

}
