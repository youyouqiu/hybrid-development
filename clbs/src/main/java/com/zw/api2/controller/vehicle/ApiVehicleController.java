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
 * @Description ??????????????????API
 */
@Controller
@RequestMapping("api/m/basicinfo/monitoring/vehicle")
@Api(tags = { "????????????_dev" }, description = "????????????api??????")
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
     * ????????????????????????
     * @param purposeQuery ????????????
     * @return ????????????
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "purposeCategory", value = "????????????", paramType = "query", dataType = "string")
    @ResponseBody
    @RequestMapping(value = "/purposeCategoryList", method = RequestMethod.POST)
    public PageGridBean list(@ModelAttribute("purposeQuery") final SwaggerVehiclePurposeQuery purposeQuery) {
        VehiclePurposeQuery query = new VehiclePurposeQuery();
        BeanUtils.copyProperties(purposeQuery, query);
        try {
            Page<VehiclePurposeDTO> result = vehicleService.findVehiclePurposeByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * ????????????
     * @param uuid uuid
     * @return ??????
     */
    @ApiIgnore
    @ApiOperation(value = "ModelAndView_??????????????????", authorizations = {
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
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????id?????? ??????
     * @param id ??????Id
     * @return result
     */
    @ApiOperation(value = "??????id?????? ??????", authorizations = {
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
                String vehicleData = RedisHelper.get(key, PublicVariable.REDIS_TEN_DATABASE);// ????????????
                Map dataMap = JSONObject.parseObject(vehicleData, Map.class);
                String brand = dataMap.get("brand").toString();
                form.setGroupId(dataMap.get("groupName").toString());
                form.setBrand(brand);
                // ???????????????ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                vehicleService.deleteVehicleWithGroup(form, ipAddress);
                // redis?????????????????????????????????
                RedisHelper.del(brand + "_vehicle_color", PublicVariable.REDIS_NINE_DATABASE);
                // ??????????????????
                RedisHelper
                    .hdel(RedisKeys.VEHICLE_DEVICE_SIMCARD_FUZZY_SEARCH, brand, PublicVariable.REDIS_TEN_DATABASE);
                // ????????????????????????
                vehicleInsuranceService.deleteByVehicleId(ipAddress, id, brand);
                //??????????????????
                transportTimesService.deleteByVehicleId(id, ipAddress, brand);
            } else {
                msg.put("infoMsg", vehicleBrandBound);
            }*/
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     * @param id vehicleId
     * @return edit page
     */
    @ApiOperation(value = "????????????", authorizations = {
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
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????
     * @param id vehicleId
     * @return detail page
     */
    @ApiOperation(value = "????????????", authorizations = {
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ???????????????????????????
     * @param id  ??????id
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
     * ??????????????????
     * @param form          vehicleForm
     * @param bindingResult bindingResult
     * @return JsonResultBean result
     * @author wangying
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VehicleForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // ????????????
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // ????????????ip
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // boolean result = vehicleService.updateVehicleInfo(form, ipAddress);
                    // if (result) {
                    //     return new JsonResultBean(JsonResultBean.SUCCESS);
                    // }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }

    }

    /**
     * ??????????????????
     * @param response response
     * @Des ????????????
     */
    @ApiIgnore
    @Deprecated
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response, VehicleQuery query) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????");
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
    }

    /**
     * ????????????????????????
     * @param response response
     * @Des ????????????
     */
    @ApiIgnore
    @Deprecated
    @RequestMapping(value = "/exportVehiclePurpose", method = RequestMethod.GET)
    public void exportVehiclePurpose(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????");
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
    }

    /**
     * ??????????????????????????????
     * @param response response
     * @Des ????????????
     */
    @ApiIgnore
    @Deprecated
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????????????????");
            vehicleService.generateTemplate(response);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
    }

    /**
     * ??????????????????????????????
     * @param response response
     * @Des ????????????
     */
    @ApiIgnore
    @RequestMapping(value = "/downloadEngineering", method = RequestMethod.GET)
    public void downloadEngineering(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????????????????");
            vehicleService.generateTemplateEngineering(response);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
    }

    /**
     * ??????????????????????????????
     * @param response response
     * @Des ????????????
     */
    @ApiIgnore
    @RequestMapping(value = "/downloadFreight", method = RequestMethod.GET)
    public void downloadFreight(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????????????????");
            vehicleService.generateTemplateFreight(response);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
    }

    /**
     * ????????????
     * @return String
     * @Des ????????????
     * @author wangying
     */
    @ApiIgnore
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * ??????
     * @param file ??????
     * @return result
     * @Des ????????????
     */
    @ApiIgnore
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public JsonResultBean importVehicle(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // ????????????IP??????
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ???????????????
     * @return List<Group>
     * @throws BusinessException exception
     */
    @ApiOperation(value = "?????????????????? (??????)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/userTree", method = RequestMethod.POST)
    @ResponseBody
    public String getTree() throws BusinessException {
        return generateUserTree(null);
    }

    /**
     * ???????????????(??????)
     * @param assignmentId ??????Id
     * @return List<Group>
     * @throws BusinessException exception
     */
    @ApiOperation(value = "?????????????????? (??????)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "assignmentId", value = "??????Id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/userEditTree", method = RequestMethod.POST)
    @ResponseBody
    public String getTreeEdit(String assignmentId) throws BusinessException {
        return generateUserTree(assignmentId);
    }

    /**
     * ??????user?????????
     * @param assignmentId ??????Id
     * @return String
     * @author wangying
     */
    private String generateUserTree(String assignmentId) {
        try {
            String orgId = userService.getOrgIdByUser();
            List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
            // ????????????????????????
            List<UserBean> users = userService.getUserList(null, orgId, true);
            // ??????User
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
                // ????????????id(????????????id????????????????????????)
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
            log.error("?????????????????????", e);
            return null;
        }
    }

    /**
     * ????????????????????????
     * @return result
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
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
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * repetition
     * @param brand ?????????
     * @return result
     */
    @ApiOperation(value = "?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })

    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ApiImplicitParam(name = "brand", value = "?????????", required = true, paramType = "query", dataType = "string")
    @ResponseBody
    public boolean repetition(@RequestParam("brand") String brand) {
        try {
            VehicleInfo vt = vehicleService.findByVehicle(brand);
            return vt == null;
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return false;
        }
    }

    /**
     * ?????????????????????
     * @param vid vehicleId
     * @return String
     * @throws BusinessException exception
     * @author fan lu
     */
    @ApiOperation(value = "?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })

    @ApiImplicitParam(name = "vid", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/vehicleOrgTree", method = RequestMethod.POST)
    @ResponseBody
    public String vehicleOrgTree(String vid) throws BusinessException {
        return getVehicleOrgTree(vid, null);
    }

    /**
     * ?????????????????????
     * @param vid   vehicleId
     * @param isOrg isOrg
     * @return String
     * @author fan lu
     */
    private String getVehicleOrgTree(String vid, String isOrg) {
        // ?????????????????????????????????????????????
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
    @ApiOperation(value = "??????????????????", authorizations = {
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
    @ApiOperation(value = "??????????????????", authorizations = {
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
                    return new JsonResultBean(JsonResultBean.FAULT, "sorry??????????????????????????????????????????????????????...");
                } else {
                    // ??????????????????IP
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    boolean flag = vehicleService.addVehiclePurpose(vehiclePurposes, ipAddress);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????????????????????????????????????????
     * @param purposeCategory purposeCategory
     * @return VehiclePurpose list
     */
    @ApiOperation(value = "????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "purposeCategory", value = "????????????", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/findVehiclePurpose", method = RequestMethod.POST)
    @ResponseBody
    public List<VehiclePurpose> findVehiclePurposes(String purposeCategory) {
        try {
            return vehicleService.findVehiclePurpose(purposeCategory);
        } catch (Exception e) {
            log.error("??????????????????????????????????????????????????????", e);
            return null;
        }
    }

    /**
     * ??????id????????????????????????
     * @param id vehicleId
     * @return VehiclePurpose
     */
    @ApiOperation(value = "??????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/findVehiclePurposeCategory", method = RequestMethod.POST)
    @ResponseBody
    public VehiclePurposeDO findPurposeCategoryById(String id) {
        return vehicleService.get(id);
    }

    /**
     * ???????????????????????????????????????,???????????????????????????????????????,????????????
     * @param newType
     * @param oldType
     * @return
     */
    @ApiOperation(value = "???????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "newType", value = "?????????????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oldType", value = "?????????????????????", required = true, paramType = "query",
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
            log.error("????????????????????????", e);
            return false;
        }

    }

    /**
     * ???????????????????????????????????????????????????????????????(??????????????????????????????)
     * @param vehicleUseType
     * @return
     */
    @ApiOperation(value = "???????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleUseType", value = "????????????", required = true, paramType = "query",
            dataType = "long") })
    @RequestMapping(value = "/findVehicleUseType", method = RequestMethod.POST)
    @ResponseBody
    public boolean findVehicleUseType(String vehicleUseType) {
        try {
            if (vehicleUseType != null && !"".equals(vehicleUseType)) {
                List<VehiclePurpose> list = vehicleService.findVehiclePurpose(vehicleUseType);// ????????????????????????????????????
                if (list.size() == 0) { // ?????????????????????,????????????
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return false;
        }

    }

    /**
     * ????????????????????????
     * @param id
     * @param purposeCategory
     * @param description
     * @return
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(
        value = { @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "query", dataType = "long"),
            @ApiImplicitParam(name = "purposeCategory", value = "????????????", required = true, paramType = "query",
                dataType = "long"),
            @ApiImplicitParam(name = "description", value = "??????", required = true, paramType = "query",
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
                // ????????????ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return vehicleService.updateVehiclePurpose(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????id??????????????????
     * @param id ????????????id
     * @return result
     * @author tangshunyu
     */
    @ApiOperation(value = "??????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "query", dataType = "long") })
    @RequestMapping(value = "/deleteVehiclePurpose", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteVehiclePurpose(String id) {
        try {
            if (id != null) {
                // ????????????ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                boolean flag = vehicleService.deletePurpose(id, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????????????????
     * @param ids ????????????Ids
     * @return result
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "ids ??????,??????", required = true, paramType = "query", dataType = "long") })
    @RequestMapping(value = "/deleteVehiclePurposeMuch", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteVehiclePurposeMuch(String ids) {
        try {
            if (ids != null) {
                String[] item = ids.split(",");
                List<String> list = Arrays.asList(item);
                // ????????????ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                boolean flag = vehicleService.deleteVehiclePurposeMuch(list, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????????????????
     * ???????????????
     * @param response response
     */
    @ApiIgnore
    @RequestMapping(value = "/downloadPurpose", method = RequestMethod.GET)
    public void downloadType(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            vehicleService.getVehiclePurposeTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ??????????????????
     * @return String
     * @Des ???????????????
     * @author tangshunyu
     */
    @ApiIgnore
    @RequestMapping(value = "/importTwo", method = RequestMethod.GET)
    public String importTwoPage() {
        return IMPORTWO_PAGE;
    }

    /**
     * ??????????????????
     * @param file ??????
     * @return result
     * @Des ????????????
     */
    @ApiIgnore
    @RequestMapping(value = "/importPurpose", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importType(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // ??????????????????????????????IP??????
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = vehicleService.importPurpose(file, ipAddress);
            String msg = "???????????????" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????
     * @return result
     */
    @ApiOperation(value = "??????????????????", authorizations = {
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????
     * @return result
     */
    @ApiOperation(value = "??????????????????", authorizations = {
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     * @return result
     */
    @ApiOperation(value = "????????????", authorizations = {
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????id????????????
     * @return result
     */
    @ApiOperation(value = "????????????id????????????", authorizations = {
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????????????????id????????????
     * @return
     */
    @ApiOperation(value = "??????????????????????????????id????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "brand", value = "????????????id????????????", required = true, paramType = "query",
            dataType = "int") })
    @RequestMapping(value = "/findTransportList", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public JsonResultBean findTransportList(@RequestParam(value = "brand", required = false) String brand) {
        List<Map<String, String>> list = new ArrayList<>();
        return new JsonResultBean(list);
    }

}
