package com.zw.api.controller.modules;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.query.VehicleQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

/**
 * <p> Title: 车辆信息Controller </p>
 * <p> Copyright: Copyright (c) 2016 </p>
 * <p> Company: ZhongWei </p>
 * <p> team: ZhongWeiTeam </p>
 * <p> date: 2016年7月22日下午1:41:46 </p>
 *
 * @version 1.0
 * @author wangying
 */
@Controller
@RequestMapping("/swagger/m/vehicle")
@Api(tags = { "车辆管理" }, description = "车辆相关api接口")
public class SwaggerVehicleController {

    private static final Logger log = LogManager.getLogger(SwaggerVehicleController.class);

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private OrganizationService organizationService;

    /**
     * 分页查询
     */
    @ApiOperation(value = "分页查询车辆列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupName", value = "所选组织/分组id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupType", value = "组织类型(类型：assignment：分组；group:企业,若为空默认为group)",
            paramType = "query", dataType = "string", defaultValue = "group") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(VehicleQuery query) {
        Page<VehicleDTO> vehicles = vehicleService.getByPage(query);
        Page<VehicleInfo> result = new Page<>(query.getPage().intValue(), query.getLimit().intValue(), false);
        for (VehicleDTO vehicle : vehicles) {
            result.add(new VehicleInfo(vehicle));
        }
        return new PageGridBean(query, result, true);
    }

    /**
     * 获取车辆信息
     */
    @ApiOperation(value = "获取车辆信息", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public JsonResultBean vehicleInfo(@PathVariable String id) {
        try {
            VehicleDTO vehicle = vehicleService.getById(id);
            if (vehicle == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
            }
            return new JsonResultBean(vehicle);
        } catch (Exception e) {
            log.error("获取车辆信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @ApiOperation(value = "根据车牌号判断车辆是否已存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/exist", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean repetition(@RequestParam("brand") String brand) {
        try {
            String id = vehicleService.getIdByBrand(brand);
            return new JsonResultBean(id != null);
        } catch (Exception e) {
            log.error("判断车辆存在异常", e);
            return new JsonResultBean(false);
        }
    }

    /**
     * 车辆所属组织树
     * @author fan lu
     */
    @ApiOperation(value = "获取车辆所属组织树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vid", value = "车辆id",
         required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/vehicleOrgTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean vehicleOrgTree(String vid) {
        try {
            return new JsonResultBean(getVehicleOrgTree(vid, null));
        } catch (Exception e) {
            log.error("车辆所属组织树异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 车辆所属组织树
     * @author fan lu
     */
    public String getVehicleOrgTree(String vid, String isOrg) {
        // 获取当前用户所在组织及下级组织
        JSONArray result = new JSONArray();
        if (StringUtils.isNotEmpty(vid)) {
            String orgId = organizationService.getCurrentUserOrgUuid();
            List<OrganizationLdap> orgs = organizationService.getOrgListByUuid(orgId);
            VehicleDTO vehicle = vehicleService.getById(vid);
            List<String> groupIds = Arrays.asList(vehicle.getGroupId().split(";"));
            for (OrganizationLdap group : orgs) {
                if ((isOrg == null || "0".equals(isOrg)) && "ou=organization".equals(group.getCid())) {
                    continue;
                }
                boolean isCheck = groupIds.contains(group.getCid());
                JSONObject obj = new JSONObject();
                obj.put("id", group.getCid());
                obj.put("pId", group.getPid());
                obj.put("name", group.getName());
                obj.put("checked", isCheck);
                obj.put("uuid", group.getUuid());
                result.add(obj);
            }
        }
        return result.toJSONString();
    }
}
