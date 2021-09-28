package com.zw.platform.controller.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.service.core.ResourceService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/r/resource")
public class ResourceController {

    private static Logger log = LogManager.getLogger(ResourceController.class);

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private UserService userService;

    private static final String RESOURCE = "core/uum/resource/resource";

    @RequestMapping(value = "/resource", method = RequestMethod.GET)
    public String getHtml() {
        return RESOURCE;
    }

    /**
     * 初始化行驶证、运输证、保险到期提醒
     * @return
     */
    @RequestMapping(value = "/initExpireRemind", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean initExpireRemind() {
        try {
            resourceService.initExpireRemind();
            return new JsonResultBean("初始化行驶证、运输证、保险到期提醒成功");
        } catch (Exception e) {
            log.error("初始化行驶证、运输证、保险到期提醒失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "初始化行驶证、运输证、保险到期提醒异常");
        }
    }

    /**
     * 初始化admin导航栏权限
     * @return
     */
    @RequestMapping(value = "/initAdminRole", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean initAdminRole() {
        resourceService.deleteinitAdminRole();
        return new JsonResultBean("初始化admin权限成功");
    }

    /**
     * 根据ID获取资源
     * @param id
     * @return
     */
    @RequestMapping(value = "/findById", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findById(String id) {
        Resource resource = resourceService.findResourceById(id);
        if (resource == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "id不存在");
        }
        return new JsonResultBean(resource);
    }

    @RequestMapping(value = "initRoleToAdmin", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean initRoleToAdmin(HttpServletRequest request) throws Exception {
        // 获取所有组织
        List<Group> allGroup = userService.getAllGroup();
        // 获取admin
        String admin = userService.getUserDetails("admin").getId().toString();
        // 更新角色
        StringBuilder builder = new StringBuilder();
        for (Group group : allGroup) {
            builder.append(group.getName()).append(",");
        }
        // 获取操作用户的IP
        String ipAddress = new GetIpAddr().getIpAddr(request);
        String substring = builder.substring(0, builder.length() - 1);
        userService.updateRolesByUser(admin, substring, ipAddress);
        return new JsonResultBean(JsonResultBean.SUCCESS, "分配角色成功");
    }

    /**
     * 获取资源列表
     * @return
     */
    @RequestMapping(value = "/roleList", method = RequestMethod.GET)
    @ResponseBody
    public JSONArray roleList() {
        List<Resource> resources = resourceService.findAll(); // 所有权限
        JSONArray result = new JSONArray();
        for (Resource resource : resources) {
            JSONObject obj = new JSONObject();
            obj.put("id", resource.getId());
            obj.put("pId", resource.getParentId());
            obj.put("name", resource.getResourceName());
            obj.put("chkDisabled", false);
            //            obj.put("nocheck", true);
            //            obj.put("type", resource.getType());
            result.add(obj);
        }
        return result;
    }

    /**
     * 添加资源
     * @param resource
     * @return
     */
    @RequestMapping(value = "/addResource", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addResource(Resource resource) {
        if (StringUtils.isBlank(resource.getResourceName())) {
            return new JsonResultBean(JsonResultBean.FAULT, "资源名称不能为空");
        }

        try {
            resourceService.addResource(resource);
        } catch (Exception e) {
            log.error("添加资源失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "添加失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "添加成功");
    }

    /**
     * 修改资源
     * @param
     * @return
     */
    @RequestMapping(value = "/updateResource", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateResource(Resource resource) {

        if (StringUtils.isBlank(resource.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, "资源id不能为空");
        }
        Resource result = resourceService.findResourceById(resource.getId());
        if (result == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "资源不存在");
        }
        resource.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        try {
            resourceService.updateResource(resource);
        } catch (Exception e) {
            log.error("修改资源失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "修改失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "修改成功");
    }

    /**
     * 删除资源
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteResource", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteResource(String id) {

        Resource result = resourceService.findResourceById(id);
        if (result == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "资源不存在");
        }
        try {
            resourceService.updateflag(0, result.getId());
        } catch (Exception e) {
            log.error("删除资源失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "删除失败");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "删除成功");
    }
}
