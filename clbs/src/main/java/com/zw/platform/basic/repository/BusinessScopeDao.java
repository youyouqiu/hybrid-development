package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.BusinessScopeDO;
import com.zw.platform.basic.dto.BusinessScopeDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author wanxing
 * @Title: 商业经营范围Dao
 * @date 2020/11/516:43
 */
public interface BusinessScopeDao {

    /**
     * 添加车辆运营范围绑定关系
     *
     * @param businessScopes 绑定范围
     * @return 是否添加成功
     */
    boolean addBusinessScope(@Param("list") Collection<BusinessScopeDO> businessScopes);

    /**
     * 绑定经营范围
     * @param id
     * @param scopeIds  经营范围 字典表的id
     * @param type 1 企业  2 车
     * @return 是否添加成功
     */
    boolean bindBusinessScope(@Param("id") String id,
        @Param("scopeIds") List<String> scopeIds, @Param("type") Integer type);

    /**
     * 通过id 进行删除
     * @param id
     * @return
     */
    boolean deleteById(@Param("id") String id);

    /**
     * 通过id 进行删除
     * @param ids
     * @return
     */
    boolean deleteByIds(@Param("ids") Collection<String> ids);

    /**
     * 获取数据字典的BusinessScope
     * @param id
     * @return
     */
    List<BusinessScopeDTO> getBusinessScope(String id);

    /**
     * 获取数据字典的BusinessScope
     * @param id
     * @return
     */
    List<BusinessScopeDTO> getBusinessScopeByIds(@Param("ids") Collection<String> id);
}
