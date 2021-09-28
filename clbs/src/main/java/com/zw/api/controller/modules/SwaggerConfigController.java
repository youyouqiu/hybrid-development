package com.zw.api.controller.modules;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.domain.infoconfig.form.ProfessionalForConfigFrom;
import com.zw.platform.domain.infoconfig.query.ConfigDetailsQuery;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.DeviceService;
import com.zw.platform.service.basicinfo.ProfessionalsService;
import com.zw.platform.service.basicinfo.SimcardService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 信息录入 <p>Title: ConfigController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年7月26日上午10:49:40
 */
@RestController
@RequestMapping("/swagger/m/infoconfig/infoinput")
@Api(tags = { "信息配置" }, description = "信息配置相关api接口")
public class SwaggerConfigController {
    private static final String LIST_PAGE = "modules/infoconfig/list";

    private static final String ADD_PAGE = "modules/infoconfig/infoinput/add";

    private static final String IMPORT_PAGE = "modules/infoconfig/import";

    private static final String EDIT_PAGE = "modules/infoconfig/infoinput/edit";

    private static final String DETAILS_PAGE = "modules/infoconfig/infoinput/details";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final Logger log = LogManager.getLogger(SwaggerConfigController.class);

    String[] sport;

    String[] nameSport;

    String globalConfigId = "";

    String globalParentGroupid = "";

    /**
     * 信息配置Service
     */
    @Autowired
    private ConfigService configService;

    /**
     * 终端管理Service
     */
    @Autowired
    private DeviceService deviceService;

    /**
     * Sim卡管理Service
     */
    @Autowired
    private SimcardService simcardService;

    /**
     * 车辆管理Service
     */
    @Autowired
    private VehicleService vehicleService;

    /**
     * 用户Service
     */
    @Autowired
    private UserService userService;

    @Autowired
    private ProfessionalsService professionalsService;

    @Autowired
    private AssignmentService assignmentService;

    /**
     * 查询信息配置列表
     * @return PageGridBean
     * @throws @Title: list
     * @author Liubangquan
     */
    @ApiOperation(value = "分页查询信息配置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照车牌号，终端号，sim卡号进行模糊搜索", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final ConfigQuery query) {
        Page<ConfigList> result = new Page<>();
        try {
            if (query != null) {
                boolean isNull = false; // 若缓存中数据为空，则到数据库中查询
                if (StringUtils.isBlank(query.getSimpleQueryParam())) { // 若不是条件搜索

                    String userId = SystemHelper.getCurrentUser().getId().toString();
                    // 判断redis里面是否包含当前key(user+table+type)
                    /*String key = RedisHelper.buildKey(userId, "zw_m_config", "list");
                    if (!RedisHelper.isContainsKey(key, PublicVariable.REDIS_TEN_DATABASE)) {
                        // 若redis中不包含当前key值，则从数据库中查询再保存到redis中
                        List<ConfigList> sqlList = configService.findByPage(query);
                        RedisHelper.rpush(key, sqlList, PublicVariable.REDIS_TEN_DATABASE);
                        RedisHelper.setExpire(key, VehicleStatus.ONE_HOUR, PublicVariable.REDIS_TEN_DATABASE);
                    }*/
                    // 从redis中查询数据并分页
                    // result = RedisQueryUtil.queryPageList(key, PublicVariable.REDIS_TEN_DATABASE, query);
                    if (result.getResult() == null || result.getResult().isEmpty()) {
                        isNull = true;
                    }
                }
                // 若是条件搜索,则在数据库中查询
                if (StringUtils.isNotBlank(query.getSimpleQueryParam()) || isNull) {
                    result = configService.findByPage(query);
                }
                // 此if语句主要给从业人员名称赋值
                if (null != result && result.size() > 0) {
                    for (ConfigList c : result) {
                        String[] proIds =
                            !Converter.toBlank(c.getProfessionalIds()).equals("") ? c.getProfessionalIds().split(",")
                                : null;
                        if (null != proIds && proIds.length > 0) {
                            StringBuilder proNames = new StringBuilder();
                            for (String proId : proIds) {
                                ProfessionalDTO proInfo = professionalsService.findProfessionalsById(proId);
                                if (null != proInfo) {
                                    proNames.append(proInfo.getName() + ",");
                                }
                            }
                            c.setProfessionalNames(Converter.removeStringLastChar(proNames.toString()));
                        }
                    }
                }
                return new PageGridBean(query, result, true);
            }

            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("分页查询信息配置列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        } finally {
            if (Objects.nonNull(result)) {
                // PageHelper.clearPage();
                result.close();
            }
        }
    }

    /**
     * 信息配置-信息录入界面
     * @return String
     * @throws BusinessException
     * @Title: add
     * @author Liubangquan
     */
    @ApiOperation(value = "根据config id 返回车，人，物，设备，sim卡，从业人员相关信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/addlist_{id}" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(String id) {
        try {
            JSONObject msg = new JSONObject();
            // 初始化车辆信息
            // msg.put("vehicleInfoList", configService.getVehicleInfoList(id));
            // 初始化人员信息
            msg.put("peopleInfoList", configService.getPeopleInfoList(id));
            // 初始化物品信息
            msg.put("thingInfoList", configService.getThingInfoList());
            // 初始化终端信息
            msg.put("deviceInfoList", configService.getDeviceInfoList(id));
            // 初始化终端信息(for 人员)
            msg.put("deviceInfoListForPeople", configService.getDeviceInfoListForPeople(id));
            // 初始化SIM卡信息
            msg.put("simCardInfoList", configService.getSimcardInfoList(id));
            // 初始化从业人员信息
            msg.put("professionalsInfoList", configService.getProfessionalsInfoList());
            // 初始化分组信息
            // msg.put("groupList", infoFastInputService.getgetGroupList());
            // 获取当前登录用户组织，若当前登录用户为admin，则默认其第一个下级组织
            String[] orgInfo = configService.getCurOrgId();
            msg.put("orgId", orgInfo != null ? orgInfo[0] : "");
            msg.put("orgName", orgInfo != null ? orgInfo[1] : "");
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("信息录入界面根据config id 返回车，人，物，设备，sim卡，从业人员相关信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据终端编号获取终端信息
     * @return JsonResultBean
     * @throws @Title: getDeviceInfoByDeviceNumber
     * @author Liubangquan
     */
    @ApiOperation(value = "根据设备编号返回设备信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getDeviceInfoByDeviceNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceInfoByDeviceNumber(String deviceNumber) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("deviceInfo", deviceService.findDeviceByDeviceNumber(deviceNumber));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("根据终端号获取终端信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据sim卡号获取simcard信息
     * @return JsonResultBean
     * @throws @Title: getSimcardInfoBySimcardNumber
     * @author Liubangquan
     */
    @ApiOperation(value = "根据sim卡号返回sim卡信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getSimcardInfoBySimcardNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimcardInfoBySimcardNumber(String simcardNumber) {
        try {
            JSONObject msg = new JSONObject();
            SimcardInfo si = simcardService.findVehicleBySimcardNumber(simcardNumber);
            if (si != null) {
                // 给sim卡所属企业赋值
                OrganizationLdap ol2 = userService.getOrgByUuid(si.getGroupId());
                si.setGroupName(null != ol2 ? ol2.getName() : "");
            }
            msg.put("simcardInfo", si);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 保存信息录入
     * @return JsonResultBean
     * @throws @Title: add
     * @author Liubangquan
     */
    @ApiOperation(value = "保存信息录入信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "brandID", value = "信息配置表车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "simID", value = "信息配置表sim卡id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "deviceID", value = "信息配置表设备id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "brands", value = "信息配置表车牌号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sims", value = "信息配置表sim卡号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "devices", value = "信息配置表设备编号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "car_groupId", value = "车辆所属企业id(如ou=zw,ou=organization)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "monitorType", value = "监控对象类型（0：车， 1：人）", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleType", value = "车辆类型id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "billingDate", value = "计费日期(如2017-02-16)", paramType = "query", dataType = "Date"),
        @ApiImplicitParam(name = "dueDate", value = "到期日期(如2017-02-16)", paramType = "query", dataType = "Date"),
        @ApiImplicitParam(name = "citySelID", value = "车辆所属分组id(可添加多个分组，多个用逗号隔开)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "professionalsID", value = "从业人员id(可添加多个分组，多个用逗号隔开)", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public JsonResultBean add(@ModelAttribute("addForm1") final Config1Form config1Form, final ConfigForm configForm,
        HttpServletRequest request, final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            boolean isBound = false;
            // 判断车辆、终端、SIM卡是否已经被绑定
            if (!Converter.toBlank(config1Form.getBrandID()).equals("") || !Converter.toBlank(config1Form.getDeviceID())
                .equals("") || !Converter.toBlank(config1Form.getSimID()).equals("")) {
                isBound = checkConfigIsBound(Converter.toBlank(config1Form.getBrandID()),
                    Converter.toBlank(config1Form.getDeviceID()), Converter.toBlank(config1Form.getSimID()));
            }
            if (isBound) { // 已经存在绑定关系
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            // configService.addConfig(config1Form, configForm, request);
            // 维护极速录入列表
            configService.addBindInfo(configForm.getDeviceid(), config1Form);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("保存绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 保存提交时，验证车辆、终端、sim卡是否已经被绑定，避免两个用户同时操作一条数据引起冲突
     * @param vid 车辆id
     * @param did 终端id
     * @param sid SIM卡id
     * @return boolean
     * @Title: checkConfigIsBound
     * @author Liubangquan
     */
    private boolean checkConfigIsBound(String vid, String did, String sid) {
        try {
            ConfigForm cf = configService.getIsBand(vid, did, sid, "");
            if (null != cf) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("验证车辆、终端、sim卡是否已经被绑定异常", e);
            return false;
        }
    }

    /**
     * 验证车牌号、终端、sim卡是否已被绑定
     * @param inputId
     * @param inputValue
     * @return JsonResultBean
     * @throws @author Liubangquan
     * @Title: checkIsBound
     */
    @ApiOperation(value = "验证车牌号、终端、sim卡是否已被绑定", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "inputId", value = "校验类型(可选值为brands，devices，sims)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "inputValue", value = "需要校验的值(车牌号，终端编号，终端手机号，需和校验类型一一对应)", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/checkIsBound" }, method = RequestMethod.POST)
    @ResponseBody
    private JsonResultBean checkIsBound(String inputId, String inputValue) {
        try {
            inputValue = inputValue.trim();
            boolean isBound = false;
            String str = "";
            if (Converter.toBlank(inputId).equals("brands")) {
                VehicleInfo vi = vehicleService.findByVehicle(inputValue);
                if (null != vi) {
                    isBound = checkConfigIsBound(Converter.toBlank(vi.getId()), "", "");
                    if (isBound) {
                        str = vi.getBrand();
                    }
                }
            } else if (Converter.toBlank(inputId).equals("devices")) {
                DeviceInfo di = deviceService.findByDevice(inputValue);
                if (null != di) {
                    isBound = checkConfigIsBound("", Converter.toBlank(di.getId()), "");
                    if (isBound) {
                        str = di.getDeviceNumber();
                    }
                }
            } else if (Converter.toBlank(inputId).equals("sims")) {
                SimcardInfo si = simcardService.findBySIMCard(inputValue);
                if (null != si) {
                    isBound = checkConfigIsBound("", "", Converter.toBlank(si.getId()));
                    if (isBound) {
                        str = si.getSimcardNumber();
                    }
                }
            }
            JSONObject msg = new JSONObject();
            msg.put("isBound", isBound);
            msg.put("boundName", str);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("验证车牌号、终端、sim卡是否已被绑定异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除config
     * @param id
     * @return JsonResultBean
     * @throws BusinessException
     * @Title: delete
     * @author Liubangquan
     */
    @ApiOperation(value = "解除绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id, HttpServletRequest req) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(req);// 获得访问ip
                List<String> ids = Arrays.asList(id);
                // JSONObject msg = configService.delete(ids, ip);
                return new JsonResultBean("");
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据id解除绑定关系发生异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @ApiOperation(value = "根据车辆id获取信息配置表config id", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getConfigIdByVehicleId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getConfigIdByVehicleId(final String vehicleId) {
        try {
            JSONObject msg = new JSONObject();
            String configId = configService.getConfigIdByVehicleId(vehicleId);
            msg.put("configId", configId);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取信息配置信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除Config
     * @param request
     * @return JsonResultBean
     * @throws BusinessException
     * @Title: deleteMore
     * @author Liubangquan
     */
    @ApiOperation(value = "根据ids批量解除绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量解除绑定关系的绑定关系表ids(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                // configService.delete(ids, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量解除绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出信息配置列表到excel
     * @param response
     * @param request
     * @return void
     * @throws UnsupportedEncodingException
     * @Title: export
     * @author Liubangquan
     */
    @ApiOperation(value = "导出信息配置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, HttpServletRequest request) {
        try {
            //List<OrganizationLdap> orgLdapList = userService.getAllOrganization(); // 获取分组信息
            String filename = "信息配置列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            //configService.exportConfig(null, 1, response, orgLdapList);
            // configService.exportConfig(null, 1, response);
        } catch (Exception e) {
            log.error("导出信息配置列表异常", e);
        }
    }

    /**
     * 模板下载
     * @param response
     * @param request
     * @return void
     * @throws UnsupportedEncodingException
     * @Title: download
     * @author Liubangquan
     */
    @ApiOperation(value = "下载信息配置导入模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, HttpServletRequest request) {
        try {
            String filename = "信息列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            configService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载信息配置导入模板异常", e);
        }
    }

    /**
     * 配置详情
     */
    @ApiOperation(value = "根据绑定表id获取信息配置详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getConfigDetails_{configId}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getConfigDetails(@PathVariable final String configId) {
        try {
            ModelAndView mav = new ModelAndView(DETAILS_PAGE);
            ConfigDetailsQuery configDetail;
            if ("0".equals(configService.findMonitorTypeById(configId))) {
                configDetail = configService.configDetails(configId);// 查询车辆详情信息
            } else {
                configDetail = configService.peopleConfigDetails(configId);// 查询人员详情信息
                if ("1".equals(configDetail.getGender())) {
                    configDetail.setGender("男");
                } else {
                    configDetail.setGender("女");
                }
            }
            dealConfigData(configDetail); // 处理配置详情数据
            if (configDetail != null) {
                // 给车辆所属企业赋值
                OrganizationLdap ol = userService.getOrgByUuid(configDetail.getParentGroupid());
                configDetail.setParentGroupname(null != ol ? ol.getName() : "");
                // 给终端所属企业赋值
                OrganizationLdap ol1 = userService.getOrgByUuid(configDetail.getDeviceParentGroupid());
                configDetail.setDeviceParentGroupname(null != ol1 ? ol1.getName() : "");
                // 给sim卡所属企业赋值
                OrganizationLdap ol2 = userService.getOrgByUuid(configDetail.getSimParentGroupid());
                configDetail.setSimParentGroupname(null != ol2 ? ol2.getName() : "");
                // 所属企业id
                globalParentGroupid = Converter.toBlank(configDetail.getParentGroupid());
            }
            mav.addObject("result", configDetail);
            // 获取从业人员信息
            List<ProfessionalForConfigFrom> professionalForConfigList =
                configService.getProfessionalForConfigListByConfigId(configId);
            if (null != professionalForConfigList && professionalForConfigList.size() > 0) {
                sport = new String[professionalForConfigList.size()];
                for (int i = 0; i < professionalForConfigList.size(); i++) {
                    ProfessionalForConfigFrom professionalForConfigFrom = professionalForConfigList.get(i);
                    sport[i] = professionalForConfigFrom.getProfessionalsid();
                }
            } else {
                sport = null;
            }
            // 获取分组信息-此分组从车辆对应分组表中来
            globalConfigId = configId;
            return mav;
        } catch (Exception e) {
            log.error("配置详情界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 处理配置详情数据 车牌颜色：0-蓝；1-黄；2-白；3-黑 是否视频：0-否；1-是 启停状态：0-停用；1-启用 设备类型：1-交通部JTB808
     * @param configDetail
     * @return void
     * @Title: dealConfigData
     * @author Liubangquan
     */
    private void dealConfigData(ConfigDetailsQuery configDetail) {
        if (null != configDetail) {
            Integer plateColor = configDetail.getPlateColor();
            configDetail.setPlateColorStr(getPlateColorStr(Converter.toInteger(plateColor, -1)));

            Integer isVideo = configDetail.getIsVideo();
            configDetail.setIsVideoStr(getIsVideoStr(Converter.toInteger(isVideo, -1)));

            Integer isStart = configDetail.getIsStart();
            configDetail.setIsStartStr(getIsStartStr(Converter.toInteger(isStart, -1)));

            String deviceType = configDetail.getDeviceType();
            configDetail.setDeviceType(getDeviceTypeStr(Converter.toInteger(deviceType, -1)));

            String deviceFunctionalType = configDetail.getFunctionalType();
            configDetail.setFunctionalType(getDeviceFunctionalTypeStr(Converter.toInteger(deviceFunctionalType, -1)));

            Integer channelNumber = configDetail.getChannelNumber();
            configDetail.setChannelNumberStr(getChannelNumberStr(Converter.toInteger(channelNumber, -1)));
        }
    }

    /**
     * 根据设备功能类型的value获取其中文意义的值
     * @param functionalType
     * @return String
     * @throws @author Liubangquan
     * @Title: getDeviceFunctionalTypeStr
     */
    private String getDeviceFunctionalTypeStr(int functionalType) {
        if (Converter.toBlank(functionalType).equals("1")) {
            return "简易型车机";
        } else if (Converter.toBlank(functionalType).equals("2")) {
            return "行车记录仪";
        } else if (Converter.toBlank(functionalType).equals("3")) {
            return "对讲设备";
        } else if (Converter.toBlank(functionalType).equals("4")) {
            return "手咪设备";
        } else if (Converter.toBlank(functionalType).equals("5")) {
            return "超长待机设备";
        } else if (Converter.toBlank(functionalType).equals("6")) {
            return "定位终端";
        } else {
            return "";
        }
    }

    /**
     * 根据车牌颜色的整型值获取其实际意义的值
     * @param plateColor
     * @return String
     * @Title: getPlateColorStr
     * @author Liubangquan
     */
    private String getPlateColorStr(int plateColor) {
        return PlateColor.getNameOrBlankByCode(plateColor);
    }

    /**
     * 根据是否视频的整型值获取其实际意义的值
     * @param isVideo
     * @return String
     * @Title: getIsVideoStr
     * @author Liubangquan
     */
    private String getIsVideoStr(int isVideo) {
        if (Converter.toBlank(isVideo).equals("0")) {
            return "否";
        } else if (Converter.toBlank(isVideo).equals("1")) {
            return "是";
        } else {
            return "";
        }
    }

    /**
     * 根据启用、停用的整型值获取其实际意义的值
     * @param isStart
     * @return String
     * @Title: getIsStartStr
     * @author Liubangquan
     */
    private String getIsStartStr(int isStart) {
        if (Converter.toBlank(isStart).equals("0")) {
            return "停用";
        } else if (Converter.toBlank(isStart).equals("1")) {
            return "启用";
        } else {
            return "";
        }
    }

    /**
     * 根据设备类型的整型值获取其实际意义的值
     * @param deviceType
     * @return String
     * @Title: getDeviceTypeStr
     * @author Liubangquan
     */
    private String getDeviceTypeStr(int deviceType) {
        if (Converter.toBlank(deviceType).equals("1")) {
            return "交通部JTB808";
        } else if (Converter.toBlank(deviceType).equals("2")) {
            return "移为GV320";
        } else if (Converter.toBlank(deviceType).equals("3")) {
            return "天禾";
        } else if (Converter.toBlank(deviceType).equals("5")) {
            return "北斗天地协议";
        } else {
            return "";
        }
    }

    /**
     * 根据通道数的整型值获取其实际意义的值
     * @param channelNumber
     * @return String
     * @Title: getChannelNumberStr
     * @author Liubangquan
     */
    private String getChannelNumberStr(int channelNumber) {
        if (Converter.toBlank(channelNumber).equals("1")) {
            return "4";
        } else if (Converter.toBlank(channelNumber).equals("2")) {
            return "5";
        } else if (Converter.toBlank(channelNumber).equals("3")) {
            return "8";
        } else if (Converter.toBlank(channelNumber).equals("4")) {
            return "16";
        } else {
            return "";
        }
    }

    /**
     * 根据configid获取与其对应的分组信息
     * @return JSONArray
     * @Title: getGroups
     * @author Liubangquan
     */
    @RequestMapping(value = { "/getGroups" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getGroups() {
        // 获取所有分组数据
        List<Assignment> assignmentList = null;
        if ("0".equals(configService.findMonitorTypeById(globalConfigId))) {
            assignmentList = configService.getAssignmentByConfigId(globalConfigId);
        } else {
            assignmentList = configService.getPeopleAssignmentByConfigId(globalConfigId);
        }
        // 给分组的上级组织名称赋值
        setGroupName(assignmentList);
        JSONArray array = new JSONArray();
        if (assignmentList != null && assignmentList.size() > 0) {
            for (int i = 0; i < assignmentList.size(); i++) {
                array.add(assignmentList.get(i));
            }
        }
        return array;
    }

    /**
     * 详情界面-所属企业数据封装
     * @return JSONArray
     * @throws @Title: getParentGroup
     * @author Liubangquan
     */
    @ApiOperation(value = "所属企业数据封装", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getParentGroup" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getParentGroup() {
        // 获取所有组织数据
        List<OrganizationLdap> orgLdapList = userService.getAllOrganization();
        JSONArray array = new JSONArray();
        // global_parent_groupid.split("#").length == 1 这是为了防止之前需求所产生的错数据而加的条件
        if (!Converter.toBlank(globalParentGroupid).equals("") && globalParentGroupid.split("#").length == 1) {
            for (int j = 0; j < orgLdapList.size(); j++) {
                if (Converter.toBlank(globalParentGroupid).equals(Converter.toBlank(orgLdapList.get(j).getId()))) {
                    array.add(orgLdapList.get(j));
                }
            }
        }
        return array;
    }

    /**
     * 给对应分组的上级组织名称赋值
     * @param assignmentList
     * @return void
     * @throws @Title: setGroupName
     * @author Liubangquan
     */
    private void setGroupName(List<Assignment> assignmentList) {
        List<OrganizationLdap> orgLdapList = userService.getAllOrganization();
        if (null != assignmentList && assignmentList.size() > 0 && null != orgLdapList && orgLdapList.size() > 0) {
            for (Assignment a : assignmentList) {
                for (OrganizationLdap ol : orgLdapList) {
                    if (Converter.toBlank(a.getGroupId()).equals(Converter.toBlank(ol.getId()))) {
                        a.setGroupName(Converter.toBlank(ol.getName()));
                        break;
                    }
                }
            }
        }
    }

    /**
     * 根据configid获取与其对应的从业人员信息
     * @return JSONArray
     * @Title: getProfessionals
     * @author Liubangquan
     */
    @ApiOperation(value = "根据configid获取与其对应的从业人员信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getprofessionals" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getProfessionals() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < sport.length; i++) {
            array.add(professionalsService.findProfessionalsById(sport[i]));
        }
        return array;
    }

    /**
     * 配置详情
     */
    @ApiOperation(value = "获取配置详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getConfigDetailsAll" }, method = RequestMethod.GET)
    @ResponseBody
    public List<ConfigDetailsQuery> getConfigDetailsAll() {
        return configService.configDetailsall();
    }

    /**
     * 修改
     * @throws BusinessException
     */
    @ApiOperation(value = "根据config id获取信息配置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{configId}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String configId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // mav.addObject("result", configService.getConfigInfo(configId));
            return mav;
        } catch (Exception e) {
            log.error("修改信息配置界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改
     */
    @ApiOperation(value = "修改信息配置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "configId", value = "信息配置表id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "brandID", value = "信息配置表车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "simID", value = "信息配置表sim卡id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "deviceID", value = "信息配置表设备id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "brands", value = "信息配置表车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sims", value = "信息配置表sim卡id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "devices", value = "信息配置表设备id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "monitorType", value = "监控对象类型（0：车， 1：人）", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleType", value = "车辆类型id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "billingDateStr", value = "计费日期(如2017-02-16)", paramType = "query", dataType = "Date"),
        @ApiImplicitParam(name = "durDateStr", value = "到期日期(如2017-02-16)", paramType = "query", dataType = "Date"),
        @ApiImplicitParam(name = "groupid", value = "车辆所属分组id(可添加多个分组，多个用逗号隔开)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "professionalsId", value = "从业人员id(可添加多个分组，多个用逗号隔开)", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final ConfigForm form,
        final BindingResult bindingResult, HttpServletRequest req) {
        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        } else {
            try {
                // return configService.updateConfig(form, req);
                return null;
            } catch (Exception e) {
                log.error("修改信息配置异常", e);
                return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
            }
        }
    }

    /**
     * 根据车辆id查询车辆详情
     * @param vehicleId
     * @return JsonResultBean
     * @Title: getVehicleInfoById
     * @author Liubangquan
     */
    @ApiOperation(value = "根据车辆id查询车辆详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getVehicleInfoById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleInfoById(String vehicleId) {
        try {
            JSONObject msg = new JSONObject();
            VehicleInfo vi = vehicleService.findVehicleById(vehicleId);
            if (vi != null) {
                OrganizationLdap ol = userService.getOrgByUuid(vi.getGroupId());
                vi.setGroupName(ol != null ? ol.getName() : "");
            }
            msg.put("vehicleInfo", vi);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询车辆详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据终端id查询终端详情
     * @param deviceId
     * @return JsonResultBean
     * @Title: getDeviceInfoDetailById
     * @author Liubangquan
     */
    @ApiOperation(value = "根据终端id查询终端详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getDeviceInfoDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceInfoDetailById(String deviceId) {
        try {
            JSONObject msg = new JSONObject();
            DeviceInfo di = deviceService.findDeviceById(deviceId);
            if (di != null) {
                OrganizationLdap ol = userService.getOrgByUuid(di.getGroupId());
                di.setGroupName(ol != null ? ol.getName() : "");
            }
            msg.put("deviceInfo", di);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("根据终端id查询终端详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据sim卡id查询SIM卡详情
     * @param simcardId
     * @return JsonResultBean
     * @Title: getSimCardInfoDetailById
     * @author Liubangquan
     */
    @ApiOperation(value = "根据sim卡id查询SIM卡详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getSimCardInfoDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimCardInfoDetailById(String simcardId) {
        try {
            JSONObject msg = new JSONObject();
            SimcardInfo si = simcardService.findSimcardById(simcardId);
            if (si != null) {
                OrganizationLdap ol = userService.getOrgByUuid(si.getGroupId());
                si.setGroupName(ol != null ? ol.getName() : "");
            }
            msg.put("simcardInfo", si);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询SIM卡详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据从业人员id查询从业人员详情
     * @param professionalId
     * @return JsonResultBean
     * @Title: getProfessionalDetailById
     * @author Liubangquan
     */
    @ApiOperation(value = "根据从业人员id查询从业人员详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getProfessionalDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getProfessionalDetailById(String professionalId) {
        JSONObject msg = new JSONObject();
        msg.put("professionalInfo", professionalsService.findProfessionalsById(professionalId));
        return new JsonResultBean(msg);
    }

    /**
     * 根据组织id查询组织详情
     * @param groupId
     * @return JsonResultBean
     * @Title: getGroupDetail
     * @author Liubangquan
     */
    @ApiOperation(value = "根据组织id查询组织详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getGroupDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getGroupDetail(String groupId) {
        try {
            JSONObject msg = new JSONObject();
            // 获取所有分组数据
            /*
             * List<OrganizationLdap> orgLdapList = userService.getAllOrganization(); if (null != orgLdapList &&
             * orgLdapList.size() > 0) { for (OrganizationLdap ol : orgLdapList) { if
             * (Converter.toBlank(ol.getId()).equals(groupId)) { if (Converter.toBlank(ol.getId()).split(",").length >
             * 1) { // 有上级组织 String orgId = Converter.toBlank(ol.getId()); String orgPId = ""; int beginIndex =
             * orgId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门) if (beginIndex > 0) { orgPId = orgId.substring(beginIndex +
             * 1); ol.setPid(userService.getOrganizationById(orgPId).getName()); } } else { // 无上级组织 ol.setPid(""); }
             * msg.put("groupInfo", ol); return new JsonResultBean(msg); } } }
             */
            Assignment assignment = assignmentService.findAssignmentById(groupId);
            msg.put("groupInfo", assignment);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询组织详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 获取分组树
     * @param isOrg
     * @return String
     * @throws BusinessException
     * @throws @Title:           getTree
     * @author Liubangquan
     */
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "获取分组树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    public String getTree(String isOrg) {
        String result = null;
        try {

            JSONArray assignmentTree = assignmentService.getAssignmentTree();
            if (CollectionUtils.isNotEmpty(assignmentTree)) {
                for (int i = 0; i < assignmentTree.size(); i++) {
                    JSONObject job = (JSONObject) assignmentTree.get(i);
                    OrganizationLdap ol = null;
                    if (!Converter.toBlank(job.get("pId")).equals("")) {
                        ol = userService.findOrganization(Converter.toBlank(job.get("pId")));
                    }
                    if (null != ol) {
                        job.put("pName", Converter.toBlank(ol.getName()));
                    } else {
                        job.put("pName", "");
                    }

                    if (Converter.toBlank(job.get("type")).equals("assignment")) {
                        job.put("iconSkin", "assignmentSkin");
                    }
                }
                result = assignmentTree.toJSONString();
            }

        } catch (Exception e) {
            log.error("获取分组树异常", e);
        }
        return result;
    }

    /**
     * 校验当前分组下的最大车辆数是否已经达到上限
     * @param
     * @return JsonResultBean
     * @throws @author Liubangquan
     * @Title: checkMaxVehicleCountOfAssignment
     */
    @ApiOperation(value = "校验当前分组下的最大车辆数是否已经达到上限", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/checkMaxVehicleCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkMaxVehicleCountOfAssignment(String assignmentId, String assignmentName) {
        try {
            return configService.isVehicleCountExceedMaxNumber(assignmentId, assignmentName);
        } catch (Exception e) {
            log.error("校验当前分组下的最大车辆数是否已经达到上限异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}
