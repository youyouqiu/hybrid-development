package com.zw.platform.controller.peripheral;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.f3.SensorConfig;
import com.zw.platform.domain.vas.f3.SensorPolling;
import com.zw.platform.service.sensor.SensorConfigService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LiaoYuecai
 * @date 2017/5/8
 */
@Controller
@RequestMapping("/v/sensorConfig/vehiclePoll")
public class VehiclePollController {
    private static final Logger logger = LogManager.getLogger(VehiclePollController.class);

    private static final String LIST_PAGE = "vas/sensorConfig/vehiclePoll/list";

    private static final String EDIT_PAGE = "vas/sensorConfig/vehiclePoll/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private SensorConfigService sensorConfigService;

    @Resource
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${down.fail}")
    private String downFail;

    @Value("${set.success}")
    private String setSuccess;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * ??????/?????? ????????????
     */
    @RequestMapping(value = { "/edit_{vehicleId}.gsp" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("vehicleId") String vehicleId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            SensorConfig form = sensorConfigService.findByVehicleId(vehicleId);
            mav.addObject("result", form);
            // ??????id????????????
            BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
            if (bindDTO == null) {
                logger.error("??????????????????/????????????????????????:?????????????????????");
                return new ModelAndView(ERROR_PAGE);
            }
            VehicleInfo vehicle = new VehicleInfo();
            vehicle.setId(vehicleId);
            vehicle.setBrand(bindDTO.getName());
            String deviceType = bindDTO.getDeviceType();
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            // ??????????????????????????????
            List<SensorConfig> referVehicleList = sensorConfigService.findVehicleSensorSetting(protocols);
            mav.addObject("vehicle", vehicle);
            String referVehicleListJsonStr = JSON.toJSONString(referVehicleList);
            mav.addObject("referVehicleList", JSON.parseArray(referVehicleListJsonStr));
            if (form != null) {
                String pollingListJsonStr = JSON.toJSONString(form.getPollings());
                mav.addObject("pollingsListJson", JSON.parseArray(pollingListJsonStr));
                mav.addObject("pollingsList", form.getPollings());
            }
            return mav;
        } catch (Exception e) {
            logger.error("??????/????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }

    }

    /**
     * ????????????????????????????????????
     */
    @RequestMapping(value = { "/getPollingParameter_{vehicleId}.gsp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPollingParameter(@PathVariable("vehicleId") String vehicleId) {
        try {
            if (vehicleId != null) {
                SensorConfig form = sensorConfigService.findByVehicleId(vehicleId);
                return new JsonResultBean(JSON.parseArray(JSON.toJSONString(form.getPollings())));
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = { "/delete_{vehicleId}.gsp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteByVehicleId(@PathVariable("vehicleId") String vehicleId) {
        try {
            if (vehicleId != null) {
                return sensorConfigService.deleteByVehicleId(vehicleId);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = { "/deletemore" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteByVehicleId() {
        try {
            String items = request.getParameter("deltems");
            if (StringUtils.isNotBlank(items)) {
                List<String> ids = Arrays.stream(items.split(",")).collect(Collectors.toList());
                return sensorConfigService.deleteBatchByVehicleId(ids);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/edit" }, method = RequestMethod.GET)
    public String edit() {
        return EDIT_PAGE;
    }

    /**
     * ?????????????????????
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addPlant(SensorConfig sensorConfig) {
        try {
            String[] types = request.getParameterValues("sensorType");
            String[] pollingTime = request.getParameterValues("pollingTime");
            if (types.length != pollingTime.length) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            SensorConfig tempConfig = this.sensorConfigService.findByVehicleId(sensorConfig.getVehicleId());
            List<SensorPolling> sensorPollingList = new ArrayList<>();
            for (int i = 0; i < types.length; i++) {
                SensorPolling sensorPolling = new SensorPolling();
                sensorPolling.setPollingTime(Integer.valueOf(pollingTime[i]));
                sensorPolling.setSensorType(types[i]);
                if (tempConfig != null) {
                    sensorPolling.setConfigId(tempConfig.getId());
                }
                sensorPollingList.add(sensorPolling);
            }
            sensorConfig.setPollings(sensorPollingList);
            sensorConfig.setStatus("1");
            sensorConfig.setCreateDataUsername(SystemHelper.getCurrentUsername());
            if (tempConfig == null) {
                sensorConfigService.addSensorConfig(sensorConfig, sensorConfig.getVehicleId());
            } else {
                tempConfig.setPollings(sensorPollingList);
                tempConfig.setUpdateDataUsername(SystemHelper.getCurrentUsername());
                sensorConfigService.updateSensorConfig(tempConfig, sensorConfig.getVehicleId());
            }
            return new JsonResultBean(JsonResultBean.SUCCESS, setSuccess);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????id??????????????????
     */
    @RequestMapping(value = "/sendAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendAlarm(String sendParam) {
        try {
            ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
            if (paramList == null || paramList.size() == 0) {
                return new JsonResultBean(JsonResultBean.FAULT, downFail);
            }
            return sensorConfigService.sendParam(paramList);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????id????????????
     */
    @RequestMapping(value = "/clearPolling", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean clearPolling(String sendParam) {
        try {
            ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
            if (paramList == null || paramList.size() == 0) {
                return new JsonResultBean(JsonResultBean.FAULT, downFail);
            }
            return sensorConfigService.sendClearPolling(paramList);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final SensorConfigQuery query) {
        try {
            return new PageGridBean(query, sensorConfigService.findByPage(query), true);
        } catch (Exception e) {
            logger.error("?????????????????????findByPage?????????", e);
            return new PageGridBean(false);
        }

    }

    /**
     * ???????????????????????????
     */
    @ResponseBody
    @RequestMapping(value = "/refreshSendStatus", method = RequestMethod.GET)
    public JsonResultBean refreshSendStatus(String vehicleId) {
        try {
            SensorConfig sensorConfig = sensorConfigService.getSendWebSocket(vehicleId);
            if (sensorConfig != null) {
                return new JsonResultBean(sensorConfig);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
            return new JsonResultBean(false);
        }

    }

    /**
     * ????????????????????????????????????
     */
    @ResponseBody
    @RequestMapping(value = "/getDirectiveStatus")
    public JsonResultBean getDirectiveStatus(String vehicleId, Integer swiftNumber) {
        try {
            Directive directive = sensorConfigService.getDirectiveStatus(vehicleId, swiftNumber);
            if (directive == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            return new JsonResultBean(directive.getStatus());
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

}
