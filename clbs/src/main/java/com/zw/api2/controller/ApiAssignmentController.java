package com.zw.api2.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerAssignmentForm;
import com.zw.api2.swaggerEntity.SwaggerAssignmentQuery;
import com.zw.api2.swaggerEntity.SwaggerAssignmentUpdateForm;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentForm;
import com.zw.platform.domain.basicinfo.form.AssignmentGroupForm;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.MagicNumbers;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ????????????controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team: ZhongWeiTeam </p>
 * @version 1.0
 * @author wangying
 */
@Controller
@RequestMapping("/api/m/basicinfo/enterprise/assignment")
@Api(tags = { "????????????_dev" }, description = "????????????api??????")
public class ApiAssignmentController {
    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    private static final Logger log = LogManager.getLogger(ApiAssignmentController.class);

    private static final String ADD_PAGE = "modules/basicinfo/enterprise/assignment/add";

    private static final String EDIT_PAGE = "modules/basicinfo/enterprise/assignment/edit";

    private static final String ASSIGNMENT_PAGE = "modules/basicinfo/enterprise/assignment/assignmentPer";

    private static final String REMOVE_VEHICLE_PAGE = "modules/basicinfo/enterprise/assignment/vehiclePer";

    private static final String IMPORT_PAGE = "modules/basicinfo/enterprise/assignment/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private HttpServletRequest request;

    /**
     * ????????????
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "???????????????????????????????????????????????????????????????????????????", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "??????????????????", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "???????????????,????????????20", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "???????????????????????????????????????id", paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(@ModelAttribute("assignmentQuery") SwaggerAssignmentQuery assignmentQuery) {
        try {
            AssignmentQuery query = new AssignmentQuery();
            BeanUtils.copyProperties(assignmentQuery, query);
            Page<Assignment> result = (Page<Assignment>) assignmentService.findAssignment(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("?????????????????????findAssignment?????????", e);
            return new PageGridBean(false);
        }
    }

    /**
     * ??????????????????
     * @param uuid uuid
     * @return String
     * @author wangying
     */
    @ApiIgnore
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "uuid", value = "??????id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
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
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * addProfessionals
     * @param groupId        groupId
     * @param bindingResult  bindingResult
     * @return result
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "??????????????????????????????????????????????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "????????????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "contacts", value = "?????????", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "telephone", value = "????????????", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "??????", paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addProfessionals(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("assignmentForm") SwaggerAssignmentForm assignmentForm,
        @RequestParam("groupId") final String groupId, final BindingResult bindingResult) {
        try {
            AssignmentForm form = new AssignmentForm();
            BeanUtils.copyProperties(assignmentForm, form);
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // ?????????????????????IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                AssignmentGroupForm assignmentGroupForm = new AssignmentGroupForm();
                // ???????????????
                assignmentGroupForm.setAssignmentId(form.getId());
                assignmentGroupForm.setGroupId(groupId);
                // // ?????????????????????????????????
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     * @param id id
     * @return page
     */
    @ApiIgnore
    @ApiOperation(value = "??????????????????_ModelAndView", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "?????????id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            Assignment assignment = assignmentService.findAssignmentById(id);
            mav.addObject("result", assignment);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * edit
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "name", value = "??????????????????????????????????????????????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "contacts", value = "?????????", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "telephone", value = "????????????", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "??????", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") SwaggerAssignmentUpdateForm updateForm,
        final BindingResult bindingResult) {
        try {
            AssignmentForm form = new AssignmentForm();
            BeanUtils.copyProperties(updateForm, form);
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // return assignmentService.updateAssignment(form, ipAddress);
            }
            return null;
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ??????id?????? ??????
     * @param id id
     * @return result
     */
    @ApiOperation(value = "??????id????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@ApiParam(value = "id") @PathVariable("id") final String id) {
        try {
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // ????????????id?????????
            List<VehicleInfo> vehicleList = assignmentService.findVehicleByAssignmentId(id);
            if (vehicleList != null && vehicleList.size() > 0) {
                // ????????????????????????????????????
                return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????????????????");
            }
            // boolean flag = assignmentService.deleteAssignment(id, ipAddress);
            boolean flag = false;
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("??????id??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ????????????
     * @return result
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deltems", value = "????????????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            List<String> ids = Arrays.asList(item);
            for (String id : ids) {
                // ????????????id?????????
                List<VehicleInfo> vehicleList = assignmentService.findVehicleByAssignmentId(id);
                if (vehicleList != null && vehicleList.size() > 0) {
                    // ????????????????????????????????????
                    return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????");
                }
            }
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // ????????????
            // boolean flag = assignmentService.deleteAssignmentByBatch(ids, ipAddress);
            boolean flag = false;
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ?????????
     * @return String
     * @author wangying
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/assignmentTree", method = RequestMethod.POST)
    @ResponseBody
    public String getAssignmentTree() {
        try {
            JSONArray treeList = assignmentService.getAssignmentTree();
            return treeList.toJSONString();
        } catch (Exception e) {
            log.error("?????????????????????", e);
            return null;
        }
    }

    /**
     * ?????????(??????)
     * @param id id
     * @return String
     * @author wangying
     */
    @ApiOperation(value = "??????userId?????????????????????????????????????????????", notes = "????????????(??????id???????????????userId)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/editAssignmentTree_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public String getEditAssignmentTree(@ApiParam(value = "id") @PathVariable String id) {
        try {
            // JSONArray treeList = assignmentService.getEditAssignmentTree(id, null);
            return "treeList.toJSONString()";
        } catch (Exception e) {
            log.error("?????????????????????", e);
            return null;
        }
    }

    /**
     * ??????????????????
     * @param id ??????ID
     * @return String
     * @author wangying
     */
    @ApiIgnore
    @ApiOperation(value = "????????????id????????????????????????????????????????????????????????????_ModelAndView", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/assignmentPer_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView getVehiclePer(@ApiParam(value = "id") @PathVariable String id) {
        try {
            ModelAndView mav = new ModelAndView(ASSIGNMENT_PAGE);
            // String tree = assignmentService.getAssignMonitorUserTree(id);
            Assignment assign = assignmentService.findAssignmentById(id);
            String name = "?????????????????????" + assign.getName() + "";
            mav.addObject("assignmentId", id);
            mav.addObject("userTree", "tree");
            mav.addObject("groupName", name);
            return mav;
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * saveVehiclePer
     * @param assignmentId    assignmentId
     * @param userVehicleList userVehicleList
     * @return result
     */
    @ApiOperation(value = "???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "assignmentId", value = "??????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "userVehicleList", value = "????????????id??????????????????(;)??????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/assignmentPer.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveVehiclePer(@RequestParam("assignmentId") final String assignmentId,
        @RequestParam("userVehicleList") final String userVehicleList) {
        try {
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // boolean flag = vehicleService.updateUserAssignByUser(assignmentId, userVehicleList, ipAddress);
            boolean flag = false;
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ?????????
     * @param id ??????ID
     * @return String
     * @author wangying
     */
    @ApiIgnore
    @ApiOperation(value = "???????????????????????????_ModelAndView", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string") })
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/vehiclePer_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView getRemoveVehiclePer(@PathVariable String id) {
        try {
            ModelAndView mav = new ModelAndView(REMOVE_VEHICLE_PAGE);
            // ?????????????????????????????????tree
            String userTreeData = assignmentService.getMonitorByAssignmentID(id).toJSONString();
            Assignment assign = assignmentService.findAssignmentById(id);
            String name = "?????????????????????" + assign.getName() + "";
            mav.addObject("assignmentId", id);
            mav.addObject("userTree", userTreeData);
            mav.addObject("groupName", name);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * getTreeNodeCounts
     * @param id id
     * @return result
     */
    @ApiOperation(value = "?????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "aid", value = "??????id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/vehiclePer.gsp/count", method = RequestMethod.POST)
    @ResponseBody
    public int getTreeNodeCounts(@RequestParam("aid") final String id) {
        try {
            return vehicleService.countMonitors(id);
        } catch (Exception e) {
            log.error("???????????????????????????", e);
            return 0;
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
            JSONArray result = vehicleService.vehicleTreeForAssign("multiple", id, queryParam, queryType);
            if (result.isEmpty()) {
                return "";
            }
            return ZipUtil.compress(result.toJSONString());
        } catch (Exception e) {
            log.error("???????????????????????????", e);
            return null;
        }
    }

    /**
     * getAssignmentTreeParentNodes
     * @param id id
     * @return result
     */
    @ApiOperation(value = "?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/vehiclePer.gsp/org", method = RequestMethod.POST)
    @ResponseBody
    public String getAssignmentTreeParentNodes(@RequestParam("aid") final String id,
        @RequestParam("queryParam") final String queryParam, @RequestParam("queryType") final String queryType) {
        try {
            JSONArray result = vehicleService.listMonitorTreeParentNodes(id, queryParam, queryType);
            if (result.isEmpty()) {
                return "";
            }
            return ZipUtil.compress(result.toJSONString());
        } catch (Exception e) {
            log.error("???????????????????????????", e);
            return null;
        }
    }

    /**
     * getVehicleListByAssignmentId
     * @param id id
     * @return result
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "aid", value = "??????id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/vehiclePer.gsp/vehicles", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleListByAssignmentId(@RequestParam("aid") String id) {
        try {
            JSONArray result = vehicleService.listMonitorsByAssignmentID(id);
            return ZipUtil.compress(result.toJSONString());
        } catch (Exception e) {
            log.error("???????????????????????????", e);
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
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "assignmentId", value = "??????id", required = true, paramType = "query",
            dataType = "string"), @ApiImplicitParam(name = "vehiclePerAddList",
            value = "????????????????????????list " + "?????????'[{\"vehicleId\":\"b98d03e3-7bb8-405f-b7e0-2f5e2f1e5e01\","
            + "\"assignmentId\":\"ea076006-fa74-4c87-859d-1b661628ca4c\","
            + "\"assignmentName\":\"k\",\"sourceAssignId\":" + "\"fcfe6ccc-f6a2-411d-802d-3bd8670e8231\","
            + "\"sourceAssignName\":\"12111\"," + "\"monitorType\":\"0\"},{\"vehicleId\":\""
            + "cec6d6d0-73b5-49ab-abb3-f08aaf1f8642\"," + "\"assignmentId\":\"ea076006-fa74-4c87-859d-1b661628ca4c\","
            + "\"assignmentName\":\"k\",\"sourceAssignId\":" + "\"fcfe6ccc-f6a2-411d-802d-3bd8670e8231\","
            + "\"sourceAssignName\":\"12111\",\"monitorType\":\"0\"}]'", required = true, paramType = "query",
            dataType = "string"), @ApiImplicitParam(name = "vehiclePerDelete",
            value = "????????????????????????list " + "?????????'[{\"vehicleId\":\"b98d03e3-7bb8-405f-b7e0-2f5e2f1e5e01\","
            + "\"assignmentId\":\"fcfe6ccc-f6a2-411d-802d-3bd8670e8231\","
            + "\"assignmentName\":\"12111\",\"sourceAssignId\":" + "\"fcfe6ccc-f6a2-411d-802d-3bd8670e8231\","
            + "\"sourceAssignName\":\"12111\",\"monitorType\":\"0\"},"
            + "{\"vehicleId\":\"cec6d6d0-73b5-49ab-abb3-f08aaf1f8642\","
            + "\"assignmentId\":\"fcfe6ccc-f6a2-411d-802d-3bd8670e8231\","
            + "\"assignmentName\":\"12111\",\"sourceAssignId\":"
            + "\"fcfe6ccc-f6a2-411d-802d-3bd8670e8231\",\"sourceAssignName\"" + ":\"12111\",\"monitorType\":\"0\"}]'",
            required = true, paramType = "query", dataType = "string") })
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
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            boolean flag = false;
            // assignmentService.saveVehiclePer(vehiclePerAddList, vehiclePerDeleteList, assignmentId, ipAddress);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * repetition
     * @param name  name
     * @param group group
     * @return boolean
     */
    @ApiOperation(value = "?????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "??????????????????????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "group", value = "???????????????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "assignmentId", value = "??????????????????????????????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("name") String name, @RequestParam("group") String group,
        String assignmentId) {
        try {
            List<Assignment> assign = assignmentService.findByNameForOneOrg(name, group);
            if (assignmentId == null || "".equals(assignmentId)) {
                //??????
                return assign == null || assign.isEmpty();
            } else {
                //??????
                if (CollectionUtils.isNotEmpty(assign)) {
                    for (Assignment assignment : assign) {
                        if (!assignmentId.equals(assignment.getId())) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return false;
        }
    }

    /**
     * assignCountLimit
     * @param group group
     * @return boolean
     */
    @ApiOperation(value = "???????????????????????????????????????????????????????????????100??????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "group", value = "??????id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/assignCountLimit", method = RequestMethod.POST)
    @ResponseBody
    public boolean assignCountLimit(@RequestParam("group") String group) {
        try {
            List<Assignment> assign = assignmentService.findAssignmentByGroupId(group);
            return assign == null || assign.size() < MagicNumbers.INT_HUNDRED;
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return false;
        }
    }

    /**
     * assignCountLimitForEdit
     * @param group        group
     * @param assignmentId assignmentId
     * @return boolean
     */
    @ApiIgnore
    @RequestMapping(value = "/assignCountLimitForEdit", method = RequestMethod.POST)
    @ResponseBody
    public boolean assignCountLimitForEdit(@RequestParam("group") String group,
        @RequestParam("assignmentId") String assignmentId) {
        try {
            List<Assignment> assign = assignmentService.findAssignByGroupIdExpectVehicle(group, assignmentId);
            return assign == null || assign.size() < MagicNumbers.INT_HUNDRED;
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return false;
        }
    }

    /**
     * ????????????
     * @param response response
     */
    @ApiIgnore
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            assignmentService.generateTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ??????
     * @return String
     * @author wangying
     */
    @ApiIgnore
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * ??????
     * @param response response
     */
    @ApiIgnore
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????");
            assignmentService.exportAssignment(null, 1, response);
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
    }
}

