package com.zw.api2.controller.ApiGroup;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zw.api2.swaggerEntity.SwaggerOrganizationLdap;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.GroupRepo;
import com.zw.platform.domain.core.OperationForm;
import com.zw.platform.domain.core.Operations;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.OrganizationRepo;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.exception.OrganizationDeleteException;
import com.zw.platform.service.core.OperationService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("api/c/group")
@Api(tags = { "????????????_dev" }, description = "????????????api??????")
public class ApiGroupController {
    private static final String ADD_PAGE = "core/uum/group/add";

    private static final String INSERT_PAGE = "core/uum/group/insert";

    private static final String EDIT_PAGE = "core/uum/group/edit";

    private static final String DETAIL_PAGE = "core/uum/group/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static Logger log = LogManager.getLogger(ApiGroupController.class);

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private OperationService operationService;

    @Autowired
    private OrganizationRepo organizationRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${group.exist.vehicle}")
    private String groupExistVehicle;

    @Value("${group.exist.person}")
    private String groupExistPerson;

    @Value("${group.exist.employee}")
    private String groupExistEmployee;

    @Value("${group.exist.device}")
    private String groupExistDevice;

    @Value("${group.exist.sim}")
    private String groupExistSim;

    @Value("${group.exist.assignment}")
    private String groupExistAssignment;

    @Value("${group.exist.fence}")
    private String groupExistFence;

    @Value("${group.exist.user}")
    private String groupExistUser;

    @Value("${operational.type.exist}")
    private String operationalTypeExist;

    @Value("${experience.id}")
    private String experienceId;

    @ApiIgnore
    @RequestMapping(value = "/groups", method = GET)
    public String listGroups(ModelMap map) {
        map.put("groups", groupRepo.getAllGroupNames());
        return "listGroups";
    }

    @ApiIgnore
    @RequestMapping(value = "/newGroup", method = GET)
    public String initNewGroup() {
        return "newGroup";
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "id", defaultValue = "101", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "pid", value = "??????id", required = true, paramType = "query", dataType = "string") })
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean addPage(@RequestParam("id") final String id, @RequestParam("pid") final String pid) {
        try {
            OrganizationLdap gf = new OrganizationLdap();
            gf.setCid(id);
            gf.setPid(pid);
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            mav.addObject("result", gf);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);

        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "pid", value = "??????id", required = true, paramType = "query", dataType = "string")
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/insert.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean insertPage(@RequestParam("pid") final String pid) {
        try {
            OrganizationLdap gf = userService.findOrganization(pid);
            gf.setCid(gf.getUuid());
            gf.setPid(pid);
            ModelAndView mav = new ModelAndView(INSERT_PAGE);
            mav.addObject("result", gf);
            mav.addObject("isInsert", "true");
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);

        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    // @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/insertGroup", method = POST)
    @ResponseBody
    public JsonResultBean insertGroup(@ModelAttribute("organizationLdap") SwaggerOrganizationLdap organizationLdap) {
        OrganizationLdap organizationLdap1 = new OrganizationLdap();
        BeanUtils.copyProperties(organizationLdap, organizationLdap1);
        try {
            if (organizationLdap1 != null) {
                organizationLdap1.setPid(java.net.URLDecoder.decode(organizationLdap1.getPid(), "UTF-8"));
                // groupService.insert(organizationLdap1);
                return new JsonResultBean(organizationLdap1.getOu());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     * @param organizationLdap
     * @param bindingResult
     * @return
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    //    @ApiImplicitParams(value = {
    //        @ApiImplicitParam(name = "pid", value = "???????????????id", required = true,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "name", value = "????????????,????????????25", required = true,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "principal", value = "????????????????????????20", required = false,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "phone", value = "????????????????????????????????????????????????", required = false,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "address", value = "?????????????????????50", required = false,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "description", value = "??????", required = false,
    //            paramType = "query", dataType = "string")})
    //@AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/newgroup", method = POST)
    @ResponseBody
    public JsonResultBean createUser(@ModelAttribute("organizationLdap") SwaggerOrganizationLdap organizationLdap,
        final BindingResult bindingResult) {
        OrganizationLdap organizationLdap1 = new OrganizationLdap();
        BeanUtils.copyProperties(organizationLdap, organizationLdap1);
        try {
            if (organizationLdap1 != null) {
                organizationLdap1.setPid(java.net.URLDecoder.decode(organizationLdap1.getPid(), "UTF-8"));
                // groupService.add(organizationLdap1);
                return new JsonResultBean(organizationLdap1.getOu());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "ou", value = "ldap????????????", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/addGroupRedis", method = POST)
    @ResponseBody
    public JsonResultBean addGroupRedis(String ou) {
        try {
            OrganizationLdap organization = userService.findGroupByOu(ou);
            if (organization != null) {
                userService.addGroupNameRedis(organization);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);

        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????
     * @param id
     * @return
     */
    @ApiOperation(value = "??????id????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(final String id) {
        try {
            if (id == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            if (experienceId.equals(id)) { //??????????????????????????????????????????
                return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????");
            }
            try {
                /*  if (groupService.delete(id)) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }*/
                return new JsonResultBean(JsonResultBean.FAULT);
            } catch (OrganizationDeleteException e) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    @ApiIgnore
    @RequestMapping(value = "/groups", method = POST)
    public String newGroup(Group group) {
        groupRepo.create(group);

        return "redirect:groups/" + group.getName();
    }

    @ApiIgnore
    @RequestMapping(value = "/groups/{name}", method = GET)
    public String editGroup(@PathVariable String name, ModelMap map) {
        Group foundGroup = groupRepo.findByName(name);
        map.put("group", foundGroup);

        final Set<UserBean> groupMembers = userService.findAllMembers(foundGroup.getMembers());
        map.put("members", groupMembers);

        Iterable<UserBean> otherUsers = Iterables.filter(userService.findAll(), new Predicate<UserBean>() {
            @Override
            public boolean apply(UserBean user) {
                return !groupMembers.contains(user);
            }
        });
        map.put("nonMembers", Lists.newLinkedList(otherUsers));

        return "editGroup";
    }

    @ApiIgnore
    @RequestMapping(value = "/groups/{name}/members", method = POST)
    public String addUserToGroup(@PathVariable String name, @RequestParam String userId) {
        Group group = groupRepo.findByName(name);
        group.addMember(userService.toAbsoluteDn(LdapUtils.newLdapName(userId)));

        groupRepo.save(group);

        return "redirect:/groups/" + name;
    }

    @ApiIgnore
    @RequestMapping(value = "/groups/{name}/members", method = DELETE)
    public String removeUserFromGroup(@PathVariable String name, @RequestParam String userId) {
        Group group = groupRepo.findByName(name);
        group.removeMember(userService.toAbsoluteDn(LdapUtils.newLdapName(userId)));

        groupRepo.save(group);

        return "redirect:/groups/" + name;
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "pid", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@RequestParam("pid") final String id) {
        try {
            OrganizationLdap gf = findOrgDetailById(java.net.URLDecoder.decode(id, "UTF-8"));
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", gf);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        // @ApiImplicitParam(name = "id", value = "????????????", required = true, paramType = "query",dataType = "string"),
        @ApiImplicitParam(name = "pid", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "name", value = "????????????,????????????25", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "principal", value = "????????????????????????20", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "phone", value = "????????????????????????????????????????????????", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "address", value = "?????????????????????50", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "description", value = "??????", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edits.gsp", method = POST)
    @ResponseBody
    public JsonResultBean edit(@ModelAttribute("form") SwaggerOrganizationLdap form,
        final BindingResult bindingResult) {
        try {
            final OrganizationLdap form1 = new OrganizationLdap();
            BeanUtils.copyProperties(form, form1);
            if (form1 != null) {
                // ????????????
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // ?????????????????????IP
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // ????????????
                    userService.update(form1, ipAddress);
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "??????????????????????????????", notes = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "pid", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/detail.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detailPage(@RequestParam("pid") final String id) {
        try {
            OrganizationLdap gf = findOrgDetailById(java.net.URLDecoder.decode(id, "UTF-8"));
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            mav.addObject("result", gf);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return OrganizationLdap
     * @throws @Title: ??????id??????????????????
     * @author wangying
     */
    @ApiOperation(value = "??????id????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    private OrganizationLdap findOrgDetailById(String id) throws Exception {
        OrganizationLdap gf = new OrganizationLdap();
        gf.setPid(id);
        OrganizationLdap org = userService.getOrganizationById(id);
        String operation = org.getOperation();
        Operations operations = operationService.findOperationByOperation(operation);
        if (operations == null) {
            gf.setOperation("");
        } else {
            gf.setOperation(operation);
        }
        gf.setOu(org.getOu());
        gf.setOrganizationCode(org.getOrganizationCode());
        gf.setLicense(org.getLicense());
        gf.setRegisterDate(org.getRegisterDate());
        gf.setPrincipal(org.getPrincipal());
        gf.setPhone(org.getPhone());
        gf.setAddress(org.getAddress());
        gf.setDescription(org.getDescription());
        gf.setName(org.getName());
        gf.setId(org.getId());
        gf.setOperatingState(org.getOperatingState());
        gf.setScopeOfOperation(org.getScopeOfOperation());
        gf.setProvinceName(org.getProvinceName());
        gf.setIssuingOrgan(org.getIssuingOrgan());
        gf.setCityName(org.getCityName());
        gf.setCountyName(org.getCountyName());
        gf.setAreaNumber(org.getAreaNumber());
        return gf;
    }

    /**
     * ??????????????????
     * @param addproperationtype ??????????????????
     * @param adddescription     ??????
     * @return
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "addproperationtype", value = "????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "adddescription", value = "??????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/addOperational", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addOperational(String addproperationtype, String adddescription) {
        try {
            if (addproperationtype != null && !addproperationtype.isEmpty()) {
                // ?????????????????????IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return operationService.addOperation(addproperationtype, adddescription, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("?????????????????????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ??????????????????????????????
     * @return
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "type", value = "??????????????????", required = false, paramType = "query", dataType = "string")
    @RequestMapping(value = "/findOperations", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOperations(String type) {
        try {
            if (type == null) {
                type = "";
            }
            JSONObject msg = new JSONObject();
            List<Operations> operation = operationService.findOperation(type);
            msg.put("operation", operation);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @param id
     * @return
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "????????????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/deleteOperation", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteOPeration(String id) {
        try {
            if (id != null) {
                // ?????????????????????IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                List<String> ids = Arrays.asList(id);
                return operationService.deleteOperation(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ??????id????????????
     * @param id
     * @return
     */
    @ApiOperation(value = "??????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "????????????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/findOperationById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findoperationById(String id) {
        try {
            if (id != null) {
                JSONObject msg = new JSONObject();
                Operations operation = operationService.findOperationById(id);
                msg.put("operation", operation);
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @param id
     * @param operationType
     * @param explains
     * @return
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "????????????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "operationType", value = "????????????????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "explains", value = "??????????????????,?????????????????????????????????null", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/updateOperation", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateOperations(String id, String operationType, String explains) {
        try {
            if (id != null && operationType != null && explains != null) {
                OperationForm operationForm = new OperationForm();
                // ??????????????????IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                operationForm.setId(id);
                operationForm.setOperationType(operationType);
                operationForm.setExplains(explains);
                return operationService.updateOperation(operationForm, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????????????????(???????????????????????????)
     * @return
     */
    @ApiOperation(value = "????????????????????????????????????(???????????????????????????)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "type", value = "??????????????????", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/findOperationByoperation", method = RequestMethod.POST)
    @ResponseBody
    public boolean findOperationByOperation(String type) {
        try {
            Operations operation = operationService.findOperationByOperation(type);
            if (operation == null) { // ?????????????????????????????????????????????
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return false;
        }

    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     */
    @ApiOperation(value = "??????????????????????????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "??????????????????", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "recomposeType", value = "???????????????????????????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/findOperationCompare", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOperationCompare(String type, String recomposeType) {
        try {
            // ?????????type????????????
            Operations operation = operationService.findOperationByOperation(type);
            if (operation == null) { // ????????????????????????????????????????????????,????????????????????????????????????,?????????true
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else { // ??????,??????????????????????????????
                if (type.equals(recomposeType)) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
        } catch (Exception e) {
            log.error("??????????????????????????????????????????????????????validate.remote????????????");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     * @param ids
     * @return
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "ids", value = "????????????ids,???????????????", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/deleteOperationMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteOperations(String ids) {
        try {
            if (ids != null) {
                List<String> operationId = Arrays.asList(ids.split(","));
                // ????????????????????????????????????ip??????
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return operationService.deleteOperation(operationId, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ?????????????????????????????????
     * @param organizationCode
     * @return
     */
    @ApiOperation(value = "?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "organizationCode", value = "??????????????????", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/uniquenessOrganizationCode", method = RequestMethod.POST)
    @ResponseBody
    public boolean codeUniqueness(String organizationCode) {
        try {
            LinkedList<OrganizationLdap> list = (LinkedList<OrganizationLdap>) organizationRepo.findAll();
            Iterator<OrganizationLdap> itr = list.iterator();
            boolean flag = true;
            int s = 0;
            while (itr.hasNext()) {
                OrganizationLdap organization = itr.next();
                String organizationCodes = organization.getOrganizationCode();
                if (organizationCode.equals(organizationCodes)) {
                    flag = false;
                    s++;
                }
            }
            if (flag || s == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return false;
        }
    }

    /**
     * ???????????????????????????????????????
     * @param license
     * @return
     */
    @ApiOperation(value = "???????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "license", value = "????????????????????????", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/uniquenessLicense", method = RequestMethod.POST)
    @ResponseBody
    public boolean licenseUniqueness(String license) {
        try {
            LinkedList<OrganizationLdap> list = (LinkedList<OrganizationLdap>) organizationRepo.findAll();
            Iterator<OrganizationLdap> itr = list.iterator();
            boolean flag = true;
            int s = 0;// ?????????????????????????????????ldap????????????.
            while (itr.hasNext()) {
                OrganizationLdap organization = itr.next();
                String licenses = organization.getLicense();
                if (license.equals(licenses)) {
                    flag = false;
                    s++;
                }
            }
            if (flag || s == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("?????????????????????????????????????????????", e);
            return false;
        }
    }

}
