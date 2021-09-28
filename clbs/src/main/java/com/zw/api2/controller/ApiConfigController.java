package com.zw.api2.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.api2.swaggerEntity.SwaggerConfigAddForm;
import com.zw.api2.swaggerEntity.SwaggerConfigUpdateForm;
import com.zw.api2.swaggerEntity.SwaggerSimpleConfigQuery;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.domain.infoconfig.form.ProfessionalForConfigFrom;
import com.zw.platform.domain.infoconfig.query.ConfigDetailsQuery;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.domain.topspeed_entering.DeviceRegister;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.DeviceService;
import com.zw.platform.service.basicinfo.ProfessionalsService;
import com.zw.platform.service.basicinfo.SimcardService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.service.topspeed.TopSpeedService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.imports.ProgressDetails;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 信息录入 <p> Title: ConfigController.java </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p>
 * team: ZhongWeiTeam </p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年7月26日上午10:49:40
 */
@RestController
@RequestMapping("/api/m/infoconfig/infoinput")
@Api(tags = { "信息配置dev" }, description = "信息配置相关api接口")
public class ApiConfigController {
    private static final String LIST_PAGE = "modules/infoconfig/list";

    private static final String ADD_PAGE = "modules/infoconfig/infoinput/add";

    private static final String IMPORT_PAGE = "modules/infoconfig/import";

    private static final String EDIT_PAGE = "modules/infoconfig/infoinput/edit";

    private static final String DETAILS_PAGE = "modules/infoconfig/infoinput/details";

    private static Logger log = LogManager.getLogger(ApiConfigController.class);

    private String[] sport;

    private String globalConfigId = "";

    private String globalParentGroupid = "";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

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

    @Autowired
    private TopSpeedService topSpeedService;

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
     * 信息配置列表页面
     * @return String
     * @Title: listPage
     * @author Liubangquan
     */
    @ApiIgnore
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询信息配置列表
     * @return PageGridBean
     * @Title: list
     * @author Liubangquan
     */
    @ApiOperation(value = "查询信息配置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(
        @ModelAttribute("swaggerSimpleConfigQuery") final SwaggerSimpleConfigQuery swaggerSimpleConfigQuery) {
        ConfigQuery query = new ConfigQuery();
        try {
            // BeanUtils.copyProperties(query, swaggerSimpleConfigQuery);
            // Page<ConfigList> result = configService.listConfigInfo(query);
            // return new PageGridBean(query, result, true);
            return null;
        } catch (Exception e) {
            log.error("查询信息配置列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @ApiIgnore
    @RequestMapping(value = { "/addlist" }, method = RequestMethod.GET)
    public String addList() {
        return ADD_PAGE;
    }

    /**
     * 极速录入查询数据
     */
    @ApiOperation(value = "极速录入查询数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/topspeedlist" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean topSpeedList() {
        try {
            JSONObject msg = new JSONObject();
            List<DeviceRegister> list = topSpeedService.findDeviceData();
            msg.put("list", list);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } catch (Exception e) {
            log.error("查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据终端编号获取终端信息
     * @return JsonResultBean
     * @Title: getDeviceInfoByDeviceNumber
     * @author Liubangquan
     */
    @ApiOperation(value = "根据终端编号获取终端信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deviceNumber", value = "设备编号", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/getDeviceInfoByDeviceNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceInfoByDeviceNumber(String deviceNumber) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("deviceInfo", deviceService.findDeviceByDeviceNumber(deviceNumber));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("根据终端号获取终端信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据sim卡号获取simcard信息
     * @return JsonResultBean
     * @Title: getSimcardInfoBySimcardNumber
     * @author Liubangquan
     */
    @ApiOperation(value = "根据终端id查询终端详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simcardNumber", value = "simcard的卡号", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/getSimcardInfoBySimcardNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimcardInfoBySimcardNumber(String simcardNumber) {
        try {
            JSONObject msg = new JSONObject();
            SimcardInfo si = simcardService.findVehicleBySimcardNumber(simcardNumber);
            // 给sim卡所属企业赋值
            OrganizationLdap ol2 = userService.getOrgByUuid(si.getGroupId());
            si.setGroupName(null != ol2 ? ol2.getName() : "");
            msg.put("simcardInfo", si);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据组织名称获取组织id
     * @return JsonResultBean
     * @Title: getGroupIdByGroupName
     * @author Liubangquan
     */
    @ApiIgnore
    @RequestMapping(value = { "/getGroupIdByGroupName" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getGroupIdByGroupName(String cid) {
        try {
            JSONObject msg = new JSONObject();
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取组织信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 保存信息录入
     * @return JsonResultBean
     * @Title: add
     * @author Liubangquan
     */
    @ApiOperation(value = "保存信息录入信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@ModelAttribute("addForm1") SwaggerConfigAddForm swaggerConfigAddForm,
        HttpServletRequest request) {
        ConfigForm configForm = new ConfigForm();
        Config1Form config1Form = new Config1Form();

        try {
            BeanUtils.copyProperties(configForm, swaggerConfigAddForm);
            BeanUtils.copyProperties(config1Form, swaggerConfigAddForm);
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
            // 维护绑定信息
            configService.addBindInfo(configForm.getDeviceid(), config1Form);
            if (config1Form.getMonitorType().equals("0")) {
                ZMQFencePub.pubChangeFence("1," + config1Form.getBrandID());
            } else if (config1Form.getMonitorType().equals("1")) {
                ZMQFencePub.pubChangeFence("2");
            } else if (config1Form.getMonitorType().equals("2")) {
                ZMQFencePub.pubChangeFence("17");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("保存绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
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
            return null != cf;
        } catch (Exception e) {
            log.error("验证车辆、终端、sim卡是否已经被绑定异常", e);
            return false;
        }
    }

    /**
     * 检验sim卡是否已经绑定了
     */
    @ApiOperation(value = "检验sim卡是否已经绑定了", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "did", value = "设备id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sid", value = "sim卡id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/checkSimIsBound" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkSimIsBound(String vid, String did, String sid) {
        try {
            SimcardInfo simcardInfo = simcardService.findVehicleBySimcardNumber(sid);
            List<ConfigForm> cf = new ArrayList<>();
            if (simcardInfo != null) {
                cf = configService.getIsBands(vid, did, simcardInfo.getId(), "");
            }
            JSONObject msg = new JSONObject();
            if (null != cf) {
                msg.put("msg", cf);
                return new JsonResultBean(msg);
            } else {
                return new JsonResultBean(msg);
            }
        } catch (Exception e) {
            log.error("检查sim卡是否绑定异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 验证车牌号、终端、sim卡是否已被绑定
     * @param inputId
     * @param inputValue
     * @return JsonResultBean
     * @Title: checkIsBound
     * @author Liubangquan
     */
    @ApiOperation(value = "验证车牌号、人、终端、sim卡是否已被绑定", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "inputId", value = "校验类型(可选值为brands，devices，sims)", required = true,
            paramType = "query",  dataType = "string"),
        @ApiImplicitParam(name = "inputValue", value = "需要校验的值(车牌号，终端编号，终端手机号，需和校验类型一一对应)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "monitorType", value = "要校验的类型（0：车 1：人 2：物）", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/checkIsBound" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkIsBound(String inputId, String inputValue, Integer monitorType) {
        try {
            inputValue = inputValue.trim();
            boolean isBound = false;
            String str = "";//校验的监控对象编号/终端编号/sim卡号
            String checkId = "";//校验对象的id
            if (Converter.toBlank(inputId).equals("brands") && monitorType != null) {
                switch (monitorType) {
                    case 0://车
                        VehicleInfo vi = vehicleService.findByVehicle(inputValue);
                        if (null != vi) {
                            str = vi.getBrand();
                            checkId = vi.getId();
                        }
                        break;
                    case 1://人
                        // Personnel pi = personnelService.findByNumber(inputValue);
                        Personnel pi = null;
                        if (null != pi) {
                            str = pi.getPeopleNumber();
                            checkId = pi.getId();
                        }
                        break;
                    case 2://物
                        // ThingInfo ti = thingInfoService.findByThingInfo(inputValue);
                        ThingInfo ti = null;
                        if (null != ti) {
                            str = ti.getThingNumber();
                            checkId = ti.getId();
                        }
                        break;
                    default:
                        break;
                }
                //校验监控对象是否绑定
                if (StringUtils.isNotBlank(checkId)) {
                    isBound = checkConfigIsBound(Converter.toBlank(checkId), "", "");
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
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id删除config
     * @param id
     * @return JsonResultBean
     * @Title: delete
     * @author Liubangquan
     */
    @ApiOperation(value = "解除绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") @ApiParam("信息配置id") final String id,
        HttpServletRequest request) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                List<String> ids = Arrays.asList(id);
                // JSONObject msg = configService.delete(ids, ip);
                //通知Storm数据变更
                // configService.sendMonitorDataMaintain(msg, msg.getString("vehicleId"));
                // ZMQFencePub.pubChangeFence("16,0," + msg.getString("vehicleId"));
                return new JsonResultBean("msg");
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据id解除绑定关系发生异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @ApiOperation(value = "根据车辆id获取信息配置id", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getConfigIdByVehicleId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getConfigIdByVehicleId(final String vehicleId) {
        try {
            JSONObject msg = new JSONObject();
            String configId = configService.getConfigIdByVehicleId(vehicleId);
            msg.put("configId", configId);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除Config
     * @param request
     * @return JsonResultBean
     * @Title: deleteMore
     * @author Liubangquan
     */
    @ApiOperation(value = "根据ids批量解除绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
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
                // JSONObject msg = configService.delete(ids, ip);
                // //通知Storm数据变更
                // configService.sendMonitorDataMaintain(msg, msg.getString("vehicleId"));
                // ZMQFencePub.pubChangeFence("16,0," + msg.getString("vehicleId"));
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
     * @Title: export
     * @author Liubangquan
     */
    @ApiIgnore
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, HttpServletRequest request) {
        try {
            ExportExcelUtil.setResponseHead(response, "信息配置列表");
            // configService.exportConfig(null, 1, response);
        } catch (Exception e) {
            log.error("导出信息配置列表异常", e);
        }
    }

    /**
     * 导入Config页面
     * @return String
     * @Title: importPage
     * @author Liubangquan
     */
    @ApiIgnore
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }


    /**
     * 货运导入Config
     * @param file    file
     * @param request request
     * @return JsonResultBean
     */
    @ApiIgnore
    @RequestMapping(value = "/importTransport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importTransport(@RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest request) {
        try {
            ProgressDetails progress = new ProgressDetails();
            request.getSession().setAttribute("CONFIG_IMPORT_PROGRESS", progress);
            // return configService.importTransportConfigAndSend(file, progress, request);
            return null;
        } catch (Exception e) {
            log.error("导入信息配置信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/importProgress", method = RequestMethod.GET)
    @ResponseBody
    public int importProgress(HttpServletRequest request) {
        ProgressDetails progress = (ProgressDetails) request.getSession().getAttribute("CONFIG_IMPORT_PROGRESS");
        return Optional.ofNullable(progress).orElse(new ProgressDetails()).getProgress();
    }

    /**
     * 模板下载
     * @param response
     * @param request
     * @return void
     * @Title: download
     * @author Liubangquan
     */
    @ApiIgnore
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, HttpServletRequest request) {
        try {
            ExportExcelUtil.setResponseHead(response, "信息列表模板");
            configService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载信息列表模板异常", e);
        }
    }

    /**
     * 配置详情
     */
    @ApiOperation(value = "配置详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getConfigDetails_{configId}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getConfigDetails(@PathVariable("configId") @ApiParam("信息配置id") final String configId,
        HttpServletRequest request) {
        try {
            ModelAndView mav = new ModelAndView(DETAILS_PAGE);
            ConfigDetailsQuery configDetail = null;
            //获取监控对象类型
            String monitorType = configService.findMonitorTypeById(configId);
            if ("0".equals(monitorType)) {
                configDetail = configService.configDetails(configId);// 查询车辆详情信息
            } else if ("1".equals(monitorType)) {
                configDetail = configService.peopleConfigDetails(configId);// 查询人员详情信息
                if ("1".equals(configDetail.getGender())) {
                    configDetail.setGender("男");
                } else {
                    configDetail.setGender("女");
                }
            } else {
                configDetail = configService.thingConfigDetails(configId);
                //获取物品类别、类型字典
                Map<String, String> thingCategoryDetail =
                    (Map) request.getSession().getAttribute("thingCategoryDetail");
                Map<String, String> thingTypeDetail = (Map) request.getSession().getAttribute("thingTypeDetail");
                configDetail.setThingCategoryName(thingCategoryDetail.get(configDetail.getThingCategory()));
                configDetail.setThingTypeName(thingTypeDetail.get(configDetail.getThingType()));
            }
            dealConfigData(configDetail); // 处理配置详情数据
            // 给车辆所属企业赋值
            OrganizationLdap ol = userService.getOrgByUuid(null != configDetail ? configDetail.getParentGroupid() : "");
            if (configDetail != null) {
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
            if (null != professionalForConfigList && !professionalForConfigList.isEmpty()) {
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
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("配置详情界面弹出异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 处理配置详情数据 车牌颜色：0-蓝；1-黄；2-白；3-黑 是否视频：0-否；1-是 启停状态：0-停用；1-启用 设备类型：1-交通部JT808
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
            configDetail.setDeviceType(ProtocolEnum.getDeviceNameByDeviceType(deviceType));

            String deviceFunctionalType = configDetail.getFunctionalType();
            configDetail.setFunctionalType(getDeviceFunctionalTypeStr(Converter.toInteger(deviceFunctionalType, -1)));

            Integer channelNumber = configDetail.getChannelNumber();
            configDetail.setChannelNumberStr(getChannelNumberStr(Converter.toInteger(channelNumber, -1)));
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
     * 根据设备功能类型的value获取其中文意义的值
     * @param functionalType
     * @return String
     * @Title: getDeviceFunctionalTypeStr
     * @author Liubangquan
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
     * 获取组织数据
     * @return JSONArray
     * @Title: getGroups
     * @author Liubangquan
     */
    @ApiOperation(value = "根据configid获取与其对应的分组信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })

    @RequestMapping(value = { "/getGroups" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getGroups() {
        List<Assignment> assignmentList;
        //获取监控对象类型
        String monitorType = configService.findMonitorTypeById(globalConfigId);
        if ("0".equals(monitorType)) {
            assignmentList = configService.getAssignmentByConfigId(globalConfigId);
        } else if ("1".equals(monitorType)) {
            assignmentList = configService.getPeopleAssignmentByConfigId(globalConfigId);
        } else {
            assignmentList = configService.getThingAssignmentByConfigId(globalConfigId);
        }
        // 给分组的上级组织名称赋值
        setGroupName(assignmentList);
        JSONArray array = new JSONArray();
        if (assignmentList != null && !assignmentList.isEmpty()) {
            for (int i = 0; i < assignmentList.size(); i++) {
                array.add(assignmentList.get(i));
            }
        }
        return array;
    }

    /**
     * 详情界面-所属企业数据封装
     * @return JSONArray
     * @Title: getParentGroup
     * @author Liubangquan
     */
    @ApiOperation(value = "详情界面-所属企业数据封装", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
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
     * @Title: setGroupName
     * @author Liubangquan
     */
    private void setGroupName(List<Assignment> assignmentList) {
        List<OrganizationLdap> orgLdapList = userService.getAllOrganization();
        if (null != assignmentList && !assignmentList.isEmpty() && null != orgLdapList && !orgLdapList.isEmpty()) {
            for (Assignment a : assignmentList) {
                for (OrganizationLdap ol : orgLdapList) {
                    if (Converter.toBlank(a.getGroupId()).equals(Converter.toBlank(ol.getUuid()))) {
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
    @ApiOperation(value = "获取从业人员", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getprofessionals" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getProfessionals() {
        JSONArray array = new JSONArray();
        try {
            if (sport != null) {
                for (int i = 0; i < sport.length; i++) {
                    Map<String, Object> resultMap = professionalsService.findProGroupById(sport[i]);
                    String groupId = (String) resultMap.get("groupName");
                    // 重组返回结果
                    ProfessionalsForm form = new ProfessionalsForm();
                    ConvertUtils.register(form, Date.class);
                    BeanUtils.populate(form, resultMap);
                    form.setGroupId(groupId);
                    array.add(form);
                }
            }
        } catch (Exception e) {
            log.error("根据configid获取与其对应的从业人员信息异常", e);
        }
        return array;
    }

    /**
     * 配置详情
     */
    @ApiIgnore
    @RequestMapping(value = { "/getConfigDetailsAll" }, method = RequestMethod.GET)
    @ResponseBody
    public List<ConfigDetailsQuery> getConfigDetailsAll() {
        return configService.configDetailsall();
    }

    /**
     * 修改
     */
    @ApiOperation(value = "修改信息配置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{configId}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable final @ApiParam("信息配置id") String configId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // mav.addObject("result", configService.getConfigInfo(configId));
            mav.addObject("VehicleTypeList", vehicleService.getVehicleTypeList());
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("修改信息配置弹出界面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改
     */
    /**
     * 修改
     */
    @ApiOperation(value = "修改信息配置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({
        ValidGroupUpdate.class }) @ModelAttribute("form") final SwaggerConfigUpdateForm swaggerConfigUpdateForm,
        final BindingResult bindingResult, HttpServletRequest request) {
        ConfigForm form = new ConfigForm();

        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        } else {
            try {
                // BeanUtils.copyProperties(form, swaggerConfigUpdateForm);
                // return configService.updateOrAdd(form, request, true);
                return null;
            } catch (Exception e) {
                log.error("修改信息配置异常", e);
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
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
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getVehicleInfoById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleInfoById(String vehicleId) {
        try {
            JSONObject msg = new JSONObject();
            VehicleInfo vi = vehicleService.findVehicleById(vehicleId);
            OrganizationLdap ol = userService.getOrgByUuid(vi != null ? vi.getGroupId() : "");
            if (vi != null) {
                vi.setGroupName(ol != null ? ol.getName() : "");
            }
            msg.put("vehicleInfo", vi);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询车辆详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据人员id查询人员详情
     * @param peopleId
     * @return JsonResultBean
     * @Title: getPeopleInfoById
     * @author hujun
     */
    @ApiOperation(value = "根据人员id查询人员详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "peopleId", value = "人员id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getPeopleInfoById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPeopleInfoById(String peopleId) {
        try {
            JSONObject msg = new JSONObject();
            // Personnel person = personnelService.get(peopleId);
            msg.put("peopleInfo", null);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询人员详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据物品id查询物品详情
     * @param thingId
     * @return
     */
    @ApiOperation(value = "根据物品id查询物品详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "thingId", value = "物品id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getThingInfoById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getThingInfoById(String thingId, HttpServletRequest request) {
        try {
            JSONObject msg = new JSONObject();
            // ThingInfo ti = thingInfoService.getThingInfoById(thingId, request);
            msg.put("thingInfo", "ti");
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询物品详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
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
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deviceId", value = "设备id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getDeviceInfoDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceInfoDetailById(String deviceId) {
        try {
            JSONObject msg = new JSONObject();
            DeviceInfo di = deviceService.findDeviceById(deviceId);
            OrganizationLdap ol = userService.getOrgByUuid(di != null ? di.getGroupId() : "");
            if (di != null) {
                di.setGroupName(ol != null ? ol.getName() : "");
            }
            msg.put("deviceInfo", di);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("根据终端id查询终端详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
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
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simcardId", value = "sim卡的id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getSimCardInfoDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimCardInfoDetailById(String simcardId) {
        try {
            JSONObject msg = new JSONObject();
            SimcardInfo si = simcardService.findSimcardById(simcardId);
            OrganizationLdap ol = userService.getOrgByUuid(si != null ? si.getGroupId() : "");
            if (si != null) {
                si.setGroupName(ol != null ? ol.getName() : "");
            }
            msg.put("simcardInfo", si);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询SIM卡详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
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
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "professionalId", value = "从业人员id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getProfessionalDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getProfessionalDetailById(String professionalId) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("professionalInfo", professionalsService.findProfessionalsById(professionalId));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询从业人员详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据组织id查询组织详情
     * @param groupId
     * @return JsonResultBean
     * @Title: getGroupDetail
     * @author Liubangquan
     */
    @ApiOperation(value = "根据组织id查询组织详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "groupId", value = "组织id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getGroupDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getGroupDetail(String groupId) {
        try {
            JSONObject msg = new JSONObject();
            Assignment assignment = assignmentService.findAssignmentById(groupId);
            msg.put("groupInfo", assignment);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询组织详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取分组树
     * @param isOrg
     * @return String
     * @Title: getTree
     * @author Liubangquan
     */
    @ApiOperation(value = "获取分组树", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    public String getTree(String isOrg) {
        try {
            JSONArray result = assignmentService.getAssignmentTree();
            return result.toJSONString();
        } catch (Exception e) {
            log.error("获取分组树异常", e);
            return null;
        }
    }

    /**
     * 校验当前分组下的最大车辆数是否已经达到上限
     * @param
     * @return JsonResultBean
     * @Title: checkMaxVehicleCountOfAssignment
     * @author Liubangquan
     */
    @ApiOperation(value = "校验当前分组下的最大车辆数是否已经达到上限", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })

    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "assignmentId", value = "分组id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "assignmentName", value = "分组名称", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/checkMaxVehicleCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkMaxVehicleCountOfAssignment(String assignmentId, String assignmentName) {
        try {
            return configService.isVehicleCountExceedMaxNumber(assignmentId, assignmentName);
        } catch (Exception e) {
            log.error("校验当前分组下的最大车辆数是否已经达到上限异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据分组/企业id查询监控对象少于100的分组id
     * @param id   节点id
     * @param type 节点类型 1：分组 2：企业
     * @return
     * @author hujun
     * @Date 创建时间：2018年4月12日 下午5:29:01
     */
    @ApiOperation(value = "根据分组/企业id查询监控对象少于100的分组id", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "节点id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "1：分组 2：企业", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getAssignmentCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAllAssignmentVehicleNumber(String id, int type) {
        try {
            JSONObject result = configService.getAllAssignmentVehicleNumber(id, type);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获得所有分组id及其分组下监控对象的数量异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    // 点击详情时获取当前车辆绑定个外设列表
    @ApiOperation(value = "点击详情时获取当前车辆绑定个外设列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getPeripherals", method = RequestMethod.POST)
    @ResponseBody

    public JsonResultBean getPeripherals(String vehicleId) {
        try {
            JSONObject msg = new JSONObject();
            String name = configService.getPeripherals(vehicleId);
            msg.put("pname", name);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获得所有分组id及其分组下监控对象的数量异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "获取车辆的设备id下拉框", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "configId", value = "信息配置id", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/getVDeviceSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVDeviceSelect(String configId) {
        try {
            return new JsonResultBean("");
        } catch (Exception e) {
            log.error("获取没有绑定的车辆设备异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "获取人的设备id列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "configId", value = "信息配置id", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/getPDeviceSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPDeviceSelect(String configId) {
        try {
            String deviceId = null;
            if (StringUtils.isNotBlank(configId)) {
                ConfigDO configDO = configService.get(configId);
                deviceId = configDO != null ? configDO.getDeviceId() : null;
            }
            List<DeviceInfo> deviceList = configService.getDeviceInfoListForPeople(deviceId);
            JSONArray result = new JSONArray();
            for (DeviceInfo device : deviceList) {
                JSONObject proObj = new JSONObject();
                proObj.put("id", device.getId());
                proObj.put("name", device.getDeviceNumber());
                result.add(proObj);
            }
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取没有绑定的人员（北斗）设备异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "获取simcard的下拉框", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "configId", value = "信息配置id", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/getSimcardSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimcardSelect(String configId) {
        try {
            return new JsonResultBean("null");
        } catch (Exception e) {
            log.error("获取没有绑定的simcard异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "获取从业人员（司机）的下拉框", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getProfessionalSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getProfessionalSelect() {
        try {
            // 初始化从业人员信息
            List<ProfessionalsInfo> proList = configService.getProfessionalsInfoList();
            JSONArray result = new JSONArray();
            for (ProfessionalsInfo pro : proList) {
                JSONObject proObj = new JSONObject();
                proObj.put("id", pro.getId());
                proObj.put("name", pro.getName());
                result.add(proObj);
            }
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取从业人员异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "获取分组下拉框", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getAssignSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAssignSelect() {
        try {
            JSONObject msg = new JSONObject();
            // 获取当前登录用户组织，若当前登录用户为admin，则默认其第一个下级组织
            String[] orgInfo = configService.getCurOrgId();
            msg.put("orgId", orgInfo != null ? orgInfo[0] : "");
            msg.put("orgName", orgInfo != null ? orgInfo[1] : "");
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取分组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
