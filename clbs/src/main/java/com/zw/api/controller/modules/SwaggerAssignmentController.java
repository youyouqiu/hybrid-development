package com.zw.api.controller.modules;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api.config.ResponseUntil;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentForm;
import com.zw.platform.domain.basicinfo.form.AssignmentGroupForm;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * <p> Title: 分组管理controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年10月9日下午6:16:57
 */
@Controller
@RequestMapping("/swagger/m/assignment")
@Api(tags = { "分组管理" }, description = "分组相关api接口")
public class SwaggerAssignmentController {
    private static Logger log = LogManager.getLogger(SwaggerAssignmentController.class);

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 分页查询
     */
    @Auth
    @ApiOperation(value = "分页查询分组列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "查询某个组织下的分组，组织id", paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final AssignmentQuery query) {
        try {
            // 校验传入字段
            if (query != null) {
                if (query.getPage() == null || query.getLimit() == null) { // page和limit不能为空
                    return new PageGridBean(PageGridBean.FAULT);
                }
                // 模糊搜索长度小于20
                if (StringUtils.isNotBlank(query.getSimpleQueryParam()) && query.getSimpleQueryParam().length() > 20) {
                    return new PageGridBean(PageGridBean.FAULT);
                }

                Page<Assignment> result = (Page<Assignment>) assignmentService.findAssignment(query);
                return new PageGridBean(query, result, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("分页查询分组信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @ApiOperation(value = "新增分组", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "分组名称，同一企业下分组名称不能相同", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属企业id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "contacts", value = "联系人", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "telephone", value = "电话号码", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "描述", paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addProfessionals(@Validated({ ValidGroupAdd.class }) final AssignmentForm form,
        @RequestParam("groupId") final String groupId, final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                //获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 数据校验
                try {
                    if (userService.getOrgByUuid(groupId) == null) { // 所属企业是否存在
                        return new JsonResultBean(JsonResultBean.FAULT, "所属企业不存在！");
                    }
                } catch (Exception e) {
                    return new JsonResultBean(JsonResultBean.FAULT, "所属企业不存在！");
                }
                List<Assignment> assign = assignmentService.findByNameForOneOrg(form.getName(), groupId);
                if (assign != null && !assign.isEmpty()) { // 同一企业下分组名称不能相同
                    return new JsonResultBean(JsonResultBean.FAULT, "当前组织下该分组已存在！");
                }
                if (StringUtils.isNotBlank(form.getTelephone()) && !RegexUtils.checkMobile(form.getTelephone())
                    && !RegexUtils.checkPhone(form.getTelephone())) { // 校验电话号码格式
                    return new JsonResultBean(JsonResultBean.FAULT, "电话号码必须为手机/电话！");
                }

                AssignmentGroupForm assignmentGroupForm = new AssignmentGroupForm();
                // 组装关联表
                assignmentGroupForm.setAssignmentId(form.getId());
                assignmentGroupForm.setGroupId(groupId);
                // // 同时修改分组和人的关联
                // boolean flag = vehicleService.updateUserAssignByUser(form.getId(),permissionTree);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            log.error("新增分组信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改车辆
     */
    @ApiOperation(value = "根据分组id查询分组详细信息", notes = "用于修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            Assignment assignment = assignmentService.findAssignmentById(id);
            return new JsonResultBean(assignment);
        } catch (Exception e) {
            log.error("查询分组详细信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "修改分组", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "分组id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "name", value = "分组名称，同一企业下分组名称不能相同", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "contacts", value = "联系人", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "telephone", value = "电话号码", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "描述", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) final AssignmentForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                //获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 数据校验
                    Assignment ass = assignmentService.findAssignmentById(form.getId());
                    if (ass == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, "分组不存在！");
                    }
                    List<Assignment> assign =
                        assignmentService.findOneOrgAssiForNameRep(form.getId(), form.getName(), ass.getGroupId());
                    if (assign != null && !assign.isEmpty()) { // 同一企业下分组名称不能相同
                        return new JsonResultBean(JsonResultBean.FAULT, "当前组织下该分组已存在！");
                    }
                    if (StringUtils.isNotBlank(form.getTelephone()) && !RegexUtils.checkMobile(form.getTelephone())
                        && !RegexUtils.checkPhone(form.getTelephone())) { // 校验电话号码格式
                        return new JsonResultBean(JsonResultBean.FAULT, "电话号码必须为手机/电话！");
                    }
                    // return assignmentService.updateAssignment(form, ipAddress);
                    return null;
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改分组信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除 分组
     */
    @ApiOperation(value = "根据id删除 分组", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            boolean flag = false;
            //获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // 校验分组是否存在
            if (assignmentService.findAssignmentById(id) == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "分组不存在！");
            }
            // 根据分组id查询车
            List<VehicleInfo> vehicleList = assignmentService.findVehicleByAssignmentId(id);
            if (vehicleList != null && vehicleList.size() > 0) { // 分组中存在车辆，不能删除
                return new JsonResultBean(JsonResultBean.FAULT, "该分组里存在车辆，不能删除！");
            } else { // 删除分组
                // flag = assignmentService.deleteAssignment(id, ipAddress);
            }
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }

        } catch (Exception e) {
            log.error("删除分组信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除分组", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的分组ids(用逗号隔开)", required = true, paramType = "query",
        dataType = "Stirng")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            boolean flag = false;
            //获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            List<String> ids = Arrays.asList(item);
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                // 校验分组是否存在
                if (assignmentService.findAssignmentById(id) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的分组！");
                }
                // 根据分组id查询车
                List<VehicleInfo> vehicleList = assignmentService.findVehicleByAssignmentId(id);
                if (vehicleList != null && vehicleList.size() > 0) { // 分组中存在车辆，不能删除
                    return new JsonResultBean(JsonResultBean.FAULT, "分组里存在车辆，不能删除！");
                }
            }
            // 批量删除
            // flag = assignmentService.deleteAssignmentByBatch(ids, ipAddress);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量删除分组信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
         * @param response
     * @return String
     * @Title: 分组树
     * @author wangying
     */
    @ApiOperation(value = "获取分组权限的树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "isOrg", value = "是否显示最顶级组织（0：不显示，用户新增编辑页面； 1：显示，用户显示页面）", required = true,
        paramType = "query", dataType = "Stirng", defaultValue = "0")
    @RequestMapping(value = "/assignmentTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAssignmentTree(String isOrg, HttpServletResponse response) {
        try {
            ResponseUntil.setResponseHeader(response); // 解决跨域问题
            // 校验参数值
            if (!"0".equals(isOrg) && !"1".equals(isOrg)) {
                return new JsonResultBean(JsonResultBean.FAULT, "传入参数值错误，只能为1或者0！");
            }
            JSONArray treeList = assignmentService.getAssignmentTree();
            return new JsonResultBean(treeList);
        } catch (Exception e) {
            log.error("分组树查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
         * @param id
     * @return String
     * @Title: 分组树(可选)
     * @author wangying
     */
    @ApiOperation(value = "根据userId查询可为该用户分配的分组权限树", notes = "用于编辑(参数id为要分配的userId)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/editAssignmentTree_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getEditAssignmentTree(@PathVariable String id) {
        try {
            // 校验用户是否存在
            try {
                if (userService.findUser(id) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "用户不存在！");
                }
            } catch (Exception e) {
                return new JsonResultBean(JsonResultBean.FAULT, "用户不存在！");
            }
            // JSONArray treeList = assignmentService.getEditAssignmentTree(id, null);
            return new JsonResultBean("treeList");
        } catch (Exception e) {
            log.error("查询可为该用户分配的分组权限树异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
         * @param id
     * @return String
     * @Title: 分配人
     * @author wangying
     */
    @ApiOperation(value = "根据分组id获取分组所属组织及上级组织下用户的树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/assignmentPer_{id}.gsp", method = GET)
    public JsonResultBean getVehiclePer(@PathVariable String id) {
        try {
            // 校验分组是否存在
            if (assignmentService.findAssignmentById(id) == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "分组不存在！");
            }
            // UserBean user = userService.findUser(id);
            JSONObject obj = new JSONObject();
            // 用户tree
            // String tree = assignmentService.getAssignMonitorUserTree(id);
            obj.put("assignmentId", id);
            obj.put("userTree", "tree");
            return new JsonResultBean(obj);
        } catch (Exception e) {
            log.error("获取分组所属组织及上级组织下用户的树结构异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "修改用户的分组权限", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "assignmentId", value = "分组id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "userVehicleList", value = "所选用户id集合，用分号(;)隔开", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/assignmentPer.gsp", method = POST)
    @ResponseBody
    public JsonResultBean saveVehiclePer(@RequestParam("assignmentId") final String assignmentId,
        @RequestParam("userVehicleList") final String userVehicleList) {
        try {
            boolean flag = false;
            //获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // 数据校验
            if (StringUtils.isBlank(assignmentId) || StringUtils.isBlank(userVehicleList)) { // 必填参数
                return new JsonResultBean(JsonResultBean.FAULT, "请填写参数assignmentId和userVehicleList！");
            }
            if (assignmentService.findAssignmentById(assignmentId) == null) { // 分组是否存在
                return new JsonResultBean(JsonResultBean.FAULT, "分组不存在！");
            }
            // 用户是否存在
            List<String> userList = Arrays.asList(userVehicleList.split(";"));
            if (!userList.isEmpty()) {
                for (String id : userList) {
                    try {
                        if (userService.getUserByUuid(id) == null) {
                            return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的用户！");
                        }
                    } catch (Exception e) {
                        return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的用户！");
                    }
                }
            }
            // flag = vehicleService.updateUserAssignByUser(assignmentId, userVehicleList, ipAddress);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("修改用户分组权限异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
         * @param id
     * @return String
     * @Title: 移除车
     * @author wangying
     */
    @ApiOperation(value = "根据分组id查询该分组下的车辆树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/vehiclePer_{id}.gsp", method = GET)
    public JsonResultBean getRemoveVehiclePer(@PathVariable String id) {
        try {
            JSONObject object = new JSONObject();
            // 车辆tree
            JSONArray userTreeData = assignmentService.getMonitorByAssignmentID(id);
            object.put("assignmentId", id);
            object.put("vehicleTree", userTreeData);
            return new JsonResultBean(object);
        } catch (Exception e) {
            log.error("查询该分组下的车辆树结构异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "移除分组中的车辆", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "assignmentId", value = "分组id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "userVehicleList", value = "所选车辆id集合，用分号(;)隔开", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/removeVehiclePer.gsp", method = POST)
    @ResponseBody
    public JsonResultBean removeVehicle(@RequestParam("assignmentId") final String assignmentId,
        @RequestParam("userVehicleList") final String userVehicleList) {
        try {
            boolean flag = false;
            flag = assignmentService.removeAssignmentVehicle(assignmentId, userVehicleList);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("移除分组中的车辆异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "查询同一组织下是否存在相同名称的分组", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "分组名称", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "group", value = "分组所属组织id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean repetition(@RequestParam("name") String name, @RequestParam("group") String group) {
        try {
            // 校验组织是否存在
            try {
                if (userService.getOrgByUuid(group) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                }
            } catch (Exception e) {
                return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
            }
            List<Assignment> assign = assignmentService.findByNameForOneOrg(name, group);
            if (assign == null || assign.isEmpty()) {
                return new JsonResultBean(JsonResultBean.SUCCESS, "该组织下不存在相同名称的分组！");
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "该组织下已存在相同名称的分组！");
            }
        } catch (Exception e) {
            log.error("校验分组存在异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }
}
