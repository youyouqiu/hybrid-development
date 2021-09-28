package com.zw.platform.service.core;

import com.github.pagehelper.Page;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.query.ResourceQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ResourceService
 */
public interface ResourceService {

	/**
	 * 获取所有权限
	 */
	List<Resource> findAll();

	/**
	 * 
	 * 
	 * @Title: 分页查询
	 * @param query
	 * @return
	 * @return Page<Resource>
	 * @throws @author
	 *             wangying
	 */
	Page<Resource> findResourceByPage(ResourceQuery query);
	
	/**
	 * 
	*   根据角色list查询权限
	* @Title: findResourceByRoleIds
	* @param roleIds
	* @return
	* @return List<Resource>
	* @throws
	* @author wangying
	 */
	List<String> findResourceByRoleIds(List<String> roleIds);
	/**
	 *
	 *   根据角色list查询权限
	 * @Title: findResourceByRoleIds
	 * @param roleIds
	 * @return
	 * @return List<Resource>
	 * @throws
	 * @author wangying
	 */
	List<Resource> findResourceListByRoleIds(List<String> roleIds);

	/**
	 *  根据id查询资源
	 * @param id
	 * @return
	 */
	Resource findResourceById(String id);

	/**
	 * 初始化admin权限
	 * @return
	 */
	boolean deleteinitAdminRole();

	/**
	 * 逻辑删除
	 * @param flag
	 * @param id
	 * @return
	 */
	Integer updateflag(Integer flag,String id);
	/**
	 * 添加资源
	 * @param resource
	 * @return
	 */
	Integer addResource(Resource resource);

	/**
	 * 修改资源信息
	 * @param resource
	 * @return
	 */
	Integer updateResource(Resource resource);

	/**
	 * 初始化行驶证、运输证、保险到期提醒
	 * @return
	 * @throws Exception
	 */
	void initExpireRemind() throws Exception;
}