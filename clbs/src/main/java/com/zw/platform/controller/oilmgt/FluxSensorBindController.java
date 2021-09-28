package com.zw.platform.controller.oilmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.oilmgt.FluxSensor;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorBindForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorBindQuery;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.service.oilmgt.FluxSensorBindService;
import com.zw.platform.service.oilmgt.FluxSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.VehicleUtil;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * <p>Title: 流量传感器绑定Controller</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年9月18日下午5:07:13
 */
@Controller
@RequestMapping("/v/oilmgt/fluxsensorbind")
public class FluxSensorBindController {
    private static Logger log = LogManager.getLogger(FluxSensorBindController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${up.error}")
    private String upError;

    @Value("${device.info.null}")
    private String deviceInfoNull;

    @Value("${vehicle.bound.oilwear}")
    private String vehicleBoundOilwear;

    @Value("${up.error.fluxsensor.type}")
    private String upErrorFluxsensorType;

    @Value("${data.relieve.bound}")
    private String dataRelieveBound;

    @Autowired
    private FluxSensorBindService fluxSensorBindService;

    @Autowired
    private FluxSensorService fluxSensorService;

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    @Autowired
    private AlarmSettingService alarmSettingService;

    @Autowired
    private HttpServletRequest request;

    private static final String LIST_PAGE = "vas/oilmgt/fluxsensorbind/list";

    private static final String BIND_PAGE = "vas/oilmgt/fluxsensorbind/bind";

    private static final String EDIT_PAGE = "vas/oilmgt/fluxsensorbind/edit";

    private static final String DETAIL_PAGE = "vas/oilmgt/fluxsensorbind/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String UPGRADE_PAGE = "vas/oilmgt/fluxsensorbind/upgrade";

    private static final String BASICINFO_PAGE = "vas/oilmgt/fluxsensorbind/basicInfo"; // 基本信息

    private static final String GENERAL_PAGE = "vas/oilmgt/fluxsensorbind/general"; // 常规参数

    private static final String NEWSLETTER_PAGE = "vas/oilmgt/fluxsensorbind/newsletter"; // 通讯参数

    private static final String PARAMETERS_PAGE = "vas/oilmgt/fluxsensorbind/parameters"; // 私有参数

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final FluxSensorBindQuery query) {
        try {
            if (query != null) {
                Page<FuelVehicle> result = (Page<FuelVehicle>) fluxSensorBindService.findFluxSensorBind(query);
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            log.error("分页查询分组（findFluxSensorBind）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 刷新参数下发状态
     * @param vehicleId
     * @return
     */
    @RequestMapping(value = { "/refreshSendStatus" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean refreshSendStatus(String vehicleId) {
        try {
            FuelVehicle fuelVehicle = fluxSensorBindService.findFluxSensorByVid(vehicleId);
            if (fuelVehicle != null) {
                return new JsonResultBean(fuelVehicle);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("油耗刷新参数下发状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }


    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true) // 防止表单重复提交
    public ModelAndView bindPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            // 查询已绑定的车
            VehicleInfo vehicle = new VehicleInfo();
            BindDTO configList = VehicleUtil.getBindInfoByRedis(id);
            String deviceType = configList.getDeviceType();
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            List<FuelVehicle> vehicleList = fluxSensorBindService.findReferenceVehicleByProtocols(protocols);
            vehicle.setId(configList.getId());
            vehicle.setBrand(configList.getName());
            // 查询流量传感器
            List<FluxSensor> fluxSensorList = fluxSensorService.findFluxSensorByPage(null, false);
            mav.addObject("vehicle", vehicle);
            mav.addObject("fluxSensorList", JSON.toJSONString(fluxSensorList));
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
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 绑定
     */
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true) // 防止表单重复提交
    public JsonResultBean bind(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final FluxSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
                    // 新增绑定表
                    return fluxSensorBindService.addFluxSensorBind(form, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("设置油耗参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         * @param id
     * @return ModelAndView
     * @throws BusinessException
     * @throws IOException
     * @throws @author           wangying
     * @Title: 修改
     */
    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") final String id, HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // 查询流量传感器
            List<FluxSensor> fluxSensorList = fluxSensorService.findFluxSensorByPage(null, false);
            // 根据车辆id查询车与传感器的绑定
            FuelVehicle sensor = fluxSensorBindService.findFuelVehicleById(id);
            if (sensor != null) {
                // 查询已绑定的车
                BindDTO configList = VehicleUtil.getBindInfoByRedis(sensor.getVId());
                String deviceType = configList.getDeviceType();
                List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
                List<FuelVehicle> vehicleList = fluxSensorBindService.findReferenceVehicleByProtocols(protocols);
                mav.addObject("fluxSensorList", JSON.toJSONString(fluxSensorList));
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
            log.error("修改油耗参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
         * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws IOException
     * @throws @author           wangying
     * @Title: 修改
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final FluxSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    if (fluxSensorBindService.findFuelVehicleById(form.getId()) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, dataRelieveBound);
                    }
                    String ipAddress = new GetIpAddr().getIpAddr(request); // 修改分组日志
                    // 新增绑定表
                    return fluxSensorBindService.updateFluxSensorBind(form, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改油耗参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id删除 终端
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !"".equals(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fluxSensorBindService.deleteFluxSensorBind(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("解绑流量传感器异常", e);
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
            String items = request.getParameter("deltems");
            if (items != null && !"".equals(items)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fluxSensorBindService.deleteFluxSensorBind(items, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("油耗车辆 批量解绑异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/detail_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView detailPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            // 查询车
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleByVid(id);
            mav.addObject("vehicle", vehicle);
            mav.addObject("result", fuelVehicle);
            return mav;
        } catch (Exception e) {
            log.error("油耗配置详情界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 根据id下发围栏
     */
    @RequestMapping(value = "/sendFuel", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendFuel(String sendParam) {
        try {
            if (sendParam != null && !"".equals(sendParam)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fluxSensorBindService.sendFuel(sendParam, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("下发油耗参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * @return 远程升级
     * @throws BusinessException
     * @author Axh
     */
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView upgradePage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(UPGRADE_PAGE);
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return mav;
        } catch (Exception e) {
            log.error("远程升级弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 远程升级
     * @author LiFudong
     */
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(WirelessUpdateParam wirelessParam, String vehicleId, final BindingResult bindingResult) {
        try {
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            Integer commandType = 131;
            String commandTypeStr = request.getParameter("commandType");
            if (commandTypeStr != null) {
                if (StringUtil.isNull(commandTypeStr)) {
                    return new JsonResultBean(JsonResultBean.FAULT, upErrorFluxsensorType);
                }
                commandType = Integer.parseInt(commandTypeStr);
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fluxSensorBindService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        }
    }

    /**
     * 读取基本参数
     * @return 基本参数
     * @author LiFudong
     */
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView basicInfo(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // 根据车辆id查询车与传感器的绑定
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return mav;
        } catch (Exception e) {
            log.error("基本信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 读取常规参数
     * @return 常规参数
     * @throws BusinessException
     * @author LiFudong
     */
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView generalPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(GENERAL_PAGE);
            // 根据车辆id查询车与传感器的绑定
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return mav;
        } catch (Exception e) {
            log.error("常规参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 读取通讯参数
     * @return 通讯参数
     * @author LiFudong
     */
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView newsletterPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(NEWSLETTER_PAGE);
            // 根据车辆id查询车与传感器的绑定
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return mav;
        } catch (Exception e) {
            log.error("通讯参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 读取私有参数
     * @return 私有参数
     * @author LiFudong
     */
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView parametersPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(PARAMETERS_PAGE);
            // 根据车辆id查询车与传感器的绑定
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return mav;
        } catch (Exception e) {
            log.error("私有参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取F3传感器平台设置常规参数
     * @param id
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/getSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSensorSetting(String id) {
        try {
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            JSONObject msg = new JSONObject();
            msg.put("setting", fuelVehicle);
            if (fuelVehicle != null) {
                return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
            }
            return new JsonResultBean(JsonResultBean.FAULT, deviceInfoNull);
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器数据
     * @param vid
     * @param commandType
     * @param sensorID
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService
                .sendF3SensorParam(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType));
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器私有数据
     * @param vid
     * @param commandStr
     * @param sensorID
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/getF3PrivateParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3PrivateParam(String vid, Integer sensorID, String commandStr) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
            return f3OilVehicleSettingService
                .sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID), commandStr, ipAddress, "2");
        } catch (Exception e) {
            log.error("获取F3传感器私有数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 传感器常规参数修正下发
     * @param setting
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/updateSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSensorSetting(FuelVehicle setting) {
        try {
            String dealType = request.getParameter("deal_type");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateFuelSetting(setting, dealType, ipAddress);
        } catch (Exception e) {
            log.error("传感器常规参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
