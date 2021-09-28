package com.zw.api2.controller;

import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerRodSensorForm;
import com.zw.api2.swaggerEntity.SwaggerRodSensorUpdateForm;
import com.zw.api2.swaggerEntity.SwaggerSimpleRodSensorQuery;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.basicinfo.query.RodSensorQuery;
import com.zw.platform.service.basicinfo.RodSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * Created by Tdz on 2016/7/20.
 */
@RestController
@RequestMapping("/api/m/basicinfo/equipment/rodsensor")
@Api(tags = {"油杆传感器（即油位传感器）dev"}, description = "油杆传感器相关api")
public class ApiRodSensorController {

    private static final String ADD_PAGE = "modules/basicinfo/equipment/rodsensor/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/rodsensor/edit";

    @Autowired
    private RodSensorService rodSensorService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String syError;

    @Value("${add.success}")
    private String addSuccess;

    @Value("${add.fail}")
    private String addFail;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${set.fail}")
    private String setFail;

    private static Logger logger = LogManager.getLogger(ApiRodSensorController.class);

    /**
     * 分页查询
     */
    @ApiOperation(value = "获取油杆传感器列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(
        @ModelAttribute("swaggerSimpleRodSensorQuery") final SwaggerSimpleRodSensorQuery swaggerSimpleRodSensorQuery) {
        RodSensorQuery query = new RodSensorQuery();

        BeanUtils.copyProperties(swaggerSimpleRodSensorQuery, query);
        try {
            Page<RodSensor> result = rodSensorService.findByPage(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            logger.error("分页查询（findByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 新增
     */
    @ApiIgnore
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增
     */
    @ApiOperation(value = "添加油杆传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ValidGroupAdd.class})
                                    @ModelAttribute("form") final SwaggerRodSensorForm swaggerRodSensorForm) {
        RodSensorForm form = new RodSensorForm();
        try {
            BeanUtils.copyProperties(swaggerRodSensorForm, form);
            String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return rodSensorService.add(form, ipAddress);
        } catch (Exception e) {
            logger.error("新增油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 根据id（油位id）删除 Personnl
     */
    @ApiOperation(value = "删除油杆传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") @ApiParam("油位id") final String id) {
        try {
            if (id != null && !"".equals(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return rodSensorService.delete(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("删除油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改
     */
    @ApiOperation(value = "根据id获取油杆传感器信息", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ResponseBody
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable("id") @ApiParam("油位传感器id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", rodSensorService.get(id));
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            logger.error("修改油位传感器参数弹出页面异常", e);
            return new JsonResultBean(syError);
        }
    }

    /**
     * 修改
     */
    @ApiOperation(value = "修改油杆传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class })
        @ModelAttribute("form") final SwaggerRodSensorUpdateForm swaggerRodSensorUpdateForm) {
        RodSensorForm form = new RodSensorForm();
        BeanUtils.copyProperties(swaggerRodSensorUpdateForm, form);
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);// 获取客户端的IP地址
            return rodSensorService.update(form, ipAddress);
        } catch (Exception e) {
            logger.error("修改油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除油杆传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "deltems", value = "批量删除的油杆传感器ids(用逗号隔开)",
        required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return rodSensorService.delete(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("批量删除油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 导出excel表
     *
     * @throws UnsupportedEncodingException
     */
    @ApiIgnore
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "油位传感器列表");
            rodSensorService.exportInfo(null, 1, response);
        } catch (Exception e) {
            logger.error("导出油位传感器列表异常", e);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // 客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = rodSensorService.importSensor(file, request, ipAddress);
            String msg = "导入结果：" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            logger.error("导入油位传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }

    }

    /**
     * 下载模板
     *
     * @throws UnsupportedEncodingException
     */
    @ApiIgnore
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "油位传感器列表模板");
            rodSensorService.generateTemplate(response);
        } catch (Exception e) {
            logger.error("下载油位传感器列表模板异常", e);
        }
    }

    @ApiOperation(value = "检查油杆传感器是否已经存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("sensorNumber") String sensorNumber) {
        try {
            RodSensor vt = rodSensorService.findByRodSensor(sensorNumber);
            return vt == null;
        } catch (Exception e) {
            logger.error("油位传感器检查异常", e);
            return false;
        }
    }
}
