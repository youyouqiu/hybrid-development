package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.vas.carbonmgt.form.EquipForm;
import com.zw.platform.domain.vas.carbonmgt.query.EquipQuery;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.carbonmgt.EquipEntryService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Title: 设备录入Controller
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: fanlu
 * @date 2016年9月18日下午3:11
 */
@RestController
@RequestMapping("/swagger/v/carbonmgt/equipEntry")
@Api(tags = { "基准信息录入" }, description = "基准信息录入相关api")
public class SwaggerEquipEntryController {
    private static Logger log = LogManager.getLogger(SwaggerEquipEntryController.class);
    @Autowired
    private EquipEntryService equipEntryService;
    @Autowired
    private UserService userService;
    @Autowired
    private VehicleService vehicleService;
    private static final String LIST_PAGE = "vas/carbonmgt/equipEntry/list";
    private static final String ADD_PAGE = "vas/carbonmgt/equipEntry/add";
    private static final String EDIT_PAGE = "vas/carbonmgt/equipEntry/edit";

    /**
     * 分页查询
     */
    @ApiOperation(value = "获取基准信息列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照车牌号，车辆类型，燃料类型进行模糊搜索", required = false,
            paramType = "query", dataType = "string"), })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final EquipQuery query) throws BusinessException {
        try {
            if (query != null) {
                Page<EquipForm> result =
                    (Page<EquipForm>) equipEntryService.findBenchmark(userService.getOrgByUser(), query, true);
                if (CollectionUtils.isNotEmpty(result)) {
                    for (EquipForm form : result) {
                        if (StringUtil.isNotEmpty(form.getGroupId())) {
                            StringBuilder groupNameBuilder = new StringBuilder();

                            List<String> groupIds = Arrays.asList(form.getGroupId().split("#"));
                            for (String groupId : groupIds) {
                                OrganizationLdap organization = userService.findOrganization(groupId);
                                if (organization != null) {
                                    groupNameBuilder.append(organization.getName() + ",");
                                }
                            }

                            String groupName = groupNameBuilder.toString();
                            groupName =
                                groupName.endsWith(",") ? groupName.substring(0, groupName.length() - 1) : groupName;
                            form.setGroupName(groupName);
                        }
                    }
                }
                return new PageGridBean(query, result, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("获取基准信息列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /*
     */

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws
     * @Title: 基准信息录入
     * @author fanlu
     */
    @ApiOperation(value = "添加基准信息", notes = "车辆类别不同，基准录入信息不同。车辆类别4(工程车辆)对应时间基准能耗,工时基准能耗,怠速基准能耗;"
        + "其余类别对应里程基准能耗，时间基准能耗,工时基准能耗。车辆类别可在车辆表中查询，对应字段为vehicleCategory", authorizations = {
            @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
                scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "groupId", value = "分组", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "time_benchmark", value = "时间基准能耗", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "idle_benchmark", value = "怠速基准能耗", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "work_hours_benchmark", value = "工时基准能耗", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "mileage_benchmark", value = "里程基准能耗", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final EquipForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                EquipForm equipForm = equipEntryService.findBenchmarkByVehicleId(form.getVehicleId());
                if (equipForm != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该车辆已存在基准信息，请重新选择车辆！");
                }
                if (vehicleService.findVehicleById(form.getVehicleId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该车辆id不存在，请重新输入！");
                }
                form.setFlag(1);
                form.setCreateDataTime(new Date());
                form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                boolean flag = equipEntryService.addBenchmark(form);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }

            }
        } catch (Exception e) {
            log.error("基准信息录入异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 根据id删除 基准信息
     */
    @ApiOperation(value = "删除基准信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @Transactional
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) throws BusinessException {
        EquipForm form = new EquipForm();
        boolean flag = false;
        form.setId(id);
        form.setFlag(0);
        flag = equipEntryService.deleteBenchmark(form);
        if (flag) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除基准信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的基准信息ids(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @Transactional
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) throws BusinessException {
        String items = request.getParameter("deltems");
        String[] item = items.split(",");
        for (int i = 0; i < item.length; i++) {
            EquipForm form = new EquipForm();
            form.setId(item[i]);
            form.setFlag(0);
            equipEntryService.deleteBenchmark(form);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 修改基准信息
     */
    @ApiOperation(value = "根据id获取基准信息", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable final String id) {
        return new JsonResultBean(equipEntryService.findBenchmarkById(id));
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws
     * @Title: 修改基准信息
     * @author fanlu
     */
    @ApiOperation(value = "修改基准信息", notes = "车辆类别不同，基准录入信息不同。车辆类别4(工程车辆)对应时间基准能耗,工时基准能耗,怠速基准能耗;"
        + "其余类别对应里程基准能耗，时间基准能耗,工时基准能耗。车辆类别可在车辆表中查询，对应字段为vehicleCategory", authorizations = {
            @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
                scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "基准信息id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "分组", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "time_benchmark", value = "时间基准能耗", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "idle_benchmark", value = "怠速基准能耗", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "work_hours_benchmark", value = "工时基准能耗", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "mileage_benchmark", value = "里程基准能耗", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final EquipForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                EquipForm equipForm = equipEntryService.findBenchmarkByVehicleId(form.getVehicleId());
                if (equipForm != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该车辆已存在基准信息，请重新选择车辆！");
                }
                if (vehicleService.findVehicleById(form.getVehicleId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该车辆id不存在，请重新输入！");
                }
                form.setUpdateDataTime(new Date());
                form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
                boolean flag = equipEntryService.updateBenchmark(form);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
        } catch (Exception e) {
            log.error("修改基准信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 根据所选组织返回车辆列表
     * @param ids 组织id
     * @return
     * @throws BusinessException
     * @author fan lu
     */
    @ApiOperation(value = "根据所选组织返回车辆列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "ids", value = "分组列表id集合,用逗号隔开", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "type", value = "所选分组是否为分组(还可以为企业)", required = true, paramType = "query",
            dataType = "boolean") })
    @RequestMapping(value = "/findVehicle", method = RequestMethod.POST)
    @ResponseBody
    public String findVehicle(String ids, String type) throws BusinessException {
        List<String> group = new ArrayList<String>();
        group.add(ids);
        boolean flag = false;
        flag = "assignment".equals(type) ? true : false;
        List<VehicleInfo> vehicle = (List<VehicleInfo>) equipEntryService.findVehicleByUser(group, flag);
        JSONArray result = new JSONArray();
        result.addAll(vehicle);
        return result.toJSONString();
    }

    /**
     * 车辆已存在基准信息
     * @param brand 车牌号
     * @return
     * @throws BusinessException
     * @author fan lu
     */
    @ApiOperation(value = "检查车辆基准信息是否已经存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/checkExsit", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkExsit(@ApiParam(required = true, name = "brand", value = "车牌号") String brand) {
        try {
            boolean flag = true;
            List<EquipForm> result = equipEntryService.findBenchmark(userService.getOrgByUser(), null, false);
            if (CollectionUtils.isNotEmpty(result)) {
                for (EquipForm eq : result) {
                    if (brand.equals(eq.getBrand())) {
                        flag = false;
                        return flag;
                    }
                }
            }
            return flag;
        } catch (Exception e) {
            log.error("检查车辆基准信息存在异常", e);
            return false;
        }
    }
}
