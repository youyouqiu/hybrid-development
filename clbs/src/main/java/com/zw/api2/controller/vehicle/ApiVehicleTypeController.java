package com.zw.api2.controller.vehicle;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerVehicleStyleFormAdd;
import com.zw.api2.swaggerEntity.SwaggerVehicleStyleUpdateForm;
import com.zw.api2.swaggerEntity.SwaggerVehicleSubTypeForm;
import com.zw.api2.swaggerEntity.SwaggerVehicleSubTypeQuery;
import com.zw.api2.swaggerEntity.SwaggerVehicleSubTypeUpdateForm;
import com.zw.api2.swaggerEntity.SwaggerVehicleTypeFormAdd;
import com.zw.api2.swaggerEntity.SwaggerVehicleTypeQuery;
import com.zw.api2.swaggerEntity.SwaggerVehicleTypeUpdateForm;
import com.zw.platform.basic.domain.VehicleCategoryDO;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleSubTypeInfo;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.VehicleSubTypeForm;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleSubTypeQuery;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;
import com.zw.platform.service.basicinfo.VehicleCategoryService;
import com.zw.platform.service.basicinfo.VehicleSubTypeService;
import com.zw.platform.service.basicinfo.VehicleTypeService;
import com.zw.platform.service.personalized.IcoService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
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
import org.apache.commons.lang3.StringUtils;
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
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 车型管理Controller Modification by Wjy on 2016/8/31.
 */
@Controller
@RequestMapping("/api/m/basicinfo/monitoring/vehicle/type")
@Api(tags = { "车辆类型管理_dev" }, description = "车辆类型相关api")
public class ApiVehicleTypeController {
    private static Logger log = LogManager.getLogger(ApiVehicleTypeController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/vehicle/type/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/vehicle/type/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/vehicle/type/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/vehicle/type/import";

    private static final String LOGOADD_PAGE = "modules/basicinfo/monitoring/vehicle/type/vTypeAdd";

    private static final String LOGOEDIT_PAGE = "modules/basicinfo/monitoring/vehicle/type/vTypeEdit";

    private static final String ICO_PAGE = "modules/basicinfo/monitoring/vehicle/type/ico";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String ADD_SUB_TYPE_PAGE = "modules/basicinfo/monitoring/vehicle/type/addSubTypePage";

    private static final String EDIT_SUB_TYPE_PAGE = "modules/basicinfo/monitoring/vehicle/type/editSubTypePage";

    private static final String IMPORT_SUB_TYPE_PAGE = "modules/basicinfo/monitoring/vehicle/type/importSubTypePage";

    @Autowired
    private VehicleTypeService vehicleTypeService;

    @Autowired
    private VehicleCategoryService vehicleCategoryService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private VehicleSubTypeService vehicleSubTypeService;

    @Autowired
    private IcoService icoService;

    @Value("${sys.error.msg}")
    private String syError;

    @ApiIgnore
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 新增车辆类别
     */
    @ApiIgnore
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    @ApiIgnore
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/addLogo", method = RequestMethod.GET)
    public String addLogo() {
        return LOGOADD_PAGE;
    }

    @ApiIgnore
    @RequestMapping(value = "/ico", method = RequestMethod.GET)
    public String ico() {
        return ICO_PAGE;
    }

    @ApiIgnore
    @ApiOperation(value = "修改车辆类型logo界面_ModelAndView", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "cid", value = "车辆类型id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/editLogo", method = RequestMethod.GET)
    public ModelAndView editLogo(HttpServletRequest request, String cid) {
        try {
            ModelAndView mav = new ModelAndView(LOGOEDIT_PAGE);
            //String cid = request.getParameter("cid");
            VehicleType vehicle = vehicleCategoryService.get(cid);
            mav.addObject("result", vehicle);
            return mav;
        } catch (Exception e) {
            log.error("修改logo界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 新增车辆类型
     */
    @ApiOperation(value = "添加车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleCategory", value = "车辆类别id", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleType", value = "车辆类型", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "备注", required = false,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({
        ValidGroupAdd.class }) @ModelAttribute("vehicleTypeFormAdd") final SwaggerVehicleTypeFormAdd vehicleTypeFormAdd,
        final BindingResult bindingResult) {
        try {
            VehicleTypeForm form = new VehicleTypeForm();
            BeanUtils.copyProperties(vehicleTypeFormAdd, form);
            if (form != null) {
                // 获取访问服务器的客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                vehicleTypeService.add(form, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增车型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 分页查询车辆类型信息
     */
    @ApiOperation(value = "获取车辆类型信息列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query",
            dataType = "Long", defaultValue = "1"),
        @ApiImplicitParam(name = "length", value = "每页显示条数", required = true, paramType = "query",
            dataType = "Long", defaultValue = "10"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照车辆类别、车辆类型进行模糊搜索",
            required = false, paramType = "query", dataType = "string"), })
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(@ModelAttribute("vehicleTypeQuery") SwaggerVehicleTypeQuery vehicleTypeQuery) {
        try {
            VehicleTypeQuery query = new VehicleTypeQuery();
            BeanUtils.copyProperties(vehicleTypeQuery, query);
            Page<VehicleTypeDO> result = vehicleTypeService.findByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询车辆类型信息异常", e);
            return new PageGridBean(null, PageGridBean.FAULT, e.getMessage());
        }
    }

    /**
     * 根据id删除 VehicleType
     */
    @ApiOperation(value = "删除车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@ApiParam(value = "id") @PathVariable("id") final String id) {
        try {
            if (id != null) {
                // 绑定了子类型的类型不能被删除、绑定了车辆也不能被删除
                boolean isBand = vehicleTypeService.checkTypeIsBindingSubType(id);
                if (isBand) {
                    return new JsonResultBean(JsonResultBean.FAULT, "车辆类型已绑定车辆子类型，先解除绑定后才能删除！");
                }
                boolean isBandingVehicle = vehicleTypeService.getIsBand(id);
                if (isBandingVehicle) {
                    return new JsonResultBean(JsonResultBean.FAULT, "车辆类型已绑定车辆，先解除绑定后才能删除！");
                }
                // 获得访问服务器的客户端的ip地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return vehicleTypeService.delete(id, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 通过ID得到VehicleType
     */
    @ApiIgnore
    @ApiOperation(value = "根据id获取车辆类型信息_ModelAndView", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@ApiParam(value = "id") @PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", vehicleTypeService.get(id));
            return mav;
        } catch (Exception e) {
            log.error("修改车辆信息界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改VehicleType
     */
    @ApiOperation(value = "修改车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆类型id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleCategory", value = "车辆类别", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleType", value = "车辆类型", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "备注", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({
        ValidGroupUpdate.class }) @ModelAttribute("updateForm") final SwaggerVehicleTypeUpdateForm updateForm,
        final BindingResult bindingResult) {
        try {
            VehicleTypeForm form = new VehicleTypeForm();
            BeanUtils.copyProperties(updateForm, form);
            if (form != null) {
                // 获得访问服务器的客户端的ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return vehicleTypeService.update(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 导出excel表
     * @throws UnsupportedEncodingException
     * @Des 忽略导出
     */
    @ApiIgnore
    @ApiOperation(value = "导出车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "车型列表");
            vehicleTypeService.exportVehicleType(null, 1, response);
        } catch (Exception e) {
            log.error("导出车辆类型列表异常", e);
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除车辆类型信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的车辆类型ids(用逗号隔开)",
        required = true, paramType = "query", dataType = "Stirng")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null) {
                // 获得访问服务器的客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return vehicleTypeService.delete(items, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 导入
     * @Des 忽略导入
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
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = vehicleTypeService.importVehicleType(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     * @throws UnsupportedEncodingException
     */
    @ApiIgnore
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, HttpServletRequest request) {
        try {
            ExportExcelUtil.setResponseHead(response, "车型模板");
            response.setContentType("application/msexcel;charset=UTF-8");
            vehicleTypeService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载车辆类型模板异常", e);
        }
    }

    @ApiOperation(value = "检查车辆类型是否已经存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "车辆类型id", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "category", value = "车辆类别名称", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleType", value = "车辆类型名称", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(String vehicleType, String category, String vid) {
        try {
            VehicleTypeDO vt = null;
            VehicleTypeDTO vtype = vehicleTypeService.get(vid);
            vt = vehicleTypeService.findVehicleTypeId(category, vehicleType);
            if (vt == null) {
                return true;
            } else {
                if (vt != null && vtype != null) {
                    if (vt.getId().equals(vtype.getId())) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            log.error("校验车辆类型是否存在异常", e);
            return false;
        }

    }

    /**
     * 新增 车辆类别
     */
    @ApiOperation(value = "添加车辆类别信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/addCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addcategory(@Validated({
        ValidGroupAdd.class }) @ModelAttribute("styleFormAdd") final SwaggerVehicleStyleFormAdd styleFormAdd,
        final BindingResult bindingResult) {
        try {
            VehicleTypeForm form = new VehicleTypeForm();
            BeanUtils.copyProperties(styleFormAdd, form);
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                boolean flag = vehicleCategoryService.add(form, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增车辆类别信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 分页车辆类别信息
     */
    @ApiOperation(value = "分页车辆类别信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleCategory", value = "车辆类别模糊查询", required = true, paramType = "query") })
    @ResponseBody
    @RequestMapping(value = "/listCategory", method = RequestMethod.POST)
    public PageGridBean listCategory(String vehicleCategory) {
        VehicleTypeQuery query = new VehicleTypeQuery();
        query.setVehicleCategory(vehicleCategory);
        try {
            Page<VehicleType> result = vehicleCategoryService.findByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("查询车辆类别信息异常", e);
            return new PageGridBean(null, PageGridBean.FAULT, e.getMessage());
        }
    }

    /**
     * 通过车辆类别ID 查询车辆类别下的所有车辆类型
     * @return PageGridBean
     * @author zhouzongbo
     */
    @ApiOperation(value = "通过车辆类别ID 查询车辆类别下的所有车辆类型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "findCategoryById_{id}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findCategoryById(
        @ApiParam(value = "id") @PathVariable(value = "id") final String id) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("vehicleTypeList", vehicleCategoryService.findVehicleTypeByCategoryId(id));
            return new JsonResultBean(jsonObject);
        } catch (Exception e) {
            log.error("通过车辆类别ID查询车辆类别下的所以车辆类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }




    /**
     * 删除车辆类别
     * @param id
     * @return
     */
    @ApiOperation(value = "删除车辆类别信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆类别id", required = true, paramType = "query") })
    @RequestMapping(value = "/deleteCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteCategory(final String id) {
        try {
            if (id != null) {
                List<VehicleTypeDO> vehicleList = vehicleTypeService.findVehicleType(id);
                if (vehicleList.size() == 0) {
                    // 获取访问服务器的客户端的IP地址
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    // 删除
                    boolean flag = vehicleCategoryService.delete(id, ipAddress);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                } else {
                    String result = "false";
                    return new JsonResultBean(JsonResultBean.SUCCESS, result);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除车辆类别信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 批量删除车辆类别
     * @param ids 车辆类别id集合
     * @return
     */
    @ApiOperation(value = "批量删除车辆类别信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "ids", value = "要删除的车辆类别ids（id以“,”分开）", required = true, paramType = "query") })
    @RequestMapping(value = "/deleteMoreCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMoreCategory(String ids) {
        try {
            // 获取访问服务器的客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
            String result = vehicleCategoryService.deleteBatch(ids, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS, result);
        } catch (Exception e) {
            log.error("批量删除车辆类别信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 修改车辆类别
     * @param updateForm
     * @param bindingResult
     * @return
     */
    @ApiOperation(value = "修改车辆类别信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆类别id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "category", value = "车辆类别", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "standard", value = "标准（0：通用；1：货运；2：工程机械）", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "ico", value = "车辆类别图标", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "description", value = "备注", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/editCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editCategory(@Validated({
        ValidGroupUpdate.class }) @ModelAttribute("updateForm") final SwaggerVehicleStyleUpdateForm updateForm,
        final BindingResult bindingResult) {
        try {
            VehicleTypeForm form = new VehicleTypeForm();
            BeanUtils.copyProperties(updateForm, form);
            if (form != null) {
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                boolean flag = vehicleCategoryService.update(form, ipAddress);
                if (flag) {
                    //维护车辆使用图标文件名缓存
                    List<String> vid = icoService.getVidsByCategoryId(form.getId());
                    // icoService.maintainMonitorUseIcoName(vid);
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改车辆类别信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * @param vehicleCategory
     * @param id
     * @return
     */
    @ApiOperation(value = "校验车辆类别信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆类别id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleCategory", value = "车辆类别", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/repetitions", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitionCategory(String vehicleCategory, String id) {
        try {
            VehicleCategoryDO vt = null;
            if (StringUtils.isBlank(vehicleCategory)) {
                return false;
            }
            vt = vehicleCategoryService.findByVehicleType(vehicleCategory);

            if (StringUtils.isNotBlank(id)) {
                VehicleCategoryDTO category = vehicleCategoryService.getByStandard(id);
                if (Objects.isNull(category)) {
                    return false;
                }
                if (vehicleCategory.equals(category.getCategory())) {
                    return true;
                }
            }

            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("校验车辆类别信息存在异常", e);
            return false;
        }
    }

    @ApiIgnore
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/addSubType", method = RequestMethod.GET)
    public String addSubTypePage() {
        return ADD_SUB_TYPE_PAGE;
    }

    /**
     * 新增车辆子类型
     * @param subTypeForm   this
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @ApiOperation(value = "新增车辆子类型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "pid", value = "车辆类型id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleSubtypes", value = "车辆子类别", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "drivingWay", value = "行驶方式（0：自行；1：运输）",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "icoId", value = "车辆类别图标id", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "description", value = "备注", required = false,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/addSubType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addSubType(
        @Validated(ValidGroupAdd.class) @ModelAttribute("subTypeForm") SwaggerVehicleSubTypeForm subTypeForm,
        final BindingResult bindingResult) {
        try {
            VehicleSubTypeForm form = new VehicleSubTypeForm();
            BeanUtils.copyProperties(subTypeForm, form);
            if (form != null) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 获取访问ip
                    String ip = new GetIpAddr().getIpAddr(request);
                    boolean flag = vehicleSubTypeService.addSubType(form, ip);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增子类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * (新增/编辑)验证子类型是否重复
     * @param vehicleType     类型名
     * @param vehicleSubtypes 子类型名
     * @param id              编辑时传入id
     * @return boolean
     * @author zhouzongbo
     */
    @ApiOperation(value = "(新增/编辑)验证子类型是否重复", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleType", value = "车辆类型",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleSubtypes", value = "车辆子类别",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "checkSubTypeRepeat", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkSubTypeRepeat(String vehicleType, String vehicleSubtypes, String id) {
        try {
            if (StringUtils.isBlank(vehicleType) || StringUtils.isBlank(vehicleSubtypes)) {
                return false;
            }
            VehicleSubTypeDTO vehicleSubTypeInfo = vehicleSubTypeService.getSubTypeBy(vehicleType, vehicleSubtypes);
            if (vehicleSubTypeInfo == null) {
                return true;
            } else {
                if (StringUtils.isNotBlank(id)) {
                    VehicleSubTypeInfo vehicleSub = vehicleSubTypeService.getVehicleSubTypeById(id);
                    if (vehicleSub != null) {
                        if (vehicleSub.getId().equals(vehicleSubTypeInfo.getId())) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } catch (Exception e) {
            log.error("校验=子类型信息存在异常", e);
            return false;
        }
    }

    /**
     * 分页查询子类型列表
     * @param vehicleSubTypeQuery
     * @return PageGridBean
     * @author zhouzongbo
     */
    @ApiOperation(value = "获取车辆子类型信息列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true,
            paramType = "query", dataType = "Long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true,
            paramType = "query", dataType = "Long", defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照车辆子类型进行模糊搜索",
            required = false, paramType = "query", dataType = "string"), })
    @RequestMapping(value = "/findVehicleSubType", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean findVehicleSubType(SwaggerVehicleSubTypeQuery vehicleSubTypeQuery) {
        try {
            VehicleSubTypeQuery query = new VehicleSubTypeQuery();
            BeanUtils.copyProperties(vehicleSubTypeQuery, query);
            Page<VehicleSubTypeDTO> vehicleSubTypePage = vehicleSubTypeService.findVehicleSubTypePage(query);
            List<VehicleSubTypeInfo> pageList =
                vehicleSubTypePage.stream().map(VehicleSubTypeInfo::new).collect(Collectors.toList());
            Page<VehicleSubTypeInfo> result =
                new Page<>(query.getPage().intValue(), query.getLimit().intValue(), false);
            result.setTotal(vehicleSubTypePage.getTotal());
            result.addAll(pageList);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询车辆子类型信息异常", e);
            return new PageGridBean(null, PageGridBean.FAULT, e.getMessage());
        }
    }

    /**
     * 修改页面
     * @return ModelAndView
     * @author zhouzongbo
     */
    @ApiIgnore
    @ApiOperation(value = "根据id获取车辆子类型信息_ModelAndView", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆类型id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/updateSubType_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editSubType(@PathVariable(value = "id") String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_SUB_TYPE_PAGE);
            VehicleSubTypeInfo vehicleSubTypeInfo = vehicleSubTypeService.getVehicleSubTypeById(id);
            mav.addObject("result", vehicleSubTypeInfo);
            return mav;
        } catch (Exception e) {
            log.error("修改子类型界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 编辑子类型
     * @param subTypeUpdateForm this
     * @param result            验证
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @ApiOperation(value = "修改车辆子类型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/updateSubType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSubType(@Validated(value = {
        ValidGroupUpdate.class }) @ModelAttribute("subTypeUpdateForm")
        SwaggerVehicleSubTypeUpdateForm subTypeUpdateForm,
        BindingResult result) {
        try {
            VehicleSubTypeForm form = new VehicleSubTypeForm();
            BeanUtils.copyProperties(subTypeUpdateForm, form);
            if (form != null) {
                if (result.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
                } else {
                    String ip = new GetIpAddr().getIpAddr(request);
                    boolean flag = vehicleSubTypeService.updateSubType(form, ip);
                    if (flag) {
                        //维护车辆使用图标文件名缓存
                        List<String> vids = icoService.getVidsBySubTypeId(form.getId());
                        if (vids != null && vids.size() > 0) {
                            // icoService.maintainMonitorUseIcoName(vids);
                        }
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("修改车辆子类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 删除子类型
     * @param id 子类型id
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @ApiOperation(value = "删除子类型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/deleteSubType_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean deleteSubType(@ApiParam(value = "id") @PathVariable(value = "id") final String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                // 判断是否绑定了车辆
                boolean isBinding = vehicleSubTypeService.checkVehicleSubTypeIsBinding(id);
                if (isBinding) {
                    return new JsonResultBean(JsonResultBean.FAULT, "车辆子类型已经绑定了车辆，请先解绑后删除~");
                }
                String ip = new GetIpAddr().getIpAddr(request);
                return vehicleSubTypeService.deleteSubType(id, ip, 0);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除子类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 批量删除子类型
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @ApiOperation(value = "批量删除子类型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/deleteMoreSubType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMoreSubType() {
        try {
            String vehicleSubTypeIds = request.getParameter("vehicleSubTypeIds");
            if (StringUtils.isNotBlank(vehicleSubTypeIds)) {
                String ip = new GetIpAddr().getIpAddr(request);
                return vehicleSubTypeService.deleteSubType(vehicleSubTypeIds, ip, 1);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除子类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 车辆子类型导出(暂时保留，后续可删除)
     * @param response this response
     * @author zhouzongbo
     */
    @ApiIgnore
    @ApiOperation(value = "车辆子类型导出", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/exportSubType", method = RequestMethod.GET)
    @ResponseBody
    public void exportSubType(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "车辆子类型");
            vehicleSubTypeService.exportSubType(null, 1, response);
        } catch (Exception e) {
            log.error("导出车辆子类型列表异常", e);
        }
    }

    /**
     * 查询车辆类型下的车辆子类型
     * @param id 车辆类型id
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @ApiOperation(value = "查询车辆类型下的车辆子类型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/findTypeIsBindingSubType_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findTypeIsBindingSubType(@ApiParam(value = "id") @PathVariable(value = "id") String id) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", vehicleTypeService.findTypeIsBindingSubType(id));
            return new JsonResultBean(jsonObject);
        } catch (Exception e) {
            log.error("查询车辆类型下的车辆子类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 查询类别标准是2(工程机械)的所有类型
     */
    @ApiOperation(value = "查询类别标准是2(工程机械)的所有类型", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ResponseBody
    @RequestMapping(value = "/vehicleTypes", method = RequestMethod.POST)
    public JsonResultBean list() {
        try {
            List<VehicleTypeDO> result = vehicleTypeService.findVehicleTypes(2);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("查询车辆类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 查询绑定了车辆类型的类别
     * @return JsonResultBean
     * @author zhouzongbo
     */
    @ApiOperation(value = "查询绑定了车辆类型的类别", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/findAllVehicleCategoryHasBindingVehicleType.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean findAllVehicleCategoryHasBindingVehicleType() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", vehicleCategoryService.findVehicleCategoryList());
            return new JsonResultBean(jsonObject);
        } catch (Exception e) {
            log.error("查询车辆列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }
}
