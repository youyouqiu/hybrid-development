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
@Api(tags = { "组织管理_dev" }, description = "组织相关api接口")
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
     * 新增组织
     */
    @ApiOperation(value = "获得新增组织基础数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "id", defaultValue = "101", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "pid", value = "组织id", required = true, paramType = "query", dataType = "string") })
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
            log.error("新增组织弹出窗口异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);

        }
    }

    /**
     * 插入组织
     */
    @ApiOperation(value = "获得插入组织基础数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "pid", value = "组织id", required = true, paramType = "query", dataType = "string")
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
            log.error("新增组织弹出窗口异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);

        }
    }

    /**
     * 插入组织
     */
    @ApiOperation(value = "插入组织", authorizations = {
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
            log.error("增加组织信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 增加组织
     * @param organizationLdap
     * @param bindingResult
     * @return
     */
    @ApiOperation(value = "新增组织", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    //    @ApiImplicitParams(value = {
    //        @ApiImplicitParam(name = "pid", value = "父节点组织id", required = true,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "name", value = "组织名称,长度小于25", required = true,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "principal", value = "负责人，长度小于20", required = false,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "phone", value = "电话号码，必须为电话或者手机号码", required = false,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "address", value = "地址，长度小于50", required = false,
    //            paramType = "query", dataType = "string"),
    //        @ApiImplicitParam(name = "description", value = "描述", required = false,
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
            log.error("增加组织信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "新增组织维护缓存", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "ou", value = "ldap组织单元", required = true, paramType = "query", dataType = "string")
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
            log.error("增加组织信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id删除组织", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "组织id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(final String id) {
        try {
            if (id == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            if (experienceId.equals(id)) { //若为即刻体验企业，则不能删除
                return new JsonResultBean(JsonResultBean.FAULT, "即刻体验企业不允许删除！");
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
            log.error("删除组织信息异常", e);
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
     * 修改组织页面
     */
    @ApiOperation(value = "获取修改组织基础数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "pid", value = "组织id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@RequestParam("pid") final String id) {
        try {
            OrganizationLdap gf = findOrgDetailById(java.net.URLDecoder.decode(id, "UTF-8"));
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", gf);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("修改组织页面弹出异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改组织
     */
    @ApiOperation(value = "修改组织", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        // @ApiImplicitParam(name = "id", value = "组织名称", required = true, paramType = "query",dataType = "string"),
        @ApiImplicitParam(name = "pid", value = "组织id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "name", value = "组织名称,长度小于25", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "principal", value = "负责人，长度小于20", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "phone", value = "电话号码，必须为电话或者手机号码", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "address", value = "地址，长度小于50", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "description", value = "描述", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edits.gsp", method = POST)
    @ResponseBody
    public JsonResultBean edit(@ModelAttribute("form") SwaggerOrganizationLdap form,
        final BindingResult bindingResult) {
        try {
            final OrganizationLdap form1 = new OrganizationLdap();
            BeanUtils.copyProperties(form, form1);
            if (form1 != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 获取操作用户的IP
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // 修改组织
                    userService.update(form1, ipAddress);
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改组织信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 组织详情页面
     */
    @ApiOperation(value = "获取组织详情基础数据", notes = "用于详情页面弹出", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "pid", value = "组织id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/detail.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detailPage(@RequestParam("pid") final String id) {
        try {
            OrganizationLdap gf = findOrgDetailById(java.net.URLDecoder.decode(id, "UTF-8"));
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            mav.addObject("result", gf);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("组织详情页面弹出异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return OrganizationLdap
     * @throws @Title: 根据id查询组织详情
     * @author wangying
     */
    @ApiOperation(value = "根据id查询组织详细信息", authorizations = {
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
     * 新增行业类别
     * @param addproperationtype 运营资质类别
     * @param adddescription     说明
     * @return
     */
    @ApiOperation(value = "新增行业类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "addproperationtype", value = "行业类别", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "adddescription", value = "备注", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/addOperational", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addOperational(String addproperationtype, String adddescription) {
        try {
            if (addproperationtype != null && !addproperationtype.isEmpty()) {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return operationService.addOperation(addproperationtype, adddescription, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("组织与用户管理页面新增运营资质类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 查询全部运营资质类别
     * @return
     */
    @ApiOperation(value = "查询全部运营资质类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "type", value = "行业类别名称", required = false, paramType = "query", dataType = "string")
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
            log.error("查询全部运营资质类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 删除行业类别
     * @param id
     * @return
     */
    @ApiOperation(value = "删除行业类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "行业类别id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/deleteOperation", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteOPeration(String id) {
        try {
            if (id != null) {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                List<String> ids = Arrays.asList(id);
                return operationService.deleteOperation(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除运营资质异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 根据id行业类别
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id查询行业类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "行业类别id", required = true, paramType = "query", dataType = "string")
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
            log.error("查询运营资质类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改行业类别
     * @param id
     * @param operationType
     * @param explains
     * @return
     */
    @ApiOperation(value = "修改行业类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "行业类别id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "operationType", value = "修改后的行业类别", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "explains", value = "修改后的备注,可以为空字符串，不能为null", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/updateOperation", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateOperations(String id, String operationType, String explains) {
        try {
            if (id != null && operationType != null && explains != null) {
                OperationForm operationForm = new OperationForm();
                // 获取客户端的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                operationForm.setId(id);
                operationForm.setOperationType(operationType);
                operationForm.setExplains(explains);
                return operationService.updateOperation(operationForm, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改运营资质类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据行业类别查询行业类别(控制行业类别不重复)
     * @return
     */
    @ApiOperation(value = "根据行业类别查询行业类别(控制行业类别不重复)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "type", value = "行业类别名称", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/findOperationByoperation", method = RequestMethod.POST)
    @ResponseBody
    public boolean findOperationByOperation(String type) {
        try {
            Operations operation = operationService.findOperationByOperation(type);
            if (operation == null) { // 为空则说明数据库没有重复的数据
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("查询运营资质类别异常", e);
            return false;
        }

    }

    /**
     * 根据运营资质类别查询运营资质类别（用于修改时的比较）
     */
    @ApiOperation(value = "根据运营资质类别查询运营资质类别（用于修改时的比较）", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "行业类别名称", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "recomposeType", value = "修改前行业类别名称", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/findOperationCompare", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOperationCompare(String type, String recomposeType) {
        try {
            // 先检查type是否存在
            Operations operation = operationService.findOperationByOperation(type);
            if (operation == null) { // 根据运营资质类别查询运营资质类别,如果没有改运营资质的记录,则返回true
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else { // 否则,判断两个参数是否相同
                if (type.equals(recomposeType)) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
        } catch (Exception e) {
            log.error("组织与用户管理页面修改运营资质类别时validate.remote验证异常");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除运营资质类别
     * @param ids
     * @return
     */
    @ApiOperation(value = "批量删除运营资质类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "ids", value = "行业类别ids,以逗号隔开", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/deleteOperationMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteOperations(String ids) {
        try {
            if (ids != null) {
                List<String> operationId = Arrays.asList(ids.split(","));
                // 获取访问服务器的客户端的ip地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return operationService.deleteOperation(operationId, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除运营资质类别异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 组织结构代码唯一性校验
     * @param organizationCode
     * @return
     */
    @ApiOperation(value = "组织结构代码唯一性校验", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "organizationCode", value = "组织结构代码", required = true, paramType = "query",
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
            log.error("组织结构代码唯一性校验异常", e);
            return false;
        }
    }

    /**
     * 企业营业执照代码唯一性校验
     * @param license
     * @return
     */
    @ApiOperation(value = "企业营业执照代码唯一性校验", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "license", value = "企业营业执照代码", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/uniquenessLicense", method = RequestMethod.POST)
    @ResponseBody
    public boolean licenseUniqueness(String license) {
        try {
            LinkedList<OrganizationLdap> list = (LinkedList<OrganizationLdap>) organizationRepo.findAll();
            Iterator<OrganizationLdap> itr = list.iterator();
            boolean flag = true;
            int s = 0;// 控制企业营业执照代码在ldap中的个数.
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
            log.error("企业营业执照代码唯一性校验异常", e);
            return false;
        }
    }

}
