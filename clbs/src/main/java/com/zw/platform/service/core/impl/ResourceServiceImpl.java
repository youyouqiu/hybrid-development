package com.zw.platform.service.core.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.form.RoleResourceForm;
import com.zw.platform.domain.core.query.ResourceQuery;
import com.zw.platform.repository.core.ResourceDao;
import com.zw.platform.repository.core.RoleDao;
import com.zw.platform.repository.modules.VehicleInsuranceDao;
import com.zw.platform.service.core.ResourceService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.MethodLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ResourceService
 */
@Service
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private NewVehicleDao newVehicleDao;
    @Autowired
    private VehicleInsuranceDao vehicleInsuranceDao;

    @Override
    public List<Resource> findAll() {
        List<Resource> list = new ArrayList<>();
        list = resourceDao.find();
        // 解决bug400,bug410:企业管理-角色管理：修改角色界面，操作权限有时显示只有系统管理和功能配置、有时显示3.0平台的所有功能。
        //		if (!list.isEmpty() && (list instanceof Page<?>)){
        //			list = resourceDao.find();
        //		}
        return list;
    }

    /**
     * 查询终端（分页）
     */
    @MethodLog(name = "分页查询资源", description = "分页查询资源")
    @Override
    public Page<Resource> findResourceByPage(ResourceQuery query) {
        return PageHelperUtil.doSelect(query, () -> resourceDao.findResource(query));
    }

    @Override
    public List<String> findResourceByRoleIds(List<String> roleIds) {
        if (roleIds != null && roleIds.size() > 0) {
            return resourceDao.findResourceByRoleIds(roleIds);
        }
        return null;
    }

    @Override
    public List<Resource> findResourceListByRoleIds(List<String> roleIds) {
        if (roleIds != null && roleIds.size() > 0) {
            return resourceDao.findResourceListByRoleIds(roleIds);
        }
        return null;
    }

    @Override
    public Resource findResourceById(String id) {
        if (StringUtils.isNotBlank(id)) {
            return resourceDao.findResourceById(id);
        }
        return null;
    }

    /**
     * 添加资源
     * @param
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addResource(Resource resource) {
        resource.setId(UUID.randomUUID().toString());
        resource.setCreateDataTime(new Date());
        resource.setCreateDataUsername(SystemHelper.getCurrentUsername());
        resource.setFlag(1);
        resource.setPermission("");

        Integer is = resourceDao.addResource(resource);
        //添加admin的权限
        RoleResourceForm roleResourceForm = new RoleResourceForm();
        roleResourceForm.setId(UUID.randomUUID().toString());
        roleResourceForm.setRoleId("cn=ROLE_ADMIN,ou=Groups");
        roleResourceForm.setResourceId(resource.getId());
        roleResourceForm.setCreateDataTime(new Date());
        roleResourceForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        roleResourceForm.setFlag(1);
        roleResourceForm.setEditable(1);
        List list = new ArrayList(1);
        list.add(roleResourceForm);
        roleDao.addRoleResourceByBatch(list);
        return is;
    }

    /**
     * 初始化admin权限
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteinitAdminRole() {
        //删除
        roleDao.deleteByAdmin("cn=ROLE_ADMIN,ou=Groups");
        //获取所有权限
        List<Resource> list = resourceDao.find();
        List<RoleResourceForm> roleList = new ArrayList<>(list.size());
        for (Resource resource : list) {
            RoleResourceForm roleResourceForm = new RoleResourceForm();
            roleResourceForm.setId(UUID.randomUUID().toString());
            roleResourceForm.setRoleId("cn=ROLE_ADMIN,ou=Groups");
            roleResourceForm.setResourceId(resource.getId());
            roleResourceForm.setCreateDataTime(new Date());
            roleResourceForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
            roleResourceForm.setFlag(1);
            roleResourceForm.setEditable(1);
            roleList.add(roleResourceForm);
        }
        //现有的导航栏添加到admin权限中
        return roleDao.addRoleResourceByBatch(roleList);
    }

    /**
     * 逻辑删除
     * @param flag
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer updateflag(Integer flag, String id) {
        //删除权限的关联关系
        roleDao.deleteByResourceId(id);
        return resourceDao.updateflag(flag, id);
    }

    /**
     * 修改资源
     * @param
     * @return
     */
    @Override
    public Integer updateResource(Resource resource) {
        resource.setUpdateDataTime(new Date());
        return resourceDao.update(resource);
    }

    @Override
    public void initExpireRemind() throws Exception {
        //运输证即将到期
        List<String> expireRoadTransport = newVehicleDao.getVehicleIdsByWillExpireRoadTransport();
        if (CollectionUtils.isNotEmpty(expireRoadTransport)) {
            RedisHelper
                .setString(HistoryRedisKeyEnum.EXPIRE_ROAD_TRANSPORT.of(), JSON.toJSONString(expireRoadTransport));
        } else {
            RedisHelper.setString(
                HistoryRedisKeyEnum.EXPIRE_ROAD_TRANSPORT.of(), new JSONArray(new ArrayList<>()).toJSONString());
        }
        //行驶证即将到期
        List<String> expireLicense = newVehicleDao.getVehicleIdsByWillExpireLicense();
        if (CollectionUtils.isNotEmpty(expireLicense)) {
            RedisHelper.setString(HistoryRedisKeyEnum.EXPIRE_DRIVING_LICENSE.of(), JSON.toJSONString(expireLicense));
        } else {
            RedisHelper.setString(HistoryRedisKeyEnum.EXPIRE_DRIVING_LICENSE.of(),
                new JSONArray(new ArrayList<>()).toJSONString());
        }
        //保险即将到期
        List<String> expireVehicleInsurance = vehicleInsuranceDao.findExpireVehicleInsurance();
        if (CollectionUtils.isNotEmpty(expireVehicleInsurance)) {
            RedisHelper.addToList(HistoryRedisKeyEnum.EXPIRE_INSURANCE_ID.of(), expireVehicleInsurance);
        } else {
            RedisHelper.addToList(HistoryRedisKeyEnum.EXPIRE_INSURANCE_ID.of(), new ArrayList<>());
        }
    }

}