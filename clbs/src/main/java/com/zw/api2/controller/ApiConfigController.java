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
 * ???????????? <p> Title: ConfigController.java </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p>
 * team: ZhongWeiTeam </p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016???7???26?????????10:49:40
 */
@RestController
@RequestMapping("/api/m/infoconfig/infoinput")
@Api(tags = { "????????????dev" }, description = "??????????????????api??????")
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
     * ????????????Service
     */
    @Autowired
    private ConfigService configService;

    /**
     * ????????????Service
     */
    @Autowired
    private DeviceService deviceService;

    /**
     * Sim?????????Service
     */
    @Autowired
    private SimcardService simcardService;

    /**
     * ????????????Service
     */
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private TopSpeedService topSpeedService;

    /**
     * ??????Service
     */
    @Autowired
    private UserService userService;

    @Autowired
    private ProfessionalsService professionalsService;

    @Autowired
    private AssignmentService assignmentService;

    /**
     * ????????????????????????
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
     * ????????????????????????
     * @return PageGridBean
     * @Title: list
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
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
            log.error("??????????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @ApiIgnore
    @RequestMapping(value = { "/addlist" }, method = RequestMethod.GET)
    public String addList() {
        return ADD_PAGE;
    }

    /**
     * ????????????????????????
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
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
            log.error("????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????????????????
     * @return JsonResultBean
     * @Title: getDeviceInfoByDeviceNumber
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deviceNumber", value = "????????????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/getDeviceInfoByDeviceNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceInfoByDeviceNumber(String deviceNumber) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("deviceInfo", deviceService.findDeviceByDeviceNumber(deviceNumber));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????sim????????????simcard??????
     * @return JsonResultBean
     * @Title: getSimcardInfoBySimcardNumber
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simcardNumber", value = "simcard?????????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/getSimcardInfoBySimcardNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimcardInfoBySimcardNumber(String simcardNumber) {
        try {
            JSONObject msg = new JSONObject();
            SimcardInfo si = simcardService.findVehicleBySimcardNumber(simcardNumber);
            // ???sim?????????????????????
            OrganizationLdap ol2 = userService.getOrgByUuid(si.getGroupId());
            si.setGroupName(null != ol2 ? ol2.getName() : "");
            msg.put("simcardInfo", si);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????id
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @return JsonResultBean
     * @Title: add
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
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
            // ????????????????????????SIM????????????????????????
            if (!Converter.toBlank(config1Form.getBrandID()).equals("") || !Converter.toBlank(config1Form.getDeviceID())
                .equals("") || !Converter.toBlank(config1Form.getSimID()).equals("")) {
                isBound = checkConfigIsBound(Converter.toBlank(config1Form.getBrandID()),
                    Converter.toBlank(config1Form.getDeviceID()), Converter.toBlank(config1Form.getSimID()));
            }
            if (isBound) { // ????????????????????????
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            // configService.addConfig(config1Form, configForm, request);
            // ??????????????????
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????????????????sim?????????????????????????????????????????????????????????????????????????????????
     * @param vid ??????id
     * @param did ??????id
     * @param sid SIM???id
     * @return boolean
     * @Title: checkConfigIsBound
     * @author Liubangquan
     */
    private boolean checkConfigIsBound(String vid, String did, String sid) {
        try {
            ConfigForm cf = configService.getIsBand(vid, did, sid, "");
            return null != cf;
        } catch (Exception e) {
            log.error("????????????????????????sim??????????????????????????????", e);
            return false;
        }
    }

    /**
     * ??????sim????????????????????????
     */
    @ApiOperation(value = "??????sim????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "did", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sid", value = "sim???id", required = true, paramType = "query", dataType = "string") })
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
            log.error("??????sim?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ???????????????????????????sim?????????????????????
     * @param inputId
     * @param inputValue
     * @return JsonResultBean
     * @Title: checkIsBound
     * @author Liubangquan
     */
    @ApiOperation(value = "?????????????????????????????????sim?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "inputId", value = "????????????(????????????brands???devices???sims)", required = true,
            paramType = "query",  dataType = "string"),
        @ApiImplicitParam(name = "inputValue", value = "??????????????????(???????????????????????????????????????????????????????????????????????????)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "monitorType", value = "?????????????????????0?????? 1?????? 2?????????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/checkIsBound" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkIsBound(String inputId, String inputValue, Integer monitorType) {
        try {
            inputValue = inputValue.trim();
            boolean isBound = false;
            String str = "";//???????????????????????????/????????????/sim??????
            String checkId = "";//???????????????id
            if (Converter.toBlank(inputId).equals("brands") && monitorType != null) {
                switch (monitorType) {
                    case 0://???
                        VehicleInfo vi = vehicleService.findByVehicle(inputValue);
                        if (null != vi) {
                            str = vi.getBrand();
                            checkId = vi.getId();
                        }
                        break;
                    case 1://???
                        // Personnel pi = personnelService.findByNumber(inputValue);
                        Personnel pi = null;
                        if (null != pi) {
                            str = pi.getPeopleNumber();
                            checkId = pi.getId();
                        }
                        break;
                    case 2://???
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
                //??????????????????????????????
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
            log.error("???????????????????????????sim???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????id??????config
     * @param id
     * @return JsonResultBean
     * @Title: delete
     * @author Liubangquan
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") @ApiParam("????????????id") final String id,
        HttpServletRequest request) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                List<String> ids = Arrays.asList(id);
                // JSONObject msg = configService.delete(ids, ip);
                //??????Storm????????????
                // configService.sendMonitorDataMaintain(msg, msg.getString("vehicleId"));
                // ZMQFencePub.pubChangeFence("16,0," + msg.getString("vehicleId"));
                return new JsonResultBean("msg");
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????id??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @ApiOperation(value = "????????????id??????????????????id", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query",
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????Config
     * @param request
     * @return JsonResultBean
     * @Title: deleteMore
     * @author Liubangquan
     */
    @ApiOperation(value = "??????ids????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "??????????????????????????????????????????ids(???????????????)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                // JSONObject msg = configService.delete(ids, ip);
                // //??????Storm????????????
                // configService.sendMonitorDataMaintain(msg, msg.getString("vehicleId"));
                // ZMQFencePub.pubChangeFence("16,0," + msg.getString("vehicleId"));
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * ???????????????????????????excel
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
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            // configService.exportConfig(null, 1, response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ??????Config??????
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
     * ????????????Config
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
            log.error("??????????????????????????????", e);
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
     * ????????????
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
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            configService.generateTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getConfigDetails_{configId}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getConfigDetails(@PathVariable("configId") @ApiParam("????????????id") final String configId,
        HttpServletRequest request) {
        try {
            ModelAndView mav = new ModelAndView(DETAILS_PAGE);
            ConfigDetailsQuery configDetail = null;
            //????????????????????????
            String monitorType = configService.findMonitorTypeById(configId);
            if ("0".equals(monitorType)) {
                configDetail = configService.configDetails(configId);// ????????????????????????
            } else if ("1".equals(monitorType)) {
                configDetail = configService.peopleConfigDetails(configId);// ????????????????????????
                if ("1".equals(configDetail.getGender())) {
                    configDetail.setGender("???");
                } else {
                    configDetail.setGender("???");
                }
            } else {
                configDetail = configService.thingConfigDetails(configId);
                //?????????????????????????????????
                Map<String, String> thingCategoryDetail =
                    (Map) request.getSession().getAttribute("thingCategoryDetail");
                Map<String, String> thingTypeDetail = (Map) request.getSession().getAttribute("thingTypeDetail");
                configDetail.setThingCategoryName(thingCategoryDetail.get(configDetail.getThingCategory()));
                configDetail.setThingTypeName(thingTypeDetail.get(configDetail.getThingType()));
            }
            dealConfigData(configDetail); // ????????????????????????
            // ???????????????????????????
            OrganizationLdap ol = userService.getOrgByUuid(null != configDetail ? configDetail.getParentGroupid() : "");
            if (configDetail != null) {
                configDetail.setParentGroupname(null != ol ? ol.getName() : "");
                // ???????????????????????????
                OrganizationLdap ol1 = userService.getOrgByUuid(configDetail.getDeviceParentGroupid());
                configDetail.setDeviceParentGroupname(null != ol1 ? ol1.getName() : "");
                // ???sim?????????????????????
                OrganizationLdap ol2 = userService.getOrgByUuid(configDetail.getSimParentGroupid());
                configDetail.setSimParentGroupname(null != ol2 ? ol2.getName() : "");
                // ????????????id
                globalParentGroupid = Converter.toBlank(configDetail.getParentGroupid());
            }
            mav.addObject("result", configDetail);
            // ????????????????????????
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
            // ??????????????????-???????????????????????????????????????
            globalConfigId = configId;
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * ???????????????????????? ???????????????0-??????1-??????2-??????3-??? ???????????????0-??????1-??? ???????????????0-?????????1-?????? ???????????????1-?????????JT808
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
     * ?????????????????????????????????????????????????????????
     * @param plateColor
     * @return String
     * @Title: getPlateColorStr
     * @author Liubangquan
     */
    private String getPlateColorStr(int plateColor) {
        return PlateColor.getNameOrBlankByCode(plateColor);
    }

    /**
     * ?????????????????????????????????????????????????????????
     * @param isVideo
     * @return String
     * @Title: getIsVideoStr
     * @author Liubangquan
     */
    private String getIsVideoStr(int isVideo) {
        if (Converter.toBlank(isVideo).equals("0")) {
            return "???";
        } else if (Converter.toBlank(isVideo).equals("1")) {
            return "???";
        } else {
            return "";
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     * @param isStart
     * @return String
     * @Title: getIsStartStr
     * @author Liubangquan
     */
    private String getIsStartStr(int isStart) {
        if (Converter.toBlank(isStart).equals("0")) {
            return "??????";
        } else if (Converter.toBlank(isStart).equals("1")) {
            return "??????";
        } else {
            return "";
        }
    }

    /**
     * ???????????????????????????value???????????????????????????
     * @param functionalType
     * @return String
     * @Title: getDeviceFunctionalTypeStr
     * @author Liubangquan
     */
    private String getDeviceFunctionalTypeStr(int functionalType) {
        if (Converter.toBlank(functionalType).equals("1")) {
            return "???????????????";
        } else if (Converter.toBlank(functionalType).equals("2")) {
            return "???????????????";
        } else if (Converter.toBlank(functionalType).equals("3")) {
            return "????????????";
        } else if (Converter.toBlank(functionalType).equals("4")) {
            return "????????????";
        } else if (Converter.toBlank(functionalType).equals("5")) {
            return "??????????????????";
        } else if (Converter.toBlank(functionalType).equals("6")) {
            return "????????????";
        } else {
            return "";
        }
    }

    /**
     * ??????????????????????????????????????????????????????
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
     * ??????????????????
     * @return JSONArray
     * @Title: getGroups
     * @author Liubangquan
     */
    @ApiOperation(value = "??????configid?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })

    @RequestMapping(value = { "/getGroups" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getGroups() {
        List<Assignment> assignmentList;
        //????????????????????????
        String monitorType = configService.findMonitorTypeById(globalConfigId);
        if ("0".equals(monitorType)) {
            assignmentList = configService.getAssignmentByConfigId(globalConfigId);
        } else if ("1".equals(monitorType)) {
            assignmentList = configService.getPeopleAssignmentByConfigId(globalConfigId);
        } else {
            assignmentList = configService.getThingAssignmentByConfigId(globalConfigId);
        }
        // ????????????????????????????????????
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
     * ????????????-????????????????????????
     * @return JSONArray
     * @Title: getParentGroup
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????-????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/getParentGroup" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getParentGroup() {
        // ????????????????????????
        List<OrganizationLdap> orgLdapList = userService.getAllOrganization();
        JSONArray array = new JSONArray();
        // global_parent_groupid.split("#").length == 1 ??????????????????????????????????????????????????????????????????
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
     * ??????????????????????????????????????????
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
     * ??????configid???????????????????????????????????????
     * @return JSONArray
     * @Title: getProfessionals
     * @author Liubangquan
     */
    @ApiOperation(value = "??????????????????", authorizations = {
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
                    // ??????????????????
                    ProfessionalsForm form = new ProfessionalsForm();
                    ConvertUtils.register(form, Date.class);
                    BeanUtils.populate(form, resultMap);
                    form.setGroupId(groupId);
                    array.add(form);
                }
            }
        } catch (Exception e) {
            log.error("??????configid?????????????????????????????????????????????", e);
        }
        return array;
    }

    /**
     * ????????????
     */
    @ApiIgnore
    @RequestMapping(value = { "/getConfigDetailsAll" }, method = RequestMethod.GET)
    @ResponseBody
    public List<ConfigDetailsQuery> getConfigDetailsAll() {
        return configService.configDetailsall();
    }

    /**
     * ??????
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{configId}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable final @ApiParam("????????????id") String configId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // mav.addObject("result", configService.getConfigInfo(configId));
            mav.addObject("VehicleTypeList", vehicleService.getVehicleTypeList());
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????
     */
    /**
     * ??????
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({
        ValidGroupUpdate.class }) @ModelAttribute("form") final SwaggerConfigUpdateForm swaggerConfigUpdateForm,
        final BindingResult bindingResult, HttpServletRequest request) {
        ConfigForm form = new ConfigForm();

        // ????????????
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        } else {
            try {
                // BeanUtils.copyProperties(form, swaggerConfigUpdateForm);
                // return configService.updateOrAdd(form, request, true);
                return null;
            } catch (Exception e) {
                log.error("????????????????????????", e);
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            }
        }
    }

    /**
     * ????????????id??????????????????
     * @param vehicleId
     * @return JsonResultBean
     * @Title: getVehicleInfoById
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query",
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????id??????????????????
     * @param peopleId
     * @return JsonResultBean
     * @Title: getPeopleInfoById
     * @author hujun
     */
    @ApiOperation(value = "????????????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "peopleId", value = "??????id", required = true, paramType = "query",
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????id??????????????????
     * @param thingId
     * @return
     */
    @ApiOperation(value = "????????????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "thingId", value = "??????id", required = true, paramType = "query",
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????id??????????????????
     * @param deviceId
     * @return JsonResultBean
     * @Title: getDeviceInfoDetailById
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deviceId", value = "??????id", required = true, paramType = "query",
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
            log.error("????????????id????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????sim???id??????SIM?????????
     * @param simcardId
     * @return JsonResultBean
     * @Title: getSimCardInfoDetailById
     * @author Liubangquan
     */
    @ApiOperation(value = "??????sim???id??????SIM?????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simcardId", value = "sim??????id", required = true, paramType = "query",
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
            log.error("??????SIM???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????id????????????????????????
     * @param professionalId
     * @return JsonResultBean
     * @Title: getProfessionalDetailById
     * @author Liubangquan
     */
    @ApiOperation(value = "??????????????????id????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "professionalId", value = "????????????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getProfessionalDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getProfessionalDetailById(String professionalId) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("professionalInfo", professionalsService.findProfessionalsById(professionalId));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????id??????????????????
     * @param groupId
     * @return JsonResultBean
     * @Title: getGroupDetail
     * @author Liubangquan
     */
    @ApiOperation(value = "????????????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "groupId", value = "??????id", required = true, paramType = "query",
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ???????????????
     * @param isOrg
     * @return String
     * @Title: getTree
     * @author Liubangquan
     */
    @ApiOperation(value = "???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    public String getTree(String isOrg) {
        try {
            JSONArray result = assignmentService.getAssignmentTree();
            return result.toJSONString();
        } catch (Exception e) {
            log.error("?????????????????????", e);
            return null;
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????
     * @param
     * @return JsonResultBean
     * @Title: checkMaxVehicleCountOfAssignment
     * @author Liubangquan
     */
    @ApiOperation(value = "???????????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })

    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "assignmentId", value = "??????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "assignmentName", value = "????????????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/checkMaxVehicleCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkMaxVehicleCountOfAssignment(String assignmentId, String assignmentName) {
        try {
            return configService.isVehicleCountExceedMaxNumber(assignmentId, assignmentName);
        } catch (Exception e) {
            log.error("?????????????????????????????????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????/??????id????????????????????????100?????????id
     * @param id   ??????id
     * @param type ???????????? 1????????? 2?????????
     * @return
     * @author hujun
     * @Date ???????????????2018???4???12??? ??????5:29:01
     */
    @ApiOperation(value = "????????????/??????id????????????????????????100?????????id", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "1????????? 2?????????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getAssignmentCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAllAssignmentVehicleNumber(String id, int type) {
        try {
            JSONObject result = configService.getAllAssignmentVehicleNumber(id, type);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("??????????????????id??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    // ??????????????????????????????????????????????????????
    @ApiOperation(value = "??????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query",
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
            log.error("??????????????????id??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "?????????????????????id?????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "configId", value = "????????????id", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/getVDeviceSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVDeviceSelect(String configId) {
        try {
            return new JsonResultBean("");
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "??????????????????id??????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "configId", value = "????????????id", paramType = "query", dataType = "string") })
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
            log.error("???????????????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "??????simcard????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "configId", value = "????????????id", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/getSimcardSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimcardSelect(String configId) {
        try {
            return new JsonResultBean("null");
        } catch (Exception e) {
            log.error("?????????????????????simcard??????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "??????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getProfessionalSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getProfessionalSelect() {
        try {
            // ???????????????????????????
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getAssignSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAssignSelect() {
        try {
            JSONObject msg = new JSONObject();
            // ?????????????????????????????????????????????????????????admin????????????????????????????????????
            String[] orgInfo = configService.getCurOrgId();
            msg.put("orgId", orgInfo != null ? orgInfo[0] : "");
            msg.put("orgName", orgInfo != null ? orgInfo[1] : "");
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
