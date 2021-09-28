package com.zw.talkback.controller.basicinfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentGroupForm;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.MagicNumbers;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.form.AssignmentVehicleForm;
import com.zw.talkback.domain.basicinfo.form.ClusterForm;
import com.zw.talkback.domain.basicinfo.query.AssignmentQuery;
import com.zw.talkback.service.baseinfo.ClusterService;
import com.zw.talkback.util.common.JsonResultBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 群组管理controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team: ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 */
@Controller
@RequestMapping("/talkback/basicinfo/enterprise/assignment")
public class ClusterController {
    @Autowired
    private ClusterService clusterService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Value("${sys.error.msg}")
    private String syError;

    private static final Logger log = LogManager.getLogger(ClusterController.class);

    private static final String LIST_PAGE = "talkback/basicinfo/enterprise/assignment/list";

    private static final String ADD_PAGE = "talkback/basicinfo/enterprise/assignment/add";

    private static final String EDIT_PAGE = "talkback/basicinfo/enterprise/assignment/edit";

    private static final String ASSIGNMENT_PAGE = "talkback/basicinfo/enterprise/assignment/assignmentPer";

    private static final String REMOVE_VEHICLE_PAGE = "talkback/basicinfo/enterprise/assignment/vehiclePer";

    private static final String IMPORT_PAGE = "talkback/basicinfo/enterprise/assignment/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String REQUEST_TOKEN = "avoidRepeatSubmitToken";

    @Autowired
    private HttpServletRequest request;

    /**
     * listPage
     * @return page
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     * @param query query
     * @return result
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final AssignmentQuery query) {
        try {
            Page<Cluster> result = (Page<Cluster>) clusterService.findAssignment(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询群组（findAssignment）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 添加群组页面
     * @param uuid uuid
     * @return String
     * @author wangying
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView initNewUser(@RequestParam("uuid") String uuid) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            String user = userService.getOrgUuidByUser();
            if (uuid.equals(user)) {
                uuid = "";
            }
            if (!"".equals(uuid)) {
                OrganizationLdap organization = userService.getOrgByUuid(uuid);
                mav.addObject("orgId", uuid);
                mav.addObject("groupName", organization.getName());
            }
            return mav;
        } catch (Exception e) {
            log.error("新增群组弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * addProfessionals
     * @param form          form
     * @param groupId       groupId
     * @param bindingResult bindingResult
     * @return result
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addProfessionals(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final ClusterForm form,
        @RequestParam("groupId") final String groupId, final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 获取操作用户的IP
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    //查询所有
                    List<ClusterForm> clusterList = clusterService.findAll();

                    //拿到名字
                    List<String> collect = clusterList.stream().map(x -> x.getName()).collect(Collectors.toList());
                    if (collect.contains(form.getName())) {
                        clusterList.stream().filter(x -> x.getName().equals(form.getName()));
                        for (ClusterForm clusterForm : clusterList) {

                            //说明重复情况
                            if (clusterForm.getName().equals(form.getName())) {

                                /*if (clusterForm.getIntercomGroupId() != null) {*/
                                if (clusterForm.getTypes() == (short) 1) {

                                    return new JsonResultBean(JsonResultBean.FAULT, "该群组重名！");
                                } else {
                                    //分组中的补全

                                    String id = clusterForm.getId();
                                    AssignmentGroupForm groupForm = clusterService.getGroupForm(id);
                                    if (groupForm.getGroupId().equals(groupId)) {
                                        AssignmentGroupForm assignmentGroupForm = new AssignmentGroupForm();
                                        // 组装关联表
                                        assignmentGroupForm.setAssignmentId(id);
                                        // assignmentGroupForm.setGroupId(groupForm.getGroupId());
                                        //assignmentGroupForm.setGroupId(groupId);

                                        //clusterForm.setGroupId(groupId);
                                        clusterForm.setDescription(form.getDescription());
                                        clusterForm.setSoundRecording(form.getSoundRecording());
                                        clusterForm.setTelephone(form.getTelephone());
                                        clusterForm.setContacts(form.getContacts());
                                        return clusterService
                                            .addAssignmentAndPermission(clusterForm, assignmentGroupForm, ipAddress,
                                                false);
                                    } else {

                                        AssignmentGroupForm assignmentGroupForm = new AssignmentGroupForm();
                                        // 组装关联表
                                        assignmentGroupForm.setAssignmentId(form.getId());
                                        assignmentGroupForm.setGroupId(groupId);
                                        return clusterService
                                            .addAssignmentAndPermission(form, assignmentGroupForm, ipAddress, true);

                                    }

                                }
                            }
                        }
                    }
                    AssignmentGroupForm assignmentGroupForm = new AssignmentGroupForm();
                    // 组装关联表
                    assignmentGroupForm.setAssignmentId(form.getId());
                    assignmentGroupForm.setGroupId(groupId);
                    return clusterService.addAssignmentAndPermission(form, assignmentGroupForm, ipAddress, true);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增群组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改群组
     * @param id id
     * @return page
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            Cluster cluster = clusterService.findAssignmentById(id);
            mav.addObject("result", cluster);
            return mav;
        } catch (Exception e) {
            log.error("修改群组弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * edit
     * @param form          form
     * @param bindingResult bindingResult
     * @return result
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final ClusterForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    return clusterService.updateAssignment(form, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改群组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除 群组
     * @param id id
     * @return result
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // 根据群组id查询车
            List<VehicleInfo> vehicleList = clusterService.findVehicleByAssignmentId(id);
            if (vehicleList != null && vehicleList.size() > 0) {
                // 群组中存在车辆，不能删除
                return new JsonResultBean(JsonResultBean.FAULT, "该群组里存在监控对象，不能删除！");
            }
            return clusterService.deleteAssignment(id, ipAddress);
        } catch (Exception e) {
            log.error("根据id删除群组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     * @return result
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            List<String> ids = Arrays.asList(item);
            for (String id : ids) {
                // 根据群组id查询车
                List<VehicleInfo> vehicleList = clusterService.findVehicleByAssignmentId(id);
                if (vehicleList != null && vehicleList.size() > 0) {
                    // 群组中存在车辆，不能删除
                    return new JsonResultBean(JsonResultBean.FAULT, "群组里存在监控对象，不能删除！");
                }
            }
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // 批量删除
            return clusterService.deleteAssignmentByBatch(ids, ipAddress);
        } catch (Exception e) {
            log.error("批量删除群组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 群组树
     * @param isOrg isOrg
     * @return String
     * @author wangying
     */
    @RequestMapping(value = "/assignmentTree", method = RequestMethod.POST)
    @ResponseBody
    public String getAssignmentTree(String isOrg) {
        try {
            JSONArray treeList = clusterService.getAssignmentTree();
            return treeList.toJSONString();
        } catch (Exception e) {
            log.error("群组树查询异常", e);
            return null;
        }
    }

    /**
     * 群组树(可选)
     * @param id id
     * @return String
     * @author wangying
     */
    @RequestMapping(value = "/editAssignmentTree_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public String getEditAssignmentTree(@PathVariable String id) {
        try {
            JSONArray treeList = clusterService.getEditAssignmentTree(id);
            return treeList.toJSONString();
        } catch (Exception e) {
            log.error("群组树查询异常", e);
            return null;
        }
    }

    /**
     * 分配监控人员
     * @param id 群组ID
     * @return String
     * @author wangying
     */
    @RequestMapping(value = "/assignmentPer_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView getVehiclePer(@PathVariable String id) {
        try {
            ModelAndView mav = new ModelAndView(ASSIGNMENT_PAGE);
            String tree = clusterService.getAssignMonitorUserTree(id);
            Cluster assign = clusterService.findAssignmentById(id);
            String name = "分配监控人员：" + assign.getName() + "";
            mav.addObject("assignmentId", id);
            mav.addObject("userTree", tree);
            mav.addObject("groupName", name);
            return mav;
        } catch (Exception e) {
            log.error("分配监控人员界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * saveVehiclePer
     * @param assignmentId    assignmentId
     * @param userVehicleList userVehicleList
     * @return result
     */
    @RequestMapping(value = "/assignmentPer.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveVehiclePer(@RequestParam("assignmentId") final String assignmentId,
        @RequestParam("userVehicleList") final String userVehicleList) {
        try {
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            boolean flag = vehicleService.updateUserAssignByUser(assignmentId, userVehicleList, ipAddress);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("分配监控人员异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 移除车
     * @param id 群组ID
     * @return String
     * @author wangying
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/vehiclePer_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView getRemoveVehiclePer(@PathVariable String id) {
        try {
            ModelAndView mav = new ModelAndView(REMOVE_VEHICLE_PAGE);
            // 除去当前群组的群组车辆tree
            String userTreeData = clusterService.getMonitorByAssignmentID(id).toJSONString();
            Cluster assign = clusterService.findAssignmentById(id);
            int leaveJobPeopleNum = clusterService.countLeaveJobPeopleNum(id);
            String name = "分配监控对象：" + assign.getName() + "";
            mav.addObject("assignmentId", id);
            mav.addObject("userTree", userTreeData);
            mav.addObject("groupName", name);
            mav.addObject("leaveJobPeopleNum", leaveJobPeopleNum);
            return mav;
        } catch (Exception e) {
            log.error("移除车辆异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * getTreeNodeCounts
     * @param id id
     * @return result
     */
    @RequestMapping(value = "/vehiclePer.gsp/count", method = RequestMethod.POST)
    @ResponseBody
    public int getTreeNodeCounts(@RequestParam("aid") final String id) {
        try {
            return clusterService.countMonitors(id);
        } catch (Exception e) {
            log.error("监控对象树查询异常", e);
            return 0;
        }
    }

    /**
     * 获得群组下离职人员数量
     * @param assignmentIds
     * @return
     */
    @RequestMapping(value = "/getLeaveJobPeopleNum", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getLeaveJobPeopleNum(@RequestParam("assignmentIds") final String assignmentIds) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            if (StringUtils.isNotBlank(assignmentIds)) {
                List<String> assignmentIdList = Arrays.asList(assignmentIds.split(","));
                for (String assignmentId : assignmentIdList) {
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("assignmentId", assignmentId);
                    map.put("leaveJobPeopleNum", clusterService.countLeaveJobPeopleNum(assignmentId));
                    result.add(map);
                }
            }
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获得群组下离职人员数量异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * getAllAssignmentTreeData
     * @param id id
     * @return result
     */
    @RequestMapping(value = "/vehiclePer.gsp/all", method = RequestMethod.POST)
    @ResponseBody
    public String getAllAssignmentTreeData(@RequestParam("aid") final String id,
        @RequestParam("queryParam") final String queryParam, @RequestParam("queryType") final String queryType) {
        try {
            JSONArray result = clusterService.vehicleTreeForAssign("multiple", id, queryParam, queryType);
            if (result.isEmpty()) {
                return "";
            }
            return ZipUtil.compress(result.toJSONString());
        } catch (Exception e) {
            log.error("监控对象树查询异常", e);
            return null;
        }
    }

    /**
     * getAssignmentTreeParentNodes
     * @param id id
     * @return result
     */
    @RequestMapping(value = "/vehiclePer.gsp/org", method = RequestMethod.POST)
    @ResponseBody
    public String getAssignmentTreeParentNodes(@RequestParam("aid") final String id,
        @RequestParam("queryParam") final String queryParam, @RequestParam("queryType") final String queryType) {
        try {
            JSONArray result = clusterService.listMonitorTreeParentNodes(id, queryParam, queryType);
            if (result.isEmpty()) {
                return "";
            }
            return ZipUtil.compress(result.toJSONString());
        } catch (Exception e) {
            log.error("监控对象树查询异常", e);
            return null;
        }
    }

    /**
     * getVehicleListByAssignmentId
     * @param id id
     * @return result
     */
    @RequestMapping(value = "/vehiclePer.gsp/vehicles", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleListByAssignmentId(@RequestParam("aid") String id) {
        try {
            JSONArray result = clusterService.listMonitorsByAssignmentID(id);
            return ZipUtil.compress(result.toJSONString());
        } catch (Exception e) {
            log.error("监控对象树查询异常", e);
            return null;
        }
    }

    /**
     * saveVehiclePer
     * @param assignmentId     assignmentId
     * @param vehiclePerAdd    vehiclePerAdd
     * @param vehiclePerDelete vehiclePerDelete
     * @return result
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/vehiclePer.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveVehiclePer(@RequestParam("assignmentId") final String assignmentId,
        @RequestParam("vehiclePerAddList") final String vehiclePerAdd,
        @RequestParam("vehiclePerDeleteList") final String vehiclePerDelete) {
        try {
            List<AssignmentVehicleForm> vehiclePerAddList = new ArrayList<>();
            List<AssignmentVehicleForm> vehiclePerDeleteList = new ArrayList<>();
            if (StringUtils.isNotBlank(vehiclePerAdd) && !"[]".equals(vehiclePerAdd)) {
                vehiclePerAddList = JSON.parseArray(vehiclePerAdd, AssignmentVehicleForm.class);
            }
            if (StringUtils.isNotBlank(vehiclePerDelete) && !"[]".equals(vehiclePerDelete)) {
                vehiclePerDeleteList = JSON.parseArray(vehiclePerDelete, AssignmentVehicleForm.class);
            }
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            JsonResultBean result =
                clusterService.saveVehiclePer(vehiclePerAddList, vehiclePerDeleteList, assignmentId, ipAddress);
            if (!result.isSuccess()) {
                // 异常是返回token
                String token = UUID.randomUUID().toString();
                JSONObject object = new JSONObject();
                object.put("token", token);
                request.getSession(false).setAttribute(REQUEST_TOKEN, token);
                result.setObd(object);
            }
            return result;
        } catch (Exception e) {
            log.error("分配监控对象异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * repetition
     * @param name  name
     * @param group group
     * @return boolean
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("name") String name, @RequestParam("group") String group,
        String assignmentId) {
        try {
            List<Cluster> assign = clusterService.findByNameForOne(name);
            if (assignmentId == null || "".equals(assignmentId)) {
                //新增
                return assign == null || assign.isEmpty();
            } else {
                //编辑
                if (CollectionUtils.isNotEmpty(assign)) {
                    for (Cluster cluster : assign) {
                        if (!assignmentId.equals(cluster.getId())) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } catch (Exception e) {
            log.error("校验群组存在异常", e);
            return false;
        }
    }

    /**
     * assignCountLimit
     * @param group group
     * @return boolean
     */
    @RequestMapping(value = "/assignCountLimit", method = RequestMethod.POST)
    @ResponseBody
    public boolean assignCountLimit(@RequestParam("group") String group) {
        try {
            List<Cluster> assign = clusterService.findAssignmentByGroupId(group);
            return assign == null || assign.size() < MagicNumbers.INT_HUNDRED;
        } catch (Exception e) {
            log.error("校验群组存在异常", e);
            return false;
        }
    }

    /**
     * assignCountLimitForEdit
     * @param group        group
     * @param assignmentId assignmentId
     * @return boolean
     */
    @RequestMapping(value = "/assignCountLimitForEdit", method = RequestMethod.POST)
    @ResponseBody
    public boolean assignCountLimitForEdit(@RequestParam("group") String group,
        @RequestParam("assignmentId") String assignmentId) {
        try {
            List<Cluster> assign = clusterService.findAssignByGroupIdExpectVehicle(group, assignmentId);
            return assign == null || assign.size() < MagicNumbers.INT_HUNDRED;
        } catch (Exception e) {
            log.error("校验群组存在异常", e);
            return false;
        }
    }

    /**
     * 下载模板
     * @param response response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "群组列表模板");
            clusterService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载群组列表模板异常", e);
        }
    }

    /**
     * 导入
     * @return String
     * @author wangying
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * 导入
     * @param file        文件
     * @param httpRequest request
     * @return result
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importAssignment(@RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest httpRequest) {
        try {
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(httpRequest);
            Map resultMap = clusterService.importAssignment(file, ipAddress, httpRequest.getSession());
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入群组信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 导出
     * @param response response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "群组列表");
            clusterService.exportAssignment(null, 1, response);
        } catch (Exception e) {
            log.error("导出群组列表异常", e);
        }
    }

    /**
     * 获取监控对象所在群组总数
     * @param id
     * @return
     */
    @RequestMapping(value = "/getAssignmentNumberOfMonitor", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAssignmentNumberOfMonitor(String id) {
        try {
            if (StringUtils.isEmpty(id)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            return clusterService.getAssignmentNumberOfMonitor(id);
        } catch (Exception e) {
            log.error("获取监控对象所在群组总数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 群组录音开关
     * @return
     */
    @RequestMapping(value = "/changeRecordingSwitch", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean changeRecordingSwitch(String assignmentId, Integer flag) {
        try {
            if (StringUtils.isEmpty(assignmentId) || flag == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            String currentUsername = SystemHelper.getCurrentUsername();
            if (!"admin".equals(currentUsername)) {
                return new JsonResultBean(JsonResultBean.FAULT, "admin用户才能修改录音状态");
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return clusterService.changeRecordingSwitch(ipAddress, assignmentId, flag);
        } catch (Exception e) {
            log.error("群组录音开关异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }
}
