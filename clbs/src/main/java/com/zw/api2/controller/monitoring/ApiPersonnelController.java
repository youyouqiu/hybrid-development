package com.zw.api2.controller.monitoring;

import com.alibaba.fastjson.JSONObject;
import com.zw.api2.swaggerEntity.SwaggerPersonnelForm;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.basicinfo.form.PersonnelForm;
import com.zw.platform.domain.basicinfo.query.PersonnelQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.imports.ZwImportException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/m/basicinfo/monitoring/personnel")
@Api(tags = { "人员管理_dev" }, description = "人员相关api")
public class ApiPersonnelController {

    private static Logger log = LogManager.getLogger(ApiPersonnelController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/personnel/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/personnel/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/personnel/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/personnel/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${vehicle.brand.bound}")
    private String vehicleBrandBound;

    @Value("${sys.error.msg}")
    private String syError;

    @Autowired
    private UserService userService;

    // @Autowired
    // private PersonnelService personnelService;

    @Autowired
    ConfigService configService;

    @Autowired
    private HttpServletRequest request;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    @ApiIgnore
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @ApiOperation(value = "获取人员信息列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(@ApiParam(value = "页数", required = true) Long page,
        @ApiParam(value = "每页显示条数", required = true) Long limit,
        @ApiParam(value = "按照姓名、电话号码进行模糊搜索") String simpleQueryParam) {
        try {
            PersonnelQuery query = new PersonnelQuery();
            query.setSimpleQueryParam(simpleQueryParam);
            // Page<Map<String, Object>> result = personnelService.findByPage(query);
            // return new PageGridBean(query, result, true);
            return null;
        } catch (Exception e) {
            log.error("分页查询失败人员信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 新增
     */

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @ApiOperation(value = "新增人员,返回带页面的接口,传入企业Id", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    public JsonResultBean initNewUser(
        @ApiParam(value = "企业id", required = true) @RequestParam("uuid") final String uuid) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            if (!uuid.equals("") && !uuid.equals("ou=organization")) {
                OrganizationLdap org = userService.getOrgByEntryDN(uuid);
                mav.addObject("orgId", org.getUuid());
                mav.addObject("groupName", org.getName());
            }
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("新增人员界面弹出异常", e);
            return new JsonResultBean(e.getMessage());
        }
    }

    /**
     * 新增人员
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "新增人员", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean add(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("source") final SwaggerPersonnelForm source,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            PersonnelForm form = new PersonnelForm();
            BeanUtils.copyProperties(source, form);
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // 获取当前用户的Ip地址
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                /* boolean flag = personnelService.addWithGroup(form, ip);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }*/
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 根据id删除 Personnl
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ApiOperation(value = "删除人员", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean delete(@ApiParam(value = "人员id", required = true) @PathVariable("id") final String id) {
        try {
            if (id != null) {
                JSONObject msg = new JSONObject();
                msg.put("peopleId", id);
                ConfigForm c = configService.getIsBand(id, "", "", "");
                if (c != null) {
                    msg.put("infoMsg", vehicleBrandBound);
                    return new JsonResultBean(msg);
                }
                // 获取到操作用户的IP
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                // 根据id删除人员信息
                /*boolean flag = personnelService.delete(id, ip);
                if (flag) {
                    return new JsonResultBean(msg);
                }*/
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 批量删除
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ApiOperation(value = "批量删除人员", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public JsonResultBean deleteMore(@ApiParam(value = "人员id,使用逗号隔开", required = true) String delIds) {
        try {
            if (delIds != null) {
                String userIp = new GetIpAddr().getIpAddr(request);
                // 获取选中的单选框
                String[] item = delIds.split(",");
                StringBuilder boundBrands = new StringBuilder(); // 已被绑定的人员编号，用于提示信息时使用
                StringBuilder boundBrandIds = new StringBuilder(); // 已被绑定的人员id， 用于删除时使用
                StringBuilder notBoundBrands = new StringBuilder(); // 没有被绑定的人员id
                for (int i = 0, n = item.length; i < n; i++) {
                    ConfigForm c = configService.getIsBand(item[i], "", "", "");
                    if (c != null) {
                        boundBrands.append(c.getBrands()).append(",");
                        boundBrandIds.append(item[i]).append(",");
                    } else {
                        notBoundBrands.append(item[i]).append(",");
                    }
                }
                // 没有被绑定的人员，直接删除
                if (notBoundBrands.length() > 0) {
                    notBoundBrands = new StringBuilder(Converter.removeStringLastChar(notBoundBrands.toString()));
                    // personnelService.deleteMuch(notBoundBrands.toString().split(","), userIp);
                }
                // 已经被绑定的人员，提示用户是否确认删除
                JSONObject msg = new JSONObject();
                if (boundBrands.length() > 0) {
                    boundBrands = new StringBuilder(Converter.removeStringLastChar(boundBrands.toString()));
                    boundBrandIds = new StringBuilder(Converter.removeStringLastChar(boundBrandIds.toString()));
                    msg.put("boundBrands", boundBrands);
                    msg.put("boundBrandIds", boundBrandIds);
                    msg.put("infoMsg", vehicleBrandBound);
                } else {
                    msg.put("boundBrands", "");
                    msg.put("boundBrandIds", "");
                    msg.put("infoMsg", "");
                }
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 根据id获取人员详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/getPeopleById", method = RequestMethod.POST)
    @ApiOperation(value = "获取人员详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "id", value = "人员id", paramType = "query", required = true) })
    @ResponseBody
    public JsonResultBean getPeopleById(String id) {
        try {
            // return new JsonResultBean(personnelService.get(id));
            return null;
        } catch (Exception e) {
            log.error("获取人员详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 修改Personnl
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ApiOperation(value = "修改人员,返回带页面的接口", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    public JsonResultBean editPage(@ApiParam(value = "人员id", required = true) String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // mav.addObject("result", personnelService.get(id));
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("修改人员信息弹出界面异常", e);
            return new JsonResultBean(e.getMessage());
        }
    }

    /**
     * 修改Personnl
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") SwaggerPersonnelForm source,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                PersonnelForm form = new PersonnelForm();
                BeanUtils.copyProperties(source, form);
                // boolean flag = personnelService.updateWithGroup(form, ip);
                // if (flag) {
                //     return new JsonResultBean(JsonResultBean.SUCCESS);
                // }
            }
        } catch (Exception e) {
            log.error("修改人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 导出excel表
     */
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    @ApiIgnore
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "人员列表");
            // personnelService.exportInfo(null, 2, response);
        } catch (Exception e) {
            log.error("导出人员信息异常", e);
        }
    }

    /**
     * @return String
     * @Title: 导入
     * @author
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    @ApiIgnore
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    @ApiIgnore
    public JsonResultBean importPersonnel(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // 客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // return personnelService.importPersonnel(file, ipAddress);
            return null;
        } catch (Exception e) {
            log.error("导入人员信息异常", e);
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ApiIgnore
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "人员列表模板");
            // personnelService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载人员列表模板异常", e);
        }
    }

    @RequestMapping(value = "/repetitionAdd", method = RequestMethod.POST)
    @ApiOperation(value = "人员编号重复校验,新增人员调用", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public boolean repetition(
        @ApiParam(value = "人员编号", required = true) @RequestParam("peopleNumber") String peopleNumber) {
        try {
            // Personnel vt = personnelService.findByNumber(peopleNumber);
            Personnel vt = null;
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据人员编号查询人员异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/repetitionEdit", method = RequestMethod.POST)
    @ApiOperation(value = "人员编号重复校验,修改以后用户时校验名称是否重复", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public boolean repetition(
        @ApiParam(value = "人员编号", required = true) @RequestParam("peopleNumber") String peopleNumber,
        @ApiParam(value = "人员ID", required = true) @RequestParam("id") String id) {
        try {
            // Personnel vt = personnelService.findByNumberId(id, peopleNumber);
            Personnel vt = null;
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据人员编号id查询人员异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/repetitionIdentity", method = RequestMethod.POST)
    @ApiOperation(value = "新增人员身份证重复校验", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public boolean repetitionIdentity(
        @ApiParam(value = "身份证号", required = true) @RequestParam("identity") String identity) {
        try {
            // Personnel vt = personnelService.findByPersonnel(identity);
            Personnel vt = null;
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据人员身份证号查询人员异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/repetitionIdentityEdit", method = RequestMethod.POST)
    @ApiOperation(value = "修改人员身份证重复校验", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    public boolean repetitionIdentityEdit(
        @ApiParam(value = "身份证号", required = true) @RequestParam("identity") String identyty,
        @ApiParam(value = "人员ID", required = true) @RequestParam("id") String id) {
        try {
            // Personnel vt = personnelService.findByPersonnel(id, identyty);
            Personnel vt = null;
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据人员身份证号和id查询人员异常", e);
            return false;
        }
    }

}
