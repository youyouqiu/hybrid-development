package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.DictionaryType;
import com.zw.platform.basic.constant.InputTypeEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.MessageConfig;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.DictionaryDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.ConfigDetailDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.ConfigService;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleTypeService;
import com.zw.platform.basic.service.impl.MonitorFactory;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.domain.infoconfig.query.ConfigDetailsQuery;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.service.topspeed.TopSpeedService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.imports.ProgressDetails;
import com.zw.talkback.common.ControllerTemplate;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ???????????? <p> Title: ConfigController.java </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p>
 * team: ZhongWeiTeam </p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016???7???26?????????10:49:40
 */
@Controller
@RequestMapping("/m/infoconfig/infoinput")
public class NewConfigController {

    private static final String LIST_PAGE = "modules/infoconfig/list";

    private static final String ADD_PAGE = "modules/infoconfig/infoinput/add";

    private static final String IMPORT_PAGE = "modules/infoconfig/import";

    private static final String IMPORT_TRANSPORT_PAGE = "modules/infoconfig/import_transport";

    private static final String EDIT_PAGE = "modules/infoconfig/infoinput/edit";

    private static final String DETAILS_PAGE = "modules/infoconfig/infoinput/details";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String NEW_LIST_PAGE = "modules/infoconfig/newlist";

    private static Logger log = LogManager.getLogger(NewConfigController.class);

    private ConfigDetailDTO configDetailDTO;

    @Autowired
    private CacheService[] cacheServices;

    @Autowired
    private TopSpeedService topSpeedService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private MonitorFactory monitorFactory;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private SimCardService simCardService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private VehicleTypeService vehicleTypeService;

    @Autowired
    private MessageConfig messageConfig;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private ProfessionalService professionalService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMonitorService groupMonitorService;

    @RequestMapping(value = { "/init" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean test() {
        try {
            for (CacheService cacheService : cacheServices) {
                cacheService.initCache();
            }
            return new JsonResultBean(true);
        } catch (Exception e) {
            log.error("?????????????????????", e);
        }
        return new JsonResultBean(false);

    }

    /**
     * ????????????????????????
     * @return String
     * @author Liubangquan
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @Auth
    @RequestMapping(value = { "/newlist" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public String newListPage() {
        return NEW_LIST_PAGE;
    }

    /**
     * ????????????????????????
     * @return PageGridBean
     * @author Liubangquan
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final ConfigQuery query) {
        Page<BindDTO> bindDTOList = configService.getByPage(query);
        List<ConfigList> configLists = bindDTOList.stream().map(ConfigList::new).collect(Collectors.toList());
        Integer total = Integer.parseInt(String.valueOf(bindDTOList.getTotal()));
        return new PageGridBean(RedisQueryUtil.getListToPage(configLists, query, total), true);
    }

    @RequestMapping(value = { "/addlist" }, method = RequestMethod.GET)
    public String addList() {
        return ADD_PAGE;
    }

    /**
     * ????????????????????????-??????????????????
     * @return JsonResultBean
     */
    @RequestMapping(value = { "/topspeedlist" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean topSpeedList(Integer identifyNumber) {
        JSONObject msg = new JSONObject();
        msg.put("list", topSpeedService.findDeviceData());
        return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
    }

    /**
     * ????????????-??????????????????
     * @return String
     * @author Liubangquan
     */
    @RequestMapping(value = { "/addlist_{id}" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(String id) {
        JSONObject msg = new JSONObject();
        //????????????
        msg.put("vehicleInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.VEHICLE.getType()));
        //????????????
        msg.put("peopleInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.PEOPLE.getType()));
        //????????????
        msg.put("thingInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.THING.getType()));
        //????????????
        msg.put("deviceInfoList", deviceService.getUbBindSelectList(null, null));
        // ????????????????????????
        List<String> terminalManufacturers =
            TypeCacheManger.getInstance().getDictionaryList(DictionaryType.TERMINAL_MANUFACTURER).stream()
                .map(DictionaryDO::getValue).collect(Collectors.toList());
        msg.put("terminalManufacturerInfoList", terminalManufacturers);

        //sim?????????
        msg.put("simCardInfoList", simCardService.getUbBindSelectList(null));

        //??????????????????
        msg.put("professionalsInfoList", professionalService.getSelectList(null));

        OrganizationLdap organization = userService.getCurrentUserOrg();
        msg.put("orgId", Objects.isNull(organization) ? "" : organization.getUuid());
        msg.put("orgName", Objects.isNull(organization) ? "" : organization.getName());
        return new JsonResultBean(msg);
    }

    /**
     * ????????????????????????????????????
     * @return JsonResultBean
     * @author Liubangquan
     */
    @RequestMapping(value = { "/getDeviceInfoByDeviceNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceInfoByDeviceNumber(String deviceNumber) {
        DeviceDTO deviceDTO = deviceService.getByNumber(deviceNumber);
        JSONObject msg = new JSONObject();
        if (Objects.isNull(deviceDTO)) {
            msg.put("deviceInfo", null);
            return new JsonResultBean(msg);
        }
        DeviceInfo deviceInfo = new DeviceInfo();
        BeanUtils.copyProperties(deviceDTO, deviceInfo);
        deviceInfo.setGroupId(deviceDTO.getOrgId());
        deviceInfo.setGroupName(deviceDTO.getOrgName());
        msg.put("deviceInfo", deviceInfo);
        return new JsonResultBean(msg);
    }

    /**
     * ??????sim????????????simcard??????
     * @return JsonResultBean
     * @Title: getSimcardInfoBySimcardNumber
     * @author Liubangquan
     */
    @RequestMapping(value = { "/getSimcardInfoBySimcardNumber" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimcardInfoBySimcardNumber(String simcardNumber) {
        SimCardDTO simCardDTO = simCardService.getByNumber(simcardNumber);
        JSONObject msg = new JSONObject();
        if (Objects.isNull(simCardDTO)) {
            msg.put("simcardInfo", null);
            return new JsonResultBean(msg);
        }
        SimcardInfo simcardInfo = new SimcardInfo(simCardDTO);
        msg.put("simcardInfo", simcardInfo);
        return new JsonResultBean(msg);
    }

    /**
     * ??????????????????--????????????
     * @return JsonResultBean
     * @Title: add
     * @author Liubangquan
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(@ModelAttribute("addForm1") final Config1Form config1Form, final ConfigForm configForm) {
        log.info("????????????????????????-????????????{}", com.alibaba.fastjson.JSON.toJSONString(config1Form));
        ConfigDTO configDTO = config1Form.convertAddConfig();
        //??????????????????
        if (Objects.equals(configDTO.getMonitorType(), MonitorTypeEnum.THING.getType())) {
            configDTO.setAlias(config1Form.getThingName());
        }
        configDTO.setInputType(InputTypeEnum.PROCESS_INPUT);
        return ControllerTemplate.getBooleanResult(() -> configService.add(configDTO));
    }

    /**
     * ???????????????????????????sim?????????????????????
     * @param inputId    ????????????
     * @param inputValue ?????????
     * @return JsonResultBean
     * @author Liubangquan
     */
    @RequestMapping(value = { "/checkIsBound" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkIsBound(String inputId, String inputValue, Integer monitorType) {
        inputValue = inputValue.trim();
        boolean isBound = configService.checkIsBound(inputId, inputValue, monitorType);
        return new JsonResultBean(ImmutableMap.of("isBound", isBound, "boundName", inputValue));
    }

    /**
     * ??????configId??????config
     * @param id configId
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") String id) {
        return ControllerTemplate.getResult(() -> configService.unbind(Collections.singletonList(id)));
    }

    /**
     * ??????????????????????????????????????????????????????
     * @param id ????????????ID
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deleteConfig_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteConfig(@PathVariable("id") String id) {
        return ControllerTemplate.getBooleanResult(() -> configService.delete(id));
    }

    /**
     * ??????????????????ID??????????????????ID
     * @param vehicleId ????????????ID
     * @return ????????????ID
     */
    @RequestMapping(value = "/getConfigIdByVehicleId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getConfigIdByVehicleId(String vehicleId) {
        JSONObject result = new JSONObject();
        result.put("configId", configService.getConfigId(vehicleId));
        return new JsonResultBean(result);
    }

    /**
     * ????????????????????????
     * @param request request
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        String items = request.getParameter("deltems");
        if (StringUtils.isBlank(items)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        List<String> ids = Arrays.asList(items.split(","));
        return ControllerTemplate.getResult(() -> configService.unbind(ids));
    }

    /**
     * ???????????????????????????excel
     */
    @RequestMapping(value = "/export.gsp", method = RequestMethod.POST)
    @ResponseBody
    public void export(HttpServletResponse response, ConfigQuery query) {
        ControllerTemplate.export(() -> configService.export(response, query), "??????????????????", response, "??????????????????????????????");
    }

    /**
     * ??????Config??????
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * ??????Config
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importConfig(@RequestParam(value = "file", required = false) MultipartFile file) {
        return ControllerTemplate.getBooleanResult(() -> configService.importExcel(file));
    }

    /**
     * ??????Config??????
     */
    @RequestMapping(value = { "/importTransport" }, method = RequestMethod.GET)
    public String importTransportPage() {
        return IMPORT_TRANSPORT_PAGE;
    }

    /**
     * ????????????Config
     */
    @RequestMapping(value = "/importTransport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importTransport(@RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest request) {
        ProgressDetails progress = new ProgressDetails();
        request.getSession().setAttribute("CONFIG_IMPORT_PROGRESS", progress);
        try {
            return configService.importTransport(file, progress, request);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    @RequestMapping(value = "/importProgress", method = RequestMethod.GET)
    @ResponseBody
    public int importProgress(HttpServletRequest request) {
        ProgressDetails progress = (ProgressDetails) request.getSession().getAttribute("CONFIG_IMPORT_PROGRESS");
        return Optional.ofNullable(progress).orElse(new ProgressDetails()).getProgress();
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        ControllerTemplate.export(() -> configService.generateTemplate(response), "??????????????????", response, "??????????????????????????????");
    }

    /**
     * ????????????
     */
    @RequestMapping(value = { "/getConfigDetails_{configId}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getConfigDetails(@PathVariable String configId) {
        try {
            ModelAndView mav = new ModelAndView(DETAILS_PAGE);
            //??????????????????
            this.configDetailDTO = configService.getDetailById(configId);
            if (configDetailDTO == null) {
                return mav;
            }
            ConfigDetailsQuery configDetailsQuery = new ConfigDetailsQuery(configDetailDTO);

            mav.addObject("result", configDetailsQuery);
            return mav;
        } catch (BusinessException e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????configid?????????????????????????????????
     * @return JSONArray
     */
    @RequestMapping(value = { "/getGroups" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getGroups() {
        JSONArray array = new JSONArray();
        if (this.configDetailDTO == null || CollectionUtils.isEmpty(configDetailDTO.getGroupList())) {
            return array;
        }
        List<GroupDTO> groupList = configDetailDTO.getGroupList();

        groupList.forEach(groupDTO -> {
            Assignment assignment = new Assignment();
            BeanUtils.copyProperties(groupDTO, assignment);
            assignment.setGroupName(groupDTO.getOrgName());
            assignment.setGroupId(groupDTO.getOrgId());
            array.add(assignment);
        });
        return array;
    }

    /**
     * ????????????-???????????????????????? ??????????????????
     * @return JSONArray
     */
    @RequestMapping(value = { "/getParentGroup" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getParentGroup() {
        JSONArray array = new JSONArray();
        if (this.configDetailDTO == null || null == configDetailDTO.getMonitor()) {
            return array;
        }
        String monitorOrgId = configDetailDTO.getMonitor().getOrgId();
        List<OrganizationLdap> orgLdapList = organizationService.getAllOrganization();
        if (!Converter.toBlank(monitorOrgId).equals("") && monitorOrgId.split("#").length == 1) {
            for (int j = 0; j < orgLdapList.size(); j++) {
                if (Converter.toBlank(monitorOrgId).equals(Converter.toBlank(orgLdapList.get(j).getId()))) {
                    array.add(orgLdapList.get(j));
                }
            }
        }
        return array;
    }

    /**
     * ??????configid???????????????????????????????????????
     * @return JSONArray
     */
    @RequestMapping(value = { "/getprofessionals" }, method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getProfessionals() {
        JSONArray array = new JSONArray();
        if (this.configDetailDTO == null || null == configDetailDTO.getProfessionalList()) {
            return array;
        }
        List<ProfessionalDTO> professionals = configDetailDTO.getProfessionalList();
        for (ProfessionalDTO professional : professionals) {
            ProfessionalsForm form = new ProfessionalsForm();
            BeanUtils.copyProperties(professional, form);
            form.setGroupId(professional.getOrgId());
            form.setGroupName(professional.getOrgName());
            array.add(form);
        }
        return array;
    }

    /**
     * ?????????????????????
     * @param configId ????????????ID
     * @return ?????????????????????
     */
    @RequestMapping(value = "/edit_{configId}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable String configId) {
        ModelAndView mav = new ModelAndView(EDIT_PAGE);
        try {
            configDetailDTO = configService.getDetailById(configId);
            List<VehicleTypeDTO> vehicleTypeList = vehicleTypeService.getListByKeyword(null);
            List<VehicleType> vehicleTypes =
                vehicleTypeList.stream().map(VehicleType::new).collect(Collectors.toList());

            mav.addObject("result", new Config1Form(configDetailDTO));
            mav.addObject("VehicleTypeList", vehicleTypes);
            return mav;
        } catch (BusinessException e) {
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????????????????
     * @param form          ????????????
     * @param bindingResult ??????????????????
     * @return ??????????????????
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") ConfigForm form,
        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        ConfigDTO configDTO = form.convert();
        return ControllerTemplate.getBooleanResult(() -> configService.update(configDTO, form.getBrandID()));
    }

    /**
     * ????????????id??????????????????
     * @param vehicleId ??????ID
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getVehicleInfoById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleInfoById(String vehicleId) {
        VehicleDTO vehicleDTO =
            (VehicleDTO) monitorFactory.create(MonitorTypeEnum.VEHICLE.getType()).getById(vehicleId);
        VehicleInfo vehicleInfo = null;
        if (Objects.nonNull(vehicleDTO)) {
            vehicleInfo = new VehicleInfo(vehicleDTO);
        }
        JSONObject result = new JSONObject();
        result.put("vehicleInfo", vehicleInfo);
        return new JsonResultBean(result);
    }

    /**
     * ????????????id??????????????????
     * @param peopleId peopleId
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getPeopleInfoById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPeopleInfoById(String peopleId) {
        try {
            PeopleDTO peopleDTO = (PeopleDTO) monitorFactory.create(MonitorTypeEnum.PEOPLE.getType()).getById(peopleId);
            Personnel personnel = null;
            if (Objects.nonNull(peopleDTO)) {
                personnel = new Personnel(peopleDTO);
            }
            JSONObject result = new JSONObject();
            result.put("peopleInfo", personnel);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    /**
     * ????????????id??????????????????
     * @param thingId thingId
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getThingInfoById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getThingInfoById(String thingId) {
        ThingDTO thingDTO = (ThingDTO) monitorFactory.create(MonitorTypeEnum.THING.getType()).getById(thingId);
        ThingInfo thingInfo = null;
        if (Objects.nonNull(thingDTO)) {
            thingInfo = new ThingInfo(thingDTO);
        }
        JSONObject result = new JSONObject();
        result.put("thingInfo", thingInfo);
        return new JsonResultBean(result);
    }

    /**
     * ????????????id??????????????????
     * @param deviceId deviceId
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getDeviceInfoDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceInfoDetailById(String deviceId) {
        DeviceDTO deviceDTO = deviceService.findById(deviceId);
        DeviceInfo deviceInfo = null;
        if (Objects.nonNull(deviceDTO)) {
            deviceInfo = new DeviceInfo(deviceDTO);
        }
        JSONObject result = new JSONObject();
        result.put("deviceInfo", deviceInfo);
        return new JsonResultBean(result);
    }

    /**
     * ??????sim???id??????SIM?????????
     * @param simcardId simcardId
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getSimCardInfoDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimCardInfoDetailById(String simcardId) {
        SimCardDTO simCardDTO = simCardService.getById(simcardId);
        SimcardInfo simcardInfo = null;
        if (Objects.nonNull(simCardDTO)) {
            simcardInfo = new SimcardInfo(simCardDTO);
        }
        return new JsonResultBean(ImmutableMap.of("simcardInfo", simcardInfo));
    }

    /**
     * ??????????????????id????????????????????????
     * @param professionalId professionalId
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getProfessionalDetailById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getProfessionalDetailById(String professionalId) {
        ProfessionalDTO professionalDTO = professionalService.editPageData(professionalId);
        JSONObject result = new JSONObject();
        result.put("professionalInfo", professionalDTO);
        return new JsonResultBean(result);
    }

    /**
     * ??????????????????
     * @param groupId ??????ID
     * @return ??????
     */
    @RequestMapping(value = "/getGroupDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getGroupDetail(String groupId) {
        try {
            GroupDTO groupDTO = groupService.getById(groupId);
            Assignment assignment = null;
            if (Objects.nonNull(groupDTO)) {
                assignment = new Assignment();
                BeanUtils.copyProperties(groupDTO, assignment);
                assignment.setGroupId(groupDTO.getOrgId());
                assignment.setGroupName(groupDTO.getOrgName());
            }
            JSONObject result = new JSONObject();
            result.put("groupInfo", assignment);
            return new JsonResultBean(result);
        } catch (BusinessException e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    /**
     * ???????????????
     * @param isOrg isOrg
     * @return String
     */
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    public String getTree(String isOrg) {
        JSONArray result = userService.getCurrentGroupTree();
        return result.toJSONString();
    }

    /**
     * ???????????????????????????????????????????????????????????????
     * @param assignmentId   ??????ID
     * @param assignmentName ????????????
     * @return JsonResultBean
     */
    @RequestMapping(value = "/checkMaxVehicleCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkMaxVehicleCountOfAssignment(String assignmentId, String assignmentName) {
        boolean canAdd = groupMonitorService.checkGroupMonitorNum(assignmentId);
        JSONObject msg = new JSONObject();
        msg.put("success", canAdd);
        if (!canAdd) {
            int maxNum = configHelper.getMaxNumberAssignmentMonitor();
            msg.put("msg", "???" + assignmentName + "?????????????????????????????????????????????" + maxNum + "???????????????????????????");
        }
        return new JsonResultBean(msg);
    }

    /**
     * ????????????/??????id????????????????????????100?????????id
     * @param id   ??????id
     * @param type ???????????? 1????????? 2?????????
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getAssignmentCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAllAssignmentVehicleNumber(String id, int type) {
        return new JsonResultBean(configService.checkGroupMonitorCount(id, type));
    }

    @RequestMapping(value = "todo/getPeripherals", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPeripherals(String vehicleId) {
        return null;
    }

    @RequestMapping(value = "/getMonitorSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorSelect(String configId, int monitorType) {
        String moType = String.valueOf(monitorType);
        List<Map<String, Object>> unbindMonitors = monitorFactory.getUbBindSelectList(moType);

        //??????configId???????????????????????????
        List<Map<String, Object>> result = new ArrayList<>();
        BindDTO bindDTO = configService.getByConfigId(configId);
        if (Objects.nonNull(bindDTO)) {
            Map<String, Object> monitor = ImmutableMap.of("id", bindDTO.getId(), "name", bindDTO.getName());
            if (Objects.nonNull(bindDTO.getPlateColor())) {
                monitor.put("plateColor", bindDTO.getPlateColor());
            }
            result.add(monitor);
        }
        // ????????????
        for (Map<String, Object> monitor : unbindMonitors) {
            monitor.put("name", monitor.get("brand"));
            monitor.remove("brand");
            result.add(monitor);
        }
        return new JsonResultBean(result);
    }

    @RequestMapping(value = "/getVDeviceSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVDeviceSelect(String configId) {
        List<Map<String, String>> result = new ArrayList<>();
        BindDTO config = configService.getByConfigId(configId);
        if (Objects.nonNull(config)) {
            Map<String, String> device = ImmutableMap
                .of("id", config.getDeviceId(), "name", config.getDeviceNumber(), "deviceType", config.getDeviceType());
            result.add(device);
        }
        //????????????
        List<Map<String, String>> devices = deviceService.getUbBindSelectList(null, null);
        for (Map<String, String> device : devices) {
            device.put("name", device.get("deviceNumber"));
            device.remove("deviceNumber");
            result.add(device);
        }
        return new JsonResultBean(result);
    }

    @RequestMapping(value = "/getSimcardSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSimcardSelect(String configId) {
        List<Map<String, String>> result = new ArrayList<>();
        BindDTO config = configService.getByConfigId(configId);
        if (Objects.nonNull(config)) {
            Map<String, String> sim = ImmutableMap.of("id", config.getSimCardId(), "name", config.getSimCardNumber());
            result.add(sim);
        }

        //????????????
        List<Map<String, String>> simCards = simCardService.getUbBindSelectList(null);
        for (Map<String, String> simCard : simCards) {
            simCard.put("name", simCard.get("simcardNumber"));
            simCard.remove("simcardNumber");
            result.add(simCard);
        }
        return new JsonResultBean(result);
    }

    @RequestMapping(value = "/getProfessionalSelect", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getProfessionalSelect(String configId, String keyword) {
        return new JsonResultBean(configService.getProfessionalSelect(configId, keyword));
    }

    /**
     * ????????????id?????????????????????id(??????)
     */
    @RequestMapping(value = "/getConfigIdByMonitorIds", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getConfigIdByMonitorIds(String vehicleId) {
        List<String> monitorIds = Arrays.asList(vehicleId.split(","));
        List<String> configIds = configService.getConfigIds(monitorIds);
        return new JsonResultBean(ImmutableMap.of("configId", String.join(",", configIds)));
    }
}
