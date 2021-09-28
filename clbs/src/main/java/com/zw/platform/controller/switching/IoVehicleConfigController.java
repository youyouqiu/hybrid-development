package com.zw.platform.controller.switching;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zw.platform.domain.vas.switching.IoVehicleConfig;
import com.zw.platform.service.switching.IoVehicleConfigService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangsq
 * @date 2018/6/28 10:20
 */
@Controller
@RequestMapping("/m/io/config")
public class IoVehicleConfigController {

    private static final Logger log = LogManager.getLogger(IoVehicleConfigController.class);

    @Autowired
    private IoVehicleConfigService ioVehicleConfigService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @ResponseBody
    @RequestMapping("/binds")
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean binds(String ioVehicleConfigStr) {
        try {
            List<IoVehicleConfig> ioVehicleConfigs =
                    JSON.parseObject(ioVehicleConfigStr, new TypeReference<ArrayList<IoVehicleConfig>>() { });
            if (ioVehicleConfigs != null && ioVehicleConfigs.size() > 0) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return ioVehicleConfigService.addIoConfigs(ioVehicleConfigs, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "请至少选一项");
        } catch (Exception e) {
            log.error("设置信号位异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ResponseBody
    @RequestMapping("/updateBinds")
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updateBinds(String ioVehicleConfigStr, String delIds, String vehicleId) {
        try {
            List<IoVehicleConfig> ioVehicleConfigs =
                    JSON.parseObject(ioVehicleConfigStr, new TypeReference<ArrayList<IoVehicleConfig>>() {
                    });
            if (ioVehicleConfigs != null && ioVehicleConfigs.size() > 0) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return ioVehicleConfigService.updateIoConfigs(ioVehicleConfigs, delIds, ip, vehicleId);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "请至少选一项");
        } catch (Exception e) {
            log.error("设置信号位异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取车辆已绑定的IO列表
     *
     * @param vehicleId
     * @return
     */
    @ResponseBody
    @RequestMapping("/getVehicleBindIos")
    public JsonResultBean getVehicleBindIos(String vehicleId) {
        try {

            Map<String, List<Map>> stringListMap = new HashMap<>();
            stringListMap.put("deviceIos", ioVehicleConfigService.getVehicleBindIos(vehicleId, 1));
            stringListMap.put("collectionOneIos", ioVehicleConfigService.getVehicleBindIos(vehicleId, 2));
            stringListMap.put("collectionTwoIos", ioVehicleConfigService.getVehicleBindIos(vehicleId, 3));
            return new JsonResultBean(stringListMap);
        } catch (Exception e) {
            log.error("设置信号位异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }


}
