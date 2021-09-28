package com.zw.platform.service.intercomplatform.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.intercomplatform.IntercomPlatForm;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfig;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigView;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormQuery;
import com.zw.platform.repository.modules.IntercomPlatFormDao;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.intercomplatform.IntercomPlatFormService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by LiaoYuecai on 2017/3/3.
 */
@Service
public class IntercomPlatFormServiceImpl implements IntercomPlatFormService {

    @Autowired
    private IntercomPlatFormDao dao;

    @Autowired
    private UserService userService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private LogSearchService logSearchService;

    @Override
    public List<IntercomPlatForm> findList(IntercomPlatFormQuery query, boolean doPage) throws Exception {
        return doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> dao.findList(query))
                : dao.findList(query);
    }

    @Override
    public JsonResultBean add(IntercomPlatForm form, String ipAddress) throws Exception {
        form.setId(UUID.randomUUID().toString());
        form.setCreateDataTime(new Date());
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = dao.add(form);
        if (flag) {
            String message = "新增对讲平台 : " + form.getPlatformName() + " IP 为 : " + form.getPlatformIp();
            logSearchService.addLog(ipAddress, message, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteById(List<String> ids, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        for (String id : ids) {
            IntercomPlatForm intercomPlatForm = dao.findById(id);
            boolean flag = dao.deleteById(id);
            if (flag && intercomPlatForm != null) {
                message.append("删除对讲平台 : ").append(intercomPlatForm.getPlatformName()).append(
                    " IP为 : ").append(intercomPlatForm.getPlatformIp()).append(" <br/>");
            }
        }
        if (!message.toString().isEmpty()) {
            if (ids.size() == 1) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除对讲平台");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean update(IntercomPlatForm form, String ipAddress) throws Exception {
        IntercomPlatForm intercomPlatForm = dao.findById(form.getId());
        form.setUpdateDataTime(new Date());
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = dao.update(form);
        if (flag && intercomPlatForm != null) {
            String beforName = intercomPlatForm.getPlatformName(); // 修改前的名字
            String beforIp = intercomPlatForm.getPlatformIp(); // 修改前的IP
            String nowName = form.getPlatformName();
            String nowIp = form.getPlatformIp();
            String message = "";
            if (!beforName.equals(nowName) && beforIp.equals(nowIp)) { // 修改了对讲平台的名称
                message = "修改对讲平台的名称 : " + beforName + " 为 : " + nowName;
            } else if (beforName.equals(nowName) && !beforIp.equals(nowIp)) { // 修改了对讲平台的IP
                message = "修改对讲平台名称为 :" + nowName + " 的IP地址 : " + beforIp + " 为 :" + nowIp;
            } else if (!beforName.equals(nowName) && !beforIp.equals(nowIp)) { // 修改了名称和IP
                message = "修改对讲平台" + beforName + " 的名称为 : " + nowName + " 和IP为 : " + nowIp;
            } else {
                message = "修改对讲平台名称为 : " + nowName + " 的信息";
            }
            logSearchService.addLog(ipAddress, message, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public IntercomPlatForm findById(String id) throws Exception {
        return dao.findById(id);
    }

    @Override
    public Page<IntercomPlatFormConfigView> findConfigViewList(IntercomPlatFormConfigQuery query) throws Exception {
        return PageHelperUtil.doSelect(query, () -> dao.findConfigViewList(query));
    }

    @Override
    public void addConfig(IntercomPlatFormConfig config) {
        dao.addConfig(config);
    }

    @Override
    public void deleteConfigById(String id) {
        dao.deleteConfigById(id);
    }

    @Override
    public List<String> findConFigIdByVIds(List<String> vehicleIds) {
        return dao.findConFigIdByVIds(vehicleIds);
    }

    @Override
    public List<String> findConFigIdByPIds(List<String> pidLists) {
        return dao.findConFigIdByPIds(pidLists);
    }

    @Override
    public void updateConfigById(IntercomPlatFormConfig config) {
        dao.updateConfigById(config);
    }

    @Override
    public IntercomPlatFormConfigView findConfigViewByConfigId(String configId) {
        return dao.findConfigViewByConfigId(configId);
    }

    /**
     * 车辆权限树结构 首先查询组织，再查询组织下的分组，再查询分组下的车
     */
    @Override
    public JSONArray getVehicleTreeByPlatform() throws Exception {
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<String>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }
        // 查询当前用户权限分组
        List<Assignment> assignmentList = assignmentService.findUserAssignment(userService.getUserUuidById(userId),
            userOrgListId);
        List<String> assignIdList = new ArrayList<String>();
        if (assignmentList != null && assignmentList.size() > 0) {
            for (Assignment assign : assignmentList) {
                OrganizationLdap organization = userService.getOrgByUuid(assign.getGroupId());
                if (organization != null && organization.getId() != null) {
                    // 分组id list
                    assignIdList.add(assign.getId());
                    // 组装分组树
                    JSONObject assignmentObj = new JSONObject();
                    assignmentObj.put("id", assign.getId());
                    assignmentObj.put("pId", organization.getId().toString());
                    assignmentObj.put("name", assign.getName());
                    assignmentObj.put("type", "assignment");
                    assignmentObj.put("iconSkin", "assignmentSkin");
                    result.add(assignmentObj);
                }
            }
        }
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
        if (assignIdList != null && assignIdList.size() > 0) {
            List<VehicleInfo> vehicleList = dao.findVehicleTreeByPlatform(assignIdList);
            // 组装车辆树
            result.addAll(JsonUtil.getVehicleTree(vehicleList));
        }
        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(orgs, "multiple"));
        return result;
    }

    /**
     * 车辆权限树结构 首先查询组织，再查询组织下的分组，再查询分组下的车
     */
    @Override
    public JSONArray getVehicleTreeByThirdPlatform() throws Exception {
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<String>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }
        // 查询当前用户权限分组
        List<Assignment> assignmentList = assignmentService.findUserAssignment(userService.getUserUuidById(userId),
            userOrgListId);
        List<String> assignIdList = new ArrayList<String>();
        if (assignmentList != null && assignmentList.size() > 0) {
            for (Assignment assign : assignmentList) {
                OrganizationLdap organization = userService.getOrgByUuid(assign.getGroupId());
                if (organization != null && organization.getId() != null) {
                    // 分组id list
                    assignIdList.add(assign.getId());
                    // 组装分组树
                    JSONObject assignmentObj = new JSONObject();
                    assignmentObj.put("id", assign.getId());
                    assignmentObj.put("pId", organization.getId().toString());
                    assignmentObj.put("name", assign.getName());
                    assignmentObj.put("type", "assignment");
                    assignmentObj.put("iconSkin", "assignmentSkin");
                    result.add(assignmentObj);
                }
            }
        }
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
        if (assignIdList.size() > 0) {
            List<VehicleInfo> vehicleList = dao.findVehicleTreeByThirdPlatform(assignIdList);
            // 组装车辆树
            result.addAll(JsonUtil.getVehicleTree(vehicleList));
        }
        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(orgs, "multiple"));
        return result;
    }

    /**
         * @Title: 组装组织树结构
     * @return
     * @return JSONObject
     * @throws @author wangying
     */
    /*
     * public void getGroupTree(List<OrganizationLdap> orgs, JSONArray result) { // 组装组 for (OrganizationLdap group :
     * orgs) { JSONObject obj = new JSONObject(); // 组装group数结构 obj.put("id", group.getCid()); obj.put("pId",
     * group.getPid()); obj.put("name", group.getName()); obj.put("iconSkin", "groupSkin"); obj.put("type", "group");
     * result.add(obj); } }
     */

}
