package com.zw.api.controller.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.oilmgt.FluxSensor;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorQuery;
import com.zw.platform.service.oilmgt.FluxSensorService;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/swagger/v/oilmgt/fluxsensormgt")
@Api(tags = { "流量传感器" }, description = "流量传感器相关api")
public class SwaggerFluxSensorMgtController {
    @Autowired
    private FluxSensorService fluxSensorService;

    @Autowired
    private HttpServletRequest request;

    private static Logger logger = LogManager.getLogger(SwaggerFluxSensorMgtController.class);

    @ApiOperation(value = "获取流量传感器列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照流量传感器型号进行模糊搜索", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final FluxSensorQuery query) {
        try {
            if (query != null) {
                Page<FluxSensor> result = (Page<FluxSensor>) fluxSensorService.findFluxSensorByPage(query, true);
                return new PageGridBean(query, result, true);
            }
            return null;
        } catch (Exception e) {
            logger.error("分页查询分组（findFluxSensorByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * @param form
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author wangying
     * @Title: 添加流量传感器
     */
    @ApiOperation(value = "添加流量传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "oilWearNumber", value = "流量传感器型号", required = true, paramType = "query",
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
    public JsonResultBean addFluxSensor(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final FluxSensorForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                FluxSensor sensor = fluxSensorService.findByNumber(form.getOilWearNumber());
                if (sensor != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "流量传感器型号已存在，请重新输入！");
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fluxSensorService.addFluxSensor(form, ipAddress);
            }
        } catch (Exception e) {
            logger.error("添加流量传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "根据id获取流量传感器信息", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            FluxSensor sensor = fluxSensorService.findById(id);
            return new JsonResultBean(sensor);
        } catch (Exception e) {
            logger.error("获取流量传感器信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 修改
     */
    @ApiOperation(value = "修改流量传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "流量传感器id", required = true, paramType = "query", dataType = "String"),
        @ApiImplicitParam(name = "oilWearNumber", value = "流量传感器型号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "parity", value = "奇偶校验(1:奇校验;2:偶校验;3:无校验)", required = true, paramType = "query",
            dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "filterFactor", value = "滤波系数(1:实时;2:平滑;3:平稳)", required = true, paramType = "query",
            dataType = "string", defaultValue = "2"),
        @ApiImplicitParam(name = "baudRate", value = "波特率(1:2400;2:4800;3:9600;4:19200;5:38400;6:57600;7:115200)",
            required = true, paramType = "query", dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "inertiaCompEn", value = "补偿使能", required = true, paramType = "query",
            dataType = "string", defaultValue = "1") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final FluxSensorForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                FluxSensor sensor = fluxSensorService.findByNumber(form.getId(), form.getOilWearNumber());
                if (sensor != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "流量传感器型号已存在，请重新输入！");
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fluxSensorService.updateFluxSensor(form, ipAddress);
            }
        } catch (Exception e) {
            logger.error("修改流量传感器信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * @param id
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 删除
     */
    @ApiOperation(value = "删除流量传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !"".equals(id)) {
                String ip = new GetIpAddr().getIpAddr(request);
                // 删除流量传感器
                return fluxSensorService.deleteFluxSensor(id, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("删除流量传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * @param request
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 批量删除
     */
    @ApiOperation(value = "根据ids批量删除流量传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的流量传感器ids(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !"".equals(items)) {
                String ip = new GetIpAddr().getIpAddr(request);
                // 删除流量传感器
                return fluxSensorService.deleteFluxSensor(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("批量删除流量传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * @param fluxSensorNumber
     * @return JsonResultBean
     * @throws @author wangying
     * @Title: 编号去重
     */
    @ApiOperation(value = "检查流量传感器是否已经存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean repetition(@RequestParam("fluxSensorNumber") String fluxSensorNumber) {
        try {
            FluxSensor sensor = fluxSensorService.findByNumber(fluxSensorNumber);
            if (sensor == null) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("流量传感器是否已经存在检查异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "导入流量传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importFluxSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = fluxSensorService.importSensor(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            JsonResultBean result = new JsonResultBean(true, msg);
            return result;
        } catch (Exception e) {
            logger.error("导入流量传感器信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "下载流量传感器导入模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response, HttpServletRequest request) {
        try {
            ExportExcelUtil.setResponseHead(response, "流量传感器信息列表模板");
            fluxSensorService.generateTemplate(response);
        } catch (Exception e) {
            logger.error("下载流量传感器导入模板异常", e);
        }
    }

    /**
     * 导出
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "导出流量传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportTank(HttpServletResponse response, HttpServletRequest request) {
        try {
            ExportExcelUtil.setResponseHead(response, "流量传感器信息列表");
            fluxSensorService.export(null, 1, response);
        } catch (Exception e) {
            logger.error("导出流量传感器信息异常", e);
        }
    }
}
