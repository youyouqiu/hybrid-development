package com.zw.platform.service.personalized.impl;

import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.FastNavConfigForm;
import com.zw.platform.domain.core.Group;
import com.zw.platform.repository.modules.FastNavConfigDao;
import com.zw.platform.service.core.ResourceService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.personalized.FastNavConfigService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FastNavConfigServiceImpl implements FastNavConfigService {

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private FastNavConfigDao fastNavConfigDao;

    /**
     * 获取当前用户的首页定制信息
     * @param userId
     * @return
     */
    @Override
    public List<FastNavConfigForm> getList(String userId) {
        List<FastNavConfigForm> list = fastNavConfigDao.getList(userId);
        if (list != null) {
            // 当前用户所拥有角色
            LdapName name = LdapUtils
                .newLdapName(SystemHelper.getCurrentUser().getId() + "," + userService.getBaseLdapPath().toString());
            List<Group> roles = (List<Group>) userService.findByMember(name);
            List<String> roleIds = new ArrayList<String>();
            if (roles != null && roles.size() > 0) {
                for (int i = 0; i < roles.size(); i++) {
                    roleIds.add(roles.get(i).getId().toString());
                }
            }
            // 查询当前用户拥有的菜单权限
            List<String> curResources = resourceService.findResourceByRoleIds(roleIds);
            if (curResources == null) {
                return new ArrayList<>();
            }
            for (FastNavConfigForm form : list) {
                if (!curResources.contains(form.getUrlId())) {
                    //没有权限的菜单地址设置为空 不跳转
                    form.setUrl("");
                }
            }
        }
        List<FastNavConfigForm> list1 = fastNavConfigDao.getOutSideNav(userId);
        if (list1 != null && list1.size() != 0) {
            list.addAll(list1);
        }
        return list;
    }

    /**
     * 新增或修改
     * @param fastNavConfigForm
     * @param ipAddress
     * @return
     */
    @Override
    public JsonResultBean add(FastNavConfigForm fastNavConfigForm, String ipAddress) throws Exception {
        fastNavConfigForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        //先删掉该用户同位置保存的信息
        fastNavConfigDao.delete(fastNavConfigForm);
        if (fastNavConfigForm.getNavType() == 1 && !StringUtils.isNotBlank(fastNavConfigForm.getDescription())) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        fastNavConfigDao.add(fastNavConfigForm);
        String message = "用户：" + SystemHelper.getCurrentUsername() + " 修改首页功能入口";
        logSearchService.addLog(ipAddress, message, "3", "", "", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean delete(String userId, String order, String ipAddress) throws Exception {
        FastNavConfigForm fastNavConfigForm = new FastNavConfigForm();
        fastNavConfigForm.setUserId(userId);
        fastNavConfigForm.setOrder(order);
        fastNavConfigDao.delete(fastNavConfigForm);
        String message = "用户：" + SystemHelper.getCurrentUsername() + " 删除首页功能入口";
        logSearchService.addLog(ipAddress, message, "3", "", "", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public FastNavConfigForm findBySort(String userId, String order) {
        FastNavConfigForm form = fastNavConfigDao.findBySort(userId, order);
        if (form != null) {
            // 当前用户所拥有角色
            LdapName name = LdapUtils
                .newLdapName(SystemHelper.getCurrentUser().getId() + "," + userService.getBaseLdapPath().toString());
            List<Group> roles = (List<Group>) userService.findByMember(name);
            List<String> roleIds = new ArrayList<String>();
            if (roles != null && roles.size() > 0) {
                for (int i = 0; i < roles.size(); i++) {
                    roleIds.add(roles.get(i).getId().toString());
                }
            }
            // 查询当前用户拥有的菜单权限
            List<String> curResources = resourceService.findResourceByRoleIds(roleIds);
            if (curResources.contains(form.getUrlId())) {
                return form;
            }
        }
        return null;
    }

    @Override
    public JsonResultBean updateOrders(String editOrder, String editedOrder, String editId, String editedId,
        String userId) {
        boolean boolEdit = false;
        boolean boolEdited = false;
        if (StringUtils.isNotBlank(editedId)) {
            FastNavConfigForm fastNavConfigFormEdited = fastNavConfigDao.findNavById(editedId);
            fastNavConfigFormEdited.setOrder(editOrder);
            fastNavConfigFormEdited.setUpdateDataTime(new Date());
            fastNavConfigFormEdited.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            boolEdited = fastNavConfigDao.updateNavOrderByForm(fastNavConfigFormEdited);
        }
        if (StringUtils.isNotBlank(editId)) {
            FastNavConfigForm fastNavConfigFormEdit = fastNavConfigDao.findNavById(editId);
            fastNavConfigFormEdit.setOrder(editedOrder);
            fastNavConfigFormEdit.setUpdateDataTime(new Date());
            fastNavConfigFormEdit.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            boolEdit = fastNavConfigDao.updateNavOrderByForm(fastNavConfigFormEdit);
        }
        if (boolEdit || boolEdited) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }
}
