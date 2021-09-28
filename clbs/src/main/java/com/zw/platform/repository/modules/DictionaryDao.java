package com.zw.platform.repository.modules;

import com.zw.platform.domain.statistic.DictionaryInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author zhouzongbo on 2018/10/10 14:58
 */
@Deprecated
public interface DictionaryDao {
    List<Map> findOBD();

    /**
     * 根据code和type获取字段表数据
     * @param code code
     * @param type type
     * @return DictionaryInfo
     */
    String getValueByCodeAndType(@Param("code") String code, @Param("type") String type);

    List<DictionaryInfo> findByType(@Param("type") String type);

    List<DictionaryInfo> findById(@Param("ids") List<String> ids);
}
