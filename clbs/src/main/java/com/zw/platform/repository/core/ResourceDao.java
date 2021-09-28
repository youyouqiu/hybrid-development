package com.zw.platform.repository.core;

import com.zw.lkyw.domain.ReportMenu;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.query.ResourceQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 资源Dao
 */
public interface ResourceDao {
    /**
     * 查询全部 资源
     */
    List<Resource> find();

    /**
     * @param query
     * @return List<Resource>
     * @throws @author wangying
     * @Title: 根据查询条件查询（搜索及分页）
     */
    List<Resource> findResource(ResourceQuery query);

    /**
     * 根据角色list查询权限
     * @param roleIds
     * @return List<Resource>
     * @throws
     * @Title: findResourceByRoleIds
     * @author wangying
     */
    List<String> findResourceByRoleIds(@Param("roleIds") List<String> roleIds);

    /**
     * 根据角色list查询权限
     * @param roleIds
     * @return List<Resource>
     * @throws
     * @Title: findResourceByRoleIds
     * @author wangying
     */
    List<Resource> findResourceListByRoleIds(@Param("roleIds") List<String> roleIds);

    /**
     * 添加资源
     * @param resource
     * @return
     */
    Integer addResource(@Param("resource") Resource resource);

    /**
     * 逻辑删除
     * @param flag
     * @param id
     * @return
     */
    Integer updateflag(@Param("flag") Integer flag, @Param("id") String id);

    /**
     * 修改资源信息
     * @param resource
     * @return
     */
    Integer update(@Param("resource") Resource resource);

    /**
     * 根据资源id查询资源
     * @param id
     * @return
     */
    Resource findResourceById(String id);

    /**
     * 检查是否有APP登录权限
     * @param roleIds
     * @return
     */
    Integer checkAppRegister(@Param("roleIds") List<String> roleIds);

    /**
     * 通过名称查询指定资源列表的id
     * @param names
     * @return
     */
    List<ReportMenu> getReportMenuByNames(@Param("names") Collection<String> names);

    /**
     * 根据资源类型和名称获取资源id
     * @param map
     * @return
     */
    List<String> getIdByNameAndType(Map<String, String> map);
}