package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.basicinfo.query.RodSensorQuery;
import com.zw.platform.service.basicinfo.RodSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

/**
 * Created by Tdz on 2016/7/20.
 */
@RestController
@RequestMapping("/swagger/m/basicinfo/equipment/rodsensor")
@Api(tags = { "油杆传感器" }, description = "油杆传感器相关api")
public class SwaggerRodSensorController {

    private static final String DELETE_ERROR_MSSAGE = "部分传感器已经和车辆绑定了，到【油量车辆设置】中解除绑定后才可以删除哟！";

    private static final String LIST_PAGE = "modules/basicinfo/equipment/rodsensor/list";

    private static final String ADD_PAGE = "modules/basicinfo/equipment/rodsensor/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/rodsensor/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/equipment/rodsensor/import";

    @Autowired
    private RodSensorService rodSensorService;

    @Autowired
    private HttpServletRequest request;

    private static Logger logger = LogManager.getLogger(SwaggerRodSensorController.class);

    /**
     * 分页查询
     */
    @ApiOperation(value = "获取油杆传感器列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照油杆传感器型号进行模糊搜索", required = false, paramType = "query",
            dataType = "string"), })
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final RodSensorQuery query) {
        try {
            Page<RodSensor> result = rodSensorService.findByPage(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            logger.error("分页查询分组（findByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 新增
     */
    @ApiOperation(value = "添加油杆传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "sensorNumber", value = "油杆传感器型号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorLength", value = "油杆传感器长度(长度mm)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oddEvenCheck", value = "奇偶校验(1:奇校验;2:偶校验;3:无校验)", required = true,
            paramType = "query", dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "filteringFactor", value = "滤波系数(1:实时;2:平滑;3:平稳)", required = true,
            paramType = "query", dataType = "string", defaultValue = "2"),
        @ApiImplicitParam(name = "baudRate", value = "波特率(1:2400;2:4800;3:9600;4:19200;5:38400;6:57600;7:115200)",
            required = true, paramType = "query", dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "compensationCanMake", value = "补偿使能", required = true, paramType = "query",
            dataType = "string", defaultValue = "1") })
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final RodSensorForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                RodSensor vt = rodSensorService.findByRodSensor(form.getSensorNumber());
                if (vt != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油杆传感器型号已存在，请重新输入！");
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                rodSensorService.add(form, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("新增油杆传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除 Personnl
     */
    @ApiOperation(value = "删除油杆传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (rodSensorService.getIsBand(id) > 0) {
                return new JsonResultBean(JsonResultBean.FAULT, "已绑定油箱不能删除");
            } else {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                rodSensorService.delete(id, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("删除油杆传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改
     */
    @ApiOperation(value = "根据id获取油杆传感器信息", notes = "修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable final String id) {
        try {
            return new JsonResultBean(rodSensorService.get(id));
        } catch (Exception e) {
            logger.error("获取油杆传感器信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改
     */
    @ApiOperation(value = "修改油杆传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "油杆传感器id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sensorNumber", value = "油杆传感器型号", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorLength", value = "油杆传感器长度(长度mm)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oddEvenCheck", value = "奇偶校验(1:奇校验;2:偶校验;3:无校验)", required = true,
            paramType = "query", dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "filteringFactor", value = "滤波系数(1:实时;2:平滑;3:平稳)", required = true,
            paramType = "query", dataType = "string", defaultValue = "2"),
        @ApiImplicitParam(name = "baudRate", value = "波特率(1:2400;2:4800;3:9600;4:19200;5:38400;6:57600;7:115200)",
            required = true, paramType = "query", dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "compensationCanMake", value = "补偿使能", required = true, paramType = "query",
            dataType = "string", defaultValue = "1") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final RodSensorForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                RodSensor vt = rodSensorService.findByRodSensor(form.getId(), form.getSensorNumber());
                if (vt != null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "油杆传感器型号已存在，请重新输入！");
                }
                // 获取客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                rodSensorService.update(form, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("修改油杆传感器信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除油杆传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除的油杆传感器ids(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            JSONObject msg = new JSONObject();
            StringBuilder deleteFailMsg = new StringBuilder();
            for (int i = 0; i < item.length; i++) {
                if (rodSensorService.getIsBand(item[i]) > 0) {
                    RodSensor rs = rodSensorService.findById(item[i]);
                    deleteFailMsg.append((null != rs ? rs.getSensorNumber() : "") + "</br>");
                    // return new JsonResultBean(JsonResultBean.FAULT,"已绑定油箱不能删除");
                }
            }
            if (deleteFailMsg.length() > 0) {
                msg.put("msg", DELETE_ERROR_MSSAGE + "</br>" + "已绑定传感器型号如下：</br>" + deleteFailMsg.toString());
                return new JsonResultBean(msg);
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            rodSensorService.delete(items, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("批量删除的油杆传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 导出excel表
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "导出油杆传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, HttpServletRequest request) {
        try {
            String filename = "油杆传感器列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            rodSensorService.exportInfo(null, 1, response);
        } catch (Exception e) {
            logger.error("导出油杆传感器信息异常", e);
        }
    }

    @ApiOperation(value = "导入油杆传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSensor(@RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest request) {
        try {
            // 获取客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = rodSensorService.importSensor(file, request, ipAddress);
            String msg = "导入结果：" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            JsonResultBean result = new JsonResultBean(true, msg);
            return result;
        } catch (Exception e) {
            logger.error("导入油杆传感器信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "下载油杆传感器导入模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, HttpServletRequest request) {
        try {
            String filename = "油杆传感器列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            rodSensorService.generateTemplate(response);
        } catch (Exception e) {
            logger.error("下载油杆传感器导入模板异常", e);
        }
    }

    @ApiOperation(value = "检查油杆传感器是否已经存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("sensorNumber") @ApiParam("传感器型号") String sensorNumber) {
        try {
            RodSensor vt = rodSensorService.findByRodSensor(sensorNumber);
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("油杆传感器检查异常", e);
            return false;
        }
    }
}
