package com.zw.api2.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerProfessionalsForm;
import com.zw.api2.swaggerEntity.SwaggerProfessionalsQuery;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.domain.ProfessionalsTypeDO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.basicinfo.form.ProfessionalsGroupForm;
import com.zw.platform.domain.basicinfo.form.ProfessionalsTypeForm;
import com.zw.platform.domain.basicinfo.query.ProfessionalsTypeQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.basicinfo.ProfessionalsService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.FileUtil;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.FtpClientUtil;
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
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ????????????
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016???7???26?????????4:44:08
 */
@Controller
@RequestMapping("/api/m/basicinfo/enterprise/professionals")
@Api(tags = { "??????????????????_dev" }, description = "??????????????????api??????")
public class ApiProfessionalsController {
    private static Logger log = LogManager.getLogger(ApiProfessionalsController.class);

    @Resource
    private ProfessionalsService professionalsService;

    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String syError;

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host.clbs}")
    private String ftpHostClbs;

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${ftp.port.clbs}")
    private int ftpPortClbs;

    @Value("${adas.professionalFtpPath}")
    private String professionalFtpPath;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    private static final String LIST_PAGE = "modules/basicinfo/enterprise/professionals/list";

    private static final String ADD_PAGE = "modules/basicinfo/enterprise/professionals/add";

    private static final String EDIT_PAGE = "modules/basicinfo/enterprise/professionals/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/enterprise/professionals/import";

    private static final String IMPORTWO_PAGE = "modules/basicinfo/enterprise/professionals/importTwo";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * ????????????
     */
    @ApiIgnore
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * ???????????????tokey??????
     * @deprecated  ??????
     */
    @ApiIgnore
    @RequestMapping("/generateFormToken")
    @AvoidRepeatSubmitToken(setToken = true)
    @ResponseBody
    @ApiOperation(value = "???????????????tokey??????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    public JsonResultBean generateFormToken(HttpServletRequest request) {
        String formKey = (String) request.getSession().getAttribute("avoidRepeatSubmitToken");
        return new JsonResultBean(true, formKey);
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "???????????????????????????????????????????????????????????????????????????",
            required = true, paramType = "query", dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "??????????????????", required = true, paramType = "query",
            dataType = "long", defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "???????????????,????????????20",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupName", value = "??????id,????????????????????????????????????",
            paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(
        @ModelAttribute("professionalsQuery") SwaggerProfessionalsQuery professionalsQuery) {
        Jedis jedis = null;
        try {
            /*            ProfessionalsQuery query = new ProfessionalsQuery();
            org.springframework.beans.BeanUtils.copyProperties(professionalsQuery, query);
            long start = System.currentTimeMillis();
            Page<Map<String, Object>> result;
            jedis = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
            Pipeline pl = jedis.pipelined();
            JSONArray cvs = JSON.parseArray(jedis.get(RedisKeys.SORT_PROFESSIONAL_LIST));
            // ???????????????????????????????????????????????????????????????
            Set<String> groupDevice = professionalsService.getRedisGroupProfessionalId(query.getGroupName());
            // ??????????????????????????????
            List<String> sortGroupProfessional = new ArrayList<>();
            if (cvs != null && cvs.size() > 0) {
                for (Object obj : cvs) {
                    String did = (String) obj;
                    if (groupDevice.contains(did)) {
                        sortGroupProfessional.add(did);
                    }
                }
            }
            int listSize = sortGroupProfessional.size();
            // ?????????
            int curPage = query.getPage().intValue();
            // ????????????
            int pageSize = query.getLimit().intValue();
            // ??????????????????
            int lst = (curPage - 1) * pageSize;
            // ????????????
            int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);
            List<OrganizationLdap> allGroup = userService.getAllOrganization(); // ????????????
            List<Map<String, Object>> cons = new ArrayList<>();
            // ??????????????????????????????????????????
            if (StringUtil.isNullOrBlank(query.getSimpleQueryParam())) {
                Map<Integer, Response<String>> maps = new HashMap<>();
                for (int i = lst; i < ps; i++) {
                    String vt = sortGroupProfessional.get(i);
                    String svt = RedisHelper.buildKey(vt, "professional", "list");
                    Response<String> strs = pl.get(svt);
                    maps.put(i, strs);
                }
                pl.sync();
                for (Response<String> response : maps.values()) {
                    String jarray = response.get();
                    if (StringUtils.isNotBlank(jarray)) {
                        JSONObject data = JSONObject.parseObject(jarray);
                        setIcCardEndDateFormat(data);
                        // ???Ldap????????????????????????
                        userService.assembleGroupName(allGroup, data);
                        // ??????????????????????????????????????????
                        constructProMap(data);

                        cons.add(data);
                    }
                }
            } else {
                // ??????????????????
                String queryPara = query.getSimpleQueryParam();
                List<Map.Entry<String, String>> vsdIds = RedisHelper
                    .hscan(RedisKeys.NAME_IDENTITY_STATE_FUZZY_SEARCH, "*" + queryPara + "*", 100000,
                        PublicVariable.REDIS_TEN_DATABASE);
                List<String> sortIds = new ArrayList<>();
                Set<String> proIds = new TreeSet<>();
                for (Map.Entry<String, String> entry : vsdIds) {
                    String value = entry.getValue();
                    proIds.add(value);
                }

                for (String proId : sortGroupProfessional) {
                    if (proIds.contains(proId)) {
                        sortIds.add(proId);
                    }
                }
                // ????????????????????????????????????Redis??????????????????
                List<Response<String>> resList = new ArrayList<>();
                listSize = sortIds.size();
                ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);
                for (int i = lst; i < ps; i++) {
                    resList.add(pl.get(RedisHelper.buildKey(sortIds.get(i), "professional", "list")));
                }
                pl.sync();
                for (Response<String> res : resList) {
                    String str = res.get();
                    if (!StringUtil.isNullOrBlank(str)) {
                        JSONObject map = JSON.parseObject(str);
                        setIcCardEndDateFormat(map);
                        // ???Ldap????????????????????????
                        userService.assembleGroupName(allGroup, map);
                        constructProMap(map);
                        cons.add(map);
                    }
                }
            }

            result = RedisQueryUtil.getListToPage(cons, query, listSize);
            long end = System.currentTimeMillis();
            log.info("search device cast : " + (end - start) + "ms");
            return new PageGridBean(query, result, true);*/
            return null;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new PageGridBean(null, false, e.getMessage());
        } finally {
            // RedisHelper.returnResource(jedis);
        }
    }

    private void setIcCardEndDateFormat(JSONObject map) {
        if (map.containsKey("icCardEndDate")) {
            long icCardEndDate = (long) map.get("icCardEndDate");
            String icCardEndDateStr = DateFormatUtils.format(icCardEndDate, "yyyy-MM-dd");
            map.put("icCardEndDate", icCardEndDateStr);
        }
    }

    private void constructProMap(Map<String, Object> data) throws Exception {
        // ??????????????????
        data.put("hiredate", DateUtil.getLongToDateStr(String.valueOf(data.get("hiredate")), null));
        data.put("birthday", DateUtil.getLongToDateStr(String.valueOf(data.get("birthday")), null));
        // ??????????????????
        String positionType = "";
        if (data.get("positionType") != null) {
            positionType = data.get("positionType").toString();
        }
        ProfessionalsTypeDO typeObj = professionalsService.get(positionType);
        if (typeObj != null) {
            String type = typeObj.getProfessionalstype() == null ? "" : typeObj.getProfessionalstype();
            data.put("type", type);
        }
        if (sslEnabled) {
            mediaServer = "/mediaserver";
        }
        if (data.get("photograph") != null && !data.get("photograph").equals("")) {
            String fileName = data.get("photograph").toString();
            data.put("photograph", mediaServer + professionalFtpPath + fileName);
        }
    }

    /**
     * ????????????
     */
    @ApiIgnore
    @ApiOperation(value = "????????????????????????_ModelAndView", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "uuid", value = "?????????uuid", required = true,
            paramType = "query", dataType = "string", defaultValue = "1") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView initNewUser(@RequestParam("uuid") String uuid) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            String user = userService.getOrgUuidByUser();
            if (uuid.equals(user)) {
                uuid = "";
            }
            if (!uuid.equals("")) {
                OrganizationLdap organization = userService.getOrgByUuid(uuid);
                mav.addObject("orgId", uuid);
                mav.addObject("groupName", organization.getName());
            }
            return mav;
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "??????????????????(?????????????????????2-20)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "????????????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "phone", value = "????????????1", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addProfessionals(@Validated({
        ValidGroupAdd.class }) @ModelAttribute("professionalsForm") SwaggerProfessionalsForm professionalsForm,
        final BindingResult bindingResult, @RequestParam("groupId") final String groupId) {
        try {
            ProfessionalsForm form = new ProfessionalsForm();
            org.springframework.beans.BeanUtils.copyProperties(professionalsForm, form);
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // // ????????????
                if (form.getPhotograph() != null && !form.getPhotograph().equals("") && !form.getPhotograph()
                    .equals("0")) {
                    if (sslEnabled) {
                        mediaServer = "/mediaserver";
                    }
                    if (editImg(form.getPhotograph())) {
                        form.setPhotograph(form.getPhotograph().split(mediaServer + professionalFtpPath)[1]);
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????!,?????????");
                    }
                }

                // if (form.getDrivingType() != null && form.getDrivingType() == -1) {
                //     form.setDrivingType(null);
                // }
                ProfessionalsGroupForm proGroupForm = new ProfessionalsGroupForm();
                // ???????????????
                proGroupForm.setProfessionalsId(form.getId());
                proGroupForm.setGroupId(groupId);
                // ????????????ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // boolean flag = professionalsService.addProfessionalsWithGroup(form, proGroupForm, ipAddress);
                boolean flag = false;
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }

            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ??????id?????? ????????????
     */
    @ApiOperation(value = "??????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@ApiParam(value = "id") @PathVariable("id") final String id) {
        try {
            if (id != null) {
                int isBandOrg = professionalsService.getIsBandGroup(id); // ??????????????????????????????
                // ??????????????????????????????
                if (isBandOrg > 0) {
                    return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????????????????");
                }
                // ????????????ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // return professionalsService.deleteProfessionalsById(id, ipAddress);
                return null;
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "??????ids????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "???????????????????????????ids(???????????????)",
        required = true, paramType = "query", dataType = "Stirng")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                List<String> list = new ArrayList<>();
                if (ids.size() > 0) {
                    int before = ids.size();
                    for (String id : ids) {
                        int isBandOrg = professionalsService.getIsBandGroup(id);
                        if (isBandOrg == 0) {
                            list.add(id);
                        }
                    }
                    // ????????????ip
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    if (list.size() == before) {
                        // return professionalsService.deleteProfessionalsByBatch(list, ipAddress);
                        return null;
                    } else {
                        if (list.size() != 0) {
                            // professionalsService.deleteProfessionalsByBatch(list, ipAddress);
                            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????????????????????????????????????????????????????.");
                        } else {
                            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????");
                        }
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ??????????????????
     */
    @ApiIgnore
    @ApiOperation(value = "??????id??????????????????????????????", notes = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@ApiParam(value = "id") @PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            Map<String, Object> resultMap = professionalsService.findProGroupById(id);
            String groupId = (String) resultMap.get("groupName");
            OrganizationLdap organization = userService.getOrgByUuid(groupId);
            resultMap.put("groupName", organization.getName());
            // ??????????????????
            ProfessionalsForm form = new ProfessionalsForm();
            ConvertUtils.register(form, Date.class);
            BeanUtils.populate(form, resultMap);
            form.setGroupId(groupId);
            String fileName = form.getPhotograph();
            if (sslEnabled) {
                mediaServer = "/mediaserver";
            }
            if (fileName != null && !fileName.equals("")) {
                form.setPhotograph(mediaServer + professionalFtpPath + fileName);
            }
            mav.addObject("result", form);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "????????????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "name", value = "??????????????????"
            + "(????????????????????????????????????(IC???)???????????????????????????)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "????????????id",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "phone", value = "????????????1", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "positionType", value = "????????????id"
            + "(????????????????????????????????????(IC???)???????????????????????????)", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "cardNumber", value = "??????????????????"
            + "(????????????????????????????????????(IC???)???????????????????????????)", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "icCardAgencies", value = "???????????????????????????"
            + "(????????????????????????????????????(IC???)???????????????????????????)", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "icCardEndDate", value = "???????????????????????????"
            + "(????????????????????????????????????(IC???)???????????????????????????)", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final ProfessionalsForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // ????????????
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    if (form.getPhotograph() != null && !form.getPhotograph().equals("") && !form.getPhotograph()
                        .equals("0")) {
                        if (sslEnabled) {
                            mediaServer = "/mediaserver";
                        }
                        if (editImg(form.getPhotograph())) {
                            form.setPhotograph(form.getPhotograph().split(mediaServer + professionalFtpPath)[1]);
                        } else {
                            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????!,?????????");
                        }
                    }
                    // if (form.getDrivingType() != null && form.getDrivingType() == -1) {
                    //     form.setDrivingType(null);
                    // }
                    ProfessionalsGroupForm proGroupForm = new ProfessionalsGroupForm();
                    proGroupForm.setProfessionalsId(form.getId());
                    proGroupForm.setGroupId(form.getGroupId());
                    String beforeGroId =
                        String.valueOf(professionalsService.findProGroupById(form.getId()).get("groupName"));
                    // ????????????ip
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // boolean flag =
                    //     professionalsService.updateProGroupByProId(form, proGroupForm, beforeGroId, ipAddress);
                    boolean flag = false;
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }

    }

    /**
     * ??????
     * @deprecated  ????????????
     */
    @ApiIgnore
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            professionalsService.exportProfessionals(null, 1, response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ????????????
     */
    @ApiIgnore
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????????????????");
            professionalsService.generateTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ??????
     * @author wangying
     */
    @ApiIgnore
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    @ApiIgnore
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // ????????????ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // return professionalsService.importProfessionals(file, ipAddress);
            return null;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????????????? @Title: list @return List<Group>
     */
    @ApiOperation(value = "?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "isOrg", value = "?????????????????????????????????????????????(0???"
        + "?????????(????????????????????????); 1:??????(??????????????????))", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    public String getTree(String isOrg) {
        try {
            if ("admin".equals(SystemHelper.getCurrentUsername())) {
                // ?????????????????????????????????????????????
                String orgId = userService.getOrgIdByUser();
                List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
                JSONArray result = new JSONArray();
                for (OrganizationLdap group : orgs) {
                    if ((isOrg == null || "0".equals(isOrg)) && "ou=organization".equals(group.getCid())) {
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("id", group.getCid());
                    obj.put("pId", group.getPid());
                    obj.put("name", group.getName());
                    obj.put("uuid", group.getUuid());
                    obj.put("type", "group");
                    obj.put("adCode", group.getCountyCode());
                    obj.put("isarea", group.getIsArea());
                    result.add(obj);
                }
                return result.toJSONString();
            } else {
                // ???redis??????????????????key??????????????????????????????????????????redis???
                // ?????????????????????????????????????????????
                String orgId = userService.getOrgIdByUser();
                List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
                JSONArray result = new JSONArray();
                for (OrganizationLdap group : orgs) {
                    if ((isOrg == null || "0".equals(isOrg)) && "ou=organization".equals(group.getCid())) {
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("id", group.getCid());
                    obj.put("pId", group.getPid());
                    obj.put("name", group.getName());
                    obj.put("uuid", group.getUuid());
                    obj.put("isarea", group.getIsArea());
                    obj.put("adCode", group.getCountyCode());
                    obj.put("type", "group");
                    result.add(obj);
                }
                return result.toJSONString();
            }
        } catch (Exception e) {
            log.error("?????????????????????", e);
            return e.getMessage();
        }
    }

    /**
     * ????????????????????????
     * @deprecated  ??????
     */
    @ApiIgnore
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/uploadImg" }, method = RequestMethod.POST)
    @ResponseBody
    public JSONObject uploadProfessionalImg(MultipartFile file, String id) {
        String newName = "";
        JSONObject resultMap = new JSONObject();
        try {
            // ??????
            if (!file.isEmpty() && id != null && !id.equals("")) {
                // ??????????????????
                String filePath = request.getSession().getServletContext().getRealPath("/") + "upload/";
                File saveFile = new File(filePath);
                if (!saveFile.exists()) {
                    saveFile.mkdirs();
                }
                //???????????????????????????
                InputStream inputStream = file.getInputStream();
                String type = FileUtil.getFileType(inputStream);
                // ?????????????????????
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                if ((suffix.equals(".png") || suffix.equals(".jpg") || suffix.equals(".gif") || suffix.equals(".jpeg"))
                    && (type != null && (type.equals("png") || type.equals("jpg") || type.equals("gif") || type
                    .equals("tif") || type.equals("bmp")))) {
                    newName = id + suffix;
                    // ????????????
                    file.transferTo(new File(filePath + newName));
                    if (sslEnabled) {
                        mediaServer = "/mediaserver";
                    }
                    resultMap.put("imgName", mediaServer + professionalFtpPath + newName);
                } else {
                    // ????????????
                    (new File(filePath + newName)).delete();
                    // ??????0 ????????????????????????
                    resultMap.put("imgName", "0");
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.error("??????????????????", e);
            return resultMap;
        }
    }

    //???????????????ftp
    private boolean editImg(String fileUrl) {
        try {
            String[] fu = fileUrl.split("/");
            String fileName = "";
            if (fu.length > 0) {
                fileName = fu[fu.length - 1];
            }
            String filePath = request.getSession().getServletContext().getRealPath("/") + "upload/";
            FileInputStream fis = new FileInputStream(filePath + fileName);
            boolean success = FtpClientUtil
                .uploadFile(ftpHostClbs, ftpPortClbs, ftpUserName, ftpPassword, professionalFtpPath, fileName, fis);
            File picFile = new File(filePath + fileName);
            if (picFile.exists()) {
                picFile.delete();
            }
            return success;
        } catch (Exception e) {
            if (!e.getClass().equals(FileNotFoundException.class)) {
                log.error("???????????????????????????ftp??????", e);
                return false;
            } else {
                return true;
            }
        }
    }

    //??????????????????????????????????????????
    @ApiOperation(value = "????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "name", value = "???????????????????????????", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("name") String name) {
        try {
            ProfessionalDO vt = professionalsService.findProfessionalsByName(name);
            return vt == null;
        } catch (Exception e) {
            log.error("??????????????????????????????????????????", e);
            return false;
        }
    }

    @ApiOperation(value = "??????????????????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "????????????id",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "identity", value = "????????????????????????",
            required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/repetitions", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitions(String id, String identity) {
        try {
            ProfessionalDO vt = professionalsService.findByProfessionalsInfo(identity);
            boolean flag = false;
            if (id != null) {
                ProfessionalDTO pif = professionalsService.findProfessionalsById(id);
                if (pif.getIdentity().equals(identity)) {
                    flag = true;
                } else if (vt == null) {
                    return true;
                }
                return flag;
            } else {
                return vt == null;
            }
        } catch (Exception e) {
            log.error("??????????????????id??????????????????????????????", e);
            return false;
        }
    }

    /**
     * ?????? ????????????
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "professionalstype", value = "????????????",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addDescription", value = "????????????",
            required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/addType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(String professionalstype, String addDescription) {
        try {
            if (professionalstype != null && !"".equals(professionalstype)) { // ???????????????????????????
                ProfessionalsTypeDO professionalsTypeDO = new ProfessionalsTypeDO();
                professionalsTypeDO.setProfessionalstype(professionalstype);
                professionalsTypeDO.setDescription(addDescription);
                String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
                professionalsService.add(professionalsTypeDO, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????????????????");
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ??????????????????
     */
    @ResponseBody
    @RequestMapping(value = "/listType", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    public PageGridBean list() {
        try {
            ProfessionalsTypeQuery query = new ProfessionalsTypeQuery();
            Page<ProfessionalsTypeDO> result = professionalsService.findByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * ??????id?????? ????????????
     */
    @ApiOperation(value = "??????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "????????????id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/deleteType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteType(final String id) {
        try {
            if (id != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
                boolean flag = professionalsService.deletePostType(id, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ??????id???????????? ????????????
     */
    @ApiOperation(value = "??????id???????????? ????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "ids", value = "??????????????????id????????????',????????????",
            required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/deleteTypeMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteTypeMore(final String ids) {
        try {
            if (ids != null) {
                String[] item = ids.split(",");
                List<String> list = Arrays.asList(item);
                // ????????????ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // boolean flag = professionalsService.deleteMore(list, ipAddress);
                boolean flag = false;
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ??????ID??????????????????
     */
    @ApiOperation(value = "??????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "????????????id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/findTypeById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findTypeById(String id) {
        try {
            JSONObject msg = new JSONObject();
            ProfessionalsTypeDO professionalsType = null;
            if (id != null && !id.isEmpty()) {
                professionalsType = professionalsService.get(id);
            }
            msg.put("operation", professionalsType);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "????????????id",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "jobType", value = "??????????????????",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "jobDescription", value = "??????????????????", paramType = "query", dataType = "string") })
    @RequestMapping(value = "/editJobType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(String id, String jobType, String jobDescription) {
        try {
            if (jobType != null && !"".equals(jobType) && id != null && !"".equals(id)) {
                // ?????????????????????IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                ProfessionalsTypeForm professionalsTypeForm = new ProfessionalsTypeForm();
                professionalsTypeForm.setId(id);
                professionalsTypeForm.setProfessionalstype(jobType);
                professionalsTypeForm.setDescription(jobDescription);
                return professionalsService.update(professionalsTypeForm, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * ??????excel???
     */
    @ApiIgnore
    @ApiOperation(value = "??????????????????excel???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/exportType", method = RequestMethod.GET)
    @ResponseBody
    public void exportType(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            professionalsService.exportType(null, 1, response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ??????
     * @author yangyi
     */
    @ApiIgnore
    @RequestMapping(value = { "/importTwo" }, method = RequestMethod.GET)
    public String importTwoPage() {
        return IMPORTWO_PAGE;
    }

    @ApiIgnore
    @RequestMapping(value = "/importType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importType(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = professionalsService.importType(file, ipAddress);
            String msg = "???????????????" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     */
    @ApiIgnore
    @RequestMapping(value = "/downloadType", method = RequestMethod.GET)
    public void downloadType(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            professionalsService.generateTemplateType(response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????
     */
    @ApiOperation(value = "???????????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "????????????", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/comparisonType", method = RequestMethod.POST)
    @ResponseBody
    public boolean findTypeMessage(String type) {
        try {
            ProfessionalsTypeDO professionalsType = professionalsService.findTypeByType(type);
            // ??????????????????,???????????????????????????????????????,????????????
            return professionalsType == null;
        } catch (Exception e) {
            log.error("?????????????????????????????????????????????????????????????????????", e);
        }

        return true;
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     */
    @ApiOperation(value = "??????????????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "????????????????????????", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startpostType", value = "????????????????????????",
            required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/findPostTypeCompare", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOperationCompare(String type, String startpostType) {
        try {
            // ?????????type????????????
            ProfessionalsTypeDO professionalsType = professionalsService.findTypeByType(type);
            if (professionalsType == null) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                if (type.equals(startpostType)) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
        } catch (Exception e) {
            log.error("???????????????????????????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }
}
