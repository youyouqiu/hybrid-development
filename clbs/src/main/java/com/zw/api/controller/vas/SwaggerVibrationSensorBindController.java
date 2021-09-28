package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorBindForm;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorBindQuery;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.workhourmgt.VibrationSensorBindService;
import com.zw.platform.service.workhourmgt.VibrationSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/swagger/v/workhourmgt/vbbind")
@Api(tags = { "工时车辆设置" }, description = "工时车辆设置相关api")
public class SwaggerVibrationSensorBindController {
    private static Logger log = LogManager.getLogger(SwaggerVibrationSensorBindController.class);

    private static final String LIST_PAGE = "vas/workhourmgt/vibrationsensorbind/list";

    private static final String BIND_PAGE = "vas/workhourmgt/vibrationsensorbind/bind";

    private static final String EDIT_PAGE = "vas/workhourmgt/vibrationsensorbind/edit";

    private static final String DETAIL_PAGE = "vas/workhourmgt/vibrationsensorbind/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private VibrationSensorBindService vibrationSensorBindService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VibrationSensorService vibrationSensorService;

    @Autowired
    private VehicleService vehService;

    @Autowired
    private HttpServletRequest request;

    @ApiOperation(value = "获取工时车辆设置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照振动传感器型号、车牌号进行模糊搜索", required = false,
            paramType = "query", dataType = "string"), })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final VibrationSensorBindQuery query) {
        try {
            if (query != null) {
                Page<VibrationSensorBind> result = vibrationSensorBindService.findWorkHourSensorBind(query);
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            log.error("获取工时车辆设置列表（findWorkHourSensorBind）异常", e);
            return new PageGridBean(false);
        }
    }

    @ApiOperation(value = "根据车辆id查询工时车辆绑定选项值", notes = "返回车辆实体，参考车辆集合，振动传感器集合", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    public JsonResultBean bindPage(@PathVariable("id") final String id) {
        try {
            JSONObject objJson = new JSONObject();
            // 查询车
            VehicleInfo vehicle = vehicleService.findVehicleById(id);
            // 查询参考车辆
            List<VibrationSensorBind> vehicleList = vibrationSensorBindService.findReferenceVehicle();
            // 查询震动传感器
            List<VibrationSensorForm> vibrationSensorList =
                    vibrationSensorService.findVibrationSensorByPage(null, false);
            objJson.put("vehicle", vehicle);
            objJson.put("vibrationSensorList", vibrationSensorList);
            objJson.put("vehicleList", vehicleList);
            return new JsonResultBean(objJson);
        } catch (Exception e) {
            log.error("查询工时车辆绑定选项值异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
         * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 绑定
     */
    @ApiOperation(value = "保存车辆与振动传感器的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "shockSensorId", value = "振动传感器id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean bind(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VibrationSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                if (vehService.findVehicleById(form.getVehicleId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该车辆id不存在，请重新输入！");
                }
                if (vibrationSensorService.findVibrationSensorById(form.getShockSensorId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该振动传感器id不存在，请重新输入！");
                }
                // 根据车辆id 删除绑定关系（避免同时操作）
                if (StringUtils
                    .isNotBlank(vibrationSensorBindService.findWorkHourVehicleByVid(form.getVehicleId()).getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该车辆已绑定振动传感器！");
                }
                String k = form.getOutputCorrectionK();
                String b = form.getOutputCorrectionB();
                if (StringUtils.isNotBlank(k)) {
                    if (!StringUtils.isNumeric(k)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "输出修正系数K必须为非负整数！");
                    } else {
                        int ki = Integer.parseInt(k);
                        if (ki < 1 || ki > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "输出修正系数K必须为1-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(b)) {
                    if (!StringUtils.isNumeric(b)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "输出修正系数B必须为非负整数！");
                    } else {
                        int bi = Integer.parseInt(b);
                        if (bi < 0 || bi > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "输出修正系数K必须为0-200之间的整数！");
                        }
                    }
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 新增绑定表
                return vibrationSensorBindService.addWorkHourSensorBind(form, ipAddress);
            }
        } catch (Exception e) {
            log.error("保存车辆与振动传感器的绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
         * @param id
     * @return ModelAndView
     * @throws @author wangying
     * @Title: 修改
     */
    @ApiOperation(value = "根据车辆id查询车辆与邮箱油杆的绑定详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable("id") final String id, HttpServletResponse response) {
        try {
            JSONObject objJson = new JSONObject();
            // 查询已绑定的车
            List<VibrationSensorBind> vehicleList = vibrationSensorBindService.findReferenceVehicle();
            // 查询震动传感器
            List<VibrationSensorForm> vibrationSensorList =
                    vibrationSensorService.findVibrationSensorByPage(null, false);
            // 根据车辆id查询车与传感器的绑定
            VibrationSensorBind sensor = vibrationSensorBindService.findWorkHourVehicleById(id);
            if (sensor != null) {
                objJson.put("vibrationSensorList", vibrationSensorList);
                objJson.put("vehicleList", vehicleList);
                objJson.put("result", sensor);
                return new JsonResultBean(objJson);
            } else {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('该条数据已解除绑定！');");
                out.println("myTable.refresh();");
                out.println("</script>");
                return null;
            }
        } catch (Exception e) {
            log.error("查询车辆与邮箱油杆的绑定详情异常", e);
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
    @ApiOperation(value = "修改车辆与振动传感器的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "shockSensorId", value = "振动传感器id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VibrationSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                if (vehService.findVehicleById(form.getVehicleId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该车辆id不存在，请重新输入！");
                }
                if (vibrationSensorService.findVibrationSensorById(form.getShockSensorId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该振动传感器id不存在，请重新输入！");
                }
                if (vibrationSensorBindService.findWorkHourVehicleById(form.getId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该条数据已解除绑定！");
                }
                String k = form.getOutputCorrectionK();
                String b = form.getOutputCorrectionB();
                if (StringUtils.isNotBlank(k)) {
                    if (!StringUtils.isNumeric(k)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "输出修正系数K必须为非负整数！");
                    } else {
                        int ki = Integer.parseInt(k);
                        if (ki < 1 || ki > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "输出修正系数K必须为1-200之间的整数！");
                        }
                    }
                }
                if (StringUtils.isNotBlank(b)) {
                    if (!StringUtils.isNumeric(b)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "输出修正系数B必须为非负整数！");
                    } else {
                        int bi = Integer.parseInt(b);
                        if (bi < 0 || bi > 200) {
                            return new JsonResultBean(JsonResultBean.FAULT, "输出修正系数K必须为0-200之间的整数！");
                        }
                    }
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 新增绑定表
                return vibrationSensorBindService.updateWorkHourSensorBind(form, ipAddress);
            }
        } catch (Exception e) {
            log.error("修改车辆与振动传感器的绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除 终端
     */
    @ApiOperation(value = "根据绑定id删除车辆与振动传感器的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (!id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return vibrationSensorBindService.deleteWorkHourSensorBindById(id, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除车辆与振动传感器的绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据绑定ids批量删除车辆与振动传感器的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除id集合String(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            // 获取参数---页面传过来的一组数据
            String items = request.getParameter("deltems");
            // 获取客户端的IP地址
            String ipAddress = new GetIpAddr().getIpAddr(request);
            if (!items.isEmpty()) {
                return vibrationSensorBindService.deleteWorkHourSensorBindById(items, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除车辆与振动传感器的绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id下发围栏
     */
    @ApiOperation(value = "工时车辆设置下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/sendWorkHour", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendWorkHour(String sendParam) {
        try {
            ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
            if (paramList != null && paramList.size() > 0) {
                // 获取ip地址
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                // 工时下发
                vibrationSensorBindService.sendWorkHour(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("工时车辆设置下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}
