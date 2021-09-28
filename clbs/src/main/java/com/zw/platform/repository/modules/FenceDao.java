package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.FenceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 
 * <p>
 * Title: 围栏Dao
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * 
 * @author: wangying
 * @date 2016年8月4日下午1:50:24
 * @version 1.0
 */
public interface FenceDao {

    /**
     * 查询围栏
     */
    List<FenceInfo> findFence();

    /**
     *
         *
     * @Title: 查询所有的类型
     * @return
     * @return List<String>
     * @throws @author
     *             wangying
     */
    List<String> findType();

    /**
     *
         *
     * @Title: 根据type查询围栏
     * @param type
     * @return
     * @return List<FenceInfo>
     * @throws @author
     *             wangying
     */
    List<Map<String, Object>> findFenceByType(@Param("type") String type, @Param("orgIds") List<String> orgIds);

    /**
    * 根据type查询围栏及相应围栏的详情
    * @Title: findFenceDetailByType
    * @param type
    * @return
    * @return List<Map<String,Object>>
    * @author Liubangquan
     */
    List<Map<String, Object>> findFenceDetailByType(@Param("type") String type);

    /**
     * 根据id查询围栏信息
     * @param
     * @return
     */
    FenceInfo findFenceInfoById(String id);

    /**
     * 通过企业ID获取
     * @param orgId
     * @return
     */
    int checkBindByOrgId(String orgId);
}
