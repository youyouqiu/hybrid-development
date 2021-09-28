package com.zw.platform.controller.OBDManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.OBDManagerSettingForm;
import com.zw.platform.domain.basicinfo.form.OBDVehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.OBDManagerSettingQuery;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.statistic.info.FaultCodeInfo;
import com.zw.platform.service.obdManager.OBDManagerSettingService;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * OBD管理设置
 * create by denghuabing 2018.12.27
 */
@Controller
@RequestMapping("/v/obdManager/obdManagerSetting")
public class OBDManagerSettingController {

    private final Logger logger = LogManager.getLogger(OBDManagerSettingController.class);

    @Autowired
    private OBDManagerSettingService obdManagerSettingService;

    @Autowired
    private OBDVehicleTypeService obdVehicleTypeService;

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    private static final String LIST_PAGE = "vas/obdManager/obdManagerSetting/list";

    private static final String ADD_PAGE = "vas/obdManager/obdManagerSetting/add";

    private static final String EDIT_PAGE = "vas/obdManager/obdManagerSetting/edit";

    private static final String DETAILS_PAGE = "vas/obdManager/obdManagerSetting/detail";

    private static final String BASE_PAGE = "vas/obdManager/obdManagerSetting/baseInfo";

    private static final String FAULT_PAGE = "vas/obdManager/obdManagerSetting/fault";

    private static final String PARAMETER_PAGE = "vas/obdManager/obdManagerSetting/parameter";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Auth
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(OBDManagerSettingQuery obdManagerSettingQuery) {
        try {
            if (obdManagerSettingQuery != null) {
                Page<OBDManagerSettingForm> list = obdManagerSettingService.findList(obdManagerSettingQuery);
                return new PageGridBean(list, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("OBD设置管理页面异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 刷新参数下发状态
     */
    @RequestMapping(value = { "/refreshSendStatus" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean refreshSendStatus(String vehicleId) {
        try {
            OBDManagerSettingForm obdManagerSettingForm = obdManagerSettingService.findByVid(vehicleId);
            if (obdManagerSettingForm != null) {
                return new JsonResultBean(obdManagerSettingForm);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("obd刷新参数下发状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }


    @RequestMapping(value = "/add_{vid}", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView getBindPage(@PathVariable("vid") String vid) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            List<Integer> protocols = setVehicleInfo(mav, vid);
            getReferent(mav, vid, protocols);
            getObdType(mav);
            return mav;
        } catch (Exception e) {
            logger.error("OBD设置管理设置页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addObdManagerSetting(OBDManagerSettingForm form) {
        try {
            if (form != null) {
                List<OBDManagerSettingForm> obdManagerSettingForm =
                    obdManagerSettingService.findObdSettingByVid(form.getVehicleId());
                if (obdManagerSettingForm != null && obdManagerSettingForm.size() > 0) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该车OBD管理已设置,请刷新！");
                }
                return obdManagerSettingService.addObdManagerSetting(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("设置OBD管理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/edit_{id}_{vid}", method = RequestMethod.GET)
    public ModelAndView getEditPage(@PathVariable("id") String id, @PathVariable("vid") String vid) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            //查询当前绑定信息
            OBDManagerSettingForm form = obdManagerSettingService.findObdSettingById(id);
            mav.addObject("result", form);
            List<Integer> protocols = setVehicleInfo(mav, vid);
            getReferent(mav, vid, protocols);
            getObdType(mav);
            return mav;
        } catch (Exception e) {
            logger.error("OBD设置管理修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updateObdManagerSetting(OBDManagerSettingForm form) {
        try {
            if (form != null) {
                return obdManagerSettingService.updateObdManagerSetting(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改OBD设置管理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 设置车辆信息
     */
    private List<Integer> setVehicleInfo(ModelAndView mav, String id) {
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(id);
        if (bindDTO == null) {
            return new ArrayList<>();
        }
        VehicleInfo vehicle = new VehicleInfo();
        vehicle.setId(id);
        vehicle.setBrand(bindDTO.getName());
        String deviceType = bindDTO.getDeviceType();

        mav.addObject("vehicleInfo", vehicle);
        return ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
    }

    private void getReferent(ModelAndView mav, String vid, List<Integer> protocols) {
        //查询参考对象
        List<OBDManagerSettingForm> referent = obdManagerSettingService.getReferentInfo(vid, protocols);
        mav.addObject("referent", JSONObject.toJSONString(referent));
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteObdManagerSetting(String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                return obdManagerSettingService.deleteObdManagerSetting(id);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除OBD设置管理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 详情页面
     */
    @RequestMapping(value = "/detail_{id}_{vid}.gsp", method = RequestMethod.GET)
    public ModelAndView getDetailPage(@PathVariable("id") String id, @PathVariable("vid") String vid) {
        try {
            ModelAndView mav = new ModelAndView(DETAILS_PAGE);
            //查询当前绑定信息
            OBDManagerSettingForm form = obdManagerSettingService.findObdSettingById(id);
            mav.addObject("result", form);
            setVehicleInfo(mav, vid);
            return mav;
        } catch (Exception e) {
            logger.error("获取OBD设置管理详情页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 下发obd参数
     */
    @RequestMapping(value = "/sendOBDParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendObdParam(String sendParam) {
        try {
            if (StringUtils.isNotBlank(sendParam)) {
                List<JSONObject> list = JSON.parseObject(sendParam, ArrayList.class);
                obdManagerSettingService.sendObdParam(list);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("下发OBD参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取OBD
     * @param vid         vid
     * @param commandType OBD基本信息 0xF0
     * @param sensorID    外设ID : E5
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            if (StringUtils.isBlank(vid) || commandType == null || sensorID == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            if (commandType == 0xF0) {
                return f3OilVehicleSettingService
                    .sendF3SensorParam(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType));
            } else {
                return obdManagerSettingService.sendObdInfo(vid, commandType);
            }
        } catch (Exception e) {
            logger.error("获取OBD数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 基本信息页面
     */
    @RequestMapping(value = "/base_{id}_{vid}", method = RequestMethod.GET)
    public ModelAndView getBasePage(@PathVariable("id") String id, @PathVariable("vid") String vid) {
        try {
            ModelAndView mav = new ModelAndView(BASE_PAGE);
            //查询当前绑定信息
            OBDManagerSettingForm form = obdManagerSettingService.findObdSettingById(id);
            mav.addObject("result", form);
            setVehicleInfo(mav, vid);
            return mav;
        } catch (Exception e) {
            logger.error("OBD设置管理修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 查看obd参数页面
     */
    @RequestMapping(value = "/findOBDParameter_{vid}", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView findObdParameter(@PathVariable("vid") String vid) {
        try {
            ModelAndView mav = new ModelAndView(PARAMETER_PAGE);
            setVehicleInfo(mav, vid);
            return mav;
        } catch (Exception e) {
            logger.error("查看OBD参数异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取obd参数
     */
    @RequestMapping(value = "/getParameter", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getParameter(String vid) {
        try {
            return obdManagerSettingService.getCacheObd(vid);
        } catch (Exception e) {
            logger.error("获取OBD参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取故障码
     */
    @RequestMapping(value = "/getFaultPage_{vid}", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getFaultPage(@PathVariable("vid") String vid) {
        try {
            ModelAndView mav = new ModelAndView(FAULT_PAGE);
            setVehicleInfo(mav, vid);
            FaultCodeInfo info = obdManagerSettingService.findFaultCodeByVid(vid);
            if (info == null) {
                info = new FaultCodeInfo();
            }
            mav.addObject("result", info);
            return mav;
        } catch (Exception e) {
            logger.error("获取OBD故障码异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取车辆类型树
     */
    private void getObdType(ModelAndView mav) {
        List<OBDVehicleTypeForm> list = obdVehicleTypeService.findAll();
        mav.addObject("type", JSONObject.toJSONString(list));
    }
}
