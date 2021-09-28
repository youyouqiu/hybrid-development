package com.zw.talkback.service.intercom;

import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.form.RoleForm;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletRequest;

/***
 @Author zhengjc
 @Date 2019/8/7 11:11
 @Description 测试的service
 @version 1.0
 **/
public interface IntercomInitService {
    /**
     * 更新顶级企业和组织结构
     * @param name    第一个代表组织机构的名称，逗号之后是第一级企业的名称
     * @param request
     * @throws Exception
     */
    void updateTopOrgName(String name, HttpServletRequest request) throws Exception;

    /**
     * 新增企业
     * @param form           表单数据包含角色名称和角色描述
     * @param permissionTree 角色绑定的菜单
     * @param ipAddress      操作的客户端ip地址
     * @return
     * @throws Exception
     */
    JsonResultBean addRole(RoleForm form, String permissionTree, String ipAddress);

    /**
     * 新增用户
     * @param user
     * @param ipAddress
     * @return
     * @throws Exception
     */
    JsonResultBean addUser(UserBean user, String ipAddress);

}
