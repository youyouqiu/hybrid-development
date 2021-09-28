package com.zw.api2.controller.vehicle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerVehiclePurposeForm;
import com.zw.api2.swaggerEntity.SwaggerVehiclePurposeQuery;
import com.zw.platform.basic.domain.VehiclePurposeDO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.VehiclePurpose;
import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.domain.basicinfo.form.VehiclePurposeForm;
import com.zw.platform.domain.basicinfo.query.VehiclePurposeQuery;
import com.zw.platform.domain.basicinfo.query.VehicleQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @version 1.0
 * @Author gfw
 * @Date 2018/12/11 13:47
 * @Description 车辆管理相关API
 */
@Controller
@RequestMapping("api/m/basicinfo/monitoring/vehicle")
@Api(tags = { "车辆管理_dev" }, description = "车辆相关api接口")
public class ApiVehicleController {
    private static final Logger log = LogManager.getLogger(ApiVehicleController.class);

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Value("${vehicle.brand.bound}")
    private String vehicleBrandBound;

    @Value("${sys.error.msg}")
    private String syError;

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/vehicle/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/vehicle/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/vehicle/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/vehicle/import";

    private static final String IMPORTWO_PAGE = "modules/basicinfo/monitoring/vehicle/importTwo";

    private static final String LOGO_EDIT = "modules/basicinfo/monitoring/vehicle/vIcoEdit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String VEHICLE_DETAIL_PAGE = "modules/basicinfo/monitoring/vehicle/vehicleDetail";

    /**
     * table
     * @return resource Page
     */
    @Auth
    @ApiIgnore
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * add page
     * @return resource Page
     */
    @ApiIgnore
    @RequestMapping(value = { "/vlist" }, method = RequestMethod.GET)
    public String vlistPage() {
        return ADD_PAGE;
    }

    /**
     * vehicle icon edit page
     * @return resource Page
     */
    @ApiIgnore
    @RequestMapping(value = { "/editLogo" }, method = RequestMethod.GET)
    public String editLogo() {
        return LOGO_EDIT;
    }

    /**
     * 运营类别查询分页
     * @param purposeQuery 查询参数
     * @return 分页数据
     */
    @ApiOperation(value = "运营类别查询分页", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "purposeCategory", value = "运营类别", paramType = "query", dataType = "string")
    @ResponseBody
    @RequestMapping(value = "/purposeCategoryList", method = RequestMethod.POST)
    public PageGridBean list(@ModelAttribute("purposeQuery") final SwaggerVehiclePurposeQuery purposeQuery) {
        VehiclePurposeQuery query = new VehiclePurposeQuery();
        BeanUtils.copyProperties(purposeQuery, query);
        try {
            Page<VehiclePurposeDTO> result = vehicleService.findVehiclePurposeByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询运营类别异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 新增页面
     * @param uuid uuid
     * @return 页面
     */
    @ApiIgnore
    @ApiOperation(value = "ModelAndView_车辆新增页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "uuid", value = "uuid", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean initNewUser(@RequestParam("uuid") final String uuid) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            if (!"".equals(uuid) && !"ou=organization".equals(uuid)) {
                OrganizationLdap org = userService.getOrgByEntryDN(uuid);
                mav.addObject("orgId", org.getUuid());
                mav.addObject("groupName", org.getName());
            }
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("新增车辆信息弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据id删除 车辆
     * @param id 车辆Id
     * @return result
     */
    @ApiOperation(value = "根据id删除 车辆", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@ApiParam(value = "id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject msg = new JSONObject();
            /*msg.put("vehicleId", id);
            VehicleForm form = new VehicleForm();
            ConfigForm c = configService.getIsBand(id, "", "", "");
            if (c == null) {
                form.setId(id);
                form.setFlag(0);
                String key = RedisHelper.buildKey(id, "vehicle", "list");
                String vehicleData = RedisHelper.get(key, PublicVariable.REDIS_TEN_DATABASE);// 车辆信息
                Map dataMap = JSONObject.parseObject(vehicleData, Map.class);
                String brand = dataMap.get("brand").toString();
                form.setGroupId(dataMap.get("groupName").toString());
                form.setBrand(brand);
                // 获得客户端ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                vehicleService.deleteVehicleWithGroup(form, ipAddress);
                // redis中清除该车辆的车牌颜色
                RedisHelper.del(brand + "_vehicle_color", PublicVariable.REDIS_NINE_DATABASE);
                // 删除模糊搜索
                RedisHelper
                    .hdel(RedisKeys.VEHICLE_DEVICE_SIMCARD_FUZZY_SEARCH, brand, PublicVariable.REDIS_TEN_DATABASE);
                // 删除车辆保险信息
                vehicleInsuranceService.deleteByVehicleId(ipAddress, id, brand);
                //删除趟次管理
                transportTimesService.deleteByVehicleId(id, ipAddress, brand);
            } else {
                msg.put("infoMsg", vehicleBrandBound);
            }*/
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("删除车辆信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改车辆
     * @param id vehicleId
     * @return edit page
     */
    @ApiOperation(value = "修改车辆", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@ApiParam(value = "id") @PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            getVehicleDetail(id, mav);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("修改车辆信息弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 详情页面
     * @param id vehicleId
     * @return detail page
     */
    @ApiOperation(value = "详情信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/vehicleDetail_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean vehicleDetail(@ApiParam(value = "id") @PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(VEHICLE_DETAIL_PAGE);
            getVehicleDetail(id, mav);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("车辆详情信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改和详情公共方法
     * @param id  车辆id
     * @param mav mav
     * @throws Exception this
     */
    private void getVehicleDetail(@PathVariable String id, ModelAndView mav) throws Exception {
        VehicleInfo vehicle = vehicleService.findVehicleById(id);
        if (Objects.nonNull(vehicle) && StringUtils.isNotBlank(vehicle.getGroupId())) {
            List<String> groupIds = Arrays.asList(vehicle.getGroupId().split(";"));
            StringBuilder groupNameBuilder = new StringBuilder();
            for (String groupId : groupIds) {
                OrganizationLdap organization = userService.getOrgByUuid(groupId);
                if (organization != null) {
                    groupNameBuilder.append(organization.getName() + ",");
                }
            }
            String groupName = groupNameBuilder.toString();
            groupName = groupName.endsWith(",") ? groupName.substring(0, groupName.length() - 1) : groupName;
            vehicle.setGroupName(groupName);
        }
        mav.addObject("result", vehicle);
    }

    /**
     * 修改车辆信息
     * @param form          vehicleForm
     * @param bindingResult bindingResult
     * @return JsonResultBean result
     * @author wangying
     */
    @ApiOperation(value = "修改车辆信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VehicleForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 获得访问ip
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // boolean result = vehicleService.updateVehicleInfo(form, ipAddress);
                    // if (result) {
                    //     return new JsonResultBean(JsonResultBean.SUCCESS);
                    // }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改车辆信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 车辆列表导出
     * @param response response
     * @Des 忽略导出
     */
    @ApiIgnore
    @Deprecated
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response, VehicleQuery query) {
        try {
            ExportExcelUtil.setResponseHead(response, "车辆列表");
        } catch (Exception e) {
            log.error("导出车辆信息异常", e);
        }
    }

    /**
     * 运营类别类别导出
     * @param response response
     * @Des 忽略导出
     */
    @ApiIgnore
    @Deprecated
    @RequestMapping(value = "/exportVehiclePurpose", method = RequestMethod.GET)
    public void exportVehiclePurpose(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "运营类别");
        } catch (Exception e) {
            log.error("导出运营类别异常", e);
        }
    }

    /**
     * 下载通用车辆列表模板
     * @param response response
     * @Des 忽略下载
     */
    @ApiIgnore
    @Deprecated
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "通用车辆列表模板");
            vehicleService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载通用车辆列表模板异常", e);
        }
    }

    /**
     * 下载工程机械列表模板
     * @param response response
     * @Des 忽略下载
     */
    @ApiIgnore
    @RequestMapping(value = "/downloadEngineering", method = RequestMethod.GET)
    public void downloadEngineering(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "工程机械列表模板");
            vehicleService.generateTemplateEngineering(response);
        } catch (Exception e) {
            log.error("下载工程机械列表模板异常", e);
        }
    }

    /**
     * 下载货运车辆列表模板
     * @param response response
     * @Des 忽略下载
     */
    @ApiIgnore
    @RequestMapping(value = "/downloadFreight", method = RequestMethod.GET)
    public void downloadFreight(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "货运车辆列表模板");
            vehicleService.generateTemplateFreight(response);
        } catch (Exception e) {
            log.error("下载货运车辆列表模板异常", e);
        }
    }

    /**
     * 导入页面
     * @return String
     * @Des 忽略导入
     * @author wangying
     */
    @ApiIgnore
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * 导入
     * @param file 文件
     * @return result
     * @Des 忽略导入
     */
    @ApiIgnore
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public JsonResultBean importVehicle(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("导入车辆信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 用户树数据
     * @return List<Group>
     * @throws BusinessException exception
     */
    @ApiOperation(value = "用户所属企业 (新增)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/userTree", method = RequestMethod.POST)
    @ResponseBody
    public String getTree() throws BusinessException {
        return generateUserTree(null);
    }

    /**
     * 用户树数据(编辑)
     * @param assignmentId 组织Id
     * @return List<Group>
     * @throws BusinessException exception
     */
    @ApiOperation(value = "用户所属企业 (编辑)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "assignmentId", value = "组织Id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/userEditTree", method = RequestMethod.POST)
    @ResponseBody
    public String getTreeEdit(String assignmentId) throws BusinessException {
        return generateUserTree(assignmentId);
    }

    /**
     * 生成user树结构
     * @param assignmentId 组织Id
     * @return String
     * @author wangying
     */
    private String generateUserTree(String assignmentId) {
        try {
            String orgId = userService.getOrgIdByUser();
            List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
            // 根据组织查询用户
            List<UserBean> users = userService.getUserList(null, orgId, true);
            // 已选User
            List<String> isChecked = new ArrayList<String>();
            List<String> ulist = new ArrayList<String>();
            for (UserBean user : users) {
                ulist.add(user.getId().toString());
            }
            if (!StringUtil.isNullOrBlank(assignmentId)) {
                isChecked = vehicleService.findUserAssignByAid(assignmentId, ulist);
            }
            JSONArray result = new JSONArray();
            for (OrganizationLdap group : orgs) {
                JSONObject obj = new JSONObject();
                obj.put("id", group.getCid());
                obj.put("pId", group.getPid());
                obj.put("name", group.getName());
                obj.put("iconSkin", "groupSkin");
                result.add(obj);
            }

            for (UserBean user : users) {
                JSONObject userObj = new JSONObject();
                String uid = user.getId().toString();
                // 获取组织id(根据用户id得到用户所在部门)
                int userIndex = uid.indexOf(",");
                String userPid = uid.substring(userIndex + 1);
                userObj.put("id", uid);
                userObj.put("pId", userPid);
                userObj.put("name", user.getUsername());
                userObj.put("type", "user");
                userObj.put("iconSkin", "userSkin");
                if (isChecked.size() > 0 && isChecked.contains(uid)) {
                    userObj.put("checked", true);
                }
                result.add(userObj);
            }

            return result.toJSONString();
        } catch (Exception e) {
            log.error("生成车辆树异常", e);
            return null;
        }
    }

    /**
     * 查询车辆类型信息
     * @return result
     */
    @ApiOperation(value = "查询车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/addList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean pageBean() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("VehicleTypeList", vehicleService.getVehicleTypeList());
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * repetition
     * @param brand 车牌号
     * @return result
     */
    @ApiOperation(value = "车牌号是否重复", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })

    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ApiImplicitParam(name = "brand", value = "车牌号", required = true, paramType = "query", dataType = "string")
    @ResponseBody
    public boolean repetition(@RequestParam("brand") String brand) {
        try {
            VehicleInfo vt = vehicleService.findByVehicle(brand);
            return vt == null;
        } catch (Exception e) {
            log.error("校验车辆存在异常", e);
            return false;
        }
    }

    /**
     * 车辆所属组织树
     * @param vid vehicleId
     * @return String
     * @throws BusinessException exception
     * @author fan lu
     */
    @ApiOperation(value = "车辆所属组织树", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })

    @ApiImplicitParam(name = "vid", value = "车辆id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/vehicleOrgTree", method = RequestMethod.POST)
    @ResponseBody
    public String vehicleOrgTree(String vid) throws BusinessException {
        return getVehicleOrgTree(vid, null);
    }

    /**
     * 车辆所属组织树
     * @param vid   vehicleId
     * @param isOrg isOrg
     * @return String
     * @author fan lu
     */
    private String getVehicleOrgTree(String vid, String isOrg) {
        // 获取当前用户所在组织及下级组织
        JSONArray result = new JSONArray();
        String orgId = userService.getOrgIdByUser();
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        for (OrganizationLdap group : orgs) {
            if ((isOrg == null || "0".equals(isOrg)) && "ou=organization".equals(group.getCid())) {
                continue;
            }
            JSONObject obj = new JSONObject();
            obj.put("id", group.getCid());
            obj.put("pId", group.getPid());
            obj.put("name", group.getName());
            obj.put("uuid", group.getUuid());
            obj.put("checked", false);
            obj.put("uuid", group.getUuid());
            result.add(obj);
        }
        return result.toJSONString();
    }

    /**
     * subscribeVehicleList
     * @return vehicle list
     */
    @ApiOperation(value = "订阅车辆列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/subscribeVehicleList", method = RequestMethod.POST)
    @ResponseBody
    public List<VehicleInfo> subscribeVehicleList() {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        List<String> groupList = userService.getOrgByUser();
        return vehicleService.findAllSendVehicle(userId, groupList);
    }

    /**
     * @param vehiclePurposesForm
     * @return result
     * @author tangshunyu
     */
    @ApiOperation(value = "新增运营类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/addVehiclePurpose", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addVehiclePurpose(@Validated({ ValidGroupAdd.class }) @ModelAttribute(
        "vehiclePurposesForm") final SwaggerVehiclePurposeForm vehiclePurposesForm) {
        VehiclePurposeForm vehiclePurposes = new VehiclePurposeForm();
        BeanUtils.copyProperties(vehiclePurposesForm, vehiclePurposes);
        try {
            if (vehiclePurposes != null) {
                List<VehiclePurpose> list = vehicleService.findVehiclePurpose(vehiclePurposes.getPurposeCategory());
                if (list == null || list.size() != 0) {
                    return new JsonResultBean(JsonResultBean.FAULT, "sorry！您输入的运营类别已存在，请重新输入...");
                } else {
                    // 获得客户端的IP
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    boolean flag = vehicleService.addVehiclePurpose(vehiclePurposes, ipAddress);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增运营类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据运营类别名称获取运营类别实体
     * @param purposeCategory purposeCategory
     * @return VehiclePurpose list
     */
    @ApiOperation(value = "根据运营类别名称获取运营类别实体", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "purposeCategory", value = "运营类别", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/findVehiclePurpose", method = RequestMethod.POST)
    @ResponseBody
    public List<VehiclePurpose> findVehiclePurposes(String purposeCategory) {
        try {
            return vehicleService.findVehiclePurpose(purposeCategory);
        } catch (Exception e) {
            log.error("根据运营类别名称获取运营类别实体异常", e);
            return null;
        }
    }

    /**
     * 根据id获取运营类别类别
     * @param id vehicleId
     * @return VehiclePurpose
     */
    @ApiOperation(value = "根据id获取运营类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/findVehiclePurposeCategory", method = RequestMethod.POST)
    @ResponseBody
    public VehiclePurposeDO findPurposeCategoryById(String id) {
        return vehicleService.get(id);
    }

    /**
     * 查询数据库是否有该运营类别,并对比修改前和修改后的用途,是否相同
     * @param newType
     * @param oldType
     * @return
     */
    @ApiOperation(value = "修改时比较运营类别是否存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "newType", value = "修改前运营类型", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oldType", value = "修改后运营类型", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/comparison", method = RequestMethod.POST)
    @ResponseBody
    public boolean comparisonVehicleUserType(String newType, String oldType) {
        try {
            if (newType != null && !"".equals(newType) && oldType != null && !"".equals(oldType)) {
                if (newType.equals(oldType)) {
                    return true;
                } else {
                    List<VehiclePurpose> list = vehicleService.findVehiclePurpose(newType);
                    if (list.size() == 0) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.error("查询运营类别异常", e);
            return false;
        }

    }

    /**
     * 根据运营类别查询数据库是否有相同的运营类别(新增运营类别时的验证)
     * @param vehicleUseType
     * @return
     */
    @ApiOperation(value = "新增时判断运营类别是否存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleUseType", value = "运营类型", required = true, paramType = "query",
            dataType = "long") })
    @RequestMapping(value = "/findVehicleUseType", method = RequestMethod.POST)
    @ResponseBody
    public boolean findVehicleUseType(String vehicleUseType) {
        try {
            if (vehicleUseType != null && !"".equals(vehicleUseType)) {
                List<VehiclePurpose> list = vehicleService.findVehiclePurpose(vehicleUseType);// 根据运营类别查询运营类别
                if (list.size() == 0) { // 如果没有查询到,可以新增
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("查询运营类别异常", e);
            return false;
        }

    }

    /**
     * 修改运营类别类别
     * @param id
     * @param purposeCategory
     * @param description
     * @return
     */
    @ApiOperation(value = "修改运营类别类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(
        value = { @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "query", dataType = "long"),
            @ApiImplicitParam(name = "purposeCategory", value = "运营类型", required = true, paramType = "query",
                dataType = "long"),
            @ApiImplicitParam(name = "description", value = "备注", required = true, paramType = "query",
                dataType = "long") })
    @RequestMapping(value = "/updateVehiclePurposeCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateVehiclePurpose(String id, String purposeCategory, String description) {
        try {
            if (id != null && !"".equals(id) && purposeCategory != null && !"".equals(purposeCategory)) {
                VehiclePurposeForm form = new VehiclePurposeForm();
                form.setId(id);
                form.setPurposeCategory(purposeCategory);
                form.setDescription(description);
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return vehicleService.updateVehiclePurpose(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改运营类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除运营类别
     * @param id 运营类别id
     * @return result
     * @author tangshunyu
     */
    @ApiOperation(value = "根据id删除运营类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "query", dataType = "long") })
    @RequestMapping(value = "/deleteVehiclePurpose", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteVehiclePurpose(String id) {
        try {
            if (id != null) {
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                boolean flag = vehicleService.deletePurpose(id, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除运营类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除运营类别
     * @param ids 运营类别Ids
     * @return result
     */
    @ApiOperation(value = "批量删除运营类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "ids 使用,分割", required = true, paramType = "query", dataType = "long") })
    @RequestMapping(value = "/deleteVehiclePurposeMuch", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteVehiclePurposeMuch(String ids) {
        try {
            if (ids != null) {
                String[] item = ids.split(",");
                List<String> list = Arrays.asList(item);
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                boolean flag = vehicleService.deleteVehiclePurposeMuch(list, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除运营类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 运营类别下载模板
     * 下载不提供
     * @param response response
     */
    @ApiIgnore
    @RequestMapping(value = "/downloadPurpose", method = RequestMethod.GET)
    public void downloadType(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "运营类别模板");
            vehicleService.getVehiclePurposeTemplate(response);
        } catch (Exception e) {
            log.error("下载运营类别模板异常", e);
        }
    }

    /**
     * 导入运营类别
     * @return String
     * @Des 页面不提供
     * @author tangshunyu
     */
    @ApiIgnore
    @RequestMapping(value = "/importTwo", method = RequestMethod.GET)
    public String importTwoPage() {
        return IMPORTWO_PAGE;
    }

    /**
     * 导入运营类别
     * @param file 文件
     * @return result
     * @Des 忽略导入
     */
    @ApiIgnore
    @RequestMapping(value = "/importPurpose", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importType(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // 访问服务器的客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = vehicleService.importPurpose(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入运营类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 查询运营类别
     * @return result
     */
    @ApiOperation(value = "查询运营类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/findAllPurposeCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findAllPurposeCategory() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("VehicleCategoryList", vehicleService.findVehicleCategory());
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询运营类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 查询燃料类型
     * @return result
     */
    @ApiOperation(value = "查询燃料类型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/findAllFuelType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findAllFuelType() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("FuelTypeList", vehicleService.findFuelType());
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询燃料类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 查询品牌
     * @return result
     */
    @ApiOperation(value = "查询品牌", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })

    @RequestMapping(value = "/findBrand", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findBrand() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("brandList", vehicleService.findBrand());
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询品牌异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据品牌id查询机型
     * @return result
     */
    @ApiOperation(value = "根据品牌id查询机型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/findBrandModelsByBrandId_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findBrandModelsByBrandId(@ApiParam(value = "id") @PathVariable final String id) {
        try {
            JSONObject msg = new JSONObject();
            // msg.put("brandModelList", vehicleService.findBrandModelsByBrandId(id));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询品牌异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据车牌号获取车辆的id和车牌号
     * @return
     */
    @ApiOperation(value = "根据车牌号获取车辆的id和车牌号", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "brand", value = "根据品牌id查询机型", required = true, paramType = "query",
            dataType = "int") })
    @RequestMapping(value = "/findTransportList", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public JsonResultBean findTransportList(@RequestParam(value = "brand", required = false) String brand) {
        List<Map<String, String>> list = new ArrayList<>();
        return new JsonResultBean(list);
    }

}
