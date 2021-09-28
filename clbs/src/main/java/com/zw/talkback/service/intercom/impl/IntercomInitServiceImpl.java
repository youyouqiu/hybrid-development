package com.zw.talkback.service.intercom.impl;

import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.form.RoleForm;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.IPAddrUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.talkback.service.core.RoleService;
import com.zw.talkback.service.intercom.IntercomInitService;
import com.zw.talkback.util.TalkCallUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/8/7 11:13
 @Description 初始化的service
 @version 1.0
 **/
@Service
public class IntercomInitServiceImpl implements IntercomInitService {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TalkCallUtil talkCallUtils;

    /**
     * 对讲平台顶级组织名称
     */
    @Value("${task.top.org.name:F3物联网监控平台}")
    private String topOrgName;
    @Value("${task.first.org.name:北京中位科技}")
    private String firstOrgName;

    /**
     * 一级客户账号
     */
    @Value("${task.first.customer.name:ZWKJ}")
    private String firstCustomerName;

    /**
     * 最大支持II类账号数
     */
    @Value("${task.first.customer.maxIINum:500}")
    private String firstCustomerMaxIINum;
    /**
     * 最大支持III类账号数
     */
    @Value("${task.first.customer.maxIIINum:20}")
    private String firstCustomerMaxIIINum;

    /**
     * 最大支持群组数
     */
    @Value("${task.first.customer.maxGroupNum:5000}")
    private String firstCustomerMaxGroupNum;
    /**
     * 客户地址
     */
    @Value("${task.first.customer.address:北京市市政府}")
    private String firstCustomerAddress;

    /**
     * 一级客户密码
     */
    @Value("${task.first.customer.password:ZWLBS16888}")
    private String firstCustomerPassword;

    @Override
    public void updateTopOrgName(String name, HttpServletRequest request) throws Exception {
        if (StrUtil.isBlank(name)) {
            name = topOrgName + "," + firstOrgName;
        }
        String[] topOrgName = name.split(",");
        OrganizationLdap topOrg = new OrganizationLdap();
        topOrg.setName(topOrgName[1]);
        topOrg.setPid("ou=Enterprise_top,ou=organization");
        userService.update(topOrg, IPAddrUtil.getClientIp(request));
        OrganizationLdap firstOrg = new OrganizationLdap();
        firstOrg.setName(topOrgName[0]);
        firstOrg.setPid("ou=organization");
        userService.update(firstOrg, IPAddrUtil.getClientIp(request));
    }

    @Override
    public JsonResultBean addRole(RoleForm form, String permissionTree, String ipAddress) {
        JsonResultBean result = null;
        try {
            if (StrUtil.isBlank(permissionTree) && StrUtil.isBlank(form.getRoleName())) {
                if (CollectionUtils.isEmpty(userService.queryRoleList("调度员角色", false))) {
                    RoleForm dispatchRole = new RoleForm();
                    dispatchRole.setRoleName("调度员角色");
                    //默认监控调度页面菜单
                    permissionTree = "[{\"id\":\"bdd2f745-c05a-4d1d-ac01-6d06b8007b74\",\"edit\":false},"
                        + "{\"id\":\"a6a65e0a-5e2f-40f9-b488-f3fcf84fe1ca\",\"edit\":true}]";
                    result = roleService.addRole(dispatchRole, permissionTree, ipAddress);
                }

                if (CollectionUtils.isEmpty(userService.queryRoleList("禁言角色", false))) {
                    //禁言角色
                    RoleForm bannedRole = new RoleForm();
                    bannedRole.setRoleName("禁言角色");
                    //默认监控调度页面菜单
                    permissionTree = "[{\"id\":\"bdd2f745-c05a-4d1d-ac01-6d06b8007b74\",\"edit\":false},"
                        + "{\"id\":\"a6a65e0a-5e2f-40f9-b488-f3fcf84fe1ca\",\"edit\":true}]";
                    result = roleService.addRole(bannedRole, permissionTree, ipAddress);
                }
                /*if (CosUtil.isEmpty(userService.queryRoleList("监听角色", false))) {
                    //监听角色
                    RoleForm monitorRole = new RoleForm();
                    monitorRole.setRoleName("监听角色");
                    //默认监控调度页面菜单
                    permissionTree = "[{\"id\":\"84113afa-df07-4247-8666-f164a7acbefa\",\"edit\":false},"
                        + "{\"id\":\"715b3ab5-b3fc-11e9-9469-000c29920fdc\",\"edit\":true}]";

                    result = roleService.addRole(monitorRole, permissionTree, ipAddress);
                }*/
                return result;

            }
            return roleService.addRole(form, permissionTree, ipAddress);
        } catch (Exception e) {
            return new JsonResultBean(false);
        }
    }

    @Override
    public JsonResultBean addUser(UserBean user, String ipAddress) {
        return new JsonResultBean(talkCallUtils.addFirstCustomer(getFirstCustomerInfo()));

    }

    public Map<String, String> getFirstCustomerInfo() {
        Map<String, String> firstCustomer = new HashMap<>();
        firstCustomer.put("customer.custId", "1");
        firstCustomer.put("customer.name", firstCustomerName);
        firstCustomer.put("customer.loginName", firstCustomerName);
        firstCustomer.put("customer.password", firstCustomerPassword);
        firstCustomer.put("customer.supportTalk", "1");
        firstCustomer.put("customer.supportVideo", "1");
        firstCustomer.put("customer.supportSensor", "1");
        firstCustomer.put("customer.maxIINum", firstCustomerMaxIINum);
        firstCustomer.put("customer.maxIIINum", firstCustomerMaxIIINum);
        firstCustomer.put("customer.maxGroupNum", "2");
        firstCustomer.put("customer.maxSecondaryCustNum", "1");
        firstCustomer.put("customer.recordDownloadEnable", "1");
        firstCustomer.put("customer.appEnable", "1");
        firstCustomer.put("customer.recordUploadEnable", "1");
        firstCustomer.put("customer.address", firstCustomerAddress);
        return firstCustomer;
    }

}
