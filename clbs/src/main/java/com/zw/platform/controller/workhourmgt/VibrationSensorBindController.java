package com.zw.platform.controller.workhourmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorBindForm;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorBindQuery;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.workhourmgt.VibrationSensorBindService;
import com.zw.platform.service.workhourmgt.VibrationSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/v/workhourmgt/vbbind")
public class VibrationSensorBindController {
    private static Logger log = LogManager.getLogger(VibrationSensorBindController.class);

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
    private AlarmSettingService alarmSettingService;

    @Autowired
    private VibrationSensorService vibrationSensorService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${vehicle.bound.shocksensor}")
    private String vehicleBoundShocksensor;

    @Value("${data.relieve.bound}")
    private String dataRelieveBound;

    @Value("${bound.seccess}")
    private String boundSuccess;

    @Value("${bound.fail}")
    private String boundFail;

    @Value("${edit.success}")
    private String editSuccess;

    @Value("${edit.fail}")
    private String editFail;

    /**
     * 工时车辆设置列表
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页
     */
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
            log.error("分页查询分组（findWorkHourSensorBind）异常", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true) // 防止表单重复提交
    public ModelAndView bindPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            // 查询车
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            // 查询参考车辆
            List<VibrationSensorBind> vehicleList = vibrationSensorBindService.findReferenceVehicle();
            // 查询震动传感器
            List<VibrationSensorForm> vibrationSensorList =
                    vibrationSensorService.findVibrationSensorByPage(null, false);
            mav.addObject("vehicle", vehicle);
            mav.addObject("vibrationSensorList", JSON.toJSONString(vibrationSensorList));
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("绑定界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
         * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author wangying
     * @Title: 绑定(设置)
     */
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true) // 防止表单重复提交
    public JsonResultBean bind(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VibrationSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // 根据车辆id 删除绑定关系（避免同时操作）
                if (StringUtils
                    .isNotBlank(vibrationSensorBindService.findWorkHourVehicleByVid(form.getVehicleId()).getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, vehicleBoundShocksensor);
                }
                // 获取操作用户的IP地址
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                // 新增绑定表
                return vibrationSensorBindService.addWorkHourSensorBind(form, ip);
            }
        } catch (Exception e) {
            log.error("绑定震动传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         * @param id
     * @return ModelAndView
     * @throws @author wangying
     * @Title: 返回修改页面
     */
    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") final String id, HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // 查询已绑定的车
            List<VibrationSensorBind> vehicleList = vibrationSensorBindService.findReferenceVehicle();
            // 查询震动传感器
            List<VibrationSensorForm> vibrationSensorList =
                    vibrationSensorService.findVibrationSensorByPage(null, false);
            // 根据车辆id查询车与传感器的绑定

            VibrationSensorBind sensor = vibrationSensorBindService.findWorkHourVehicleById(id);
            if (sensor != null) {
                mav.addObject("vibrationSensorList", JSON.toJSONString(vibrationSensorList));
                mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
                mav.addObject("result", sensor);
                return mav;
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
            log.error("修改工时参数界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
         * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author wangying
     * @Title: 修改
     */
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
                if (vibrationSensorBindService.findWorkHourVehicleById(form.getId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, dataRelieveBound);
                }
                // 获取IP地址
                String ip = new GetIpAddr().getIpAddr(request);
                // 新增绑定表
                return vibrationSensorBindService.updateWorkHourSensorBind(form, ip);
            }
        } catch (Exception e) {
            log.error("修改工时参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         * @param id
     * @return ModelAndView
     * @throws @author wangying
     * @Title: 详情
     */
    @RequestMapping(value = { "/detail_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView detailPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            // 根据车辆id查询车与传感器的绑定
            VibrationSensorBind vibrationSensor = vibrationSensorBindService.findWorkHourVehicleByVid(id);
            mav.addObject("result", vibrationSensor);
            return mav;
        } catch (Exception e) {
            log.error("工时详情界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 根据id删除(解除绑定)
     */
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
            log.error("解除绑定振动传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            // 获取参数---页面传过来的一组数据
            String items = request.getParameter("deltems");
            if (!items.isEmpty()) {
                // 获取客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return vibrationSensorBindService.deleteWorkHourSensorBindById(items, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量解除振动传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id下发参数
     */
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
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
