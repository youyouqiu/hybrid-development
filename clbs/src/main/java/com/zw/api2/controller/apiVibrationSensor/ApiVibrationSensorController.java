package com.zw.api2.controller.apiVibrationSensor;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorQuery;
import com.zw.platform.service.workhourmgt.VibrationSensorService;
import com.zw.platform.util.GetIpAddr;
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
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("api/v/workhourmgt/vb")
@Api(tags = { "振动传感器_dev" }, description = "振动传感器相关api")
public class ApiVibrationSensorController {
    @Autowired
    private VibrationSensorService vibrationSensorService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${shocksensor.use.warn}")
    private String shocksensorUseWarn;

    @Value("${shocksensor.use.deleter}")
    private String shocksensorUseDeleter;

    private static Logger log = LogManager.getLogger(ApiVibrationSensorController.class);

    private static final String LIST_PAGE = "vas/workhourmgt/vibrationsensormgt/list";

    private static final String ADD_PAGE = "vas/workhourmgt/vibrationsensormgt/add";

    private static final String EDIT_PAGE = "vas/workhourmgt/vibrationsensormgt/edit";

    private static final String IMPORT_PAGE = "vas/workhourmgt/vibrationsensormgt/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @ApiIgnore
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询振动传感器
     * @param query
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "获取振动传感器列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照振动传感器型号进行模糊搜索", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final VibrationSensorQuery query) {
        try {
            if (query != null) {
                Page<VibrationSensorForm> result =
                    (Page<VibrationSensorForm>) vibrationSensorService.findVibrationSensorByPage(query, true);
                return new PageGridBean(query, result, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("分页查询分组（findVibrationSensorByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    @ApiIgnore
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() throws BusinessException {
        return ADD_PAGE;
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author fanlu
     * @Title: 添加振动传感器
     */
    @ApiOperation(value = "添加振动传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "sensorType", value = "振动传感器型号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "parity", value = "奇偶校验(1:奇校验;2:偶校验;3:无校验)", required = true, paramType = "query",
            dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "filterFactor", value = "滤波系数(1:实时;2:平滑;3:平稳)", required = true, paramType = "query",
            dataType = "string", defaultValue = "2"),
        @ApiImplicitParam(name = "baudRate", value = "波特率(1:2400;2:4800;3:9600;4:19200;5:38400;6:57600;7:115200)",
            required = true, paramType = "query", dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "inertiaCompEn", value = "补偿使能", required = true, paramType = "query",
            dataType = "string", defaultValue = "1") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final VibrationSensorForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                    return vibrationSensorService.addVibrationSensor(form, ip);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增振动传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id删除振动传感器
     */
    @ApiOperation(value = "删除振动传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @Transactional
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return vibrationSensorService.deleteVibrationSensor(id, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除振动传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除振动传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的振动传感器ids(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!items.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return vibrationSensorService.deleteVibrationSensor(items, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除振动传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改振动传感器
     */
    @ApiOperation(value = "根据id获取振动传感器信息", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            VibrationSensorForm form = vibrationSensorService.findVibrationSensorById(id);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("修改振动传感器弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           fanlu
     * @Title: 修改振动传感器
     */
    @ApiOperation(value = "修改振动传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "振动传感器id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sensorType", value = "振动传感器型号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "parity", value = "奇偶校验(1:奇校验;2:偶校验;3:无校验)", required = true, paramType = "query",
            dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "filterFactor", value = "滤波系数(1:实时;2:平滑;3:平稳)", required = true, paramType = "query",
            dataType = "string", defaultValue = "2"),
        @ApiImplicitParam(name = "baudRate", value = "波特率(1:2400;2:4800;3:9600;4:19200;5:38400;6:57600;7:115200)",
            required = true, paramType = "query", dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "inertiaCompEn", value = "补偿使能", required = true, paramType = "query",
            dataType = "string", defaultValue = "1") })
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VibrationSensorForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                form.setUpdateDataTime(new Date());
                form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                boolean flag = vibrationSensorService.updateVibrationSensor(form, ip);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
        } catch (Exception e) {
            log.error("修改振动传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * @param sensorNumber
     * @return JsonResultBean
     * @throws @author fanlu
     * @Title: 编号去重
     */
    @ApiOperation(value = "检查振动传感器是否已经存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean repetition(@RequestParam("sensorNumber") String sensorNumber) {
        try {
            if (sensorNumber != null) {
                int sensor = vibrationSensorService.findByNumber(sensorNumber);
                if (sensor == 0) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改振动传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出
     * @throws UnsupportedEncodingException
     */
    @ApiIgnore
    @ApiOperation(value = "导出振动传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "振动传感器列表");
            vibrationSensorService.export(null, 1, response);
        } catch (Exception e) {
            log.error("导出振动传感器列表异常", e);
        }
    }

    /**
     * 跳转到导入界面
     * @return String
     * @throws @author Liubangquan
     * @Title: importPage
     */
    @ApiIgnore
    @ApiOperation(value = "导入振动传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * 导入操作
     * @param file
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: importData
     */
    @ApiIgnore
    @ApiOperation(value = "导入振动传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importData(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
            Map resultMap = vibrationSensorService.importData(file, ipAddress);
            String msg = "导入结果：" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入振动传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下载模板
     * @param response
     * @return void
     * @throws @author Liubangquan
     * @Title: download
     */
    @ApiIgnore
    @ApiOperation(value = "下载振动传感器导入模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "振动传感器列表模板");
            vibrationSensorService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载振动传感器列表模板异常", e);
        }
    }

}
