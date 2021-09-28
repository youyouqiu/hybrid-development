package com.zw.platform.basic.service;

import com.zw.platform.basic.domain.BusinessScopeDO;
import com.zw.platform.basic.dto.BusinessScopeDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author wanxing
 * @Title: 商业经营范围service
 * @date 2020/11/516:46
 */
public interface BusinessScopeService {
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
     * @param ids  经营范围 字典表的id
     * @param type 1 企业  2 车
     * @return 是否添加成功
     */
    boolean bindBusinessScope(@Param("id") String id, @Param("ids") List<String> ids, @Param("type") Integer type);

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

    List<BusinessScopeDTO> getBusinessScope(String id);

    List<BusinessScopeDTO> getBusinessScopeByIds(Collection<String> ids);
}
