package com.zw.api.controller.core;

import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/swagger/c/group")
@Api(tags = { "组织管理" }, description = "组织相关api接口")
public class SwaggerGroupController {
    private static Logger log = LogManager.getLogger(SwaggerGroupController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 新增组织
     */
    // @ApiOperation(value = "根据组织id和该组织的父id组装组织实体", notes = "用于新增组织")
    // @ApiImplicitParam(name = "pid", value = "组织id", required = true, paramType = "query",dataType = "string")
    // @RequestMapping(value = "/add.gsp", method = RequestMethod.GET)
    // public JsonResultBean addPage(@RequestParam("pid") final String pid) {
    // OrganizationLdap gf = new OrganizationLdap();
    // gf.setPid(pid);
    // return new JsonResultBean(gf);
    // }

    /**
     * 增加组织
     * @param organizationLdap
     * @param bindingResult
     * @return
     */
    @ApiOperation(value = "新增组织", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "pid", value = "父节点组织id", required = true, paramType = "query", dataType = "string"),
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
    @RequestMapping(value = "/newgroup", method = POST)
    @ResponseBody
    public JsonResultBean createUser(@Validated({ ValidGroupAdd.class }) OrganizationLdap organizationLdap,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // 数据校验
                try {
                    if (userService.findOrganization(organizationLdap.getPid().toString()) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, "父节点组织不存在！");
                    }
                } catch (Exception e) {
                    return new JsonResultBean(JsonResultBean.FAULT, "父节点组织不存在！");
                }
                if (StringUtils.isNotBlank(organizationLdap.getPhone()) && !RegexUtils
                    .checkMobile(organizationLdap.getPhone()) && !RegexUtils
                    .checkPhone(organizationLdap.getPhone())) { // 校验电话必须为手机/电话
                    return new JsonResultBean(JsonResultBean.FAULT, "电话号码必须为手机/电话！");
                }
                //获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 新增
                userService.addCreateGroup(organizationLdap, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            log.error("新增组织异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 删除
     * @param id
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "根据id删除组织", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "组织id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(final String id) throws BusinessException {
        try {
            //获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            if (userService.findOrganization(id) == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
            }
            if (configService.isBnadP(id).size() == 0 && configService.isBnadG(id).size() == 0
                && configService.isBandDevice(id) == 0 && configService.isBandSimcard(id) == 0
                && configService.isBandAssignment(id) == 0) {
                userService.deleteOrganizationLdap(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "isBand");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
        }
        // userService.deleteOrganizationLdap(id);
        return new JsonResultBean(JsonResultBean.SUCCESS);

    }

    /**
     * 修改组织页面
     */
    // @ApiOperation(value = "根据id获取组织详细信息", notes = "用于编辑")
    // @ApiImplicitParam(name = "pid", value = "组织id", required = true, paramType = "query",dataType = "string")
    // @RequestMapping(value = "/edit.gsp", method = RequestMethod.GET)
    // public JsonResultBean editPage(@RequestParam("pid") final String id) {
    // OrganizationLdap gf = findOrgDetailById(id);
    // return new JsonResultBean(gf);
    // }

    /**
     * 修改角色
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
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) final OrganizationLdap form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // 数据校验
                try {
                    if (userService.findOrganization(form.getPid().toString()) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                    }
                } catch (Exception e) {
                    return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                }
                if (StringUtils.isNotBlank(form.getPhone()) && !RegexUtils.checkMobile(form.getPhone()) && !RegexUtils
                    .checkPhone(form.getPhone())) { // 校验电话必须为手机/电话
                    return new JsonResultBean(JsonResultBean.FAULT, "电话号码必须为手机/电话！");
                }
                // 修改
                //获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                userService.update(form, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            log.error("修改组织异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 组织详情页面
     */
    @ApiOperation(value = "根据id查询组织详细信息", notes = "用于详情页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "pid", value = "组织id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/detail.gsp", method = RequestMethod.GET)
    public JsonResultBean detailPage(@RequestParam("pid") final String id) {
        OrganizationLdap gf = null;
        try {
            gf = findOrgDetailById(id);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
        }
        return new JsonResultBean(gf);
    }

    /**
         * @return OrganizationLdap
     * @throws @Title: 根据id查询组织详情
     * @author wangying
     */
    private OrganizationLdap findOrgDetailById(String id) {
        OrganizationLdap gf = new OrganizationLdap();
        gf.setPid(id);
        OrganizationLdap org = userService.getOrganizationById(id);
        gf.setOu(org.getOu());
        gf.setAddress(org.getAddress());
        gf.setDescription(org.getDescription());
        gf.setPhone(org.getPhone());
        gf.setPrincipal(org.getPrincipal());
        gf.setName(org.getName());
        return gf;
    }

}
