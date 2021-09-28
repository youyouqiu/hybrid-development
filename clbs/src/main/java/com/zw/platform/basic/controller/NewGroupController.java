package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.basic.dto.OrganizationGroupDO;
import com.zw.platform.basic.dto.query.GroupPageQuery;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.form.AssignmentForm;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm;
import com.zw.platform.util.MagicNumbers;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.imports.ZwImportException;
import com.zw.platform.util.imports.lock.ImportModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author wanxing
 * @Title: 分组管理controller
 * @date 2020/12/1717:55
 */
@Controller
@RequestMapping("/m/basicinfo/enterprise/assignment")
public class NewGroupController {

    private static final Logger log = LogManager.getLogger(NewGroupController.class);

    private static final String LIST_PAGE = "modules/basicinfo/enterprise/assignment/list";

    private static final String ADD_PAGE = "modules/basicinfo/enterprise/assignment/add";

    private static final String EDIT_PAGE = "modules/basicinfo/enterprise/assignment/edit";

    private static final String GROUP_PAGE = "modules/basicinfo/enterprise/assignment/assignmentPer";

    private static final String DISTRIBUTION_VEHICLE_PAGE = "modules/basicinfo/enterprise/assignment/vehiclePer";

    private static final String IMPORT_PAGE = "modules/basicinfo/enterprise/assignment/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private GroupService groupService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private GroupMonitorService groupMonitorService;

    @Autowired
    private UserGroupService userGroupService;

    @Value("${sys.error.msg}")
    private String syError;

    @Autowired
    private UserService userService;

    /**
     * 添加分组页面
     * @param uuid uuid
     * @return String
     * @author wangying
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public ModelAndView add(@RequestParam("uuid") String uuid) {

        if (uuid == null) {
            return new ModelAndView(ERROR_PAGE);
        }
        ModelAndView mav = new ModelAndView(ADD_PAGE);
        if ("".equals(uuid)) {
            return mav;
        }
        try {
            OrganizationLdap organization = organizationService.getOrganizationByUuid(uuid);
            if (organization == null) {
                log.error("传入的企业UUID：{}非法", uuid);
                return new ModelAndView(ERROR_PAGE);
            }
            mav.addObject("orgId", uuid);
            mav.addObject("groupName", organization.getName());
            return mav;
        } catch (Exception e) {
            log.error("新增分组弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * @param form          form
     * @param bindingResult bindingResult
     * @return result
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) AssignmentForm form,
        final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        GroupDO groupDO = new GroupDO();
        groupDO.setId(UUID.randomUUID().toString());
        groupDO.setDescription(form.getDescription());
        groupDO.setName(form.getName());
        groupDO.setTelephone(form.getTelephone());
        groupDO.setContacts(form.getContacts());
        groupDO.setOrgId(form.getGroupId());
        groupDO.setCreateDataUsername(SystemHelper.getCurrentUsername());

        OrganizationGroupDO organizationGroupDO = new OrganizationGroupDO();
        organizationGroupDO.setOrgId(form.getGroupId());
        organizationGroupDO.setGroupId(groupDO.getId());
        organizationGroupDO.setCreateDataUser(SystemHelper.getCurrentUsername());
        try {
            groupService.add(groupDO, organizationGroupDO);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("新增分组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

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
     * 修改分组
     * @param id id
     * @return page
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            GroupDTO groupDTO = groupService.getById(id);
            Assignment assignment = groupDTO.translate();
            mav.addObject("result", assignment);
            return mav;
        } catch (Exception e) {
            log.error("修改分组弹出页面异常", e);
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
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) AssignmentForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            GroupDTO groupDTO = form.translate();
            groupService.update(groupDTO);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("修改分组异常", e);
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * getTreeNodeCounts
     * @param id id
     * @return result
     */
    @RequestMapping(value = "/vehiclePer.gsp/count", method = RequestMethod.POST)
    @ResponseBody
    public int getCurrentUserMonitorCount(@RequestParam("aid") final String id) {
        try {
            return groupService.getCurrentUserMonitorCount(id);
        } catch (Exception e) {
            log.error("监控对象树查询异常", e);
            return 0;
        }
    }

    @RequestMapping(value = "/vehiclePer.gsp/org", method = RequestMethod.POST)
    @ResponseBody
    public String getAssignmentTreeParentNodes(@RequestParam("aid") final String groupId,
        @RequestParam("queryParam") final String queryParam, @RequestParam("queryType") final String queryType) {
        try {
            JSONArray result = groupMonitorService.getGroupMonitorTree(null, groupId, queryParam, queryType);
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
     * 分组树
     * @return String
     * @author wangying
     */
    @RequestMapping(value = "/assignmentTree", method = RequestMethod.POST)
    @ResponseBody
    public String getAssignmentTree() {
        try {
            JSONArray treeList = userService.getCurrentGroupTree();
            return treeList.toJSONString();
        } catch (Exception e) {
            log.error("分组树查询异常", e);
            return null;
        }
    }

    /**
     * getAllAssignmentTreeData
     * @param groupId    groupId
     * @param queryParam queryParam
     * @param queryType  queryType
     * @return result
     */
    @RequestMapping(value = "/vehiclePer.gsp/all", method = RequestMethod.POST)
    @ResponseBody
    public String getAllAssignmentTreeData(@RequestParam("aid") final String groupId,
        @RequestParam("queryParam") final String queryParam, @RequestParam("queryType") final String queryType) {
        try {
            JSONArray result = groupMonitorService.getGroupMonitorTree("multiple", groupId, queryParam, queryType);
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
     * 移除车
     * @param id 分组ID
     * @return String
     * @author wangying
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/vehiclePer_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView distributionMonitorPage(@PathVariable String id) {

        ModelAndView mav = new ModelAndView(DISTRIBUTION_VEHICLE_PAGE);
        if (StringUtils.isEmpty(id)) {
            return mav;
        }
        try {
            // 除去当前分组的分组车辆tree
            Map<String, String> resultMap = groupMonitorService.getGroupMonitorTreeByGroupId(id);
            String groupMonitorTree = resultMap.get("groupMonitorTree");
            mav.addObject("assignmentId", id);
            mav.addObject("userTree", groupMonitorTree);
            mav.addObject("groupName", "分配监控对象：" + resultMap.get("groupName") + "");
            return mav;
        } catch (Exception e) {
            log.error("移除车辆异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 根据id删除 分组
     * @param id id
     * @return result
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            String errMsg = groupService.delete(id);
            if (StringUtils.isNotEmpty(errMsg)) {
                return new JsonResultBean(JsonResultBean.FAULT, errMsg);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("根据id删除分组异常", e);
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * saveVehiclePer
     * @param groupId          分组Id
     * @param vehicleIdAddList vehicleIdAddList
     * @param vehicleIdDelList vehicleIdDelList
     * @return result
     */
    // @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/vehiclePer.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveVehiclePer(@RequestParam("assignmentId") final String groupId,
        @RequestParam("vehiclePerAddList") final String vehicleIdAddList,
        @RequestParam("vehiclePerDeleteList") final String vehicleIdDelList) {

        if (StringUtils.isEmpty(groupId)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        boolean flag1 = StringUtils.isEmpty(vehicleIdAddList) || "[]".equals(vehicleIdAddList);
        boolean flag2 = StringUtils.isEmpty(vehicleIdDelList) || "[]".equals(vehicleIdDelList);
        if (flag1 && flag2) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        List<GroupMonitorDTO> addList = new ArrayList<>();
        List<GroupMonitorDTO> delList = new ArrayList<>();
        try {
            if (!flag1) {
                List<AssignmentVehicleForm> vehiclePerAddList =
                    JSON.parseArray(vehicleIdAddList, AssignmentVehicleForm.class);
                for (AssignmentVehicleForm ass : vehiclePerAddList) {
                    addList.add(GroupMonitorDTO.translate(ass));
                }
            }
            if (!flag2) {
                List<AssignmentVehicleForm> vehiclePerDeleteList =
                    JSON.parseArray(vehicleIdDelList, AssignmentVehicleForm.class);
                for (AssignmentVehicleForm ass : vehiclePerDeleteList) {
                    delList.add(GroupMonitorDTO.translate(ass));
                }
            }
            groupMonitorService.updateMonitorGroup(addList, delList, groupId);
            //@TODO wanxing 未同步对讲功能
        } catch (Exception e) {
            log.error("分配监控对象异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 批量删除
     * @return result
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String groupIds = request.getParameter("deltems");
            if (StringUtils.isEmpty(groupIds)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数错误");
            }
            List<String> ids = Arrays.asList(groupIds.split(","));
            String errMsg = groupService.deleteBatch(ids);
            if (StringUtils.isNotEmpty(errMsg)) {
                return new JsonResultBean(JsonResultBean.FAULT, errMsg);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("批量删除分组异常", e);
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 分页查询
     * @param query query
     * @return result
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(AssignmentQuery query) {
        try {
            Page<GroupDTO> result = groupService.getPageByKeyword(GroupPageQuery.transform(query));
            Page<Assignment> page = PageHelperUtil.copyPage(result);
            for (GroupDTO groupDTO : result) {
                page.add(groupDTO.translate());
            }
            return new PageGridBean(query, page, true);
        } catch (Exception e) {
            log.error("分页查询分组（findAssignment）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * assignCountLimit
     * @param orgId group
     * @return boolean
     */
    @RequestMapping(value = "/assignCountLimit", method = RequestMethod.POST)
    @ResponseBody
    public boolean assignCountLimit(@RequestParam("group") String orgId) {
        try {
            List<String> groupIds = groupService.getGroupIdsByOrgId(orgId);
            return groupIds.size() < MagicNumbers.INT_HUNDRED;
        } catch (Exception e) {
            log.error("校验分组存在异常", e);
            return false;
        }
    }

    /**
     * repetition
     * @param name         name
     * @param orgId        orgId
     * @param assignmentId 分组Id
     * @return boolean
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("name") String name, @RequestParam("group") String orgId,
        String assignmentId) {
        try {
            return groupService.checkNameExist(name, orgId, assignmentId);
        } catch (Exception e) {
            log.error("校验分组存在异常", e);
            return false;
        }
    }

    /**
     * 导出
     * @param response response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response, AssignmentQuery query) {
        try {
            ExportExcelUtil.setResponseHead(response, "分组列表");
            groupService.export(null, 1, response, query);
        } catch (Exception e) {
            log.error("导出分组列表异常", e);
        }
    }

    /**
     * 下载模板
     * @param response response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "分组列表模板");
            groupService.exportTemplate(response);
        } catch (Exception e) {
            log.error("下载分组列表模板异常", e);
        }
    }

    /**
     * 分配监控人员页面
     * @param id 分组ID
     * @return String
     * @author wangying
     */
    @RequestMapping(value = "/assignmentPer_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView getDistributionPage(@PathVariable String id) {
        try {
            ModelAndView mav = new ModelAndView(GROUP_PAGE);
            String tree = userGroupService.getUserGroupTree(id);
            GroupDTO groupDTO = groupService.getById(id);
            String name = "分配监控人员：" + groupDTO.getName() + "";
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
     * 分配分组给用户
     * @param groupId groupId
     * @param userDn  userDn
     * @return result
     */
    @RequestMapping(value = "/assignmentPer.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addGroup2User(@RequestParam("assignmentId") String groupId,
        @RequestParam("userVehicleList") String userDn) {

        if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(userDn)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误");
        }
        try {
            userGroupService.addGroup2User(groupId, userDn);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("分配监控人员异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 导入
     * @param file 文件
     * @return result
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importAssignment(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            return groupService.importGroup(file);
        } catch (Exception e) {
            log.error("导入分组信息异常", e);
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, syError);
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

    @RequestMapping(value = "/exportError", method = RequestMethod.GET)
    public void exportDeviceError(HttpServletResponse response) {
        try {
            ImportErrorUtil.generateErrorExcel(ImportModule.ASSIGNMENT, "分组导入错误信息", null, response);
        } catch (Exception e) {
            log.error("导出终端错误信息异常", e);
        }
    }
}
