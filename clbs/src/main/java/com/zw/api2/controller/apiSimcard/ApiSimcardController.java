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
 * <p> Title: sim卡controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 */
@Controller
@RequestMapping("api/m/basicinfo/equipment/simcard")
@Api(tags = { "SIM卡管理_dev" }, description = "sim卡相关api")
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
     * 分页页面
     * @return 地址
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
    @ApiOperation(value = "获取下发界面数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "sim卡ID", required = true, paramType = "query", dataType = "string")
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
            log.error("下发界面弹出异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 分页查询
     * @param query query
     * @return 分页结果
     * @throws BusinessException exception
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "获取sim卡列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "simpleQueryParam", value = "按照sim卡号、车牌号进行模糊搜索", required = false, paramType = "query",
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
                // 获取用户所在所属企业及下级企业下的device
                Set<String> groupSim = simcardService.getRedisGroupSimId(null);
                // 筛选权限数据，并排序
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
                // 当前页
                int curPage = query1.getPage().intValue();
                // 每页条数
                int pageSize = query1.getLimit().intValue();
                // 遍历开始条数
                int lst = (curPage - 1) * pageSize;
                // 遍历条数
                int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);
                // 所有组织
                List<OrganizationLdap> allGroup = userService.getAllOrganization();
                List<Map<String, Object>> cons = new ArrayList<>();
                Pipeline pl = jedis.pipelined();
                // 非模糊搜索
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
                            // 从Ldap中查询出组织名称
                            userService.assembleGroupName(allGroup, data);
                            // 查询当前下发状态
                            setSendStatus(data);
                            cons.add(data);
                        }
                    }
                } else {
                    // 模糊搜索
                    // 模糊查询参数
                    String queryPara = query.getSimpleQueryParam();
                    // 先查询出真实SIM卡号匹配的sim卡ID
                    List<String> realResult = simcardService.findIdByRealIdQuery(queryPara);
                    List<Map.Entry<String, String>> vsdIds =
                        configService.getVsdIds(queryPara, query1.getGroupType(), query1.getGroupName());
                    Set<String> simcardIds = new TreeSet<String>();
                    // 得到模糊搜索SIM卡Id
                    for (Map.Entry<String, String> entry : vsdIds) {
                        String value = entry.getValue();
                        if (value.contains("simcard")) {
                            simcardIds.add(value.split("simcard")[1].split("&")[1]);
                        }
                    }
                    simcardIds.addAll(realResult);
                    // 排序
                    List<String> sortSimIds = new ArrayList<String>();
                    for (String simId : sortGroupSim) {
                        if (simcardIds.contains(simId)) {
                            sortSimIds.add(simId);
                        }
                    }
                    // 得到当前页码的数据
                    listSize = sortSimIds.size();
                    // 遍历条数
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
                            // 从Ldap中查询出组织名称
                            userService.assembleGroupName(allGroup, map);
                            // 查询当前下发状态
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
            log.error("分页查询SIM卡信息异常", e);
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
     * 跳转新增页面
     * @return 页面路径
     */
    @ApiIgnore
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增sim卡信息
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "添加sim卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simcardNumber", value = "SIM卡卡号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属企业id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "isStart", value = "启停状态,1启用，0停用", required = true, paramType = "query",
            dataType = "string", defaultValue = "1") })
    //@AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final ApiAddSimcardForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
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
            log.error("新增sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 根据id删除sim卡
     * @param request request
     * @param id      SIM卡Id
     * @return result
     */
    @ApiOperation(value = "删除sim卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id, HttpServletRequest request) {
        try {
            if (id != null) {
                ConfigForm c = configService.getIsBand("", "", id, ""); // 判断SIM卡是否绑定
                if (c == null) { // SIM卡未绑定
                    SimcardForm form = new SimcardForm();
                    form.setId(id);
                    form.setFlag(0);
                    // 获取访问服务器的客户端的IP地址
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
            log.error("删除sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 批量删除
     * @param request request
     * @return result
     */
    @ApiOperation(value = "根据ids批量删除sim卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的sim卡ids(用逗号隔开)", required = true, paramType = "query",
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
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // simcardService.delMoreSimcard(item); // 维护缓存
                // boolean flag = simcardService.deleteSimcardWithGroupByBatch(item, ipAddress);
                boolean flag = false;
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 修改sim卡
     * @param id sim卡Id
     * @return 修改页面
     */
    @ApiOperation(value = "根据id获取sim卡信息", notes = "修改", authorizations = {
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
            log.error("修改sim信息界面弹出异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改sim卡
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "修改sim卡信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final ApiEditSimcardForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    SimcardForm form1 = new SimcardForm();
                    BeanUtils.copyProperties(form1, form);
                    // 获取访问服务器的客户端的IP地址
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
            log.error("修改sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }

    }

    /**
     * 导出
     * @param response response
     */
    @ApiIgnore
    @ApiOperation(value = "导出sim卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "SIM卡列表");
            // simcardService.exportSimcard(null, 1, response);
        } catch (Exception e) {
            log.error("导出sim卡信息异常", e);
        }
    }

    /**
     * 下载模板
     * @param response response
     */
    @ApiIgnore
    @ApiOperation(value = "下载sin卡导入模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "终端手机号列表模板");
            simcardService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载模板异常", e);
        }
    }

    /**
     * @return String
     * @author wangying
     * @Title: 导入
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
     */
    @ApiIgnore
    @ApiOperation(value = "导入sim卡", authorizations = {
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
            log.error("导入sim卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * repetition
     * @param simcardNumber sim卡号
     * @return result
     */
    @ApiOperation(value = "检查sim卡编号是否已经存在", authorizations = {
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
            log.error("校验sim卡信息存在异常", e);
            return false;
        }
    }

    /**
     * 根据id下发参数设置
     * @return result
     */
    @ApiOperation(value = "根据id下发参数设置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simId", value = "SIM卡id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "监控对象id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "parameterName", value = "绑定id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "type", value = "标识0下发（实时监控）,1获取sim卡（实时监控）,2参数下发（SIM管理）,3点击关闭（实时监控）", required = true,
            paramType = "query", dataType = "Integer"),
        @ApiImplicitParam(name = "upTime", value = "流量最后更新时间", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "realId", value = "真实SIM卡号-物联网卡平台sim卡号", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/sendSimP", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendSimP(@ModelAttribute("sendSimCard") SendSimCard sendSimCard) {
        try {
            // 获得访问ip
            String ip = new GetIpAddr().getIpAddr(request);
            // return simcardService.sendSimCard(sendSimCard, ip);
            return null;
        } catch (Exception e) {
            log.error("SIM下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 实时监控修改SIM卡
     * @param form sim卡信息
     * @return 是否成功
     */
    @ApiOperation(value = "实时监控修改SIM卡", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "form",
        value = "例:{\"sid\":\"SIM卡id\",\"simcardNumber\":\"sim卡号\",\"iccid\":\"ICCID\",\"imei\":\"IMEI\",\"imsi\":"
            + "\"IMSI\",\"dayRealValue\":\"当日流量\",\"monthRealValue\":\"当月流量\",\"correctionCoefficient\":"
            + "\"修正系数\",\"forewarningCoefficient\":\"预警系数\",\"hourThresholdValue\":\"小时流量阈值\","
            + "\"dayThresholdValue\":\"日流量阈值\",\"monthThresholdValue\":\"月流量阈值\",\"monthTrafficDeadline\":"
            + "\"流量最后更新时间\",\"monitorType\":\"监控对象类型\"}",
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
            log.error("实时监控修改SIM卡信息异常", e);
            return false;
        }
    }

    /**
     * 查看历史轨迹日志
     * @param vehicleId 车辆Id
     * @return 日志
     */
    @ApiOperation(value = "查看历史轨迹日志", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/simLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean simLog(String vehicleId) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            if (vehicle != null) {
                String brand = vehicle[0];
                String plateColor = vehicle[1];
                String logs = "监控对象：" + brand + " 获得SIM卡信息";
                logSearchService.addLog(ip, logs, "3", "MONITORING", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("获得SIM卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 实时监控查看sim卡下发日志
     * @param vid 监控对象id
     * @return JsonResultBean
     */
    @ApiOperation(value = "实时监控查看sim卡下发日志", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vid", value = "车辆id", required = true, paramType = "query", dataType = "string")
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
                        "监控对象：" + configInfo.getCarLicense() + " ( @" + configInfo.getGroupName() + " ) SIM卡信息修正下发";
                    String plateColor = configInfo.getPlateColor() != null ? configInfo.getPlateColor().toString() : "";
                    logSearchService.addLog(ip, logs, "2", "", configInfo.getCarLicense(), plateColor);
                }
            }*/
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("实时监控查看sim卡下发日志异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 实时监控sim卡修正日志
     * @param vehicleId 车辆Id
     * @return JsonResultBean
     */
    @ApiOperation(value = "实时监控sim卡修正日志", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/csimLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean csimLog(String vehicleId) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            if (vehicle != null) {
                String brand = vehicle[0];
                String plateColor = vehicle[1];
                String logs = "监控对象：" + brand + " sim卡信息修正";
                logSearchService.addLog(ip, logs, "3", "MONITORING", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("sim卡信息修正异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 实时监控右键指令-获取SIM卡信息,物联网卡平台返回SIM卡数据后,将物联网卡平台SIM卡号
     * 存储到F3平台对应的sim卡号的真实SIM卡号字段中
     */
    @ApiOperation(value = "将物联网卡平台SIM卡号存储到F3平台对应的sim卡号的真实SIM卡号字段中", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "simCardId", value = "sim卡id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "realCard", value = "真实sim卡", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/updateRealId" }, method = RequestMethod.POST)
    @ResponseBody
    public void saveRealSimCard(String simCardId, String realCard) {
        try {
            // simcardService.updateRealIdBySimId(simCardId, realCard);
        } catch (Exception e) {
            log.error("更新真实SIM卡信息异常", e);
        }

    }
}
