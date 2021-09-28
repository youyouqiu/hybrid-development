package com.zw.platform.controller.oilmassmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.query.OilVehicleSettingQuery;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.service.oilmassmgt.OilVehicleSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.common.ZipUtil;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 油量车辆设置Controller <p> Title: OilVehicleSettingController.java </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company:
 * ZhongWei </p> <p> team: ZhongWeiTeam </p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月24日下午4:26:13
 */
@Controller
@RequestMapping("/v/oilmassmgt/oilvehiclesetting")
public class OilVehicleSettingController {

    private static DecimalFormat dfInt = new DecimalFormat("#"); // 整数

    private static DecimalFormat df_1 = new DecimalFormat("#.#"); // 保留一位小数

    private static Logger log = LogManager.getLogger(OilVehicleSettingController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${vehicle.bound.oilsensor}")
    private String vehicleBoundOilsensor;

    @Value("${data.relieve.bound}")
    private String dataRelieveBound;

    @Value("${set.relieve.bound}")
    private String setRelieveBound;

    @Value("${terminal.off.line}")
    private String terminalOffLine;

    @Value("${device.info.null}")
    private String deviceInfoNull;

    @Value("${up.error}")
    private String upError;

    @Value("${up.error.oil.type}")
    private String upErrorOilType;

    @Autowired
    private OilVehicleSettingService oilVehicleSettingService;

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AlarmSettingService alarmSettingService;

    private static final String LIST_PAGE = "vas/oilmassmgt/oilvehiclesetting/list";

    private static final String BIND_PAGE = "vas/oilmassmgt/oilvehiclesetting/bind";

    private static final String EDIT_PAGE = "vas/oilmassmgt/oilvehiclesetting/edit";

    private static final String DETAIL_PAGE = "vas/oilmassmgt/oilvehiclesetting/detail";

    private static final String BASICINFO_PAGE = "vas/oilmassmgt/oilvehiclesetting/basicInfo"; // 基本信息

    private static final String GENERAL_PAGE = "vas/oilmassmgt/oilvehiclesetting/general"; // 常规参数

    private static final String NEWSLETTER_PAGE = "vas/oilmassmgt/oilvehiclesetting/newsletter"; // 通讯参数

    private static final String CALIBRATION_PAGE = "vas/oilmassmgt/oilvehiclesetting/calibration"; // 标定数据

    private static final String PARAMETERS_PAGE = "vas/oilmassmgt/oilvehiclesetting/parameters"; // 私有参数

    private static final String UPGRADE_PAGE = "vas/oilmassmgt/oilvehiclesetting/upgrade"; // 远程升级

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * @return 远程升级
     * @throws BusinessException
     * @author angbike
     */
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView upgradePage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(UPGRADE_PAGE);
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            log.error("远程升级弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * @return 私有参数
     * @author angbike
     */
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView parametersPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(PARAMETERS_PAGE);
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            log.error("私有参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * @return 标定数据
     * @author angbike
     */
    @RequestMapping(value = { "/calibration_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView calibrationPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(CALIBRATION_PAGE);
            // 根据id查询车与油箱的绑定
            OilVehicleSetting oilSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (null != oilSetting) {
                oilSetting.setOilBoxType("4" + oilSetting.getOilBoxType());
                VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(oilSetting.getVId());
                oilSetting.setBrand(vehicle.getBrand());
                mav.addObject("result", oilSetting);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            log.error("标定数据弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * @return 通讯参数
     * @author angbike
     */
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView newsletterPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(NEWSLETTER_PAGE);
            // 根据id查询车绑定
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            log.error("通讯参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * @return 常规参数
     * @throws BusinessException
     * @author angbike
     */
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView generalPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(GENERAL_PAGE);
            // 根据id查询车绑定
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            log.error("常规参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * @return 基本信息
     * @author angbike
     */
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView basicInfo(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // 根据id查询车与油箱的绑定
            OilVehicleSetting oilSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilSetting != null) {
                oilSetting.setOilBoxType("4" + oilSetting.getOilBoxType());
                mav.addObject("result", oilSetting);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            log.error("基本信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final OilVehicleSettingQuery query) {
        try {
            if (query != null) {
                Page<OilVehicleSetting> result = oilVehicleSettingService.findOilVehicleList(query);
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            log.error("分页查询分组（findOilVehicleList）异常", e);
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
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilVehicleByVid(vehicleId);
            if (oilVehicleSetting != null) {
                return new JsonResultBean(oilVehicleSetting);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("油量刷新参数下发状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 设置 TODO
     * @param id
     * @return ModelAndView
     * @throws BusinessException
     * @throws @Title:           bindPage
     * @author wangying
     */
    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true) // 防止表单重复提交
    public ModelAndView bindPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            // 查询车
            BindDTO configList = VehicleUtil.getBindInfoByRedis(id);
            VehicleInfo vehicle = new VehicleInfo();
            vehicle.setId(configList.getId());
            vehicle.setBrand(configList.getName());
            String deviceType = configList.getDeviceType();
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            // 查询参考车辆
            List<DoubleOilVehicleSetting> vehicleList =
                oilVehicleSettingService.findReferenceVehicleByProtocols(protocols);
            // 查询油箱
            List<FuelTank> fuelTankList = oilVehicleSettingService.findFuelTankList();
            // 查询油杆
            List<RodSensor> rodSensorList = oilVehicleSettingService.findRodSensorList();
            mav.addObject("vehicle", vehicle);
            mav.addObject("fuelTankList", JSON.toJSONString(fuelTankList));
            mav.addObject("rodSensorList", JSON.toJSONString(rodSensorList));
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            mav.addObject("id", UUID.randomUUID().toString());
            mav.addObject("id2", UUID.randomUUID().toString());
            return mav;
        } catch (Exception e) {
            log.error("绑定传感器弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * TODO 油箱车辆绑定
     * @param bean
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @Title:           bind
     * @author wangying
     */
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true) // 防止表单重复提交
    public JsonResultBean bind(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final DoubleOilVehicleSetting bean,
        final BindingResult bindingResult) {
        try {
            if (bean != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 根据车辆id 删除绑定关系（避免同时操作）
                    if (StringUtils
                        .isNotBlank(oilVehicleSettingService.findOilVehicleSettingByVid(bean.getVehicleId()).getId())) {
                        return new JsonResultBean(JsonResultBean.FAULT, vehicleBoundOilsensor);
                    }
                    String ipAddress = new GetIpAddr().getIpAddr(request); // 获取客户端的IP地址
                    // 新增绑定表
                    return oilVehicleSettingService.addFuelTankBind(bean, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("油箱车辆绑定异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/edit_{vId}_{type}.gsp" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("vId") final String vehicleId, @PathVariable("type") String type,
        HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // 查询已绑定的车
            BindDTO configList = VehicleUtil.getBindInfoByRedis(vehicleId);
            String deviceType = configList.getDeviceType();
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            List<DoubleOilVehicleSetting> vehicleList =
                oilVehicleSettingService.findReferenceVehicleByProtocols(protocols);
            // 查询油箱
            List<FuelTank> fuelTankList = oilVehicleSettingService.findFuelTankList();
            // 根据车辆id查询车与传感器的绑定
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(vehicleId);
            // 查询油杆
            List<RodSensor> rodSensorList = oilVehicleSettingService.findRodSensorList();
            if (StringUtils.isBlank(oilSetting.getId2())) { // 双油箱
                oilSetting.setNewId2(UUID.randomUUID().toString());
            }
            // 根据油量车辆设置表id读取油箱标定数据
            getOilCalibrationList(oilSetting);
            if (StringUtils.isNotBlank(oilSetting.getId())) {
                mav.addObject("fuelTankList", JSON.toJSONString(fuelTankList));
                mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
                mav.addObject("rodSensorList", JSON.toJSONString(rodSensorList));
                mav.addObject("result", oilSetting);
                mav.addObject("oilBoxType", type);
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
            log.error("修改油量车辆设置弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 查询标定数据
     * @param oilSetting
     * @return void
     * @throws BusinessException
     * @throws @Title:           getOilCalibrationList
     * @author Liubangquan
     */
    private void getOilCalibrationList(DoubleOilVehicleSetting oilSetting) {
        try {
            if (StringUtils.isNotBlank(oilSetting.getOilBoxId())) { // 油箱1标定数据
                // 读取油箱标定数据
                List<OilCalibrationForm> list = fuelTankManageService.getOilCalibrationList(oilSetting.getId());
                if (null != list && list.size() > 0) {
                    for (OilCalibrationForm ocf : list) {
                        ocf.setOilLevelHeight(df_1.format(Converter.toDouble(ocf.getOilLevelHeight())));
                        ocf.setOilValue(df_1.format(Converter.toDouble(ocf.getOilValue())));
                    }
                }
                if (null != list && list.size() > 0) {
                    StringBuilder oilLevelHeights = new StringBuilder();
                    StringBuilder oilValues = new StringBuilder();
                    for (OilCalibrationForm of : list) {
                        oilLevelHeights.append(of.getOilLevelHeight()).append(",");
                        oilValues.append(of.getOilValue()).append(",");
                    }

                    oilSetting.setOilLevelHeights(StrUtil.getFinalStr(oilLevelHeights));
                    oilSetting.setOilValues(StrUtil.getFinalStr(oilValues));

                }
            }
            if (StringUtils.isNotBlank(oilSetting.getOilBoxId2())) { // 油箱2标定数据
                // 读取油箱标定数据
                List<OilCalibrationForm> list = fuelTankManageService.getOilCalibrationList(oilSetting.getId2());
                if (null != list && list.size() > 0) {
                    for (OilCalibrationForm ocf : list) {
                        ocf.setOilLevelHeight(df_1.format(Converter.toDouble(ocf.getOilLevelHeight())));
                        ocf.setOilValue(df_1.format(Converter.toDouble(ocf.getOilValue())));
                    }
                }
                if (null != list && list.size() > 0) {
                    StringBuilder oilLevelHeights = new StringBuilder();
                    StringBuilder oilValues = new StringBuilder();
                    for (OilCalibrationForm of : list) {
                        oilLevelHeights.append(of.getOilLevelHeight()).append(",");
                        oilValues.append(of.getOilValue()).append(",");
                    }
                    oilSetting.setOilLevelHeights2(StrUtil.getFinalStr(oilLevelHeights));
                    oilSetting.setOilValues2(StrUtil.getFinalStr(oilValues));

                }
            }
        } catch (Exception e) {
            log.error("查询标定数据异常", e);
        }

    }

    /**
     * TODO 修改油箱车辆设置
     * @param bean
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @Title:           edit
     * @author wangying
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final DoubleOilVehicleSetting bean,
        final BindingResult bindingResult) {
        try {
            if (bean != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    if (oilVehicleSettingService.findOilBoxVehicleByBindId(bean.getId()) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, dataRelieveBound);
                    }
                    String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                    // 修改绑定
                    return oilVehicleSettingService.updateOilVehicleSetting(bean, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改油箱车辆设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id删除绑定
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (!"".equals(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return oilVehicleSettingService.deleteFuelTankBindById(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("删除绑定设置异常", e);
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
            if (!"".equals(items)) {
                String ipAddress = new GetIpAddr().getIpAddr(request); // 获得访问ip
                return oilVehicleSettingService.deleteFuelTankBindById(items, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }

        } catch (Exception e) {
            log.error("批量删除绑定设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         * @param id
     * @return ModelAndView
     * @throws BusinessException
     * @throws @Title:           详情
     * @author wangying
     */
    @RequestMapping(value = { "/detail_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView detailPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            // 根据车辆id查询车与油箱的绑定
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(id);
            if (null != oilSetting) {
                VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
                oilSetting.setBrand(vehicle.getBrand());
            }
            // 根据油量车辆设置表id读取油箱标定数据
            getOilCalibrationList(oilSetting);
            mav.addObject("result", oilSetting);
            return mav;
        } catch (Exception e) {
            log.error("删除油量车辆设置弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
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
            if (!"".equals(vid) && !"".equals(commandStr) && sensorID != 0) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return f3OilVehicleSettingService
                    .sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID), commandStr, ip, "1");
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("获取F3传感器私有数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取邮箱标定数据
     * @param id
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/getOilCalibrationList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilCalibrationList(String id, Integer sensorID) {
        JSONObject msg = new JSONObject();
        try {
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(id);
            if (oilSetting == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            // 根据油量车辆设置表id读取油箱标定数据
            getOilCalibrationList(oilSetting);

            List<Map<String, String>> list = new ArrayList<>();
            if (sensorID == 65) {
                for (int i = 0; i < oilSetting.getOilLevelHeights().split(",").length; i++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("oilLevelHeight", String.valueOf(oilSetting.getOilLevelHeights().split(",")[i]));
                    map.put("oilValue", String.valueOf(oilSetting.getOilValues().split(",")[i]));
                    list.add(map);
                }
            } else {
                if (oilSetting.getOilValues2() != null && oilSetting.getOilValues2().split(",").length > 1) {
                    list = new ArrayList<>();
                    for (int i = 0; i < oilSetting.getOilLevelHeights2().split(",").length; i++) {
                        Map<String, String> map = new HashMap<>();
                        map.put("oilLevelHeight", String.valueOf(oilSetting.getOilLevelHeights2().split(",")[i]));
                        map.put("oilValue", String.valueOf(oilSetting.getOilValues2().split(",")[i]));
                        list.add(map);
                    }
                }
            }

            msg.put("settingList", list);
            msg.put("setting", oilSetting);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());

        } catch (Exception e) {
            log.error("获取邮箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 获取F3传感器数据
     * @param id
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/getSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSensorSetting(String id, String queryType) {
        try {
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            JSONObject msg = new JSONObject();
            msg.put("setting", oilVehicleSetting);
            if (oilVehicleSetting != null) {
                return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
            }
            return new JsonResultBean(JsonResultBean.FAULT, deviceInfoNull);
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 外设软件升级
     * @return ModelAndView
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @author FanLu
     */
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(WirelessUpdateParam wirelessParam, String vehicleId, final BindingResult bindingResult) {
        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        try {
            Integer commandType = 13141;
            try {
                String commandTypeStr = request.getParameter("commandType");
                if (StringUtil.isNull(commandTypeStr)) {
                    return new JsonResultBean(JsonResultBean.FAULT, upErrorOilType);
                }
                commandType = Integer.parseInt(commandTypeStr);
            } catch (Exception ex) {
                return new JsonResultBean(JsonResultBean.FAULT, upErrorOilType);

            }
            String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 0);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
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
    public JsonResultBean updateSensorSetting(OilVehicleSetting setting) {
        try {
            String dealType = request.getParameter("deal_type");
            if (!"".equals(dealType) && setting != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
                return f3OilVehicleSettingService.updateRoutineSetting(setting, dealType, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("传感器常规参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 传感器标定参数修正下发
     * @param setting
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/updateDemarcateSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateDemarcateSetting(OilVehicleSetting setting) {
        try {
            String[] oilLevelHeight = request.getParameterValues("oilLevelHeight");
            String[] oilValue = request.getParameterValues("oilValue");
            String type = request.getParameter("deal_type");
            List<OilCalibrationForm> list = new ArrayList<>();
            if (type.equals("report")) { // 以上报为准
                for (int i = 0; i < oilLevelHeight.length; i++) {
                    OilCalibrationForm form = new OilCalibrationForm();
                    form.setOilBoxVehicleId(setting.getId());
                    form.setOilLevelHeight(oilLevelHeight[i]);
                    form.setOilValue(oilValue[i]);
                    form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                    list.add(form);
                }
            }
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return f3OilVehicleSettingService.updateDemarcateSetting(list, setting, ip);
        } catch (Exception e) {
            log.error("传感器标定参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下发油箱参数(包括批量下发)
     */
    @RequestMapping(value = "/sendOil", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendOil(String sendParam) {
        try {
            ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
            if (paramList != null && paramList.size() > 0) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                oilVehicleSettingService.sendOil(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("下发油箱参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据车辆Id查询车与油箱的绑定信息
     * @param vehicleId 设置的监控对象id
     */
    @RequestMapping(value = "/getBindInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean referenceBrandSet(String vehicleId) {
        try {
            if (!vehicleId.isEmpty()) {
                JSONObject msg = new JSONObject();
                // 根据车辆id查询车与油箱的绑定
                DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(vehicleId);
                msg.put("oilSetting", oilSetting);
                return new JsonResultBean(oilSetting);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据车辆Id查询车与油箱的绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    // ----------------------------油量里程报表组织树接口-----------------------------------------------

    /**
     * 获取设置了油量管理设置的监控对象树
     */
    @RequestMapping(value = "/oilSetMonitorTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilSettingVehicleTree() {
        try {
            String result = vehicleService.getOilVehicleSettingMonitorTree().toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取设置了油量管理设置的监控对象树异常", e);
            return null;
        }
    }

    /**
     * 根据分组id查询设置了油量管理设置的监控对象(组装成树节点的形式)
     */
    @RequestMapping(value = "/oilSetMonitorTreeByAssignment", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilSettingVehicleByAssign(String assignmentId, boolean isChecked, String type) {
        try {
            JSONArray result = vehicleService.getOilVehicleSetMonitorByAssign(assignmentId, isChecked, type);
            // 压缩数据
            String tree = ZipUtil.compress(result.toJSONString());
            return new JsonResultBean(tree);
        } catch (Exception e) {
            log.error("获取油量里程报表信息异常", e);
            return null;
        }
    }

    /**
     * 组织树的模糊搜索(组织树显示的车辆，为【油量管理设置】中设置了参数的车辆)
     */
    @RequestMapping(value = "/oilSetMonitorTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilSettingVehicleByFuzzy(String param, String queryPattern) {
        try {
            JSONArray result = vehicleService.getOilVehicleSetMonitorByFuzzy(param, queryPattern);
            // 压缩数据
            String tree = ZipUtil.compress(result.toJSONString());
            return new JsonResultBean(tree);
        } catch (Exception e) {
            log.error("油量里程报表组织树模糊搜索异常", e);
            return null;
        }
    }

    /**
     * 根据父节点获取父节点下的监控对象数量
     * @param parId
     * @param type
     * @return
     */
    @RequestMapping(value = "/oilSetMonitorNumberCountByParId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilSettingMonitorNumberByParId(String parId, String type) {
        try {
            int monitorNumber = vehicleService.getSensorVehicleNumberByPid(parId, type);
            return new JsonResultBean(monitorNumber);
        } catch (Exception e) {
            log.error("获取组织下的车id异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据组织id查询车辆（组装成树节点的格式）
     */
    @RequestMapping(value = "/oilSetMonitorByGroupId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilSetMonitorByGroupId(String groupId, String type, boolean isChecked) {
        try {
            Map<String, JSONArray> result = vehicleService.getSensorVehicleByGroupId(groupId, type, isChecked);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("根据组织id查询车辆异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
    //----------------------------油量里程报表组织树接口-----------------------------------------------
}
