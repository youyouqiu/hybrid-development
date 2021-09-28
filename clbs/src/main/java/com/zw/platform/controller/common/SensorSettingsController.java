package com.zw.platform.controller.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfigQuery;
import com.zw.platform.domain.vas.workhourmgt.SensorSettingInfo;
import com.zw.platform.service.sensorSettings.SensorSettingsService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Controller
@RequestMapping("/v/sensorSettings")
public class SensorSettingsController {
    private static Logger log = LogManager.getLogger(SensorSettingsController.class);

    @Autowired
    private SensorSettingsService sensorSettingsService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${down.fail}")
    private String downFail;

    /**
     * 分页查询
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final MileageSensorConfigQuery query, int sensorType) {
        try {
            Page<TransdusermonitorSet> result = sensorSettingsService.findTransduserByType(query, sensorType);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询（findTransduserByType）异常", e);
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
    public JsonResultBean refreshSendStatus(String vehicleId, int sensorType) {
        try {
            TransdusermonitorSet transdusermonitorSet
                = sensorSettingsService.findWorkHourSettingByVid(vehicleId, sensorType);
            if (transdusermonitorSet != null) {
                return new JsonResultBean(transdusermonitorSet);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("温，湿度正反转刷新参数下发状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 新增传感器
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @AvoidRepeatSubmitToken(removeToken = true) // 防止表单重复提交
    public JsonResultBean addTransdusermonitorSet(String[] sensorList, int sensorType) {
        try {
            if (sensorList != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return sensorSettingsService.addTransdusermonitorSet(sensorList, sensorType, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增温/湿/正反转传感器设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改传感器
     */
    @ResponseBody
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(String[] sensorList, String vid, int type) {
        try {
            if (sensorList != null && vid != null) {
                /*
                 * // 删除车辆与传感器的绑定关系 sensorSettingsService.deleteAllBind(vid, type); // 新增车辆与传感器的绑定关系
                 * sensorSettingsService.addBatchTransdusermonitorSet(ts);
                 */
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                boolean result = sensorSettingsService.updateSensorSetting(vid, type, sensorList, ip);
                if (result) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改温/湿/正反转传感器信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据车辆id解除与传感器的绑定关系
     * @param id
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/delete_{id}", method = RequestMethod.POST)
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                List<String> ids = Arrays.asList(id);
                return sensorSettingsService.deleteSensorVehicle(ids, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("解除与温/湿/正反转传感器的绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量解除车辆与传感器绑定关系
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    public JsonResultBean delMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return sensorSettingsService.deleteSensorVehicle(ids, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量解除车辆与温/湿/正反转传感器绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询该车辆绑定的传感器类型和信息
     * @param id
     * @param sensorType
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/findVehicleBrand", method = RequestMethod.POST)
    public List<TransdusermonitorSet> findVehicleBrandByType(String id, int sensorType) {
        try {
            List<TransdusermonitorSet> list = sensorSettingsService.findByVehicleId(sensorType, id);
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据id下发参数设置
     */
    @RequestMapping(value = "/sendSetDeviceParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendTemperature(String sendParam, int sensorType, HttpServletRequest request,
        Integer flag) {
        try {
            ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
            if (paramList == null || paramList.size() == 0) {
                return new JsonResultBean(JsonResultBean.FAULT, downFail);
            }
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            this.sensorSettingsService.sendSetDeviceParam(paramList, sensorType, ip, flag);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("下发温/湿/正反转传感器参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取传感器
     * @param sensorType 1温度传感器  2湿度传感器  3正反转传感器 4工时传感器
     * @param detectionMode 检测方式(1:电压比较式;2:油耗流量计式)
     * @return list
     */
    @RequestMapping(value = "/findSensorInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findSensorInfo(final String sensorType, final String detectionMode) {
        try {
            List<SensorSettingInfo> list = sensorSettingsService.findSensorInfo(detectionMode, sensorType);
            return new JsonResultBean(list);
        } catch (Exception e) {
            log.error("获取传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }
}
