package com.zw.api2.controller.apiSimcard;

import com.alibaba.fastjson.JSON;
import com.zw.api2.swaggerEntity.SwaggerPageParamQuery;
import com.zw.platform.basic.dto.F3SimCardDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.SendSimCard;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.form.ApiAddSimcardForm;
import com.zw.platform.domain.basicinfo.form.ApiEditSimcardForm;
import com.zw.platform.domain.basicinfo.form.SimGroupForm;
import com.zw.platform.domain.basicinfo.form.SimcardForm;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.service.basicinfo.SimcardService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
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
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import redis.clients.jedis.Jedis;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p> Title: sim???controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 */
@Controller
@RequestMapping("api/m/basicinfo/equipment/simcard")
@Api(tags = { "SIM?????????_dev" }, description = "sim?????????api")
public class ApiSimcardController {
    private static final Logger log = LogManager.getLogger(ApiSimcardController.class);

    @Autowired
    private SimcardService simcardService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sim.number.bound}")
    private String simNumberBound;

    @Value("${sys.error.msg}")
    private String syError;

    @Autowired
    private LogSearchService logSearchService;

    private static final String LIST_PAGE = "modules/basicinfo/equipment/simcard/list";

    private static final String ADD_PAGE = "modules/basicinfo/equipment/simcard/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/simcard/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/equipment/simcard/import";

    private static final String PROOFREADING_PAGE = "vas/monitoring/proofreading";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * ????????????
     * @return ??????
     * @throws BusinessException exception
     */
    @Auth
    @ApiIgnore
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * proofreadingPage
     * @param id id
     * @return modelandview
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "sim???ID", required = true, paramType = "query", dataType = "string")
    @Auth
    @ResponseBody
    @RequestMapping(value = { "/proofreading_{id}" }, method = RequestMethod.GET)
    public JsonResultBean proofreadingPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(PROOFREADING_PAGE);
            F3SimCardDTO f3SimInfo = simcardService.getF3SimInfo(id);
            mav.addObject("result", f3SimInfo);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????
     * @param query query
     * @return ????????????
     * @throws BusinessException exception
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "??????sim?????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "simpleQueryParam", value = "??????sim????????????????????????????????????", required = false, paramType = "query",
        dataType = "string")
    public PageGridBean getListPage(@ModelAttribute("query") SwaggerPageParamQuery query) {
        Jedis jedis = null;
        try {
            /*final SimcardQuery query1 = new SimcardQuery();
            BeanUtils.copyProperties(query, query1);
            if (query1 != null) {
                Page<Map<String, Object>> result;
                jedis = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
                JSONArray cvs = JSON.parseArray(jedis.get(RedisKeys.SORT_SIMCARD_LIST));
                // ???????????????????????????????????????????????????device
                Set<String> groupSim = simcardService.getRedisGroupSimId(null);
                // ??????????????????????????????
                List<String> sortGroupSim = new ArrayList<>();
                if (cvs != null && cvs.size() > 0) {
                    for (Object obj : cvs) {
                        String sid = (String) obj;
                        if (groupSim.contains(sid)) {
                            sortGroupSim.add(sid);
                        }
                    }
                }
                int listSize = sortGroupSim.size();
                // ?????????
                int curPage = query1.getPage().intValue();
                // ????????????
                int pageSize = query1.getLimit().intValue();
                // ??????????????????
                int lst = (curPage - 1) * pageSize;
                // ????????????
                int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);
                // ????????????
                List<OrganizationLdap> allGroup = userService.getAllOrganization();
                List<Map<String, Object>> cons = new ArrayList<>();
                Pipeline pl = jedis.pipelined();
                // ???????????????
                if (StringUtil.isNullOrBlank(query.getSimpleQueryParam()) && StringUtil
                    .isNullOrBlank(query1.getGroupType()) && StringUtil.isNullOrBlank(query1.getGroupName())) {
                    Map<Integer, Response<String>> maps = new HashMap<>();
                    for (int i = lst; i < ps; i++) {
                        String vt = sortGroupSim.get(i);
                        String svt = RedisHelper.buildKey(vt, "simcard", "list");
                        Response<String> strs = pl.get(svt);
                        maps.put(i, strs);
                    }
                    pl.sync();
                    for (Map.Entry<Integer, Response<String>> entry : maps.entrySet()) {
                        String jarray = entry.getValue().get();
                        if (StringUtils.isNotBlank(jarray)) {
                            Map<String, Object> data = JSONObject.parseObject(jarray, Map.class);
                            // ???Ldap????????????????????????
                            userService.assembleGroupName(allGroup, data);
                            // ????????????????????????
                            setSendStatus(data);
                            cons.add(data);
                        }
                    }
                } else {
                    // ????????????
                    // ??????????????????
                    String queryPara = query.getSimpleQueryParam();
                    // ??????????????????SIM???????????????sim???ID
                    List<String> realResult = simcardService.findIdByRealIdQuery(queryPara);
                    List<Map.Entry<String, String>> vsdIds =
                        configService.getVsdIds(queryPara, query1.getGroupType(), query1.getGroupName());
                    Set<String> simcardIds = new TreeSet<String>();
                    // ??????????????????SIM???Id
                    for (Map.Entry<String, String> entry : vsdIds) {
                        String value = entry.getValue();
                        if (value.contains("simcard")) {
                            simcardIds.add(value.split("simcard")[1].split("&")[1]);
                        }
                    }
                    simcardIds.addAll(realResult);
                    // ??????
                    List<String> sortSimIds = new ArrayList<String>();
                    for (String simId : sortGroupSim) {
                        if (simcardIds.contains(simId)) {
                            sortSimIds.add(simId);
                        }
                    }
                    // ???????????????????????????
                    listSize = sortSimIds.size();
                    // ????????????
                    ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);
                    List<Response<String>> resList = new ArrayList<Response<String>>();
                    for (int i = lst; i < ps; i++) {
                        resList.add(pl.get(RedisHelper.buildKey(sortSimIds.get(i), "simcard", "list")));
                    }
                    pl.sync();
                    for (Response<String> res : resList) {
                        String str = res.get();
                        if (!StringUtil.isNullOrBlank(str)) {
                            Map<String, Object> map = JSON.parseObject(str, Map.class);
                            // ???Ldap????????????????????????
                            userService.assembleGroupName(allGroup, map);
                            // ????????????????????????
                            setSendStatus(map);
                            cons.add(map);
                        }
                    }
                }
                result = RedisQueryUtil.getListToPage(cons, query1, listSize);
                return new PageGridBean(query1, result, true);
            }*/
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("????????????SIM???????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        } finally {
            // RedisHelper.returnResource(jedis);
        }
    }

    private void setSendStatus(Map<String, Object> map) throws Exception {
        String brand = (String) map.get("brand");
        String simId = (String) map.get("id");
        String vehicleId = (String) map.get("vehicleId");
        if (!StringUtil.isNullOrBlank(brand)) {
            List<Map<String, String>> pstatus = vehicleService.findParmStatus(simId);
            if (StringUtils.isNotBlank(vehicleId)) {
                map.put("vehicleId", vehicleId);
            } else {
                List<Map<String, String>> peopleInfo = vehicleService.findPeopleByNumber(brand);
                if (CollectionUtils.isNotEmpty(peopleInfo)) {
                    map.put("vehicleId", peopleInfo.get(0).get("id"));
                }
            }
            if (CollectionUtils.isNotEmpty(pstatus)) {
                Map<String, String> info = pstatus.get(0);
                map.put("pstatus", info.get("status"));
                map.put("paramId", info.get("dirId"));
            }
        } else {
            map.put("vehicleId", "");
            map.put("pstatus", "");
            map.put("paramId", "");
        }
    }

    /**
     * ??????????????????
     * @return ????????????
     */
    @ApiIgnore
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * ??????sim?????????
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "??????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simcardNumber", value = "SIM?????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "????????????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "isStart", value = "????????????,1?????????0??????", required = true, paramType = "query",
            dataType = "string", defaultValue = "1") })
    //@AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final ApiAddSimcardForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // ????????????
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    SimcardForm form1 = new SimcardForm();
                    BeanUtils.copyProperties(form1, form);
                    SimGroupForm groupForm = new SimGroupForm();
                    groupForm.setSimcardId(form1.getId());
                    if (!"".equals(Converter.toBlank(form1.getGroupId()))) {
                        groupForm.setGroupId(form1.getGroupId());
                    } else {
                        groupForm.setGroupId(Converter.toBlank(userService.getOrgUuidByUser()));
                    }
                    if (StringUtil.isNullOrBlank(form1.getMonthlyStatement())) {
                        form1.setMonthlyStatement("01");
                    }
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // boolean flag = simcardService.addSimcardWithGroup(form1, groupForm, ipAddress);
                    boolean flag = false;
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ??????id??????sim???
     * @param request request
     * @param id      SIM???Id
     * @return result
     */
    @ApiOperation(value = "??????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id, HttpServletRequest request) {
        try {
            if (id != null) {
                ConfigForm c = configService.getIsBand("", "", id, ""); // ??????SIM???????????????
                if (c == null) { // SIM????????????
                    SimcardForm form = new SimcardForm();
                    form.setId(id);
                    form.setFlag(0);
                    // ????????????????????????????????????IP??????
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // boolean flag = simcardService.updateSimcardWithGroup(form, ipAddress);
                    boolean flag = false;
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT, simNumberBound);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ????????????
     * @param request request
     * @return result
     */
    @ApiOperation(value = "??????ids????????????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "???????????????sim???ids(???????????????)", required = true, paramType = "query",
        dataType = "Stirng")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(",");
                ConfigForm c;
                for (String str : item) {
                    c = configService.getIsBand("", "", str, "");
                    if (c != null) {
                        return new JsonResultBean(JsonResultBean.FAULT, simNumberBound);
                    }
                }
                // ????????????ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // simcardService.delMoreSimcard(item); // ????????????
                // boolean flag = simcardService.deleteSimcardWithGroupByBatch(item, ipAddress);
                boolean flag = false;
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ??????sim???
     * @param id sim???Id
     * @return ????????????
     */
    @ApiOperation(value = "??????id??????sim?????????", notes = "??????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            Map<String, Object> resultMap = simcardService.findSimcardGroupById(id);
            String groupId = (String) resultMap.get("groupName");
            OrganizationLdap organization = userService.getOrgByUuid(groupId);
            resultMap.put("groupName", organization.getName());
            SimcardForm form = new SimcardForm();
            ConvertUtils.register(form, Date.class);
            BeanUtils.populate(form, resultMap);
            form.setGroupId(groupId);
            mav.addObject("result", form);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????sim????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????sim???
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "??????sim?????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final ApiEditSimcardForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // ????????????
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    SimcardForm form1 = new SimcardForm();
                    BeanUtils.copyProperties(form1, form);
                    // ????????????????????????????????????IP??????
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // boolean flag = simcardService.updateSimcardWithGroup(form1, ipAddress);
                    boolean flag = false;
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }

    }

    /**
     * ??????
     * @param response response
     */
    @ApiIgnore
    @ApiOperation(value = "??????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "SIM?????????");
            // simcardService.exportSimcard(null, 1, response);
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
        }
    }

    /**
     * ????????????
     * @param response response
     */
    @ApiIgnore
    @ApiOperation(value = "??????sin???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "???????????????????????????");
            simcardService.generateTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????", e);
        }
    }

    /**
     * @return String
     * @author wangying
     * @Title: ??????
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
     */
    @ApiIgnore
    @ApiOperation(value = "??????sim???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSimcard(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // return simcardService.importSimcard(file, ipAddress, request);
            return null;
        } catch (Exception e) {
            log.error("??????sim???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * repetition
     * @param simcardNumber sim??????
     * @return result
     */
    @ApiOperation(value = "??????sim???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("simcardNumber") String simcardNumber) {
        try {
            if (simcardNumber != null && simcardNumber.length() == 13 && simcardNumber.startsWith("106")) {
                simcardNumber = "1" + simcardNumber.substring(3);
            }
            SimcardInfo vt = simcardService.findBySIMCard(simcardNumber);
            return vt == null;
        } catch (Exception e) {
            log.error("??????sim?????????????????????", e);
            return false;
        }
    }

    /**
     * ??????id??????????????????
     * @return result
     */
    @ApiOperation(value = "??????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simId", value = "SIM???id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "????????????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "parameterName", value = "??????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "type", value = "??????0????????????????????????,1??????sim?????????????????????,2???????????????SIM?????????,3??????????????????????????????", required = true,
            paramType = "query", dataType = "Integer"),
        @ApiImplicitParam(name = "upTime", value = "????????????????????????", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "realId", value = "??????SIM??????-??????????????????sim??????", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/sendSimP", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendSimP(@ModelAttribute("sendSimCard") SendSimCard sendSimCard) {
        try {
            // ????????????ip
            String ip = new GetIpAddr().getIpAddr(request);
            // return simcardService.sendSimCard(sendSimCard, ip);
            return null;
        } catch (Exception e) {
            log.error("SIM??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ??????????????????SIM???
     * @param form sim?????????
     * @return ????????????
     */
    @ApiOperation(value = "??????????????????SIM???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "form",
        value = "???:{\"sid\":\"SIM???id\",\"simcardNumber\":\"sim??????\",\"iccid\":\"ICCID\",\"imei\":\"IMEI\",\"imsi\":"
            + "\"IMSI\",\"dayRealValue\":\"????????????\",\"monthRealValue\":\"????????????\",\"correctionCoefficient\":"
            + "\"????????????\",\"forewarningCoefficient\":\"????????????\",\"hourThresholdValue\":\"??????????????????\","
            + "\"dayThresholdValue\":\"???????????????\",\"monthThresholdValue\":\"???????????????\",\"monthTrafficDeadline\":"
            + "\"????????????????????????\",\"monitorType\":\"??????????????????\"}",
        required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/updataSimCradInfo", method = RequestMethod.POST)
    @ResponseBody
    public Boolean updateSimCradInfo(String form) {
        try {
            SimcardForm simcardForm = JSON.parseObject(form, SimcardForm.class);
            simcardForm.setId(simcardForm.getSid());
            simcardForm.setMonthlyStatement("01");
            // simcardService.updateSimCradInfo(simcardForm);
            return true;
        } catch (Exception e) {
            log.error("??????????????????SIM???????????????", e);
            return false;
        }
    }

    /**
     * ????????????????????????
     * @param vehicleId ??????Id
     * @return ??????
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/simLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean simLog(String vehicleId) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            if (vehicle != null) {
                String brand = vehicle[0];
                String plateColor = vehicle[1];
                String logs = "???????????????" + brand + " ??????SIM?????????";
                logSearchService.addLog(ip, logs, "3", "MONITORING", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("??????SIM???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????sim???????????????
     * @param vid ????????????id
     * @return JsonResultBean
     */
    @ApiOperation(value = "??????????????????sim???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vid", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/simIssueLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean simIssueLog(String vid) {
        try {
            /* String ip = new GetIpAddr().getIpAddr(request);
            String info =
                RedisHelper.get(RedisHelper.buildKey(vid, "config", "list"), PublicVariable.REDIS_TEN_DATABASE);
            if (StringUtils.isNotBlank(info)) {
                ConfigList configInfo = JSON.parseObject(info, ConfigList.class);
                if (configInfo != null) {
                    String logs =
                        "???????????????" + configInfo.getCarLicense() + " ( @" + configInfo.getGroupName() + " ) SIM?????????????????????";
                    String plateColor = configInfo.getPlateColor() != null ? configInfo.getPlateColor().toString() : "";
                    logSearchService.addLog(ip, logs, "2", "", configInfo.getCarLicense(), plateColor);
                }
            }*/
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("??????????????????sim?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ????????????sim???????????????
     * @param vehicleId ??????Id
     * @return JsonResultBean
     */
    @ApiOperation(value = "????????????sim???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/csimLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean csimLog(String vehicleId) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            if (vehicle != null) {
                String brand = vehicle[0];
                String plateColor = vehicle[1];
                String logs = "???????????????" + brand + " sim???????????????";
                logSearchService.addLog(ip, logs, "3", "MONITORING", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("sim?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ????????????????????????-??????SIM?????????,????????????????????????SIM????????????,?????????????????????SIM??????
     * ?????????F3???????????????sim???????????????SIM???????????????
     */
    @ApiOperation(value = "?????????????????????SIM???????????????F3???????????????sim???????????????SIM???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simCardId", value = "sim???id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "realCard", value = "??????sim???", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/updateRealId" }, method = RequestMethod.POST)
    @ResponseBody
    public void saveRealSimCard(String simCardId, String realCard) {
        try {
            // simcardService.updateRealIdBySimId(simCardId, realCard);
        } catch (Exception e) {
            log.error("????????????SIM???????????????", e);
        }

    }
}
