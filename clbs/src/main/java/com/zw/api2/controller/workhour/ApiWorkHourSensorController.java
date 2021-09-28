package com.zw.api2.controller.workhour;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerWorkHourSensorForm;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSensorInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSensorQuery;
import com.zw.platform.service.workhourmgt.WorkHourSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 工时传感器控制层
 * @author denghuabing
 * @version 1.0
 * @date 2018.5.29
 */
@Controller
@RequestMapping("api/v/workhourmgt/workhoursensor")
@Api(tags = { "工时传感器管理_dev" }, description = "工时传感器管理相关api")
public class ApiWorkHourSensorController {
    private static Logger log = LogManager.getLogger(ApiWorkHourSensorController.class);

    @Autowired
    private WorkHourSensorService workHourSensorService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 分页查询
     */
    @ApiOperation(value = "工时传感器管理列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照传感器型号进行模糊搜索", paramType = "query",
            dataType = "string"), })
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean workHourSensorList(final WorkHourSensorQuery query) {
        try {
            if (query != null) {
                Page<WorkHourSensorInfo> result = workHourSensorService.findByPage(query);
                return new PageGridBean(result, true);
            } else {
                return new PageGridBean(PageGridBean.FAULT);
            }
        } catch (Exception e) {
            log.error("分页查询异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 传感器型号重复校验
     * 修改时传id
     * @param id 修改时使用
     */
    @ApiOperation(value = "工时传感器型号重复校验", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "sensorNumber", value = "传感器型号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "id", value = "传感器id（修改时传递）", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(final String sensorNumber, String id) {
        if (sensorNumber != null) {
            return workHourSensorService.repetition(sensorNumber, id);
        } else {
            return false;
        }
    }

    /**
     * 新增工时传感器
     */
    @ApiOperation(value = "新增工时传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "detectionMode", value = "检测方式(1:电压比较式;2:油耗阈值式;3:油耗波动式)", required = true,
            paramType = "query", dataType = "Integer") })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addWorkHourSensor(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("swaggerForm") final SwaggerWorkHourSensorForm swaggerForm,
        final BindingResult bindingResult) {
        try {
            WorkHourSensorForm form = new WorkHourSensorForm();
            BeanUtils.copyProperties(swaggerForm, form);
            //校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSensorService.addWorkHourSensor(form, ipAddress);
            }
        } catch (Exception e) {
            log.error("新增工时传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改工时传感器页面
     */
    @ApiOperation(value = "修改工时传感器页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean workHourSensorEditPage(
        @ApiParam(value = "工时传感器id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            WorkHourSensorForm result = workHourSensorService.findWorkHourSensorById(id);
            data.put("result", result);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("修改传感器页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改传感器
     */
    @ApiOperation(value = "修改工时传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "传感器id", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editWorkHourSensor(@Validated({ ValidGroupUpdate.class }) @ModelAttribute(
        "swaggerForm") final SwaggerWorkHourSensorForm swaggerForm, final BindingResult bindingResult) {
        try {
            WorkHourSensorForm form = new WorkHourSensorForm();
            BeanUtils.copyProperties(swaggerForm, form);
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSensorService.updateWorkHourSensor(form, ipAddress);
            }
        } catch (Exception e) {
            log.error("修改传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 删除工时传感器
     */
    @ApiOperation(value = "删除工时传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteWorkHourSensor(
        @ApiParam(value = "传感器id", required = true) @PathVariable("id") final String id) {
        try {
            if (id != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSensorService.deleteWorkHourSensor(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("删除工时传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量删除
     * @param deltems 需要删除的id以','分割
     */
    @ApiOperation(value = "批量删除工时传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deltems", value = "传感器ids（多个id以','隔开）", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(final String deltems) {
        try {
            if (!StringUtil.isNullOrEmpty(deltems)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSensorService.deleteMore(deltems, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量删除异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 下载模板
     */
    @ApiIgnore
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "工时传感器模板");
            workHourSensorService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载模板异常", e);
        }
    }

    /**
     * 导出excel表
     */
    @ApiIgnore
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "工时传感器表");
            workHourSensorService.exportWorkHourSensor(null, 1, response);
        } catch (Exception e) {
            log.error("导出excel表异常");
        }
    }

    /**
     * 批量导入
     */
    @ApiIgnore
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importWorkHourSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            if (file != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                Map resultMap = workHourSensorService.importWorkHourSensor(file, ipAddress);
                String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
                return new JsonResultBean(true, msg);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量导入异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }
}
