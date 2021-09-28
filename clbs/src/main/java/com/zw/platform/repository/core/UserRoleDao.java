package com.zw.platform.repository.core;

import com.zw.platform.domain.core.Role;
import com.zw.platform.domain.core.form.UserRoleForm;

import java.util.List;


/**
 * UserRoleDao
 */
public interface UserRoleDao {
    /**
     * 查询用户拥有的角色
     */
    List<Role> findByUser(String userId);

    /**
     * 通过用户id获得角色id集合
     */
    List<String> getRoleIdsByUser(String userId);

    /**
     * 根据userId删除roles
     */
    int deleteByUserId(String userId);

    /**
     * 为某个用户新增角色 同时插入多条数据
     */
    void addUserRole(UserRoleForm form);
}
